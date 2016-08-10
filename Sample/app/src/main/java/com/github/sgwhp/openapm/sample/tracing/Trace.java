package com.github.sgwhp.openapm.sample.tracing;

import com.github.sgwhp.openapm.sample.Instrumentation.MetricCategory;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;
import com.github.sgwhp.openapm.sample.util.Util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by user on 2016/8/1.
 */
public class Trace {

    private static final String CATEGORY_PARAMETER = "category";
    private static final AgentLog log = AgentLogManager.getAgentLog();

    public final UUID parentUUID;//父类的ID
    public final UUID myUUID;//自己的ID

    public long entryTimestamp;//进入时间戳
    public long exitTimestamp;//离开的时间戳
    public long exclusiveTime;//耗时
    public long childExclusiveTime;//子类耗时

    //关于metric的属性
    public String metricName;
    public String metricBackgroundName;
    public String displayName;
    public String scope;
    public long threadId;
    public String threadName;

    private volatile Map<String, Object> params;//参数
    private List<String> rawAnnotationParams;//原生的声明参数
    private volatile Set<UUID> children;//获取子类
    private TraceType type;//跟踪类型
    private boolean isComplete;//是否完成

    public TraceMachine traceMachine;

    public Trace() {
        this.myUUID = new UUID(Util.getRandom().nextLong(), Util.getRandom().nextLong());
        this.entryTimestamp = 0L;
        this.exitTimestamp = 0L;
        this.exclusiveTime = 0L;
        this.childExclusiveTime = 0L;
        this.threadId = 0L;
        this.threadName = "main";
        this.type = TraceType.TRACE;
        this.isComplete = false;
        this.parentUUID = null;
    }

    public Trace(final String displayName, final UUID parentUUID, final TraceMachine traceMachine) {
        this.myUUID = new UUID(Util.getRandom().nextLong(), Util.getRandom().nextLong());
        this.entryTimestamp = 0L;
        this.exitTimestamp = 0L;
        this.exclusiveTime = 0L;
        this.childExclusiveTime = 0L;
        this.threadId = 0L;
        this.threadName = "main";
        this.type = TraceType.TRACE;
        this.isComplete = false;
        this.displayName = displayName;
        this.parentUUID = parentUUID;
        this.traceMachine = traceMachine;
    }

    //添加子类的信息
    public void addChild(final Trace trace) {
        if (this.children == null) {
            synchronized (this) {
                if (this.children == null) {
                    this.children = Collections.synchronizedSet(new HashSet<UUID>());//
                }
            }
        }
        this.children.add(trace.myUUID);
    }
    //获取子类的信息
    public Set<UUID> getChildren() {
        if (this.children == null) {
            synchronized (this) {
                if (this.children == null) {
                    this.children = Collections.synchronizedSet(new HashSet<UUID>());
                }
            }
        }
        return this.children;
    }
    //获取参数
    public Map<String, Object> getParams() {
        if (this.params == null) {
            synchronized (this) {
                if (this.params == null) {
                    this.params = new ConcurrentHashMap<String, Object>();
                }
            }
        }
        return this.params;
    }
    //设置声明的参数
    public void setAnnotationParams(final List<String> rawAnnotationParams) {
        this.rawAnnotationParams = rawAnnotationParams;
    }
    //获取声明的参数
    public Map<String, Object> getAnnotationParams() {
        final HashMap<String, Object> annotationParams = new HashMap<String, Object>();
        if (this.rawAnnotationParams != null && this.rawAnnotationParams.size() > 0) {
            final Iterator<String> i = this.rawAnnotationParams.iterator();
            while (i.hasNext()) {
                final String paramName = i.next();
                final String paramClass = i.next();
                final String paramValue = i.next();
                final Object param = createParameter(paramName, paramClass, paramValue);
                if (param != null) {
                    annotationParams.put(paramName, param);
                }
            }
        }
        return annotationParams;
    }
    //
    public boolean isComplete() {
        return this.isComplete;
    }

    //追踪结束,调用了TraceMachine的方法
    public void complete() throws TracingInactiveException {
        if (this.isComplete) {
            Trace.log.warning("Attempted to double complete trace " + this.myUUID.toString());
            return;
        }
        if (this.exitTimestamp == 0L) {
            this.exitTimestamp = System.currentTimeMillis();
        }
        this.exclusiveTime = this.getDurationAsMilliseconds() - this.childExclusiveTime;
        this.isComplete = true;
        try {
            this.traceMachine.storeCompletedTrace(this);//调用TraceMachine存储完成跟踪
        }
        catch (NullPointerException e) {
            throw new TracingInactiveException();
        }
    }

    public void prepareForSerialization() {
        this.getParams().put("type", this.type.toString());
    }

    //获取跟踪种类
    public TraceType getType() {
        return this.type;
    }
    //设置跟踪种类
    public void setType(final TraceType type) {
        this.type = type;
    }
    //获取时间间隔(毫秒)
    public long getDurationAsMilliseconds() {
        return this.exitTimestamp - this.entryTimestamp;
    }
    //获取时间间隔(秒)
    public float getDurationAsSeconds() {
        return (this.exitTimestamp - this.entryTimestamp) / 1000.0f;
    }
    //获取测量种类,MetriCategory
    public MetricCategory getCategory() {
        if (!this.getAnnotationParams().containsKey("category")) {
            return null;
        }
        final Object category = this.getAnnotationParams().get("category");
        if (!(category instanceof MetricCategory)) {
            Trace.log.error("Category annotation parameter is not of type MetricCategory");
            return null;
        }
        return (MetricCategory)category;
    }

    //获取函数参数
    private static Object createParameter(final String parameterName, final String parameterClass, final String parameterValue) {
        Class clazz;
        try {
            clazz = Class.forName(parameterClass);
        }
        catch (ClassNotFoundException e) {
            Trace.log.error("Unable to resolve parameter class in enterMethod: " + e.getMessage(), e);
            return null;
        }
        if (MetricCategory.class == clazz) {
            return MetricCategory.valueOf(parameterValue);
        }
        if (String.class == clazz) {
            return parameterValue;
        }
        return null;
    }


}
