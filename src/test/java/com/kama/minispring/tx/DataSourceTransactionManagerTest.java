package com.kama.minispring.tx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 事务管理器测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class DataSourceTransactionManagerTest {
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    private DataSourceTransactionManager transactionManager;
    
    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        transactionManager = new DataSourceTransactionManager(dataSource);
    }
    
    @Test
    public void testBeginTransaction() throws SQLException {
        // 创建事务定义
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        
        // 开始事务
        TransactionStatus status = transactionManager.getTransaction(definition);
        
        // 验证事务是否正确开始
        assertNotNull(status);
        assertTrue(status.isNewTransaction());
        
        // 验证是否正确设置了连接属性
        verify(connection).setTransactionIsolation(TransactionDefinition.ISOLATION_READ_COMMITTED);
        verify(connection).setAutoCommit(false);
    }
    
    @Test
    public void testCommitTransaction() throws SQLException {
        // 创建事务定义
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        
        // 开始事务
        TransactionStatus status = transactionManager.getTransaction(definition);
        
        // 提交事务
        transactionManager.commit(status);
        
        // 验证是否调用了commit方法
        verify(connection).commit();
        assertTrue(status.isCompleted());
    }
    
    @Test
    public void testRollbackTransaction() throws SQLException {
        // 创建事务定义
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        
        // 开始事务
        TransactionStatus status = transactionManager.getTransaction(definition);
        
        // 回滚事务
        transactionManager.rollback(status);
        
        // 验证是否调用了rollback方法
        verify(connection).rollback();
        assertTrue(status.isCompleted());
    }
    
    @Test
    public void testRollbackOnlyTransaction() throws SQLException {
        // 创建事务定义
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        
        // 开始事务
        TransactionStatus status = transactionManager.getTransaction(definition);
        
        // 设置为只回滚
        status.setRollbackOnly();
        
        // 尝试提交事务（应该会回滚）
        transactionManager.commit(status);
        
        // 验证是否调用了rollback方法而不是commit方法
        verify(connection, never()).commit();
        verify(connection).rollback();
        assertTrue(status.isCompleted());
    }
    
    @Test
    public void testTransactionWithException() throws SQLException {
        // 创建事务定义
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        
        // 开始事务
        TransactionStatus status = transactionManager.getTransaction(definition);
        
        // 模拟提交时发生异常
        doThrow(new SQLException("Commit failed")).when(connection).commit();
        
        // 验证是否抛出了TransactionException
        assertThrows(TransactionException.class, () -> transactionManager.commit(status));
        assertTrue(status.isCompleted());
    }
} 