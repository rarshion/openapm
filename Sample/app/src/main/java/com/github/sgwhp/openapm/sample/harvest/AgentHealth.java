package com.github.sgwhp.openapm.sample.harvest;

import com.github.sgwhp.openapm.sample.TaskQueue;
import com.github.sgwhp.openapm.sample.harvest.type.HarvestableArray;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.stats.StatsEngine;
import com.google.gson.JsonArray;

import java.text.MessageFormat;

/**
 * Created by user on 2016/8/2.
 */
public class AgentHealth extends HarvestableArray {

    private static final AgentLog log;
    public static final String DEFAULT_KEY = "Exception";
    protected final AgentHealthExceptions agentHealthExceptions;

    public AgentHealth() {
        this.agentHealthExceptions = new AgentHealthExceptions();
    }

    public static void noticeException(final Exception exception) {
        AgentHealthException agentHealthException = null;
        if (exception != null) {
            agentHealthException = new AgentHealthException(exception);
        }
        noticeException(agentHealthException);
    }

    public static void noticeException(final AgentHealthException exception) {
        noticeException(exception, "Exception");
    }

    public static void noticeException(final AgentHealthException exception, final String key) {
        if (exception != null) {
            final StatsEngine statsEngine = StatsEngine.get();
            if (statsEngine != null) {
                if (key == null) {
                    AgentHealth.log.warning("Passed metric key is null. Defaulting to Exception");
                }
                statsEngine.inc(MessageFormat.format("Supportability/AgentHealth/{0}/{1}/{2}/{3}", (key == null) ? "Exception" : key, exception.getSourceClass(), exception.getSourceMethod(), exception.getExceptionClass()));
                TaskQueue.queue(exception);
            }
            else {
                AgentHealth.log.error("StatsEngine is null. Exception not recorded.");
            }
        }
        else {
            AgentHealth.log.error("AgentHealthException is null. StatsEngine not updated");
        }
    }

    public void addException(final AgentHealthException exception) {
        this.agentHealthExceptions.add(exception);
    }

    public void clear() {
        this.agentHealthExceptions.clear();
    }

    @Override
    public JsonArray asJsonArray() {
        final JsonArray data = new JsonArray();
        if (!this.agentHealthExceptions.isEmpty()) {
            data.add(this.agentHealthExceptions.asJsonObject());
        }
        return data;
    }

    static {
        log = AgentLogManager.getAgentLog();
    }

}
