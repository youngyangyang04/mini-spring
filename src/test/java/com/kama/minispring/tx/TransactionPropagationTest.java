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
 * 事务传播行为测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class TransactionPropagationTest {
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    private DataSourceTransactionManager transactionManager;
    
    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        transactionManager = new DataSourceTransactionManager(dataSource);
    }
    
    @Test
    void testPropagationRequired() throws SQLException {
        // 第一个事务
        DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
        def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status1 = transactionManager.getTransaction(def1);
        assertNotNull(status1);
        assertFalse(status1.isCompleted());
        
        verify(connection).setAutoCommit(false);
        
        // 第二个事务（应该加入第一个事务）
        DefaultTransactionDefinition def2 = new DefaultTransactionDefinition();
        def2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status2 = transactionManager.getTransaction(def2);
        assertNotNull(status2);
        assertFalse(status2.isCompleted());
        
        // 验证没有创建新的连接
        verify(dataSource, times(1)).getConnection();
        
        // 提交事务
        transactionManager.commit(status2);
        transactionManager.commit(status1);
        
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
    }
    
    @Test
    void testPropagationRequiresNew() throws SQLException {
        // 第一个事务
        DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
        def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status1 = transactionManager.getTransaction(def1);
        
        // 第二个事务（应该创建新事务）
        DefaultTransactionDefinition def2 = new DefaultTransactionDefinition();
        def2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        
        Connection connection2 = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection2);
        
        TransactionStatus status2 = transactionManager.getTransaction(def2);
        
        // 验证创建了新的连接
        verify(dataSource, times(2)).getConnection();
        
        // 提交事务
        transactionManager.commit(status2);
        transactionManager.commit(status1);
        
        verify(connection).commit();
        verify(connection2).commit();
    }
    
    @Test
    void testPropagationNested() throws SQLException {
        // 第一个事务
        DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
        def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status1 = transactionManager.getTransaction(def1);
        
        // 第二个事务（应该创建嵌套事务）
        DefaultTransactionDefinition def2 = new DefaultTransactionDefinition();
        def2.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
        
        TransactionStatus status2 = transactionManager.getTransaction(def2);
        
        // 验证使用了同一个连接
        verify(dataSource, times(1)).getConnection();
        
        // 回滚嵌套事务
        transactionManager.rollback(status2);
        
        // 提交外部事务
        transactionManager.commit(status1);
        
        verify(connection).commit();
    }
} 