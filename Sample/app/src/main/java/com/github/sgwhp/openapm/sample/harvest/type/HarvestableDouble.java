package com.github.sgwhp.openapm.sample.harvest.type;

import com.google.gson.*;
/**
 * Created by user on 2016/8/1.
 */
public class HarvestableDouble extends HarvestableValue
{
    private double value;

    public HarvestableDouble() {
    }

    public HarvestableDouble(final double value) {
        this();
        this.value = value;
    }

    @Override
    public JsonPrimitive asJsonPrimitive() {
        return new JsonPrimitive(this.value);
    }
}
