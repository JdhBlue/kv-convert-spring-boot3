package com.jblue.kv.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jblue.kv.bo.BaseCacheObject;
import com.jblue.kv.bo.CacheInfo;
import com.jblue.kv.bo.CacheProperty;
import com.jblue.kv.service.IKVService;
import com.jblue.kv.util.AopTargetUtils;
import com.jblue.kv.util.KVContext;
import com.jblue.kv.util.SpringBeanUtilForKV;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class KVGuavaKVCache implements BaseKVCache {

    String name = "guavaCache";

    @Override
    public void loadCache() {
        //根据配置项构建缓存数据
        Map<String, IKVService> map = SpringBeanUtilForKV.getImpls(IKVService.class);
        map.values().forEach(a -> {
            Object oa = null;
            try {
                oa = AopTargetUtils.getTarget(a);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            String cacheName = KVContext.getCacheName(oa.getClass());
            CacheProperty cp = KVContext.getTempParam(cacheName);
            LoadingCache<String, BaseCacheObject> cache = CacheBuilder.newBuilder()
                    .maximumSize(cp.getCacheNumber())
                    .refreshAfterWrite(cp.getRefreshSeconds(), TimeUnit.SECONDS).recordStats().build(
                            new CacheLoader<>() {
                                @Override
                                public BaseCacheObject load(String key) {
                                    return a.select(KVContext.JSON2BaseCacheObject(key));
                                }
                            }
                    );
//            初次加载全部数据作为缓存
            loadAll(a, cache);
            addCacheContext(a, cacheName, cp, cache);

        });
        log.debug("kv转换所需的初始化内容加载完毕！");
    }

    @Override
    public String getName() {
        return name;
    }

    private void addCacheContext(IKVService a, String cacheName,
                                 CacheProperty cp,
                                 Cache<String, BaseCacheObject> cache) {
        CacheInfo<Cache<String, BaseCacheObject>> ci = new CacheInfo();
        ci.setCache(cache);
        ci.setCacheProperty(cp);
        ci.setService(a);
        log.debug("初始化缓存信息：" + ci);
        KVContext.addCache(cacheName, ci);
    }

    private void loadAll(IKVService a, Cache<String, BaseCacheObject> cache) {
        List<BaseCacheObject> infos = a.list();
        infos.forEach(t -> cache.put(t._cacheKey(), t));
    }


}
