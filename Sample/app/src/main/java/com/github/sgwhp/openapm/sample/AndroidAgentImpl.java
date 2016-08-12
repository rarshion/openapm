package com.github.sgwhp.openapm.sample;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.text.TextUtils;

import com.github.sgwhp.openapm.sample.Instrumentation.MetricCategory;
import com.github.sgwhp.openapm.sample.analytics.AnalyticAttribute;
import com.github.sgwhp.openapm.sample.analytics.AnalyticsControllerImpl;
import com.github.sgwhp.openapm.sample.api.common.TransactionData;
import com.github.sgwhp.openapm.sample.api.v1.ConnectionEvent;
import com.github.sgwhp.openapm.sample.api.v1.ConnectionListener;
import com.github.sgwhp.openapm.sample.api.v1.DeviceForm;
import com.github.sgwhp.openapm.sample.api.v2.TraceMachineInterface;
import com.github.sgwhp.openapm.sample.background.ApplicationStateEvent;
import com.github.sgwhp.openapm.sample.background.ApplicationStateListener;
import com.github.sgwhp.openapm.sample.background.ApplicationStateMonitor;
import com.github.sgwhp.openapm.sample.crashes.CrashReporter;
import com.github.sgwhp.openapm.sample.harvest.AgentHealth;
import com.github.sgwhp.openapm.sample.harvest.ApplicationInformation;
import com.github.sgwhp.openapm.sample.harvest.ConnectInformation;
import com.github.sgwhp.openapm.sample.harvest.DeviceInformation;
import com.github.sgwhp.openapm.sample.harvest.EnvironmentInformation;
import com.github.sgwhp.openapm.sample.harvest.Harvest;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.metric.MetricUnit;
import com.github.sgwhp.openapm.sample.sample.MachineMeasurementConsumer;
import com.github.sgwhp.openapm.sample.sample.Sampler;
import com.github.sgwhp.openapm.sample.stats.StatsEngine;
import com.github.sgwhp.openapm.sample.tracing.TraceMachine;
import com.github.sgwhp.openapm.sample.util.ActivityLifecycleBackgroundListener;
import com.github.sgwhp.openapm.sample.util.AndroidEncoder;
import com.github.sgwhp.openapm.sample.util.Connectivity;
import com.github.sgwhp.openapm.sample.util.Encoder;
import com.github.sgwhp.openapm.sample.util.JsonCrashStore;
import com.github.sgwhp.openapm.sample.util.NewRelicCanary;
import com.github.sgwhp.openapm.sample.util.PersistentUUID;
import com.github.sgwhp.openapm.sample.util.SharedPrefsAnalyticAttributeStore;
import com.github.sgwhp.openapm.sample.util.UiBackgroundListener;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016/8/8.
 */
public class AndroidAgentImpl implements AgentImpl, ConnectionListener, ApplicationStateListener, TraceMachineInterface {

    private static final float LOCATION_ACCURACY_THRESHOLD = 500.0f;
    private static final AgentLog log;
    private final Context context;
    private SavedState savedState;
    private LocationListener locationListener;
    private final Lock lock;
    private final Encoder encoder;
    private DeviceInformation deviceInformation;
    private ApplicationInformation applicationInformation;
    private final AgentConfiguration agentConfiguration;
    private MachineMeasurementConsumer machineMeasurementConsumer;
    private static final Comparator<TransactionData> cmp;

    public AndroidAgentImpl(final Context context, final AgentConfiguration agentConfiguration) throws AgentInitializationException {

        this.lock = new ReentrantLock();
        this.encoder = new AndroidEncoder();
        this.context = appContext(context);
        this.agentConfiguration = agentConfiguration;
        this.savedState = new SavedState(this.context);

        if (this.isDisabled()) {
            throw new AgentInitializationException("This version of the agent has been disabled");
        }

        this.initApplicationInformation();//初始化应用信息

        //注册位置监听器
        if (agentConfiguration.useLocationService() &&
                this.context.getPackageManager().checkPermission("android.permission.ACCESS_FINE_LOCATION", this.getApplicationInformation().getPackageId()) == 0) {
            AndroidAgentImpl.log.debug("Location stats enabled");
            this.addLocationListener();
        }

        TraceMachine.setTraceMachineInterface(this);//设置当前的线程编号,线程名

        agentConfiguration.setCrashStore(new JsonCrashStore(context));
        agentConfiguration.setAnalyticAttributeStore(new SharedPrefsAnalyticAttributeStore(context));

        ApplicationStateMonitor.getInstance().addApplicationStateListener(this);//添加到应用状态监控器

        if (Build.VERSION.SDK_INT >= 14) {
            UiBackgroundListener backgroundListener;
            if (Agent.getUnityInstrumentationFlag().equals("YES")) {
                backgroundListener = new ActivityLifecycleBackgroundListener();

                if (backgroundListener instanceof Application.ActivityLifecycleCallbacks) {
                    try {
                        if (context.getApplicationContext() instanceof Application) {
                            final Application application = (Application)context.getApplicationContext();
                            application.registerActivityLifecycleCallbacks((Application.ActivityLifecycleCallbacks)backgroundListener);
                        }
                    }
                    catch (Exception e) {

                    }
                }

            }
            else {
                backgroundListener = new UiBackgroundListener();
            }

            context.registerComponentCallbacks((ComponentCallbacks)backgroundListener);

            this.setupSession();

        }
    }

