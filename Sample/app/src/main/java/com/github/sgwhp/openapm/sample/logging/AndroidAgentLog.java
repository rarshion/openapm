package com.github.sgwhp.openapm.sample.logging;

import android.util.Log;

/**
 * Created by user on 2016/8/1.
 */
public class AndroidAgentLog implements  AgentLog{

    private static final String TAG = "com.newrelic.agent.android";
    private int level;

    public AndroidAgentLog() {
        this.level = 3;
    }

    public void debug(final String message) {
        if (this.level == 5) {
            Log.d("com.newrelic.agent.android", message);
        }
    }

    public void verbose(final String message) {
        if (this.level >= 4) {
            Log.v("com.newrelic.agent.android", message);
        }
    }

    public void info(final String message) {
        if (this.level >= 3) {
            Log.i("com.newrelic.agent.android", message);
        }
    }

    public void warning(final String message) {
        if (this.level >= 2) {
            Log.w("com.newrelic.agent.android", message);
        }
    }

    public void error(final String message) {
        if (this.level >= 1) {
            Log.e("com.newrelic.agent.android", message);
        }
    }

    public void error(final String message, final Throwable cause) {
        if (this.level >= 1) {
            Log.e("com.newrelic.agent.android", message, cause);
        }
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(final int level) {
        if (level <= 5 && level >= 1) {
            this.level = level;
            return;
        }
        throw new IllegalArgumentException("Log level is not between ERROR and DEBUG");
    }
}
