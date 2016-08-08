package com.github.sgwhp.openapm.sample;

/**
 * Created by user on 2016/8/8.
 */
public class AgentInitializationException extends Exception
{
    private static final long serialVersionUID = 2725421917845262499L;

    public AgentInitializationException(final String message) {
        super(message);
    }
}