    protected void initialize() {

        System.out.println("---Rarshion:AndroidAgentImpl#initialize 2---");

        this.setupSession();

        AnalyticsControllerImpl.getInstance();
        AnalyticsControllerImpl.initialize(this.agentConfiguration, this);

        Harvest.addHarvestListener(this.savedState);//收集模块初始化
        Harvest.initialize(this.agentConfiguration);
        Harvest.setHarvestConfiguration(this.savedState.getHarvestConfiguration());
        Harvest.setHarvestConnectInformation(this.savedState.getConnectInformation());

        Measurements.initialize();//测量初始化
        Measurements.addMeasurementConsumer(this.machineMeasurementConsumer = new MachineMeasurementConsumer());

        //AndroidAgentImpl.log.info(MessageFormat.format("New Relic Agent v{0}", Agent.getVersion()));
        //AndroidAgentImpl.log.verbose(MessageFormat.format("Application token: {0}", this.agentConfiguration.getApplicationToken()));

        StatsEngine.get().inc("Supportability/AgentHealth/UncaughtExceptionHandler/" + this.getUnhandledExceptionHandlerName());//状态引擎初始化
        CrashReporter.initialize(this.agentConfiguration);//异常上报模块初始化
        Sampler.init(this.context);//数据采集模块初始化

    }

    protected void setupSession() {
        this.agentConfiguration.provideSessionId();
    }

    protected void finalizeSession() {
    }

    //更新并保存连接信息
    public boolean updateSavedConnectInformation() {
        final ConnectInformation savedConnectInformation = this.savedState.getConnectInformation();
        final ConnectInformation newConnectInformation = new ConnectInformation(this.getApplicationInformation(), this.getDeviceInformation());
        final String savedAppToken = this.savedState.getAppToken();
        if (!newConnectInformation.equals(savedConnectInformation) || !this.agentConfiguration.getApplicationToken().equals(savedAppToken)) {
            if (newConnectInformation.getApplicationInformation().isAppUpgrade(savedConnectInformation.getApplicationInformation())) {
                StatsEngine.get().inc("Mobile/App/Upgrade");
                final AnalyticAttribute attribute = new AnalyticAttribute("upgradeFrom", savedConnectInformation.getApplicationInformation().getAppVersion());
                AnalyticsControllerImpl.getInstance().addAttributeUnchecked(attribute, false);
            }
            this.savedState.clear();
            this.savedState.saveConnectInformation(newConnectInformation);
            this.savedState.saveAppToken(this.agentConfiguration.getApplicationToken());
            return true;
        }
        return false;
    }
    //获取设备信息
    public DeviceInformation getDeviceInformation() {
        if (this.deviceInformation != null) {
            return this.deviceInformation;
        }
        final DeviceInformation info = new DeviceInformation();
        info.setOsName("Android");
        info.setOsVersion(Build.VERSION.RELEASE);
        info.setOsBuild(Build.VERSION.INCREMENTAL);
        info.setModel(Build.MODEL);
        info.setAgentName("AndroidAgent");
        info.setAgentVersion(Agent.getVersion());
        info.setManufacturer(Build.MANUFACTURER);
        info.setDeviceId(this.getUUID());
        info.setArchitecture(System.getProperty("os.arch"));
        info.setRunTime(System.getProperty("java.vm.version"));
        info.setSize(deviceForm(this.context).name().toLowerCase());
        info.setApplicationPlatform(this.agentConfiguration.getApplicationPlatform());
        info.setApplicationPlatformVersion(this.agentConfiguration.getApplicationPlatformVersion());
        return this.deviceInformation = info;
    }
    //获取环境信息
    public EnvironmentInformation getEnvironmentInformation() {

        //System.out.println("---Rarshion:AndroidAgentImpl#getEnvironmentInformation");

        final EnvironmentInformation envInfo = new EnvironmentInformation();//只有地点、接入网络状态等属性信息
        final ActivityManager activityManager = (ActivityManager)this.context.getSystemService("activity");

        final long[] free = new long[2];

        try {
            final StatFs rootStatFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
            final StatFs externalStatFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());

            if (Build.VERSION.SDK_INT >= 18) {
                free[0] = rootStatFs.getAvailableBlocksLong() * rootStatFs.getBlockSizeLong();
                free[1] = externalStatFs.getAvailableBlocksLong() * rootStatFs.getBlockSizeLong();
            }
            else {
                free[0] = rootStatFs.getAvailableBlocks() * rootStatFs.getBlockSize();
                free[1] = externalStatFs.getAvailableBlocks() * externalStatFs.getBlockSize();
            }
        }
        catch (Exception e) {
            AgentHealth.noticeException(e);
        }
        finally {
            if (free[0] < 0L) {
                free[0] = 0L;
            }
            if (free[1] < 0L) {
                free[1] = 0L;
            }
            envInfo.setDiskAvailable(free);
        }

