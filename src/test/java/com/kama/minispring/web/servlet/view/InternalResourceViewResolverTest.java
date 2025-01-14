package com.kama.minispring.web.servlet.view;

import com.kama.minispring.web.servlet.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * InternalResourceViewResolver的测试类
 */
class InternalResourceViewResolverTest {

    private InternalResourceViewResolver viewResolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        viewResolver = new InternalResourceViewResolver();
    }

    @Test
    void shouldResolveViewWithDefaultSettings() throws Exception {
        // given
        String viewName = "test";

        // when
        View view = viewResolver.resolveViewName(viewName);

        // then
        assertNotNull(view);
        assertTrue(view instanceof InternalResourceView);
        assertEquals("text/html;charset=UTF-8", view.getContentType());
    }

    @Test
    void shouldResolveViewWithCustomPrefixAndSuffix() throws Exception {
        // given
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        String viewName = "test";

        // when
        View view = viewResolver.resolveViewName(viewName);

        // then
        assertNotNull(view);
        assertTrue(view instanceof InternalResourceView);
    }

    @Test
    void shouldRenderViewCorrectly() throws Exception {
        // given
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        String viewName = "test";
        View view = viewResolver.resolveViewName(viewName);
        Map<String, Object> model = new HashMap<>();
        model.put("message", "Hello World");

        when(request.getRequestDispatcher("/WEB-INF/jsp/test.jsp")).thenReturn(requestDispatcher);

        // when
        view.render(model, request, response);

        // then
        verify(request).setAttribute("message", "Hello World");
        verify(response).setContentType("text/html;charset=UTF-8");
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void shouldHandleNullModelAttributes() throws Exception {
        // given
        View view = viewResolver.resolveViewName("test");
        Map<String, Object> model = new HashMap<>();
        model.put("nullValue", null);

        when(request.getRequestDispatcher("test")).thenReturn(requestDispatcher);

        // when
        view.render(model, request, response);

        // then
        verify(request).removeAttribute("nullValue");
    }

    @Test
    void shouldThrowExceptionWhenRequestDispatcherNotFound() throws Exception {
        // given
        View view = viewResolver.resolveViewName("test");
        Map<String, Object> model = new HashMap<>();

        when(request.getRequestDispatcher("test")).thenReturn(null);

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            view.render(model, request, response);
        });
    }
} 