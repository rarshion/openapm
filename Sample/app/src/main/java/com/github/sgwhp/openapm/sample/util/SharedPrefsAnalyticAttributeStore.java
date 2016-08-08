package com.github.sgwhp.openapm.sample.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.sgwhp.openapm.sample.analytics.AnalyticAttribute;
import com.github.sgwhp.openapm.sample.analytics.AnalyticAttributeStore;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 2016/8/8.
 */
public class SharedPrefsAnalyticAttributeStore implements AnalyticAttributeStore {

    private static final String STORE_FILE = "NRAnalyticAttributeStore";
    private static final AgentLog log;
    private final Context context;

    public SharedPrefsAnalyticAttributeStore(final Context context) {
        this.context = context;
    }

    public boolean store(final AnalyticAttribute attribute) {
        synchronized (this) {
            if (attribute.isPersistent()) {
                final SharedPreferences preferences = this.context.getSharedPreferences("NRAnalyticAttributeStore", 0);
                final SharedPreferences.Editor editor = preferences.edit();
                switch (attribute.getAttributeDataType()) {
                    case STRING: {
                        SharedPrefsAnalyticAttributeStore.log.verbose("SharedPrefsAnalyticAttributeStore.store - storing analytic attribute " + attribute.getName() + "=" + attribute.getStringValue());
                        editor.putString(attribute.getName(), attribute.getStringValue());
                        break;
                    }
                    case FLOAT: {
                        SharedPrefsAnalyticAttributeStore.log.verbose("SharedPrefsAnalyticAttributeStore.store - storing analytic attribute " + attribute.getName() + "=" + attribute.getFloatValue());
                        editor.putFloat(attribute.getName(), attribute.getFloatValue());
                        break;
                    }
                    case BOOLEAN: {
                        SharedPrefsAnalyticAttributeStore.log.verbose("SharedPrefsAnalyticAttributeStore.store - storing analytic attribute " + attribute.getName() + "=" + attribute.getBooleanValue());
                        editor.putBoolean(attribute.getName(), attribute.getBooleanValue());
                        break;
                    }
                    default: {
                        SharedPrefsAnalyticAttributeStore.log.error("SharedPrefsAnalyticAttributeStore.store - unsupported analytic attribute data type" + attribute.getName());
                        break;
                    }
                }
                return editor.commit();
            }
            return false;
        }
    }

    public List<AnalyticAttribute> fetchAll() {
        if (SharedPrefsAnalyticAttributeStore.log.getLevel() == 4) {
            SharedPrefsAnalyticAttributeStore.log.verbose("SharedPrefsAnalyticAttributeStore.fetchAll invoked.");
        }
        final SharedPreferences preferences = this.context.getSharedPreferences("NRAnalyticAttributeStore", 0);
        final ArrayList<AnalyticAttribute> analyticAttributeArrayList = new ArrayList<AnalyticAttribute>();
        Map<String, ?> storedAttributes = null;
        synchronized (this) {
            storedAttributes = (Map<String, ?>)preferences.getAll();
        }
        for (final Map.Entry entry : storedAttributes.entrySet()) {
            SharedPrefsAnalyticAttributeStore.log.debug("SharedPrefsAnalyticAttributeStore.fetchAll - found analytic attribute " + entry.getKey() + "=" + entry.getValue());
            if (entry.getValue() instanceof String) {
                analyticAttributeArrayList.add(new AnalyticAttribute(entry.getKey().toString(), entry.getValue().toString(), true));
            }
            else if (entry.getValue() instanceof Float) {
                analyticAttributeArrayList.add(new AnalyticAttribute(entry.getKey().toString(), Float.valueOf(entry.getValue().toString()), true));
            }
            else if (entry.getValue() instanceof Boolean) {
                analyticAttributeArrayList.add(new AnalyticAttribute(entry.getKey().toString(), Boolean.valueOf(entry.getValue().toString()), true));
            }
            else {
                SharedPrefsAnalyticAttributeStore.log.error("SharedPrefsAnalyticAttributeStore.fetchAll - unsupported analytic attribute " + entry.getKey() + "=" + entry.getValue());
            }
        }
        return analyticAttributeArrayList;
    }

    public int count() {
        final SharedPreferences preferences = this.context.getSharedPreferences("NRAnalyticAttributeStore", 0);
        final int size = preferences.getAll().size();
        if (SharedPrefsAnalyticAttributeStore.log.getLevel() == 4) {
            SharedPrefsAnalyticAttributeStore.log.verbose("SharedPrefsAnalyticAttributeStore.count - returning " + size);
        }
        return size;
    }

    public void clear() {
        if (SharedPrefsAnalyticAttributeStore.log.getLevel() == 4) {
            SharedPrefsAnalyticAttributeStore.log.verbose("SharedPrefsAnalyticAttributeStore.clear - flushing stored attributes");
        }
        synchronized (this) {
            final SharedPreferences preferences = this.context.getSharedPreferences("NRAnalyticAttributeStore", 0);
            preferences.edit().clear().commit();
        }
    }

    public void delete(final AnalyticAttribute attribute) {
        synchronized (this) {
            if (SharedPrefsAnalyticAttributeStore.log.getLevel() == 4) {
                SharedPrefsAnalyticAttributeStore.log.verbose("SharedPrefsAnalyticAttributeStore.delete - deleting attribute " + attribute.getName());
            }
            final SharedPreferences preferences = this.context.getSharedPreferences("NRAnalyticAttributeStore", 0);
            preferences.edit().remove(attribute.getName()).commit();
        }
    }

    static {
        log = AgentLogManager.getAgentLog();
    }

}
