package com.github.sgwhp.openapm.agent.visitor;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by user on 2016/8/7.
 */
public class TraceClassDecorator extends ClassVisitor{

    private ClassVisitor classVisitor;

    public TraceClassDecorator(final ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
        this.classVisitor = classVisitor;
    }

    public void addTraceField() {
        this.classVisitor.visitField(1, "_nr_trace", "Lcom/github/sgwhp/openapm/sample/tracing/Trace;", null, null);
    }

    public static String[] addInterface(final String[] interfaces) {
        final ArrayList<String> newInterfaces = new ArrayList<String>(Arrays.asList(interfaces));
        newInterfaces.add("com/github/sgwhp/openapm/sample/api/v2/TraceFieldInterface");
        return newInterfaces.toArray(new String[newInterfaces.size()]);
    }

    public void addTraceInterface(final Type ownerType) {
        MethodVisitor mv = this.classVisitor.visitMethod(1, "_nr_setTrace", "(Lcom/github/sgwhp/openapm/sample/tracing/Trace;)V", null, null);
        final Method method = new Method("_nr_setTrace", "(Lcom/github/sgwhp/openapm/sample/tracing/Trace;)V");

        mv = new GeneratorAdapter(1, method, mv) {
            @Override
            public void visitCode() {
                final Label tryStart = new Label();
                final Label tryEnd = new Label();
                final Label tryHandler = new Label();
                super.visitCode();
                this.visitLabel(tryStart);
                this.loadThis();
                this.loadArgs();
                //生成追踪的方法对象
                this.putField(ownerType, "_nr_trace", Type.getType("Lcom/github/sgwhp/openapm/sample/tracing/Trace;"));
                this.goTo(tryEnd);
                this.visitLabel(tryHandler);
                this.pop();
                this.visitLabel(tryEnd);
                this.visitTryCatchBlock(tryStart, tryEnd, tryHandler, "java/lang/Exception");
                this.visitInsn(177);
            }
        };

        mv.visitCode();
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
