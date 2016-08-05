package com.github.sgwhp.openapm.sample.Instrumentation.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by user on 2016/8/1.
 */
public class CountingOutputStream extends OutputStream implements StreamCompleteListenerSource {
    private final OutputStream impl;
    private long count;
    private final StreamCompleteListenerManager listenerManager;

    public CountingOutputStream(final OutputStream impl) {
        this.count = 0L;
        this.listenerManager = new StreamCompleteListenerManager();
        this.impl = impl;
    }

    @Override
    public void addStreamCompleteListener(final StreamCompleteListener streamCompleteListener) {
        this.listenerManager.addStreamCompleteListener(streamCompleteListener);
    }

    @Override
    public void removeStreamCompleteListener(final StreamCompleteListener streamCompleteListener) {
        this.listenerManager.removeStreamCompleteListener(streamCompleteListener);
    }

    public long getCount() {
        return this.count;
    }

    @Override
    public void write(final int oneByte) throws IOException {
        try {
            this.impl.write(oneByte);
            ++this.count;
        }
        catch (IOException e) {
            this.notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public void write(final byte[] buffer) throws IOException {
        try {
            this.impl.write(buffer);
            this.count += buffer.length;
        }
        catch (IOException e) {
            this.notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int count) throws IOException {
        try {
            this.impl.write(buffer, offset, count);
            this.count += count;
        }
        catch (IOException e) {
            this.notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            this.impl.flush();
        }
        catch (IOException e) {
            this.notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.impl.close();
            this.notifyStreamComplete();
        }
        catch (IOException e) {
            this.notifyStreamError(e);
            throw e;
        }
    }

    private void notifyStreamComplete() {
        if (!this.listenerManager.isComplete()) {
            this.listenerManager.notifyStreamComplete(new StreamCompleteEvent(this, this.count));
        }
    }

    private void notifyStreamError(final Exception e) {
        if (!this.listenerManager.isComplete()) {
            this.listenerManager.notifyStreamError(new StreamCompleteEvent(this, this.count, e));
        }
    }
}
