package com.github.sgwhp.openapm.sample;

import com.github.sgwhp.openapm.sample.activity.MeasuredActivity;
import com.github.sgwhp.openapm.sample.api.common.TransactionData;
import com.github.sgwhp.openapm.sample.harvest.Harvest;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.measurement.ThreadInfo;
import com.github.sgwhp.openapm.sample.measurement.consumer.ActivityMeasurementConsumer;
import com.github.sgwhp.openapm.sample.measurement.consumer.CustomMetricConsumer;
import com.github.sgwhp.openapm.sample.measurement.consumer.HttpErrorHarvestingConsumer;
import com.github.sgwhp.openapm.sample.measurement.consumer.HttpTransactionHarvestingConsumer;
import com.github.sgwhp.openapm.sample.measurement.consumer.MeasurementConsumer;
import com.github.sgwhp.openapm.sample.measurement.consumer.MethodMeasurementConsumer;
import com.github.sgwhp.openapm.sample.measurement.consumer.SummaryMetricMeasurementConsumer;
import com.github.sgwhp.openapm.sample.measurement.http.HttpTransactionMeasurement;
import com.github.sgwhp.openapm.sample.measurement.producer.ActivityMeasurementProducer;
import com.github.sgwhp.openapm.sample.measurement.producer.CustomMetricProducer;
import com.github.sgwhp.openapm.sample.measurement.producer.HttpErrorMeasurementProducer;
import com.github.sgwhp.openapm.sample.measurement.producer.MeasurementProducer;
import com.github.sgwhp.openapm.sample.measurement.producer.MethodMeasurementProducer;
import com.github.sgwhp.openapm.sample.measurement.producer.NetworkMeasurementProducer;
import com.github.sgwhp.openapm.sample.metric.MetricUnit;
import com.github.sgwhp.openapm.sample.tracing.Trace;

import java.util.Map;

/**
 * Created by user on 2016/8/1.
 */
public class Measurements {

    private static final AgentLog log;

    private static final MeasurementEngine measurementEngine;
    private static final HttpErrorMeasurementProducer httpErrorMeasurementProducer;
    private static final NetworkMeasurementProducer networkMeasurementProducer;
    private static final ActivityMeasurementProducer activityMeasurementProducer;
    private static final MethodMeasurementProducer methodMeasurementProducer;
    private static final CustomMetricProducer customMetricProducer;

    private static final HttpErrorHarvestingConsumer httpErrorHarvester;
    private static final HttpTransactionHarvestingConsumer httpTransactionHarvester;
    private static final ActivityMeasurementConsumer activityConsumer;
    private static final MethodMeasurementConsumer methodMeasurementConsumer;
    private static final SummaryMetricMeasurementConsumer summaryMetricMeasurementConsumer;
    private static final CustomMetricConsumer customMetricConsumer;
    private static boolean broadcastNewMeasurements;

    static {
        log = AgentLogManager.getAgentLog();

        measurementEngine = new MeasurementEngine();//MeasurementEngine实例化,该对象代管生产者与消费者缓冲区

        httpErrorMeasurementProducer = new HttpErrorMeasurementProducer();
        networkMeasurementProducer = new NetworkMeasurementProducer();
        activityMeasurementProducer = new ActivityMeasurementProducer();
        methodMeasurementProducer = new MethodMeasurementProducer();//生产者容器
        customMetricProducer = new CustomMetricProducer();

        httpErrorHarvester = new HttpErrorHarvestingConsumer();
        httpTransactionHarvester = new HttpTransactionHarvestingConsumer();
        activityConsumer = new ActivityMeasurementConsumer();
        methodMeasurementConsumer = new MethodMeasurementConsumer();//消费者容器
        summaryMetricMeasurementConsumer = new SummaryMetricMeasurementConsumer();
        customMetricConsumer = new CustomMetricConsumer();

        Measurements.broadcastNewMeasurements = true;
    }

