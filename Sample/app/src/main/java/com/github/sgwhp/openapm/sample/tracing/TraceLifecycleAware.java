package com.github.sgwhp.openapm.sample.tracing;

/**
 * Created by user on 2016/8/1.
 */
public interface TraceLifecycleAware {
    void onEnterMethod();

    void onExitMethod();

    void onTraceStart(ActivityTrace p0);

    void onTraceComplete(ActivityTrace p0);
}
