package com.jblue.kv.util;

import com.alibaba.fastjson2.JSON;
import com.jblue.kv.annotation.Desensitise;
import com.jblue.kv.annotation.KV;
import com.jblue.kv.bo.BaseCacheObject;
import com.jblue.kv.bo.DefaultCacheObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @Author jdh
 **/
@Data
@Slf4j
public final class KVUtil {

    private KVUtil() {

    }

    public static void convertKV(Object o, String... fields) {
        convertlKV(o, o.getClass(), fields);
    }

    public static void convertKV(List o) throws InterruptedException {
        doConverts(o, o.get(0).getClass());
    }

    public static void convertKV(List o, Class clz) throws InterruptedException {
        doConverts(o, clz);
    }

    public static void doConverts(List infos, Class clz) throws InterruptedException {
//        Long s = System.currentTimeMillis();
//        String ra = UUIDUtil.shortUuid();
//        String token = TokenContext.getTokenHolder();
//        if (StringUtils.isEmpty(token)) {
//            token = StpUtil.getTokenValue();
//        }
//        if (StringUtils.isEmpty(token)) {
//            log.info("当前无法获取 token！");
//        }
//        String finalToken = token;

        List<Field> kvFields = FieldUtils.getFieldsListWithAnnotation(clz, KV.class);
        List<Field> desenList = FieldUtils.getFieldsListWithAnnotation(clz, Desensitise.class);
        Boolean needKV = !CollectionUtils.isEmpty(kvFields);
        Boolean needDesen = !CollectionUtils.isEmpty(desenList);
        if (!needKV && !needDesen) {
            return;
        }

        List<List<Object>> subs = ListUtils.partition(infos, 10000);
        CountDownLatch cd = new CountDownLatch(subs.size());
        for (List<Object> ts : subs) {
            ExecutorUtil.commonExecutorService.execute(() -> {
//                log.info("ra:{} countDown：{}--{}", ra, cd.getCount(), cd);
                for (Object t : ts) {
//                    TokenUtil.setToken(finalToken);
                    if (needKV) {
                        doKV(t, kvFields);
                    }
                    if (needDesen) {
                        doDesen(t, desenList);
                    }
                }
                cd.countDown();
            });
        }
        cd.await();
//        log.info("ra:{} 数据转换总耗时：" + (System.currentTimeMillis() - s), ra);
    }


    public static void convertKV(Object o) {
        convertlKV(o, o.getClass());
    }


    public static void convertDesensitise(Object o, String... fields) {
        convertDesensitization(o, o.getClass(), fields);
    }

    public static void convertDesensitise(Object o) {
        convertDesensitization(o, o.getClass());
    }

/*
    public static void doConvert(List infos, Class clz) throws InterruptedException {
        Long s = System.currentTimeMillis();
        String token = TokenContext.getTokenHolder();
        if (StringUtils.isEmpty(token)) {
            token = StpUtil.getTokenValue();
        }
        if (StringUtils.isEmpty(token)) {
            log.info("当前无法获取token2！");
        }
        String finalToken = token;
        List<Field> kvFields = FieldUtils.getFieldsListWithAnnotation(clz, KV.class);
        List<Field> desenList = FieldUtils.getFieldsListWithAnnotation(clz, Desensitise.class);

        Boolean needKV = !CollectionUtils.isEmpty(kvFields);
        Boolean needDesen = !CollectionUtils.isEmpty(desenList);
        if(!needKV &&!needDesen){
            return;
        }
        List<List<Object>> subs = ListUtils.partition(infos, 10000);
        CountDownLatch cd = new CountDownLatch(subs.size());
        for (List<Object> ts : subs) {
            ExecutorUtil.commonExecutorService.execute(() -> {
                for (Object t : ts) {
                    TokenContext.setTokenHolder(finalToken);
                    if(needKV){
                        doKV(t, kvFields);
                    }
                    if(needDesen){
                        doDesen(t, desenList);
                    }
                }
                log.debug("countDown：{}--{}", cd.getCount(), cd);
                cd.countDown();
            });
        }
        cd.await();
        log.debug("数据转换总耗时：" + (System.currentTimeMillis() - s));
    }
*/

    private static void convertlKV(Object t, Class clz) {
        convertlKV(t, clz, null);
    }

