package com.github.sgwhp.openapm.sample.Instrumentation.io;

/**
 * Created by user on 2016/8/1.
 */
public interface StreamCompleteListener {
    void streamComplete(StreamCompleteEvent p0);
    void streamError(StreamCompleteEvent p0);
}
