package com.kama.minispring.web.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DispatcherServlet测试类
 * 使用嵌套测试类组织不同场景的测试用例
 *
 * @author kama
 * @version 1.0.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DispatcherServletTest {
    
    private DispatcherServlet dispatcherServlet;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private HandlerMapping handlerMapping;
    
    @Mock
    private HandlerAdapter handlerAdapter;
    
    @Mock
    private ViewResolver viewResolver;
    
    @Mock
    private View view;
    
    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handlerMapping = mock(HandlerMapping.class);
        handlerAdapter = mock(HandlerAdapter.class);
        viewResolver = mock(ViewResolver.class);
        view = mock(View.class);
        
        dispatcherServlet = new DispatcherServlet();
        ReflectionTestUtils.setField(dispatcherServlet, "handlerMappings", 
                Collections.singletonList(handlerMapping));
        ReflectionTestUtils.setField(dispatcherServlet, "handlerAdapters", 
                Collections.singletonList(handlerAdapter));
        ReflectionTestUtils.setField(dispatcherServlet, "viewResolvers", 
                Collections.singletonList(viewResolver));
    }
    
    @Test
    void shouldInitializeComponentsOnServletInit() throws Exception {
        DispatcherServlet servlet = new DispatcherServlet();
        servlet.init();
        
        assertNotNull(servlet.handlerMappings);
        assertNotNull(servlet.handlerAdapters);
        assertNotNull(servlet.viewResolvers);
        
        assertTrue(servlet.handlerMappings.isEmpty());
        assertTrue(servlet.handlerAdapters.isEmpty());
        assertTrue(servlet.viewResolvers.isEmpty());
    }
    
    @Nested
    class NormalRequestTests {
        @Test
        void shouldProcessRequestSuccessfully() throws Exception {
            // 准备测试数据
            Object handler = new Object();
            HandlerExecutionChain chain = new HandlerExecutionChain(handler);
            ModelAndView mv = new ModelAndView("test");
            Map<String, Object> model = new HashMap<>();
            mv.addAllAttributes(model);
            
            // 设置Mock对象行为
            when(handlerMapping.getHandler(request)).thenReturn(chain);
            when(handlerAdapter.supports(handler)).thenReturn(true);
            when(handlerAdapter.handle(request, response, handler)).thenReturn(mv);
            when(viewResolver.resolveViewName("test")).thenReturn(view);
            
            // 执行测试
            dispatcherServlet.processRequest(request, response);
            
            // 验证结果
            verify(view).render(eq(model), eq(request), eq(response));
        }
        
        @Test
        void shouldHandleViewRenderingFailure() throws Exception {
            // 准备测试数据
            Object handler = new Object();
            HandlerExecutionChain chain = new HandlerExecutionChain(handler);
            ModelAndView mv = new ModelAndView("test");
            Map<String, Object> model = new HashMap<>();
            mv.addAllAttributes(model);
            
            // 设置Mock对象行为
            when(handlerMapping.getHandler(request)).thenReturn(chain);
            when(handlerAdapter.supports(handler)).thenReturn(true);
            when(handlerAdapter.handle(request, response, handler)).thenReturn(mv);
            when(viewResolver.resolveViewName("test")).thenReturn(view);
            doThrow(new RuntimeException("Render Error"))
                .when(view).render(any(), eq(request), eq(response));
            
            // 执行测试并验证异常
            ServletException exception = assertThrows(ServletException.class, () ->
                dispatcherServlet.processRequest(request, response));
            assertEquals("Could not render view", exception.getMessage());
            assertNotNull(exception.getCause());
            assertEquals("Render Error", exception.getCause().getMessage());
        }
    }
    
    @Nested
    class ErrorHandlingTests {
        @Test
        void shouldHandleNoHandlerFound() throws Exception {
            when(handlerMapping.getHandler(request)).thenReturn(null);
            
            dispatcherServlet.processRequest(request, response);
            
            verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        
        @Test
        void shouldHandleNoAdapterFound() throws Exception {
            Object handler = new Object();
            HandlerExecutionChain chain = new HandlerExecutionChain(handler);
            
            when(handlerMapping.getHandler(request)).thenReturn(chain);
            when(handlerAdapter.supports(handler)).thenReturn(false);
            
            assertThrows(ServletException.class, () ->
                dispatcherServlet.processRequest(request, response));
        }
        
        @Test
        void shouldHandleHandlerException() throws Exception {
            Object handler = new Object();
            HandlerExecutionChain chain = new HandlerExecutionChain(handler);
            
            when(handlerMapping.getHandler(request)).thenReturn(chain);
            when(handlerAdapter.supports(handler)).thenReturn(true);
            when(handlerAdapter.handle(request, response, handler))
                .thenThrow(new RuntimeException("Handler Error"));
            
            dispatcherServlet.processRequest(request, response);
            
            verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Nested
    class InterceptorTests {
        @Test
        void shouldExecuteInterceptorsInOrder() throws Exception {
            // 准备测试数据
            Object handler = new Object();
            HandlerExecutionChain chain = new HandlerExecutionChain(handler);
            HandlerInterceptor interceptor1 = mock(HandlerInterceptor.class);
            HandlerInterceptor interceptor2 = mock(HandlerInterceptor.class);
            chain.addInterceptor(interceptor1);
            chain.addInterceptor(interceptor2);
            
            ModelAndView mv = new ModelAndView("test");
            
            // 设置Mock对象行为
            when(handlerMapping.getHandler(request)).thenReturn(chain);
            when(handlerAdapter.supports(handler)).thenReturn(true);
            when(handlerAdapter.handle(request, response, handler)).thenReturn(mv);
            when(viewResolver.resolveViewName("test")).thenReturn(view);
            when(interceptor1.preHandle(request, response, handler)).thenReturn(true);
            when(interceptor2.preHandle(request, response, handler)).thenReturn(true);
            
            // 执行测试
            dispatcherServlet.processRequest(request, response);
            
            // 验证拦截器执行顺序
            var inOrder = inOrder(interceptor1, interceptor2);
            inOrder.verify(interceptor1).preHandle(request, response, handler);
            inOrder.verify(interceptor2).preHandle(request, response, handler);
            inOrder.verify(interceptor2).postHandle(request, response, handler, mv);
            inOrder.verify(interceptor1).postHandle(request, response, handler, mv);
            inOrder.verify(interceptor2).afterCompletion(eq(request), eq(response), eq(handler), any());
            inOrder.verify(interceptor1).afterCompletion(eq(request), eq(response), eq(handler), any());
        }
        
        @Test
        void shouldStopExecutionWhenInterceptorReturnsFalse() throws Exception {
            Object handler = new Object();
            HandlerExecutionChain chain = new HandlerExecutionChain(handler);
            HandlerInterceptor interceptor = mock(HandlerInterceptor.class);
            chain.addInterceptor(interceptor);
            
            when(handlerMapping.getHandler(request)).thenReturn(chain);
            when(handlerAdapter.supports(handler)).thenReturn(true);
            when(interceptor.preHandle(request, response, handler)).thenReturn(false);
            
            dispatcherServlet.doDispatch(request, response);
            
            verify(handlerAdapter, never()).handle(any(), any(), any());
            verify(interceptor, never()).postHandle(any(), any(), any(), any());
            verify(interceptor).afterCompletion(eq(request), eq(response), eq(handler), isNull());
        }
    }
} 