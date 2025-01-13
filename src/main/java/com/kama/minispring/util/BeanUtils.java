package com.kama.minispring.util;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.BeanFactory;
import com.kama.minispring.beans.factory.config.PropertyValue;
import com.kama.minispring.beans.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.Method;

/**
 * Bean操作的工具类
 */
public class BeanUtils {
    /**
     * 设置bean的属性值
     */
    public static void setProperty(Object bean, PropertyValue propertyValue, BeanFactory beanFactory) throws BeansException {
        String propertyName = propertyValue.getName();
        Object value = propertyValue.getValue();
        Class<?> type = propertyValue.getType();

        try {
            // 如果值是字符串，且类型不是String，尝试从BeanFactory获取引用的bean
            if (value instanceof String && type != String.class) {
                String beanName = (String) value;
                // 直接从BeanFactory获取bean
                value = beanFactory.getBean(beanName, type);
            }

            String methodName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            Method setter = bean.getClass().getMethod(methodName, type);
            // 设置方法可访问
            setter.setAccessible(true);
            setter.invoke(bean, value);
        } catch (Exception e) {
            throw new BeansException("Error setting property '" + propertyName + "' to bean", e);
        }
    }
} 