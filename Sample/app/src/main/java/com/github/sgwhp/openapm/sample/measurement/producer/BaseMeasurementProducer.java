package com.github.sgwhp.openapm.sample.measurement.producer;

import com.github.sgwhp.openapm.sample.logging.*;
import com.github.sgwhp.openapm.sample.measurement.*;
import java.util.*;

/**
 * Created by user on 2016/8/1.
 */
public class BaseMeasurementProducer implements MeasurementProducer
{
    private static final AgentLog log;
    private final MeasurementType producedMeasurementType;
    private final ArrayList<Measurement> producedMeasurements;

    public BaseMeasurementProducer(final MeasurementType measurementType) {
        this.producedMeasurements = new ArrayList<Measurement>();
        this.producedMeasurementType = measurementType;
    }

    @Override
    public MeasurementType getMeasurementType() {
        return this.producedMeasurementType;
    }

    @Override
    public void produceMeasurement(final Measurement measurement) {
        synchronized (this.producedMeasurements) {
            if (measurement != null) {
                this.producedMeasurements.add(measurement);
            }
        }
    }

    @Override
    public void produceMeasurements(final Collection<Measurement> measurements) {
        synchronized (this.producedMeasurements) {
            if (measurements != null) {
                this.producedMeasurements.addAll(measurements);
                while (this.producedMeasurements.remove(null)) {}
            }
        }
    }
    @Override
    public Collection<Measurement> drainMeasurements() {
        synchronized (this.producedMeasurements) {
            if (this.producedMeasurements.size() == 0) {
                /*这是注释掉的，注意*/
                //return (Collection<Measurement>)Collections.emptyList();
                return null;
            }
            final Collection<Measurement> measurements = new ArrayList<Measurement>(this.producedMeasurements);
            this.producedMeasurements.clear();
            return measurements;
        }
    }
    static {
        log = AgentLogManager.getAgentLog();
    }
}
