package com.github.sgwhp.openapm.agent.visitor;

import com.github.sgwhp.openapm.agent.util.Log;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import java.util.*;

/**
 * Created by user on 2016/8/3.
 */
public abstract class EventHookClassVisitor extends ClassVisitor {

    protected final Set<String> baseClasses;
    private final Map<Method, MethodVisitorFactory> methodVisitors;
    protected String superName;
    protected boolean instrument;
    protected final TransformContext context;
    protected final Log log;

    public EventHookClassVisitor(final ClassVisitor cv, final TransformContext context, final Log log, final Set<String> baseClasses, final Map<Method, Method> methodMappings) {
        super(Opcodes.ASM5, cv);
        this.instrument = false;
        this.context = context;
        this.log = log;
        this.baseClasses = Collections.unmodifiableSet((Set<? extends String>)baseClasses);
        this.methodVisitors = new HashMap<Method, MethodVisitorFactory>();
        for (final Map.Entry<Method, Method> entry : methodMappings.entrySet()) {
            this.methodVisitors.put(entry.getKey(), new MethodVisitorFactory(entry.getValue()));
        }
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.superName = superName;
        this.instrument = this.baseClasses.contains(superName);
        if (this.instrument) {
            this.context.markModified();
            this.log.d("Rewriting " + name);
        }
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {

        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (!this.instrument) {
            return mv;
        }

        final Method method = new Method(name, desc);
        final MethodVisitorFactory v = this.methodVisitors.get(method);
        if (v != null) {
            this.methodVisitors.remove(method);
            return v.createMethodVisitor(access, method, mv, false);
        }

        return mv;
    }

    @Override
    public void visitEnd() {
        if (!this.instrument) {
            return;
        }
        for (final Map.Entry<Method, MethodVisitorFactory> entry : this.methodVisitors.entrySet()) {
            MethodVisitor mv = super.visitMethod(4, entry.getKey().getName(), entry.getKey().getDescriptor(), null, null);
            mv = entry.getValue().createMethodVisitor(4, entry.getKey(), mv, true);
            mv.visitCode();
            mv.visitInsn(177);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        super.visitEnd();
    }

    protected abstract void injectCodeIntoMethod(final GeneratorAdapter p0, final Method p1, final Method p2);

    protected class MethodVisitorFactory
    {
        final Method monitorMethod;

        public MethodVisitorFactory(final Method monitorMethod) {
            this.monitorMethod = monitorMethod;
        }

        public MethodVisitor createMethodVisitor(final int access, final Method method, final MethodVisitor mv, final boolean callSuper) {
            return new GeneratorAdapter(access, method, mv) {
                @Override
                public void visitCode() {
                    super.visitCode();
                    if (callSuper) {
                        this.loadThis();
                        for (int i = 0; i < method.getArgumentTypes().length; ++i) {
                            this.loadArg(i);
                        }
                        this.visitMethodInsn(183, EventHookClassVisitor.this.superName, method.getName(), method.getDescriptor());
                    }
                    EventHookClassVisitor.this.injectCodeIntoMethod(this, method, MethodVisitorFactory.this.monitorMethod);
                }
            };
        }
    }


}
