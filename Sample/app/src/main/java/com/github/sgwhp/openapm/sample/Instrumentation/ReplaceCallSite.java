package com.github.sgwhp.openapm.sample.Instrumentation;

/**
 * Created by user on 2016/8/6.
 */
public @interface ReplaceCallSite {
    boolean isStatic() default false;
    String scope() default "";
}
