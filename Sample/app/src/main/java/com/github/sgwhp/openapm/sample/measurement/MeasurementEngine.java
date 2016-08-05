package com.github.sgwhp.openapm.sample.measurement;

import com.github.sgwhp.openapm.sample.activity.MeasuredActivity;
import com.github.sgwhp.openapm.sample.activity.NamedActivity;
import com.github.sgwhp.openapm.sample.measurement.consumer.MeasurementConsumer;
import com.github.sgwhp.openapm.sample.measurement.producer.MeasurementProducer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by user on 2016/8/2.
 */
public class MeasurementEngine {
    private final Map<String, MeasuredActivity> activities;
    private final MeasurementPool rootMeasurementPool;

    public MeasurementEngine() {
        this.activities = new ConcurrentHashMap<String, MeasuredActivity>();
        this.rootMeasurementPool = new MeasurementPool();
    }

    public MeasuredActivity startActivity(final String activityName) {
        if (this.activities.containsKey(activityName)) {
            throw new MeasurementException("An activity with the name '" + activityName + "' has already started.");
        }
        final NamedActivity activity = new NamedActivity(activityName);
        this.activities.put(activityName, activity);
        final MeasurementPool measurementPool = new MeasurementPool();
        activity.setMeasurementPool(measurementPool);
        this.rootMeasurementPool.addMeasurementConsumer(measurementPool);
        return activity;
    }

    public void renameActivity(final String oldName, final String newName) {
        final MeasuredActivity namedActivity = this.activities.remove(oldName);
        if (namedActivity != null && namedActivity instanceof NamedActivity) {
            this.activities.put(newName, namedActivity);
            ((NamedActivity)namedActivity).rename(newName);
        }
    }

    public MeasuredActivity endActivity(final String activityName) {
        final MeasuredActivity measuredActivity = this.activities.get(activityName);
        if (measuredActivity == null) {
            throw new MeasurementException("Activity '" + activityName + "' has not been started.");
        }
        this.endActivity(measuredActivity);
        return measuredActivity;
    }

    public void endActivity(final MeasuredActivity activity) {
        this.rootMeasurementPool.removeMeasurementConsumer(activity.getMeasurementPool());
        this.activities.remove(activity.getName());
        activity.finish();
    }

    public void clear() {
        this.activities.clear();
    }

    public void addMeasurementProducer(final MeasurementProducer measurementProducer) {
        this.rootMeasurementPool.addMeasurementProducer(measurementProducer);
    }

    public void removeMeasurementProducer(final MeasurementProducer measurementProducer) {
        this.rootMeasurementPool.removeMeasurementProducer(measurementProducer);
    }

    public void addMeasurementConsumer(final MeasurementConsumer measurementConsumer) {
        this.rootMeasurementPool.addMeasurementConsumer(measurementConsumer);
    }

    public void removeMeasurementConsumer(final MeasurementConsumer measurementConsumer) {
        this.rootMeasurementPool.removeMeasurementConsumer(measurementConsumer);
    }

    public void broadcastMeasurements() {
        this.rootMeasurementPool.broadcastMeasurements();
    }
}
