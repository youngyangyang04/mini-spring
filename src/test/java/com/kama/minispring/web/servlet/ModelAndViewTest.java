package com.kama.minispring.web.servlet;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModelAndView测试类
 *
 * @author kama
 * @version 1.0.0
 */
class ModelAndViewTest {
    
    @Test
    void testDefaultConstructor() {
        ModelAndView mv = new ModelAndView();
        assertNull(mv.getViewName());
        assertNotNull(mv.getModelInternal());
        assertTrue(mv.getModelInternal().isEmpty());
        assertFalse(mv.wasCleared());
    }
    
    @Test
    void testConstructorWithViewName() {
        String viewName = "testView";
        ModelAndView mv = new ModelAndView(viewName);
        assertEquals(viewName, mv.getViewName());
        assertNotNull(mv.getModelInternal());
        assertTrue(mv.getModelInternal().isEmpty());
    }
    
    @Test
    void testConstructorWithViewNameAndModel() {
        String viewName = "testView";
        Map<String, Object> model = new HashMap<>();
        model.put("key1", "value1");
        model.put("key2", "value2");
        
        ModelAndView mv = new ModelAndView(viewName, model);
        
        assertEquals(viewName, mv.getViewName());
        assertNotNull(mv.getModelInternal());
        assertEquals(2, mv.getModelInternal().size());
        assertEquals("value1", mv.getModelInternal().get("key1"));
        assertEquals("value2", mv.getModelInternal().get("key2"));
    }
    
    @Test
    void testSetViewName() {
        ModelAndView mv = new ModelAndView();
        String viewName = "testView";
        mv.setViewName(viewName);
        assertEquals(viewName, mv.getViewName());
    }
    
    @Test
    void testAddAttribute() {
        ModelAndView mv = new ModelAndView();
        mv.addAttribute("key", "value");
        
        assertEquals(1, mv.getModelInternal().size());
        assertEquals("value", mv.getModelInternal().get("key"));
    }
    
    @Test
    void testAddAllAttributes() {
        ModelAndView mv = new ModelAndView();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        
        mv.addAllAttributes(attributes);
        
        assertEquals(2, mv.getModelInternal().size());
        assertEquals("value1", mv.getModelInternal().get("key1"));
        assertEquals("value2", mv.getModelInternal().get("key2"));
    }
    
    @Test
    void testClear() {
        ModelAndView mv = new ModelAndView();
        mv.addAttribute("key", "value");
        assertFalse(mv.wasCleared());
        
        mv.clear();
        
        assertTrue(mv.wasCleared());
        assertTrue(mv.getModelInternal().isEmpty());
    }
    
    @Test
    void testMethodChaining() {
        ModelAndView mv = new ModelAndView()
            .addAttribute("key1", "value1")
            .addAttribute("key2", "value2");
        
        assertEquals(2, mv.getModelInternal().size());
        assertEquals("value1", mv.getModelInternal().get("key1"));
        assertEquals("value2", mv.getModelInternal().get("key2"));
    }
} 