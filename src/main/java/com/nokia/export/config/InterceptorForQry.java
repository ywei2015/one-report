package com.nokia.export.config;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author  yww
 * @date 2019-04-17
 */
@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
        RowBounds.class, ResultHandler.class }) })
public class InterceptorForQry implements Interceptor {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            Object[] b = invocation.getArgs();
            HashMap hashMap = (HashMap) b[1];
            String sql  = (String) hashMap.get("sqlStr");
            sql = sql.replace("\n", "").replace("\t", "").replaceAll("\\s+", " ");
            log.info("执行SQL: [{}]花费{}ms", sql, (endTime - startTime));
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
