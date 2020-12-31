package com.znq.runtime;

import android.util.Log;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @desc:
 * @author: ningqiang.zhao
 * @time: 2020-12-23 18:19
 **/
public class JsonKnife {
    private static final String TAG = "JsonKnife";
    private static boolean debug = false;
    private static final String NAME_JSONFACTORY = "JSONFactory";
    private static final String NAME_GEN_JSONOBJECT = "genJSONObject";
    private static final Map<Class<?>, Method> BINDINGS = new LinkedHashMap<>();

    public static JSONObject convert(Object obj) {
        Method toJsonMethod = null;
        try {
            toJsonMethod = findToJsonMethod(obj);
            if (toJsonMethod == null) {
                throw new IllegalArgumentException(obj + " not found mapping toJson method. is " + obj.getClass() + " add @JSONAble annotation?");
            } else {
                toJsonMethod.setAccessible(true);
                return (JSONObject) toJsonMethod.invoke(null, obj);
            }
        } catch (Exception e) {
            RuntimeException exception = new RuntimeException(e.getMessage());
            exception.setStackTrace(e.getStackTrace());
            throw exception;
        }
    }


    private static Method findToJsonMethod(Object obj) throws IllegalStateException, ClassNotFoundException {
        Class<?> cls = obj.getClass();
        Method toJsonMethod = BINDINGS.get(cls);
        if (toJsonMethod != null || BINDINGS.containsKey(cls)) {
            if (debug) {
                Log.d(TAG, "HIT: Cached in binding map.");
            }
            return toJsonMethod;
        }
        Package clsPkg = cls.getPackage();
        String pkgName = clsPkg == null ? "" : clsPkg.getName();
        Class<?> utilClass = cls.getClassLoader().loadClass(String.format("%s.%s", pkgName, NAME_JSONFACTORY));
        Method[] methods;
        try {
            methods = utilClass.getDeclaredMethods();
        } catch (Throwable throwable) {
            methods = utilClass.getMethods();
        }
        for (Method method : methods) {
            if (NAME_GEN_JSONOBJECT.equals(method.getName())) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    BINDINGS.put(parameterTypes[0], method);
                } else {
                    String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                    throw new RuntimeException(String.format("%s %s method must have exactly 1 parameter but has %d", methodName, NAME_GEN_JSONOBJECT, parameterTypes.length));
                }
            }
        }
        return BINDINGS.get(cls);
    }
}
