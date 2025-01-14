package com.kama.minispring.core.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * AnnotationMetadata的简单实现类
 *
 * @author kama
 * @version 1.0.0
 */
public class SimpleAnnotationMetadata extends SimpleClassMetadata implements AnnotationMetadata {
    
    public SimpleAnnotationMetadata(Class<?> introspectedClass) {
        super(introspectedClass);
    }
    
    @Override
    public boolean hasAnnotation(String annotationName) {
        try {
            Class<?> annotationClass = Class.forName(annotationName);
            return getIntrospectedClass().isAnnotationPresent((Class<? extends Annotation>) annotationClass);
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }
    
    @Override
    public Set<String> getAnnotationTypes() {
        Set<String> types = new HashSet<>();
        for (Annotation annotation : getIntrospectedClass().getAnnotations()) {
            types.add(annotation.annotationType().getName());
        }
        return types;
    }
    
    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName) {
        try {
            Class<?> annotationClass = Class.forName(annotationName);
            Annotation annotation = getIntrospectedClass().getAnnotation((Class<? extends Annotation>) annotationClass);
            if (annotation == null) {
                return null;
            }
            
            Map<String, Object> attributes = new HashMap<>();
            Method[] methods = annotationClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getParameterCount() == 0 && method.getReturnType() != void.class) {
                    attributes.put(method.getName(), method.invoke(annotation));
                }
            }
            return attributes;
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    @Override
    public boolean hasAnnotatedMethods(String annotationName) {
        try {
            Class<?> annotationClass = Class.forName(annotationName);
            for (Method method : getIntrospectedClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent((Class<? extends Annotation>) annotationClass)) {
                    return true;
                }
            }
            return false;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }
    
    private Class<?> getIntrospectedClass() {
        try {
            return Class.forName(getClassName());
        }
        catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Class not found: " + getClassName(), ex);
        }
    }
} 