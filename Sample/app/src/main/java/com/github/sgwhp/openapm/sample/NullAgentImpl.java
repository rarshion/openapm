package com.github.sgwhp.openapm.sample;

import com.github.sgwhp.openapm.sample.api.common.TransactionData;
import com.github.sgwhp.openapm.sample.harvest.ApplicationInformation;
import com.github.sgwhp.openapm.sample.harvest.DeviceInformation;
import com.github.sgwhp.openapm.sample.harvest.EnvironmentInformation;
import com.github.sgwhp.openapm.sample.util.Encoder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016/8/2.
 */
public class NullAgentImpl implements AgentImpl{

    public static final NullAgentImpl instance;
    private final ReentrantLock lock;
    private int responseBodyLimit;
    private final AgentConfiguration agentConfiguration;
    private long sessionStartTimeMillis;

    public NullAgentImpl() {
        this.lock = new ReentrantLock();
        this.agentConfiguration = new AgentConfiguration();
        this.sessionStartTimeMillis = 0L;
    }

    @Override
    public void addTransactionData(final TransactionData transactionData) {
    }

    @Override
    public List<TransactionData> getAndClearTransactionData() {
        return new ArrayList<TransactionData>();
    }

    @Override
    public void mergeTransactionData(final List<TransactionData> transactionDataList) {
    }

    @Override
    public String getCrossProcessId() {
        return null;
    }

    @Override
    public int getStackTraceLimit() {
        return 0;
    }

    @Override
    public int getResponseBodyLimit() {
        return this.responseBodyLimit;
    }

    public void setResponseBodyLimit(final int responseBodyLimit) {
        this.responseBodyLimit = responseBodyLimit;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void disable() {
    }

    @Override
    public boolean isDisabled() {
        return true;
    }

    @Override
    public String getNetworkCarrier() {
        return "unknown";
    }

    @Override
    public String getNetworkWanType() {
        return "unknown";
    }

    @Override
    public void setLocation(final String countryCode, final String adminRegion) {
    }

    @Override
    public Encoder getEncoder() {
        return new Encoder() {
            @Override
            public String encode(final byte[] bytes) {
                return new String(bytes);
            }
        };
    }

    @Override
    public DeviceInformation getDeviceInformation() {
        final DeviceInformation devInfo = new DeviceInformation();
        devInfo.setOsName("Android");
        devInfo.setOsVersion("2.3");
        devInfo.setOsBuild("a.b.c");
        devInfo.setManufacturer("Fake");
        devInfo.setModel("NullAgent");
        devInfo.setAgentName("AndroidAgent");
        devInfo.setAgentVersion("2.123");
        devInfo.setDeviceId("389C9738-A761-44DE-8A66-1668CFD67DA1");
        devInfo.setArchitecture("Fake Arch");
        devInfo.setRunTime("1.7.0");
        devInfo.setSize("Fake Size");
        return devInfo;
    }

    @Override
    public ApplicationInformation getApplicationInformation() {
        return new ApplicationInformation("null", "0.0", "null", "0");
    }

    @Override
    public EnvironmentInformation getEnvironmentInformation() {
        return new EnvironmentInformation(0L, 1, "none", "none", new long[] { 0L, 0L });
    }

    @Override
    public boolean updateSavedConnectInformation() {
        return false;
    }

    @Override
    public long getSessionDurationMillis() {
        return this.sessionStartTimeMillis;
    }

    static {
        instance = new NullAgentImpl();
    }


}
