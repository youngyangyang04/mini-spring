package com.kama.minispring.context.event;

import com.kama.minispring.context.ApplicationContext;
import com.kama.minispring.context.ApplicationEvent;
import com.kama.minispring.context.ApplicationListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 应用事件相关类的测试
 *
 * @author kama
 * @version 1.0.0
 */
class ApplicationEventTest {
    
    @Mock
    private ApplicationContext mockContext;
    
    private SimpleApplicationEventMulticaster eventMulticaster;
    private TestApplicationListener testListener;
    private static final Instant FIXED_TIME = Instant.parse("2024-01-10T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_TIME, ZoneId.systemDefault());
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventMulticaster = new SimpleApplicationEventMulticaster();
        testListener = new TestApplicationListener();
        eventMulticaster.addApplicationListener(testListener);
    }
    
    @Test
    void testContextRefreshedEvent() {
        // 创建并发布ContextRefreshedEvent
        ContextRefreshedEvent event = new ContextRefreshedEvent(mockContext);
        eventMulticaster.multicastEvent(event);
        
        // 验证监听器是否正确接收到事件
        assertEquals(1, testListener.getRefreshedEvents().size());
        assertSame(event, testListener.getRefreshedEvents().get(0));
        assertEquals(0, testListener.getClosedEvents().size());
    }
    
    @Test
    void testContextClosedEvent() {
        // 创建并发布ContextClosedEvent
        ContextClosedEvent event = new ContextClosedEvent(mockContext);
        eventMulticaster.multicastEvent(event);
        
        // 验证监听器是否正确接收到事件
        assertEquals(1, testListener.getClosedEvents().size());
        assertSame(event, testListener.getClosedEvents().get(0));
        assertEquals(0, testListener.getRefreshedEvents().size());
    }
    
    @Test
    void testEventTimestamp() {
        // 使用固定时间创建事件
        TestEvent event = new TestEvent("test", FIXED_CLOCK);
        assertEquals(FIXED_TIME, event.getTimestamp());
    }
    
    @Test
    void testEventSource() {
        String source = "testSource";
        TestEvent event = new TestEvent(source);
        assertEquals(source, event.getSource());
    }
    
    @Test
    void testNullSourceThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new TestEvent(null));
    }
    
    @Test
    void testListenerRemoval() {
        eventMulticaster.removeApplicationListener(testListener);
        ContextRefreshedEvent event = new ContextRefreshedEvent(mockContext);
        eventMulticaster.multicastEvent(event);
        
        assertEquals(0, testListener.getRefreshedEvents().size());
    }
    
    @Test
    void testRemoveAllListeners() {
        eventMulticaster.removeAllListeners();
        ContextRefreshedEvent event = new ContextRefreshedEvent(mockContext);
        eventMulticaster.multicastEvent(event);
        
        assertEquals(0, testListener.getRefreshedEvents().size());
    }
    
    // 测试用的事件类
    private static class TestEvent extends ApplicationEvent {
        public TestEvent(Object source) {
            super(source);
        }
        
        public TestEvent(Object source, Clock clock) {
            super(source, clock);
        }
    }
    
    // 测试用的监听器类
    private static class TestApplicationListener implements ApplicationListener<ApplicationEvent> {
        private final List<ContextRefreshedEvent> refreshedEvents = new ArrayList<>();
        private final List<ContextClosedEvent> closedEvents = new ArrayList<>();
        
        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ContextRefreshedEvent) {
                refreshedEvents.add((ContextRefreshedEvent) event);
            } else if (event instanceof ContextClosedEvent) {
                closedEvents.add((ContextClosedEvent) event);
            }
        }
        
        public List<ContextRefreshedEvent> getRefreshedEvents() {
            return refreshedEvents;
        }
        
        public List<ContextClosedEvent> getClosedEvents() {
            return closedEvents;
        }
    }
} 