package com.jblue.kv.bo;

import com.google.common.cache.Cache;
import com.jblue.kv.service.IKVService;
import lombok.Data;


import java.io.Serializable;

/**
 * @author njjdh
 */
@Data
public class CacheInfo<T> implements Serializable {

    private T cache;

    private CacheProperty cacheProperty;

    private IKVService service;

}
