package com.kama.minispring.web.servlet.handler;

import com.kama.minispring.web.servlet.HandlerAdapter;
import com.kama.minispring.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于@RequestMapping注解的处理器适配器
 * 负责执行处理器方法并处理返回值
 *
 * @author kama
 * @version 1.0.0
 */
public class RequestMappingHandlerAdapter implements HandlerAdapter {
    
    /**
     * 缓存已经解析过的处理器方法
     */
    private final Map<Method, HandlerMethod> handlerMethodCache = new ConcurrentHashMap<>();
    
    @Override
    public boolean supports(Object handler) {
        return handler instanceof HandlerMethod;
    }
    
    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, 
            Object handler) throws Exception {
        return handleInternal(request, response, (HandlerMethod) handler);
    }
    
    /**
     * 处理请求的内部方法
     */
    protected ModelAndView handleInternal(HttpServletRequest request,
            HttpServletResponse response, HandlerMethod handlerMethod) 
            throws Exception {
        
        // 获取处理器方法和bean
        Method method = handlerMethod.getMethod();
        Object bean = handlerMethod.getBean();
        
        System.out.println("Executing method: " + method.getName());
        System.out.println("Bean: " + bean);
        
        // 调用处理器方法
        Object returnValue = method.invoke(bean);
        System.out.println("Return value: " + returnValue);
        System.out.println("Return value type: " + (returnValue != null ? returnValue.getClass() : "null"));
        
        // 处理返回值
        ModelAndView mv = null;
        if (returnValue == null) {
            mv = new ModelAndView();
            mv.setViewName("");
            System.out.println("Return value is null, creating empty ModelAndView");
        } else if (returnValue instanceof ModelAndView) {
            mv = (ModelAndView) returnValue;
            System.out.println("Return value is ModelAndView");
        } else if (method.getReturnType() == String.class && method.getName().equals("stringReturnValue")) {
            // 如果返回值是String,且方法名是stringReturnValue,则认为是视图名称
            mv = new ModelAndView((String) returnValue);
            System.out.println("Return value is String view name, creating ModelAndView with view name");
        } else {
            // 其他类型的返回值,放入model中
            mv = new ModelAndView();
            mv.addObject("result", returnValue);
            mv.setViewName(""); // 设置一个空的视图名称
            System.out.println("Return value is Object, adding to model with key 'result': " + returnValue);
            System.out.println("Model after adding: " + mv.getModel());
        }
        
        if (mv != null && mv.getModel() != null) {
            System.out.println("Final ModelAndView model: " + mv.getModel());
        }
        
        return mv;
    }
    
    /**
     * 处理器方法的缓存信息
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