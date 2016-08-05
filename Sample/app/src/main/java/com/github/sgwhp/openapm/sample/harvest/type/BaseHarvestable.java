package com.github.sgwhp.openapm.sample.harvest.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import java.util.Map;

/**
 * Created by user on 2016/8/1.
 */
public class BaseHarvestable implements Harvestable
{
    private final Type type;
    protected static final java.lang.reflect.Type GSON_STRING_MAP_TYPE;

    public BaseHarvestable(final Type type) {
        this.type = type;
    }

    @Override
    public JsonElement asJson() {
        switch (this.type) {
            case OBJECT: {
                return this.asJsonObject();
            }
            case ARRAY: {
                return this.asJsonArray();
            }
            case VALUE: {
                return this.asJsonPrimitive();
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public String toJsonString() {
        return this.asJson().toString();
    }

    @Override
    public JsonArray asJsonArray() {
        return null;
    }

    @Override
    public JsonObject asJsonObject() {
        return null;
    }

    @Override
    public JsonPrimitive asJsonPrimitive() {
        return null;
    }

    protected void notEmpty(final String argument) {
        if (argument == null || argument.length() == 0) {
            throw new IllegalArgumentException("Missing Harvestable field.");
        }
    }

    protected void notNull(final Object argument) {
        if (argument == null) {
            throw new IllegalArgumentException("Null field in Harvestable object");
        }
    }

    protected String optional(final String argument) {
        if (argument == null) {
            return "";
        }
        return argument;
    }

    static {
        GSON_STRING_MAP_TYPE = new TypeToken<Map>() {}.getType();
    }
}
