package com.jblue.kv.util;

import com.alibaba.fastjson2.JSON;
import com.google.common.cache.Cache;
import com.jblue.kv.annotation.KVCache;
import com.jblue.kv.bo.BaseCacheObject;
import com.jblue.kv.bo.CacheInfo;
import com.jblue.kv.bo.CacheProperty;
import com.jblue.kv.bo.DefaultCacheObject;
import com.jblue.kv.cache.BaseKVCache;
import com.jblue.kv.cache.KVGuavaKVCache;
import com.jblue.kv.config.KVCacheConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * @Author jdh
 **/
@Data
@Slf4j
public class KVContext {


    private static Map<String, CacheInfo> cacheInfos = new ConcurrentHashMap<>();

    private static KVCacheConfig cacheConfig;

    private static BaseKVCache baseKVCache;


    public static void loadCache() {
        baseKVCache.init();
    }


    public static void setCacheConfig(KVCacheConfig cacheConfig) {
        //默认guava
        baseKVCache = new KVGuavaKVCache();
        KVContext.cacheConfig = cacheConfig;
        Collection<BaseKVCache> list = SpringBeanUtilForKV.getImpls(BaseKVCache.class).values();
        if (CollectionUtils.isEmpty(list)) {
            for (BaseKVCache bc : list) {
                if (StringUtils.equalsIgnoreCase(bc.getName(), cacheConfig.getCacheType())) {
                    baseKVCache = bc;
                    return;
                }
            }
        }
    }

    public static void addCache(String name, CacheInfo cache) {
        cacheInfos.put(name, cache);
    }

    public static CacheInfo getCache(String name) {
        return cacheInfos.get(name);
    }


    public static CacheProperty getTempParam(String name) {
        CacheProperty cp = null;
        CacheInfo ci = getCache(name);
        if (null != ci) {
            cp = ci.getCacheProperty();
        }
        if (null == cp) {
            cp = MapUtils.getObject(cacheConfig.getCaches(), name);
        }
        if (null == cp) {
            cp = cacheConfig.getCommon();
        }
        if (null == cp) {
            cp = new CacheProperty();
        }
        return cp;
    }


    public static BaseCacheObject getCachedObject(String name, BaseCacheObject info) {
        return getCachedObject(name, info._cacheKey());
    }

    public static BaseCacheObject getCachedObject(String name, String cacheKey) {
        CacheInfo ci = getCache(name);
        if (null == ci) {
            log.error("\nname:{}\nCacheInfo is null!", name);
            return null;
        }
        try {
            if (ci.getCache() instanceof com.google.common.cache.Cache googleCache) {

                return (BaseCacheObject) googleCache.get(cacheKey, () -> ci.getService().select(cacheKey));

            }
            if (ci.getCache() instanceof com.alicp.jetcache.Cache jetCache) {
                return (BaseCacheObject) jetCache.computeIfAbsent(cacheKey,
                        (k) -> ci.getService().select(cacheKey),true);
            }
            return null;
        } catch (Exception e) {
            log.error("\nname:{}\ncacheKey:{}\ne:{}", name, cacheKey, e);
            return null;
        }
    }


    public static String getCacheName(Class clazz) {
        String result = null;
        if (clazz.isAnnotationPresent(KVCache.class)) {
            result = ((KVCache) clazz.getAnnotation(KVCache.class)).value();
        }
        if (StringUtils.isEmpty(result)) {
            return getDefaultCacheName(clazz);
        }
        return result;
    }

    public static String getDefaultCacheName(Class clazz) {
        //获取泛型信息
        ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericInterfaces()[0];
        if (null == parameterizedType) {
            return StringUtils.substringAfterLast(clazz.getSimpleName(), ".");
        }
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return StringUtils.substringAfterLast(actualTypeArguments[0].getTypeName(), ".");
    }

