package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.config.BeanDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DefaultListableBeanFactory的测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class DefaultListableBeanFactoryTest {
    
    private DefaultListableBeanFactory beanFactory;
    
    @BeforeEach
    void setUp() {
        beanFactory = new DefaultListableBeanFactory();
    }
    
    @Test
    void testRegisterAndGetBean() {
        // 注册TestBean的定义
        BeanDefinition beanDefinition = new GenericBeanDefinition(TestBean.class);
        beanFactory.registerBeanDefinition("testBean", beanDefinition);
        
        // 测试getBean(String)
        Object bean = beanFactory.getBean("testBean");
        assertNotNull(bean);
        assertTrue(bean instanceof TestBean);
        
        // 测试getBean(String, Class)
        TestBean testBean = beanFactory.getBean("testBean", TestBean.class);
        assertNotNull(testBean);
        
        // 测试getBean(Class)
        TestBean testBean2 = beanFactory.getBean(TestBean.class);
        assertNotNull(testBean2);
        
        // 验证单例行为
        assertSame(testBean, testBean2);
    }
    
    @Test
    void testBeanNotFound() {
        // 测试获取未定义的bean
        assertThrows(BeansException.class, () -> {
            beanFactory.getBean("nonExistingBean");
        });
    }
    
    @Test
    void testContainsBean() {
        // 注册TestBean的定义
        BeanDefinition beanDefinition = new GenericBeanDefinition(TestBean.class);
        beanFactory.registerBeanDefinition("testBean", beanDefinition);
        
        // 测试containsBean方法
        assertTrue(beanFactory.containsBean("testBean"));
        assertFalse(beanFactory.containsBean("nonExistingBean"));
    }
    
    @Test
    void testScopeAndLifecycle() {
        // 创建带有初始化和销毁方法的bean定义
        BeanDefinition beanDefinition = new GenericBeanDefinition(LifecycleBean.class);
        beanDefinition.setInitMethodName("init");
        beanDefinition.setDestroyMethodName("destroy");
        beanFactory.registerBeanDefinition("lifecycleBean", beanDefinition);
        
        // 获取bean并验证初始化方法被调用
        LifecycleBean bean = beanFactory.getBean("lifecycleBean", LifecycleBean.class);
        assertTrue(bean.isInitialized());
        
        // 测试单例作用域
        assertTrue(beanFactory.isSingleton("lifecycleBean"));
        assertFalse(beanFactory.isPrototype("lifecycleBean"));
        
        // 修改作用域为prototype
        beanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        assertFalse(beanFactory.isSingleton("lifecycleBean"));
        assertTrue(beanFactory.isPrototype("lifecycleBean"));
        
        // 获取两个原型bean实例并验证它们不同
        LifecycleBean bean1 = beanFactory.getBean("lifecycleBean", LifecycleBean.class);
        LifecycleBean bean2 = beanFactory.getBean("lifecycleBean", LifecycleBean.class);
        assertNotSame(bean1, bean2);
        
        // 销毁所有单例bean
        beanFactory.destroySingletons();
        assertTrue(bean.isDestroyed());
    }
    
    @Test
    void testAliasRegistry() {
        // 注册TestBean的定义
        BeanDefinition beanDefinition = new GenericBeanDefinition(TestBean.class);
        beanFactory.registerBeanDefinition("testBean", beanDefinition);
        
        // 注册别名
        beanFactory.registerAlias("testBean", "testBean1");
        beanFactory.registerAlias("testBean", "testBean2");
        
        // 测试别名是否正确注册
        assertTrue(beanFactory.isAlias("testBean1"));
        assertTrue(beanFactory.isAlias("testBean2"));
        assertFalse(beanFactory.isAlias("testBean"));
        
        // 测试通过别名获取bean
        TestBean bean1 = beanFactory.getBean("testBean1", TestBean.class);
        TestBean bean2 = beanFactory.getBean("testBean2", TestBean.class);
        TestBean originalBean = beanFactory.getBean("testBean", TestBean.class);
        
        // 验证通过不同名称获取的是同一个bean
        assertSame(bean1, originalBean);
        assertSame(bean2, originalBean);
        
        // 测试获取别名
        String[] aliases = beanFactory.getAliases("testBean");
        assertEquals(2, aliases.length);
        assertTrue(containsAll(aliases, "testBean1", "testBean2"));
        
        // 测试移除别名
        beanFactory.removeAlias("testBean1");
        assertFalse(beanFactory.isAlias("testBean1"));
        assertTrue(beanFactory.isAlias("testBean2"));
    }
    
    @Test
    void testCircularAlias() {
        // 测试循环别名引用
        assertThrows(BeansException.class, () -> {
            beanFactory.registerAlias("testBean", "alias1");
            beanFactory.registerAlias("alias1", "alias2");
            beanFactory.registerAlias("alias2", "testBean");
        });
    }
    
    @Test
    void testAliasOverriding() {
        // 注册TestBean的定义
        BeanDefinition beanDefinition = new GenericBeanDefinition(TestBean.class);
        beanFactory.registerBeanDefinition("testBean", beanDefinition);
        
        // 注册别名
        beanFactory.registerAlias("testBean", "alias");
        
        // 尝试为不同的bean注册相同的别名
        BeanDefinition anotherDefinition = new GenericBeanDefinition(LifecycleBean.class);
        beanFactory.registerBeanDefinition("anotherBean", anotherDefinition);
        
        // 应该抛出异常
        assertThrows(BeansException.class, () -> {
            beanFactory.registerAlias("anotherBean", "alias");
        });
    }
    
    /**
     * 辅助方法：检查数组是否包含所有指定的值
     */
    private boolean containsAll(String[] array, String... values) {
        for (String value : values) {
            boolean found = false;
            for (String item : array) {
                if (item.equals(value)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 用于测试的简单Bean类
     */
    static class TestBean {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    /**
     * 用于测试生命周期方法的Bean类
     */
    static class LifecycleBean {
        private boolean initialized;
        private boolean destroyed;
        
        public void init() {
            this.initialized = true;
        }
        
        public void destroy() {
            this.destroyed = true;
        }
        
        public boolean isInitialized() {
            return initialized;
        }
        
        public boolean isDestroyed() {
            return destroyed;
        }
    }
} 