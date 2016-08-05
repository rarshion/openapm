package com.github.sgwhp.openapm.sample.harvest;

import com.github.sgwhp.openapm.sample.harvest.type.HarvestableArray;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by user on 2016/8/2.
 */
public class Events extends HarvestableArray {

    private final Collection<Event> events;

    public Events() {
        this.events = new ArrayList<Event>();
    }

    @Override
    public JsonArray asJsonArray() {
        final JsonArray array = new JsonArray();
        for (final Event event : this.events) {
            array.add(event.asJson());
        }
        return array;
    }

    public void addEvent(final Event event) {
        this.events.add(event);
    }
}
