package com.github.sgwhp.openapm.sample.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.sgwhp.openapm.sample.crashes.CrashStore;
import com.github.sgwhp.openapm.sample.harvest.crash.Crash;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 2016/8/8.
 */
public class JsonCrashStore implements CrashStore
{
    //使用sharePreferences来存储，key值为NRCrashStore

    private static final String STORE_FILE = "NRCrashStore";
    private static final AgentLog log = AgentLogManager.getAgentLog();
    private final Context context;

    public JsonCrashStore(final Context context) {
        this.context = context;
    }

    public void store(final Crash crash) {
        synchronized (this) {
            final SharedPreferences store = this.context.getSharedPreferences("NRCrashStore", 0);
            final SharedPreferences.Editor editor = store.edit();
            final JsonObject jsonObj = crash.asJsonObject();
            jsonObj.add("uploadCount", SafeJsonPrimitive.factory(crash.getUploadCount()));
            editor.putString(crash.getUuid().toString(), jsonObj.toString());
            editor.commit();
        }
    }

    public List<Crash> fetchAll() {
        final SharedPreferences store = this.context.getSharedPreferences("NRCrashStore", 0);
        final List<Crash> crashes = new ArrayList<Crash>();
        final Map<String, ?> crashStrings;
        synchronized (this) {
            crashStrings = (Map<String, ?>)store.getAll();
        }
        for (final Object string : crashStrings.values()) {
            if (string instanceof String) {
                try {
                    crashes.add(Crash.crashFromJsonString((String)string));
                }
                catch (Exception e) {
                    JsonCrashStore.log.error("Exception encountered while deserializing crash", e);
                }
            }
        }
        return crashes;
    }

    public int count() {
        int count = 0;
        synchronized (this) {
            final SharedPreferences store = this.context.getSharedPreferences("NRCrashStore", 0);
            count = store.getAll().size();
        }
        return count;
    }

    public void clear() {
        synchronized (this) {
            final SharedPreferences store = this.context.getSharedPreferences("NRCrashStore", 0);
            final SharedPreferences.Editor editor = store.edit();
            editor.clear();
            editor.commit();
        }
    }

    public void delete(final Crash crash) {
        synchronized (this) {
            final SharedPreferences store = this.context.getSharedPreferences("NRCrashStore", 0);
            final SharedPreferences.Editor editor = store.edit();
            editor.remove(crash.getUuid().toString());
            editor.commit();
        }
    }

}

