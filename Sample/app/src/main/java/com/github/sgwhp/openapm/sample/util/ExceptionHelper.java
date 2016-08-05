package com.github.sgwhp.openapm.sample.util;

import com.github.sgwhp.openapm.sample.harvest.AgentHealth;
import com.github.sgwhp.openapm.sample.harvest.AgentHealthException;
import com.github.sgwhp.openapm.sample.harvest.type.HarvestErrorCodes;

import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

/**
 * Created by user on 2016/8/1.
 */
public class ExceptionHelper implements HarvestErrorCodes {

    private static final AgentLog log = AgentLogManager.getAgentLog();

    public static int exceptionToErrorCode(final Exception e) {
        int errorCode = -1;
        ExceptionHelper.log.debug("ExceptionHelper: exception " + e.getClass().getName() + " to error code.");
        if (e instanceof ClientProtocolException) {
            errorCode = -1011;
        }
        else if (e instanceof UnknownHostException) {
            errorCode = -1006;
        }
        else if (e instanceof NoRouteToHostException) {
            errorCode = -1003;
        }
        else if (e instanceof PortUnreachableException) {
            errorCode = -1003;
        }
        else if (e instanceof SocketTimeoutException) {
            errorCode = -1001;
        }
        else if (e instanceof ConnectTimeoutException) {
            errorCode = -1001;
        }
        else if (e instanceof ConnectException) {
            errorCode = -1004;
        }
        else if (e instanceof MalformedURLException) {
            errorCode = -1000;
        }
        else if (e instanceof SSLException) {
            errorCode = -1200;
        }
        else if (e instanceof FileNotFoundException) {
            errorCode = -1100;
        }
        else if (e instanceof EOFException) {
            errorCode = -1021;
        }
        else if (e instanceof IOException) {
            recordSupportabilityMetric(e, "IOException");
        }
        else if (e instanceof RuntimeException) {
            recordSupportabilityMetric(e, "RuntimeException");
        }
        return errorCode;
    }

    public static void recordSupportabilityMetric(final Exception e, final String baseExceptionKey) {
        final AgentHealthException agentHealthException = new AgentHealthException(e);
        final StackTraceElement topTraceElement = agentHealthException.getStackTrace()[0];
        ExceptionHelper.log.error(String.format("ExceptionHelper: %s:%s(%s:%s) %s[%s] %s", agentHealthException.getSourceClass(),
                agentHealthException.getSourceMethod(), topTraceElement.getFileName(), topTraceElement.getLineNumber(),
                baseExceptionKey, agentHealthException.getExceptionClass(), agentHealthException.getMessage()));
        AgentHealth.noticeException(agentHealthException, baseExceptionKey);
    }

}
