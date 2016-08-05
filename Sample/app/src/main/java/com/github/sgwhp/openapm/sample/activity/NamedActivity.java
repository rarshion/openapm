package com.github.sgwhp.openapm.sample.activity;

/**
 * Created by user on 2016/8/1.
 */
public class NamedActivity extends BaseMeasuredActivity {
    public NamedActivity(final String activityName) {
        this.setName(activityName);
        this.setAutoInstrumented(false);
    }

    public void rename(final String activityName) {
        this.setName(activityName);
    }
}
