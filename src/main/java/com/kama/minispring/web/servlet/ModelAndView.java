package com.kama.minispring.web.servlet;

import java.util.HashMap;
import java.util.Map;

/**
 * 模型和视图的封装类
 * 用于处理器返回模型数据和视图信息
 *
 * @author kama
 * @version 1.0.0
 */
public class ModelAndView {
    
    /** 视图名称 */
    private String viewName;
    
    /** 模型数据 */
    private final Map<String, Object> model = new HashMap<>();
    
    /** 是否已清除 */
    private boolean cleared = false;
    
    /**
     * 默认构造函数
     */
    public ModelAndView() {
    }
    
    /**
     * 使用视图名称构造
     *
     * @param viewName 视图名称
     */
    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }
    
    /**
     * 使用视图名称和模型数据构造
     *
     * @param viewName 视图名称
     * @param model 模型数据
     */
    public ModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        if (model != null) {
            addAllAttributes(model);
        }
    }
    
    /**
     * 获取视图名称
     */
    public String getViewName() {
        return this.viewName;
    }
    
    /**
     * 设置视图名称
     */
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
    
    /**
     * 添加属性
     */
    public ModelAndView addAttribute(String attributeName, Object attributeValue) {
        this.model.put(attributeName, attributeValue);
        return this;
    }
    
    /**
     * 添加所有属性
     */
    public ModelAndView addAllAttributes(Map<String, ?> attributes) {
        this.model.putAll(attributes);
        return this;
    }
    
    /**
     * 获取模型数据
     */
    public Map<String, Object> getModelInternal() {
        return this.model;
    }
    
    /**
     * 是否已清除
     */
    public boolean wasCleared() {
        return this.cleared;
    }
    
    /**
     * 清除模型数据
     */
    public void clear() {
        this.model.clear();
        this.cleared = true;
    }
    
    public ModelAndView addObject(String attributeName, Object attributeValue) {
        model.put(attributeName, attributeValue);
        return this;
    }
    
    public void addAllObjects(Map<String, ?> modelMap) {
        if (modelMap != null) {
            model.putAll(modelMap);
        }
    }
    
    public boolean hasView() {
        return (this.viewName != null);
    }
    
    public boolean isReference() {
        return (this.viewName instanceof String);
    }
    
    public Map<String, Object> getModel() {
        return this.model;
    }
} 