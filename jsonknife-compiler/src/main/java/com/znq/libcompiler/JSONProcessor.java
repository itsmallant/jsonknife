package com.znq.libcompiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.znq.nanotation.Exclude;
import com.znq.nanotation.GenerateName;
import com.znq.nanotation.JSONAble;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @desc:
 * @author: ningqiang.zhao
 * @time: 2020-12-14 18:09
 **/
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.znq.nanotation.JSONAble")
public class JSONProcessor extends AbstractProcessor {
    private static final String TAG = "JSONProcessor";

    private static final int TYPE_JSONARRAY = 1;

    private static final int TYPE_JSONOBJECT = 2;

    private static final String NAME_JSON_FACTORY = "JSONFactory";

    private final static String NAME_GEN_JSON_OBJECT = "genJSONObject";

    private static final ClassName CLASSNAME_JSONOBJECT = ClassName.get("org.json", "JSONObject");
    private static final ClassName CLASSNAME_JSONARRAY = ClassName.get(" org.json", "JSONArray");
    private static final ClassName CLASSNAME_JSONEXCEPTION = ClassName.get("org.json", "JSONException");
    private static final ClassName CLASSNAME_ENTRY = ClassName.get("java.util", "Map");

    private static final String TYPE_STRING = String.class.getCanonicalName();

