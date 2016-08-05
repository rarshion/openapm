package com.github.sgwhp.openapm.sample.activity;

import com.github.sgwhp.openapm.sample.measurement.*;
import com.github.sgwhp.openapm.sample.tracing.TraceMachine;

/**
 * Created by user on 2016/8/1.
 */
public class BaseMeasuredActivity implements MeasuredActivity {
    private String name;
    private long startTime;
    private long endTime;
    private ThreadInfo startingThread;
    private ThreadInfo endingThread;
    private boolean autoInstrumented;
    private Measurement startingMeasurement;
    private Measurement endingMeasurement;
    private MeasurementPool measurementPool;
    private boolean finished;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getMetricName() {
        return TraceMachine.formatActivityMetricName(this.name);
    }

    @Override
    public String getBackgroundMetricName() {
        return TraceMachine.formatActivityBackgroundMetricName(this.name);
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public long getEndTime() {
        return this.endTime;
    }

    @Override
    public ThreadInfo getStartingThread() {
        return this.startingThread;
    }

    @Override
    public ThreadInfo getEndingThread() {
        return this.endingThread;
    }

    @Override
    public boolean isAutoInstrumented() {
        return this.autoInstrumented;
    }

    @Override
    public Measurement getStartingMeasurement() {
        return this.startingMeasurement;
    }

    @Override
    public Measurement getEndingMeasurement() {
        return this.endingMeasurement;
    }

    @Override
    public MeasurementPool getMeasurementPool() {
        return this.measurementPool;
    }

    @Override
    public void setName(final String name) {
        this.throwIfFinished();
        this.name = name;
    }

    public void setStartTime(final long startTime) {
        this.throwIfFinished();
        this.startTime = startTime;
    }

    public void setEndTime(final long endTime) {
        this.throwIfFinished();
        this.endTime = endTime;
    }

    public void setStartingThread(final ThreadInfo startingThread) {
        this.throwIfFinished();
        this.startingThread = startingThread;
    }

    public void setEndingThread(final ThreadInfo endingThread) {
        this.throwIfFinished();
        this.endingThread = endingThread;
    }

    public void setAutoInstrumented(final boolean autoInstrumented) {
        this.throwIfFinished();
        this.autoInstrumented = autoInstrumented;
    }

    public void setStartingMeasurement(final Measurement startingMeasurement) {
        this.throwIfFinished();
        this.startingMeasurement = startingMeasurement;
    }

    public void setEndingMeasurement(final Measurement endingMeasurement) {
        this.throwIfFinished();
        this.endingMeasurement = endingMeasurement;
    }

    public void setMeasurementPool(final MeasurementPool measurementPool) {
        this.throwIfFinished();
        this.measurementPool = measurementPool;
    }

    @Override
    public void finish() {
        this.finished = true;
    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    private void throwIfFinished() {
        if (this.finished) {
            throw new MeasurementException("Cannot modify finished Activity");
        }
    }
}
