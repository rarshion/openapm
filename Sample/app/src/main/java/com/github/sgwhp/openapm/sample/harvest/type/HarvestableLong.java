package com.github.sgwhp.openapm.sample.harvest.type;

/**
 * Created by user on 2016/8/1.
 */
import com.google.gson.*;

public class HarvestableLong extends HarvestableValue
{
    private long value;

    public HarvestableLong() {
    }

    public HarvestableLong(final long value) {
        this();
        this.value = value;
    }

    @Override
    public JsonPrimitive asJsonPrimitive() {
        return new JsonPrimitive(this.value);
    }
}
