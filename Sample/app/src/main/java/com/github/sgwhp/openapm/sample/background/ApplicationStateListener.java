package com.github.sgwhp.openapm.sample.background;

/**
 * Created by user on 2016/8/2.
 */
public interface ApplicationStateListener {
    void applicationForegrounded(ApplicationStateEvent p0);
    void applicationBackgrounded(ApplicationStateEvent p0);
}
