package com.jblue.kv.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SpringBeanUtilForKV implements ApplicationContextAware, ResourceLoaderAware {


    private static ApplicationContext applicationContext;

    private static ResourceLoader resourceLoader;

    private static ResourcePatternResolver resolver ;
    private static MetadataReaderFactory metadataReaderFactory ;

    /**
     * set注入对象
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        SpringBeanUtilForKV.resourceLoader = resourceLoader;
        resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringBeanUtilForKV.applicationContext == null) {
            SpringBeanUtilForKV.applicationContext = applicationContext;
        }
    }


    // 获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    public static <T> Map<String, T> getImpls(Class<T> cls) {
        Map<String, T> map = applicationContext.getBeansOfType(cls);
        return map;
    }

    // 通过name获取 Bean.
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    // 通过class获取Bean.
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        try {
            char[] cs = clazz.getSimpleName().toCharArray();
            cs[0] += 32;// 首字母大写到小写
            return (T) getApplicationContext().getBean(String.valueOf(cs));
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    // 通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSuperBean(Class<T> clazz) {
        try {
            char[] cs = clazz.getSimpleName().toCharArray();
            cs[0] += 32;// 首字母大写到小写
            return (T) getApplicationContext().getBean(String.valueOf(cs));
        } catch (Exception e) {
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> interfaceClazz : interfaces) {
                if (clazz.getSimpleName().contains(interfaceClazz.getSimpleName())) {
                    char[] cs = interfaceClazz.getSimpleName().toCharArray();
                    cs[0] += 32;// 首字母大写到小写
                    return (T) getApplicationContext().getBean(String.valueOf(cs));
                }
            }
            return null;
        }
    }

    /**
     * 利用spring提供的扫描包下面的类信息,再通过classfrom反射获得类信息
     *
     * @param scanPath
     * @return
     * @throws IOException
     */
    public static Set<Class<?>> doScan(String scanPath) throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        String[] paths = scanPath.split(",");
        for (int i=0; i<paths.length; i++) {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    .concat(ClassUtils.convertClassNameToResourcePath(
                            SystemPropertyUtils.resolvePlaceholders(paths[i]))
                            .concat("/**/*.class"));
            Resource[] resources = resolver.getResources(packageSearchPath);
            MetadataReader metadataReader = null;
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    try {
                        if (metadataReader.getClassMetadata().isConcrete()) {// 当类型不是抽象类或接口在添加到集合
                            classes.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
                        }
                    } catch (Exception e) {
                        log.error("doScan error:", e);
                    }
                }
            }
        }
        return classes;
    }
}