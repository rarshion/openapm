package com.github.sgwhp.openapm.sample.sample;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Process;

import com.github.sgwhp.openapm.sample.harvest.AgentHealth;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.metric.Metric;
import com.github.sgwhp.openapm.sample.stats.TicToc;
import com.github.sgwhp.openapm.sample.tracing.ActivityTrace;
import com.github.sgwhp.openapm.sample.tracing.Sample;
import com.github.sgwhp.openapm.sample.tracing.TraceLifecycleAware;
import com.github.sgwhp.openapm.sample.tracing.TraceMachine;
import com.github.sgwhp.openapm.sample.util.NamedThreadFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016/8/8.
 */
public class Sampler implements TraceLifecycleAware, Runnable
{

    private static final long SAMPLE_FREQ_MS = 100L;
    protected static final long SAMPLE_FREQ_MS_MAX = 250L;
    private static final int[] PID;
    private static final int KB_IN_MB = 1024;
    private static final AgentLog log;
    private static final ReentrantLock samplerLock;
    protected static Sampler sampler;
    protected static boolean cpuSamplingDisabled;
    private final ActivityManager activityManager;
    private final EnumMap<Sample.SampleType, Collection<Sample>> samples;//<采集类型:cpu/内存, 采集数据对象:值/时间>
    private final ScheduledExecutorService scheduler;//使用了定时周期执行任务服务接口
    protected ScheduledFuture sampleFuture;//定时任务句柄
    protected final AtomicBoolean isRunning;
    protected long sampleFreqMs;//采集间隔
    private Long lastCpuTime;
    private Long lastAppCpuTime;
    private RandomAccessFile procStatFile;//内存与CPU信息访问
    private RandomAccessFile appStatFile;
    private Metric samplerServiceMetric;//采集标记

    protected Sampler(final Context context) {
        this.samples = new EnumMap<Sample.SampleType, Collection<Sample>>(Sample.SampleType.class);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Sampler"));
        this.isRunning = new AtomicBoolean(false);
        this.sampleFreqMs = 100L;
        this.activityManager = (ActivityManager)context.getSystemService("activity");
        this.samples.put(Sample.SampleType.MEMORY, new ArrayList<Sample>()); //sample对象包括了采集时间/值/
        this.samples.put(Sample.SampleType.CPU, new ArrayList<Sample>());
    }

