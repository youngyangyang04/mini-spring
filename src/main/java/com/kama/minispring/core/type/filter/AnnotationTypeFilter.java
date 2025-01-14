package com.kama.minispring.core.type.filter;

import com.kama.minispring.core.type.AnnotationMetadata;
import com.kama.minispring.core.type.ClassMetadata;

import java.lang.annotation.Annotation;

/**
 * 注解类型过滤器
 *
 * @author kama
 * @version 1.0.0
 */
public class AnnotationTypeFilter implements TypeFilter {
    
    private final Class<? extends Annotation> annotationType;
    
    public AnnotationTypeFilter(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }
    
    @Override
    public boolean match(ClassMetadata metadata) {
        if (metadata instanceof AnnotationMetadata) {
            return ((AnnotationMetadata) metadata).hasAnnotation(annotationType.getName());
        }
        return false;
    }
} 