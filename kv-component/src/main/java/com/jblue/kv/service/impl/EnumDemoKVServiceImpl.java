package com.jblue.kv.service.impl;

import com.jblue.kv.annotation.KVCache;
import com.jblue.kv.bo.BaseCacheObject;
import com.jblue.kv.bo.DefaultCacheObject;
import com.jblue.kv.service.IKVService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author jdh
 */
@Service
@Slf4j
@KVCache("itemType")
public class EnumDemoKVServiceImpl implements IKVService<DefaultCacheObject> {


    @Override
    public List list() {
        return Arrays.stream(ItemType.values())
                .map(t -> new DefaultCacheObject(t.getKey(), null, null, null, t.getValue())).toList();
    }

    @Override
    public DefaultCacheObject select(String s) {
        BaseCacheObject bo = getObject(s);
        try {
            bo.setValue(ItemType.getByKey(bo.getKey()).getValue());
        } catch (Exception e) {
            log.error("KV " + this.getClass() + " Info error:", e);
        }
        return (DefaultCacheObject) bo;
    }

    enum ItemType {
        DRUG("01", "处方"),
        EXAM("02", "检查"),
        LAB("03", "检验"),
        TREAT("04", "处置"),
        MATERIAL("05", "耗材");

        private final String key;

        private final String value;

        ItemType(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getKey() {
            return key;
        }


        public Boolean is(String v) {
            return key.equals(v);
        }

        public static ItemType getByKey(String v) {
            return Arrays.stream(ItemType.values()).filter(t -> t.getKey().equals(v)).findFirst().orElse(null);
        }

        public static ItemType getByValue(String v) {
            return Arrays.stream(ItemType.values()).filter(t -> t.getValue().equals(v)).findFirst().orElse(null);
        }

    }
}
