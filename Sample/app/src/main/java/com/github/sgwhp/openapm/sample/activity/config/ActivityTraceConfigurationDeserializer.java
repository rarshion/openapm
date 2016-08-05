package com.github.sgwhp.openapm.sample.activity.config;

/**
 * Created by user on 2016/8/1.
 */

import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ActivityTraceConfigurationDeserializer implements JsonDeserializer<ActivityTraceConfiguration>{

    private final AgentLog log;

    public ActivityTraceConfigurationDeserializer() {
        this.log = AgentLogManager.getAgentLog();
    }

    @Override
    public ActivityTraceConfiguration deserialize(final JsonElement root, final Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final ActivityTraceConfiguration configuration = new ActivityTraceConfiguration();
        if (!root.isJsonArray()) {
            this.error("Expected root element to be an array.");
            return null;
        }
        final JsonArray array = root.getAsJsonArray();
        if (array.size() != 2) {
            this.error("Root array must contain 2 elements.");
            return null;
        }
        final Integer maxTotalTraceCount = this.getInteger(array.get(0));
        if (maxTotalTraceCount == null) {
            return null;
        }
        if (maxTotalTraceCount < 0) {
            this.error("The first element of the root array must not be negative.");
            return null;
        }
        configuration.setMaxTotalTraceCount(maxTotalTraceCount);
        return configuration;
    }

    private Integer getInteger(final JsonElement element) {
        if (!element.isJsonPrimitive()) {
            this.error("Expected an integer.");
            return null;
        }
        final JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isNumber()) {
            this.error("Expected an integer.");
            return null;
        }
        final int value = primitive.getAsInt();
        if (value < 0) {
            this.error("Integer value must not be negative");
            return null;
        }
        return value;
    }

    private void error(final String message) {
        this.log.error("ActivityTraceConfigurationDeserializer: " + message);
    }
}