    public static BaseCacheObject JSON2BaseCacheObject(String st) {
        //接收{}对象，此处接收数组对象会有异常
        if (st.indexOf("[") != -1) {
            st = st.replace("[", "");
        }
        if (st.indexOf("]") != -1) {
            st = st.replace("]", "");
        }
        BaseCacheObject r = JSON.parseObject(st, DefaultCacheObject.class);
        if (StringUtils.isEmpty(r.getValue())) {
            r.setValue(r.getKey());
        }
        return r;
    }


//    public static void setCacheParams(Map<String, CacheParam> cacheParams) {
//        KVContext.cacheParams = cacheParams;
//    }

//    public static void loadAll(CacheParam param) {
//        Object service =
//                KVSpringBeanUtil.getBean(param.getInterfaceClassName());
//        Method method = getMatchMethod(param, service);
//        if (null == method) {
//            return;
//        }
//        Object datas = getRemoteObject(param, null, service, method);
//        Cache cache = getCache(param.getTable());
//        if (datas instanceof Collection) {
//            Collection list = (Collection) datas;
//            list.forEach(t -> {
//                Object key = getCacheKey(param.getKeyName(), t);
//                if (null != key) {
//                    cache.putIfAbsent(key, t);
//                }
//            });
//        } else if (datas instanceof Map) {
//            Map map = (Map) datas;
//            for (Object key : map.keySet()) {
//                cache.PUT(key, map.get(key));
//            }
//        } else {
//            Object key = getCacheKey(param.getKeyName(), datas);
//            if (null != key) {
//                cache.PUT(key, datas);
//            }
//        }
//    }
//
//
//    public static DyParam getCacheKey(String[] keyName, Object data) {
//        try {
//            String id = null, hosId = null, type = null;
//            Object _id = null;
//            Object _hosId = null;
//            Object _type = null;
//            type = keyName[2];
//            hosId = keyName[1];
//            id = keyName[0];
//            if (StringUtils.isNotEmpty(id)) {
//                _id = FieldUtils.readField(FieldUtils.getField(data
//                .getClass(),
//                        id, true), data);
//            }
//            if (StringUtils.isNotEmpty(hosId)) {
//                _hosId =
//                        FieldUtils.readField(FieldUtils.getField(data
//                        .getClass(),
//                                hosId, true), data);
//            }
//            if (StringUtils.isNotEmpty(type)) {
//                _type = FieldUtils.readField(FieldUtils.getField(data
//                .getClass(),
//                        type, true), data);
//            }
//            return new DyParamBuilder().setId(_id).setType(_type).setHosId
//            (_hosId).createDyParam();
//
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        return null;
//
//    }
//
//
//    public static Object loadByMethod(CacheParam param, Object dyParam) {
//        if (!(dyParam instanceof DyParam)) {
//            return null;
//        }
//        Object service =
//                KVSpringBeanUtil.getBean(param.getInterfaceClassName());
//        Method method = getMatchMethod(param, service);
//        if (null == method) {
//            return null;
//        }
//        return getRemoteObject(param, (DyParam) dyParam, service, method);
//    }
//
//    private static Object getRemoteObject(CacheParam param, DyParam dyParam,
//                                          Object service, Method method) {
//        Object result = null;
//        try {
//            Object[] params = getMethodParams(method,
//                    param.getInterfaceParams(), dyParam);
//            Object data = method.invoke(service, params);
//            if (data instanceof ResponseResult) {
//                ResponseResult rr = (ResponseResult) data;
//                if (rr.isSuccess()) {
//                    result = rr.getDatas();
//                }
//            } else if (data instanceof Result) {
//                Result rr = (Result) data;
//                if (rr.isSuccess()) {
//                    result = rr.getDatas();
//                }
//            } else {
//                result = data;
//            }
//            if (result instanceof Collection) {
//                Collection c = (Collection) result;
//                if (c.size() == 1) {
//                    result = c.iterator().next();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

//    public static Method getMatchMethod(CacheParam param, Object service) {
//        Method[] methods = service.getClass().getDeclaredMethods();
//        if (null == methods || methods.length == 0) {
//            return null;
//        }
//        for (Method method : methods) {
//            if (method.getName().equals(param.getInterfaceMethod()) &&
//            method.getParameterTypes().length >= param.getInterfaceParams()
//            .length) {
//                return method;
//            }
//        }
//        return null;
//    }

//    public static Object[] getMethodParams(Method method, Object[] params,
//                                           DyParam dyParam) {
//        Object[] objects = null;
//        if (null == params || params.length == 0) {
//            return null;
//        }
//
//        Class[] c = method.getParameterTypes();
//        objects = new Object[c.length];
//        //设置参数
//        for (int i = 0; i < c.length; i++) {
//            if (i >= params.length) {
//                objects[i] = null;
//            } else if (StringUtils.startsWith(String.valueOf(params[i]),
//                    PREFIX_STR)) {
//                String realParam =
//                        StringUtils.substring(String.valueOf(params[i]), 1);
//                if (StringUtils.equalsIgnoreCase(CACHE_KEY, realParam)) {
//                    objects[i] = null == dyParam ? null :
//                            (null == dyParam.getId() ? null : cast(c[i],
//                                    dyParam.getId()));
//                } else if (StringUtils.equalsIgnoreCase(PARAM_HOS,
//                realParam)) {
//                    objects[i] = null == dyParam ? null :
//                            (null == dyParam.getHosId() ? null : cast(c[i],
//                                    dyParam.getHosId()));
//                } else {
//                    objects[i] = null == dyParam ? null :
//                            (null == dyParam.getType() ? null : cast(c[i],
//                                    dyParam.getType()));
//                }
//
//            } else {
//                objects[i] = cast(c[i], params[i]);
//            }
//        }
//        return objects;
//    }

//    private static Object cast(Class clazz, Object o) {
//        Object ret = String.valueOf(o);
//        if ("null".equals(ret)) {
//            ret = null;
//        } else if (clazz.equals(Integer.class)) {
//            ret = Integer.valueOf(o.toString());
//        } else if (clazz.equals(Long.class)) {
//            ret = Long.valueOf(o.toString());
//        } else if (clazz.equals(Double.class)) {
//            ret = Double.valueOf(o.toString());
//        }
//        return ret;
//    }


}
