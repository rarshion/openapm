package com.github.sgwhp.openapm.sample.tracing;

/**
 * Created by user on 2016/8/1.
 */
//追踪过程的接口
public interface TraceLifecycleAware {

    void onEnterMethod();//进入方法

    void onExitMethod();//离开方法

    void onTraceStart(ActivityTrace p0);//开始跟踪

    void onTraceComplete(ActivityTrace p0);//结束跟踪
}
