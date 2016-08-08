package com.github.sgwhp.openapm.sample.util;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;

import com.github.sgwhp.openapm.sample.background.ApplicationStateMonitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by user on 2016/8/8.
 */
public class UiBackgroundListener implements ComponentCallbacks2 {

    protected final ScheduledExecutorService executor;

    public UiBackgroundListener() {
        this.executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("UiBackgroundListener"));
    }

    public void onConfigurationChanged(final Configuration newConfig) {
    }

    public void onLowMemory() {
    }

    public void onTrimMemory(final int level) {
        switch (level) {
            case 20: {
                final Runnable runner = new Runnable() {
                    public void run() {
                        ApplicationStateMonitor.getInstance().uiHidden();
                    }
                };
                this.executor.submit(runner);
                break;
            }
        }
    }
}
