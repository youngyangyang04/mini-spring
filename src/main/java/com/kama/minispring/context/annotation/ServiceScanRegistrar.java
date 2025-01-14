package com.kama.minispring.context.annotation;

import com.kama.minispring.beans.factory.support.BeanDefinitionRegistry;
import com.kama.minispring.core.type.AnnotationMetadata;
import com.kama.minispring.core.type.filter.AnnotationTypeFilter;
import com.kama.minispring.stereotype.Service;
import com.kama.minispring.util.ClassUtils;

import java.util.Map;

/**
 * 用于注册带有@Service注解的类
 *
 * @author kama
 * @version 1.0.0
 */
public class ServiceScanRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableServiceScan.class.getName());
        String[] basePackages = null;
        
        if (attributes != null) {
            basePackages = (String[]) attributes.get("basePackages");
        }
        
        if (basePackages == null || basePackages.length == 0) {
            String basePackage = ClassUtils.getPackageName(metadata.getClassName());
            basePackages = new String[]{basePackage};
        }
        
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        // 添加@Service注解过滤器
        scanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));
        scanner.scan(basePackages);
    }
} 