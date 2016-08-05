package com.github.sgwhp.openapm.sample.harvest.type;

import com.google.gson.*;
/**
 * Created by user on 2016/8/1.
 */
public interface Harvestable {
    Type getType();

    JsonElement asJson();

    JsonObject asJsonObject();

    JsonArray asJsonArray();

    JsonPrimitive asJsonPrimitive();

    String toJsonString();

    public enum Type
    {
        OBJECT,
        ARRAY,
        VALUE;
    }



}
