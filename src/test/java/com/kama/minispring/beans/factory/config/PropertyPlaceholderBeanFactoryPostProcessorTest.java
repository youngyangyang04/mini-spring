package com.kama.minispring.beans.factory.config;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.config.PropertyValues;
import com.kama.minispring.beans.factory.config.PropertyValue;
import com.kama.minispring.beans.factory.ConfigurableListableBeanFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Properties;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PropertyPlaceholderBeanFactoryPostProcessor的测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class PropertyPlaceholderBeanFactoryPostProcessorTest {

    @Mock
    private ConfigurableListableBeanFactory beanFactory;

    @Mock
    private BeanDefinition beanDefinition;

    private PropertyPlaceholderBeanFactoryPostProcessor processor;
    private Properties properties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        processor = new PropertyPlaceholderBeanFactoryPostProcessor();
        properties = new Properties();
        properties.setProperty("jdbc.url", "jdbc:mysql://localhost:3306/test");
        properties.setProperty("jdbc.username", "root");
        processor.setProperties(properties);
    }

    @Test
    void shouldReplacePlaceholders() throws Exception {
        // 准备测试数据
        PropertyValues propertyValues = new PropertyValues();
        propertyValues.addPropertyValue(new PropertyValue("url", "${jdbc.url}"));
        propertyValues.addPropertyValue(new PropertyValue("username", "${jdbc.username}"));
        propertyValues.addPropertyValue(new PropertyValue("password", "123456")); // 非占位符值

        // 设置模拟对象的行为
        when(beanFactory.getBeanDefinitionNames()).thenReturn(new String[]{"dataSource"});
        when(beanFactory.getBeanDefinition("dataSource")).thenReturn(beanDefinition);
        when(beanDefinition.getPropertyValues()).thenReturn(propertyValues);

        // 执行测试
        processor.postProcessBeanFactory(beanFactory);

        // 验证结果
        PropertyValue urlPv = propertyValues.getPropertyValue("url");
        PropertyValue usernamePv = propertyValues.getPropertyValue("username");
        PropertyValue passwordPv = propertyValues.getPropertyValue("password");

        assertEquals("jdbc:mysql://localhost:3306/test", urlPv.getValue());
        assertEquals("root", usernamePv.getValue());
        assertEquals("123456", passwordPv.getValue()); // 非占位符值应保持不变
    }

    @Test
    void shouldThrowExceptionForUnresolvedPlaceholder() {
        // 准备测试数据
        PropertyValues propertyValues = new PropertyValues();
        propertyValues.addPropertyValue(new PropertyValue("driver", "${jdbc.driver}")); // 未定义的属性

        // 设置模拟对象的行为
        when(beanFactory.getBeanDefinitionNames()).thenReturn(new String[]{"dataSource"});
        when(beanFactory.getBeanDefinition("dataSource")).thenReturn(beanDefinition);
        when(beanDefinition.getPropertyValues()).thenReturn(propertyValues);

        // 验证是否抛出异常
        assertThrows(BeansException.class, () -> {
            processor.postProcessBeanFactory(beanFactory);
        });
    }

    @Test
    void shouldIgnoreNonPlaceholderValues() throws Exception {
        // 准备测试数据
        PropertyValues propertyValues = new PropertyValues();
        propertyValues.addPropertyValue(new PropertyValue("normalValue", "test")); // 普通字符串值

        // 设置模拟对象的行为
        when(beanFactory.getBeanDefinitionNames()).thenReturn(new String[]{"testBean"});
        when(beanFactory.getBeanDefinition("testBean")).thenReturn(beanDefinition);
        when(beanDefinition.getPropertyValues()).thenReturn(propertyValues);

        // 执行测试
        processor.postProcessBeanFactory(beanFactory);

        // 验证结果
        PropertyValue normalValuePv = propertyValues.getPropertyValue("normalValue");
        assertEquals("test", normalValuePv.getValue()); // 值应该保持不变
    }

    @Test
    void shouldHandleNestedPlaceholders() throws Exception {
        // 准备测试数据
        properties.setProperty("database", "test");
        properties.setProperty("port", "3306");
        PropertyValues propertyValues = new PropertyValues();
        propertyValues.addPropertyValue(new PropertyValue("url", "jdbc:mysql://localhost:${port}/${database}"));

        // 设置模拟对象的行为
        when(beanFactory.getBeanDefinitionNames()).thenReturn(new String[]{"dataSource"});
        when(beanFactory.getBeanDefinition("dataSource")).thenReturn(beanDefinition);
        when(beanDefinition.getPropertyValues()).thenReturn(propertyValues);

        // 执行测试
        processor.postProcessBeanFactory(beanFactory);

        // 验证结果
        PropertyValue urlPv = propertyValues.getPropertyValue("url");
        assertEquals("jdbc:mysql://localhost:3306/test", urlPv.getValue());
    }
} 