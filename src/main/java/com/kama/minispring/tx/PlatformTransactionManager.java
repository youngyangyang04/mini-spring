package com.kama.minispring.tx;

/**
 * 事务管理器接口
 * 定义了事务管理的基本操作
 *
 * @author kama
 * @version 1.0.0
 */
public interface PlatformTransactionManager {
    
    /**
     * 获取事务状态
     *
     * @param definition 事务定义
     * @return 事务状态
     * @throws TransactionException 事务异常
     */
    TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException;
    
    /**
     * 提交事务
     *
     * @param status 事务状态
     * @throws TransactionException 事务异常
     */
    void commit(TransactionStatus status) throws TransactionException;
    
    /**
     * 回滚事务
     *
     * @param status 事务状态
     * @throws TransactionException 事务异常
     */
    void rollback(TransactionStatus status) throws TransactionException;
} 