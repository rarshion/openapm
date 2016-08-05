package com.github.sgwhp.openapm.sample.Instrumentation.httpclient;

import com.github.sgwhp.openapm.sample.Instrumentation.TransactionState;
import com.github.sgwhp.openapm.sample.Instrumentation.TransactionStateUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;

/**
 * Created by user on 2016/8/2.
 */
public final class ResponseHandlerImpl<T> implements ResponseHandler<T>
{
    private final ResponseHandler<T> impl;
    private final TransactionState transactionState;

    private ResponseHandlerImpl(final ResponseHandler<T> impl, final TransactionState transactionState) {
        this.impl = impl;
        this.transactionState = transactionState;
    }

    public T handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        TransactionStateUtil.inspectAndInstrument(this.transactionState, response);
        return (T)this.impl.handleResponse(response);
    }

    public static <T> ResponseHandler<? extends T> wrap(final ResponseHandler<? extends T> impl, final TransactionState transactionState) {
        return (ResponseHandler<? extends T>)new ResponseHandlerImpl((ResponseHandler<Object>)impl, transactionState);
    }

}

