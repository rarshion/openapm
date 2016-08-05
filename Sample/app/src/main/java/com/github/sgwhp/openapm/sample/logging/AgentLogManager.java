package com.github.sgwhp.openapm.sample.logging;

/**
 * Created by user on 2016/8/1.
 */
public class AgentLogManager {

    private static DefaultAgentLog instance = new DefaultAgentLog();

    public static AgentLog getAgentLog() {
        return instance;
    }

    public static void setAgentLog(final AgentLog instance) {
        AgentLogManager.instance.setImpl(instance);
    }
}
