package com.kama.minispring.tx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事务同步管理器
 * 用于管理事务的同步状态，基于ThreadLocal实现
 *
 * @author kama
 * @version 1.0.0
 */
public abstract class TransactionSynchronizationManager {
    
    private static final ThreadLocal<Map<Object, Object>> resources = new ThreadLocal<>();
    
    private static final ThreadLocal<Boolean> synchronizationActive = new ThreadLocal<>();
    
    private static final ThreadLocal<Boolean> actualTransactionActive = new ThreadLocal<>();
    
    private static final ThreadLocal<List<TransactionSynchronization>> synchronizations = new ThreadLocal<>();
    
    public static Map<Object, Object> getResources() {
        Map<Object, Object> map = resources.get();
        return map != null ? map : new HashMap<>();
    }
    
    public static boolean hasResource(Object key) {
        Map<Object, Object> map = resources.get();
        return map != null && map.containsKey(key);
    }
    
    public static Object getResource(Object key) {
        Map<Object, Object> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }
    
    public static void bindResource(Object key, Object value) {
        Map<Object, Object> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        map.put(key, value);
    }
    
    public static Object unbindResource(Object key) {
        Map<Object, Object> map = resources.get();
        if (map == null) {
            return null;
        }
        Object value = map.remove(key);
        if (map.isEmpty()) {
            resources.remove();
        }
        return value;
    }
    
    public static void initSynchronization() {
        if (synchronizationActive.get() != null) {
            throw new IllegalStateException("事务同步已经初始化");
        }
        synchronizationActive.set(true);
        synchronizations.set(new ArrayList<>());
    }
    
    public static void clearSynchronization() {
        synchronizationActive.remove();
        actualTransactionActive.remove();
        synchronizations.remove();
    }
    
    public static boolean isSynchronizationActive() {
        return synchronizationActive.get() != null && synchronizationActive.get();
    }
    
    public static void setActualTransactionActive(boolean active) {
        actualTransactionActive.set(active);
    }
    
    public static boolean isActualTransactionActive() {
        return actualTransactionActive.get() != null && actualTransactionActive.get();
    }
    
    public static void registerSynchronization(TransactionSynchronization synchronization) {
        if (!isSynchronizationActive()) {
            throw new IllegalStateException("事务同步未激活");
        }
        synchronizations.get().add(synchronization);
    }
    
    public static List<TransactionSynchronization> getSynchronizations() {
        return synchronizations.get();
    }
    
    public static void triggerBeforeBegin() {
        List<TransactionSynchronization> synchs = getSynchronizations();
        if (synchs != null) {
            for (TransactionSynchronization synch : synchs) {
                synch.beforeBegin();
            }
        }
    }
    
    /**
     * 触发事务提交前的同步回调
     */
    public static void triggerBeforeCommit() {
        List<TransactionSynchronization> synchs = synchronizations.get();
        if (synchs != null) {
            for (TransactionSynchronization synchronization : synchs) {
                synchronization.beforeCommit();
            }
        }
    }
    
    /**
     * 触发事务提交后的同步回调
     */
    public static void triggerAfterCommit() {
        List<TransactionSynchronization> synchs = synchronizations.get();
        if (synchs != null) {
            for (TransactionSynchronization synchronization : synchs) {
                synchronization.afterCommit();
            }
        }
    }
    
    /**
     * 触发事务回滚前的同步回调
     */
    public static void triggerBeforeRollback() {
        List<TransactionSynchronization> synchs = synchronizations.get();
        if (synchs != null) {
            for (TransactionSynchronization synchronization : synchs) {
                synchronization.beforeRollback();
            }
        }
    }
    
    /**
     * 触发事务回滚后的同步回调
     */
    public static void triggerAfterRollback() {
        List<TransactionSynchronization> synchs = synchronizations.get();
        if (synchs != null) {
            for (TransactionSynchronization synchronization : synchs) {
                synchronization.afterRollback();
            }
        }
    }
    
    /**
     * 触发事务完成后的同步回调
     */
    public static void triggerAfterCompletion(int status) {
        List<TransactionSynchronization> synchs = synchronizations.get();
        if (synchs != null) {
            for (TransactionSynchronization synchronization : synchs) {
                synchronization.afterCompletion(status);
            }
        }
    }
    
    public static void clear() {
        synchronizationActive.remove();
        actualTransactionActive.remove();
        resources.remove();
        synchronizations.remove();
    }
} 