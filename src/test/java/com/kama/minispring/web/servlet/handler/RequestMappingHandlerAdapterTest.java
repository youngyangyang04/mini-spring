package com.kama.minispring.web.servlet.handler;

import com.kama.minispring.web.servlet.ModelAndView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RequestMappingHandlerAdapter的测试类
 */
class RequestMappingHandlerAdapterTest {

    private RequestMappingHandlerAdapter handlerAdapter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handlerAdapter = new RequestMappingHandlerAdapter();
    }

    @Test
    void shouldSupportHandlerMethodType() {
        // given
        TestController controller = new TestController();
        Method method = TestController.class.getDeclaredMethods()[0];
        RequestMappingHandlerAdapter.HandlerMethod handlerMethod = 
            new RequestMappingHandlerAdapter.HandlerMethod(controller, method);

        // when
        boolean supports = handlerAdapter.supports(handlerMethod);

        // then
        assertTrue(supports);
    }

    @Test
    void shouldNotSupportNonHandlerMethodType() {
        // given
        Object handler = new Object();

        // when
        boolean supports = handlerAdapter.supports(handler);

        // then
        assertFalse(supports);
    }

    @Test
    void shouldHandleStringReturnValue() throws Exception {
        // given
        TestController controller = new TestController();
        Method method = TestController.class.getDeclaredMethod("stringReturnValue");
        RequestMappingHandlerAdapter.HandlerMethod handlerMethod = 
            new RequestMappingHandlerAdapter.HandlerMethod(controller, method);

        // when
        ModelAndView mv = handlerAdapter.handle(request, response, handlerMethod);

        // then
        assertNotNull(mv);
        assertEquals("test", mv.getViewName());
    }

    @Test
    void shouldHandleModelAndViewReturnValue() throws Exception {
        // given
        TestController controller = new TestController();
        Method method = TestController.class.getDeclaredMethod("modelAndViewReturnValue");
        RequestMappingHandlerAdapter.HandlerMethod handlerMethod = 
            new RequestMappingHandlerAdapter.HandlerMethod(controller, method);

        // when
        ModelAndView mv = handlerAdapter.handle(request, response, handlerMethod);

        // then
        assertNotNull(mv);
        assertEquals("test", mv.getViewName());
        assertEquals("value", mv.getModel().get("key"));
    }

    @Test
    void shouldHandleObjectReturnValue() throws Exception {
        // given
        TestController controller = new TestController();
        Method method = TestController.class.getDeclaredMethod("objectReturnValue");
        RequestMappingHandlerAdapter.HandlerMethod handlerMethod = 
            new RequestMappingHandlerAdapter.HandlerMethod(controller, method);

        // when
        ModelAndView mv = handlerAdapter.handle(request, response, handlerMethod);

        // then
        assertNotNull(mv);
        assertEquals("test value", mv.getModel().get("result"));
    }

    /**
     * 测试用的Controller类
     */
    private static class TestController {
        
        public String stringReturnValue() {
            return "test";
        }
        
        public ModelAndView modelAndViewReturnValue() {
            ModelAndView mv = new ModelAndView("test");
            mv.addObject("key", "value");
            return mv;
        }
        
        public String objectReturnValue() {
            return "test value";
        }
    }
} 