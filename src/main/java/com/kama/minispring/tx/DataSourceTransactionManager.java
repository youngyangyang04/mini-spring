package com.kama.minispring.tx;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 基于JDBC的事务管理器实现类
 *
 * @author kama
 * @version 1.0.0
 */
public class DataSourceTransactionManager extends AbstractPlatformTransactionManager {
    
    private DataSource dataSource;
    
    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public DataSource getDataSource() {
        return this.dataSource;
    }
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    protected Object doGetTransaction() {
        DataSourceTransactionObject txObject = new DataSourceTransactionObject();
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(this.dataSource);
        txObject.setConnectionHolder(conHolder, false);
        return txObject;
    }
    
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        
        try {
            if (!txObject.hasConnectionHolder()) {
                Connection newCon = this.dataSource.getConnection();
                ConnectionHolder conHolder = new ConnectionHolder(newCon);
                txObject.setConnectionHolder(conHolder, true);
            }
            
            ConnectionHolder conHolder = txObject.getConnectionHolder();
            conHolder.setTransactionActive(true);
            
            // 设置隔离级别
            if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
                int currentIsolation = conHolder.getConnection().getTransactionIsolation();
                if (currentIsolation != definition.getIsolationLevel()) {
                    conHolder.setPreviousIsolationLevel(currentIsolation);
                    conHolder.getConnection().setTransactionIsolation(definition.getIsolationLevel());
                }
            }
            
            // 开启事务
            conHolder.getConnection().setAutoCommit(false);
            
            // 绑定到当前线程
            TransactionSynchronizationManager.bindResource(this.dataSource, conHolder);
        } catch (SQLException ex) {
            throw new TransactionException("Could not open JDBC Connection for transaction", ex);
        }
    }
    
    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
        Connection con = txObject.getConnectionHolder().getConnection();
        try {
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            throw new TransactionException("Could not commit JDBC transaction", ex);
        }
    }
    
    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
        Connection con = txObject.getConnectionHolder().getConnection();
        try {
            con.rollback();
        } catch (SQLException ex) {
            throw new TransactionException("Could not roll back JDBC transaction", ex);
        }
    }
    
    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        return txObject.hasConnectionHolder() && txObject.getConnectionHolder().isTransactionActive();
    }
    
    @Override
    protected Object suspend(Object transaction) throws TransactionException {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        txObject.setConnectionHolder(null, false);
        return TransactionSynchronizationManager.unbindResource(this.dataSource);
    }
    
    @Override
    protected void resume(Object transaction, Object suspendedResources) throws TransactionException {
        if (suspendedResources != null) {
            TransactionSynchronizationManager.bindResource(this.dataSource, suspendedResources);
        }
    }
    
    /**
     * 清理事务完成后的资源
     */
    protected void doCleanupAfterCompletion(Object transaction) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        if (txObject.isNewConnectionHolder()) {
            TransactionSynchronizationManager.unbindResource(this.dataSource);
        }
        
        // 恢复之前的隔离级别
        ConnectionHolder conHolder = txObject.getConnectionHolder();
        if (conHolder != null) {
            conHolder.restorePreviousIsolationLevel();
            try {
                conHolder.getConnection().setAutoCommit(true);
            } catch (SQLException ex) {
                throw new TransactionException("Could not reset JDBC Connection after transaction", ex);
            }
            
            if (txObject.isNewConnectionHolder()) {
                try {
                    conHolder.getConnection().close();
                } catch (SQLException ex) {
                    throw new TransactionException("Could not close JDBC Connection after transaction", ex);
                }
            }
        }
    }
    
    /**
     * 事务对象，持有数据库连接
     */
    private static class DataSourceTransactionObject {
        private ConnectionHolder connectionHolder;
        private boolean newConnectionHolder;
        
        public boolean hasConnectionHolder() {
            return this.connectionHolder != null;
        }
        
        public void setConnectionHolder(ConnectionHolder connectionHolder, boolean newConnectionHolder) {
            this.connectionHolder = connectionHolder;
            this.newConnectionHolder = newConnectionHolder;
        }
        
        public ConnectionHolder getConnectionHolder() {
            return this.connectionHolder;
        }
        
        public boolean isNewConnectionHolder() {
            return this.newConnectionHolder;
        }
    }
    
    /**
     * 连接持有者，管理数据库连接
     */
    private static class ConnectionHolder {
        private Connection connection;
        private boolean transactionActive;
        private Integer previousIsolationLevel;
        
        public ConnectionHolder(Connection connection) {
            this.connection = connection;
        }
        
        public Connection getConnection() {
            return this.connection;
        }
        
        public boolean isTransactionActive() {
            return this.transactionActive;
        }
        
        public void setTransactionActive(boolean transactionActive) {
            this.transactionActive = transactionActive;
        }
        
        public Integer getPreviousIsolationLevel() {
            return this.previousIsolationLevel;
        }
        
        public void setPreviousIsolationLevel(Integer previousIsolationLevel) {
            this.previousIsolationLevel = previousIsolationLevel;
        }
        
        /**
         * 恢复之前的隔离级别
         */
        public void restorePreviousIsolationLevel() {
            if (this.previousIsolationLevel != null) {
                try {
                    this.connection.setTransactionIsolation(this.previousIsolationLevel);
                    this.previousIsolationLevel = null;
                } catch (SQLException ex) {
                    throw new TransactionException("Could not restore JDBC Connection isolation level", ex);
                }
            }
        }
    }
} 