package com.kama.minispring.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理器异常解析器接口
 * 用于解析处理器(handler)执行过程中抛出的异常
 * 
 * @author kama
 * @version 1.0.0
 */
public interface HandlerExceptionResolver {
    
    /**
     * 解析处理器执行过程中抛出的异常
     * 
     * @param request 当前HTTP请求
     * @param response 当前HTTP响应
     * @param handler 发生异常的处理器
     * @param ex 抛出的异常
     * @return 用于处理异常的ModelAndView,如果无法处理则返回null
     */
    ModelAndView resolveException(
            HttpServletRequest request, 
            HttpServletResponse response,
            Object handler, 
            Exception ex);
} 