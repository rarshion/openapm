package com.github.sgwhp.openapm.sample.measurement.producer;

import com.github.sgwhp.openapm.sample.Agent;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.measurement.MeasurementType;
import com.github.sgwhp.openapm.sample.measurement.ThreadInfo;
import com.github.sgwhp.openapm.sample.measurement.http.HttpErrorMeasurement;
import com.github.sgwhp.openapm.sample.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 2016/8/2.
 */
public class HttpErrorMeasurementProducer extends BaseMeasurementProducer {

    private static final AgentLog log;
    public static final String HTTP_METHOD_PARAMS_KEY = "http_method";
    public static final String WAN_TYPE_PARAMS_KEY = "wan_type";


    public HttpErrorMeasurementProducer() {
        super(MeasurementType.HttpError);
    }

    public void produceMeasurement(final String url, final String httpMethod, final int statusCode) {
        this.produceMeasurement(url, httpMethod, statusCode, "");
    }

    public void produceMeasurement(final String url, final String httpMethod, final int statusCode, final String responseBody) {
        this.produceMeasurement(url, httpMethod, statusCode, responseBody, null);
    }

    public void produceMeasurement(final String url, final String httpMethod, final int statusCode, final String responseBody, final Map<String, String> params) {
        this.produceMeasurement(url, httpMethod, statusCode, responseBody, params, new ThreadInfo());
    }

    public void produceMeasurement(final String urlString, final String httpMethod, final int statusCode, final String responseBody, Map<String, String> params, final ThreadInfo threadInfo) {
        final String url = Util.sanitizeUrl(urlString);
        if (url == null) {
            return;
        }
        final HttpErrorMeasurement measurement = new HttpErrorMeasurement(url, statusCode);
        if (params == null) {
            params = new HashMap<String, String>();
        }
        params.put("http_method", httpMethod);
        params.put("wan_type", Agent.getActiveNetworkWanType());
        measurement.setThreadInfo(threadInfo);
        measurement.setStackTrace(this.getSanitizedStackTrace());
        measurement.setResponseBody(responseBody);
        measurement.setParams(params);
        this.produceMeasurement(measurement);
    }

    private String getSanitizedStackTrace() {
        final StringBuilder stackTrace = new StringBuilder();
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        int numErrors = 0;
        for (int i = 0; i < stackTraceElements.length; ++i) {
            final StackTraceElement frame = stackTraceElements[i];
            if (!this.shouldFilterStackTraceElement(frame)) {
                stackTrace.append(frame.toString());
                if (i <= stackTraceElements.length - 1) {
                    stackTrace.append("\n");
                }
                if (++numErrors >= Agent.getStackTraceLimit()) {
                    break;
                }
            }
        }
        return stackTrace.toString();
    }

    private boolean shouldFilterStackTraceElement(final StackTraceElement element) {
        final String className = element.getClassName();
        final String method = element.getMethodName();
        return className.startsWith("com.newrelic") || (className.startsWith("dalvik.system.VMStack") && method.startsWith("getThreadStackTrace")) || (className.startsWith("java.lang.Thread") && method.startsWith("getStackTrace"));
    }

    static {
        log = AgentLogManager.getAgentLog();
    }
}
