package com.github.sgwhp.openapm.sample.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by user on 2016/8/1.
 */
public class NamedThreadFactory implements ThreadFactory {

    final ThreadGroup group;
    final String namePrefix;
    final AtomicInteger threadNumber;

    public NamedThreadFactory(final String factoryName){
        this.threadNumber = new AtomicInteger(1);
        final SecurityManager s = System.getSecurityManager();
        this.group = ((s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup());
        this.namePrefix = "NR_" + factoryName + "-";
    }

    public Thread newThread(final  Runnable r){
            final Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
            if(t.isDaemon()){
                t.setDaemon(false);
            }
            if (t.getPriority() != 5) {
                t.setPriority(5);
            }
            return t;
    }

}



