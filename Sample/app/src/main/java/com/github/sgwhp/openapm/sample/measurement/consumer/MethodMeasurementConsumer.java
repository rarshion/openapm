package com.github.sgwhp.openapm.sample.measurement.consumer;

import com.github.sgwhp.openapm.sample.measurement.MeasurementType;

/**
 * Created by user on 2016/8/2.
 */
public class MethodMeasurementConsumer extends MetricMeasurementConsumer {

    private static final String METRIC_PREFIX = "Method/";

    public MethodMeasurementConsumer() {
        super(MeasurementType.Method);
    }

    @Override
    protected String formatMetricName(final String name) {
        return "Method/" + name.replace("#", "/");
    }
}
