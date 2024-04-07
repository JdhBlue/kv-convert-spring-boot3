package com.jblue.kv.executor;

import java.util.concurrent.LinkedTransferQueue;
/**
 * LinkedTransferQueue 能保证更高性能，相比与LinkedBlockingQueue有明显提升
 * 1) 不过LinkedTransferQueue的缺点是没有队列长度控制，需要在外层协助控制
 */
public class ExecutorQueue  extends LinkedTransferQueue<Runnable> {
    final ExecutorQueue executorQueue = new ExecutorQueue();

    public ExecutorQueue() {
        super();
    }

    public void setStandardThreadExecutor(StandardThreadExecutor threadPoolExecutor) {
        executorQueue.setStandardThreadExecutor(threadPoolExecutor);
    }

    // 注：代码来源于 tomcat
    public boolean force(Runnable o) {
        // forces the item onto the queue, to be used if the task is rejected
        return executorQueue.force(o);
    }

    @Override
    public boolean offer(Runnable o) {

        // we are maxed out on threads, simply queue the object
        // we have idle threads, just add it to the queue
        // note that we don't use getActiveCount(), see BZ 49730
        // if we have less threads than maximum force creation of a new
        // thread
        // if we reached here, we need to add it to the queue
        return executorQueue.offer(o);
    }
}