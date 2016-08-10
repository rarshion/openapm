package com.github.sgwhp.openapm.sample.measurement.producer;

import com.github.sgwhp.openapm.sample.measurement.MeasurementType;
import com.github.sgwhp.openapm.sample.measurement.MethodMeasurement;
import com.github.sgwhp.openapm.sample.tracing.Trace;

/**
 * Created by user on 2016/8/2.
 */
//对象的容器管理,并无逻辑处理过程
public class MethodMeasurementProducer extends BaseMeasurementProducer {

    public MethodMeasurementProducer() {
        super(MeasurementType.Method);
    }

    public void produceMeasurement(final Trace trace) {
        final MethodMeasurement methodMeasurement =
                new MethodMeasurement(trace.displayName, trace.scope,
                        trace.entryTimestamp, trace.exitTimestamp,
                        trace.exclusiveTime, trace.getCategory());//这里使用了Trace的一些属性值如时间戳/作用范围/分类别

        this.produceMeasurement(methodMeasurement);//父类方法,添加到容器中
    }
}
