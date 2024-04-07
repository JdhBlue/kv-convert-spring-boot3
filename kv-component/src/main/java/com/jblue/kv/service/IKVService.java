package com.jblue.kv.service;


import com.jblue.kv.bo.BaseCacheObject;
import com.jblue.kv.util.KVContext;

import java.util.List;

public interface IKVService<T extends BaseCacheObject> {


    /**
     * 获取全量数据进行缓存
     */
    List<T> list();

    /**
     * 根据现有数据去刷新获取
     */
    default T select(T bo) {
        return select(bo._cacheKey());
    }

    T select(String cacheKey);


    default BaseCacheObject getObject(String cacheKey) {
        return KVContext.JSON2BaseCacheObject(cacheKey);
    }


}
