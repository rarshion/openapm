package com.github.sgwhp.openapm.sample;

import com.github.sgwhp.openapm.sample.harvest.AgentHealth;
import com.github.sgwhp.openapm.sample.harvest.AgentHealthException;
import com.github.sgwhp.openapm.sample.harvest.Harvest;
import com.github.sgwhp.openapm.sample.harvest.HarvestAdapter;
import com.github.sgwhp.openapm.sample.measurement.http.HttpTransactionMeasurement;
import com.github.sgwhp.openapm.sample.metric.Metric;
import com.github.sgwhp.openapm.sample.tracing.ActivityTrace;
import com.github.sgwhp.openapm.sample.tracing.Trace;
import com.github.sgwhp.openapm.sample.util.NamedThreadFactory;

import java.util.concurrent.*;

/**
 * Created by user on 2016/8/1.
 */
public class TaskQueue extends HarvestAdapter {

    private static final long DEQUEUE_PREIOD_MS = 1000L;
    private static final ScheduledExecutorService queueExecutor  = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("TaskQueue"));//定时任务服务接口
    private static final ConcurrentLinkedQueue<Object> queue = new ConcurrentLinkedQueue<Object>();//任务队列
    private static Future dequeueFuture;//定时任务服务句柄
    private static final Runnable dequeueTask = new Runnable() {
        @Override
        public void run() {
            dequeue();
        }
    };

    //进队列
    public static void queue(final Object object) {
        TaskQueue.queue.add(object);
    }
    //
    public static void backgroundDequeue() {
        TaskQueue.queueExecutor.execute(TaskQueue.dequeueTask);
    }
    //同步队列
    public static void synchronousDequeue() {
        final Future future = TaskQueue.queueExecutor.submit(TaskQueue.dequeueTask);
        try {
            future.get();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e2) {
            e2.printStackTrace();
        }
    }
    //执行任务队列中的任务
    public static void start() {
        System.out.println("---Rarshion:TaskQueue#start");
        if (TaskQueue.dequeueFuture != null) {
            return;
        }
        TaskQueue.dequeueFuture = TaskQueue.queueExecutor.scheduleAtFixedRate(
                TaskQueue.dequeueTask, 0L, 1000L, TimeUnit.MILLISECONDS);//dequeueTask可理解为方法
    }
    //关掉服务接口句柄
    public static void stop() {
        if (TaskQueue.dequeueFuture == null) {
            return;
        }
        TaskQueue.dequeueFuture.cancel(true);
        TaskQueue.dequeueFuture = null;
    }
    //出队列
    private static void dequeue() {
        if (TaskQueue.queue.size() == 0) {
            return;
        }

        Measurements.setBroadcastNewMeasurements(false);

        while (!TaskQueue.queue.isEmpty()) {
            try {
                final Object object = TaskQueue.queue.remove();
                //判断出队列对象元素类型然后添加到havest对应的容器中
                if (object instanceof ActivityTrace) {
                    Harvest.addActivityTrace((ActivityTrace)object);
                }
                else if (object instanceof Metric) {
                    Harvest.addMetric((Metric)object);
                }
                else if (object instanceof AgentHealthException) {
                    Harvest.addAgentHealthException((AgentHealthException)object);
                }
                else if (object instanceof Trace) {
                    Measurements.addTracedMethod((Trace)object);
                }
                else {
                    if (!(object instanceof HttpTransactionMeasurement)) {
                        continue;
                    }
                    Measurements.addHttpTransaction((HttpTransactionMeasurement)object);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                AgentHealth.noticeException(e);
            }
        }

        Measurements.broadcast();
        Measurements.setBroadcastNewMeasurements(true);
    }
    //队列大小
    public static int size() {
        return TaskQueue.queue.size();
    }
    //清除队列
    public static void clear() {
        TaskQueue.queue.clear();
    }

}
