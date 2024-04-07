package com.jblue.kv.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.security.SecureRandom;

@Data
public class CacheProperty {


    //缓存刷新时间 默认5分钟
    private int refreshSeconds=300;

    //缓存刷新时间偏移量 1分钟的随机差
    private int refreshSecondsOffset = 60;

    //缓存默认数量
    private int cacheNumber = 10000;


    public int getRefreshSeconds() {
        SecureRandom r= new SecureRandom();
        return refreshSeconds +  r.nextInt(refreshSecondsOffset);
    }

}
