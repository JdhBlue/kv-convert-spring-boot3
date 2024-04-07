package com.jblue.kv.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation - kv转换注解
 *
 * @author njjdh
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KV {

    //缓存名
    String cache() default "default";

    //获取对象中某个参数名的值
    String relatePar1() default "";

    //获取对象中某个参数名的值
    String relatePar2() default "";

    //获取对象中某个参数名的值
    String relatePar3() default "";

    //固定的参数值
    String fixedPar1() default "";

    //固定的参数值
    String fixedPar2() default "";

    //固定的参数值
    String fixedPar3() default "";


    //获取对象中某个参数名的值
    String relateKey() default "";
}
