package com.github.sgwhp.openapm.sample.analytics;

import java.util.Set;

/**
 * Created by user on 2016/8/2.
 */
public class InteractionEvent extends AnalyticsEvent{

    public InteractionEvent(final String name) {
        super(name, AnalyticsEventCategory.Interaction);
    }

    public InteractionEvent(final String name, final Set<AnalyticAttribute> attributeSet) {
        super(name, AnalyticsEventCategory.Interaction, "Mobile", attributeSet);
    }
}
