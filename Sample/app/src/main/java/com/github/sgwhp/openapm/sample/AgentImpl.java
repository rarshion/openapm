package com.github.sgwhp.openapm.sample;

import com.github.sgwhp.openapm.sample.api.common.TransactionData;
import com.github.sgwhp.openapm.sample.harvest.ApplicationInformation;
import com.github.sgwhp.openapm.sample.harvest.DeviceInformation;
import com.github.sgwhp.openapm.sample.harvest.EnvironmentInformation;
import com.github.sgwhp.openapm.sample.util.Encoder;

import java.util.List;

/**
 * Created by user on 2016/8/2.
 */
public interface AgentImpl {

    void addTransactionData(TransactionData p0);

    List<TransactionData> getAndClearTransactionData();

    void mergeTransactionData(List<TransactionData> p0);

    String getCrossProcessId();

    int getStackTraceLimit();

    int getResponseBodyLimit();

    void start();

    void stop();

    void disable();

    boolean isDisabled();

    String getNetworkCarrier();

    String getNetworkWanType();

    void setLocation(String p0, String p1);

    Encoder getEncoder();

    DeviceInformation getDeviceInformation();

    ApplicationInformation getApplicationInformation();

    EnvironmentInformation getEnvironmentInformation();

    boolean updateSavedConnectInformation();

    long getSessionDurationMillis();
}
