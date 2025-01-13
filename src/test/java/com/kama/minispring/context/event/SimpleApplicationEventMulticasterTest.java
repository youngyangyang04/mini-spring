package com.kama.minispring.context.event;

import com.kama.minispring.context.ApplicationEvent;
import com.kama.minispring.context.ApplicationListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SimpleApplicationEventMulticaster的测试类
 *
 * @author kama
 * @version 1.0.0
 */
class SimpleApplicationEventMulticasterTest {
    
    private SimpleApplicationEventMulticaster multicaster;
    private TestListener testListener;
    
    @BeforeEach
    void setUp() {
        multicaster = new SimpleApplicationEventMulticaster();
        testListener = new TestListener();
        multicaster.addApplicationListener(testListener);
    }
    
    @Test
    void testSynchronousEventMulticasting() {
        TestEvent event = new TestEvent(this);
        multicaster.multicastEvent(event);
        
        assertEquals(1, testListener.getEventCount());
        assertSame(event, testListener.getLastEvent());
    }
    
    @Test
    void testAsynchronousEventMulticasting() throws InterruptedException {
        // 设置异步执行器
        Executor executor = Executors.newSingleThreadExecutor();
        multicaster.setTaskExecutor(executor);
        
        // 使用CountDownLatch等待异步事件处理完成
        CountDownLatch latch = new CountDownLatch(1);
        TestListener asyncListener = new TestListener() {
            @Override
            public void onApplicationEvent(TestEvent event) {
                super.onApplicationEvent(event);
                latch.countDown();
            }
        };
        
        // 移除之前的监听器，只使用新的异步监听器
        multicaster.removeAllListeners();
        multicaster.addApplicationListener(asyncListener);
        
        TestEvent event = new TestEvent(this);
        multicaster.multicastEvent(event);
        
        // 等待事件处理完成
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(1, asyncListener.getEventCount());
        assertSame(event, asyncListener.getLastEvent());
    }
    
    @Test
    void testListenerRemoval() {
        multicaster.removeApplicationListener(testListener);
        multicaster.multicastEvent(new TestEvent(this));
        
        assertEquals(0, testListener.getEventCount());
    }
    
    @Test
    void testRemoveAllListeners() {
        multicaster.removeAllListeners();
        multicaster.multicastEvent(new TestEvent(this));
        
        assertEquals(0, testListener.getEventCount());
    }
    
    @Test
    void testEventTypeFiltering() {
        // 发送不同类型的事件
        multicaster.multicastEvent(new TestEvent(this));
        multicaster.multicastEvent(new OtherEvent(this));
        
        // 只有TestEvent应该被处理
        assertEquals(1, testListener.getEventCount());
        assertTrue(testListener.getLastEvent() instanceof TestEvent);
    }
    
    // 测试用的监听器
    private static class TestListener implements ApplicationListener<TestEvent> {
        private final AtomicInteger eventCount = new AtomicInteger(0);
        private volatile ApplicationEvent lastEvent;
        
        @Override
        public void onApplicationEvent(TestEvent event) {
            eventCount.incrementAndGet();
            lastEvent = event;
        }
        
        public int getEventCount() {
            return eventCount.get();
        }
        
        public ApplicationEvent getLastEvent() {
            return lastEvent;
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