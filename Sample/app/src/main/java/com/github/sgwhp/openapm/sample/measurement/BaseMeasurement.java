package com.github.sgwhp.openapm.sample.measurement;

import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;

/*
 * Created by user on 2016/8/1.
 */
public class BaseMeasurement implements Measurement{

    private static final AgentLog log;
    private MeasurementType type;
    private String name;
    private String scope;
    private long startTime;
    private long endTime;
    private long exclusiveTime;
    private ThreadInfo threadInfo;
    private boolean finished;

    public BaseMeasurement(final MeasurementType measurementType) {
        this.setType(measurementType);
    }

    public BaseMeasurement(final Measurement measurement) {
        this.setType(measurement.getType());
        this.setName(measurement.getName());
        this.setScope(measurement.getScope());
        this.setStartTime(measurement.getStartTime());
        this.setEndTime(measurement.getEndTime());
        this.setExclusiveTime(measurement.getExclusiveTime());
        this.setThreadInfo(measurement.getThreadInfo());
        this.finished = measurement.isFinished();
    }

    void setType(final MeasurementType type) {
        this.throwIfFinished();
        this.type = type;
    }

    public void setName(final String name) {
        this.throwIfFinished();
        this.name = name;
    }

    public void setScope(final String scope) {
        this.throwIfFinished();
        this.scope = scope;
    }

    public void setStartTime(final long startTime) {
        this.throwIfFinished();
        this.startTime = startTime;
    }

    public void setEndTime(final long endTime) {
        this.throwIfFinished();
        if (endTime < this.startTime) {
            BaseMeasurement.log.error("Measurement end time must not precede start time - startTime: " + this.startTime + " endTime: " + endTime);
            return;
        }
        this.endTime = endTime;
    }

    public void setExclusiveTime(final long exclusiveTime) {
        this.throwIfFinished();
        this.exclusiveTime = exclusiveTime;
    }

    @Override
    public MeasurementType getType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getScope() {
        return this.scope;
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public double getStartTimeInSeconds() {
        return this.startTime / 1000.0;
    }

    @Override
    public long getEndTime() {
        return this.endTime;
    }

    @Override
    public double getEndTimeInSeconds() {
        return this.endTime / 1000.0;
    }

    @Override
    public long getExclusiveTime() {
        return this.exclusiveTime;
    }

    @Override
    public double getExclusiveTimeInSeconds() {
        return this.exclusiveTime / 1000.0;
    }

    @Override
    public double asDouble() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ThreadInfo getThreadInfo() {
        return this.threadInfo;
    }

    public void setThreadInfo(final ThreadInfo threadInfo) {
        this.threadInfo = threadInfo;
    }

    @Override
    public boolean isInstantaneous() {
        return this.endTime == 0L;
    }

    @Override
    public void finish() {
        if (this.finished) {
            throw new MeasurementException("Finish called on already finished Measurement");
        }
        this.finished = true;
    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    private void throwIfFinished() {
        if (this.finished) {
            throw new MeasurementException("Attempted to modify finished Measurement");
        }
    }

    @Override
    public String toString() {
        return "BaseMeasurement{type=" + this.type + ", name='" + this.name + '\'' + ", scope='" + this.scope + '\'' + ", startTime=" + this.startTime + ", endTime=" + this.endTime + ", exclusiveTime=" + this.exclusiveTime + ", threadInfo=" + this.threadInfo + ", finished=" + this.finished + '}';
    }

    static {
        log = AgentLogManager.getAgentLog();
    }

}
