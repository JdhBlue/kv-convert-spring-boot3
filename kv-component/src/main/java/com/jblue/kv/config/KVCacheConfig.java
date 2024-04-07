package com.jblue.kv.config;

import com.jblue.kv.bo.CacheProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @Author jiangdehao
 **/
@Configuration
@ConfigurationProperties("jblue.kv")
@Data
public class KVCacheConfig {

    private String cacheType="guavaCache";

    private CacheProperty common;

    private Map<String, CacheProperty> caches;

}