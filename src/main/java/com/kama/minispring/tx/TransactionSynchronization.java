package com.kama.minispring.tx;

/**
 * 事务同步接口
 * 用于在事务执行的不同阶段进行回调
 *
 * @author kama
 * @version 1.0.0
 */
public interface TransactionSynchronization {
    
    /** 事务同步的优先级：最低 */
    int STATUS_COMMITTED = 0;
    
    /** 事务同步的优先级：已回滚 */
    int STATUS_ROLLED_BACK = 1;
    
    /** 事务同步的优先级：未知状态 */
    int STATUS_UNKNOWN = 2;
    
    /**
     * 在事务开始之前调用
     */
    default void beforeBegin() {
    }
    
    /**
     * 事务提交前的回调
     */
    default void beforeCommit() {
    }
    
    /**
     * 在事务提交之后调用
     */
    default void afterCommit() {
    }
    
    /**
     * 在事务完成之后调用
     *
     * @param status 事务状态
     */
    default void afterCompletion(int status) {
    }
    
    /**
     * 在事务回滚之前调用
     */
    default void beforeRollback() {
    }
    
    /**
     * 在事务回滚之后调用
     */
    default void afterRollback() {
    }
    
    /**
     * 刷新事务相关的资源
     */
    default void flush() {
    }
    
    /**
     * 在事务挂起之前调用
     */
    default void suspend() {
    }
    
    /**
     * 在事务恢复之前调用
     */
    default void resume() {
    }
} 