    //初始化
    //1.开始任务队列;
    //2.在Engine中添加消费者与生产者
    public static void initialize() {

        System.out.println("---Rarshion:Measurements#Engine initialize---");
        Measurements.log.info("Measurement Engine initialized.");

        TaskQueue.start();//开始任务队列

        addMeasurementProducer(Measurements.httpErrorMeasurementProducer);
        addMeasurementProducer(Measurements.networkMeasurementProducer);
        addMeasurementProducer(Measurements.activityMeasurementProducer);
        addMeasurementProducer(Measurements.methodMeasurementProducer);
        addMeasurementProducer(Measurements.customMetricProducer);

        addMeasurementConsumer(Measurements.httpErrorHarvester);
        addMeasurementConsumer(Measurements.httpTransactionHarvester);
        addMeasurementConsumer(Measurements.activityConsumer);
        addMeasurementConsumer(Measurements.methodMeasurementConsumer);
        addMeasurementConsumer(Measurements.summaryMetricMeasurementConsumer);
        addMeasurementConsumer(Measurements.customMetricConsumer);

    }
    //关闭
    //1.结束任务队列;
    //2.在Engine中移除消费者与生产者
    public static void shutdown() {

        Measurements.log.info("Measurement Engine shutting down.");

        TaskQueue.stop();
        Measurements.measurementEngine.clear();

        removeMeasurementProducer(Measurements.httpErrorMeasurementProducer);
        removeMeasurementProducer(Measurements.networkMeasurementProducer);
        removeMeasurementProducer(Measurements.activityMeasurementProducer);
        removeMeasurementProducer(Measurements.methodMeasurementProducer);
        removeMeasurementProducer(Measurements.customMetricProducer);

        removeMeasurementConsumer(Measurements.httpErrorHarvester);
        removeMeasurementConsumer(Measurements.httpTransactionHarvester);
        removeMeasurementConsumer(Measurements.activityConsumer);
        removeMeasurementConsumer(Measurements.methodMeasurementConsumer);
        removeMeasurementConsumer(Measurements.summaryMetricMeasurementConsumer);
        removeMeasurementConsumer(Measurements.customMetricConsumer);
    }


