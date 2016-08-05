package com.github.sgwhp.openapm.sample.measurement.producer;

import com.github.sgwhp.openapm.sample.measurement.MeasurementType;
import com.github.sgwhp.openapm.sample.measurement.MethodMeasurement;
import com.github.sgwhp.openapm.sample.tracing.Trace;

/**
 * Created by user on 2016/8/2.
 */
public class MethodMeasurementProducer extends BaseMeasurementProducer {

    public MethodMeasurementProducer() {
        super(MeasurementType.Method);
    }

    public void produceMeasurement(final Trace trace) {
        final MethodMeasurement methodMeasurement = new MethodMeasurement(trace.displayName, trace.scope, trace.entryTimestamp, trace.exitTimestamp, trace.exclusiveTime, trace.getCategory());
        this.produceMeasurement(methodMeasurement);
    }
}
