package com.github.sgwhp.openapm.sample.harvest;

import com.github.sgwhp.openapm.sample.harvest.type.Harvestable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by user on 2016/8/2.
 */
public class HarvestableCache {

    private static final int DEFAUL_CACHE_LIMIT = 1024;
    private int limit;
    private  final Collection<Harvestable> cache;

    public HarvestableCache() {
        this.limit = 1024;
        this.cache = this.getNewCache();
    }

    protected Collection<Harvestable> getNewCache() {
        return new CopyOnWriteArrayList<Harvestable>();
    }

    public void add(final Harvestable harvestable) {
        if (harvestable == null || this.cache.size() >= this.limit) {
            return;
        }
        this.cache.add(harvestable);
    }

    public boolean get(final Object h) {
        return this.cache.contains(h);
    }

    public Collection<Harvestable> flush() {
        if (this.cache.size() == 0) {
            /*这里要注意，这是注释掉的*/
            //return (Collection<Harvestable>)Collections.emptyList();
            return null;
        }
        synchronized (this) {
            final Collection<Harvestable> oldCache = this.getNewCache();
            oldCache.addAll(this.cache);
            this.cache.clear();
            return oldCache;
        }
    }

    public int getSize() {
        return this.cache.size();
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }


}
