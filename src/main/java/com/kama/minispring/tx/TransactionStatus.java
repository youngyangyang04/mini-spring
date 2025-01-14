package com.kama.minispring.tx;

/**
 * 事务状态接口
 * 定义了事务的状态信息
 *
 * @author kama
 * @version 1.0.0
 */
public interface TransactionStatus {
    
    /**
     * 是否是新事务
     *
     * @return 如果是新事务返回true，否则返回false
     */
    boolean isNewTransaction();
    
    /**
     * 是否有保存点
     *
     * @return 如果有保存点返回true，否则返回false
     */
    boolean hasSavepoint();
    
    /**
     * 设置为只回滚
     */
    void setRollbackOnly();
    
    /**
     * 是否为只回滚
     *
     * @return 如果是只回滚返回true，否则返回false
     */
    boolean isRollbackOnly();
    
    /**
     * 刷新事务
     */
    void flush();
    
    /**
     * 是否已完成
     *
     * @return 如果已完成返回true，否则返回false
     */
    boolean isCompleted();
    
    /**
     * 是否是只读事务
     *
     * @return 如果是只读事务返回true，否则返回false
     */
    boolean isReadOnly();
} 