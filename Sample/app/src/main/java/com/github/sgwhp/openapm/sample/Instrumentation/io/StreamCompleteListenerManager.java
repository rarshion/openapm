package com.github.sgwhp.openapm.sample.Instrumentation.io;
import java.util.*;
/**
 * Created by user on 2016/8/1.
 */
public class StreamCompleteListenerManager {
    private boolean streamComplete;
    private ArrayList<StreamCompleteListener> streamCompleteListeners;

    StreamCompleteListenerManager() {
        this.streamComplete = false;
        this.streamCompleteListeners = new ArrayList<StreamCompleteListener>();
    }

    public boolean isComplete() {
        synchronized (this) {
            return this.streamComplete;
        }
    }

    public void addStreamCompleteListener(final StreamCompleteListener streamCompleteListener) {
        synchronized (this.streamCompleteListeners) {
            this.streamCompleteListeners.add(streamCompleteListener);
        }
    }

    public void removeStreamCompleteListener(final StreamCompleteListener streamCompleteListener) {
        synchronized (this.streamCompleteListeners) {
            this.streamCompleteListeners.remove(streamCompleteListener);
        }
    }

    public void notifyStreamComplete(final StreamCompleteEvent ev) {
        if (!this.checkComplete()) {
            for (final StreamCompleteListener listener : this.getStreamCompleteListeners()) {
                listener.streamComplete(ev);
            }
        }
    }

    public void notifyStreamError(final StreamCompleteEvent ev) {
        if (!this.checkComplete()) {
            for (final StreamCompleteListener listener : this.getStreamCompleteListeners()) {
                listener.streamError(ev);
            }
        }
    }

    private boolean checkComplete() {
        final boolean streamComplete;
        synchronized (this) {
            streamComplete = this.isComplete();
            if (!streamComplete) {
                this.streamComplete = true;
            }
        }
        return streamComplete;
    }

    private List<StreamCompleteListener> getStreamCompleteListeners() {
        final ArrayList<StreamCompleteListener> listeners;
        synchronized (this.streamCompleteListeners) {
            listeners = new ArrayList<StreamCompleteListener>(this.streamCompleteListeners);
            this.streamCompleteListeners.clear();
        }
        return listeners;
    }
}
