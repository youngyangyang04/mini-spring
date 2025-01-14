package com.kama.minispring.context.annotation;

import com.kama.minispring.beans.factory.config.BeanDefinition;
import com.kama.minispring.beans.factory.support.DefaultListableBeanFactory;
import com.kama.minispring.core.env.StandardEnvironment;
import com.kama.minispring.core.io.DefaultResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 条件注解测试类
 * 
 * @author kama
 * @version 1.0.0
 */
class ConditionalTest {
    
    private DefaultListableBeanFactory beanFactory;
    private StandardEnvironment environment;
    private DefaultResourceLoader resourceLoader;
    private ConditionContext context;
    
    @BeforeEach
    void setUp() {
        beanFactory = mock(DefaultListableBeanFactory.class);
        environment = new StandardEnvironment();
        resourceLoader = new DefaultResourceLoader();
        context = new StandardConditionContext(beanFactory, environment, resourceLoader);
    }
    
    @Test
    void shouldMatchWhenConditionIsTrue() {
        // 创建一个总是返回true的条件
        Condition trueCondition = context -> true;
        
        assertTrue(trueCondition.matches(context));
    }
    
    @Test
    void shouldNotMatchWhenConditionIsFalse() {
        // 创建一个总是返回false的条件
        Condition falseCondition = context -> false;
        
        assertFalse(falseCondition.matches(context));
    }
    
    @Test
    void shouldMatchWhenEnvironmentPropertyIsSet() {
        // 设置环境属性
        environment.setProperty("test.property", "true");
        
        // 创建一个基于环境属性的条件
        Condition propertyCondition = context -> 
            Boolean.parseBoolean(context.getEnvironment().getProperty("test.property"));
        
        assertTrue(propertyCondition.matches(context));
    }
    
    @Test
    void shouldNotMatchWhenEnvironmentPropertyIsNotSet() {
        // 创建一个基于环境属性的条件
        Condition propertyCondition = context -> 
            Boolean.parseBoolean(context.getEnvironment().getProperty("test.property"));
        
        assertFalse(propertyCondition.matches(context));
    }
    
    @Test
    void shouldMatchWhenProfileIsActive() {
        // 激活配置文件
        environment.setActiveProfiles("test");
        
        // 创建一个基于配置文件的条件
        Condition profileCondition = context -> 
            context.getEnvironment().acceptsProfiles("test");
        
        assertTrue(profileCondition.matches(context));
    }
    
    @Test
    void shouldNotMatchWhenProfileIsNotActive() {
        // 创建一个基于配置文件的条件
        Condition profileCondition = context -> 
            context.getEnvironment().acceptsProfiles("test");
        
        assertFalse(profileCondition.matches(context));
    }
} 