package com.jblue.kv.config;

import com.jblue.kv.core.KVCacheLoader;
import com.jblue.kv.util.SpringBeanUtilForKV;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author jdh
 **/
//@ComponentScan(basePackages = {"com.jblue.kv.**"})
@Configuration
@Import({KVCacheConfig.class, SpringBeanUtilForKV.class, KVCacheLoader.class})
public class KvConfiguration {
}
