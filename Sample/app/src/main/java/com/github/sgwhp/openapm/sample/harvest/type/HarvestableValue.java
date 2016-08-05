package com.github.sgwhp.openapm.sample.harvest.type;

import com.google.gson.*;

/**
 * Created by user on 2016/8/1.
 */
public abstract class HarvestableValue extends BaseHarvestable {

    public HarvestableValue() {
        super(Type.VALUE);
    }

    @Override
    public abstract JsonPrimitive asJsonPrimitive();
}
