package com.github.sgwhp.openapm.sample.harvest;


import com.github.sgwhp.openapm.sample.AgentConfiguration;
import com.github.sgwhp.openapm.sample.activity.config.ActivityTraceConfiguration;
import com.github.sgwhp.openapm.sample.analytics.AnalyticsControllerImpl;
import com.github.sgwhp.openapm.sample.analytics.SessionEvent;
import com.github.sgwhp.openapm.sample.harvest.type.Harvestable;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.metric.Metric;
import com.github.sgwhp.openapm.sample.stats.StatsEngine;
import com.github.sgwhp.openapm.sample.tracing.ActivityTrace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
/**
 * Created by user on 2016/8/2.
 */
public class Harvest {

    private static final AgentLog log;
    private static final boolean DISABLE_ACTIVITY_TRACE_LIMITS_FOR_DEBUGGING = false;
    public static final long INVALID_SESSION_DURATION = 0L;
    protected static Harvest instance;//单例对象
    private Harvester harvester;//采集器
    private HarvestConnection harvestConnection;//采集连接器
    private HarvestTimer harvestTimer;//定时器
    protected HarvestData harvestData;//采集到的数据
    private HarvestDataValidator harvestDataValidator;//数据采集验证器
    private static final Collection<HarvestLifecycleAware> unregisteredLifecycleListeners;
    private static final HarvestableCache activityTraceCache; //采集缓存
    private HarvestConfiguration configuration; //采集配置

    public Harvest() {
        this.configuration = HarvestConfiguration.getDefaultHarvestConfiguration();//获取默认的配置
    }

    public static void initialize(final AgentConfiguration agentConfiguration) {
        //System.out.println("---Rarshion:Harvest#initialize---");
        Harvest.instance.initializeHarvester(agentConfiguration);
        registerUnregisteredListeners();
        addHarvestListener(StatsEngine.get()); //这里添加状态引擎对象类型的监听器;在havesteTimer中tick一次会调用引擎的方法
    }

    //
    public void initializeHarvester(final AgentConfiguration agentConfiguration) {
        //System.out.println("---Rarshion:Harvest#initializeHarvester---");
        this.createHarvester();
        this.harvester.setAgentConfiguration(agentConfiguration);
        this.harvester.setConfiguration(Harvest.instance.getConfiguration());
        this.flushHarvestableCaches();
    }

    //设置timer的间隔
    public static void setPeriod(final long period) {
        Harvest.instance.getHarvestTimer().setPeriod(period);
    }
    //采集开始,执行timer的一次tick
    public static void start() {
        System.out.println("---Rarshion:Harvest#start");
        Harvest.instance.getHarvestTimer().start();//timer执行一次tick
    }
    //采集停止,关掉timer
    public static void stop() {
        Harvest.instance.getHarvestTimer().stop();
    }

    public static void harvestNow() {
        if (!isInitialized()) {
            return;
        }

        final long sessionDuration = getMillisSinceStart();
        if (sessionDuration == 0L) {
            Harvest.log.error("Session duration is invalid!");
            StatsEngine.get().inc("Supportability/AgentHealth/Session/InvalidDuration");
        }

        StatsEngine.get().sampleTimeMs("Session/Duration", sessionDuration);
        Harvest.log.debug("Harvest.harvestNow - Generating sessionDuration attribute with value " + sessionDuration);
        System.out.println("---Rarshion:Harvest.harvestNow - Generating sessionDuration attribute with value " + sessionDuration);

        final AnalyticsControllerImpl analyticsController = AnalyticsControllerImpl.getInstance();
        analyticsController.setAttribute("sessionDuration", sessionDuration, false);//设置属性
        Harvest.log.debug("Harvest.harvestNow - Generating session event.");
        System.out.println("---Rarshion:Harvest.harvestNow - Generating session event.");

        final SessionEvent sessionEvent = new SessionEvent();
        analyticsController.addEvent(sessionEvent);//将事件添加到管理容器中
        analyticsController.getEventManager().shutdown();

        Harvest.instance.getHarvestTimer().tickNow();//执行timer的一次tick
    }

