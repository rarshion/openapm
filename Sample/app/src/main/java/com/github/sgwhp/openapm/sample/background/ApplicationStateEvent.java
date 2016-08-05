package com.github.sgwhp.openapm.sample.background;

import java.util.EventObject;

/**
 * Created by user on 2016/8/2.
 */
public class ApplicationStateEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    public ApplicationStateEvent(final Object source) {
        super(source);
    }
}
