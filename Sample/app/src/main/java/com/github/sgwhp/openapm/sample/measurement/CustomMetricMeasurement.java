package com.github.sgwhp.openapm.sample.measurement;

import com.github.sgwhp.openapm.sample.metric.Metric;

/**
 * Created by user on 2016/8/2.
 */
public class CustomMetricMeasurement extends CategorizedMeasurement {
    private Metric customMetric;

    public CustomMetricMeasurement() {
        super(MeasurementType.Custom);
    }

    public CustomMetricMeasurement(final String name, final int count, final double totalValue, final double exclusiveValue) {
        this();
        this.setName(name);
        (this.customMetric = new Metric(name)).sample(totalValue);
        this.customMetric.setCount(count);
        this.customMetric.setExclusive(exclusiveValue);
    }

    public Metric getCustomMetric() {
        return this.customMetric;
    }
}
