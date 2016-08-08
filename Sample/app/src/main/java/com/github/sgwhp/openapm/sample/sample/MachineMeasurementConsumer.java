package com.github.sgwhp.openapm.sample.sample;

import com.github.sgwhp.openapm.sample.measurement.Measurement;
import com.github.sgwhp.openapm.sample.measurement.MeasurementType;
import com.github.sgwhp.openapm.sample.measurement.consumer.MetricMeasurementConsumer;
import com.github.sgwhp.openapm.sample.metric.Metric;
import com.github.sgwhp.openapm.sample.tracing.Sample;

/**
 * Created by user on 2016/8/8.
 */
public class MachineMeasurementConsumer extends MetricMeasurementConsumer
{
    public MachineMeasurementConsumer() {
        super(MeasurementType.Machine);
    }

    protected String formatMetricName(final String name) {
        return name;
    }

    public void consumeMeasurement(final Measurement measurement) {
    }

    public void onHarvest() {
        final Sample memorySample = Sampler.sampleMemory();
        if (memorySample != null) {
            final Metric memoryMetric = new Metric("Memory/Used");
            memoryMetric.sample(memorySample.getValue().doubleValue());
            this.addMetric(memoryMetric);
        }
        super.onHarvest();
    }
}
