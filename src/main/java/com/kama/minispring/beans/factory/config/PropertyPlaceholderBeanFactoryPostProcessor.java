package com.kama.minispring.beans.factory.config;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.ConfigurableListableBeanFactory;

import java.util.Properties;

/**
 * BeanFactoryPostProcessor实现类，用于处理bean定义中的属性占位符
 * 例如将${jdbc.url}替换为properties文件中对应的值
 *
 * @author kama
 * @version 1.0.0
 */
public class PropertyPlaceholderBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private Properties properties;
    private String placeholderPrefix = "${";
    private String placeholderSuffix = "}";

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            // 遍历所有bean定义
            String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
            for (String beanName : beanDefinitionNames) {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                PropertyValues propertyValues = beanDefinition.getPropertyValues();
                
                // 遍历bean的所有属性
                for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
                    Object value = propertyValue.getValue();
                    if (value instanceof String) {
                        String stringValue = (String) value;
                        String resolvedValue = resolvePlaceholder(stringValue);
                        if (!stringValue.equals(resolvedValue)) {
                            propertyValues.addPropertyValue(
                                new PropertyValue(propertyValue.getName(), resolvedValue));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new BeansException("Error processing property placeholder", e);
        }
    }

    private String resolvePlaceholder(String value) throws BeansException {
        if (value == null || value.length() == 0) {
            return value;
        }

        StringBuilder result = new StringBuilder(value);
        int startIdx;
        while ((startIdx = result.indexOf(placeholderPrefix)) != -1) {
            int endIdx = result.indexOf(placeholderSuffix, startIdx + placeholderPrefix.length());
            if (endIdx == -1) {
                return result.toString();
            }

            String placeholder = result.substring(startIdx + placeholderPrefix.length(), endIdx);
            String resolvedValue = properties.getProperty(placeholder);
            
            if (resolvedValue == null) {
                throw new BeansException("Could not resolve placeholder '" + placeholder + "'");
            }

            result.replace(startIdx, endIdx + placeholderSuffix.length(), resolvedValue);
        }

        return result.toString();
    }
} 