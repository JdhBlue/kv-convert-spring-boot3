package com.jblue.kv.intercepts;

import com.jblue.kv.annotation.Desensitise;
import com.jblue.kv.annotation.KV;
import com.jblue.kv.util.KVUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/**
 * @author njjdh
 */
@Slf4j
@Component
@Intercepts({@Signature(type = ResultSetHandler.class, method =
        "handleResultSets", args = {Statement.class})})
public class ResultInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        Object result = invocation.proceed();
        List infos = (List) result;
        if (target instanceof DefaultResultSetHandler) {
            DefaultResultSetHandler resultSetHandler =
                    (DefaultResultSetHandler) target;
            MappedStatement ms =
                    (MappedStatement) FieldUtils.readField(resultSetHandler,
                            "mappedStatement", true);
            Class clz = ms.getResultMaps().get(0).getType();
            if (CollectionUtils.isEmpty(infos)||getTodoFieldSize(clz)==0) {
                return result;
            }
            KVUtil.doConverts(infos, clz);
            return result;
        }
        //如果没有进行拦截处理，则执行默认逻辑
        return result;
    }


    private int getTodoFieldSize(Class clz){
        List<Field> field1s = FieldUtils.getFieldsListWithAnnotation(clz,
                KV.class);
        List<Field> field2s = FieldUtils.getFieldsListWithAnnotation(clz,
                Desensitise.class);
        return field1s.size()+field2s.size();
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }


}