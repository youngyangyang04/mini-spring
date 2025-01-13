package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.factory.config.BeanDefinition;
import com.kama.minispring.beans.factory.config.BeanDefinitionHolder;
import com.kama.minispring.beans.factory.config.ConstructorArgumentValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 构造器注入的测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class ConstructorInjectionTest {
    
    private DefaultListableBeanFactory beanFactory;
    
    @BeforeEach
    void setUp() {
        beanFactory = new DefaultListableBeanFactory();
    }
    
    @Test
    void testConstructorInjection() {
        // 注册依赖的Bean
        BeanDefinition serviceBeanDefinition = new GenericBeanDefinition(SimpleService.class);
        beanFactory.registerBeanDefinition("simpleService", serviceBeanDefinition);
        
        // 注册需要构造器注入的Bean
        BeanDefinition controllerBeanDefinition = new GenericBeanDefinition(SimpleController.class);
        beanFactory.registerBeanDefinition("simpleController", controllerBeanDefinition);
        
        // 添加构造器参数
        controllerBeanDefinition.addConstructorArgumentValue(
            new ConstructorArgumentValue("simpleService", SimpleService.class)
        );
        
        // 获取并验证Bean
        SimpleController controller = beanFactory.getBean("simpleController", SimpleController.class);
        assertNotNull(controller);
        assertNotNull(controller.getService());
        assertEquals("Hello from Service!", controller.getService().sayHello());
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
        private final SimpleService service;
        
        public SimpleController(SimpleService service) {
            this.service = service;
        }
        
        public SimpleService getService() {
            return service;
        }
    }
} 