        /*原厂有的，出错后注释
        envInfo.setMemoryUsage(Sampler.sampleMemory(activityManager).getSampleValue().asLong());
        envInfo.setOrientation(this.context.getResources().getConfiguration().orientation);
        */

        envInfo.setNetworkStatus(this.getNetworkCarrier());
        envInfo.setNetworkWanType(this.getNetworkWanType());

        System.out.println("---Rarshion:AndroidAgentImpl#Out getEnvironmentInformation");

        return envInfo;
    }
    //初始化应用信息
    public void initApplicationInformation() throws AgentInitializationException {

        System.out.println("---Rarshion:AndroidAgentImpl#initApplicationInformation");

        if (this.applicationInformation != null) {
            AndroidAgentImpl.log.debug("attempted to reinitialize ApplicationInformation.");
            System.out.println("---Rarshion:attempted to reinitialize ApplicationInformation.");
            return;
        }

        final String packageName = this.context.getPackageName();
        final PackageManager packageManager = this.context.getPackageManager();
        PackageInfo packageInfo = null;

        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0);
        }
        catch (PackageManager.NameNotFoundException e) {
            throw new AgentInitializationException("Could not determine package version: " + e.getMessage());
        }
        String appVersion = this.agentConfiguration.getCustomApplicationVersion();
        if (TextUtils.isEmpty((CharSequence)appVersion)) {
            if (packageInfo == null || packageInfo.versionName == null || packageInfo.versionName.length() <= 0) {
                throw new AgentInitializationException("Your app doesn't appear to have a version defined. Ensure you have defined 'versionName' in your manifest.");
            }
            appVersion = packageInfo.versionName;
        }
        AndroidAgentImpl.log.debug("Using application version " + appVersion);
        String appName;
        try {
            final ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
            if (info != null) {
                appName = packageManager.getApplicationLabel(info).toString();
            }
            else {
                appName = packageName;
            }
        }
        catch (PackageManager.NameNotFoundException e2) {
            AndroidAgentImpl.log.warning(e2.toString());
            appName = packageName;
        }
        catch (SecurityException e3) {
            AndroidAgentImpl.log.warning(e3.toString());
            appName = packageName;
        }


        AndroidAgentImpl.log.debug("Using application name " + appName);
        String build = this.agentConfiguration.getCustomBuildIdentifier();
        if (TextUtils.isEmpty((CharSequence)build)) {
            if (packageInfo != null) {
                build = String.valueOf(packageInfo.versionCode);
            }
            else {
                build = "";
                AndroidAgentImpl.log.warning("Your app doesn't appear to have a version code defined. Ensure you have defined 'versionCode' in your manifest.");
            }
        }
        AndroidAgentImpl.log.debug("Using build  " + build);
        (this.applicationInformation = new ApplicationInformation(appName, appVersion, packageName, build)).setVersionCode(packageInfo.versionCode);

    }
    //获取应用信息
    public ApplicationInformation getApplicationInformation() {
        return this.applicationInformation;
    }
    //获取会话持续时间
    public long getSessionDurationMillis() {
        return Harvest.getMillisSinceStart();
    }
    //获取设备来源
    private static DeviceForm deviceForm(final Context context) {
        final int deviceSize = context.getResources().getConfiguration().screenLayout & 0xF;
        switch (deviceSize) {
            case 1: {
                return DeviceForm.SMALL;
            }
            case 2: {
                return DeviceForm.NORMAL;
            }
            case 3: {
                return DeviceForm.LARGE;
            }
            default: {
                if (deviceSize > 3) {
                    return DeviceForm.XLARGE;
                }
                return DeviceForm.UNKNOWN;
            }
        }
    }

    private static Context appContext(final Context context) {
        if (!(context instanceof Application)) {
            return context.getApplicationContext();
        }
        return context;
    }

    @Deprecated
    public void addTransactionData(final TransactionData transactionData) {
    }

    @Deprecated
    public void mergeTransactionData(final List<TransactionData> transactionDataList) {
    }

    @Deprecated
    public List<TransactionData> getAndClearTransactionData() {
        return null;
    }

    public String getCrossProcessId() {
        this.lock.lock();
        try {
            return this.savedState.getCrossProcessId();
        }
        finally {
            this.lock.unlock();
        }
    }

    public int getStackTraceLimit() {
        this.lock.lock();
        try {
            return this.savedState.getStackTraceLimit();
        }
        finally {
            this.lock.unlock();
        }
    }

    public int getResponseBodyLimit() {
        this.lock.lock();
        try {
            return this.savedState.getHarvestConfiguration().getResponse_body_limit();
        }
        finally {
            this.lock.unlock();
        }
    }

    public void start() {
        System.out.println("---Rarshion:AndroidAgentImpl#start---");

        this.initialize();
        Harvest.start();

        /* 原厂
        if (!this.isDisabled()) {
            this.initialize();
            Harvest.start();
        }
        else {
            this.stop(false);
        }
        */
    }

    public void stop() {
        this.stop(true);
    }

    private void stop(final boolean finalSendData) {
        this.finalizeSession();
        Sampler.shutdown();//关闭采集器
        TraceMachine.haltTracing();//关闭TraceMachine
        final int eventsRecorded = AnalyticsControllerImpl.getInstance().getEventManager().getEventsRecorded();
        final int eventsEjected = AnalyticsControllerImpl.getInstance().getEventManager().getEventsEjected();
        Measurements.addCustomMetric("Supportability/Events/Recorded", MetricCategory.NONE.name(), eventsRecorded, eventsEjected, eventsEjected, MetricUnit.OPERATIONS, MetricUnit.OPERATIONS);
        if (finalSendData) {
            if (this.isUIThread()) {
                StatsEngine.get().inc("Supportability/AgentHealth/HarvestOnMainThread");
            }
            Harvest.harvestNow();
        }
        AnalyticsControllerImpl.shutdown();
        TraceMachine.clearActivityHistory();
        Harvest.shutdown();//关闭Harvest
        Measurements.shutdown();
    }

    public void disable() {
        AndroidAgentImpl.log.warning("PERMANENTLY DISABLING AGENT v" + Agent.getVersion());
        try {
            this.savedState.saveDisabledVersion(Agent.getVersion());
        }
        finally {
            try {
                this.stop(false);
            }
            finally {
                Agent.setImpl(NullAgentImpl.instance);
            }
        }
    }

    public boolean isDisabled() {
        return Agent.getVersion().equals(this.savedState.getDisabledVersion());
    }

    public String getNetworkCarrier() {
        System.out.println("---Rarshion:AndroidAgentImpl#getNetworkCarrier");
        return Connectivity.carrierNameFromContext(this.context);
    }

    public String getNetworkWanType() {
        return Connectivity.wanType(this.context);
    }

    public static void init(final Context context, final AgentConfiguration agentConfiguration) {
        System.out.println("---Rarshion:NewRelic#init---");

        try {
            Agent.setImpl(new AndroidAgentImpl(context, agentConfiguration));
            Agent.start();
        }
        catch (AgentInitializationException e) {
            AndroidAgentImpl.log.error("Failed to initialize the agent: " + e.toString());
        }
    }

    @Deprecated
    public void connected(final ConnectionEvent e) {
        AndroidAgentImpl.log.error("AndroidAgentImpl: connected ");
    }

    @Deprecated
    public void disconnected(final ConnectionEvent e) {
        this.savedState.clear();
    }

    public void applicationForegrounded(final ApplicationStateEvent e) {
        AndroidAgentImpl.log.info("AndroidAgentImpl: application foregrounded ");
        this.start();
    }

    public void applicationBackgrounded(final ApplicationStateEvent e) {
        AndroidAgentImpl.log.info("AndroidAgentImpl: application backgrounded ");
        this.stop();
    }

    //设置位置
    public void setLocation(final String countryCode, final String adminRegion) {
        if (countryCode == null || adminRegion == null) {
            throw new IllegalArgumentException("Country code and administrative region are required.");
        }
    }
    //设置位置
    public void setLocation(final Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location must not be null.");
        }
        final Geocoder coder = new Geocoder(this.context);
        List<Address> addresses = null;
        try {
            addresses = (List<Address>)coder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        }
        catch (IOException e) {
            AndroidAgentImpl.log.error("Unable to geocode location: " + e.toString());
        }
        if (addresses == null || addresses.size() == 0) {
            return;
        }
        final Address address = addresses.get(0);
        if (address == null) {
            return;
        }
        final String countryCode = address.getCountryCode();
        final String adminArea = address.getAdminArea();
        if (countryCode != null && adminArea != null) {
            this.setLocation(countryCode, adminArea);
            this.removeLocationListener();
        }
    }
    //增加位置监听器
    private void addLocationListener() {
        final LocationManager locationManager = (LocationManager)this.context.getSystemService("location");
        if (locationManager == null) {
            AndroidAgentImpl.log.error("Unable to retrieve reference to LocationManager. Disabling location listener.");
            return;
        }

        /*
        locationManager.requestLocationUpdates("passive", 1000L, 0.0f, this.locationListener = (LocationListener)new LocationListener() {

            public void onLocationChanged(final Location location) {
                if (AndroidAgentImpl.this.isAccurate(location)) {
                    AndroidAgentImpl.this.setLocation(location);
                }
            }

            public void onProviderDisabled(final String provider) {
                if ("passive".equals(provider)) {
                    AndroidAgentImpl.this.removeLocationListener();
                }
            }

            public void onProviderEnabled(final String provider) {
            }

            public void onStatusChanged(final String provider, final int status, final Bundle extras) {
            }
        });
        */

    }
    //移除位置监听器
    private void removeLocationListener() {
        if (this.locationListener == null) {
            return;
        }
        final LocationManager locationManager = (LocationManager)this.context.getSystemService("location");
        if (locationManager == null) {
            AndroidAgentImpl.log.error("Unable to retrieve reference to LocationManager. Can't unregister location listener.");
            return;
        }
        synchronized (locationManager) {
            //locationManager.removeUpdates(this.locationListener);
            this.locationListener = null;
        }
    }
    //判断是否为精确定位
    private boolean isAccurate(final Location location) {
        return location != null && 500.0f >= location.getAccuracy();
    }
    //获取唯一码
    private String getUUID() {
        String uuid = this.savedState.getConnectInformation().getDeviceInformation().getDeviceId();
        if (TextUtils.isEmpty((CharSequence)uuid)) {
            final PersistentUUID persistentUUID = new PersistentUUID(this.context);
            uuid = persistentUUID.getPersistentUUID();
            this.savedState.saveDeviceId(uuid);
        }
        return uuid;
    }

    private String getUnhandledExceptionHandlerName() {
        try {
            return Thread.getDefaultUncaughtExceptionHandler().getClass().getName();
        }
        catch (Exception e) {
            return "unknown";
        }
    }

    public Encoder getEncoder() {
        return this.encoder;
    }

    public long getCurrentThreadId() {
        return Thread.currentThread().getId();
    }

    public boolean isUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public String getCurrentThreadName() {
        return Thread.currentThread().getName();
    }

    private void pokeCanary() {
        NewRelicCanary.canaryMethod();
    }

    protected SavedState getSavedState() {
        return this.savedState;
    }

    protected void setSavedState(final SavedState savedState) {
        this.savedState = savedState;
    }

    static {
        log = AgentLogManager.getAgentLog();
        cmp = new Comparator<TransactionData>() {
            public int compare(final TransactionData lhs, final TransactionData rhs) {
                if (lhs.getTimestamp() > rhs.getTimestamp()) {
                    return -1;
                }
                if (lhs.getTimestamp() < rhs.getTimestamp()) {
                    return 1;
                }
                return 0;
            }
        };
    }

}