    //设置单例对象
    public static void setInstance(final Harvest harvestInstance) {
        if (harvestInstance == null) {
            Harvest.log.error("Attempt to set Harvest instance to null value!");
        }
        else {
            Harvest.instance = harvestInstance;
        }
    }

    //创建采集器
    public void createHarvester() {
        System.out.println("---Rarshion:Harvest#createHarvester---");
        this.harvestConnection = new HarvestConnection();
        this.harvestData = new HarvestData();
        (this.harvester = new Harvester()).setHarvestConnection(this.harvestConnection);
        this.harvester.setHarvestData(this.harvestData);
        this.harvestTimer = new HarvestTimer(this.harvester);
        addHarvestListener(this.harvestDataValidator = new HarvestDataValidator());
    }
    //关闭采集器
    public void shutdownHarvester() {
        this.harvestTimer.shutdown();
        this.harvestTimer = null;
        this.harvester = null;
        this.harvestConnection = null;
        this.harvestData = null;
    }
    //关闭采集器
    public static void shutdown() {
        if (!isInitialized()) {
            return;
        }
        stop();
        Harvest.instance.shutdownHarvester();
    }

    //添加http请求错误，这个方法会在其他继承子类中调用
    public static void addHttpError(final HttpError error) {
        if (!Harvest.instance.shouldCollectNetworkErrors() || isDisabled()) {
            return;
        }
        final HttpErrors errors = Harvest.instance.getHarvestData().getHttpErrors();
        Harvest.instance.getHarvester().expireHttpErrors();
        final int errorLimit = Harvest.instance.getConfiguration().getError_limit();
        if (errors.count() >= errorLimit) {
            StatsEngine.get().inc("Supportability/AgentHealth/ErrorsDropped");
            Harvest.log.debug("Maximum number of HTTP errors (" + errorLimit + ") reached. HTTP Error dropped.");
            return;
        }
        errors.addHttpError(error);
        Harvest.log.verbose("Harvest: " + Harvest.instance + " now contains " + errors.count() + " errors.");
    }
    //添加http传输，这个方法会在其他继承子类中调用
    public static void addHttpTransaction(final HttpTransaction txn) {
        if (isDisabled()) {
            return;
        }
        final HttpTransactions transactions = Harvest.instance.getHarvestData().getHttpTransactions();
        Harvest.instance.getHarvester().expireHttpTransactions();
        final int transactionLimit = Harvest.instance.getConfiguration().getReport_max_transaction_count();
        if (transactions.count() >= transactionLimit) {
            StatsEngine.get().inc("Supportability/AgentHealth/TransactionsDropped");
            Harvest.log.debug("Maximum number of transactions (" + transactionLimit + ") reached. HTTP Transaction dropped.");
            return;
        }
        transactions.add(txn);
    }
    //添加线程跟踪，这个方法会在其他继承子类中调用
    public static void addActivityTrace(final ActivityTrace activityTrace) {
        if (isDisabled()) {
            return;
        }
        if (!isInitialized()) {
            Harvest.activityTraceCache.add(activityTrace);
            return;
        }
        if (activityTrace.rootTrace == null) {
            Harvest.log.error("Activity trace is lacking a root trace!");
            return;
        }
        if (activityTrace.rootTrace.childExclusiveTime == 0L) {
            Harvest.log.error("Total trace exclusive time is zero. Ignoring trace " + activityTrace.rootTrace.displayName);
            return;
        }
        final double traceExclusiveTime = activityTrace.rootTrace.childExclusiveTime / 1000.0;
        if (traceExclusiveTime < Harvest.instance.getConfiguration().getActivity_trace_min_utilization()) {
            StatsEngine.get().inc("Supportability/AgentHealth/IgnoredTraces");
            Harvest.log.debug("Total trace exclusive time is too low (" + traceExclusiveTime + "). Ignoring trace " + activityTrace.rootTrace.displayName);
            return;
        }
        final ActivityTraces activityTraces = Harvest.instance.getHarvestData().getActivityTraces();
        final ActivityTraceConfiguration configurations = Harvest.instance.getActivityTraceConfiguration();
        Harvest.instance.getHarvester().expireActivityTraces();
        if (activityTraces.count() >= configurations.getMaxTotalTraceCount()) {
            Harvest.log.debug("Activity trace limit of " + configurations.getMaxTotalTraceCount() + " exceeded. Ignoring trace: " + activityTrace.toJsonString());
            return;
        }
        Harvest.log.debug("Adding activity trace: " + activityTrace.toJsonString());
        activityTraces.add(activityTrace);
    }
    //添加采集标记，这个方法会在其他继承子类中调用
    public static void addMetric(final Metric metric) {
        if (isDisabled() || !isInitialized()) {
            return;
        }
        Harvest.instance.getHarvestData().getMetrics().addMetric(metric);
    }
    //添加转换异常，这个方法会在其他继承子类中调用
    public static void addAgentHealthException(final AgentHealthException exception) {
        if (isDisabled() || !isInitialized()) {
            return;
        }
        Harvest.instance.getHarvestData().getAgentHealth().addException(exception);
    }

