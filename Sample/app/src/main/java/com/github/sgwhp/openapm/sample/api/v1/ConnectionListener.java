package com.github.sgwhp.openapm.sample.api.v1;

/**
 * Created by user on 2016/8/1.
 */
public interface ConnectionListener {
    void connected(ConnectionEvent p0);
    void disconnected(ConnectionEvent p0);
}
