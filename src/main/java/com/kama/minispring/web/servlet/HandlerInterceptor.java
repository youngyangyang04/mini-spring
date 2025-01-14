package com.kama.minispring.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理器拦截器接口
 * 定义了处理器执行前后的拦截点
 *
 * @author kama
 * @version 1.0.0
 */
public interface HandlerInterceptor {
    
    /**
     * 在处理器执行之前调用
     *
     * @param request 当前HTTP请求
     * @param response 当前HTTP响应
     * @param handler 选择的处理器
     * @return 如果继续处理，则为true；如果中断处理，则为false
     * @throws Exception 如果发生错误
     */
    default boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        return true;
    }
    
    /**
     * 在处理器执行之后，视图渲染之前调用
     *
     * @param request 当前HTTP请求
     * @param response 当前HTTP响应
     * @param handler 选择的处理器
     * @param modelAndView 处理器返回的ModelAndView
     * @throws Exception 如果发生错误
     */
    default void postHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler, ModelAndView modelAndView) throws Exception {
    }
    
    /**
     * 在请求处理完成后调用，即视图渲染完毕或发生异常时
     *
     * @param request 当前HTTP请求
     * @param response 当前HTTP响应
     * @param handler 选择的处理器
     * @param ex 处理器执行过程中抛出的异常，如果没有异常则为null
     * @throws Exception 如果发生错误
     */
    default void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {
    }
} 