package com.github.sgwhp.openapm.sample.measurement.consumer;

import com.github.sgwhp.openapm.sample.measurement.MeasurementType;

/**
 * Created by user on 2016/8/2.
 */
public class ActivityMeasurementConsumer extends MetricMeasurementConsumer {

    public ActivityMeasurementConsumer() {
        super(MeasurementType.Activity);
    }

    @Override
    protected String formatMetricName(final String name) {
        return name;
    }
}
