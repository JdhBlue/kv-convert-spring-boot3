package com.jblue.kv.service.impl;

import com.jblue.kv.annotation.KVCache;
import com.jblue.kv.bo.BaseCacheObject;
import com.jblue.kv.bo.DefaultCacheObject;
import com.jblue.kv.service.IKVService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@KVCache("trtItem")
public class RPCDemoKVServiceImpl implements IKVService<DefaultCacheObject> {

//    @DubboReference
//    private TrtItemDictService trtItemDictService;

    @Override
    public List<DefaultCacheObject> list() {
        return new ArrayList<>();
    }

    @Override
    public DefaultCacheObject select(String s) {
        BaseCacheObject bo = getObject(s);
        try {
            if (StringUtils.isEmpty(bo.getKey())) {
                return (DefaultCacheObject) bo;
            }
            String value = "";
//            value = trtItemDictService.getNameByCode(bo.getKey(),bo.getPar1(), bo.getPar2());
            bo.setValue(value);
        } catch (Exception e) {
            log.error("KV " + this.getClass() + " Info error:", e);
        }
        return (DefaultCacheObject) bo;
    }
}
