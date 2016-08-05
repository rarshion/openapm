package com.github.sgwhp.openapm.sample.harvest;

import com.github.sgwhp.openapm.sample.harvest.type.HarvestableArray;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

/**
 * Created by user on 2016/8/2.
 */
public class DataToken extends HarvestableArray {

    private int accountId;
    private int agentId;

    public DataToken() {
    }

    public DataToken(final int accountId, final int agentId) {
        this.accountId = accountId;
        this.agentId = agentId;
    }

    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(this.accountId));
        array.add(new JsonPrimitive(this.agentId));
        return array;
    }

    public void clear() {
        this.accountId = 0;
        this.agentId = 0;
    }

    public int getAccountId() {
        return this.accountId;
    }

    public void setAccountId(final int accountId) {
        this.accountId = accountId;
    }

    public int getAgentId() {
        return this.agentId;
    }

    public void setAgentId(final int agentId) {
        this.agentId = agentId;
    }

    public boolean isValid() {
        return this.accountId > 0 && this.agentId > 0;
    }

    @Override
    public String toString() {
        return "DataToken{accountId=" + this.accountId + ", agentId=" + this.agentId + '}';
    }

}
