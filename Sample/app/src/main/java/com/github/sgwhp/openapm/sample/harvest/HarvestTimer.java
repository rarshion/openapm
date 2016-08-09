package com.github.sgwhp.openapm.sample.harvest;

import com.github.sgwhp.openapm.sample.background.ApplicationStateMonitor;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.stats.TicToc;
import com.github.sgwhp.openapm.sample.util.NamedThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016/8/2.
 */
public class HarvestTimer implements Runnable {

    private static final long DEFAULT_HARVEST_PERIOD = 60000L;
    private static final long HARVEST_PERIOD_LEEWAY = 1000L;
    private static final long NEVER_TICKED = -1L;
    private final ScheduledExecutorService scheduler;
    private final AgentLog log;
    private ScheduledFuture tickFuture;
    protected long period;
    protected final Harvester harvester;
    protected long lastTickTime;
    private long startTimeMs;
    private Lock lock;

    public HarvestTimer(final Harvester harvester) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Harvester"));
        this.log = AgentLogManager.getAgentLog();
        this.tickFuture = null;
        this.period = 60000L;
        this.lock = new ReentrantLock();
        this.harvester = harvester;
        this.startTimeMs = 0L;
    }

    @Override
    public void run() {
        try {
            this.lock.lock();
            this.tickIfReady();
        }
        catch (Exception e) {
            this.log.error("HarvestTimer: Exception in timer tick: " + e.getMessage());
            e.printStackTrace();
            AgentHealth.noticeException(e);
        }
        finally {
            this.lock.unlock();
        }
    }

    //tick完可以执行
    private void tickIfReady() {
        final long lastTickDelta = this.timeSinceLastTick();
        if (lastTickDelta + 1000L < this.period && lastTickDelta != -1L) { //判断是否到了tick时间
            this.log.debug("HarvestTimer: Tick is too soon (" + lastTickDelta + " delta) Last tick time: " + this.lastTickTime + " . Skipping.");
            return;
        }
        this.log.debug("HarvestTimer: time since last tick: " + lastTickDelta);
        System.out.println("---Rarshion:HarvestTimer: time since last tick: " + lastTickDelta);

        final long tickStart = this.now();
        try {
            this.tick();
        }
        catch (Exception e) {
            this.log.error("HarvestTimer: Exception in timer tick: " + e.getMessage());
            e.printStackTrace();
            AgentHealth.noticeException(e);
        }
        this.lastTickTime = tickStart;
        this.log.debug("Set last tick time to: " + this.lastTickTime);
    }

    //执行tick
    protected void tick() {
        this.log.debug("Harvest: tick");
        System.out.println("---Rarshion:HavestTimer#tick---");

        final TicToc t = new TicToc();//只是用来计算tick的没有其他的用处
        t.tic();
        try {
            if (ApplicationStateMonitor.isAppInBackground()) {
                this.log.error("HarvestTimer: Attempting to harvest while app is in background");
                System.out.println("---Rarshion:HarvestTimer: Attempting to harvest while app is in background");
            }
            else {
                this.harvester.execute();
                this.log.debug("Harvest: executed");
                System.out.println("---Rarshion:Harvest: executed");
            }
        }
        catch (Exception e) {
            this.log.error("HarvestTimer: Exception in harvest execute: " + e.getMessage());
            e.printStackTrace();
            AgentHealth.noticeException(e);
        }

        if (this.harvester.isDisabled()) {
            this.stop();
        }

        final long tickDelta = t.toc();
        this.log.debug("HarvestTimer tick took " + tickDelta + "ms");
        System.out.println("---Rarshion:HarvestTimer tick took " + tickDelta + "ms");
    }

    public void start() {

        if (ApplicationStateMonitor.isAppInBackground()) {
            this.log.warning("HarvestTimer: Attempting to start while app is in background");
            System.out.println("---Rarshion;HarvestTimer: Attempting to start while app is in background");
            return;
        }

        if (this.isRunning()) {
            this.log.warning("HarvestTimer: Attempting to start while already running");
            System.out.println("---Rarshion;HarvestTimer: Attempting to start while already running");
            return;
        }

        if (this.period <= 0L) {
            this.log.error("HarvestTimer: Refusing to start with a period of 0 ms");
            System.out.println("---Rarshion;HarvestTimer: Refusing to start with a period of 0 ms");
            return;
        }

        this.log.debug("HarvestTimer: Starting with a period of " + this.period + "ms");
        System.out.println("---Rarshion:HarvestTimer: Starting with a period of " + this.period + "ms");

        this.startTimeMs = System.currentTimeMillis();
        this.tickFuture = this.scheduler.scheduleAtFixedRate(this, 0L, this.period, TimeUnit.MILLISECONDS);
        this.harvester.start();
    }


    //关掉未执行任务与scheduler容器
    public void stop() {
        if (!this.isRunning()) {
            this.log.warning("HarvestTimer: Attempting to stop when not running");
            return;
        }
        this.cancelPendingTasks();
        this.log.debug("HarvestTimer: Stopped.");
        this.startTimeMs = 0L;
        this.harvester.stop();
    }
    //关掉未执行任务与scheduler容器
    public void shutdown() {
        this.cancelPendingTasks();
        this.scheduler.shutdownNow();
    }


    public void tickNow() {
        final ScheduledFuture future = this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                HarvestTimer.this.tick();
            }
        }, 0L, TimeUnit.SECONDS);
        try {
            future.get();
        }
        catch (Exception e) {
            this.log.error("Exception waiting for tickNow to finish: " + e.getMessage());
            e.printStackTrace();
            AgentHealth.noticeException(e);
        }
    }

    public boolean isRunning() {
        return this.tickFuture != null;
    }
    //设置tick间隔
    public void setPeriod(final long period) {
        this.period = period;
    }
    //当前时间-最后更新时间
    public long timeSinceLastTick() {
        if (this.lastTickTime == 0L) {
            return -1L;
        }
        return this.now() - this.lastTickTime;
    }
    //当前时间-开始时间
    public long timeSinceStart() {
        if (this.startTimeMs == 0L) {
            return 0L;
        }
        return this.now() - this.startTimeMs;
    }
    //返回当前时间
    private long now() {
        return System.currentTimeMillis();
    }
    //取消未定任务
    protected void cancelPendingTasks() {
        try {
            this.lock.lock();
            if (this.tickFuture != null) {
                this.tickFuture.cancel(true);
                this.tickFuture = null;
            }
        }
        finally {
            this.lock.unlock();
        }
    }

}
