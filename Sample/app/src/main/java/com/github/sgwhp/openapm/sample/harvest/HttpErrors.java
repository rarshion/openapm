package com.github.sgwhp.openapm.sample.harvest;

import com.github.sgwhp.openapm.sample.harvest.type.HarvestableArray;
import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by user on 2016/8/2.
 */
public class HttpErrors extends HarvestableArray {

    private final Collection<HttpError> httpErrors;

    public HttpErrors() {
        this.httpErrors = new CopyOnWriteArrayList<HttpError>();
    }

    public void addHttpError(final HttpError httpError) {
        synchronized (httpError) {
            for (final HttpError error : this.httpErrors) {
                if (httpError.getHash().equals(error.getHash())) {
                    error.incrementCount();
                    return;
                }
            }
            this.httpErrors.add(httpError);
        }
    }

    public synchronized void removeHttpError(final HttpError error) {
        this.httpErrors.remove(error);
    }

    public void clear() {
        this.httpErrors.clear();
    }

    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        for (final HttpError httpError : this.httpErrors) {
            array.add(httpError.asJson());
        }
        return array;
    }

    public Collection<HttpError> getHttpErrors() {
        return this.httpErrors;
    }

    public int count() {
        return this.httpErrors.size();
    }

}
