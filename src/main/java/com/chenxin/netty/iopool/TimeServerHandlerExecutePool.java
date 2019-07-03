package com.chenxin.netty.iopool;


import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author chenxin
 * @date 2019/07/03
 */
public class TimeServerHandlerExecutePool {

    private ExecutorService executor;

    public TimeServerHandlerExecutePool(int maxPoolSize, int queueSize) {
        ThreadFactory nameThreadFactory = new ThreadFactoryBuilder().setNameFormat("thread-%d").build();

        executor = new ThreadPoolExecutor(5, maxPoolSize, 120L,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueSize),
                nameThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }
    public void execute(Runnable task) {
        executor.execute(task);
    }
}



