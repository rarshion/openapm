package com.github.sgwhp.openapm.sample.analytics;

/**
 * Created by user on 2016/8/2.
 */
public enum AnalyticsEventCategory {

    Session,
    Interaction,
    Crash,
    Custom;

    public static AnalyticsEventCategory fromString(final String categoryString) {
        AnalyticsEventCategory category = AnalyticsEventCategory.Custom;
        if (categoryString != null) {
            if (categoryString.equalsIgnoreCase("session")) {
                category = AnalyticsEventCategory.Session;
            }
            else if (categoryString.equalsIgnoreCase("interaction")) {
                category = AnalyticsEventCategory.Interaction;
            }
            else if (categoryString.equalsIgnoreCase("crash")) {
                category = AnalyticsEventCategory.Crash;
            }
        }
        return category;
    }
}
