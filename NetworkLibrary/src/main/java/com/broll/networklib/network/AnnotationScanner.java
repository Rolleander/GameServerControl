package com.broll.networklib.network;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dorkbox.annotation.AnnotationDefaults;
import dorkbox.annotation.AnnotationDetector;

public final class AnnotationScanner {

    private AnnotationScanner() {

    }

    public static <A extends Annotation> List<Pair<Field, A>> findAnnotatedFields(Object object, Class<A> annotationClass) {
        List<Pair<Field, A>> annotations = new ArrayList<>();
        Arrays.stream(object.getClass().getFields()).forEach(field -> {
            A annotation = field.getAnnotation(annotationClass);
            if (annotation != null) {
                annotations.add(Pair.of(field, annotation));
            }
        });
        return annotations;
    }

    public static List<Method> findAnnotatedMethods(Object object, Class<? extends Annotation> annotation) {
        return Arrays.stream(object.getClass().getMethods())
                .filter(m -> m.getAnnotation(annotation) != null)
                .collect(Collectors.toList());
    }

    public static List<?> initAnnotatedClasses(Class<? extends Annotation> annotation) throws IOException {
        List<Class<?>> clazzes = AnnotationDetector.scanClassPath().forAnnotations(annotation).collect(AnnotationDefaults.getType);
        return clazzes.stream().map(clazz -> {
            try {
                return clazz.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }
}
