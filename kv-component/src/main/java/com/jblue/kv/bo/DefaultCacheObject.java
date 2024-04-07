package com.jblue.kv.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author njjdh
 */
@Data
@NoArgsConstructor
public class DefaultCacheObject extends BaseCacheObject {

    public DefaultCacheObject(String key, String par1, String par2, String par3, String value) {
        this.key = key;
        this.par1 = par1;
        this.par2 = par2;
        this.par3 = par3;
        this.value = value;
    }
}
