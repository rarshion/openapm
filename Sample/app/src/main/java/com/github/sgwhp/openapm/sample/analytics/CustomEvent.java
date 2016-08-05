package com.github.sgwhp.openapm.sample.analytics;

import java.util.Set;

/**
 * Created by user on 2016/8/2.
 */
public class CustomEvent extends AnalyticsEvent{

    public CustomEvent(final String name) {
        super(name, AnalyticsEventCategory.Custom);
    }

    public CustomEvent(final String name, final Set<AnalyticAttribute> attributeSet) {
        super(name, AnalyticsEventCategory.Custom, null, attributeSet);
    }

    public CustomEvent(final String name, final String eventType, final Set<AnalyticAttribute> attributeSet) {
        super(name, AnalyticsEventCategory.Custom, eventType, attributeSet);
    }
}
