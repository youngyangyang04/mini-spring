package com.kama.minispring.tx;

/**
 * 事务定义接口
 * 定义了事务的基本属性
 *
 * @author kama
 * @version 1.0.0
 */
public interface TransactionDefinition {
    
    /** 默认的事务隔离级别：使用数据库的默认隔离级别 */
    int ISOLATION_DEFAULT = -1;
    
    /** 事务隔离级别：读未提交 */
    int ISOLATION_READ_UNCOMMITTED = 1;
    
    /** 事务隔离级别：读已提交 */
    int ISOLATION_READ_COMMITTED = 2;
    
    /** 事务隔离级别：可重复读 */
    int ISOLATION_REPEATABLE_READ = 4;
    
    /** 事务隔离级别：串行化 */
    int ISOLATION_SERIALIZABLE = 8;
    
    /** 默认的事务传播行为：如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务 */
    int PROPAGATION_REQUIRED = 0;
    
    /** 事务传播行为：如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务方式执行 */
    int PROPAGATION_SUPPORTS = 1;
    
    /** 事务传播行为：如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常 */
    int PROPAGATION_MANDATORY = 2;
    
    /** 事务传播行为：创建一个新的事务，如果当前存在事务，则把当前事务挂起 */
    int PROPAGATION_REQUIRES_NEW = 3;
    
    /** 事务传播行为：以非事务方式执行，如果当前存在事务，则把当前事务挂起 */
    int PROPAGATION_NOT_SUPPORTED = 4;
    
    /** 事务传播行为：以非事务方式执行，如果当前存在事务，则抛出异常 */
    int PROPAGATION_NEVER = 5;
    
    /** 事务传播行为：如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行 */
    int PROPAGATION_NESTED = 6;
    
    /** 默认超时时间：永不超时 */
    int TIMEOUT_DEFAULT = -1;
    
    /**
     * 获取事务传播行为
     *
     * @return 事务传播行为
     */
    int getPropagationBehavior();
    
    /**
     * 获取事务隔离级别
     *
     * @return 事务隔离级别
     */
    int getIsolationLevel();
    
    /**
     * 获取事务超时时间
     *
     * @return 事务超时时间（秒）
     */
    int getTimeout();
    
    /**
     * 是否是只读事务
     *
     * @return 如果是只读事务返回true，否则返回false
     */
    boolean isReadOnly();
    
    /**
     * 获取事务名称
     *
     * @return 事务名称
     */
    String getName();
} 