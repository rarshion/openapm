package com.github.sgwhp.openapm.sample.measurement.consumer;

import com.github.sgwhp.openapm.sample.Instrumentation.MetricCategory;
import com.github.sgwhp.openapm.sample.harvest.Harvest;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.measurement.BaseMeasurement;
import com.github.sgwhp.openapm.sample.measurement.CustomMetricMeasurement;
import com.github.sgwhp.openapm.sample.measurement.Measurement;
import com.github.sgwhp.openapm.sample.measurement.MeasurementType;
import com.github.sgwhp.openapm.sample.measurement.MethodMeasurement;
import com.github.sgwhp.openapm.sample.measurement.http.HttpTransactionMeasurement;
import com.github.sgwhp.openapm.sample.metric.Metric;
import com.github.sgwhp.openapm.sample.tracing.ActivityTrace;
import com.github.sgwhp.openapm.sample.tracing.Trace;
import com.github.sgwhp.openapm.sample.tracing.TraceLifecycleAware;
import com.github.sgwhp.openapm.sample.tracing.TraceMachine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by user on 2016/8/2.
 */
public class SummaryMetricMeasurementConsumer extends MetricMeasurementConsumer implements TraceLifecycleAware {

    private static final String METRIC_PREFIX = "Mobile/Summary/";
    private static final String ACTIVITY_METRIC_PREFIX = "Mobile/Activity/Summary/Name/";
    private static final AgentLog log;
    private final List<ActivityTrace> completedTraces;

    public SummaryMetricMeasurementConsumer() {
        super(MeasurementType.Any);
        this.completedTraces = new CopyOnWriteArrayList<ActivityTrace>();
        this.recordUnscopedMetrics = false;
        TraceMachine.addTraceListener(this);
    }

    @Override
    public void consumeMeasurement(final Measurement measurement) {
        if (measurement == null) {
            return;
        }
        switch (measurement.getType()) {
            case Method: {
                this.consumeMethodMeasurement((MethodMeasurement)measurement);
                break;
            }
            case Network: {
                this.consumeNetworkMeasurement((HttpTransactionMeasurement)measurement);
                break;
            }
            case Custom: {
                this.consumeCustomMeasurement((CustomMetricMeasurement)measurement);
                break;
            }
        }
    }

    private void consumeMethodMeasurement(final MethodMeasurement methodMeasurement) {
        if (methodMeasurement.getCategory() == null || methodMeasurement.getCategory() == MetricCategory.NONE) {
            methodMeasurement.setCategory(MetricCategory.categoryForMethod(methodMeasurement.getName()));
            if (methodMeasurement.getCategory() == MetricCategory.NONE) {
                return;
            }
        }
        final BaseMeasurement summary = new BaseMeasurement(methodMeasurement);
        summary.setName(methodMeasurement.getCategory().getCategoryName());
        super.consumeMeasurement(summary);
    }

    private void consumeCustomMeasurement(final CustomMetricMeasurement customMetricMeasurement) {
        if (customMetricMeasurement.getCategory() == null || customMetricMeasurement.getCategory() == MetricCategory.NONE) {
            return;
        }
        final BaseMeasurement summary = new BaseMeasurement(customMetricMeasurement);
        summary.setName(customMetricMeasurement.getCategory().getCategoryName());
        super.consumeMeasurement(summary);
    }

    private void consumeNetworkMeasurement(final HttpTransactionMeasurement networkMeasurement) {
        final BaseMeasurement summary = new BaseMeasurement(networkMeasurement);
        summary.setName(MetricCategory.NETWORK.getCategoryName());
        super.consumeMeasurement(summary);
    }

    @Override
    protected String formatMetricName(final String name) {
        return "Mobile/Summary/" + name.replace("#", "/");
    }

    @Override
    public void onHarvest() {
        if (this.metrics.getAll().size() == 0) {
            return;
        }
        if (this.completedTraces.size() == 0) {
            return;
        }
        for (final ActivityTrace trace : this.completedTraces) {
            this.summarizeActivityMetrics(trace);
        }
        if (this.metrics.getAll().size() != 0) {
            SummaryMetricMeasurementConsumer.log.debug("Not all metrics were summarized!");
        }
        this.completedTraces.clear();
    }

