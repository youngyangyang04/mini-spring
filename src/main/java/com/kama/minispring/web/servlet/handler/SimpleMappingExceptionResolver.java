package com.kama.minispring.web.servlet.handler;

import com.kama.minispring.web.servlet.HandlerExceptionResolver;
import com.kama.minispring.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 简单映射异常解析器
 * 将异常类型映射到对应的错误视图名
 * 
 * @author kama
 * @version 1.0.0
 */
public class SimpleMappingExceptionResolver implements HandlerExceptionResolver {

    /** 默认的异常属性名 */
    public static final String DEFAULT_EXCEPTION_ATTRIBUTE = "exception";

    private Properties exceptionMappings;
    private String defaultErrorView;
    private String exceptionAttribute = DEFAULT_EXCEPTION_ATTRIBUTE;

    @Override
    public ModelAndView resolveException(
            HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        
        // 确定错误视图名
        String viewName = determineViewName(ex);
        if (viewName == null) {
            return null;
        }

        // 创建ModelAndView
        ModelAndView mv = new ModelAndView(viewName);
        
        // 将异常添加到模型中
        if (exceptionAttribute != null) {
            mv.addObject(exceptionAttribute, ex);
        }
        
        return mv;
    }

    /**
     * 根据异常确定视图名
     * @param ex 异常
     * @return 视图名,如果没有匹配的映射则返回默认视图名
     */
    protected String determineViewName(Exception ex) {
        String viewName = null;
        
        // 如果有异常映射配置
        if (this.exceptionMappings != null) {
            viewName = findMatchingViewName(this.exceptionMappings, ex);
        }
        
        // 如果没有找到匹配的视图名,使用默认的错误视图
        if (viewName == null && this.defaultErrorView != null) {
            viewName = this.defaultErrorView;
        }
        
        return viewName;
    }

    /**
     * 在异常映射中查找匹配的视图名
     */
    protected String findMatchingViewName(Properties exceptionMappings, Exception ex) {
        String viewName = null;
        String dominantMapping = null;
        int deepest = Integer.MAX_VALUE;
        
        for (Map.Entry<Object, Object> entry : exceptionMappings.entrySet()) {
            String exceptionMapping = (String) entry.getKey();
            int depth = getDepth(exceptionMapping, ex);
            if (depth >= 0 && depth < deepest) {
                deepest = depth;
                dominantMapping = exceptionMapping;
                viewName = (String) entry.getValue();
            }
        }
        
        return viewName;
    }

    /**
     * 获取异常类型与映射之间的继承深度
     * @return 继承深度,如果不匹配返回-1
     */
    protected int getDepth(String exceptionMapping, Exception ex) {
        try {
            Class<?> exceptionClass = Class.forName(exceptionMapping);
            return getDepth(exceptionClass, ex.getClass());
        }
        catch (ClassNotFoundException e) {
            return -1;
        }
    }

    private int getDepth(Class<?> declaredException, Class<?> exceptionClass) {
        int depth = 0;
        while (exceptionClass != null && !declaredException.equals(exceptionClass)) {
            depth++;
            exceptionClass = exceptionClass.getSuperclass();
        }
        return exceptionClass == null ? -1 : depth;
    }

    /**
     * 设置异常类型到视图名的映射
     */
    public void setExceptionMappings(Properties mappings) {
        this.exceptionMappings = mappings;
    }

    /**
     * 设置默认的错误视图名
     */
    public void setDefaultErrorView(String defaultErrorView) {
        this.defaultErrorView = defaultErrorView;
    }

    /**
     * 设置异常属性名
     */
    public void setExceptionAttribute(String exceptionAttribute) {
        this.exceptionAttribute = exceptionAttribute;
    }
} 