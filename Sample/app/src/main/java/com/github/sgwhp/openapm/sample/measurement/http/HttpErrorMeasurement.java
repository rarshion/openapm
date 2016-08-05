package com.github.sgwhp.openapm.sample.measurement.http;

import com.github.sgwhp.openapm.sample.measurement.BaseMeasurement;
import com.github.sgwhp.openapm.sample.measurement.MeasurementType;

import java.util.Map;

/**
 * Created by user on 2016/8/2.
 */
public class HttpErrorMeasurement  extends BaseMeasurement {
    private String url;
    private int httpStatusCode;
    private String responseBody;
    private String stackTrace;
    private Map<String, String> params;

    public HttpErrorMeasurement(final String url, final int httpStatusCode) {
        super(MeasurementType.HttpError);
        this.setUrl(url);
        this.setName(url);
        this.setHttpStatusCode(httpStatusCode);
        this.setStartTime(System.currentTimeMillis());
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setHttpStatusCode(final int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
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

    public String getUrl() {
        return this.url;
    }

    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }

    public String getResponseBody() {
        return this.responseBody;
    }

    public String getStackTrace() {
        return this.stackTrace;
    }

    public Map<String, String> getParams() {
        return this.params;
    }
}
