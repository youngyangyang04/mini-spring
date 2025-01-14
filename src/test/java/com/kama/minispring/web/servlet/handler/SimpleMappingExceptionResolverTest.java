package com.kama.minispring.web.servlet.handler;

import com.kama.minispring.web.servlet.ModelAndView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SimpleMappingExceptionResolver的测试类
 */
class SimpleMappingExceptionResolverTest {

    private SimpleMappingExceptionResolver resolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resolver = new SimpleMappingExceptionResolver();
    }

    @Test
    void shouldResolveExceptionWithDefaultSettings() {
        // given
        resolver.setDefaultErrorView("error");
        Exception ex = new RuntimeException("Test exception");

        // when
        ModelAndView mv = resolver.resolveException(request, response, null, ex);

        // then
        assertNotNull(mv);
        assertEquals("error", mv.getViewName());
        assertEquals(ex, mv.getModel().get("exception"));
    }

    @Test
    void shouldResolveExceptionWithCustomMapping() {
        // given
        Properties mappings = new Properties();
        mappings.setProperty("java.lang.RuntimeException", "runtime-error");
        mappings.setProperty("java.lang.Exception", "error");
        
        resolver.setExceptionMappings(mappings);
        Exception ex = new RuntimeException("Test exception");

        // when
        ModelAndView mv = resolver.resolveException(request, response, null, ex);

        // then
        assertNotNull(mv);
        assertEquals("runtime-error", mv.getViewName());
        assertEquals(ex, mv.getModel().get("exception"));
    }

    @Test
    void shouldResolveExceptionWithInheritance() {
        // given
        Properties mappings = new Properties();
        mappings.setProperty("java.lang.Exception", "error");
        
        resolver.setExceptionMappings(mappings);
        Exception ex = new RuntimeException("Test exception");

        // when
        ModelAndView mv = resolver.resolveException(request, response, null, ex);

        // then
        assertNotNull(mv);
        assertEquals("error", mv.getViewName());
        assertEquals(ex, mv.getModel().get("exception"));
    }

    @Test
    void shouldReturnNullWhenNoMappingFound() {
        // given
        Exception ex = new RuntimeException("Test exception");

        // when
        ModelAndView mv = resolver.resolveException(request, response, null, ex);

        // then
        assertNull(mv);
    }

    @Test
    void shouldUseCustomExceptionAttribute() {
        // given
        resolver.setDefaultErrorView("error");
        resolver.setExceptionAttribute("customException");
        Exception ex = new RuntimeException("Test exception");

        // when
        ModelAndView mv = resolver.resolveException(request, response, null, ex);

        // then
        assertNotNull(mv);
        assertEquals("error", mv.getViewName());
        assertEquals(ex, mv.getModel().get("customException"));
    }
} 