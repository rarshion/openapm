package com.github.sgwhp.openapm.sample.measurement;

import com.github.sgwhp.openapm.sample.Instrumentation.MetricCategory;

/**
 * Created by user on 2016/8/1.
 */
public class MethodMeasurement extends CategorizedMeasurement{
    public MethodMeasurement(final String name, final String scope, final long startTime, final long endTime, final long exclusiveTime, final MetricCategory category) {
        super(MeasurementType.Method);
        this.setName(name);
        this.setScope(scope);
        this.setStartTime(startTime);
        this.setEndTime(endTime);
        this.setExclusiveTime(exclusiveTime);
        this.setCategory(category);
    }
}
