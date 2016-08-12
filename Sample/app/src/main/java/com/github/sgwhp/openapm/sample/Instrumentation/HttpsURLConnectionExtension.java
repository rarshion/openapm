package com.github.sgwhp.openapm.sample.Instrumentation;

/**
 * Created by rarshion on 16/8/11.
 */
import com.github.sgwhp.openapm.sample.Instrumentation.io.CountingInputStream;
import com.github.sgwhp.openapm.sample.Instrumentation.io.CountingOutputStream;
import com.github.sgwhp.openapm.sample.Instrumentation.io.StreamCompleteEvent;
import com.github.sgwhp.openapm.sample.Instrumentation.io.StreamCompleteListener;
import com.github.sgwhp.openapm.sample.Measurements;
import com.github.sgwhp.openapm.sample.TaskQueue;
import com.github.sgwhp.openapm.sample.api.common.TransactionData;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.measurement.http.HttpTransactionMeasurement;

import java.io.*;
import java.net.*;
import java.security.Permission;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;


public class HttpsURLConnectionExtension extends HttpsURLConnection
{
    private HttpsURLConnection impl;
    private TransactionState transactionState;
    private static final AgentLog log = AgentLogManager.getAgentLog();

    public HttpsURLConnectionExtension(final HttpsURLConnection impl) {
        super(impl.getURL());
        TransactionStateUtil.setCrossProcessHeader(this.impl = impl);
    }

    @Override
    public String getCipherSuite() {
        return this.impl.getCipherSuite();
    }

    @Override
    public Certificate[] getLocalCertificates() {
        return this.impl.getLocalCertificates();
    }

