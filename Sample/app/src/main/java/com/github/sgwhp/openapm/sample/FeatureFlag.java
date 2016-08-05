package com.github.sgwhp.openapm.sample;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by user on 2016/8/2.
 */
public enum FeatureFlag {
    HttpResponseBodyCapture,
    CrashReporting,
    AnalyticsEvents,
    InteractionTracing,
    DefaultInteractions;

    public static final Set<FeatureFlag> enabledFeatures;

    public static void enableFeature(final FeatureFlag featureFlag) {
        FeatureFlag.enabledFeatures.add(featureFlag);
    }

    public static void disableFeature(final FeatureFlag featureFlag) {
        FeatureFlag.enabledFeatures.remove(featureFlag);
    }

    public static boolean featureEnabled(final FeatureFlag featureFlag) {
        return FeatureFlag.enabledFeatures.contains(featureFlag);
    }

    static {
        enabledFeatures = new HashSet<FeatureFlag>();
        enableFeature(FeatureFlag.HttpResponseBodyCapture);
        enableFeature(FeatureFlag.CrashReporting);
        enableFeature(FeatureFlag.AnalyticsEvents);
        enableFeature(FeatureFlag.InteractionTracing);
        enableFeature(FeatureFlag.DefaultInteractions);
    }
}
