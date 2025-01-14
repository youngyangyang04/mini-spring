package com.kama.minispring.web.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * HandlerExecutionChain测试类
 * 测试处理器执行链的功能
 *
 * @author kama
 * @version 1.0.0
 */
class HandlerExecutionChainTest {

    private HandlerExecutionChain chain;
    private Object handler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerInterceptor interceptor1;

    @Mock
    private HandlerInterceptor interceptor2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new Object();
        chain = new HandlerExecutionChain(handler);
    }
    
    @Test
    void shouldReturnHandler() {
        assertSame(handler, chain.getHandler());
    }

    @Test
    void shouldExecuteInterceptorsInOrderForSuccessfulRequest() throws Exception {
        // 添加拦截器
        chain.addInterceptor(interceptor1);
        chain.addInterceptor(interceptor2);

        // 设置Mock对象行为
        when(interceptor1.preHandle(request, response, handler)).thenReturn(true);
        when(interceptor2.preHandle(request, response, handler)).thenReturn(true);

        ModelAndView mv = new ModelAndView("test");

        // 执行preHandle
        assertTrue(chain.applyPreHandle(request, response));

        // 执行postHandle
        chain.applyPostHandle(request, response, mv);

        // 执行afterCompletion
        chain.triggerAfterCompletion(request, response, null);

        // 验证执行顺序
        var inOrder = inOrder(interceptor1, interceptor2);
        
        // preHandle顺序：1 -> 2
        inOrder.verify(interceptor1).preHandle(request, response, handler);
        inOrder.verify(interceptor2).preHandle(request, response, handler);
        
        // postHandle顺序：2 -> 1
        inOrder.verify(interceptor2).postHandle(request, response, handler, mv);
        inOrder.verify(interceptor1).postHandle(request, response, handler, mv);
        
        // afterCompletion顺序：2 -> 1
        inOrder.verify(interceptor2).afterCompletion(request, response, handler, null);
        inOrder.verify(interceptor1).afterCompletion(request, response, handler, null);
    }

    @Test
    void shouldStopExecutionWhenPreHandleReturnsFalse() throws Exception {
        // 添加拦截器
        chain.addInterceptor(interceptor1);
        chain.addInterceptor(interceptor2);

        // 设置第一个拦截器返回false
        when(interceptor1.preHandle(request, response, handler)).thenReturn(false);

        // 执行preHandle
        assertFalse(chain.applyPreHandle(request, response));

        // 验证只调用了第一个拦截器的preHandle
        verify(interceptor1).preHandle(request, response, handler);
        verify(interceptor2, never()).preHandle(request, response, handler);

        // 验证没有调用postHandle
        verify(interceptor1, never()).postHandle(any(), any(), any(), any());
        verify(interceptor2, never()).postHandle(any(), any(), any(), any());

        // 验证只调用了第一个拦截器的afterCompletion
        chain.triggerAfterCompletion(request, response, null);
        verify(interceptor1).afterCompletion(request, response, handler, null);
        verify(interceptor2, never()).afterCompletion(any(), any(), any(), any());
    }

    @Test
    void shouldHandleExceptionInPreHandle() throws Exception {
        // 添加拦截器
        chain.addInterceptor(interceptor1);
        chain.addInterceptor(interceptor2);

        Exception expectedException = new RuntimeException("Test Exception");
        when(interceptor1.preHandle(request, response, handler)).thenThrow(expectedException);

        // 执行preHandle并捕获异常
        Exception actualException = assertThrows(Exception.class, () ->
            chain.applyPreHandle(request, response));
        
        assertSame(expectedException, actualException);

        // 验证异常处理
        chain.triggerAfterCompletion(request, response, expectedException);
        verify(interceptor1).afterCompletion(request, response, handler, expectedException);
        verify(interceptor2, never()).afterCompletion(any(), any(), any(), any());
    }

    @Test
    void shouldHandleExceptionInPostHandle() throws Exception {
        // 添加拦截器
        chain.addInterceptor(interceptor1);
        chain.addInterceptor(interceptor2);

        // 设置所有preHandle返回true
        when(interceptor1.preHandle(request, response, handler)).thenReturn(true);
        when(interceptor2.preHandle(request, response, handler)).thenReturn(true);

        // 设置第二个拦截器的postHandle抛出异常
        Exception expectedException = new RuntimeException("Test Exception");
        doThrow(expectedException).when(interceptor2)
            .postHandle(request, response, handler, null);

        // 执行preHandle
        assertTrue(chain.applyPreHandle(request, response));

        // 执行postHandle并捕获异常
        Exception actualException = assertThrows(Exception.class, () ->
            chain.applyPostHandle(request, response, null));
        
        assertSame(expectedException, actualException);

        // 验证异常处理
        chain.triggerAfterCompletion(request, response, expectedException);
        
        var inOrder = inOrder(interceptor1, interceptor2);
        inOrder.verify(interceptor2).afterCompletion(request, response, handler, expectedException);
        inOrder.verify(interceptor1).afterCompletion(request, response, handler, expectedException);
    }
    
    @Test
    void shouldHandleMultipleExceptionsInAfterCompletion() throws Exception {
        // 添加拦截器
        chain.addInterceptor(interceptor1);
        chain.addInterceptor(interceptor2);
        
        // 设置两个拦截器都抛出异常
        Exception firstException = new RuntimeException("First Error");
        Exception secondException = new RuntimeException("Second Error");
        
        doThrow(firstException).when(interceptor2)
            .afterCompletion(request, response, handler, null);
        doThrow(secondException).when(interceptor1)
            .afterCompletion(request, response, handler, null);
        
        // 执行afterCompletion
        chain.triggerAfterCompletion(request, response, null);
        
        // 验证两个拦截器的afterCompletion都被调用，即使抛出异常也不影响后续拦截器的执行
        var inOrder = inOrder(interceptor1, interceptor2);
        inOrder.verify(interceptor2).afterCompletion(request, response, handler, null);
        inOrder.verify(interceptor1).afterCompletion(request, response, handler, null);
    }
} 