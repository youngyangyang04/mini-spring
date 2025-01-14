package com.kama.minispring.tx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 事务同步测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class TransactionSynchronizationTest {
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    private DataSourceTransactionManager transactionManager;
    
    private List<String> executionOrder;
    
    private TestTransactionSynchronization synchronization;
    
    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        transactionManager = new DataSourceTransactionManager(dataSource);
        executionOrder = new ArrayList<>();
        synchronization = new TestTransactionSynchronization(executionOrder);
    }
    
    @Test
    public void testTransactionSynchronization() throws SQLException {
        // 创建事务定义
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setReadOnly(true);
        
        // 初始化同步
        TransactionSynchronizationManager.initSynchronization();
        
        // 注册事务同步
        TransactionSynchronizationManager.registerSynchronization(synchronization);
        
        // 开始事务
        TransactionStatus status = transactionManager.getTransaction(definition);
        
        // 提交事务
        transactionManager.commit(status);
        
        // 验证执行顺序
        assertEquals(4, executionOrder.size());
        assertEquals("beforeBegin", executionOrder.get(0));
        assertEquals("beforeCommit", executionOrder.get(1));
        assertEquals("afterCommit", executionOrder.get(2));
        assertEquals("afterCompletion", executionOrder.get(3));
        
        // 验证是否正确调用了数据库连接的方法
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
    }
    
    @Test
    public void testTransactionSynchronizationWithRollback() throws SQLException {
        // 创建事务定义
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        
        // 初始化同步
        TransactionSynchronizationManager.initSynchronization();
        
        // 注册事务同步
        TransactionSynchronizationManager.registerSynchronization(synchronization);
        
        // 开始事务
        TransactionStatus status = transactionManager.getTransaction(definition);
        
        // 回滚事务
        transactionManager.rollback(status);
        
        // 验证执行顺序
        assertEquals(4, executionOrder.size());
        assertEquals("beforeBegin", executionOrder.get(0));
        assertEquals("beforeRollback", executionOrder.get(1));
        assertEquals("afterRollback", executionOrder.get(2));
        assertEquals("afterCompletion", executionOrder.get(3));
        
        // 验证是否正确调用了数据库连接的方法
        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
    }
    
    private static class TestTransactionSynchronization implements TransactionSynchronization {
        
        private final List<String> executionOrder;
        
        public TestTransactionSynchronization(List<String> executionOrder) {
            this.executionOrder = executionOrder;
        }
        
        @Override
        public void beforeBegin() {
            executionOrder.add("beforeBegin");
        }
        
        @Override
        public void beforeCommit() {
            executionOrder.add("beforeCommit");
        }
        
        @Override
        public void afterCommit() {
            executionOrder.add("afterCommit");
        }
        
        @Override
        public void beforeRollback() {
            executionOrder.add("beforeRollback");
        }
        
        @Override
        public void afterRollback() {
            executionOrder.add("afterRollback");
        }
        
        @Override
        public void afterCompletion(int status) {
            executionOrder.add("afterCompletion");
        }
    }
} 