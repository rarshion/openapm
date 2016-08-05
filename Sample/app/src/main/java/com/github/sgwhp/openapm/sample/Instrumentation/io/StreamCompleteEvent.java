package com.github.sgwhp.openapm.sample.Instrumentation.io;

import java.util.*;
/**
 * Created by user on 2016/8/1.
 */
public class StreamCompleteEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private final long bytes;
    private final Exception exception;

    public StreamCompleteEvent(final Object source, final long bytes, final Exception exception) {
        super(source);
        this.bytes = bytes;
        this.exception = exception;
    }

    public StreamCompleteEvent(final Object source, final long bytes) {
        this(source, bytes, null);
    }

    public long getBytes() {
        return this.bytes;
    }

    public Exception getException() {
        return this.exception;
    }

    public boolean isError() {
        return this.exception != null;
    }
}
