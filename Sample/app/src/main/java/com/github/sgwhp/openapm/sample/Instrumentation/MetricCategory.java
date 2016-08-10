package com.github.sgwhp.openapm.sample.Instrumentation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 2016/8/1.
 */
public enum MetricCategory {

    NONE("None"),
    VIEW_LOADING("View Loading"),
    VIEW_LAYOUT("Layout"),
    DATABASE("Database"),
    IMAGE("Images"),
    JSON("JSON"),
    NETWORK("Network");

    private String categoryName;//种类名
    private static final Map<String, MetricCategory> methodMap = new HashMap<String, MetricCategory>() {
        {this.put("onCreate", MetricCategory.VIEW_LOADING);}
    };

    private MetricCategory(final String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    //根据方法全名返回测量种类
    public static MetricCategory categoryForMethod(final String fullMethodName) {
        if (fullMethodName == null) {
            return MetricCategory.NONE;
        }
        String methodName = null;
        final int hashIndex = fullMethodName.indexOf("#");
        if (hashIndex >= 0) {
            methodName = fullMethodName.substring(hashIndex + 1);
        }
        MetricCategory category = MetricCategory.methodMap.get(methodName);
        if (category == null) {
            category = MetricCategory.NONE;
        }
        return category;
    }

}