    //类初始化
    //1.新建采集标志
    //2.在TraceMachine中添加监听
    public static void init(final Context context) {
        System.out.println("---Rarshion:Sampler#init---");
        Sampler.samplerLock.lock();
        try {
            if (Sampler.sampler == null) {
                Sampler.sampler = new Sampler(context);
                Sampler.sampler.sampleFreqMs = 100L;//设置采集间隔
                Sampler.sampler.samplerServiceMetric = new Metric("samplerServiceTime");//采集标志
                TraceMachine.addTraceListener(Sampler.sampler);//在TraceMachine中添加sampler对象
                Sampler.log.debug("Sampler initialized");
                System.out.println("---Rarshion:Sampler initialized---");
            }
        }
        catch (Exception e) {
            Sampler.log.error("Sampler init failed: " + e.getMessage());
            shutdown();
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    //开始执行定时任务
    public static void start() {
        System.out.println("---Rarshion:Sampler#start---");
        Sampler.samplerLock.lock();
        try {
            if (Sampler.sampler != null) {
                Sampler.sampler.schedule();
                Sampler.log.debug("Sampler started");
            }
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    //关闭
    public static void stop() {
        Sampler.samplerLock.lock();
        try {
            if (Sampler.sampler != null) {
                Sampler.sampler.stop(false);
                Sampler.log.debug("Sampler stopped");
            }
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    //立即关闭采集
    public static void stopNow() {
        Sampler.samplerLock.lock();
        try {
            if (Sampler.sampler != null) {
                Sampler.sampler.stop(true);
                Sampler.log.debug("Sampler hard stopped");
            }
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    //关闭采集
    public static void shutdown() {
        Sampler.samplerLock.lock();
        try {
            if (Sampler.sampler != null) {
                TraceMachine.removeTraceListener(Sampler.sampler);//移除TraceMachine的监听器
                stop();
                Sampler.sampler = null;
                Sampler.log.debug("Sampler shutdown");
            }
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    //执行run方法
    public void run() {
        try {
            if (this.isRunning.get()) {
                this.sample();
            }
        }
        catch (Exception e) {
            Sampler.log.error("Caught exception while running the sampler", e);
            AgentHealth.noticeException(e);
        }
    }
    //开始执行定时任务
    protected void schedule() {
        Sampler.samplerLock.lock();
        try {
            if (!this.isRunning.get()) {
                this.clear();
                this.sampleFuture = this.scheduler.scheduleWithFixedDelay(this, 0L, this.sampleFreqMs, TimeUnit.MILLISECONDS);//这里会执行类中run方法
                this.isRunning.set(true);
                Sampler.log.debug(String.format("Sampler scheduler started; sampling will occur every %d ms.", this.sampleFreqMs));
            }
        }
        catch (Exception e) {
            Sampler.log.error("Sampler scheduling failed: " + e.getMessage());
            AgentHealth.noticeException(e);
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    //关闭定时任务与/proc访问
    protected void stop(final boolean immediate) {
        Sampler.samplerLock.lock();
        try {
            if (this.isRunning.get()) {
                this.isRunning.set(false);
                if (this.sampleFuture != null) {
                    this.sampleFuture.cancel(immediate);
                }
                this.resetCpuSampler();
                Sampler.log.debug("Sampler canceled");
            }
        }
        catch (Exception e) {
            Sampler.log.error("Sampler stop failed: " + e.getMessage());
            AgentHealth.noticeException(e);
        }
        finally {
            Sampler.samplerLock.unlock();
        }
    }
    //判断是否在运行
    protected static boolean isRunning() {
        return Sampler.sampler != null && Sampler.sampler.sampleFuture != null && !Sampler.sampler.sampleFuture.isDone();
    }


    protected void monitorSamplerServiceTime(final double serviceTime) {
        this.samplerServiceMetric.sample(serviceTime);
        final Double serviceTimeAvg = this.samplerServiceMetric.getTotal() / this.samplerServiceMetric.getCount();
        if (serviceTimeAvg > this.sampleFreqMs) {
            Sampler.log.debug("Sampler: sample service time has been exceeded. Increase by 10%");
            this.sampleFreqMs = Math.min((long)(this.sampleFreqMs * 1.1f), 250L);
            if (this.sampleFuture != null) {
                this.sampleFuture.cancel(true);
            }
            this.sampleFuture = this.scheduler.scheduleWithFixedDelay(this, 0L, this.sampleFreqMs, TimeUnit.MILLISECONDS);
            Sampler.log.debug(String.format("Sampler scheduler restarted; sampling will now occur every %d ms.", this.sampleFreqMs));
            this.samplerServiceMetric.clear();
        }
    }

    //采集数据
    protected void sample() {
        final TicToc timer = new TicToc();
        Sampler.samplerLock.lock();

        try {
            timer.tic();
            final Sample memorySample = sampleMemory();
            if (memorySample != null) {
                this.getSampleCollection(Sample.SampleType.MEMORY).add(memorySample);//将数据放进容器
            }
            final Sample cpuSample = this.sampleCpu();
            if (cpuSample != null) {
                this.getSampleCollection(Sample.SampleType.CPU).add(cpuSample);
            }
        }
        catch (Exception e) {
            Sampler.log.error("Sampling failed: " + e.getMessage());
            AgentHealth.noticeException(e);
        }
        finally {
            Sampler.samplerLock.unlock();
        }
        this.monitorSamplerServiceTime(timer.toc());
    }
    //清空容器
    protected void clear() {
        for (final Collection<Sample> sampleCollection : this.samples.values()) {
            sampleCollection.clear();
        }
    }
    //采集内存信息
    public static Sample sampleMemory() {
        if (Sampler.sampler == null) {
            return null;
        }
        return sampleMemory(Sampler.sampler.activityManager);
    }
    //采集内存信息
    public static Sample sampleMemory(final ActivityManager activityManager) {

        System.out.println("---Rarshion:Sampler#sampleMemory");
        try {
            final Debug.MemoryInfo[] memInfo = activityManager.getProcessMemoryInfo(Sampler.PID);
            if (memInfo.length > 0) {
                final int totalPss = memInfo[0].getTotalPss();
                if (totalPss >= 0) {
                    final Sample sample = new Sample(Sample.SampleType.MEMORY);
                    sample.setSampleValue(totalPss / 1024.0);
                    return sample;
                }
            }
        }
        catch (Exception e) {
            Sampler.log.error("Sample memory failed: " + e.getMessage());
            System.out.println("---Rarshion:Sample memory failed" + e.getMessage());
            AgentHealth.noticeException(e);
        }

        return null;
    }
    //采集CPU性能
    protected static Sample sampleCpuInstance() {
        if (Sampler.sampler == null) {
            return null;
        }
        return Sampler.sampler.sampleCpu();
    }
    //通过访问/proc文件采集CPU性能
    public Sample sampleCpu() {
        if (Sampler.cpuSamplingDisabled) {
            return null;
        }
        try {
            if (this.procStatFile == null || this.appStatFile == null) {
                this.procStatFile = new RandomAccessFile("/proc/stat", "r");
                this.appStatFile = new RandomAccessFile("/proc/" + Sampler.PID[0] + "/stat", "r");
            }
            else {
                this.procStatFile.seek(0L);
                this.appStatFile.seek(0L);
            }
            final String procStatString = this.procStatFile.readLine();
            final String appStatString = this.appStatFile.readLine();
            final String[] procStats = procStatString.split(" ");
            final String[] appStats = appStatString.split(" ");
            final long cpuTime = Long.parseLong(procStats[2]) + Long.parseLong(procStats[3]) + Long.parseLong(procStats[4]) + Long.parseLong(procStats[5]) + Long.parseLong(procStats[6]) + Long.parseLong(procStats[7]) + Long.parseLong(procStats[8]);
            final long appTime = Long.parseLong(appStats[13]) + Long.parseLong(appStats[14]);

            if (this.lastCpuTime == null && this.lastAppCpuTime == null) {
                this.lastCpuTime = cpuTime;
                this.lastAppCpuTime = appTime;
                return null;
            }

            final Sample sample = new Sample(Sample.SampleType.CPU);
            sample.setSampleValue((appTime - this.lastAppCpuTime) / (cpuTime - this.lastCpuTime) * 100.0);
            this.lastCpuTime = cpuTime;
            this.lastAppCpuTime = appTime;
            return sample;
        }
        catch (Exception e) {
            Sampler.cpuSamplingDisabled = true;
            Sampler.log.debug("Exception hit while CPU sampling: " + e.getMessage());
            AgentHealth.noticeException(e);
            return null;
        }
    }
    //重新设置采集时间与关闭/proc文件访问
    private void resetCpuSampler() {
        this.lastCpuTime = null;
        this.lastAppCpuTime = null;
        if (this.appStatFile != null && this.procStatFile != null) {
            try {
                this.appStatFile.close();
                this.procStatFile.close();
                this.appStatFile = null;
                this.procStatFile = null;
            }
            catch (IOException e) {
                Sampler.log.debug("Exception hit while resetting CPU sampler: " + e.getMessage());
                AgentHealth.noticeException(e);
            }
        }
    }
    //复制采集数据
    public static Map<Sample.SampleType, Collection<Sample>> copySamples() {
        Sampler.samplerLock.lock();
        EnumMap<Sample.SampleType, Collection<Sample>> copy;
        try {
            if (Sampler.sampler == null) {
                Sampler.samplerLock.unlock();
                return new HashMap<Sample.SampleType, Collection<Sample>>();
            }
            copy = new EnumMap<Sample.SampleType, Collection<Sample>>(Sampler.sampler.samples);
            for (final Sample.SampleType key : Sampler.sampler.samples.keySet()) {
                copy.put(key, new ArrayList<Sample>(Sampler.sampler.samples.get(key)));
            }
        }
        finally {
            Sampler.samplerLock.unlock();
        }
        return Collections.unmodifiableMap((Map<? extends Sample.SampleType, ? extends Collection<Sample>>)copy);
    }
    //获取采集数据
    private Collection<Sample> getSampleCollection(final Sample.SampleType type) {
        return this.samples.get(type);
    }


    public void onEnterMethod() {
        if (this.isRunning.get()) {
            return;
        }
        start();
    }

    public void onExitMethod() {

    }

    public void onTraceStart(final ActivityTrace activityTrace) {
        start();//这里可能会在TraceMachine中调用
    }

    public void onTraceComplete(final ActivityTrace activityTrace) {
        stop();
        activityTrace.setVitals(copySamples());//Trace完成后拷贝数据
        this.clear();
    }

    static {
        PID = new int[] { Process.myPid() };
        log = AgentLogManager.getAgentLog();
        samplerLock = new ReentrantLock();
        Sampler.cpuSamplingDisabled = false;
    }
}
