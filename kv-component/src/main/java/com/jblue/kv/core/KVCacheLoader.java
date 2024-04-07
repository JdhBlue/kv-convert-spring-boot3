package com.jblue.kv.core;

import com.jblue.kv.config.KVCacheConfig;
import com.jblue.kv.util.KVContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Data
@Slf4j
public class KVCacheLoader implements CommandLineRunner {

    @Autowired
    private KVCacheConfig kvCacheConfig;


    @Override
    public void run(String... args) throws Exception {
        try {
            init();
        } catch (Exception e) {
            log.error("kv初始化失败！", e);
        }
    }


    private void init() {
        KVContext.setCacheConfig(kvCacheConfig);
        KVContext.loadCache();
    }


}