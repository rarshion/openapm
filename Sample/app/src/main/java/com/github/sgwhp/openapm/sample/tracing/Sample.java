package com.github.sgwhp.openapm.sample.tracing;

import com.github.sgwhp.openapm.sample.harvest.type.HarvestableArray;
import com.github.sgwhp.openapm.sample.util.SafeJsonPrimitive;
import com.google.gson.JsonArray;

/**
 * Created by user on 2016/8/1.
 */
//采集数据
public class Sample extends HarvestableArray {

    private long timestamp;//采集时间
    private SampleValue sampleValue;//采集值
    private SampleType type;//采集值类型:CPU/MEMORY

    public Sample(final SampleType type) {
        this.setSampleType(type);
        this.setTimestamp(System.currentTimeMillis());
    }

    public Sample(final long timestamp) {
        this.setTimestamp(timestamp);
    }

    public Sample(final long timestamp, final SampleValue sampleValue) {
        this.setTimestamp(timestamp);
        this.setSampleValue(sampleValue);
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public SampleValue getSampleValue() {
        return this.sampleValue;
    }

    public void setSampleValue(final SampleValue sampleValue) {
        this.sampleValue = sampleValue;
    }

    public void setSampleValue(final double value) {
        this.sampleValue = new SampleValue(value);
    }

    public void setSampleValue(final long value) {
        this.sampleValue = new SampleValue(value);
    }

    public Number getValue() {
        return this.sampleValue.getValue();
    }

    public SampleType getSampleType() {
        return this.type;
    }

    public void setSampleType(final SampleType type) {
        this.type = type;
    }

    @Override
    public JsonArray asJsonArray() {
        final JsonArray jsonArray = new JsonArray();
        jsonArray.add(SafeJsonPrimitive.factory(this.timestamp));
        jsonArray.add(SafeJsonPrimitive.factory(this.sampleValue.getValue()));
        return jsonArray;
    }

    public enum SampleType
    {
        MEMORY,
        CPU;
    }
}
