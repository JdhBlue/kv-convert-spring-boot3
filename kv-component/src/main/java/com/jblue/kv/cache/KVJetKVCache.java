package com.jblue.kv.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.RefreshPolicy;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.support.DefaultMetricsManager;
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
public class KVJetKVCache implements BaseKVCache {

    String name = "jetCache";

    public void loadAll() {

    }


    @Override
    public void loadCache() {
        //根据配置项构建缓存数据
        Map<String, IKVService> map =
                SpringBeanUtilForKV.getImpls(IKVService.class);
        map.values().forEach(a -> {
            Object oa = null;
            try {
                oa = AopTargetUtils.getTarget(a);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            String cacheName = KVContext.getCacheName(oa.getClass());
            CacheProperty cp = KVContext.getTempParam(cacheName);

            //刷新策略
            RefreshPolicy policy =
                    RefreshPolicy.newPolicy(cp.getRefreshSeconds(),
                                    TimeUnit.SECONDS)
                            .stopRefreshAfterLastAccess(30, TimeUnit.MINUTES);
            Cache<String, BaseCacheObject> cache = LinkedHashMapCacheBuilder
                    .createLinkedHashMapCacheBuilder()
                    .limit(cp.getCacheNumber())
                    .loader(key -> a.select(KVContext.JSON2BaseCacheObject(String.valueOf(key))))
                    .refreshPolicy(policy)
                    .addMonitor(new DefaultCacheMonitor(cacheName))
                    .buildCache();
//            初次加载全部数据作为缓存
            loadAll(a, cache);
            addCacheContext(a, cacheName, cp, cache);

        });
        log.debug("kv转换所需的初始化内容加载完毕！");
        //配置监控
//        DefaultMetricsManager cacheMonitorManager =
//                new DefaultMetricsManager(15, TimeUnit.MINUTES, false);
//            开启统计监控
//            CacheMonitor cacheMonitor = new DefaultCacheMonitor(cacheName);
//            cache.config().getMonitors().add(cacheMonitor);
//
//            cacheMonitorManager.add((DefaultCacheMonitor) cacheMonitor);
//        cacheMonitorManager.start();
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
