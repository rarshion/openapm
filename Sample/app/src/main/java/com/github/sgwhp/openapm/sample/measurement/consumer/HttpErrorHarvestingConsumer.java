package com.github.sgwhp.openapm.sample.measurement.consumer;

import com.github.sgwhp.openapm.sample.harvest.Harvest;
import com.github.sgwhp.openapm.sample.harvest.HttpError;
import com.github.sgwhp.openapm.sample.measurement.Measurement;
import com.github.sgwhp.openapm.sample.measurement.MeasurementType;
import com.github.sgwhp.openapm.sample.measurement.http.HttpErrorMeasurement;

/**
 * Created by user on 2016/8/2.
 */
public class HttpErrorHarvestingConsumer extends BaseMeasurementConsumer {
    public HttpErrorHarvestingConsumer() {
        super(MeasurementType.HttpError);
    }

    @Override
    public void consumeMeasurement(final Measurement measurement) {
        System.out.println("---Rarshion:HttpErrorHarvestingConsumer#consumeMeasurement" + measurement.getName());
        final HttpError error = new HttpError((HttpErrorMeasurement)measurement);
        Harvest.addHttpError(error);
    }
}
