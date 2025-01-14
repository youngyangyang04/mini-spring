package com.kama.minispring.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring MVC的核心分发器
 * 负责将请求分发给对应的处理器，并渲染响应结果
 *
 * @author kama
 * @version 1.0.0
 */
public class DispatcherServlet extends HttpServlet {
    
    /** 处理器映射列表 */
    protected List<HandlerMapping> handlerMappings;
    
    /** 处理器适配器列表 */
    protected List<HandlerAdapter> handlerAdapters;
    
    /** 视图解析器列表 */
    protected List<ViewResolver> viewResolvers;
    
    @Override
    public void init() throws ServletException {
        // 初始化组件列表
        this.handlerMappings = new ArrayList<>();
        this.handlerAdapters = new ArrayList<>();
        this.viewResolvers = new ArrayList<>();
        
        // 初始化Spring容器并加载配置
        initStrategies();
    }
    
    /**
     * 初始化各种策略
     */
    protected void initStrategies() {
        // 初始化处理器映射器
        initHandlerMappings();
        // 初始化处理器适配器
        initHandlerAdapters();
        // 初始化视图解析器
        initViewResolvers();
    }
    
    /**
     * 处理请求的核心方法
     */
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpServletRequest processedRequest = request;
        HandlerExecutionChain mappedHandler = null;
        ModelAndView mv = null;
        Exception dispatchException = null;
        
        try {
            // 1. 查找Handler
            mappedHandler = getHandler(processedRequest);
            if (mappedHandler == null) {
                noHandlerFound(processedRequest, response);
                return;
            }
            
            // 2. 查找HandlerAdapter
            HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
            
            // 3. 执行前置拦截器
            if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                return;
            }
            
            // 4. 执行处理器方法
            mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
            
            // 5. 执行后置拦截器
            mappedHandler.applyPostHandle(processedRequest, response, mv);
            
        } catch (ServletException | IOException ex) {
            throw ex;
        } catch (Exception ex) {
            dispatchException = ex;
        }
        
        try {
            // 6. 处理异常或渲染视图
            if (dispatchException != null) {
                processHandlerException(processedRequest, response, mappedHandler, dispatchException);
            } else if (mv != null && !mv.wasCleared()) {
                render(mv, request, response);
            }
        } finally {
            // 7. 触发完成回调
            if (mappedHandler != null) {
                try {
                    mappedHandler.triggerAfterCompletion(request, response, dispatchException);
                } catch (Exception ex) {
                    throw new ServletException("Could not complete after-completion", ex);
                }
            }
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /**
     * 处理请求的入口方法
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            doDispatch(request, response);
        } catch (ServletException | IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException("Request processing failed", ex);
        }
    }
    
    /**
     * 获取处理器执行链
     */
    protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        for (HandlerMapping hm : this.handlerMappings) {
            HandlerExecutionChain handler = hm.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }
    
    /**
     * 获取处理器适配器
     */
    protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        if (this.handlerAdapters != null) {
            for (HandlerAdapter ha : this.handlerAdapters) {
                if (ha.supports(handler)) {
                    return ha;
                }
            }
        }
        throw new ServletException("No adapter for handler [" + handler +
                "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
    }
    
    /**
     * 处理没有找到Handler的情况
     */
    protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    
    /**
     * 处理Handler执行过程中的异常
     */
    protected void processHandlerException(HttpServletRequest request, HttpServletResponse response,
            HandlerExecutionChain handler, Exception ex) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 处理分发结果
     */
    protected void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
            HandlerExecutionChain mappedHandler, ModelAndView mv) throws ServletException, IOException {
        // 渲染视图
        if (mv != null && !mv.wasCleared()) {
            render(mv, request, response);
        }
    }
    
    /**
     * 渲染视图
     */
    protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            View view = resolveViewName(mv.getViewName(), mv.getModelInternal(), request);
            if (view != null) {
                view.render(mv.getModelInternal(), request, response);
            }
        } catch (Exception e) {
            if (e instanceof ServletException) {
                throw (ServletException) e;
            }
            throw new ServletException("Could not render view", e);
        }
    }
    
    /**
     * 解析视图名称
     */
    protected View resolveViewName(String viewName, Map<String, Object> model,
            HttpServletRequest request) throws Exception {
        for (ViewResolver viewResolver : this.viewResolvers) {
            View view = viewResolver.resolveViewName(viewName);
            if (view != null) {
                return view;
            }
        }
        return null;
    }
    
    /**
     * 初始化处理器映射器
     */
    private void initHandlerMappings() {
        // TODO: 从Spring容器中获取所有HandlerMapping实现
    }
    
    /**
     * 初始化处理器适配器
     */
    private void initHandlerAdapters() {
        // TODO: 从Spring容器中获取所有HandlerAdapter实现
    }
    
    /**
     * 初始化视图解析器
     */
    private void initViewResolvers() {
        // TODO: 从Spring容器中获取所有ViewResolver实现
    }
} 