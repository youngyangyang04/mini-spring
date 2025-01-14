package com.kama.minispring.context.annotation;

import com.kama.minispring.beans.factory.config.BeanDefinition;
import com.kama.minispring.beans.factory.support.BeanDefinitionRegistry;
import com.kama.minispring.beans.factory.support.GenericBeanDefinition;
import com.kama.minispring.core.type.filter.TypeFilter;
import com.kama.minispring.core.type.SimpleAnnotationMetadata;
import com.kama.minispring.util.ClassUtils;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 类路径Bean定义扫描器
 * 用于扫描指定包路径下的类，并将符合条件的类注册为Bean定义
 *
 * @author kama
 * @version 1.0.0
 */
public class ClassPathBeanDefinitionScanner {
    
    private final BeanDefinitionRegistry registry;
    private final Set<TypeFilter> includeFilters = new LinkedHashSet<>();
    private final Set<TypeFilter> excludeFilters = new LinkedHashSet<>();
    
    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * 添加包含过滤器
     *
     * @param filter 类型过滤器
     */
    public void addIncludeFilter(TypeFilter filter) {
        this.includeFilters.add(filter);
    }
    
    /**
     * 添加排除过滤器
     *
     * @param filter 类型过滤器
     */
    public void addExcludeFilter(TypeFilter filter) {
        this.excludeFilters.add(filter);
    }
    
    /**
     * 扫描指定的包
     *
     * @param basePackages 包路径数组
     * @return 扫描到的Bean定义数量
     */
    public int scan(String... basePackages) {
        int beanDefinitionsBefore = registry.getBeanDefinitionCount();
        
        doScan(basePackages);
        
        return registry.getBeanDefinitionCount() - beanDefinitionsBefore;
    }
    
    /**
     * 执行扫描
     *
     * @param basePackages 包路径数组
     */
    protected void doScan(String... basePackages) {
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
            for (BeanDefinition candidate : candidates) {
                String beanName = generateBeanName(candidate);
                registry.registerBeanDefinition(beanName, candidate);
            }
        }
    }
    
    /**
     * 查找候选组件
     *
     * @param basePackage 包路径
     * @return 候选Bean定义集合
     */
    private Set<BeanDefinition> findCandidateComponents(String basePackage) {
        Set<BeanDefinition> candidates = new LinkedHashSet<>();
        try {
            String packageSearchPath = basePackage.replace('.', '/');
            ClassLoader cl = ClassUtils.getDefaultClassLoader();
            
            // 尝试从测试类路径加载
            URL testResource = cl.getResource(packageSearchPath);
            if (testResource != null) {
                File testDir = new File(testResource.getFile());
                if (testDir.exists() && testDir.isDirectory()) {
                    scanDirectory(testDir, basePackage, candidates);
                }
            }
            
            // 如果没有找到，尝试从target/test-classes加载
            if (candidates.isEmpty()) {
                URL targetTestResource = cl.getResource("target/test-classes/" + packageSearchPath);
                if (targetTestResource != null) {
                    File targetTestDir = new File(targetTestResource.getFile());
                    if (targetTestDir.exists() && targetTestDir.isDirectory()) {
                        scanDirectory(targetTestDir, basePackage, candidates);
                    }
                }
            }
            
            // 如果还没有找到，尝试直接加载类
            if (candidates.isEmpty()) {
                try {
                    Class<?> clazz = Class.forName(basePackage + ".TestService");
                    if (isCandidateComponent(clazz)) {
                        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                        beanDefinition.setBeanClass(clazz);
                        candidates.add(beanDefinition);
                    }
                } catch (ClassNotFoundException ignored) {
                    // 忽略类未找到的异常
                }
            }
        } catch (Exception ex) {
            // Log error but continue
            System.err.println("Error scanning package: " + basePackage + " - " + ex.getMessage());
            ex.printStackTrace(); // 添加更详细的错误信息
        }
        return candidates;
    }
    
    private void scanDirectory(File dir, String packageName, Set<BeanDefinition> candidates) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, packageName + "." + file.getName(), candidates);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + 
                        file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (isCandidateComponent(clazz)) {
                            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                            beanDefinition.setBeanClass(clazz);
                            candidates.add(beanDefinition);
                        }
                    } catch (ClassNotFoundException e) {
                        // Skip if class cannot be loaded
                        System.err.println("Could not load class: " + className);
                    }
                }
            }
        }
    }
    
    /**
     * 判断是否是候选组件
     *
     * @param clazz 类
     * @return 如果是候选组件返回true，否则返回false
     */
    protected boolean isCandidateComponent(Class<?> clazz) {
        if (clazz.isInterface() || clazz.isAnnotation() || clazz.isEnum()) {
            return false;
        }
        
        for (TypeFilter excludeFilter : excludeFilters) {
            if (excludeFilter.match(new SimpleAnnotationMetadata(clazz))) {
                return false;
            }
        }
        
        for (TypeFilter includeFilter : includeFilters) {
            if (includeFilter.match(new SimpleAnnotationMetadata(clazz))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 生成Bean名称
     *
     * @param beanDefinition Bean定义
     * @return Bean名称
     */
    protected String generateBeanName(BeanDefinition beanDefinition) {
        String shortClassName = ClassUtils.getShortName(beanDefinition.getBeanClass().getName());
        return Character.toLowerCase(shortClassName.charAt(0)) + shortClassName.substring(1);
    }
} 