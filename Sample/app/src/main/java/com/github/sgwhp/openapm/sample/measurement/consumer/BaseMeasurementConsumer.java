package com.github.sgwhp.openapm.sample.measurement.consumer;
import com.github.sgwhp.openapm.sample.harvest.HarvestAdapter;
import com.github.sgwhp.openapm.sample.measurement.Measurement;
import com.github.sgwhp.openapm.sample.measurement.MeasurementType;
import java.util.Collection;

/**
 * Created by user on 2016/8/1.
 */
public class BaseMeasurementConsumer extends HarvestAdapter implements MeasurementConsumer
{
    private final MeasurementType measurementType;

    public BaseMeasurementConsumer(final MeasurementType measurementType) {
        this.measurementType = measurementType;
    }

    @Override
    public MeasurementType getMeasurementType() {
        return this.measurementType;
    }

    @Override
    public void consumeMeasurement(final Measurement measurement) {
    }

    @Override
    public void consumeMeasurements(final Collection<Measurement> measurements) {
        for (final Measurement measurement : measurements) {
            this.consumeMeasurement(measurement);
        }
    }
}