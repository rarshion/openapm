package com.github.sgwhp.openapm.sample.measurement.consumer;

import com.github.sgwhp.openapm.sample.measurement.Measurement;
import com.github.sgwhp.openapm.sample.measurement.MeasurementType;
import java.util.Collection;

/**
 * Created by user on 2016/8/1.
 */
public interface MeasurementConsumer {
    MeasurementType getMeasurementType();
    void consumeMeasurement(Measurement p0);
    void consumeMeasurements(Collection<Measurement> p0);

}
