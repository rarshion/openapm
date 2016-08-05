package com.github.sgwhp.openapm.sample.crashes;

import com.github.sgwhp.openapm.sample.Agent;
import com.github.sgwhp.openapm.sample.AgentConfiguration;
import com.github.sgwhp.openapm.sample.analytics.AnalyticsControllerImpl;
import com.github.sgwhp.openapm.sample.harvest.crash.Crash;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.metric.Metric;
import com.github.sgwhp.openapm.sample.stats.StatsEngine;
import com.github.sgwhp.openapm.sample.stats.TicToc;
import com.github.sgwhp.openapm.sample.util.FeatureFlag;
import com.github.sgwhp.openapm.sample.util.NamedThreadFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by user on 2016/8/1.
 */
public class CrashReporter {

    private static final String CRASH_COLLECTOR_PATH = "/mobile_crash";
    private static final int CRASH_COLLECTOR_TIMEOUT = 5000;
    protected static CrashReporter instance;
    private static AgentConfiguration agentConfiguration;
    private static ExecutorService executor;
    private final AgentLog log;
    protected boolean isEnabled;
    private boolean reportCrashes;
    private Thread.UncaughtExceptionHandler previousExceptionHandler;
    private CrashStore crashStore;
    protected static final AtomicBoolean initialized;


    public CrashReporter() {
        this.log = AgentLogManager.getAgentLog();
        this.isEnabled = false;
        this.reportCrashes = true;
    }

    public static void initialize(final AgentConfiguration _agentConfiguration) {
        if (!CrashReporter.initialized.compareAndSet(false, true)) {
            return;
        }
        CrashReporter.executor = Executors.newCachedThreadPool(new NamedThreadFactory("CrashUploader"));
        CrashReporter.agentConfiguration = _agentConfiguration;
        CrashReporter.instance.isEnabled = CrashReporter.agentConfiguration.getReportCrashes();
        CrashReporter.instance.crashStore = CrashReporter.agentConfiguration.getCrashStore();
        CrashReporter.executor.submit(new Runnable() {
            @Override
            public void run() {
                if (CrashReporter.instance.hasReachableNetworkConnection()) {
                    CrashReporter.instance.reportSavedCrashes();
                    CrashReporter.instance.reportSupportabilityMetrics();
                }
                else {
                    CrashReporter.instance.log.warning("Unable to upload cached crash to New Relic - no network");
                }
            }
        });
        if (CrashReporter.instance.isEnabled) {
            CrashReporter.instance.installCrashHandler();
        }
    }

    public static AgentConfiguration getAgentConfiguration() {
        return CrashReporter.agentConfiguration;
    }

    public UncaughtExceptionHandler getHandler() {
        return new UncaughtExceptionHandler();
    }

    public static UncaughtExceptionHandler getInstanceHandler() {
        return CrashReporter.instance.getHandler();
    }
    public static void setReportCrashes(final boolean reportCrashes) {
        CrashReporter.instance.reportCrashes = reportCrashes;
    }

    public static int getStoredCrashCount() {
        return CrashReporter.instance.crashStore.count();
    }

    public static List<Crash> fetchAllCrashes() {
        return CrashReporter.instance.crashStore.fetchAll();
    }

    public static void clear() {
        CrashReporter.instance.crashStore.clear();
    }

