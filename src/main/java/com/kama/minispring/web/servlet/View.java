package com.kama.minispring.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 视图接口
 * 负责将模型数据渲染到响应中
 *
 * @author kama
 * @version 1.0.0
 */
public interface View {
    
    /** 默认的内容类型 */
    String DEFAULT_CONTENT_TYPE = "text/html;charset=UTF-8";
    
    /**
     * 获取内容类型
     *
     * @return 响应的内容类型
     */
    default String getContentType() {
        return DEFAULT_CONTENT_TYPE;
    }
    
    /**
     * 渲染视图
     *
     * @param model 模型数据
     * @param request 当前HTTP请求
     * @param response 当前HTTP响应
     * @throws Exception 如果渲染过程中发生错误
     */
    void render(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception;
} 