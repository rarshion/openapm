package com.github.sgwhp.openapm.sample.measurement.consumer;

import com.github.sgwhp.openapm.sample.Agent;
import com.github.sgwhp.openapm.sample.harvest.Harvest;
import com.github.sgwhp.openapm.sample.harvest.HttpTransaction;
import com.github.sgwhp.openapm.sample.measurement.Measurement;
import com.github.sgwhp.openapm.sample.measurement.MeasurementType;
import com.github.sgwhp.openapm.sample.measurement.http.HttpTransactionMeasurement;

/**
 * Created by user on 2016/8/2.
 */
public class HttpTransactionHarvestingConsumer extends BaseMeasurementConsumer {

    public HttpTransactionHarvestingConsumer() {
        super(MeasurementType.Network);
    }

    @Override
    public void consumeMeasurement(final Measurement measurement) {
        final HttpTransactionMeasurement m = (HttpTransactionMeasurement)measurement;
        final HttpTransaction txn = new HttpTransaction();
        txn.setUrl(m.getUrl());
        txn.setHttpMethod(m.getHttpMethod());
        txn.setStatusCode(m.getStatusCode());
        txn.setErrorCode(m.getErrorCode());
        txn.setTotalTime(m.getTotalTime());
        txn.setCarrier(Agent.getActiveNetworkCarrier());
        txn.setWanType(Agent.getActiveNetworkWanType());
        txn.setBytesReceived(m.getBytesReceived());
        txn.setBytesSent(m.getBytesSent());
        txn.setAppData(m.getAppData());
        txn.setTimestamp(m.getStartTime());
        Harvest.addHttpTransaction(txn);
    }
}
