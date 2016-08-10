package com.github.sgwhp.openapm.sample.measurement.http;

import com.github.sgwhp.openapm.sample.measurement.BaseMeasurement;
import com.github.sgwhp.openapm.sample.measurement.MeasurementType;

import java.util.Map;

/**
 * Created by user on 2016/8/2.
 */
//http请求错误测量
public class HttpErrorMeasurement  extends BaseMeasurement {
    private String url;//路径
    private int httpStatusCode;//状态码
    private String responseBody;//回应码
    private String stackTrace;//堆栈
    private Map<String, String> params;//参数

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
