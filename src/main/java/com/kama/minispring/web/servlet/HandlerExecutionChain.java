package com.kama.minispring.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理器执行链
 * 包含处理器和拦截器链
 *
 * @author kama
 * @version 1.0.0
 */
public class HandlerExecutionChain {
    
    private final Object handler;
    private final List<HandlerInterceptor> interceptors = new ArrayList<>();
    private int interceptorIndex = -1;
    private boolean afterCompletionCalled = false;
    
    public HandlerExecutionChain(Object handler) {
        this.handler = handler;
    }
    
    public Object getHandler() {
        return this.handler;
    }
    
    public void addInterceptor(HandlerInterceptor interceptor) {
        this.interceptors.add(interceptor);
    }
    
    /**
     * 执行所有拦截器的前置处理
     *
     * @return 如果所有拦截器的preHandle都返回true，则返回true；否则返回false
     */
    public boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        if (interceptors.isEmpty()) {
            return true;
        }

        for (int i = 0; i < interceptors.size(); i++) {
            HandlerInterceptor interceptor = interceptors.get(i);
            boolean result;
            try {
                result = interceptor.preHandle(request, response, this.handler);
            } catch (Exception ex) {
                interceptorIndex = i;
                triggerAfterCompletion(request, response, ex);
                throw ex;
            }
            if (!result) {
                interceptorIndex = i;
                triggerAfterCompletion(request, response, null);
                return false;
            }
            interceptorIndex = i;
        }
        return true;
    }
    
    /**
     * 执行所有拦截器的后置处理
     */
    public void applyPostHandle(HttpServletRequest request, HttpServletResponse response,
            ModelAndView mv) throws Exception {
        if (interceptors.isEmpty()) {
            return;
        }
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptors.get(i);
            interceptor.postHandle(request, response, this.handler, mv);
        }
    }
    
    /**
     * 触发完成处理
     * 如果处理器执行过程中出现异常，则会调用该方法
     */
    public void triggerAfterCompletion(HttpServletRequest request,
            HttpServletResponse response, Exception ex) throws Exception {
        if (interceptors.isEmpty() || afterCompletionCalled) {
            return;
        }
        afterCompletionCalled = true;
        int endIndex = interceptorIndex == -1 ? interceptors.size() - 1 : interceptorIndex;
        for (int i = endIndex; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptors.get(i);
            try {
                interceptor.afterCompletion(request, response, this.handler, ex);
            } catch (Throwable throwable) {
                // Log the exception but continue with other interceptors
                System.err.println("HandlerInterceptor.afterCompletion threw exception: " + throwable);
            }
        }
    }
} 