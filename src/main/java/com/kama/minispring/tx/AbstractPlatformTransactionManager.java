package com.kama.minispring.tx;

/**
 * 抽象的事务管理器基类
 * 实现了事务管理器的基本逻辑，子类只需要实现具体的事务操作
 *
 * @author kama
 * @version 1.0.0
 */
public abstract class AbstractPlatformTransactionManager implements PlatformTransactionManager {
    
    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        Object transaction = doGetTransaction();
        
        // 如果当前已存在事务，则根据传播行为处理
        if (isExistingTransaction(transaction)) {
            return handleExistingTransaction(definition, transaction);
        }
        
        // 检查传播行为
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
            throw new IllegalTransactionStateException("当前没有事务，但传播行为是MANDATORY");
        } else if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
                definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {
            // 创建新事务
            DefaultTransactionStatus status = newTransactionStatus(definition, transaction, true, true, false, null);
            TransactionSynchronizationManager.triggerBeforeBegin();
            doBegin(transaction, definition);
            prepareSynchronization(status, definition);
            return status;
        } else if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_SUPPORTS ||
                definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED ||
                definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER) {
            // 以非事务方式执行
            return newTransactionStatus(definition, null, false, false, false, null);
        }
        
        throw new TransactionException("不支持的传播行为: " + definition.getPropagationBehavior());
    }
    
    /**
     * 处理已存在事务的情况
     */
    private TransactionStatus handleExistingTransaction(TransactionDefinition definition, Object transaction)
            throws TransactionException {
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER) {
            throw new IllegalTransactionStateException("已存在事务，但传播行为是NEVER");
        }
        
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
            Object suspendedResources = suspend(transaction);
            return newTransactionStatus(definition, null, false, false, false, suspendedResources);
        }
        
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {
            Object suspendedResources = suspend(transaction);
            DefaultTransactionStatus status = newTransactionStatus(definition, transaction, true, true, false, suspendedResources);
            doBegin(transaction, definition);
            prepareSynchronization(status, definition);
            return status;
        }
        
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
            DefaultTransactionStatus status = newTransactionStatus(definition, transaction, false, false, true, null);
            doBegin(transaction, definition);
            prepareSynchronization(status, definition);
            return status;
        }
        
        // PROPAGATION_SUPPORTS或PROPAGATION_REQUIRED
        return newTransactionStatus(definition, transaction, false, false, false, null);
    }
    
    /**
     * 判断当前是否存在事务
     */
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        return false;
    }
    
    /**
     * 挂起当前事务
     */
    protected Object suspend(Object transaction) throws TransactionException {
        return null;
    }
    
    /**
     * 恢复被挂起的事务
     */
    protected void resume(Object transaction, Object suspendedResources) throws TransactionException {
        // 由子类实现
    }
    
    @Override
    public final void commit(TransactionStatus status) throws TransactionException {
        DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
        if (defStatus.isCompleted()) {
            throw new IllegalTransactionStateException("事务已经完成，不能重复提交");
        }
        
        try {
            // 触发事务提交前的同步回调
            TransactionSynchronizationManager.triggerBeforeCommit();
            
            // 如果事务被标记为回滚，则执行回滚
            if (defStatus.isRollbackOnly()) {
                processRollback(defStatus);
                return;
            }
            
            // 如果是新事务，则执行提交
            if (defStatus.isNewTransaction()) {
                doCommit(defStatus);
            }
            
            // 触发事务提交后的同步回调
            TransactionSynchronizationManager.triggerAfterCommit();
        } finally {
            // 触发事务完成后的同步回调
            TransactionSynchronizationManager.triggerAfterCompletion(TransactionSynchronization.STATUS_COMMITTED);
            cleanupAfterCompletion(defStatus);
        }
    }
    
    @Override
    public final void rollback(TransactionStatus status) throws TransactionException {
        if (status.isCompleted()) {
            throw new IllegalStateException("事务已经完成");
        }
        
        DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
        
        try {
            // 触发事务回滚前的同步回调
            TransactionSynchronizationManager.triggerBeforeRollback();
            doRollback(defStatus);
            // 触发事务回滚后的同步回调
            TransactionSynchronizationManager.triggerAfterRollback();
            // 触发事务完成后的同步回调
            TransactionSynchronizationManager.triggerAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        } finally {
            cleanupAfterCompletion(defStatus);
        }
    }
    
    protected final DefaultTransactionStatus newTransactionStatus(
            TransactionDefinition definition, Object transaction, boolean newSynchronization,
            boolean newTransaction, boolean debug, Object suspendedResources) {
        return new DefaultTransactionStatus(
                transaction, newTransaction, newSynchronization);
    }
    
    private void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
        if (status.isNewSynchronization()) {
            TransactionSynchronizationManager.setActualTransactionActive(status.hasTransaction());
        }
    }
    
    private void processRollback(DefaultTransactionStatus status) {
        try {
            doRollback(status);
        } finally {
            cleanupAfterCompletion(status);
        }
    }
    
    private void cleanupAfterCompletion(DefaultTransactionStatus status) {
        status.setCompleted();
        if (status.isNewSynchronization()) {
            TransactionSynchronizationManager.clear();
        }
    }
    
    /**
     * 获取事务
     *
     * @return 事务对象
     */
    protected abstract Object doGetTransaction();
    
    /**
     * 开始事务
     *
     * @param transaction 事务对象
     * @param definition 事务定义
     * @throws TransactionException 事务异常
     */
    protected abstract void doBegin(Object transaction, TransactionDefinition definition)
            throws TransactionException;
    
    /**
     * 提交事务
     *
     * @param status 事务状态
     * @throws TransactionException 事务异常
     */
    protected abstract void doCommit(DefaultTransactionStatus status)
            throws TransactionException;
    
    /**
     * 回滚事务
     *
     * @param status 事务状态
     * @throws TransactionException 事务异常
     */
    protected abstract void doRollback(DefaultTransactionStatus status)
            throws TransactionException;
} 