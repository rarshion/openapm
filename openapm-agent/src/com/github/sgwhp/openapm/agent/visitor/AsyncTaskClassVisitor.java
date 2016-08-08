package com.github.sgwhp.openapm.agent.visitor;

import com.github.sgwhp.openapm.agent.util.Log;
import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


import com.github.sgwhp.openapm.agent.util.Log;
import org.objectweb.asm.Opcodes;

/**
 * Created by user on 2016/8/7.
 */


public class AsyncTaskClassVisitor extends ClassVisitor {

    public static final String TARGET_CLASS = "android/os/AsyncTask";
    private final TransformContext context;
    private final Log log;
    private boolean instrument;

    public static final ImmutableMap<String, String> traceMethodMap;
    public static final ImmutableMap<String, String> endTraceMethodMap;

    public AsyncTaskClassVisitor(final ClassVisitor cv, final TransformContext context, final Log log) {
        super(Opcodes.ASM5, cv);
        this.instrument = false;
        this.context = context;
        this.log = log;
    }
    
    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, String[] interfaces) {
        if (superName != null && superName.equals("android/os/AsyncTask")) {
            interfaces = TraceClassDecorator.addInterface(interfaces);
            super.visit(version, access, name, signature, superName, interfaces);
            this.instrument = true;
            this.log.d("Rewriting " + this.context.getClassName());
            this.context.markModified();
        }
        else {
            super.visit(version, access, name, signature, superName, interfaces);
        }
    }

    @Override
    public void visitEnd() {
        if (this.instrument) {
            final TraceClassDecorator decorator = new TraceClassDecorator(this);
            decorator.addTraceField();
            decorator.addTraceInterface(Type.getObjectType(this.context.getClassName()));
            this.log.d("Added Trace object and interface to " + this.context.getClassName());
        }
        super.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (this.instrument) {
            if (AsyncTaskClassVisitor.traceMethodMap.containsKey(name) && AsyncTaskClassVisitor.traceMethodMap.get(name).equals(desc)) {
                final TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(methodVisitor, access, name, desc, this.context);
                traceMethodVisitor.setUnloadContext();
                return traceMethodVisitor;
            }
            if (AsyncTaskClassVisitor.endTraceMethodMap.containsKey(name) && AsyncTaskClassVisitor.endTraceMethodMap.get(name).equals(desc)) {
                final TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(methodVisitor, access, name, desc, this.context);
                return traceMethodVisitor;
            }
        }
        return methodVisitor;
    }

    static {
        traceMethodMap = ImmutableMap.of("doInBackground", "([Ljava/lang/Object;)Ljava/lang/Object;");
        endTraceMethodMap = ImmutableMap.of("onPostExecute", "(Ljava/lang/Object;)V");
    }
}