    //添加http错误到生产者对象并通知广播
    public static void addHttpError(final String url, final String httpMethod, final int statusCode) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.httpErrorMeasurementProducer.produceMeasurement(url, httpMethod, statusCode);
        newMeasurementBroadcast();//
    }
    //添加http错误到生产者对象并通知广播
    public static void addHttpError(final String url, final String httpMethod, final int statusCode, final String responseBody) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.httpErrorMeasurementProducer.produceMeasurement(url, httpMethod, statusCode, responseBody);
        newMeasurementBroadcast();
    }
    //添加http错误到生产者对象并通知广播
    public static void addHttpError(final String url, final String httpMethod, final int statusCode, final String responseBody, final Map<String, String> params) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.httpErrorMeasurementProducer.produceMeasurement(url, httpMethod, statusCode, responseBody, params);
        newMeasurementBroadcast();
    }
    //添加http错误到生产者对象并通知广播
    public static void addHttpError(final String url, final String httpMethod, final int statusCode, final String responseBody, final Map<String, String> params, final ThreadInfo threadInfo) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.httpErrorMeasurementProducer.produceMeasurement(url, httpMethod, statusCode, responseBody, params, threadInfo);
        newMeasurementBroadcast();
    }
    //添加http传输到生产者对象并通知广播
    public static void addHttpTransaction(final HttpTransactionMeasurement transactionMeasurement) {
        if (Harvest.isDisabled()) {
            return;
        }
        if (transactionMeasurement == null) {
            Measurements.log.error("TransactionMeasurement is null. HttpTransactionMeasurement measurement not created.");
        }
        else {
            Measurements.networkMeasurementProducer.produceMeasurement(transactionMeasurement);
            newMeasurementBroadcast();
        }
    }
    //添加http错误到生产者对象并通知广播
    public static void addHttpError(final TransactionData transactionData, final String responseBody, final Map<String, String> params) {
        if (transactionData == null) {
            Measurements.log.error("TransactionData is null. HttpError measurement not created.");
        }
        else {
            addHttpError(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), responseBody, params);
        }
    }
    //添加Metric生产者对象并通知广播
    public static void addCustomMetric(final String name, final String category, final int count, final double totalValue, final double exclusiveValue) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.customMetricProducer.produceMeasurement(name, category, count, totalValue, exclusiveValue);
        newMeasurementBroadcast();
    }

    public static void addCustomMetric(final String name, final String category, final int count, final double totalValue, final double exclusiveValue, final MetricUnit countUnit, final MetricUnit valueUnit) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.customMetricProducer.produceMeasurement(name, category, count, totalValue, exclusiveValue, countUnit, valueUnit);
        newMeasurementBroadcast();
    }
    //设置可以广播操作
    public static void setBroadcastNewMeasurements(final boolean broadcast) {
        Measurements.broadcastNewMeasurements = broadcast;
    }
    //广播
    private static void newMeasurementBroadcast() {
        if (Measurements.broadcastNewMeasurements) {
            broadcast();
        }
    }
    //进行广播操作,底层调用Pool中的broadcast()方法通知消费者消费
    public static void broadcast() {
        Measurements.measurementEngine.broadcastMeasurements();//在Engine中执行Pool里面的广播方法
    }

    public static MeasuredActivity startActivity(final String activityName) {
        if (Harvest.isDisabled()) {
            return null;
        }
        return Measurements.measurementEngine.startActivity(activityName);
    }

    public static void renameActivity(final String oldName, final String newName) {
        Measurements.measurementEngine.renameActivity(oldName, newName);
    }

    public static void endActivity(final String activityName) {
        if (Harvest.isDisabled()) {
            return;
        }
        final MeasuredActivity measuredActivity = Measurements.measurementEngine.endActivity(activityName);
        Measurements.activityMeasurementProducer.produceMeasurement(measuredActivity);
        newMeasurementBroadcast();
    }

    public static void endActivity(final MeasuredActivity activity) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.measurementEngine.endActivity(activity);
        Measurements.activityMeasurementProducer.produceMeasurement(activity);
        newMeasurementBroadcast();
    }

    public static void endActivityWithoutMeasurement(final MeasuredActivity activity) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.measurementEngine.endActivity(activity);
    }

    //添加方法追踪生产者对象并通知广播
    public static void addTracedMethod(final Trace trace) {
        if (Harvest.isDisabled()) {
            return;
        }
        Measurements.methodMeasurementProducer.produceMeasurement(trace);
        newMeasurementBroadcast();
    }

    //在Engine中添加生产者
    public static void addMeasurementProducer(final MeasurementProducer measurementProducer) {
        Measurements.measurementEngine.addMeasurementProducer(measurementProducer);
    }

    //在Engine中移除生产者
    public static void removeMeasurementProducer(final MeasurementProducer measurementProducer) {
        Measurements.measurementEngine.removeMeasurementProducer(measurementProducer);
    }

    //在Engine中添加消费者
    public static void addMeasurementConsumer(final MeasurementConsumer measurementConsumer) {
        //System.out.println("---Rarshion:Measurements#addMeasurementConsumer");
        Measurements.measurementEngine.addMeasurementConsumer(measurementConsumer);
    }

    //在Engine中移除消费者
    public static void removeMeasurementConsumer(final MeasurementConsumer measurementConsumer) {
        Measurements.measurementEngine.removeMeasurementConsumer(measurementConsumer);
    }

    //通知消费者消费生产者
    public static void process() {
        Measurements.measurementEngine.broadcastMeasurements();
    }
}