    private void summarizeActivityNetworkMetrics(final ActivityTrace activityTrace) {
        final String activityName = activityTrace.getActivityName();
        if (activityTrace.networkCountMetric.getCount() > 0L) {
            final String name = activityTrace.networkCountMetric.getName();
            activityTrace.networkCountMetric.setName(name.replace("<activity>", activityName));
            activityTrace.networkCountMetric.setCount(1L);
            activityTrace.networkCountMetric.setMinFieldValue(activityTrace.networkCountMetric.getTotal());
            activityTrace.networkCountMetric.setMaxFieldValue(activityTrace.networkCountMetric.getTotal());
            Harvest.addMetric(activityTrace.networkCountMetric);
        }

        if (activityTrace.networkTimeMetric.getCount() > 0L) {
            final String name = activityTrace.networkTimeMetric.getName();
            activityTrace.networkTimeMetric.setName(name.replace("<activity>", activityName));
            activityTrace.networkTimeMetric.setCount(1L);
            activityTrace.networkTimeMetric.setMinFieldValue(activityTrace.networkTimeMetric.getTotal());
            activityTrace.networkTimeMetric.setMaxFieldValue(activityTrace.networkTimeMetric.getTotal());
            Harvest.addMetric(activityTrace.networkTimeMetric);
        }
    }

    private void summarizeActivityMetrics(final ActivityTrace activityTrace) {
        final Trace trace = activityTrace.rootTrace;
        final List<Metric> activityMetrics = this.metrics.removeAllWithScope(trace.metricName);
        final List<Metric> backgroundMetrics = this.metrics.removeAllWithScope(trace.metricBackgroundName);
        final Map<String, Metric> summaryMetrics = new HashMap<String, Metric>();
        for (final Metric activityMetric : activityMetrics) {
            summaryMetrics.put(activityMetric.getName(), activityMetric);
        }
        for (final Metric backgroundMetric : backgroundMetrics) {
            if (summaryMetrics.containsKey(backgroundMetric.getName())) {
                summaryMetrics.get(backgroundMetric.getName()).aggregate(backgroundMetric);
            }
            else {
                summaryMetrics.put(backgroundMetric.getName(), backgroundMetric);
            }
        }
        double totalExclusiveTime = 0.0;
        for (final Metric metric : summaryMetrics.values()) {
            totalExclusiveTime += metric.getExclusive();
        }
        final double traceTime = (trace.exitTimestamp - trace.entryTimestamp) / 1000.0;
        for (final Metric metric2 : summaryMetrics.values()) {
            double normalizedTime = 0.0;
            if (metric2.getExclusive() != 0.0 && totalExclusiveTime != 0.0) {
                normalizedTime = metric2.getExclusive() / totalExclusiveTime;
            }
            final double scaledTime = normalizedTime * traceTime;
            metric2.setTotal(scaledTime);
            metric2.setExclusive(scaledTime);
            metric2.setMinFieldValue(0.0);
            metric2.setMaxFieldValue(0.0);
            metric2.setSumOfSquares(0.0);
            metric2.setScope("Mobile/Activity/Summary/Name/" + trace.displayName);
            Harvest.addMetric(metric2);
            final Metric unScoped = new Metric(metric2);
            unScoped.setScope(null);
            Harvest.addMetric(unScoped);
        }
        this.summarizeActivityNetworkMetrics(activityTrace);
    }

    @Override
    public void onHarvestError() {
    }

    @Override
    public void onHarvestComplete() {
    }

    @Override
    public void onTraceStart(final ActivityTrace activityTrace) {
    }

    @Override
    public void onTraceComplete(final ActivityTrace activityTrace) {
        if (!this.completedTraces.contains(activityTrace)) {
            this.completedTraces.add(activityTrace);
        }
    }

    @Override
    public void onEnterMethod() {
    }

    @Override
    public void onExitMethod() {
    }

    static {
        log = AgentLogManager.getAgentLog();
    }
}
