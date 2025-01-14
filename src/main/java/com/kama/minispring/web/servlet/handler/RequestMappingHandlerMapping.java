package com.kama.minispring.web.servlet.handler;

import com.kama.minispring.beans.factory.InitializingBean;
import com.kama.minispring.context.ApplicationContext;
import com.kama.minispring.context.ApplicationContextAware;
import com.kama.minispring.web.servlet.HandlerExecutionChain;
import com.kama.minispring.web.servlet.HandlerMapping;
import com.kama.minispring.web.servlet.annotation.RequestMapping;
import com.kama.minispring.web.servlet.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于@RequestMapping注解的处理器映射器
 * 负责查找和维护请求路径与处理器的映射关系
 *
 * @author kama
 * @version 1.0.0
 */
public class RequestMappingHandlerMapping implements HandlerMapping, ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    
    /**
     * 保存路径与处理器的映射关系
     * key: 请求路径
     * value: 处理器方法信息
     */
    private final Map<String, MappingRegistry.MappingRegistration> mappingLookup = new HashMap<>();
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("开始初始化处理器映射器...");
        // 扫描所有带@RequestMapping注解的Bean
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("找到的Bean名称: " + String.join(", ", beanNames));
        
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            System.out.println("处理Bean: " + beanName + ", 类型: " + beanClass.getName());
            
            // 获取类级别的@RequestMapping
            RequestMapping typeMapping = beanClass.getAnnotation(RequestMapping.class);
            String typePath = typeMapping != null ? typeMapping.value() : "";
            RequestMethod[] typeMethods = typeMapping != null ? typeMapping.method() : new RequestMethod[0];
            System.out.println("类级别映射路径: " + typePath);
            
            // 如果类上有@RequestMapping注解但没有指定方法级别的映射,则使用类级别的映射
            if (typeMapping != null) {
                Method[] methods = beanClass.getDeclaredMethods();
                boolean hasMethodMapping = false;
                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        hasMethodMapping = true;
                        break;
                    }
                }
                
                if (!hasMethodMapping) {
                    // 找到名为handleRequest的方法作为默认处理方法
                    try {
                        Method handleRequestMethod = beanClass.getMethod("handleRequest");
                        System.out.println("找到默认处理方法: handleRequest");
                        registerHandlerMethod(typePath, bean, handleRequestMethod, typeMethods);
                    } catch (NoSuchMethodException e) {
                        System.out.println("未找到默认处理方法handleRequest");
                    }
                }
            }
            
            // 扫描所有带@RequestMapping注解的方法
            Method[] methods = beanClass.getDeclaredMethods();
            for (Method method : methods) {
                RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
                if (methodMapping != null) {
                    String methodPath = methodMapping.value();
                    String path = combinePath(typePath, methodPath);
                    System.out.println("找到方法级别映射: " + method.getName() + " -> " + path);
                    
                    // 合并类级别和方法级别的RequestMethod
                    RequestMethod[] methodMethods = methodMapping.method();
                    RequestMethod[] combinedMethods = methodMethods.length > 0 ? methodMethods : typeMethods;
                    
                    // 注册处理器方法
                    registerHandlerMethod(path, bean, method, combinedMethods);
                }
            }
        }
        System.out.println("处理器映射器初始化完成,注册的映射: " + mappingLookup.keySet());
    }
    
    @Override
    public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        String lookupPath = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("查找处理器: 路径=" + lookupPath + ", 方法=" + method);
        
        // 查找处理器
        MappingRegistry.MappingRegistration registration = mappingLookup.get(lookupPath);
        if (registration != null) {
            System.out.println("找到映射注册信息: " + registration.method.getName());
            // 检查请求方法是否匹配
            if (registration.methods.length == 0 || isMethodMatch(method, registration.methods)) {
                System.out.println("请求方法匹配成功");
                return new HandlerExecutionChain(new HandlerMethod(registration.handler, registration.method));
            } else {
                System.out.println("请求方法不匹配");
            }
        } else {
            System.out.println("未找到映射注册信息");
        }
        
        return null;
    }
    
    private void registerHandlerMethod(String path, Object handler, Method method, RequestMethod[] methods) {
        System.out.println("注册处理器方法: " + path + " -> " + method.getName());
        MappingRegistry.MappingRegistration registration = new MappingRegistry.MappingRegistration();
        registration.handler = handler;
        registration.method = method;
        registration.methods = methods;
        
        mappingLookup.put(path, registration);
    }
    
    private String combinePath(String typePath, String methodPath) {
        if (typePath.endsWith("/")) {
            typePath = typePath.substring(0, typePath.length() - 1);
        }
        if (!methodPath.startsWith("/")) {
            methodPath = "/" + methodPath;
        }
        return typePath + methodPath;
    }
    
    private boolean isMethodMatch(String requestMethod, RequestMethod[] methods) {
        if (methods.length == 0) {
            return true;
        }
        for (RequestMethod method : methods) {
            if (method.name().equals(requestMethod)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 映射注册信息
     */
    private static class MappingRegistry {
        static class MappingRegistration {
            Object handler;
            Method method;
            RequestMethod[] methods = new RequestMethod[0]; // 初始化为空数组
        }
    }
    
    /**
     * 处理器方法
     * 包含处理器对象和方法
     */
    public static class HandlerMethod {
        private final Object bean;
        private final Method method;
        
        public HandlerMethod(Object bean, Method method) {
            this.bean = bean;
            this.method = method;
        }
        
        public Object getBean() {
            return bean;
        }
        
        public Method getMethod() {
            return method;
        }
    }
} 