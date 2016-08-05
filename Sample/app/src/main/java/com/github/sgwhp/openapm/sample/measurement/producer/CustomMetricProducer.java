package com.github.sgwhp.openapm.sample.measurement.producer;

import com.github.sgwhp.openapm.sample.measurement.CustomMetricMeasurement;
import com.github.sgwhp.openapm.sample.measurement.MeasurementType;
import com.github.sgwhp.openapm.sample.metric.MetricUnit;

/**
 * Created by user on 2016/8/2.
 */
public class CustomMetricProducer extends BaseMeasurementProducer {
    private static final String FILTER_REGEX = "[/\\[\\]|*]";

    public CustomMetricProducer() {
        super(MeasurementType.Custom);
    }

    public void produceMeasurement(final String name, final String category, final int count, final double totalValue, final double exclusiveValue) {
        this.produceMeasurement(category, name, count, totalValue, exclusiveValue, null, null);
    }

    public void produceMeasurement(final String name, final String category, final int count, final double totalValue, final double exclusiveValue, final MetricUnit countUnit, final MetricUnit valueUnit) {
        final String metricName = this.createMetricName(name, category, countUnit, valueUnit);
        final CustomMetricMeasurement custom = new CustomMetricMeasurement(metricName, count, totalValue, exclusiveValue);
        this.produceMeasurement(custom);
    }

    private String createMetricName(final String name, final String category, final MetricUnit countUnit, final MetricUnit valueUnit) {
        final StringBuffer metricName = new StringBuffer();
        metricName.append(category.replaceAll("[/\\[\\]|*]", ""));
        metricName.append("/");
        metricName.append(name.replaceAll("[/\\[\\]|*]", ""));
        if (countUnit != null || valueUnit != null) {
            metricName.append("[");
            if (valueUnit != null) {
                metricName.append(valueUnit.getLabel());
            }
            if (countUnit != null) {
                metricName.append("|");
                metricName.append(countUnit.getLabel());
            }
            metricName.append("]");
        }
        return metricName.toString();
    }
}
