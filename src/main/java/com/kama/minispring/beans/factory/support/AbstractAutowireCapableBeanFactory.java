package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.config.PropertyValues;
import com.kama.minispring.beans.factory.config.PropertyValue;
import com.kama.minispring.beans.factory.config.BeanDefinition;
import com.kama.minispring.beans.factory.DisposableBean;
import com.kama.minispring.beans.factory.config.BeanPostProcessor;
import com.kama.minispring.beans.factory.config.ConstructorArgumentValue;
import com.kama.minispring.util.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现默认bean创建的抽象类
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory {
    private static final Logger logger = LoggerFactory.getLogger(AbstractAutowireCapableBeanFactory.class);
    
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    private final Map<String, DisposableBean> disposableBeans = new ConcurrentHashMap<>();

    protected Object createBeanInstance(BeanDefinition beanDefinition) throws BeansException {
        Class<?> beanClass = beanDefinition.getBeanClass();
        if (beanClass == null) {
            throw new BeansException("Bean class is not set for bean definition");
        }
        
        try {
            if (beanDefinition.hasConstructorArgumentValues()) {
                return autowireConstructor(beanDefinition);
            }
            return beanClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new BeansException("Error creating bean instance for " + beanClass, e);
        }
    }

    protected Object autowireConstructor(BeanDefinition beanDefinition) throws BeansException {
        Class<?> beanClass = beanDefinition.getBeanClass();
        List<ConstructorArgumentValue> argumentValues = beanDefinition.getConstructorArgumentValues();
        
        try {
            // 获取所有构造函数
            Constructor<?>[] constructors = beanClass.getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == argumentValues.size()) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    Object[] args = new Object[argumentValues.size()];
                    
                    // 准备参数
                    for (int i = 0; i < argumentValues.size(); i++) {
                        ConstructorArgumentValue argumentValue = argumentValues.get(i);
                        Object value = argumentValue.getValue();
                        Class<?> requiredType = paramTypes[i];
                        
                        if (value instanceof String && requiredType != String.class) {
                            // 如果值是字符串但需要的类型不是字符串，尝试获取引用的bean
                            String refBeanName = (String) value;
                            // 优先从缓存中获取
                            if (this instanceof DefaultListableBeanFactory) {
                                value = ((DefaultListableBeanFactory) this).getSingleton(refBeanName, true);
                                if (value == null) {
                                    value = getBean(refBeanName);
                                }
                            } else {
                                value = getBean(refBeanName);
                            }
                        }
                        args[i] = value;
                    }
                    
                    return constructor.newInstance(args);
                }
            }
            throw new BeansException("Could not find matching constructor for " + beanClass);
        } catch (Exception e) {
            throw new BeansException("Error autowiring constructor for " + beanClass, e);
        }
    }

    protected void populateBean(String beanName, Object bean, BeanDefinition beanDefinition) throws BeansException {
        PropertyValues propertyValues = beanDefinition.getPropertyValues();
        if (propertyValues != null) {
            for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
                String propertyName = propertyValue.getName();
                Object value = propertyValue.getValue();
                Class<?> type = propertyValue.getType();
                
                try {
                    // 如果值是字符串，且类型不是String，尝试从缓存中获取引用的bean
                    if (value instanceof String && type != String.class) {
                        String refBeanName = (String) value;
                        // 优先从缓存中获取
                        if (this instanceof DefaultListableBeanFactory) {
                            value = ((DefaultListableBeanFactory) this).getSingleton(refBeanName, true);
                            if (value == null) {
                                value = getBean(refBeanName);
                            }
                        } else {
                            value = getBean(refBeanName);
                        }
                    }
                    
                    String methodName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
                    Method setter = bean.getClass().getMethod(methodName, type);
                    setter.setAccessible(true);
                    setter.invoke(bean, value);
                } catch (Exception e) {
                    throw new BeansException("Error setting property '" + propertyName + "' for bean '" + beanName + "'", e);
                }
            }
        }
    }

    protected void registerDisposableBean(String beanName, DisposableBean bean) {
        disposableBeans.put(beanName, bean);
    }

    public void destroySingletons() {
        synchronized (this.disposableBeans) {
            for (Map.Entry<String, DisposableBean> entry : disposableBeans.entrySet()) {
                try {
                    entry.getValue().destroy();
                    logger.debug("Invoked destroy-method of bean '{}'", entry.getKey());
                } catch (Exception e) {
                    logger.error("Error destroying bean '{}'", entry.getKey(), e);
                }
            }
            disposableBeans.clear();
        }
    }
} 