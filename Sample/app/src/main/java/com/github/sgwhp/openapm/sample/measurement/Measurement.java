package com.github.sgwhp.openapm.sample.measurement;

/**
 * Created by user on 2016/8/1.
 */
public interface Measurement {
    MeasurementType getType();

    String getName();

    String getScope();

    long getStartTime();

    double getStartTimeInSeconds();

    long getEndTime();

    double getEndTimeInSeconds();

    long getExclusiveTime();

    double getExclusiveTimeInSeconds();

    ThreadInfo getThreadInfo();

    boolean isInstantaneous();

    void finish();

    boolean isFinished();

    double asDouble();
}
