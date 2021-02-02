package com.znq.libcompiler;

import com.znq.annotation.JSONAble;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @desc:
 * @author: ningqiang.zhao
 * @time: 2020-12-14 19:22
 **/
public class Utils {
    private static final String TYPE_ITERABLE = Iterable.class.getCanonicalName();
    private static final String TYPE_MAP = Map.class.getCanonicalName();

    private Utils() {
    }

    static final String getPackageName(Elements elementUtils, TypeElement typeElement) {
        return elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
    }

    /**
     * Uses both {@link Types#erasure} and string manipulation to strip any generic types.
     */
    static String doubleErasure(Types typeUtils, TypeMirror elementType) {
        String name = typeUtils.erasure(elementType).toString();
        int typeParamStart = name.indexOf('<');
        if (typeParamStart != -1) {
            name = name.substring(0, typeParamStart);
        }
        return name;
    }

    static String erasure(String className) {
        int typeParamStart = className.indexOf('<');
        if (typeParamStart != -1) {
            className = className.substring(0, typeParamStart);
        }
        return className;
    }


    static boolean isSubtypeOfIterable(Types typeUtils, TypeMirror typeMirror) {
        return isSubtype(TYPE_ITERABLE, typeUtils, typeMirror);
    }

    static boolean isSubtypeOfMap(Types typeUtils, TypeMirror typeMirror) {
        return isSubtype(TYPE_MAP, typeUtils, typeMirror);
    }

    private static boolean isSubtype(String superClassName, Types typeUtils, TypeMirror typeMirror) {
        String erasedType = doubleErasure(typeUtils, typeMirror);
        if (superClassName.equals(erasedType)) {
            return true;
        }
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superType = typeElement.getSuperclass();
        if (isSubtype(superClassName, typeUtils, superType)) {
            return true;
        }
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtype(superClassName, typeUtils, interfaceType)) {
                return true;
            }
        }
        return false;
    }

    static TypeElement getType(Elements elementUtils, TypeMirror typeMirror) {
        String typeMirrorName;
        if (typeMirror == null || (typeMirrorName = typeMirror.toString().trim()).length() == 0) {
            return null;
        }
        return elementUtils.getTypeElement(typeMirrorName.replaceFirst("\\?", "").replaceFirst("extends", "").trim());
    }

    static ClassInfo parseClassInfo(String className, String memberName) {
        ClassInfo classInfo = parseClassInfo(className);
        classInfo.setMemberName(memberName, false);
        return classInfo;
    }


    static ClassInfo parseClassInfo(String className) {
        TypeElement typeElement = JSONProcessor.elementUtils.getTypeElement(erasure(className));
        if (typeElement == null) {//基本类型的数据类型
            return null;
        }
        TypeMirror typeMirror = JSONProcessor.elementUtils.getTypeElement(erasure(className)).asType();
        if (isSubtypeOfMap(JSONProcessor.typeUtils, typeMirror)) {
            //map
            int dotIndex = className.indexOf(',');
            if (dotIndex != -1) {
                int typeParamStart = className.indexOf('<');
                if (typeParamStart != -1) {
                    int leftMarkRemovedCount = StringUtils.getCharCount(className.substring(0, dotIndex), '<');
                    String domianClassName = className.substring(0, typeParamStart);
                    String delcareValueClassName = className.substring(dotIndex + 1, StringUtils.lastNumberIndexOf(className, leftMarkRemovedCount, '>'));
                    String delcareString = className.substring(typeParamStart + 1, className.lastIndexOf('>'));
                    return new ClassInfo(domianClassName, delcareValueClassName, delcareString);
                }
            }
        }
        if (isSubtypeOfIterable(JSONProcessor.typeUtils, typeMirror)) {
            //iterable
            int typeParamStart = className.indexOf('<');
            if (typeParamStart != -1) {
                String domianClassName = className.substring(0, typeParamStart);
                String delcareClassName = className.substring(typeParamStart + 1, className.lastIndexOf('>'));
                return new ClassInfo(domianClassName, delcareClassName);
            }
        }
        //other
        return new ClassInfo(className, null);
    }


    static boolean isDeclaredJSONAble(Types typeUtils, Elements elementUtils, ClassInfo classInfo) {
        if (classInfo == null) {//基本类型的数据类型
            return false;
        }
        TypeMirror classTypeMirror = elementUtils.getTypeElement(classInfo.domainClassName).asType();
        if (classInfo.declareClassName != null && (isSubtypeOfIterable(typeUtils, classTypeMirror) || isSubtypeOfMap(typeUtils, classTypeMirror))) {
            ClassInfo declareClassInfo = parseClassInfo(classInfo.declareClassName);
            return isDeclaredJSONAble(typeUtils, elementUtils, declareClassInfo);
        } else {
            TypeElement typeElement = elementUtils.getTypeElement(classTypeMirror.toString());
            return typeElement != null && typeElement.getAnnotation(JSONAble.class) != null;
        }
    }
}
