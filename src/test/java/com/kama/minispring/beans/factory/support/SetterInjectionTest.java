package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.factory.config.BeanDefinition;
import com.kama.minispring.beans.factory.config.BeanDefinitionHolder;
import com.kama.minispring.beans.factory.config.PropertyValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * setter注入的测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class SetterInjectionTest {
    
    private DefaultListableBeanFactory beanFactory;
    
    @BeforeEach
    void setUp() {
        beanFactory = new DefaultListableBeanFactory();
    }
    
    @Test
    void testSetterInjection() {
        // 注册依赖的Bean
        BeanDefinition serviceBeanDefinition = new GenericBeanDefinition(SimpleService.class);
        beanFactory.registerBeanDefinition("simpleService", serviceBeanDefinition);
        
        // 注册需要setter注入的Bean
        BeanDefinition controllerBeanDefinition = new GenericBeanDefinition(SimpleController.class);
        beanFactory.registerBeanDefinition("simpleController", controllerBeanDefinition);
        
        // 添加属性值
        controllerBeanDefinition.addPropertyValue(
            new PropertyValue("service", "simpleService", SimpleService.class)
        );
        
        // 获取并验证Bean
        SimpleController controller = beanFactory.getBean("simpleController", SimpleController.class);
        assertNotNull(controller);
        assertNotNull(controller.getService());
        assertEquals("Hello from Service!", controller.getService().sayHello());
    }
    
    @Test
    void testSetterInjectionWithPrimitiveValue() {
        // 注册需要setter注入的Bean
        BeanDefinition controllerBeanDefinition = new GenericBeanDefinition(SimpleController.class);
        beanFactory.registerBeanDefinition("simpleController", controllerBeanDefinition);
        
        // 添加基本类型的属性值
        controllerBeanDefinition.addPropertyValue(
            new PropertyValue("name", "testName", String.class)
        );
        
        // 获取并验证Bean
        SimpleController controller = beanFactory.getBean("simpleController", SimpleController.class);
        assertNotNull(controller);
        assertEquals("testName", controller.getName());
    }
    
    /**
     * 用于测试的Service类
     */
    static class SimpleService {
        public String sayHello() {
            return "Hello from Service!";
        }
    }
    
    /**
     * 用于测试的Controller类
     */
    static class SimpleController {
        private SimpleService service;
        private String name;
        
        public void setService(SimpleService service) {
            this.service = service;
        }
        
        public SimpleService getService() {
            return service;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
} 