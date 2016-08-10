package com.github.sgwhp.openapm.sample.measurement.producer;

import com.github.sgwhp.openapm.sample.measurement.Measurement;
import com.github.sgwhp.openapm.sample.measurement.MeasurementType;
import java.util.Collection;

/**
 * Created by user on 2016/8/1.
 */
public interface MeasurementProducer {
    MeasurementType getMeasurementType();//获取测量类型
    void produceMeasurement(Measurement p0);
    void produceMeasurements(Collection<Measurement> p0);
    Collection<Measurement> drainMeasurements();
}
