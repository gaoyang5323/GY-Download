package com.kakuiwong.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public enum ThreadPoolUtil {
    POOL;

    private ThreadPoolExecutor pool;

    ThreadPoolUtil() {
        BlockingQueue<Runnable> bqueue = new ArrayBlockingQueue<Runnable>(100);
        final int SIZE_CORE_POOL = Runtime.getRuntime().availableProcessors() + 1;
        final int SIZE_MAX_POOL = 100;
        final long ALIVE_TIME = 2000;
        pool = new ThreadPoolExecutor(SIZE_CORE_POOL, SIZE_MAX_POOL, ALIVE_TIME
                , TimeUnit.MILLISECONDS, bqueue, new ThreadPoolExecutor.CallerRunsPolicy());
        pool.prestartAllCoreThreads();
    }

    public ThreadPoolExecutor getPool() {
        return pool;
    }
}
