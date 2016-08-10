package com.github.sgwhp.openapm.sample.stats;

import com.github.sgwhp.openapm.sample.TaskQueue;
import com.github.sgwhp.openapm.sample.harvest.HarvestAdapter;
import com.github.sgwhp.openapm.sample.metric.Metric;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by user on 2016/8/1.
 */
public class StatsEngine extends HarvestAdapter {

    public static final StatsEngine INSTANCE  = new StatsEngine();
    public boolean enabled;
    private ConcurrentHashMap<String, Metric> statsMap;

    private StatsEngine() {
        this.enabled = true;
        this.statsMap = new ConcurrentHashMap<String, Metric>();
    }

    public static StatsEngine get() {
        return StatsEngine.INSTANCE;
    }

    public void inc(final String name) {
        System.out.println("---Rarshion:StatsEngine#inc---" + name);
        final Metric m = this.lazyGet(name);
        synchronized (m) {
            m.increment();
        }
    }

    public void inc(final String name, final long count) {
        System.out.println("---Rarshion:StatsEngine#inc---" + name + "count:" + count);
        final Metric m = this.lazyGet(name);
        synchronized (m) {
            m.increment(count);//增加采集的次数
        }
    }

    public void sample(final String name, final float value) {
        System.out.println("---Rarshion:StatsEngine#sample---" + name + "count:" + value);
        final Metric m = this.lazyGet(name);
        synchronized (m) {
            m.sample(value);//增加采集的数值
        }
    }

    public void sampleTimeMs(final String name, final long time) {
        this.sample(name, time / 1000.0f);
    }

    //将采集标记压入队列
    public static void populateMetrics() {
        for (final Map.Entry<String, Metric> entry : StatsEngine.INSTANCE.getStatsMap().entrySet()) {
            final Metric metric = entry.getValue();
            //metric对象作为采集标记,以字符串区分
            TaskQueue.queue(metric); //压进任务队列
        }
    }

    //harvest接口重写:
    // 1.将采集标记压入队列;
    // 2.清空本地map缓存
    @Override
    public void onHarvest() {
        populateMetrics();
        reset();
    }

    //清空本地map缓存
    public static void reset() {
        StatsEngine.INSTANCE.getStatsMap().clear();
    }
    //获取本地map缓存
    public ConcurrentHashMap<String, Metric> getStatsMap() {
        return this.statsMap;
    }
    //先从本地map中查找,如无再实例化新的对象
    private Metric lazyGet(final String name) {
        Metric m = this.statsMap.get(name);
        if (m == null) {
            m = new Metric(name);
            if (this.enabled) {
                this.statsMap.put(name, m);
            }
        }
        return m;
    }


    public static synchronized void disable() {
        StatsEngine.INSTANCE.enabled = false;
    }
    public static synchronized void enable() {
        StatsEngine.INSTANCE.enabled = true;
    }

}
