package com.github.sgwhp.openapm.sample.measurement;

import com.github.sgwhp.openapm.sample.Instrumentation.MetricCategory;

/**
 * Created by user on 2016/8/1.
 */
//不同类别的测量
public class CategorizedMeasurement extends BaseMeasurement{
    private MetricCategory category;//加多了一个Metric分类属性

    public CategorizedMeasurement(final MeasurementType measurementType) {
            super(measurementType);
        }

    public MetricCategory getCategory() {
        return this.category;
    }

    public void setCategory(final MetricCategory category) {
        this.category = category;
    }
}