    //添加采集监听器
    public static void addHarvestListener(final HarvestLifecycleAware harvestAware) {

        if (harvestAware == null) {
            Harvest.log.error("Harvest: Argument to addHarvestListener cannot be null.");
            return;
        }

        System.out.println("---Rarshion:Harvest#addHarvestListener---");

        if (!isInitialized()) {
            if (!isUnregisteredListener(harvestAware)) {
                addUnregisteredListener(harvestAware);
            }
            return;
        }

        Harvest.instance.getHarvester().addHarvestListener(harvestAware);
    }
    //移除采集监听器
    public static void removeHarvestListener(final HarvestLifecycleAware harvestAware) {
        if (harvestAware == null) {
            Harvest.log.error("Harvest: Argument to removeHarvestListener cannot be null.");
            return;
        }
        if (!isInitialized()) {
            if (isUnregisteredListener(harvestAware)) {
                removeUnregisteredListener(harvestAware);
            }
            return;
        }
        Harvest.instance.getHarvester().removeHarvestListener(harvestAware);
    }
    //判断是否已初始化
    public static boolean isInitialized() {
        return Harvest.instance != null && Harvest.instance.getHarvester() != null;
    }

    public static int getActivityTraceCacheSize() {
        return Harvest.activityTraceCache.getSize();
    }

