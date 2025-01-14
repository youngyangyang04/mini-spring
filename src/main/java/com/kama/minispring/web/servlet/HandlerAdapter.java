package com.kama.minispring.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理器适配器接口
 * 负责实际调用处理器处理请求
 *
 * @author kama
 * @version 1.0.0
 */
public interface HandlerAdapter {
    
    /**
     * 判断是否支持给定的处理器
     *
     * @param handler 要检查的处理器
     * @return 如果此适配器支持给定的处理器则返回true
     */
    boolean supports(Object handler);
    
    /**
     * 使用给定的处理器处理请求
     *
     * @param request 当前HTTP请求
     * @param response 当前HTTP响应
     * @param handler 要使用的处理器
     * @return 处理结果的ModelAndView，如果处理器返回null则可能为null
     * @throws Exception 如果处理过程中发生错误
     */
    ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception;
} 