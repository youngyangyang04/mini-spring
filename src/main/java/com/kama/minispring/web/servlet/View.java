package com.kama.minispring.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 视图接口，定义视图的渲染行为
 * 
 * @author kama
 * @version 1.0.0
 */
public interface View {
    
    /**
     * 获取内容类型
     * @return 内容类型
     */
    String getContentType();
    
    /**
     * 渲染视图
     * @param model 模型数据
     * @param request HTTP请求
     * @param response HTTP响应
     * @throws Exception 渲染过程中的异常
     */
    void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception;
} 