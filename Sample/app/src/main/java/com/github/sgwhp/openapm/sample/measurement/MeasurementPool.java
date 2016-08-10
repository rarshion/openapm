package com.github.sgwhp.openapm.sample.measurement;

import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.measurement.consumer.MeasurementConsumer;
import com.github.sgwhp.openapm.sample.measurement.producer.BaseMeasurementProducer;
import com.github.sgwhp.openapm.sample.measurement.producer.MeasurementProducer;
import com.github.sgwhp.openapm.sample.util.ExceptionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by user on 2016/8/1.
 */
//用来装生产者与消费者的缓冲区
public class MeasurementPool extends BaseMeasurementProducer implements MeasurementConsumer {

    private static final AgentLog log= AgentLogManager.getAgentLog();
    private final Collection<MeasurementProducer> producers;//生产者容器
    private final Collection<MeasurementConsumer> consumers;//消费者容器

    public MeasurementPool() {
        super(MeasurementType.Any);
        this.producers = new CopyOnWriteArrayList<MeasurementProducer>();
        this.consumers = new CopyOnWriteArrayList<MeasurementConsumer>();
        this.addMeasurementProducer(this);
    }

    //添加生产者
    public void addMeasurementProducer(final MeasurementProducer producer) {
        if (producer != null) {
            synchronized (this.producers) {
                if (this.producers.contains(producer)) {
                    MeasurementPool.log.debug("Attempted to add the same MeasurementProducer " + producer + "  multiple times.");
                    return;
                }
                this.producers.add(producer);
            }
        }
        else {
            MeasurementPool.log.debug("Attempted to add null MeasurementProducer.");
        }
    }

    //移除生产者
    public void removeMeasurementProducer(final MeasurementProducer producer) {
        synchronized (this.producers) {
            if (!this.producers.contains(producer)) {
                MeasurementPool.log.debug("Attempted to remove MeasurementProducer " + producer + " which is not registered.");
                return;
            }
            this.producers.remove(producer);
        }
    }

    //添加消费者
    public void addMeasurementConsumer(final MeasurementConsumer consumer) {
        if (consumer != null) {
            synchronized (this.consumers) {
                if (this.consumers.contains(consumer)) {
                    MeasurementPool.log.debug("Attempted to add the same MeasurementConsumer " + consumer + " multiple times.");
                    return;
                }
                this.consumers.add(consumer);
            }
        }
        else {
            MeasurementPool.log.debug("Attempted to add null MeasurementConsumer.");
        }
    }

    //移除消费者
    public void removeMeasurementConsumer(final MeasurementConsumer consumer) {
        synchronized (this.consumers) {
            if (!this.consumers.contains(consumer)) {
                MeasurementPool.log.debug("Attempted to remove MeasurementConsumer " + consumer + " which is not registered.");
                return;
            }
            this.consumers.remove(consumer);
        }
    }

    //广播测量,通知消费者消费
    public void broadcastMeasurements() {
        final List<Measurement> allProducedMeasurements = new ArrayList<Measurement>();

        //将当前生产者容器中的的对象全部拷贝出来
        synchronized (this.producers) {
            for (final MeasurementProducer producer : this.producers) {
                final Collection<Measurement> measurements = producer.drainMeasurements();
                if (measurements.size() > 0) {
                    allProducedMeasurements.addAll(measurements);
                    while (allProducedMeasurements.remove(null)) {}
                }
            }
        }

        //遍历消费者容器,如果测量类别与生产者的相同则让该消费者去消费
        if (allProducedMeasurements.size() > 0) {
            synchronized (this.consumers) {
                //遍历生产者与消费者两个容器
                for (final MeasurementConsumer consumer : this.consumers) {
                    final List<Measurement> measurements2 = new ArrayList<Measurement>(allProducedMeasurements);
                    for (final Measurement measurement : measurements2) {
                        if (consumer.getMeasurementType() != measurement.getType()) {
                            if (consumer.getMeasurementType() != MeasurementType.Any) {
                                continue;
                            }
                        }
                        try {
                            consumer.consumeMeasurement(measurement);//消费
                        }
                        catch (Exception e) {
                            ExceptionHelper.exceptionToErrorCode(e);
                            MeasurementPool.log.error("broadcastMeasurements exception[" + e.getClass().getName() + "]");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void consumeMeasurement(final Measurement measurement) {
        this.produceMeasurement(measurement);
    }

    @Override
    public void consumeMeasurements(final Collection<Measurement> measurements) {
        this.produceMeasurements(measurements);
    }

    @Override
    public MeasurementType getMeasurementType() {
        return MeasurementType.Any;
    }

    public Collection<MeasurementProducer> getMeasurementProducers() {
        return this.producers;
    }

    public Collection<MeasurementConsumer> getMeasurementConsumers() {
        return this.consumers;
    }

}
