package com.github.sgwhp.openapm.sample.analytics;

import java.util.Map;
import java.util.Set;

/**
 * Created by user on 2016/8/2.
 */
public interface AnalyticsController {
    AnalyticAttribute getAttribute(String p0);

    Set<AnalyticAttribute> getSystemAttributes();

    Set<AnalyticAttribute> getUserAttributes();

    Set<AnalyticAttribute> getSessionAttributes();

    int getSystemAttributeCount();

    int getUserAttributeCount();

    int getSessionAttributeCount();

    boolean setAttribute(String p0, String p1);

    boolean setAttribute(String p0, String p1, boolean p2);

    boolean setAttribute(String p0, float p1);

    boolean setAttribute(String p0, float p1, boolean p2);

    boolean setAttribute(String p0, boolean p1);

    boolean setAttribute(String p0, boolean p1, boolean p2);

    boolean incrementAttribute(String p0, float p1);

    boolean incrementAttribute(String p0, float p1, boolean p2);

    boolean removeAttribute(String p0);

    boolean removeAllAttributes();

    boolean addEvent(AnalyticsEvent p0);

    boolean addEvent(String p0, Set<AnalyticAttribute> p1);

    boolean addEvent(String p0, AnalyticsEventCategory p1, String p2, Set<AnalyticAttribute> p3);

    int getMaxEventPoolSize();

    void setMaxEventPoolSize(int p0);

    int getMaxEventBufferTime();

    void setMaxEventBufferTime(int p0);

    EventManager getEventManager();

    boolean recordEvent(String p0, Map<String, Object> p1);
}
