package com.github.sgwhp.openapm.sample.analytics;

import java.util.Collection;

/**
 * Created by user on 2016/8/2.
 */
public interface EventManager {
    void initialize();

    void shutdown();

    int size();

    void empty();

    boolean isTransmitRequired();

    boolean addEvent(AnalyticsEvent p0);

    int getEventsRecorded();

    int getEventsEjected();

    boolean isMaxEventBufferTimeExceeded();

    boolean isMaxEventPoolSizeExceeded();

    int getMaxEventPoolSize();

    void setMaxEventPoolSize(int p0);

    int getMaxEventBufferTime();

    void setMaxEventBufferTime(int p0);

    Collection<AnalyticsEvent> getQueuedEvents();
}
