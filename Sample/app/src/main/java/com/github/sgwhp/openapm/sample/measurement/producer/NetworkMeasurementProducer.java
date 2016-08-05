package com.github.sgwhp.openapm.sample.measurement.producer;

import com.github.sgwhp.openapm.sample.measurement.MeasurementType;
import com.github.sgwhp.openapm.sample.measurement.http.HttpTransactionMeasurement;
import com.github.sgwhp.openapm.sample.util.Util;

/**
 * Created by user on 2016/8/2.
 */
public class NetworkMeasurementProducer extends BaseMeasurementProducer {

    public NetworkMeasurementProducer() {
        super(MeasurementType.Network);
    }

    public void produceMeasurement(final String urlString, final String httpMethod, final int statusCode, final int errorCode, final long startTime, final double totalTime, final long bytesSent, final long bytesReceived, final String appData) {
        final String url = Util.sanitizeUrl(urlString);
        if (url == null) {
            return;
        }
        this.produceMeasurement(new HttpTransactionMeasurement(url, httpMethod, statusCode, errorCode, startTime, totalTime, bytesSent, bytesReceived, appData));
    }

    public void produceMeasurement(final HttpTransactionMeasurement transactionMeasurement) {
        final String url = Util.sanitizeUrl(transactionMeasurement.getUrl());
        if (url == null) {
            return;
        }
        transactionMeasurement.setUrl(url);
        super.produceMeasurement(transactionMeasurement);
    }
}
