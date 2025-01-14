package com.kama.minispring.web.servlet.handler;

import com.kama.minispring.context.ApplicationContext;
import com.kama.minispring.web.servlet.HandlerExecutionChain;
import com.kama.minispring.web.servlet.annotation.RequestMapping;
import com.kama.minispring.web.servlet.annotation.RequestMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RequestMappingHandlerMapping测试类
 *
 * @author kama
 * @version 1.0.0
 */
class RequestMappingHandlerMappingTest {

    private RequestMappingHandlerMapping handlerMapping;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handlerMapping = new RequestMappingHandlerMapping();
        handlerMapping.setApplicationContext(applicationContext);
    }

    @Test
    void shouldFindHandlerForSimpleRequestMapping() throws Exception {
        // 准备测试数据
        TestController controller = new TestController();
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[]{"testController"});
        when(applicationContext.getBean("testController")).thenReturn(controller);

        // 初始化处理器映射器
        handlerMapping.afterPropertiesSet();

        // 模拟请求
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");

        // 执行测试
        HandlerExecutionChain chain = handlerMapping.getHandler(request);

        // 验证结果
        assertNotNull(chain);
        assertTrue(chain.getHandler() instanceof RequestMappingHandlerMapping.HandlerMethod);
        RequestMappingHandlerMapping.HandlerMethod handlerMethod = 
            (RequestMappingHandlerMapping.HandlerMethod) chain.getHandler();
        assertEquals("handleRequest", handlerMethod.getMethod().getName());
        assertSame(controller, handlerMethod.getBean());
    }

    @Test
    void shouldNotFindHandlerForNonExistentPath() throws Exception {
        // 准备测试数据
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[]{});

        // 初始化处理器映射器
        handlerMapping.afterPropertiesSet();

        // 模拟请求
        when(request.getRequestURI()).thenReturn("/nonexistent");
        when(request.getMethod()).thenReturn("GET");

        // 执行测试
        HandlerExecutionChain chain = handlerMapping.getHandler(request);

        // 验证结果
        assertNull(chain);
    }

    @Test
    void shouldRespectRequestMethodConstraints() throws Exception {
        // 准备测试数据
        TestController controller = new TestController();
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[]{"testController"});
        when(applicationContext.getBean("testController")).thenReturn(controller);

        // 初始化处理器映射器
        handlerMapping.afterPropertiesSet();

        // 模拟POST请求
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("POST");

        // 执行测试
        HandlerExecutionChain chain = handlerMapping.getHandler(request);

        // 验证结果：应该找不到处理器，因为/test只支持GET方法
        assertNull(chain);
    }

    @Test
    void shouldCombineTypeAndMethodLevelMappings() throws Exception {
        // 准备测试数据
        TestControllerWithTypeLevelMapping controller = new TestControllerWithTypeLevelMapping();
        when(applicationContext.getBeanDefinitionNames())
            .thenReturn(new String[]{"testControllerWithTypeLevelMapping"});
        when(applicationContext.getBean("testControllerWithTypeLevelMapping")).thenReturn(controller);

        // 初始化处理器映射器
        handlerMapping.afterPropertiesSet();

        // 模拟请求
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");

        // 执行测试
        HandlerExecutionChain chain = handlerMapping.getHandler(request);

        // 验证结果
        assertNotNull(chain);
        assertTrue(chain.getHandler() instanceof RequestMappingHandlerMapping.HandlerMethod);
        RequestMappingHandlerMapping.HandlerMethod handlerMethod = 
            (RequestMappingHandlerMapping.HandlerMethod) chain.getHandler();
        assertEquals("handleRequest", handlerMethod.getMethod().getName());
        assertSame(controller, handlerMethod.getBean());
    }

    // 测试用的Controller类
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    static class TestController {
        public String handleRequest() {
            return "test";
        }
    }

    // 测试用的带类级别映射的Controller类
    @RequestMapping("/api")
    static class TestControllerWithTypeLevelMapping {
        @RequestMapping("/test")
        public String handleRequest() {
            return "test";
        }
    }
} 