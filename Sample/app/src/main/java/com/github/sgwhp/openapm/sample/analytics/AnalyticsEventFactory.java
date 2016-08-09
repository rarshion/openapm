package com.github.sgwhp.openapm.sample.analytics;

import java.util.Set;

/**
 * Created by user on 2016/8/2.
 */
public class AnalyticsEventFactory {

    static AnalyticsEvent createEvent(final String name, final AnalyticsEventCategory eventCategory, final String eventType,
                                      final Set<AnalyticAttribute> eventAttributes) {
        AnalyticsEvent event = null;
        //根据种类来生成不同的事件类型
        switch (eventCategory) {
            case Session: {
                event = new SessionEvent(eventAttributes);
                break;
            }
            case Interaction: {
                event = new InteractionEvent(name, eventAttributes);
                break;
            }
            case Crash: {
                event = new CrashEvent(name, eventAttributes);
                break;
            }
            case Custom: {
                event = new CustomEvent(name, eventType, eventAttributes);
                break;
            }
        }
        return event;
    }
}
