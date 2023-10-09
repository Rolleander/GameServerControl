package com.broll.networklib.network;

import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class AnnotationScanner {

    private AnnotationScanner() {

    }

    public static <A extends Annotation> List<Pair<Field, A>> findAnnotatedFields(Object object, Class<A> annotationClass) {
        List<Pair<Field, A>> annotations = new ArrayList<>();
        Arrays.stream(object.getClass().getDeclaredFields()).forEach(field -> {
            A annotation = field.getAnnotation(annotationClass);
            if (annotation != null) {
                annotations.add(Pair.of(field, annotation));
            }
        });
        return annotations;
    }

    public static List<Method> findAnnotatedMethods(Object object, Class<? extends Annotation> annotation) {
        List<Method> resolvedMethods = new ArrayList<>();
        Class<?> clazz = object.getClass();
        while(!clazz.getName().equals("java.lang.Object")){
            List<Method> annotatedMethods = Arrays.stream(clazz.getDeclaredMethods())
                    .filter(m -> m.getAnnotation(annotation) != null)
                    .collect(Collectors.toList());
            annotatedMethods.forEach(it-> addUniqueMethods(resolvedMethods,it));
            clazz= clazz.getSuperclass();
        }
        return resolvedMethods;
    }

    private static void addUniqueMethods(List<Method> methods, Method method){
        if(methods.stream().noneMatch(it->isSameSignature(it, method))) {
            methods.add(method);
        }
    }

    private static boolean isSameSignature(Method methodA, Method methodB) {
        if (methodA == null)
            return false;
        if (methodB == null)
            return false;
        List<Class<?>> parameterTypesA = Arrays.asList(methodA.getParameterTypes());
        List<Class<?>> parameterTypesB = Arrays.asList(methodB.getParameterTypes());
        return methodA.getName().equals(methodB.getName()) && parameterTypesA.containsAll(parameterTypesB);
    }
}
