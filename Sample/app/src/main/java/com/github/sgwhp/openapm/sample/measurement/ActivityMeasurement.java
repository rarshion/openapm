package com.github.sgwhp.openapm.sample.measurement;

/**
 * Created by user on 2016/8/2.
 */
public class ActivityMeasurement extends BaseMeasurement {
    public ActivityMeasurement(final String name, final long startTime, final long endTime) {
        super(MeasurementType.Activity);
        this.setName(name);
        this.setStartTime(startTime);
        this.setEndTime(endTime);
    }
}
