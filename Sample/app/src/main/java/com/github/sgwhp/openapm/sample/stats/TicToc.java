package com.github.sgwhp.openapm.sample.stats;

/**
 * Created by user on 2016/8/2.
 */
public class TicToc {

    private long startTime;
    private long endTime;
    private State state;

    public void tic() {
        this.state = State.STARTED;
        this.startTime = System.currentTimeMillis();
    }

    public long toc() {
        this.endTime = System.currentTimeMillis();
        if (this.state == State.STARTED) {
            this.state = State.STOPPED;
            return this.endTime - this.startTime;
        }
        return -1L;
    }

    private enum State
    {
        STOPPED,
        STARTED;
    }
}
