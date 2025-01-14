package com.kama.minispring.boot.autoconfigure;

import com.kama.minispring.beans.factory.annotation.Autowired;
import com.kama.minispring.context.annotation.Conditional;
import com.kama.minispring.core.env.Environment;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 数据源自动配置类，用于演示自动配置功能
 * 
 * @author kama
 * @version 1.0.0
 */
@Conditional(DataSourceCondition.class)
@RequiresEnvironment
public class DataSourceAutoConfiguration implements AutoConfiguration {
    
    @Autowired
    private Environment environment;
    
    @Override
    public void configure() {
        // 从环境中读取数据源配置
        Properties properties = new Properties();
        properties.setProperty("driverClassName", 
            environment.getProperty("spring.datasource.driver-class-name"));
        properties.setProperty("url", 
            environment.getProperty("spring.datasource.url"));
        properties.setProperty("username", 
            environment.getProperty("spring.datasource.username"));
        properties.setProperty("password", 
            environment.getProperty("spring.datasource.password"));
        
        // 创建数据源
        createDataSource(properties);
    }
    
    private DataSource createDataSource(Properties properties) {
        try {
            Class.forName(properties.getProperty("driverClassName"));
            // 这里简化实现，实际应该使用连接池
            return new SimpleDataSource(properties);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to create DataSource", e);
        }
    }
    
    /**
     * 简单数据源实现，仅用于演示
     */
    private static class SimpleDataSource implements DataSource {
        private final Properties properties;
        private PrintWriter logWriter;
        private int loginTimeout;
        
        public SimpleDataSource(Properties properties) {
            this.properties = properties;
        }
        
        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password")
            );
        }
        
        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return DriverManager.getConnection(
                properties.getProperty("url"),
                username,
                password
            );
        }
        
        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return logWriter;
        }
        
        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            this.logWriter = out;
        }
        
        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            this.loginTimeout = seconds;
        }
        
        @Override
        public int getLoginTimeout() throws SQLException {
            return loginTimeout;
        }
        
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            if (iface.isInstance(this)) {
                return iface.cast(this);
            }
            throw new SQLException("DataSource of type [" + getClass().getName() +
                "] cannot be unwrapped as [" + iface.getName() + "]");
        }
        
        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return iface.isInstance(this);
        }
    }
} 