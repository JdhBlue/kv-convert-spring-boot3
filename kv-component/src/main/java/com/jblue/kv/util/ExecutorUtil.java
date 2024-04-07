package com.jblue.kv.util;


import com.jblue.kv.executor.StandardThreadExecutor;

import java.util.concurrent.ExecutorService;

public class ExecutorUtil {

    private ExecutorUtil() {
    }

    public static final ExecutorService commonExecutorService = commonExecutorService();

    public static final ExecutorService ioExecutorService = ioExecutorService();

    public static final ExecutorService cpuExecutorService = cpuExecutorService();

    /**
     * CPU 密集型线程池
     *
     * @return
     */
    private static ExecutorService cpuExecutorService() {
        int core = Runtime.getRuntime().availableProcessors() + 1;
        int max = core * 2;
        return new StandardThreadExecutor(core,max);
    }

    private static ExecutorService commonExecutorService() {
        int core = Runtime.getRuntime().availableProcessors() + 1;
        int max = 10000;
        return new StandardThreadExecutor(core,max);
    }

    /**
     * IO 密集类型
     *
     * @return
     */
    private static ExecutorService ioExecutorService() {
        int core = Runtime.getRuntime().availableProcessors() * 2;
        int max = core * 5;
        return new StandardThreadExecutor(core,max);
    }


}
