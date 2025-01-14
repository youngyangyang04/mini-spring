package com.kama.minispring.tx;

/**
 * 默认的事务定义实现类
 *
 * @author kama
 * @version 1.0.0
 */
public class DefaultTransactionDefinition implements TransactionDefinition {
    
    private int propagationBehavior = PROPAGATION_REQUIRED;
    
    private int isolationLevel = ISOLATION_DEFAULT;
    
    private int timeout = TIMEOUT_DEFAULT;
    
    private boolean readOnly = false;
    
    private String name;
    
    public DefaultTransactionDefinition() {
    }
    
    public DefaultTransactionDefinition(int propagationBehavior) {
        this.propagationBehavior = propagationBehavior;
    }
    
    @Override
    public int getPropagationBehavior() {
        return this.propagationBehavior;
    }
    
    public void setPropagationBehavior(int propagationBehavior) {
        this.propagationBehavior = propagationBehavior;
    }
    
    @Override
    public int getIsolationLevel() {
        return this.isolationLevel;
    }
    
    public void setIsolationLevel(int isolationLevel) {
        this.isolationLevel = isolationLevel;
    }
    
    @Override
    public int getTimeout() {
        return this.timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
} 