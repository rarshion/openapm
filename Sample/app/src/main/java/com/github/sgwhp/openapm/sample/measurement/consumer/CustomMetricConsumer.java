package com.github.sgwhp.openapm.sample.measurement.consumer;

import com.github.sgwhp.openapm.sample.measurement.CustomMetricMeasurement;
import com.github.sgwhp.openapm.sample.measurement.Measurement;
import com.github.sgwhp.openapm.sample.measurement.MeasurementType;
import com.github.sgwhp.openapm.sample.metric.Metric;

/**
 * Created by user on 2016/8/2.
 */
public class CustomMetricConsumer extends MetricMeasurementConsumer {

    private static final String METRIC_PREFIX = "Custom/";

    public CustomMetricConsumer() {
        super(MeasurementType.Custom);
    }

    @Override
    protected String formatMetricName(final String name) {
        return "Custom/" + name;
    }

    @Override
    public void consumeMeasurement(final Measurement measurement) {
        final CustomMetricMeasurement custom = (CustomMetricMeasurement)measurement;
        final Metric metric = custom.getCustomMetric();
        metric.setName(this.formatMetricName(metric.getName()));
        this.addMetric(metric);
    }
}
