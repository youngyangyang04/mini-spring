package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.config.BeanDefinition;
import com.kama.minispring.beans.factory.config.BeanDefinitionHolder;
import com.kama.minispring.beans.factory.config.PropertyValue;
import com.kama.minispring.beans.factory.config.ConstructorArgumentValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 循环依赖处理的测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class CircularDependencyTest {
    
    private DefaultListableBeanFactory beanFactory;
    
    @BeforeEach
    void setUp() {
        beanFactory = new DefaultListableBeanFactory();
    }
    
    @Test
    void testCircularDependencyWithSetter() {
        // 注册CircularA的定义
        BeanDefinition beanDefinitionA = new GenericBeanDefinition(CircularA.class);
        beanFactory.registerBeanDefinition("circularA", beanDefinitionA);
        
        // 注册CircularB的定义
        BeanDefinition beanDefinitionB = new GenericBeanDefinition(CircularB.class);
        beanFactory.registerBeanDefinition("circularB", beanDefinitionB);
        
        // 设置CircularA依赖CircularB
        beanDefinitionA.addPropertyValue(
            new PropertyValue("circularB", "circularB", CircularB.class)
        );
        
        // 设置CircularB依赖CircularA
        beanDefinitionB.addPropertyValue(
            new PropertyValue("circularA", "circularA", CircularA.class)
        );
        
        // 获取并验证Bean
        CircularA circularA = beanFactory.getBean("circularA", CircularA.class);
        CircularB circularB = beanFactory.getBean("circularB", CircularB.class);
        
        assertNotNull(circularA);
        assertNotNull(circularB);
        assertSame(circularB, circularA.getCircularB());
        assertSame(circularA, circularB.getCircularA());
    }
    
    @Test
    void testCircularDependencyWithConstructor() {
        // 注册CircularC的定义
        BeanDefinition beanDefinitionC = new GenericBeanDefinition(CircularC.class);
        beanFactory.registerBeanDefinition("circularC", beanDefinitionC);
        
        // 注册CircularD的定义
        BeanDefinition beanDefinitionD = new GenericBeanDefinition(CircularD.class);
        beanFactory.registerBeanDefinition("circularD", beanDefinitionD);
        
        // 设置构造器依赖
        beanDefinitionC.addConstructorArgumentValue(
            new ConstructorArgumentValue("circularD", CircularD.class)
        );
        beanDefinitionD.addConstructorArgumentValue(
            new ConstructorArgumentValue("circularC", CircularC.class)
        );
        
        // 验证循环依赖异常
        assertThrows(BeansException.class, () -> {
            beanFactory.getBean("circularC");
        });
    }
    
    /**
     * 用于测试setter循环依赖的类A
     */
    static class CircularA {
        private CircularB circularB;
        
        public void setCircularB(CircularB circularB) {
            this.circularB = circularB;
        }
        
        public CircularB getCircularB() {
            return circularB;
        }
    }
    
    /**
     * 用于测试setter循环依赖的类B
     */
    static class CircularB {
        private CircularA circularA;
        
        public void setCircularA(CircularA circularA) {
            this.circularA = circularA;
        }
        
        public CircularA getCircularA() {
            return circularA;
        }
    }
    
    /**
     * 用于测试构造器循环依赖的类C
     */
    static class CircularC {
        private final CircularD circularD;
        
        public CircularC(CircularD circularD) {
            this.circularD = circularD;
        }
    }
    
    /**
     * 用于测试构造器循环依赖的类D
     */
    static class CircularD {
        private final CircularC circularC;
        
        public CircularD(CircularC circularC) {
            this.circularC = circularC;
        }
    }
} 