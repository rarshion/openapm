package com.github.sgwhp.openapm.sample.analytics;

import java.util.List;

/**
 * Created by user on 2016/8/2.
 */
public interface AnalyticAttributeStore {
    boolean store(AnalyticAttribute p0);
    List<AnalyticAttribute> fetchAll();
    int count();
    void clear();
    void delete(AnalyticAttribute p0);
}
