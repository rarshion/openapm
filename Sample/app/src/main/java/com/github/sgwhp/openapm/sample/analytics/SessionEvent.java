package com.github.sgwhp.openapm.sample.analytics;

import java.util.Set;

/**
 * Created by user on 2016/8/2.
 */
public class SessionEvent extends AnalyticsEvent{

    public SessionEvent() {
        super(null, AnalyticsEventCategory.Session);
    }

    public SessionEvent(final Set<AnalyticAttribute> attributeSet) {
        super(null, AnalyticsEventCategory.Session, "Mobile", attributeSet);
    }
}
