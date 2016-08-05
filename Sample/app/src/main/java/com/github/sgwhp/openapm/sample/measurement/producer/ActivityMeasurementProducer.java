package com.github.sgwhp.openapm.sample.measurement.producer;

import com.github.sgwhp.openapm.sample.activity.MeasuredActivity;
import com.github.sgwhp.openapm.sample.measurement.ActivityMeasurement;
import com.github.sgwhp.openapm.sample.measurement.MeasurementType;

/**
 * Created by user on 2016/8/2.
 */
public class ActivityMeasurementProducer extends BaseMeasurementProducer {

    public ActivityMeasurementProducer() {
        super(MeasurementType.Activity);
    }

    public void produceMeasurement(final MeasuredActivity measuredActivity) {
        super.produceMeasurement(new ActivityMeasurement(measuredActivity.getMetricName(), measuredActivity.getStartTime(), measuredActivity.getEndTime()));
        super.produceMeasurement(new ActivityMeasurement(measuredActivity.getBackgroundMetricName(), measuredActivity.getStartTime(), measuredActivity.getEndTime()));
    }
}
