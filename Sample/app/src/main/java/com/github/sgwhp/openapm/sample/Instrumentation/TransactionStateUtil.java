package com.github.sgwhp.openapm.sample.Instrumentation;

import com.github.sgwhp.openapm.sample.Agent;
import com.github.sgwhp.openapm.sample.Instrumentation.httpclient.ContentBufferingResponseEntityImpl;
import com.github.sgwhp.openapm.sample.Instrumentation.httpclient.HttpRequestEntityImpl;
import com.github.sgwhp.openapm.sample.Instrumentation.httpclient.HttpResponseEntityImpl;
import com.github.sgwhp.openapm.sample.Instrumentation.io.CountingInputStream;
import com.github.sgwhp.openapm.sample.Measurements;
import com.github.sgwhp.openapm.sample.TaskQueue;
import com.github.sgwhp.openapm.sample.api.common.TransactionData;
import com.github.sgwhp.openapm.sample.harvest.type.HarvestErrorCodes;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.measurement.http.HttpTransactionMeasurement;
import com.github.sgwhp.openapm.sample.tracing.TraceMachine;
import com.github.sgwhp.openapm.sample.util.ExceptionHelper;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by user on 2016/8/1.
 */
public class TransactionStateUtil implements HarvestErrorCodes {

    private static final AgentLog log;
    public static final String CONTENT_LENGTH_HEADER = "Content-Length";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String APP_DATA_HEADER = "X-NewRelic-App-Data";
    public static final String CROSS_PROCESS_ID_HEADER = "X-NewRelic-ID";

    public static void inspectAndInstrument(final TransactionState transactionState, final String url, final String httpMethod) {
        transactionState.setUrl(url);
        transactionState.setHttpMethod(httpMethod);
        transactionState.setCarrier(Agent.getActiveNetworkCarrier());
        transactionState.setWanType(Agent.getActiveNetworkWanType());
    }

    public static void inspectAndInstrument(final TransactionState transactionState, final HttpURLConnection conn) {
        inspectAndInstrument(transactionState, conn.getURL().toString(), conn.getRequestMethod());
    }

    public static void setCrossProcessHeader(final HttpURLConnection conn) {
        final String crossProcessId = Agent.getCrossProcessId();
        if (crossProcessId != null) {
            conn.setRequestProperty("X-NewRelic-ID", crossProcessId);
        }
    }

    public static void inspectAndInstrumentResponse(final TransactionState transactionState, final String appData, final int contentLength, final int statusCode) {
        if (appData != null && !appData.equals("")) {
            transactionState.setAppData(appData);
        }
        if (contentLength >= 0) {
            transactionState.setBytesReceived(contentLength);
        }
        transactionState.setStatusCode(statusCode);
    }

    public static void inspectAndInstrumentResponse(final TransactionState transactionState, final HttpURLConnection conn) {
        final String appData = conn.getHeaderField("X-NewRelic-App-Data");
        final int contentLength = conn.getContentLength();
        int statusCode = 0;
        try {
            statusCode = conn.getResponseCode();
        }
        catch (IOException e) {
            TransactionStateUtil.log.debug("Failed to retrieve response code due to an I/O exception: " + e.getMessage());
        }
        catch (NullPointerException e2) {
            TransactionStateUtil.log.error("Failed to retrieve response code due to underlying (Harmony?) NPE", e2);
        }
        inspectAndInstrumentResponse(transactionState, appData, contentLength, statusCode);
    }

    public static HttpRequest inspectAndInstrument(final TransactionState transactionState, final HttpHost host, final HttpRequest request) {
        addCrossProcessIdHeader(request);
        String url = null;
        final RequestLine requestLine = request.getRequestLine();
        if (requestLine != null) {
            final String uri = requestLine.getUri();
            final boolean isAbsoluteUri = uri != null && uri.length() >= 10 && uri.substring(0, 10).indexOf("://") >= 0;
            if (!isAbsoluteUri && uri != null && host != null) {
                final String prefix = host.toURI().toString();
                url = prefix + ((prefix.endsWith("/") || uri.startsWith("/")) ? "" : "/") + uri;
            }
            else if (isAbsoluteUri) {
                url = uri;
            }
            inspectAndInstrument(transactionState, url, requestLine.getMethod());
        }
        Label_0229: {
            if (transactionState.getUrl() != null) {
                if (transactionState.getHttpMethod() != null) {
                    break Label_0229;
                }
            }
            try {
                throw new Exception("TransactionData constructor was not provided with a valid URL, host or HTTP method");
            }
            catch (Exception e) {
                AgentLogManager.getAgentLog().error(MessageFormat.format("TransactionStateUtil.inspectAndInstrument(...) for {0} could not determine request URL or HTTP method [host={1}, requestLine={2}]", request.getClass().getCanonicalName(), host, requestLine), e);
                return request;
            }
        }
        wrapRequestEntity(transactionState, request);
        return request;
    }

