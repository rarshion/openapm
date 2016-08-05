package com.github.sgwhp.openapm.sample.metric;

/**
 * Created by user on 2016/8/2.
 */
public enum  MetricUnit {

    PERCENT("%"),
    BYTES("bytes"),
    SECONDS("sec"),
    BYTES_PER_SECOND("bytes/second"),
    OPERATIONS("op");


    private String label;

    private MetricUnit(final String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }
}
