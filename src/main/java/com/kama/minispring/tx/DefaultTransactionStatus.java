package com.kama.minispring.tx;

/**
 * 默认的事务状态实现类
 *
 * @author kama
 * @version 1.0.0
 */
public class DefaultTransactionStatus implements TransactionStatus {
    
    private final Object transaction;
    
    private final boolean newTransaction;
    
    private final boolean newSynchronization;
    
    private boolean rollbackOnly = false;
    
    private boolean completed = false;
    
    private boolean readOnly = false;
    
    private Object savepoint;
    
    public DefaultTransactionStatus(
            Object transaction, boolean newTransaction, boolean newSynchronization) {
        this.transaction = transaction;
        this.newTransaction = newTransaction;
        this.newSynchronization = newSynchronization;
    }
    
    public Object getTransaction() {
        return this.transaction;
    }
    
    @Override
    public boolean isNewTransaction() {
        return this.newTransaction;
    }
    
    public boolean isNewSynchronization() {
        return this.newSynchronization;
    }
    
    public void setSavepoint(Object savepoint) {
        this.savepoint = savepoint;
    }
    
    @Override
    public boolean hasSavepoint() {
        return this.savepoint != null;
    }
    
    public Object getSavepoint() {
        return this.savepoint;
    }
    
    @Override
    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }
    
    @Override
    public boolean isRollbackOnly() {
        return this.rollbackOnly;
    }
    
    @Override
    public void flush() {
        // 默认实现为空
    }
    
    public void setCompleted() {
        this.completed = true;
    }
    
    @Override
    public boolean isCompleted() {
        return this.completed;
    }
    
    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    public boolean hasTransaction() {
        return this.transaction != null;
    }
} 