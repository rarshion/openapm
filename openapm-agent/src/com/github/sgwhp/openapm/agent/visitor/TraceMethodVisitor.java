package com.github.sgwhp.openapm.agent.visitor;

import com.github.sgwhp.openapm.agent.util.Log;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import java.util.ArrayList;

/**
 * Created by user on 2016/8/3.
 */

public class TraceMethodVisitor extends AdviceAdapter {

    //public static final String TRACE_MACHINE_INTERNAL_CLASSNAME = "com/newrelic/agent/android/tracing/TraceMachine";
    public static final String TRACE_MACHINE_INTERNAL_CLASSNAME = "com/github/sgwhp/openapm/sample/tracing/TraceMachine";

    protected final Log log;
    private String name;
    protected boolean unloadContext;
    protected boolean startTracing;
    private int access;
    private TransformContext context;

    public TraceMethodVisitor(final MethodVisitor mv, final  int acc, final String name, final
                              String desc, TransformContext context){
        super(ASM5, mv, acc, name, desc);
        this.unloadContext = false;
        this.access = access;
        this.context = context;
        this.log = context.getLog();
    }

    public void setUnloadContext() {
        this.unloadContext = true;
    }

    public void setStartTracing() {
        this.startTracing = true;
    }

    @Override
    protected void onMethodEnter(){
        log.d("---TraceMethodVisitor---onMethodEnter");

        final Type targetType = Type.getObjectType("com/github/sgwhp/openapm/sample/tracing/TraceMachine");
        if (this.startTracing) {
            super.visitLdcInsn(this.context.getSimpleClassName());
            super.invokeStatic(targetType, new Method("startTracing", "(Ljava/lang/String;)V"));
        }

        if ((this.access & 0x8) != 0x0) {
            this.log.d("Tracing static method " + this.context.getClassName() + "#" + this.name);

            super.visitInsn(1);
            super.visitLdcInsn(this.context.getSimpleClassName() + "#" + this.name);
            this.emitAnnotationParamsList(this.name);
            //调用TraceMachine中的enterMethod(final Trace trace, final String name, final ArrayList<String> annotationParams)方法
            super.invokeStatic(targetType, new Method("enterMethod", "(Lcom/github/sgwhp/openapm/sample/tracing/Trace;Ljava/lang/String;Ljava/util/ArrayList;)V"));

        } else {
            this.log.d("Tracing method " + this.context.getClassName() + "#" + this.name);
            final Label tryStart = new Label();
            final Label tryEnd = new Label();
            final Label tryHandler = new Label();
            super.visitLabel(tryStart);
            super.loadThis();
            super.getField(Type.getObjectType(this.context.getClassName()), "_nr_trace", Type.getType("Lcom/github/sgwhp/openapm/sample/tracing/Trace;"));
            super.visitLdcInsn(this.context.getSimpleClassName() + "#" + this.name);
            this.emitAnnotationParamsList(this.name);
            super.invokeStatic(targetType, new Method("enterMethod", "(Lcom/github/sgwhp/openapm/sample/tracing/Trace;Ljava/lang/String;Ljava/util/ArrayList;)V"));
            super.goTo(tryEnd);
            super.visitLabel(tryHandler);
            super.pop();
            super.visitInsn(1);
            super.visitLdcInsn(this.context.getSimpleClassName() + "#" + this.name);
            this.emitAnnotationParamsList(this.name);
            super.invokeStatic(targetType, new Method("enterMethod", "(Lcom/github/sgwhp/openapm/sample/tracing/Trace;Ljava/lang/String;Ljava/util/ArrayList;)V"));
            super.visitLabel(tryEnd);
            super.visitTryCatchBlock(tryStart, tryEnd, tryHandler, "java/lang/NoSuchFieldError");
        }
    }

    private void emitAnnotationParamsList(final String name) {

        log.d("---TraceMethodVisitor---emitAnnotationParamsList " + name);

        final ArrayList<String> annotationParameters = this.context.getTracedMethodParameters(name);
        if (annotationParameters == null || annotationParameters.size() == 0) {
            super.visitInsn(1);
            return;
        }

        final Method constructor = Method.getMethod("void <init> ()");
        final Method add = Method.getMethod("boolean add(java.lang.Object)");
        final Type arrayListType = Type.getObjectType("java/util/ArrayList");
        super.newInstance(arrayListType);
        super.dup();
        super.invokeConstructor(arrayListType, constructor);
        for (final String parameterEntry : annotationParameters) {
            super.dup();
            super.visitLdcInsn(parameterEntry);
            super.invokeVirtual(arrayListType, add);
            super.pop();
        }
    }

    @Override
    protected void onMethodExit(final int opcode) {

        log.d("---TraceMethodVisitor---onMethodExit");

        Type targetType = Type.getObjectType("com/github/sgwhp/openapm/sample/tracing/TraceMachine");
        super.invokeStatic(targetType, new Method("exitMethod", "()V"));

        if (this.unloadContext) {
            super.loadThis();
            targetType = Type.getObjectType("com/github/sgwhp/openapm/sample/tracing/TraceMachine");
            super.invokeStatic(targetType, new Method("unloadTraceContext", "(Ljava/lang/Object;)V"));
        }
    }
}
