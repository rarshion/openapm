package com.github.sgwhp.openapm.sample.metric;

/**
 * Created by user on 2016/8/2.
 */
public class MetricNames {

    public static final String ACTIVITY_NETWORK_METRIC_COUNT_FORMAT = "Mobile/Activity/Network/<activity>/Count";
    public static final String ACTIVITY_NETWORK_METRIC_TIME_FORMAT = "Mobile/Activity/Network/<activity>/Time";
    public static final String SUPPORTABILITY_CRASH_UPLOAD_TIME = "Supportability/AgentHealth/Crash/UploadTime";
    public static final String SUPPORTABILITY_CRASH_FAILED_UPLOAD = "Supportability/AgentHealth/Crash/FailedUpload";
    public static final String SUPPORTABILITY_CRASH_REMOVED_STALE = "Supportability/AgentHealth/Crash/Removed/Stale";
    public static final String SUPPORTABILITY_CRASH_REMOVED_REJECTED = "Supportability/AgentHealth/Crash/Removed/Rejected";
    public static final String SUPPORTABILITY_SESSION_INVALID_DURATION = "Supportability/AgentHealth/Session/InvalidDuration";
    public static final String MOBILE_APP_INSTALL = "Mobile/App/Install";
    public static final String MOBILE_APP_UPGRADE = "Mobile/App/Upgrade";
}
