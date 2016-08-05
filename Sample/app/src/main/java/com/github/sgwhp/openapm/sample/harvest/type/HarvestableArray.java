package com.github.sgwhp.openapm.sample.harvest.type;

import com.google.gson.*;
/**
 * Created by user on 2016/8/1.
 */
public abstract class HarvestableArray extends BaseHarvestable
{
    public HarvestableArray() {
        super(Type.ARRAY);
    }

    @Override
    public abstract JsonArray asJsonArray();
}

