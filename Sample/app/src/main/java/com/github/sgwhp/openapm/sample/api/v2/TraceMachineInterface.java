package com.github.sgwhp.openapm.sample.api.v2;

/**
 * Created by user on 2016/8/1.
 */
public interface TraceMachineInterface
{
    long getCurrentThreadId();
    String getCurrentThreadName();
    boolean isUIThread();
}
