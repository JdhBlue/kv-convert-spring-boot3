package com.jblue.kv.bo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.PropertyFilter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author njjdh
 */
@Data
public abstract class BaseCacheObject implements Serializable {

    String key;
    String par1;
    String par2;
    String par3;

    String value;

    public final String _cacheKey() {
        PropertyFilter filter = (object, name, value) -> !name.equals("value");
        return JSON.toJSONString(this, filter);
    }

}
