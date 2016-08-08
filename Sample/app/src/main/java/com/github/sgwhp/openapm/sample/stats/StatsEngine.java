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

    public static final StatsEngine INSTANCE;
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
        System.out.println("---Rarshion:StatsEngine#inc");
        final Metric m = this.lazyGet(name);
        synchronized (m) {
            m.increment();
        }
    }

    public void inc(final String name, final long count) {
        final Metric m = this.lazyGet(name);
        synchronized (m) {
            m.increment(count);
        }
    }

    public void sample(final String name, final float value) {
        final Metric m = this.lazyGet(name);
        synchronized (m) {
            m.sample(value);
        }
    }

    public void sampleTimeMs(final String name, final long time) {
        this.sample(name, time / 1000.0f);
    }

    public static void populateMetrics() {
        for (final Map.Entry<String, Metric> entry : StatsEngine.INSTANCE.getStatsMap().entrySet()) {
            final Metric metric = entry.getValue();
            TaskQueue.queue(metric);
        }
    }

    @Override
    public void onHarvest() {
        populateMetrics();
        reset();
    }

    public static void reset() {
        StatsEngine.INSTANCE.getStatsMap().clear();
    }

    public static synchronized void disable() {
        StatsEngine.INSTANCE.enabled = false;
    }

    public static synchronized void enable() {
        StatsEngine.INSTANCE.enabled = true;
    }

    public ConcurrentHashMap<String, Metric> getStatsMap() {
        return this.statsMap;
    }

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

    static {
        INSTANCE = new StatsEngine();
    }

}
