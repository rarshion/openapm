package com.github.sgwhp.openapm.sample.harvest;

import com.github.sgwhp.openapm.sample.harvest.type.HarvestableArray;
import com.github.sgwhp.openapm.sample.util.SafeJsonPrimitive;
import com.google.gson.JsonArray;

/**
 * Created by user on 2016/8/1.
 */
//简单记录线程属性对象
public class ActivitySighting extends HarvestableArray {

    private String name;
    private final long timestampMs;
    private long durationMs;

    public ActivitySighting(final long timestampMs, final String name) {
        this.durationMs = 0L;
        this.timestampMs = timestampMs;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        synchronized (this) {
            this.name = name;
        }
    }

    public long getTimestampMs() {
        return this.timestampMs;
    }

    public long getDuration() {
        return this.durationMs;
    }

    public void end(final long endTimestampMs) {
        synchronized (this) {
            this.durationMs = endTimestampMs - this.timestampMs;
        }
    }

    @Override
    public JsonArray asJsonArray() {
        final JsonArray data = new JsonArray();
        synchronized (this) {
            data.add(SafeJsonPrimitive.factory(this.name));
            data.add(SafeJsonPrimitive.factory(this.timestampMs));
            data.add(SafeJsonPrimitive.factory(this.durationMs));
        }
        return data;
    }

    public JsonArray asJsonArrayWithoutDuration() {
        final JsonArray data = new JsonArray();
        synchronized (this) {
            data.add(SafeJsonPrimitive.factory(this.timestampMs));
            data.add(SafeJsonPrimitive.factory(this.name));
        }
        return data;
    }

    public static ActivitySighting newFromJson(final JsonArray jsonArray) {
        return new ActivitySighting(jsonArray.get(0).getAsLong(), jsonArray.get(1).getAsString());
    }
}
