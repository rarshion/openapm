package com.github.sgwhp.openapm.sample.harvest;

import com.github.sgwhp.openapm.sample.harvest.type.HarvestableArray;
import com.github.sgwhp.openapm.sample.tracing.ActivityTrace;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by user on 2016/8/2.
 */
//Activity跟踪
public class ActivityTraces extends HarvestableArray {

    private final Collection<ActivityTrace> activityTraces;

    public ActivityTraces() {
        this.activityTraces = new ArrayList<ActivityTrace>();
    }

    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        for (final ActivityTrace activityTrace : this.activityTraces) {
            array.add(activityTrace.asJson());
        }
        return array;
    }

    public synchronized void add(final ActivityTrace activityTrace) {
        this.activityTraces.add(activityTrace);
    }

    public synchronized void remove(final ActivityTrace activityTrace) {
        this.activityTraces.remove(activityTrace);
    }

    public void clear() {
        this.activityTraces.clear();
    }

    public int count() {
        return this.activityTraces.size();
    }

    public Collection<ActivityTrace> getActivityTraces() {
        return this.activityTraces;
    }
}
