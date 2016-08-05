package com.github.sgwhp.openapm.sample.harvest;

import com.github.sgwhp.openapm.sample.harvest.type.HarvestableArray;
import com.github.sgwhp.openapm.sample.metric.Metric;
import com.github.sgwhp.openapm.sample.metric.MetricStore;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.HashMap;

/**
 * Created by user on 2016/8/2.
 */
public class MachineMeasurements extends HarvestableArray {

    private final MetricStore metrics;

    public MachineMeasurements() {
        this.metrics = new MetricStore();
    }

    public void addMetric(final String name, final double value) {
        final Metric metric = new Metric(name);
        metric.sample(value);
        this.addMetric(metric);
    }

    public void addMetric(final Metric metric) {
        this.metrics.add(metric);
    }

    public void clear() {
        this.metrics.clear();
    }

    public boolean isEmpty() {
        return this.metrics.isEmpty();
    }

    public MetricStore getMetrics() {
        return this.metrics;
    }

    @Override
    public JsonArray asJsonArray() {
        final JsonArray metricArray = new JsonArray();
        for (final Metric metric : this.metrics.getAll()) {
            final JsonArray metricJson = new JsonArray();
            final HashMap<String, String> header = new HashMap<String, String>();
            header.put("name", metric.getName());
            header.put("scope", metric.getStringScope());
            metricJson.add(new Gson().toJsonTree(header, MachineMeasurements.GSON_STRING_MAP_TYPE));
            metricJson.add(metric.asJsonObject());
            metricArray.add(metricJson);
        }
        return metricArray;
    }
}
