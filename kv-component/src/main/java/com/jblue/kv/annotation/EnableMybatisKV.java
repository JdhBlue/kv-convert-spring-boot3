package com.jblue.kv.annotation;


import com.jblue.kv.intercepts.ResultInterceptor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation - 使用mybatis插件
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Import({ResultInterceptor.class})
public @interface EnableMybatisKV {
}