    private static void convertlKV(Object t, Class clz, String[] fieldNames) {
        if (null == t) {
            return;
        }
        List<Field> tempList = FieldUtils.getFieldsListWithAnnotation(clz, KV.class);
        if (CollectionUtils.isEmpty(tempList)) {
            return;
        }
        if (null != fieldNames && fieldNames.length > 0) {
            tempList = tempList.stream().filter(o -> ArrayUtils.contains(fieldNames, o.getName()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(tempList)) {
            return;
        }
        // kv 替换
        doKV(t, tempList);
    }

    private static void doKV(Object t, List<Field> tempList) {
        for (Field field : tempList) {
            try {
                KV param = field.getAnnotation(KV.class);
                field.setAccessible(true);
                String name = param.cache();
                String par1 = param.fixedPar1().isEmpty() ? getFieldInfo(t, param.relatePar1()) : param.fixedPar1();
                String par2 = param.fixedPar2().isEmpty() ? getFieldInfo(t, param.relatePar2()) : param.fixedPar2();
                String par3 = param.fixedPar3().isEmpty() ? getFieldInfo(t, param.relatePar3()) : param.fixedPar3();
                String key = StringUtils.isEmpty(param.relateKey()) ? (null == field.get(t) ? "" : String.valueOf(
                        field.get(t))) : param.relateKey();
                BaseCacheObject v = KVContext.getCachedObject(name, getCacheKey(key, par1, par2, par3));
                log.debug("field：{}\nparam：{}\nCacheKey：{}\nv：{}\n", field, param,
                        getCacheKey(key, par1, par2, par3), v == null ? null : v.getValue());
                if (null == v) {
                    continue;
                }
                field.set(t, v.getValue());
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    private static String getCacheKey(String key, String par1, String par2, String par3) {
        BaseCacheObject baseCacheObject = new DefaultCacheObject(key, par1, par2, par3, null);
        return baseCacheObject._cacheKey();
    }

    private static void convertDesensitization(Object t, Class clz) {
        convertDesensitization(t, clz, null);
    }

    private static void convertDesensitization(Object t, Class clz, String[] fieldNames) {
        List<Field> tempList = FieldUtils.getFieldsListWithAnnotation(clz, Desensitise.class);
        if (CollectionUtils.isEmpty(tempList)) {
            return;
        }
        List<Field> fields = tempList;
        if (null != fieldNames && fieldNames.length > 0) {
            fields = tempList.stream().filter(o -> ArrayUtils.contains(fieldNames, o.getName()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        // 敏感 替换
        doDesen(t, fields);
    }

    private static void doDesen(Object t, List<Field> fields) {
        for (Field field : fields) {
            try {
                if (null == t) {
                    return;
                }
                Desensitise param = field.getAnnotation(Desensitise.class);
                field.setAccessible(true);
                field.set(t,
                        SensitiveInfoUtils.convert(String.valueOf(field.get(t)), param.type()));
            } catch (Exception e) {
                log.error("脱敏失败" + field, e);
            }
        }
    }


    /**
     * 从缓存获取目标数据
     *
     * @return
     */
    private static String getFieldInfo(Object info, String name) {
        if (null == info) {
            return "";
        }
        try {
            Field f = FieldUtils.getField(info.getClass(), name, true);
            if (null == f) {
                return "";
            }
            Object r = FieldUtils.readField(f, info);
            return null == r ? "" : ("null".equals(String.valueOf(r)) ? "" : String.valueOf(r));
        } catch (Exception e) {
            log.debug("对象不存在指定的field", e);
        }
        return "";
    }

    /**
     * 获取缓存的value
     * @param cacheType
     * @param key
     * @param par1
     * @param par2
     * @param par3
     * @return
     */
    public static String getValue(String cacheType, String key, String par1, String par2, String par3) {
        cacheType = StringUtils.isEmpty(cacheType) ? StringUtils.EMPTY : cacheType;
        key = StringUtils.isEmpty(key) ? StringUtils.EMPTY : key;
        par1 = StringUtils.isEmpty(par1) ? StringUtils.EMPTY : par1;
        par2 = StringUtils.isEmpty(par2) ? StringUtils.EMPTY : par2;
        par3 = StringUtils.isEmpty(par3) ? StringUtils.EMPTY : par3;
        BaseCacheObject object = KVContext.getCachedObject(cacheType, getCacheKey(key, par1, par2, par3));
        return null == object ? key : object.getValue();
    }


    /**
     * 获取辅助参数不变的多个缓存值数据
     * @param name
     * @param keys
     * @param par1
     * @param par2
     * @param par3
     * @return
     */
    public static List<String> getValues(String name, List<String> keys, String par1, String par2, String par3) {
        List<String> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(keys)) {
            return result;
        }
        for (String key : keys) {
            result.add(getValue(name, key, par1, par2, par3));
        }
        return result;
    }


}
