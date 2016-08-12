package com.github.sgwhp.openapm.agent.visitor;

/**
 * Created by rarshion on 16/8/11.
 */
public class Annotations {

    public static final String INSTRUMENTED = "Lcom/github/sgwhp/openapm/sample/instrumentation/Instrumented;";

    public static boolean isNewRelicAnnotation(final String descriptor) {
        return descriptor.startsWith("Lcom/github/sgwhp/openapm/sample/instrumentation/");
    }
}
