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
    private static final ScheduledExecutorService queueExecutor;
    private static final ConcurrentLinkedQueue<Object> queue;
    private static final Runnable dequeueTask;
    private static Future dequeueFuture;

    public static void queue(final Object object) {
        TaskQueue.queue.add(object);
    }

    public static void backgroundDequeue() {
        TaskQueue.queueExecutor.execute(TaskQueue.dequeueTask);
    }

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

    public static void start() {
        if (TaskQueue.dequeueFuture != null) {
            return;
        }
        TaskQueue.dequeueFuture = TaskQueue.queueExecutor.scheduleAtFixedRate(TaskQueue.dequeueTask, 0L, 1000L, TimeUnit.MILLISECONDS);
    }

    public static void stop() {
        if (TaskQueue.dequeueFuture == null) {
            return;
        }
        TaskQueue.dequeueFuture.cancel(true);
        TaskQueue.dequeueFuture = null;
    }

    private static void dequeue() {
        if (TaskQueue.queue.size() == 0) {
            return;
        }

        Measurements.setBroadcastNewMeasurements(false);

        while (!TaskQueue.queue.isEmpty()) {
            try {
                final Object object = TaskQueue.queue.remove();
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


    public static int size() {
        return TaskQueue.queue.size();
    }

    public static void clear() {
        TaskQueue.queue.clear();
    }

    static {
        queueExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("TaskQueue"));
        queue = new ConcurrentLinkedQueue<Object>();
        dequeueTask = new Runnable() {
            @Override
            public void run() {
                dequeue();
            }
        };
    }
}
