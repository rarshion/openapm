package com.github.sgwhp.openapm.sample.harvest;

import com.github.sgwhp.openapm.sample.metric.Metric;
import com.github.sgwhp.openapm.sample.tracing.ActivityTrace;

import java.util.List;

/**
 * Created by user on 2016/8/2.
 */
public class HarvestDataValidator extends HarvestAdapter {

    @Override
    public void onHarvestFinalize() {
        if (!Harvest.isInitialized()) {
            return;
        }
        this.ensureActivityNameMetricsExist();
    }

    public void ensureActivityNameMetricsExist() {

        final HarvestData harvestData = Harvest.getInstance().getHarvestData();
        final ActivityTraces activityTraces = harvestData.getActivityTraces();


        if (activityTraces == null || activityTraces.count() == 0) {
            return;
        }
        final MachineMeasurements metrics = harvestData.getMetrics();
        if (metrics == null || metrics.isEmpty()) {
            return;
        }
        for (final ActivityTrace activityTrace : activityTraces.getActivityTraces()) {
            String activityName = activityTrace.rootTrace.displayName;
            final int hashIndex = activityName.indexOf("#");
            if (hashIndex > 0) {
                activityName = activityName.substring(0, hashIndex);
            }
            final String activityMetricRoot = "Mobile/Activity/Name/" + activityName;
            boolean foundMetricForActivity = false;
            final List<Metric> unScopedMetrics = metrics.getMetrics().getAllUnscoped();
            if (unScopedMetrics != null && unScopedMetrics.size() > 0) {
                for (final Metric metric : unScopedMetrics) {
                    if (metric.getName().startsWith(activityMetricRoot)) {
                        foundMetricForActivity = true;
                        break;
                    }
                }
            }
            if (foundMetricForActivity) {
                continue;
            }
            final Metric activityMetric = new Metric(activityMetricRoot);
            metrics.addMetric(activityMetric);
        }
    }
}
