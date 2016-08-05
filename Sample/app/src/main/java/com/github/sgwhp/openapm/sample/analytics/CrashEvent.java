package com.github.sgwhp.openapm.sample.analytics;

import java.util.Set;

/**
 * Created by user on 2016/8/2.
 */
public class CrashEvent extends AnalyticsEvent{

    public CrashEvent(final String name) {
        super(name, AnalyticsEventCategory.Crash);
    }

    public CrashEvent(final String name, final Set<AnalyticAttribute> attributeSet) {
        super(name, AnalyticsEventCategory.Crash, "Mobile", attributeSet);
    }
}
