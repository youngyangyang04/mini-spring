package com.kama.minispring.context.event;

import com.kama.minispring.context.ApplicationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApplicationListenerAdapter的测试类
 *
 * @author kama
 * @version 1.0.0
 */
class ApplicationListenerAdapterTest {
    
    private TestEventHandler testEventHandler;
    private ApplicationListenerAdapter adapter;
    private Method handleEventMethod;
    
    @BeforeEach
    void setUp() throws NoSuchMethodException {
        testEventHandler = new TestEventHandler();
        handleEventMethod = TestEventHandler.class.getMethod("handleEvent", TestEvent.class);
        adapter = new ApplicationListenerAdapter(testEventHandler, handleEventMethod, TestEvent.class);
    }
    
    @Test
    void testEventHandling() {
        // 创建测试事件
        TestEvent event = new TestEvent("test");
        adapter.onApplicationEvent(event);
        
        // 验证事件是否被正确处理
        assertTrue(testEventHandler.isEventHandled());
        assertEquals(event, testEventHandler.getLastHandledEvent());
    }
    
    @Test
    void testEventTypeFiltering() {
        // 创建不匹配的事件类型
        OtherEvent event = new OtherEvent("test");
        adapter.onApplicationEvent(event);
        
        // 验证不匹配的事件是否被正确过滤
        assertFalse(testEventHandler.isEventHandled());
        assertNull(testEventHandler.getLastHandledEvent());
    }
    
    @Test
    void testEqualsAndHashCode() {
        // 创建相同的适配器
        ApplicationListenerAdapter adapter2 = new ApplicationListenerAdapter(
            testEventHandler, handleEventMethod, TestEvent.class);
        
        // 创建不同的适配器
        TestEventHandler otherHandler = new TestEventHandler();
        ApplicationListenerAdapter adapter3 = new ApplicationListenerAdapter(
            otherHandler, handleEventMethod, TestEvent.class);
        
        // 测试相等性
        assertEquals(adapter, adapter2);
        assertNotEquals(adapter, adapter3);
        
        // 测试哈希码
        assertEquals(adapter.hashCode(), adapter2.hashCode());
        assertNotEquals(adapter.hashCode(), adapter3.hashCode());
    }
    
    @Test
    void testToString() {
        String toString = adapter.toString();
        assertTrue(toString.contains(testEventHandler.toString()));
        assertTrue(toString.contains(handleEventMethod.toString()));
    }
    
    // 测试用的事件处理器
    private static class TestEventHandler {
        private boolean eventHandled = false;
        private TestEvent lastHandledEvent;
        
        public void handleEvent(TestEvent event) {
            this.eventHandled = true;
            this.lastHandledEvent = event;
        }
        
        public boolean isEventHandled() {
            return eventHandled;
        }
        
        public TestEvent getLastHandledEvent() {
            return lastHandledEvent;
        }
    }
    
    // 测试用的事件类
    private static class TestEvent extends ApplicationEvent {
        public TestEvent(Object source) {
            super(source);
        }
    }
    
    // 其他测试用的事件类
    private static class OtherEvent extends ApplicationEvent {
        public OtherEvent(Object source) {
            super(source);
        }
    }
} 