package com.github.sgwhp.openapm.sample.activity.config;

/**
 * Created by user on 2016/8/1.
 */
public class ActivityTraceConfiguration {
    private int maxTotalTraceCount;

    public static ActivityTraceConfiguration defaultActivityTraceConfiguration() {
        final ActivityTraceConfiguration configuration = new ActivityTraceConfiguration();
        configuration.setMaxTotalTraceCount(1);
        return configuration;
    }

    public int getMaxTotalTraceCount() {
        return this.maxTotalTraceCount;
    }

    public void setMaxTotalTraceCount(final int maxTotalTraceCount) {
        this.maxTotalTraceCount = maxTotalTraceCount;
    }

    @Override
    public String toString() {
        return "ActivityTraceConfiguration{maxTotalTraceCount=" + this.maxTotalTraceCount + '}';
    }
}
