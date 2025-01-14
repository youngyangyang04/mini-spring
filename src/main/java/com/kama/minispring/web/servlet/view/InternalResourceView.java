package com.kama.minispring.web.servlet.view;

import com.kama.minispring.web.servlet.View;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 内部资源视图，用于渲染JSP页面
 * 通过RequestDispatcher将请求转发到JSP页面
 * 
 * @author kama
 * @version 1.0.0
 */
public class InternalResourceView implements View {
    
    private final String url;
    private final String contentType;
    
    /**
     * 构造函数
     * @param url JSP页面的URL
     * @param contentType 响应的内容类型
     */
    public InternalResourceView(String url, String contentType) {
        this.url = url;
        this.contentType = contentType;
    }
    
    @Override
    public String getContentType() {
        return this.contentType;
    }
    
    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        // 将模型数据添加到请求属性中
        exposeModelAsRequestAttributes(model, request);
        
        // 设置响应的内容类型
        if (getContentType() != null) {
            response.setContentType(getContentType());
        }
        
        // 获取RequestDispatcher并转发请求
        RequestDispatcher rd = request.getRequestDispatcher(this.url);
        if (rd == null) {
            throw new IllegalStateException("Could not get RequestDispatcher for [" + this.url + 
                    "]: check that the corresponding file exists within your web application archive!");
        }
        
        // 执行请求转发
        rd.forward(request, response);
    }
    
    /**
     * 将模型数据暴露为请求属性
     * @param model 模型数据
     * @param request HTTP请求
     */
    protected void exposeModelAsRequestAttributes(Map<String, ?> model, HttpServletRequest request) {
        if (model != null) {
            for (Map.Entry<String, ?> entry : model.entrySet()) {
                String modelName = entry.getKey();
                Object modelValue = entry.getValue();
                if (modelValue != null) {
                    request.setAttribute(modelName, modelValue);
                } else {
                    request.removeAttribute(modelName);
                }
            }
        }
    }
} 