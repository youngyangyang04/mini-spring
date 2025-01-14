package com.kama.minispring.web.servlet;

/**
 * 视图解析器接口，负责将视图名称解析为View对象
 * 
 * @author kama
 * @version 1.0.0
 */
public interface ViewResolver {
    
    /**
     * 将视图名称解析为View对象
     * @param viewName 视图名称
     * @return View对象，如果无法解析则返回null
     * @throws Exception 解析过程中的异常
     */
    View resolveViewName(String viewName) throws Exception;
} 