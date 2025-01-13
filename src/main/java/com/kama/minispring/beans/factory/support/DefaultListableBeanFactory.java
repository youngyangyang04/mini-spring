package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.BeanFactory;
import com.kama.minispring.beans.factory.ConfigurableListableBeanFactory;
import com.kama.minispring.beans.factory.ListableBeanFactory;
import com.kama.minispring.beans.factory.ObjectFactory;
import com.kama.minispring.beans.factory.Aware;
import com.kama.minispring.beans.factory.BeanFactoryAware;
import com.kama.minispring.beans.factory.BeanNameAware;
import com.kama.minispring.beans.factory.DisposableBean;
import com.kama.minispring.beans.factory.InitializingBean;
import com.kama.minispring.beans.factory.config.BeanDefinition;
import com.kama.minispring.beans.factory.config.BeanDefinitionHolder;
import com.kama.minispring.beans.factory.config.BeanPostProcessor;
import com.kama.minispring.beans.factory.config.ConfigurableBeanFactory;
import com.kama.minispring.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的可列表化bean工厂实现
 * 提供了bean定义的注册和获取功能
 *
 * @author kama
 * @version 1.0.0
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
        implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultListableBeanFactory.class);

    /** Map of bean definition objects, keyed by bean name */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    /** List of bean definition names, in registration order */
    private volatile List<String> beanDefinitionNames = new ArrayList<>(256);

    /** Map from bean name to merged bean definition */
    private final Map<String, BeanDefinition> mergedBeanDefinitions = new ConcurrentHashMap<>(256);

    /** Names of beans that are currently in creation */
    private final Set<String> singletonsCurrentlyInCreation =
            Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    /** Cache of singleton objects: bean name to bean instance */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /** Cache of early singleton objects: bean name to bean instance */
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);

    /** Cache of singleton factories: bean name to ObjectFactory */
    private final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>(16);

    private ClassLoader beanClassLoader = Thread.currentThread().getContextClassLoader();
    private ConfigurableBeanFactory parentBeanFactory;
    private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);
    private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64);
    private final Set<String> beansInCreation = Collections.newSetFromMap(new ConcurrentHashMap<>(16));
    private final Map<String, Set<String>> dependencyGraph = new ConcurrentHashMap<>(64);

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        Objects.requireNonNull(beanName, "Bean name must not be null");
        Objects.requireNonNull(beanDefinition, "BeanDefinition must not be null");
        
        // 检查是否存在旧的bean定义
        BeanDefinition oldBeanDefinition = this.beanDefinitionMap.get(beanName);
        if (oldBeanDefinition != null) {
            // 如果作用域发生变化，需要清理相关缓存
            if (!Objects.equals(oldBeanDefinition.getScope(), beanDefinition.getScope())) {
                cleanupSingletonCache(beanName);
                // 移除旧的bean定义
                this.beanDefinitionMap.remove(beanName);
                this.mergedBeanDefinitions.remove(beanName);
                // 处理别名
                String[] aliases = getAliases(beanName);
                for (String alias : aliases) {
                    cleanupSingletonCache(alias);
                    this.mergedBeanDefinitions.remove(alias);
                }
            }
        }
        
        this.beanDefinitionMap.put(beanName, beanDefinition);
        
        // 如果是新的bean定义，添加到名称列表中
        if (!this.beanDefinitionNames.contains(beanName)) {
            this.beanDefinitionNames.add(beanName);
        }
        
        logger.debug("Registered bean definition for bean named '{}'", beanName);
    }

    @Override
    public void removeBeanDefinition(String beanName) throws BeansException {
        if (!containsBeanDefinition(beanName)) {
            throw new BeansException("No bean named '" + beanName + "' is defined");
        }
        this.beanDefinitionMap.remove(beanName);
        this.beanDefinitionNames.remove(beanName);
        logger.debug("Removed bean definition for bean named '{}'", beanName);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        String canonicalName = canonicalName(beanName);
        BeanDefinition bd = this.beanDefinitionMap.get(canonicalName);
        if (bd == null) {
            if (getParentBeanFactory() instanceof DefaultListableBeanFactory) {
                return ((DefaultListableBeanFactory) getParentBeanFactory()).getBeanDefinition(canonicalName);
            }
            throw new BeansException("No bean named '" + beanName + "' is defined");
        }
        // 如果有合并的bean定义，返回合并后的
        BeanDefinition mergedBd = this.mergedBeanDefinitions.get(canonicalName);
        if (mergedBd != null) {
            return mergedBd;
        }
        return bd;
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionNames.toArray(new String[0]);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        this.singletonObjects.put(beanName, singletonObject);
        logger.debug("Registered singleton bean named '{}'", beanName);
    }

    @Override
    public Object getSingleton(String beanName) {
        return getSingleton(beanName, true);
    }
    
    protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        // 首先检查一级缓存
        Object singletonObject = this.singletonObjects.get(beanName);
        
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            synchronized (this.singletonObjects) {
                // 检查二级缓存
                singletonObject = this.earlySingletonObjects.get(beanName);
                
                if (singletonObject == null && allowEarlyReference) {
                    // 检查三级缓存
                    ObjectFactory<?> factory = this.singletonFactories.get(beanName);
                    if (factory != null) {
                        // 从工厂获取对象
                        singletonObject = factory.getObject();
                        // 放入二级缓存
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        // 从三级缓存移除
                        this.singletonFactories.remove(beanName);
                    }
                }
            }
        }
        return singletonObject;
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return this.singletonObjects.containsKey(beanName);
    }

    @Override
    public String[] getSingletonNames() {
        return this.singletonObjects.keySet().toArray(new String[0]);
    }

    @Override
    public int getSingletonCount() {
        return this.singletonObjects.size();
    }

    @Override
    public void preInstantiateSingletons() throws BeansException {
        List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            if (beanDefinition.isSingleton()) {
                getBean(beanName);
                logger.debug("Pre-instantiated singleton bean named '{}'", beanName);
            }
        }
    }

    @Override
    public void ensureAllSingletonsInstantiated() throws BeansException {
        preInstantiateSingletons();
    }

    @Override
    public Class<?> getType(String name) throws BeansException {
        String beanName = transformedBeanName(name);
        
        // 首先检查已经实例化的单例
        Object singleton = getSingleton(beanName);
        if (singleton != null) {
            return singleton.getClass();
        }
        
        // 然后检查bean定义
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        if (beanDefinition == null) {
            return null;
        }
        
        return beanDefinition.getBeanClass();
    }

    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.beanClassLoader = (beanClassLoader != null ? beanClassLoader : Thread.currentThread().getContextClassLoader());
    }

    @Override
    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

    @Override
    public void setParentBeanFactory(ConfigurableBeanFactory parentBeanFactory) {
        if (this.parentBeanFactory != null) {
            throw new IllegalStateException("Already has a parent BeanFactory");
        }
        this.parentBeanFactory = parentBeanFactory;
    }

    @Override
    public ConfigurableBeanFactory getParentBeanFactory() {
        return this.parentBeanFactory;
    }

    @Override
    public boolean containsLocalBean(String name) {
        String beanName = transformedBeanName(name);
        return containsBeanDefinition(beanName) || containsSingleton(beanName);
    }

    @Override
    public boolean containsBean(String name) {
        String beanName = transformedBeanName(name);
        if (containsSingleton(beanName) || containsBeanDefinition(beanName)) {
            return true;
        }
        // 检查父工厂
        BeanFactory parentBeanFactory = getParentBeanFactory();
        return parentBeanFactory != null && parentBeanFactory.containsBean(beanName);
    }

    @Override
    public boolean isSingleton(String name) throws BeansException {
        String beanName = transformedBeanName(name);
        
        // 检查bean定义
        if (containsBeanDefinition(beanName)) {
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            if (beanDefinition != null) {
                return beanDefinition.isSingleton();
            }
        }
        
        // 检查父工厂
        BeanFactory parentBeanFactory = getParentBeanFactory();
        return parentBeanFactory != null && parentBeanFactory.isSingleton(beanName);
    }

    @Override
    public boolean isPrototype(String name) throws BeansException {
        String beanName = transformedBeanName(name);
        
        // 检查bean定义
        if (containsBeanDefinition(beanName)) {
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            if (beanDefinition != null) {
                return beanDefinition.isPrototype();
            }
        }
        
        // 检查父工厂
        BeanFactory parentBeanFactory = getParentBeanFactory();
        return parentBeanFactory != null && parentBeanFactory.isPrototype(beanName);
    }

    @Override
    public void registerDependentBean(String beanName, String dependentBeanName) {
        String canonicalName = transformedBeanName(beanName);
        synchronized (this.dependencyGraph) {
            Set<String> dependencies = this.dependencyGraph.computeIfAbsent(
                canonicalName, k -> new LinkedHashSet<>());
            dependencies.add(dependentBeanName);
        }
    }

    @Override
    public String[] getDependentBeans(String beanName) {
        Set<String> dependentBeans = this.dependentBeanMap.get(beanName);
        if (dependentBeans == null) {
            return new String[0];
        }
        return dependentBeans.toArray(new String[0]);
    }

    @Override
    public String[] getDependenciesForBean(String beanName) {
        Set<String> dependencies = this.dependenciesForBeanMap.get(beanName);
        if (dependencies == null) {
            return new String[0];
        }
        return dependencies.toArray(new String[0]);
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        List<String> result = new ArrayList<>();
        
        // 检查已实例化的单例
        for (Map.Entry<String, Object> entry : singletonObjects.entrySet()) {
            if (type.isInstance(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        
        // 检查bean定义
        for (String beanName : beanDefinitionNames) {
            if (result.contains(beanName)) {
                continue;
            }
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            Class<?> beanClass = beanDefinition.getBeanClass();
            if (type.isAssignableFrom(beanClass)) {
                result.add(beanName);
            }
        }
        
        return result.toArray(new String[0]);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        Map<String, T> result = new LinkedHashMap<>();
        for (String beanName : beanDefinitionNames) {
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            Class<?> beanClass = beanDefinition.getBeanClass();
            if (type.isAssignableFrom(beanClass)) {
                @SuppressWarnings("unchecked")
                T bean = (T) getBean(beanName);
                result.put(beanName, bean);
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String beanName : beanDefinitionNames) {
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            Class<?> beanClass = beanDefinition.getBeanClass();
            if (beanClass.isAnnotationPresent(annotationType)) {
                result.put(beanName, getBean(beanName));
            }
        }
        return result;
    }

    @Override
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws BeansException {
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        Class<?> beanClass = beanDefinition.getBeanClass();
        return beanClass.getAnnotation(annotationType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        String[] beanNames = getBeanNamesForType(requiredType);
        if (beanNames.length == 0) {
            throw new BeansException("No bean of type '" + requiredType.getName() + "' is defined");
        }
        if (beanNames.length > 1) {
            throw new BeansException("More than one bean of type '" + requiredType.getName() + "' is defined: " +
                    String.join(", ", beanNames));
        }
        return getBean(beanNames[0], requiredType);
    }

    /**
     * 获取bean定义持有者
     *
     * @param beanName bean名称
     * @return bean定义持有者
     * @throws BeansException 如果找不到bean定义
     */
    public BeanDefinitionHolder getBeanDefinitionHolder(String beanName) throws BeansException {
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        String[] aliases = getAliases(beanName);
        return new BeanDefinitionHolder(beanDefinition, beanName, aliases);
    }

    /**
     * 转换bean名称
     * 处理别名等情况
     */
    protected String transformedBeanName(String name) {
        // 先检查是否是bean定义
        if (containsBeanDefinition(name)) {
            return name;
        }
        
        // 再检查是否是单例
        if (containsSingleton(name)) {
            return name;
        }
        
        // 最后才进行别名解析
        return resolveAlias(name);
    }
    
    /**
     * 解析别名
     * 如果给定的名称是别名，返回对应的bean名称
     * 否则返回原始名称
     */
    private String resolveAlias(String alias) {
        if (!isAlias(alias)) {
            return alias;
        }
        
        String[] aliases = super.getAliases(alias);
        return (aliases != null && aliases.length > 0) ? aliases[0] : alias;
    }

    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException {
        try {
            // 如果是单例且有构造器参数,在创建实例前检测循环依赖
            if (beanDefinition.isSingleton() && beanDefinition.getConstructorArgumentValues() != null 
                && beanDefinition.getConstructorArgumentValues().size() > 0) {
                beforeSingletonCreation(beanName);
                try {
                    // 创建bean实例
                    final Object bean = createBeanInstance(beanDefinition);
                    
                    // 填充属性
                    populateBean(beanName, bean, beanDefinition);
                    
                    // 初始化bean
                    Object exposedObject = initializeBean(beanName, bean, beanDefinition);
                    
                    // 将完整的bean加入到单例缓存
                    addSingleton(beanName, exposedObject);
                    
                    return exposedObject;
                } finally {
                    afterSingletonCreation(beanName);
                }
            }
            
            // 如果是单例
            if (beanDefinition.isSingleton()) {
                beforeSingletonCreation(beanName);
                
                // 创建bean实例
                final Object bean = createBeanInstance(beanDefinition);
                
                addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, beanDefinition, bean));
                
                try {
                    // 填充属性
                    populateBean(beanName, bean, beanDefinition);
                    
                    // 初始化bean
                    Object exposedObject = initializeBean(beanName, bean, beanDefinition);
                    
                    // 将完整的bean加入到单例缓存
                    addSingleton(beanName, exposedObject);
                    
                    return exposedObject;
                } finally {
                    afterSingletonCreation(beanName);
                }
            }
            
            // 如果是prototype,每次都创建新实例
            Object bean = createBeanInstance(beanDefinition);
            populateBean(beanName, bean, beanDefinition);
            return initializeBean(beanName, bean, beanDefinition);
            
        } catch (Exception e) {
            throw new BeansException("Error creating bean with name '" + beanName + "'", e);
        }
    }
    
    protected Object getEarlyBeanReference(String beanName, BeanDefinition beanDefinition, Object bean) {
        return bean;
    }
    
    protected void addSingletonFactory(String beanName, ObjectFactory<?> factory) {
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, factory);
                this.earlySingletonObjects.remove(beanName);
                // 为所有别名也添加同一个工厂
                String[] aliases = getAliases(beanName);
                for (String alias : aliases) {
                    if (!this.singletonObjects.containsKey(alias)) {
                        this.singletonFactories.put(alias, factory);
                        this.earlySingletonObjects.remove(alias);
                    }
                }
            }
        }
    }
    
    /**
     * 添加单例对象
     */
    public void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            // 从二级和三级缓存中移除
            this.earlySingletonObjects.remove(beanName);
            this.singletonFactories.remove(beanName);
        }
    }
    
    protected boolean isSingletonCurrentlyInCreation(String beanName) {
        return this.singletonsCurrentlyInCreation.contains(beanName);
    }

    @Override
    public void destroySingletons() {
        String[] singletonNames = getSingletonNames();
        for (String singletonName : singletonNames) {
            destroySingleton(singletonName);
        }
    }

    protected void destroySingleton(String beanName) {
        // 获取单例对象
        Object singletonInstance = getSingleton(beanName);
        if (singletonInstance != null) {
            // 如果bean定义了销毁方法，调用它
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            if (beanDefinition != null && beanDefinition.getDestroyMethodName() != null) {
                try {
                    java.lang.reflect.Method destroyMethod = singletonInstance.getClass()
                            .getDeclaredMethod(beanDefinition.getDestroyMethodName());
                    destroyMethod.setAccessible(true);
                    destroyMethod.invoke(singletonInstance);
                    logger.debug("Invoked destroy method '{}' on bean '{}'", 
                            beanDefinition.getDestroyMethodName(), beanName);
                } catch (Exception e) {
                    logger.error("Error invoking destroy method on bean '" + beanName + "'", e);
                }
            }
            // 从缓存中移除单例
            this.singletonObjects.remove(beanName);
            logger.debug("Destroyed singleton bean '{}'", beanName);
        }
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return doGetBean(name, null);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return doGetBean(name, requiredType);
    }

    @SuppressWarnings("unchecked")
    protected <T> T doGetBean(String name, Class<T> requiredType) throws BeansException {
        String canonicalName = canonicalName(name);
        Object bean = null;
        
        // 获取bean定义
        BeanDefinition beanDefinition = getBeanDefinition(canonicalName);
        if (beanDefinition == null && getParentBeanFactory() != null) {
            return getParentBeanFactory().getBean(name, requiredType);
        }
        
        // 只有singleton才尝试从缓存中获取
        if (beanDefinition.isSingleton()) {
            // 先尝试从单例缓存中获取
            bean = getSingleton(canonicalName, true);
            
            // 如果是单例且正在创建中，尝试从三级缓存中获取早期引用
            if (bean == null && isSingletonCurrentlyInCreation(canonicalName)) {
                bean = getSingleton(canonicalName, false);
                if (bean != null) {
                    logger.debug("Returning early reference for singleton bean '{}'", canonicalName);
                }
            }
        }
        
        // 如果没有从缓存中获取到或者是prototype，创建新的实例
        if (bean == null) {
            try {
                bean = createBean(canonicalName, beanDefinition);
            } catch (Exception e) {
                throw new BeansException("Error creating bean '" + canonicalName + "'", e);
            }
        }
        
        // 类型检查
        if (requiredType != null && !requiredType.isInstance(bean)) {
            throw new BeansException(
                "Bean named '" + name + "' is expected to be of type '" + requiredType.getName() +
                "' but was actually of type '" + bean.getClass().getName() + "'");
        }
        
        return (T) bean;
    }

    @Override
    public void registerAlias(String beanName, String alias) {
        if (alias.equals(beanName)) {
            removeAlias(alias);
            return;
        }
        
        if (hasAlias(beanName, alias)) {
            return;
        }
        
        validateAlias(beanName, alias);
        super.registerAlias(beanName, alias);
        logger.debug("Registered alias '{}' for bean '{}'", alias, beanName);
    }
    
    /**
     * 验证别名是否有效
     */
    protected void validateAlias(String beanName, String alias) {
        // 检查是否存在循环引用
        if (hasAlias(alias, beanName)) {
            throw new BeansException("Cannot register alias '" + alias + 
                    "' for bean '" + beanName + "': Circular reference - '" + 
                    beanName + "' is already defined as an alias for '" + alias + "'");
        }
        
        // 检查别名是否已经被使用
        if (containsBean(alias) && !beanName.equals(transformedBeanName(alias))) {
            throw new BeansException("Cannot register alias '" + alias + 
                    "' for bean '" + beanName + "': It's already in use for bean '" + 
                    transformedBeanName(alias) + "'");
        }
    }

    /**
     * 检查是否存在指定的别名
     */
    protected boolean hasAlias(String name, String alias) {
        String[] aliases = getAliases(name);
        for (String registeredAlias : aliases) {
            if (registeredAlias.equals(alias)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String[] getAliases(String name) {
        String beanName = transformedBeanName(name);
        List<String> aliases = new ArrayList<>();
        
        // 获取直接别名
        String[] directAliases = super.getAliases(beanName);
        if (directAliases != null) {
            for (String alias : directAliases) {
                aliases.add(alias);
                // 递归获取别名的别名
                String[] transitiveAliases = super.getAliases(alias);
                if (transitiveAliases != null) {
                    for (String transitiveAlias : transitiveAliases) {
                        if (!aliases.contains(transitiveAlias)) {
                            aliases.add(transitiveAlias);
                        }
                    }
                }
            }
        }
        
        return aliases.toArray(new String[0]);
    }

    @Override
    public boolean isAlias(String name) {
        return super.isAlias(name);
    }

    @Override
    public void removeAlias(String alias) {
        super.removeAlias(alias);
        logger.debug("Removed alias '{}' from bean factory", alias);
    }

    @Override
    public ConfigurableBeanFactory getBeanFactory() {
        return this;
    }

    protected void cleanupSingletonCache(String beanName) {
        synchronized (this.singletonObjects) {
            // 从所有缓存中移除
            this.singletonObjects.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.singletonFactories.remove(beanName);
            // 移除合并的bean定义
            this.mergedBeanDefinitions.remove(beanName);
            
            // 移除所有别名的缓存
            String[] aliases = getAliases(beanName);
            for (String alias : aliases) {
                this.singletonObjects.remove(alias);
                this.earlySingletonObjects.remove(alias);
                this.singletonFactories.remove(alias);
                this.mergedBeanDefinitions.remove(alias);
            }
        }
    }

    @Override
    protected void beforeSingletonCreation(String beanName) {
        if (!this.singletonsCurrentlyInCreation.add(beanName)) {
            // 如果bean已经在创建中,说明发生了循环依赖
            throw new BeansException("Circular dependency detected: " + beanName);
        }
    }

    @Override
    protected void afterSingletonCreation(String beanName) {
        if (!this.singletonsCurrentlyInCreation.remove(beanName)) {
            logger.warn("Bean '{}' was not in creation, this might indicate a problem", beanName);
        }
    }

    @Override
    public <T> T createBean(Class<T> beanClass) throws BeansException {
        try {
            T instance = beanClass.getDeclaredConstructor().newInstance();
            logger.debug("Created new instance of bean class [{}]", beanClass.getName());
            return instance;
        } catch (Exception e) {
            throw new BeansException("Error creating bean with class '" + beanClass.getName() + "'", e);
        }
    }

    @Override
    public void autowireBean(Object existingBean) throws BeansException {
        logger.debug("Autowiring bean of type [{}]", existingBean.getClass().getName());
    }

    @Override
    public Object configureBean(Object existingBean, String beanName) throws BeansException {
        Object result = existingBean;
        
        // 应用BeanPostProcessor的前置处理
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            Object current = processor.postProcessBeforeInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        
        // 应用BeanPostProcessor的后置处理
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            Object current = processor.postProcessAfterInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        
        logger.debug("Configured bean [{}] of type [{}]", beanName, existingBean.getClass().getName());
        return result;
    }

    @Override
    public Object resolveDependency(Class<?> descriptor, String beanName) throws BeansException {
        logger.debug("Resolving dependency of type [{}] for bean [{}]", descriptor.getName(), beanName);
        return null;
    }

    @Override
    public int getBeanPostProcessorCount() {
        return getBeanPostProcessors().size();
    }

    protected void destroyBean(String beanName) {
        synchronized(this.singletonObjects) {
            // 获取所有别名
            String[] aliases = getAliases(beanName);
            
            // 清理所有缓存
            cleanupSingletonCache(beanName);
            
            // 清理依赖关系
            this.dependentBeanMap.remove(beanName);
            this.dependenciesForBeanMap.remove(beanName);
            this.dependencyGraph.remove(beanName);
            
            // 清理别名相关的缓存和依赖
            if (aliases != null) {
                for (String alias : aliases) {
                    cleanupSingletonCache(alias);
                    this.dependentBeanMap.remove(alias);
                    this.dependenciesForBeanMap.remove(alias);
                    this.dependencyGraph.remove(alias);
                }
            }
            
            // 从创建中的bean集合移除
            this.beansInCreation.remove(beanName);
        }
    }

    @Override
    protected Object doGetBean(String beanName) throws BeansException {
        String canonicalName = canonicalName(beanName);
        Object bean;

        // 检查是否是单例
        if (isSingleton(canonicalName)) {
            // 获取单例
            bean = getSingleton(canonicalName);
            if (bean == null) {
                BeanDefinition beanDefinition = getBeanDefinition(canonicalName);
                bean = createBean(canonicalName, beanDefinition);
                // 添加到单例缓存
                addSingleton(canonicalName, bean);
            }
        } else {
            // 对于prototype，每次都创建新实例
            BeanDefinition beanDefinition = getBeanDefinition(canonicalName);
            bean = createBean(canonicalName, beanDefinition);
        }

        return bean;
    }

    /**
     * 初始化bean
     */
    protected Object initializeBean(String beanName, Object bean, BeanDefinition beanDefinition) {
        // 执行Aware方法
        if (bean instanceof Aware) {
            if (bean instanceof BeanFactoryAware) {
                ((BeanFactoryAware) bean).setBeanFactory(this);
            }
            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(beanName);
            }
        }

        // 执行BeanPostProcessor的前置处理
        Object wrappedBean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);

        // 执行初始化方法
        try {
            invokeInitMethods(beanName, wrappedBean, beanDefinition);
        } catch (Exception e) {
            throw new BeansException("Invocation of init method failed", e);
        }

        // 执行BeanPostProcessor的后置处理
        wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        return wrappedBean;
    }

    /**
     * 执行bean的初始化方法
     */
    protected void invokeInitMethods(String beanName, Object bean, BeanDefinition beanDefinition) throws Exception {
        // 执行InitializingBean接口的方法
        if (bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }

        // 执行自定义的init-method
        String initMethodName = beanDefinition.getInitMethodName();
        if (StringUtils.hasText(initMethodName)) {
            Method initMethod = bean.getClass().getMethod(initMethodName);
            if (initMethod == null) {
                throw new BeansException("Could not find an init method named '" + initMethodName + "' on bean with name '" + beanName + "'");
            }
            initMethod.invoke(bean);
        }
    }

    /**
     * 注册有销毁方法的bean
     */
    protected void registerDisposableBeanIfNecessary(String beanName, Object bean, BeanDefinition beanDefinition) {
        // 只有singleton类型的bean会执行销毁方法
        if (!beanDefinition.isSingleton()) {
            return;
        }

        if (bean instanceof DisposableBean || StringUtils.hasText(beanDefinition.getDestroyMethodName())) {
            registerDisposableBean(beanName, new DisposableBeanAdapter(bean, beanName, beanDefinition));
        }
    }

    @Override
    protected Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
            throws BeansException {
        Object result = existingBean;
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            Object current = processor.postProcessBeforeInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    @Override
    protected Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
            throws BeansException {
        Object result = existingBean;
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            Object current = processor.postProcessAfterInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }
} 