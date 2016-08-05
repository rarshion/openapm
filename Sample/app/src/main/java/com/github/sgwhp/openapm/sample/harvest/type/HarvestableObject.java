package com.github.sgwhp.openapm.sample.harvest.type;

/**
 * Created by user on 2016/8/1.
 */
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Map;

public abstract class HarvestableObject extends BaseHarvestable
{
    public static HarvestableObject fromMap(final Map<String, String> map) {
        return new HarvestableObject() {
            @Override
            public JsonObject asJsonObject() {
                return (JsonObject)new Gson().toJsonTree(map, GSON_STRING_MAP_TYPE);
            }
        };
    }

    public HarvestableObject() {
        super(Type.OBJECT);
    }

    @Override
    public abstract JsonObject asJsonObject();
}