    /*private*/static Messager messager;
    private Filer filer;
    /* private*/ static Elements elementUtils;
    /*private*/ static Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> JSONAbleElement = roundEnvironment.getElementsAnnotatedWith(JSONAble.class);
        if (!JSONAbleElement.isEmpty()) {
            handleJSONAble(JSONAbleElement);
        }
        return true;
    }

    private void handleJSONAble(Set<? extends Element> elements) {
        Map<String, List<TypeElement>> packageNameJSONAbleElements = generatePackageNameJSONAbleElementsMap(elements);

        Map<String, List<MethodSpec>> packageNameGenJSONMethods = mappingToPackageNameGenJSONMethods(packageNameJSONAbleElements);

        for (Map.Entry<String, List<MethodSpec>> entry : packageNameGenJSONMethods.entrySet()) {
            generateJSONFactory(entry.getKey(), entry.getValue());
        }
    }


    private Map<String, List<MethodSpec>> mappingToPackageNameGenJSONMethods(Map<String, List<TypeElement>> packageNameJSONAbleElements) {
        Map<String, List<MethodSpec>> result = new HashMap<>();
        for (Map.Entry<String, List<TypeElement>> entry : packageNameJSONAbleElements.entrySet()) {
            List<MethodSpec> methodList = new ArrayList<>();
            result.put(entry.getKey(), methodList);
            for (TypeElement typeElement : entry.getValue()) {
                MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(NAME_GEN_JSON_OBJECT)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addParameter(ClassName.get(typeElement.asType()), "source")
                        .addException(CLASSNAME_JSONEXCEPTION)
                        .returns(CLASSNAME_JSONOBJECT);
                methodSpecBuilder.addStatement("if(source == null) return null");
                methodSpecBuilder.addStatement("JSONObject json = new JSONObject()");
                List<? extends Element> allMembers = elementUtils.getAllMembers(typeElement);
                for (Element member : allMembers) {
                    if (member.getKind().isField()) {
                        if (member.getAnnotation(Exclude.class) != null) {
                            continue;
                        }
                        Set<Modifier> modifiers = member.getModifiers();
                        if (modifiers.contains(Modifier.TRANSIENT) || modifiers.contains(Modifier.STATIC)) {
                            continue;
                        }

                        String keyName;
                        GenerateName generateName = member.getAnnotation(GenerateName.class);
                        if (generateName != null) {
                            keyName = generateName.value();
                        } else {
                            keyName = member.getSimpleName().toString();
                        }

                        // Verify that the type is a List or an array.
                        TypeMirror memberType = member.asType();
                        if (memberType.getKind() == TypeKind.ARRAY) {
                            ArrayType arrayType = (ArrayType) memberType;
                            TypeMirror itemTypeMirror = arrayType.getComponentType();
                            boolean isJSONAble = false;
                            if (itemTypeMirror != null) {
                                TypeElement componentType = Utils.getType(elementUtils, itemTypeMirror);
                                if (componentType != null && componentType.getAnnotation(JSONAble.class) != null) {
                                    isJSONAble = true;
                                }
                            }
                            if (isJSONAble) {
                                Element fieldGetElement = findFieldGetElement(allMembers, member, modifiers);
                                methodSpecBuilder.addStatement("$T $L = source.$L", member, member, fieldGetElement);
                                TypeElement componentType = Utils.getType(elementUtils, itemTypeMirror);
                                methodSpecBuilder.addStatement("$T $L_jsonArray = new JSONArray()", CLASSNAME_JSONARRAY, member);
                                methodSpecBuilder.addCode("for ($T item : $L){\n", componentType, member);
                                String code = String.format("$L_jsonArray.put(%s.%s.%s(item))", Utils.getPackageName(elementUtils, componentType), NAME_JSON_FACTORY, NAME_GEN_JSON_OBJECT);
                                methodSpecBuilder.addStatement(code, member);
                                methodSpecBuilder.addCode("}\n");
                                methodSpecBuilder.addStatement("json.put($S,$L_jsonArray)", keyName, member);
                                continue;
                            } else {
                                if (itemTypeMirror != null && (itemTypeMirror.getKind().isPrimitive() || TYPE_STRING.equals(itemTypeMirror.toString()))) {
                                    Element fieldGetElement = findFieldGetElement(allMembers, member, modifiers);
                                    methodSpecBuilder.addStatement("json.put($S,new $T(source.$L))", keyName, CLASSNAME_JSONARRAY, fieldGetElement);
                                    continue;
                                }
                            }
                        } else {
                            if (Utils.isDeclaredJSONAble(typeUtils, elementUtils, Utils.parseClassInfo(memberType.toString()))) {
                                Element fieldGetElement = findFieldGetElement(allMembers, member, modifiers);
                                handleJSONAbleContainer(methodSpecBuilder, "source." + fieldGetElement.toString(), "json", 0, TYPE_JSONOBJECT, Utils.parseClassInfo(memberType.toString(), keyName));
                                continue;
                            }
                        }
                        TypeElement itemTypeElement = elementUtils.getTypeElement(memberType.toString());
                        if (itemTypeElement != null && itemTypeElement.getAnnotation(JSONAble.class) != null) {
                            Element fieldGetElement = findFieldGetElement(allMembers, member, modifiers);
                            String code = String.format("json.put($S,%s.%s.%s(source.$L))", Utils.getPackageName(elementUtils, itemTypeElement), NAME_JSON_FACTORY, NAME_GEN_JSON_OBJECT);
                            methodSpecBuilder.addStatement(code, keyName, fieldGetElement);
                        } else {
                            Element fieldGetElement = findFieldGetElement(allMembers, member, modifiers);
                            methodSpecBuilder.addStatement("json.put($S,source.$L)", keyName, fieldGetElement);
                        }
                    }
                }
                methodSpecBuilder.addStatement("return json");
                methodList.add(methodSpecBuilder.build());
            }
        }
        return result;
    }


    private void handleJSONAbleContainer(MethodSpec.Builder methodSpecBuilder, String fieldAccessName, String parentJson, int recursiveCount, int jsonType, ClassInfo memberClassInfo) {
        TypeElement memberTypeElement = elementUtils.getTypeElement(memberClassInfo.domainClassName);
        TypeMirror memberTypeMirror = memberTypeElement.asType();
        if (memberClassInfo.declareClassName != null) {
            if (Utils.isSubtypeOfMap(typeUtils, memberTypeMirror)) {
                String currentJson = String.format("%s_%s", CLASSNAME_JSONOBJECT.simpleName().toLowerCase(), memberClassInfo.getMemberName());
                String fieldItemName = String.format("map_item_%s", recursiveCount++);
                ClassInfo itemClassInfo = Utils.parseClassInfo(memberClassInfo.declareClassName);
                itemClassInfo.setMemberName(fieldItemName, true);
                methodSpecBuilder.addStatement("$T $L = new $T()", CLASSNAME_JSONOBJECT, currentJson, CLASSNAME_JSONOBJECT);

                methodSpecBuilder.addCode("for ($T.Entry<$L> $L : $L.entrySet()) {\n",
                        CLASSNAME_ENTRY,
                        memberClassInfo.declareString,
                        fieldItemName,
                        memberClassInfo.isMap() ? fieldAccessName + ".getValue()" : fieldAccessName
                );
                handleJSONAbleContainer(methodSpecBuilder, fieldItemName, currentJson, recursiveCount, TYPE_JSONOBJECT, itemClassInfo);

                methodSpecBuilder.addCode("}\n");

                if (parentJson != null) {
                    switch (jsonType) {
                        case TYPE_JSONARRAY: {
                            methodSpecBuilder.addStatement("$L.put($L)", parentJson, currentJson);
                        }
                        break;
                        case TYPE_JSONOBJECT: {
                            if (memberClassInfo.isMap()) {
                                methodSpecBuilder.addStatement("$L.put($L,$L)", parentJson, memberClassInfo.getMemberKey(), currentJson);
                            } else {
                                methodSpecBuilder.addStatement("$L.put($S,$L)", parentJson, memberClassInfo.getMemberName(), currentJson);
                            }
                        }
                        break;
                        default:
                    }
                }

            } else if (Utils.isSubtypeOfIterable(typeUtils, memberTypeMirror)) {
                ClassInfo itemClassInfo = Utils.parseClassInfo(memberClassInfo.declareClassName, ++recursiveCount + "");

                String fieldItemName = String.format("list_item_%s", recursiveCount);

                String currentJson = String.format("%s_%s", CLASSNAME_JSONARRAY.simpleName().toLowerCase(), memberClassInfo.getMemberName());

                methodSpecBuilder.addStatement("$T $L = new $T()", CLASSNAME_JSONARRAY, currentJson, CLASSNAME_JSONARRAY);

                methodSpecBuilder.addCode("for ($L $L : $L) {\n", memberClassInfo.declareClassName, fieldItemName, memberClassInfo.isMap() ? fieldAccessName + ".getValue()" : fieldAccessName);

                handleJSONAbleContainer(methodSpecBuilder, fieldItemName, currentJson, recursiveCount, TYPE_JSONARRAY, itemClassInfo);

                methodSpecBuilder.addCode("}\n");

                if (parentJson != null) {
                    switch (jsonType) {
                        case TYPE_JSONARRAY: {
                            methodSpecBuilder.addStatement("$L.put($L)", parentJson, currentJson);
                        }
                        break;
                        case TYPE_JSONOBJECT: {
                            if (memberClassInfo.isMap()) {
                                methodSpecBuilder.addStatement("$L.put($L,$L)", parentJson, memberClassInfo.getMemberKey(), currentJson);
                            } else {
                                methodSpecBuilder.addStatement("$L.put($S,$L)", parentJson, memberClassInfo.getMemberName(), currentJson);
                            }
                        }
                        break;
                        default:
                    }
                }
            }
        } else {
            if (parentJson != null) {
                String packageName = Utils.getPackageName(elementUtils, memberTypeElement);
                switch (jsonType) {
                    case TYPE_JSONARRAY: {
                        String code = String.format("$L.put(%s.%s.%s($L))", packageName, NAME_JSON_FACTORY, NAME_GEN_JSON_OBJECT);
                        methodSpecBuilder.addStatement(code, parentJson, fieldAccessName);
                    }
                    break;
                    case TYPE_JSONOBJECT: {
                        if (memberClassInfo.isMap()) {
                            String code = String.format("$L.put($L,%s.%s.%s($L))", packageName, NAME_JSON_FACTORY, NAME_GEN_JSON_OBJECT);
                            methodSpecBuilder.addStatement(code, parentJson, memberClassInfo.getMemberKey(), fieldAccessName + ".getValue()");
                        } else {
                            String code = String.format("$L.put($S,%s.%s.%s($L))", packageName, NAME_JSON_FACTORY, NAME_GEN_JSON_OBJECT);
                            methodSpecBuilder.addStatement(code, parentJson, memberClassInfo.getMemberName(), fieldAccessName);
                        }
                    }
                    break;
                    default: {
                        messager.printMessage(Diagnostic.Kind.ERROR, "jsonType illegal jsonType : " + jsonType);
                    }
                }
            }
        }
    }


    private Element findFieldGetElement(List<? extends Element> allElements, Element fieldElement, Set<Modifier> modifiers) {
        //first find getMethod
        String fieldName = fieldElement.toString();
        String getMethodName = String.format("get%s()", fieldName);
        String isMethodName = "";
        if (fieldElement.asType().getKind() == TypeKind.BOOLEAN) {
            isMethodName = String.format("is%s()", fieldName);
        }

        for (Element element : allElements) {
            TypeMirror elementTypeMirror = element.asType();
            if (elementTypeMirror.getKind() == TypeKind.EXECUTABLE) {
                String methodName = element.toString();
                if (methodName.equalsIgnoreCase(getMethodName) ||
                        methodName.equalsIgnoreCase(isMethodName)) {
                    return element;
                }
            }
        }
        //if field is private and not find get/is method will error
        if (modifiers.contains(Modifier.PRIVATE)) {
            logParsingError(fieldElement);
            return null;
        } else {
            return fieldElement;
        }
    }

    private void logParsingError(Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format("%s Property must public or have get/is method.", element.getSimpleName()), element);
    }

    private void generateJSONFactory(String packageName, List<MethodSpec> genJSONObjectMethods) {
        TypeSpec typeSpec = TypeSpec.classBuilder(NAME_JSON_FACTORY)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethods(genJSONObjectMethods)
                .build();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, String.format("%s generateJSONFactory 错误 e:%s", TAG, e.getMessage()));
        }
    }

    private Map<String, List<TypeElement>> generatePackageNameJSONAbleElementsMap(Set<? extends Element> elements) {
        HashMap<String, List<TypeElement>> result = new HashMap<>();
        if (!elements.isEmpty()) {
            for (Element element : elements) {
                TypeElement typeElement = (TypeElement) element;
                String packageName = Utils.getPackageName(elementUtils, typeElement);
                List<TypeElement> typeElements = result.get(packageName);
                if (typeElements == null) {
                    typeElements = new ArrayList();
                    typeElements.add(typeElement);
                    result.put(packageName, typeElements);
                } else {
                    typeElements.add(typeElement);
                }
            }
        }
        return result;
    }
}
