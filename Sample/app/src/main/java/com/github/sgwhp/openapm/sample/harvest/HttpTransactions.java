package com.github.sgwhp.openapm.sample.harvest;

import com.github.sgwhp.openapm.sample.harvest.type.HarvestableArray;
import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by user on 2016/8/2.
 */
public class HttpTransactions extends HarvestableArray {

    private final Collection<HttpTransaction> httpTransactions;

    public HttpTransactions() {
        this.httpTransactions = new CopyOnWriteArrayList<HttpTransaction>();
    }

    public synchronized void add(final HttpTransaction httpTransaction) {
        this.httpTransactions.add(httpTransaction);
    }

    public synchronized void remove(final HttpTransaction transaction) {
        this.httpTransactions.remove(transaction);
    }

    public void clear() {
        this.httpTransactions.clear();
    }

    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        for (final HttpTransaction transaction : this.httpTransactions) {
            array.add(transaction.asJson());
        }
        return array;
    }

    public Collection<HttpTransaction> getHttpTransactions() {
        return this.httpTransactions;
    }

    public int count() {
        return this.httpTransactions.size();
    }

    @Override
    public String toString() {
        return "HttpTransactions{httpTransactions=" + this.httpTransactions + '}';
    }
}
