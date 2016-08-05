package com.github.sgwhp.openapm.sample.api.v1;

import com.github.sgwhp.openapm.sample.api.common.ConnectionState;
import java.util.*;

/**
 * Created by user on 2016/8/1.
 */

public final class ConnectionEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private final ConnectionState connectionState;

    public ConnectionEvent(final Object source) {
        this(source, null);
    }

    public ConnectionEvent(final Object source, final ConnectionState connectionState) {
        super(source);
        this.connectionState = connectionState;
    }

    public ConnectionState getConnectionState() {
        return this.connectionState;
    }
}