    public static HttpUriRequest inspectAndInstrument(final TransactionState transactionState, final HttpUriRequest request) {
        addCrossProcessIdHeader((HttpRequest)request);
        inspectAndInstrument(transactionState, request.getURI().toString(), request.getMethod());
        wrapRequestEntity(transactionState, (HttpRequest)request);
        return request;
    }

    private static void addCrossProcessIdHeader(final HttpRequest request) {
        final String crossProcessId = Agent.getCrossProcessId();
        if (crossProcessId != null) {
            TraceMachine.setCurrentTraceParam("cross_process_data", crossProcessId);
            request.setHeader("X-NewRelic-ID", crossProcessId);
        }
    }

    private static void wrapRequestEntity(final TransactionState transactionState, final HttpRequest request) {
        if (request instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest entityEnclosingRequest = (HttpEntityEnclosingRequest)request;
            if (entityEnclosingRequest.getEntity() != null) {
                entityEnclosingRequest.setEntity((HttpEntity)new HttpRequestEntityImpl(entityEnclosingRequest.getEntity(), transactionState));
            }
        }
    }

    public static HttpResponse inspectAndInstrument(final TransactionState transactionState, final HttpResponse response) {
        transactionState.setStatusCode(response.getStatusLine().getStatusCode());
        final Header[] appDataHeader = response.getHeaders("X-NewRelic-App-Data");
        if (appDataHeader != null && appDataHeader.length > 0 && !"".equals(appDataHeader[0].getValue())) {
            transactionState.setAppData(appDataHeader[0].getValue());
        }
        final Header[] contentLengthHeader = response.getHeaders("Content-Length");
        long contentLengthFromHeader = -1L;
        if (contentLengthHeader != null && contentLengthHeader.length > 0) {
            try {
                contentLengthFromHeader = Long.parseLong(contentLengthHeader[0].getValue());
                transactionState.setBytesReceived(contentLengthFromHeader);
                addTransactionAndErrorData(transactionState, response);
            }
            catch (NumberFormatException e) {
                TransactionStateUtil.log.warning("Failed to parse content length: " + e.toString());
            }
        }
        else if (response.getEntity() != null) {
            response.setEntity((HttpEntity)new HttpResponseEntityImpl(response.getEntity(), transactionState, contentLengthFromHeader));
        }
        else {
            transactionState.setBytesReceived(0L);
            addTransactionAndErrorData(transactionState, null);
        }
        return response;
    }

    public static void setErrorCodeFromException(final TransactionState transactionState, final Exception e) {
        final int exceptionAsErrorCode = ExceptionHelper.exceptionToErrorCode(e);
        TransactionStateUtil.log.error("TransactionStateUtil: Attempting to convert network exception " + e.getClass().getName() + " to error code.");
        transactionState.setErrorCode(exceptionAsErrorCode);
    }

    private static void addTransactionAndErrorData(final TransactionState transactionState, final HttpResponse response) {
        final TransactionData transactionData = transactionState.end();
        if (transactionData == null) {
            return;
        }
        TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
        if (transactionState.getStatusCode() >= 400L) {
            final StringBuilder responseBody = new StringBuilder();
            final Map<String, String> params = new TreeMap<String, String>();
            if (response != null) {
                try {
                    if (response.getEntity() != null) {
                        if (!(response.getEntity() instanceof HttpRequestEntityImpl)) {
                            response.setEntity((HttpEntity)new ContentBufferingResponseEntityImpl(response.getEntity()));
                        }
                        final InputStream content = response.getEntity().getContent();
                        if (content instanceof CountingInputStream) {
                            responseBody.append(((CountingInputStream)content).getBufferAsString());
                        }
                        else {
                            TransactionStateUtil.log.error("Unable to wrap content stream for entity");
                        }
                    }
                    else {
                        TransactionStateUtil.log.debug("null response entity. response-body will be reported empty");
                    }
                }
                catch (IllegalStateException e) {
                    TransactionStateUtil.log.error(e.toString());
                }
                catch (IOException e2) {
                    TransactionStateUtil.log.error(e2.toString());
                }
                final Header[] contentTypeHeader = response.getHeaders("Content-Type");
                String contentType = null;
                if (contentTypeHeader != null && contentTypeHeader.length > 0 && !"".equals(contentTypeHeader[0].getValue())) {
                    contentType = contentTypeHeader[0].getValue();
                }
                if (contentType != null && contentType.length() > 0) {
                    params.put("content_type", contentType);
                }
            }
            params.put("content_length", transactionState.getBytesReceived() + "");
            Measurements.addHttpError(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), responseBody.toString(), params);
        }
    }

    static {
        log = AgentLogManager.getAgentLog();
    }
}
