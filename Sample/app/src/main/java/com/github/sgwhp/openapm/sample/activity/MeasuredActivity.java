package com.github.sgwhp.openapm.sample.activity;

import com.github.sgwhp.openapm.sample.measurement.*;

public interface MeasuredActivity
{
    String getName();
    String getMetricName();
    void setName(String p0);
    String getBackgroundMetricName();
    long getStartTime();
    long getEndTime();
    ThreadInfo getStartingThread();
    ThreadInfo getEndingThread();
    boolean isAutoInstrumented();
    Measurement getStartingMeasurement();
    Measurement getEndingMeasurement();
    MeasurementPool getMeasurementPool();
    void finish();
    boolean isFinished();

}