    private void installCrashHandler() {
        final Thread.UncaughtExceptionHandler currentExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (currentExceptionHandler != null) {
            if (currentExceptionHandler instanceof Thread.UncaughtExceptionHandler) {
                this.log.debug("New Relic crash handler already installed.");
                return;
            }
            this.previousExceptionHandler = currentExceptionHandler;
            this.log.debug("Installing New Relic crash handler and chaining " + this.previousExceptionHandler.getClass().getName());
        }
        else {
            this.log.debug("Installing New Relic crash handler.");
        }
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

    protected void reportSavedCrashes() {
        for (final Crash crash : this.crashStore.fetchAll()) {
            if (crash.isStale()) {
                this.crashStore.delete(crash);
                this.log.info("Crash [" + crash.getUuid().toString() + "] has become stale, and has been removed");
                StatsEngine.get().inc("Supportability/AgentHealth/Crash/Removed/Stale");
            }
            else {
                this.reportCrash(crash);
            }
        }
    }

    protected Future<?> reportCrash(final Crash crash) {
        Future<?> crashSenderThread = null;
        if (this.reportCrashes) {
            final CrashSender sender = new CrashSender(crash);
            crashSenderThread = CrashReporter.executor.submit(sender);
        }
        return crashSenderThread;
    }

    protected void recordFailedUpload(final String errorMsg) {
        this.log.error(errorMsg);
        StatsEngine.get().inc("Supportability/AgentHealth/Crash/FailedUpload");
    }

    protected void storeSupportabilityMetrics() {
        final ConcurrentHashMap<String, Metric> statsMap = StatsEngine.get().getStatsMap();
    }

    protected void reportSupportabilityMetrics() {
    }

    private boolean requestWasSuccessful(final HttpURLConnection connection) throws IOException {
        switch (connection.getResponseCode()) {
            case 200: {
                return true;
            }
            default: {
                this.log.error("[crashsender] Server returned " + Integer.valueOf(connection.getResponseCode()).toString());
                return false;
            }
        }
    }

    private boolean hasReachableNetworkConnection() {
        boolean isReachable = false;
        try {
            final InetAddress addr = InetAddress.getByName(CrashReporter.agentConfiguration.getCrashCollectorHost());
            isReachable = addr.isReachable(5000);
        }
        catch (IOException e) {
            isReachable = false;
        }
        return isReachable;
    }

    static {
        CrashReporter.instance = new CrashReporter();
        initialized = new AtomicBoolean(false);
    }

    public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
    {
        private final AtomicBoolean handledException;

        public UncaughtExceptionHandler() {
            this.handledException = new AtomicBoolean(false);
        }

        @Override
        public void uncaughtException(final Thread thread, final Throwable throwable) {
            if (!Agent.getUnityInstrumentationFlag().equals("YES") && !this.handledException.compareAndSet(false, true)) {
                StatsEngine.get().inc("Supportability/AgentHealth/Recursion/UncaughtExceptionHandler");
                return;
            }
            try {
                if (!CrashReporter.instance.isEnabled || !FeatureFlag.featureEnabled(FeatureFlag.CrashReporting)) {
                    CrashReporter.this.log.debug("A crash has been detected but crash reporting is disabled!");
                    this.chainExceptionHandler(thread, throwable);
                    return;
                }

                CrashReporter.this.log.debug("A crash has been detected in " + thread.getStackTrace()[0].getClassName() + " and will be reported ASAP.");
                CrashReporter.this.log.debug("Analytics data is currently " + (CrashReporter.agentConfiguration.getEnableAnalyticsEvents() ? "enabled " : "disabled"));
                final Crash crash = new Crash(throwable, AnalyticsControllerImpl.getInstance().getSessionAttributes(), AnalyticsControllerImpl.getInstance().getEventManager().getQueuedEvents(), CrashReporter.agentConfiguration.getEnableAnalyticsEvents());

                try {
                    CrashReporter.this.crashStore.store(crash);
                    CrashReporter.this.reportCrash(crash);
                    if (!Agent.getUnityInstrumentationFlag().equals("YES")) {
                        CrashReporter.executor.shutdown();
                        if (!CrashReporter.executor.awaitTermination(10000L, TimeUnit.MILLISECONDS)) {
                            CrashReporter.this.recordFailedUpload("Crash upload thread(s) timed-out before completion");
                        }
                    }
                }
                catch (Exception e) {
                    CrashReporter.this.recordFailedUpload("Exception caught while sending crash: " + e);
                }
            }
            catch (Throwable t) {
                CrashReporter.this.recordFailedUpload("Error encountered while preparing crash for New Relic! " + t);
            }
            finally {
                CrashReporter.this.storeSupportabilityMetrics();
                if (!Agent.getUnityInstrumentationFlag().equals("YES")) {
                    this.chainExceptionHandler(thread, throwable);
                }
            }
        }

        private void chainExceptionHandler(final Thread thread, final Throwable throwable) {
            if (CrashReporter.this.previousExceptionHandler != null) {
                CrashReporter.this.log.debug("Chaining crash reporting duties to " + CrashReporter.this.previousExceptionHandler.getClass().getSimpleName());
                CrashReporter.this.previousExceptionHandler.uncaughtException(thread, throwable);
            }
        }
    }

    private class CrashSender implements Runnable
    {
        private final Crash crash;

        CrashSender(final Crash crash) {
            this.crash = crash;
        }

        @Override
        public void run() {
            try {
                final String protocol = CrashReporter.agentConfiguration.useSsl() ? "https://" : "http://";
                final String urlString = protocol + CrashReporter.agentConfiguration.getCrashCollectorHost() + "/mobile_crash";
                final URL url = new URL(urlString);
                final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                final TicToc timer = new TicToc();
                timer.tic();
                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(0);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                try {
                    this.crash.incrementUploadCount();
                    CrashReporter.this.crashStore.store(this.crash);
                    final OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                    out.write(this.crash.toJsonString().getBytes());
                    out.close();
                    switch (connection.getResponseCode()) {
                        case 200: {
                            CrashReporter.this.crashStore.delete(this.crash);
                            StatsEngine.get().sampleTimeMs("Supportability/AgentHealth/Crash/UploadTime", timer.toc());
                            CrashReporter.this.log.info("Crash " + this.crash.getUuid().toString() + " successfully submitted.");
                            break;
                        }
                        case 500: {
                            CrashReporter.this.crashStore.delete(this.crash);
                            StatsEngine.get().inc("Supportability/AgentHealth/Crash/Removed/Rejected");
                            CrashReporter.this.recordFailedUpload("The crash was rejected and will be deleted - Response code " + connection.getResponseCode());
                            break;
                        }
                        default: {
                            CrashReporter.this.recordFailedUpload("Something went wrong while submitting a crash (will try again later) - Response code " + connection.getResponseCode());
                            break;
                        }
                    }
                }
                catch (Exception e) {
                    CrashReporter.this.recordFailedUpload("Crash upload failed: " + e);
                }
                finally {
                    connection.disconnect();
                }
                CrashReporter.this.log.debug("Crash collection took " + timer.toc() + "ms");
            }
            catch (Exception e2) {
                CrashReporter.this.recordFailedUpload("Unable to report crash to New Relic, will try again later. " + e2);
            }
        }
    }
}
