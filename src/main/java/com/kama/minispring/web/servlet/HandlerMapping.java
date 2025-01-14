package com.kama.minispring.web.servlet;

import javax.servlet.http.HttpServletRequest;

/**
 * 处理器映射接口
 * 用于查找请求对应的处理器和拦截器
 *
 * @author kama
 * @version 1.0.0
 */
public interface HandlerMapping {
    
    /**
     * 根据请求查找对应的处理器执行链
     * 返回的HandlerExecutionChain包含处理器和拦截器链
     *
     * @param request 当前HTTP请求
     * @return 处理器执行链，如果没有找到对应的处理器则返回null
     * @throws Exception 如果查找过程中出现错误
     */
    HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;
} 