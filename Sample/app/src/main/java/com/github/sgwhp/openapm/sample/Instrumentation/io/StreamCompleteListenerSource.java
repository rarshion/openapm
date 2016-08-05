package com.github.sgwhp.openapm.sample.Instrumentation.io;

/**
 * Created by user on 2016/8/1.
 */
public interface StreamCompleteListenerSource {
    void addStreamCompleteListener(StreamCompleteListener p0);
    void removeStreamCompleteListener(StreamCompleteListener p0);
}