    //获取当前到开始的时间差
    public static long getMillisSinceStart() {
        long lTime = 0L;
        final Harvest harvest = getInstance();
        if (harvest != null && harvest.getHarvestTimer() != null) {
            lTime = harvest.getHarvestTimer().timeSinceStart();
            if (lTime < 0L) {
                lTime = 0L;
            }
        }
        return lTime;
    }
    //需要收集线程跟踪信息
    public static boolean shouldCollectActivityTraces() {
        if (isDisabled()) {
            return false;
        }
        if (!isInitialized()) {
            return true;
        }
        final ActivityTraceConfiguration configurations = Harvest.instance.getActivityTraceConfiguration();
        return configurations == null || configurations.getMaxTotalTraceCount() > 0;
    }
    //清空数据采集器的缓存
    private void flushHarvestableCaches() {
        System.out.println("---Rarshion:Havest#flushHarvestableCaches");

        try {
            this.flushActivityTraceCache();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    //清空线程跟踪信息的缓存
    private void flushActivityTraceCache() {
        final Collection<Harvestable> activityTraces = Harvest.activityTraceCache.flush();
        for (final Harvestable activityTrace : activityTraces) {
            addActivityTrace((ActivityTrace)activityTrace);
        }
    }

    //添加未注册的监听器
    private static void addUnregisteredListener(final HarvestLifecycleAware harvestAware) {
        if (harvestAware == null) {
            return;
        }
        System.out.println("---Rarshion:Harvest#addUnregisteredListener---");
        synchronized (Harvest.unregisteredLifecycleListeners) {
            Harvest.unregisteredLifecycleListeners.add(harvestAware);
        }
    }
    //移除未注册的监听器
    private static void removeUnregisteredListener(final HarvestLifecycleAware harvestAware) {
        if (harvestAware == null) {
            return;
        }
        synchronized (Harvest.unregisteredLifecycleListeners) {
            Harvest.unregisteredLifecycleListeners.remove(harvestAware);
        }
    }
    //注册未注册的监听器
    private static void registerUnregisteredListeners() {
        for (final HarvestLifecycleAware harvestAware : Harvest.unregisteredLifecycleListeners) {
            addHarvestListener(harvestAware);
        }
        Harvest.unregisteredLifecycleListeners.clear();
    }
    //是否为未注册的监听器
    private static boolean isUnregisteredListener(final HarvestLifecycleAware harvestAware) {
        return harvestAware != null && Harvest.unregisteredLifecycleListeners.contains(harvestAware);
    }

    protected HarvestTimer getHarvestTimer() {
        return this.harvestTimer;
    }

    public static Harvest getInstance() {
        return Harvest.instance;
    }

    protected Harvester getHarvester() {
        return this.harvester;
    }

    public HarvestData getHarvestData() {
        return this.harvestData;
    }

    public HarvestConfiguration getConfiguration() {
        return this.configuration;
    }

    public HarvestConnection getHarvestConnection() {
        return this.harvestConnection;
    }

    public void setHarvestConnection(final HarvestConnection connection) {
        this.harvestConnection = connection;
    }

    public boolean shouldCollectNetworkErrors() {
        return this.configuration.isCollect_network_errors();
    }

    //设置配置
    public void setConfiguration(final HarvestConfiguration newConfiguration) {
        System.out.println("---Rarshion:Harvest#setConfiguration---");

        this.configuration.reconfigure(newConfiguration);
        this.harvestTimer.setPeriod(TimeUnit.MILLISECONDS.convert(this.configuration.getData_report_period(), TimeUnit.SECONDS));
        this.harvestConnection.setServerTimestamp(this.configuration.getServer_timestamp());
        this.harvestData.setDataToken(this.configuration.getDataToken());
        this.harvester.setConfiguration(this.configuration);
    }
    //设置连接信息
    public void setConnectInformation(final ConnectInformation connectInformation) {
        System.out.println("---Rarshion:Harvest#setConnectInformation");
        this.harvestConnection.setConnectInformation(connectInformation);
        this.harvestData.setDeviceInformation(connectInformation.getDeviceInformation());
    }
    //设置havest配置
    public static void setHarvestConfiguration(final HarvestConfiguration configuration) {
        System.out.println("---Rarshion:Harvest#setHarvestConfiguration---");

        if (!isInitialized()) {
            Harvest.log.error("Cannot configure Harvester before initialization.");
            new Exception().printStackTrace();
            return;
        }
        System.out.println("---Rarshion:Harvest Configuration: " + configuration);
        Harvest.log.debug("Harvest Configuration: " + configuration);
        Harvest.instance.setConfiguration(configuration);
    }
    //获取havest配置
    public static HarvestConfiguration getHarvestConfiguration() {
        if (!isInitialized()) {
            return HarvestConfiguration.getDefaultHarvestConfiguration();
        }
        return Harvest.instance.getConfiguration();
    }
    //设置连接信息
    public static void setHarvestConnectInformation(final ConnectInformation connectInformation) {
        System.out.println("---Rarshion:Havest#setHarvestConnectInformation");

        if (!isInitialized()) {
            Harvest.log.error("Cannot configure Harvester before initialization.");
            new Exception().printStackTrace();
            return;
        }

        System.out.println("---Rarshion:Setting Harvest connect information: " + connectInformation);
        Harvest.log.debug("Setting Harvest connect information: " + connectInformation);

        Harvest.instance.setConnectInformation(connectInformation);
    }
    public static boolean isDisabled() {
        return isInitialized() && Harvest.instance.getHarvester().isDisabled();
    }
    protected ActivityTraceConfiguration getActivityTraceConfiguration() {
        return this.configuration.getAt_capture();
    }

    static {
        log = AgentLogManager.getAgentLog();
        Harvest.instance = new Harvest();
        unregisteredLifecycleListeners = new ArrayList<HarvestLifecycleAware>();
        activityTraceCache = new HarvestableCache();
    }
}
