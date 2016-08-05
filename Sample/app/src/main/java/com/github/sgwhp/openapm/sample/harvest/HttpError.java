package com.github.sgwhp.openapm.sample.harvest;

import com.github.sgwhp.openapm.sample.Agent;
import com.github.sgwhp.openapm.sample.FeatureFlag;
import com.github.sgwhp.openapm.sample.harvest.type.HarvestableArray;
import com.github.sgwhp.openapm.sample.harvest.type.HarvestableObject;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.measurement.http.HttpErrorMeasurement;
import com.github.sgwhp.openapm.sample.util.SafeJsonPrimitive;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;

/**
 * Created by user on 2016/8/2.
 */
public class HttpError extends HarvestableArray {
    private static final AgentLog log;
    private String url;
    private int httpStatusCode;
    private long count;
    private String responseBody;
    private String stackTrace;
    private Map<String, String> params;
    private String appData;
    private String digest;
    private Long timestamp;

    public HttpError() {
    }

    public HttpError(final String url, final int httpStatusCode, final String responseBody, final String stackTrace, final Map<String, String> params) {
        this.url = url;
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
        this.stackTrace = stackTrace;
        this.params = params;
        this.count = 1L;
        this.digest = this.computeHash();
    }

    public HttpError(final HttpErrorMeasurement m) {
        this(m.getUrl(), m.getHttpStatusCode(), m.getResponseBody(), m.getStackTrace(), m.getParams());
        this.setTimestamp(m.getStartTime());
    }

    @Override
    public JsonArray asJsonArray() {
        final int bodyLimit = Harvest.getHarvestConfiguration().getResponse_body_limit();
        final JsonArray array = new JsonArray();
        array.add(SafeJsonPrimitive.factory(this.url));
        array.add(SafeJsonPrimitive.factory(this.httpStatusCode));
        array.add(SafeJsonPrimitive.factory(this.count));
        String body = "";
        if (FeatureFlag.featureEnabled(FeatureFlag.HttpResponseBodyCapture)) {
            body = this.optional(this.responseBody);
            if (body.length() > bodyLimit) {
                HttpError.log.warning("HTTP Error response BODY is too large. Truncating to " + bodyLimit + " bytes.");
                body = body.substring(0, bodyLimit);
            }
        }
        else {
            HttpError.log.warning("not enabled");
        }
        array.add(SafeJsonPrimitive.factory(Agent.getEncoder().encode(body.getBytes())));
        array.add(SafeJsonPrimitive.factory(this.optional(this.stackTrace)));
        final JsonObject customParams = new JsonObject();
        if (this.params == null) {
            this.params = Collections.emptyMap();
        }
        customParams.add("custom_params", HarvestableObject.fromMap(this.params).asJson());
        array.add(customParams);
        array.add(SafeJsonPrimitive.factory(this.optional(this.appData)));
        return array;
    }

    public void incrementCount() {
        ++this.count;
    }

    public String getHash() {
        return this.digest;
    }

    public void digest() {
        this.digest = this.computeHash();
    }


    private String computeHash() {
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            HttpError.log.error("Unable to initialize SHA-1 hash algorithm");
            return null;
        }
        digester.update(this.url.getBytes());
        digester.update(ByteBuffer.allocate(8).putInt(this.httpStatusCode).array());
        if (this.stackTrace != null && this.stackTrace.length() > 0) {
            digester.update(this.stackTrace.getBytes());
        }
        return new String(digester.digest());
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setHttpStatusCode(final int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public void setCount(final long count) {
        this.count = count;
    }

    public void setResponseBody(final String responseBody) {
        this.responseBody = responseBody;
    }

    public void setStackTrace(final String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public void setParams(final Map<String, String> params) {
        this.params = params;
    }

    public void setAppData(final String appData) {
        this.appData = appData;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public Map<String, String> getParams() {
        return this.params;
    }

    public void setTimestamp(final Long timestamp) {
        this.timestamp = timestamp;
    }

    static {
        log = AgentLogManager.getAgentLog();
    }
}
