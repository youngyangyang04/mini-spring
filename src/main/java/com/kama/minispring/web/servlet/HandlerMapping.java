package com.kama.minispring.web.servlet;

import javax.servlet.http.HttpServletRequest;

/**
 * 处理器映射器接口
 * 负责根据请求找到对应的处理器
 *
 * @author kama
 * @version 1.0.0
 */
public interface HandlerMapping {
    
    /**
     * 根据请求获取处理器执行链
     *
     * @param request HTTP请求
     * @return 处理器执行链
     * @throws Exception 如果获取处理器过程中发生错误
     */
    HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;
} 