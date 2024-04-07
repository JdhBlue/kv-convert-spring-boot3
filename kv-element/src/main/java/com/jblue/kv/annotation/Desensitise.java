package com.jblue.kv.annotation;



import com.jblue.kv.enums.SensitiveTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation - 脱敏注解
 *
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Desensitise {
    SensitiveTypeEnum type() default SensitiveTypeEnum.COMMON;
}