    @Override
    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        try {
            return this.impl.getServerCertificates();
        }
        catch (SSLPeerUnverifiedException e) {
            this.error(e);
            throw e;
        }
    }

    @Override
    public void addRequestProperty(final String field, final String newValue) {
        this.impl.addRequestProperty(field, newValue);
    }

    @Override
    public void disconnect() {
        if (this.transactionState != null && !this.transactionState.isComplete()) {
            this.addTransactionAndErrorData(this.transactionState);
        }
        this.impl.disconnect();
    }

    @Override
    public boolean usingProxy() {
        return this.impl.usingProxy();
    }

    @Override
    public void connect() throws IOException {
        this.getTransactionState();
        try {
            this.impl.connect();
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
    }

    @Override
    public boolean getAllowUserInteraction() {
        return this.impl.getAllowUserInteraction();
    }

    @Override
    public int getConnectTimeout() {
        return this.impl.getConnectTimeout();
    }

    @Override
    public Object getContent() throws IOException {
        this.getTransactionState();
        Object object;
        try {
            object = this.impl.getContent();
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
        final int contentLength = this.impl.getContentLength();
        if (contentLength >= 0) {
            final TransactionState transactionState = this.getTransactionState();
            if (!transactionState.isComplete()) {
                transactionState.setBytesReceived(contentLength);
                this.addTransactionAndErrorData(transactionState);
            }
        }
        return object;
    }

    @Override
    public Object getContent(final Class[] types) throws IOException {
        this.getTransactionState();
        Object object;
        try {
            object = this.impl.getContent(types);
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
        this.checkResponse();
        return object;
    }

    @Override
    public String getContentEncoding() {
        this.getTransactionState();
        final String contentEncoding = this.impl.getContentEncoding();
        this.checkResponse();
        return contentEncoding;
    }

    @Override
    public int getContentLength() {
        this.getTransactionState();
        final int contentLength = this.impl.getContentLength();
        this.checkResponse();
        return contentLength;
    }

    @Override
    public String getContentType() {
        this.getTransactionState();
        final String contentType = this.impl.getContentType();
        this.checkResponse();
        return contentType;
    }

    @Override
    public long getDate() {
        this.getTransactionState();
        final long date = this.impl.getDate();
        this.checkResponse();
        return date;
    }

    @Override
    public InputStream getErrorStream() {
        this.getTransactionState();
        CountingInputStream in;
        try {
            in = new CountingInputStream(this.impl.getErrorStream(), true);
        }
        catch (Exception e) {
            HttpsURLConnectionExtension.log.error(e.toString());
            return this.impl.getErrorStream();
        }
        return in;
    }

    @Override
    public long getHeaderFieldDate(final String field, final long defaultValue) {
        this.getTransactionState();
        final long date = this.impl.getHeaderFieldDate(field, defaultValue);
        this.checkResponse();
        return date;
    }

    @Override
    public boolean getInstanceFollowRedirects() {
        return this.impl.getInstanceFollowRedirects();
    }

    @Override
    public Permission getPermission() throws IOException {
        return this.impl.getPermission();
    }

    @Override
    public String getRequestMethod() {
        return this.impl.getRequestMethod();
    }

    @Override
    public int getResponseCode() throws IOException {
        this.getTransactionState();
        int responseCode;
        try {
            responseCode = this.impl.getResponseCode();
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
        this.checkResponse();
        return responseCode;
    }

    @Override
    public String getResponseMessage() throws IOException {
        this.getTransactionState();
        String message;
        try {
            message = this.impl.getResponseMessage();
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
        this.checkResponse();
        return message;
    }

    @Override
    public void setChunkedStreamingMode(final int chunkLength) {
        this.impl.setChunkedStreamingMode(chunkLength);
    }

    @Override
    public void setFixedLengthStreamingMode(final int contentLength) {
        this.impl.setFixedLengthStreamingMode(contentLength);
    }

    @Override
    public void setInstanceFollowRedirects(final boolean followRedirects) {
        this.impl.setInstanceFollowRedirects(followRedirects);
    }

    @Override
    public void setRequestMethod(final String method) throws ProtocolException {
        try {
            this.impl.setRequestMethod(method);
        }
        catch (ProtocolException e) {
            this.error(e);
            throw e;
        }
    }

    @Override
    public boolean getDefaultUseCaches() {
        return this.impl.getDefaultUseCaches();
    }

    @Override
    public boolean getDoInput() {
        return this.impl.getDoInput();
    }

    @Override
    public boolean getDoOutput() {
        return this.impl.getDoOutput();
    }

    @Override
    public long getExpiration() {
        this.getTransactionState();
        final long expiration = this.impl.getExpiration();
        this.checkResponse();
        return expiration;
    }

    @Override
    public String getHeaderField(final int pos) {
        this.getTransactionState();
        final String header = this.impl.getHeaderField(pos);
        this.checkResponse();
        return header;
    }

    @Override
    public String getHeaderField(final String key) {
        this.getTransactionState();
        final String header = this.impl.getHeaderField(key);
        this.checkResponse();
        return header;
    }

    @Override
    public int getHeaderFieldInt(final String field, final int defaultValue) {
        this.getTransactionState();
        final int header = this.impl.getHeaderFieldInt(field, defaultValue);
        this.checkResponse();
        return header;
    }

    @Override
    public String getHeaderFieldKey(final int posn) {
        this.getTransactionState();
        final String key = this.impl.getHeaderFieldKey(posn);
        this.checkResponse();
        return key;
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        this.getTransactionState();
        final Map<String, List<String>> fields = this.impl.getHeaderFields();
        this.checkResponse();
        return fields;
    }

    @Override
    public long getIfModifiedSince() {
        this.getTransactionState();
        final long ifModifiedSince = this.impl.getIfModifiedSince();
        this.checkResponse();
        return ifModifiedSince;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        final TransactionState transactionState = this.getTransactionState();
        CountingInputStream in;
        try {
            in = new CountingInputStream(this.impl.getInputStream());
            TransactionStateUtil.inspectAndInstrumentResponse(transactionState, this.impl);
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
        in.addStreamCompleteListener(new StreamCompleteListener() {
            @Override
            public void streamError(final StreamCompleteEvent e) {
                if (!transactionState.isComplete()) {
                    transactionState.setBytesReceived(e.getBytes());
                }
                HttpsURLConnectionExtension.this.error(e.getException());
            }

            @Override
            public void streamComplete(final StreamCompleteEvent e) {
                if (!transactionState.isComplete()) {
                    final long contentLength = HttpsURLConnectionExtension.this.impl.getContentLength();
                    long numBytes = e.getBytes();
                    if (contentLength >= 0L) {
                        numBytes = contentLength;
                    }
                    transactionState.setBytesReceived(numBytes);
                    HttpsURLConnectionExtension.this.addTransactionAndErrorData(transactionState);
                }
            }
        });
        return in;
    }

    @Override
    public long getLastModified() {
        this.getTransactionState();
        final long lastModified = this.impl.getLastModified();
        this.checkResponse();
        return lastModified;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        final TransactionState transactionState = this.getTransactionState();
        CountingOutputStream out;
        try {
            out = new CountingOutputStream(this.impl.getOutputStream());
        }
        catch (IOException e) {
            this.error(e);
            throw e;
        }
        out.addStreamCompleteListener(new StreamCompleteListener() {
            @Override
            public void streamError(final StreamCompleteEvent e) {
                if (!transactionState.isComplete()) {
                    transactionState.setBytesSent(e.getBytes());
                }
                HttpsURLConnectionExtension.this.error(e.getException());
            }

            @Override
            public void streamComplete(final StreamCompleteEvent e) {
                if (!transactionState.isComplete()) {
                    final String header = HttpsURLConnectionExtension.this.impl.getRequestProperty("content-length");
                    long numBytes = e.getBytes();
                    if (header != null) {
                        try {
                            numBytes = Long.parseLong(header);
                        }
                        catch (NumberFormatException ex) {}
                    }
                    transactionState.setBytesSent(numBytes);
                    HttpsURLConnectionExtension.this.addTransactionAndErrorData(transactionState);
                }
            }
        });
        return out;
    }

    @Override
    public int getReadTimeout() {
        return this.impl.getReadTimeout();
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        return this.impl.getRequestProperties();
    }

    @Override
    public String getRequestProperty(final String field) {
        return this.impl.getRequestProperty(field);
    }

    @Override
    public URL getURL() {
        return this.impl.getURL();
    }

    @Override
    public boolean getUseCaches() {
        return this.impl.getUseCaches();
    }

    @Override
    public void setAllowUserInteraction(final boolean newValue) {
        this.impl.setAllowUserInteraction(newValue);
    }

    @Override
    public void setConnectTimeout(final int timeoutMillis) {
        this.impl.setConnectTimeout(timeoutMillis);
    }

    @Override
    public void setDefaultUseCaches(final boolean newValue) {
        this.impl.setDefaultUseCaches(newValue);
    }

    @Override
    public void setDoInput(final boolean newValue) {
        this.impl.setDoInput(newValue);
    }

    @Override
    public void setDoOutput(final boolean newValue) {
        this.impl.setDoOutput(newValue);
    }

    @Override
    public void setIfModifiedSince(final long newValue) {
        this.impl.setIfModifiedSince(newValue);
    }

    @Override
    public void setReadTimeout(final int timeoutMillis) {
        this.impl.setReadTimeout(timeoutMillis);
    }

    @Override
    public void setRequestProperty(final String field, final String newValue) {
        this.impl.setRequestProperty(field, newValue);
    }

    @Override
    public void setUseCaches(final boolean newValue) {
        this.impl.setUseCaches(newValue);
    }

    @Override
    public String toString() {
        return this.impl.toString();
    }

    @Override
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        return this.impl.getPeerPrincipal();
    }

    @Override
    public Principal getLocalPrincipal() {
        return this.impl.getLocalPrincipal();
    }

    @Override
    public void setHostnameVerifier(final HostnameVerifier hostnameVerifier) {
        this.impl.setHostnameVerifier(hostnameVerifier);
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return this.impl.getHostnameVerifier();
    }

    @Override
    public void setSSLSocketFactory(final SSLSocketFactory sf) {
        this.impl.setSSLSocketFactory(sf);
    }

    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return this.impl.getSSLSocketFactory();
    }

    private void checkResponse() {
        if (!this.getTransactionState().isComplete()) {
            TransactionStateUtil.inspectAndInstrumentResponse(this.getTransactionState(), this.impl);
        }
    }

    private TransactionState getTransactionState() {
        if (this.transactionState == null) {
            TransactionStateUtil.inspectAndInstrument(this.transactionState = new TransactionState(), this.impl);
        }
        return this.transactionState;
    }

    private void error(final Exception e) {
        final TransactionState transactionState = this.getTransactionState();
        TransactionStateUtil.setErrorCodeFromException(transactionState, e);
        if (!transactionState.isComplete()) {
            TransactionStateUtil.inspectAndInstrumentResponse(transactionState, this.impl);
            final TransactionData transactionData = transactionState.end();
            if (transactionData != null) {
                TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
            }
        }
    }

    private void addTransactionAndErrorData(final TransactionState transactionState) {
        final TransactionData transactionData = transactionState.end();
        if (transactionData == null) {
            return;
        }
        TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
        if (transactionState.getStatusCode() >= 400L) {
            final StringBuilder responseBody = new StringBuilder();
            try {
                final InputStream errorStream = this.getErrorStream();
                if (errorStream instanceof CountingInputStream) {
                    responseBody.append(((CountingInputStream)errorStream).getBufferAsString());
                }
            }
            catch (Exception e) {
                HttpsURLConnectionExtension.log.error(e.toString());
            }
            final Map<String, String> params = new TreeMap<String, String>();
            final String contentType = this.impl.getContentType();
            if (contentType != null && !"".equals(contentType)) {
                params.put("content_type", contentType);
            }
            params.put("content_length", transactionState.getBytesReceived() + "");
            Measurements.addHttpError(transactionData, responseBody.toString(), params);
        }
    }

}