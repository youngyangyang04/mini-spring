package com.kama.minispring.web.servlet;

/**
 * 视图解析器接口
 * 负责将视图名称解析为具体的视图对象
 *
 * @author kama
 * @version 1.0.0
 */
public interface ViewResolver {
    
    /**
     * 将给定的视图名称解析为View对象
     *
     * @param viewName 要解析的视图名称
     * @return 解析得到的View对象，如果不能解析则返回null
     * @throws Exception 如果解析过程中发生错误
     */
    View resolveViewName(String viewName) throws Exception;
} 