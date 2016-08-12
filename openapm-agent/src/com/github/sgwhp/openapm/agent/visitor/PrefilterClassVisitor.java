package com.github.sgwhp.openapm.agent.visitor;


import com.github.sgwhp.openapm.agent.util.Log;
import org.objectweb.asm.*;
import java.text.MessageFormat;

/**
 * Created by rarshion on 16/8/11.
 */
public class PrefilterClassVisitor extends ClassVisitor {

    private static final String TRACE_ANNOTATION_CLASSPATH = "Lcom/github/sgwhp/openapm/instrumentation/Trace;";
    private static final String SKIP_TRACE_ANNOTATION_CLASSPATH = "Lcom/github/sgwhp/openapm/instrumentation/SkipTrace;";
    private final TransformContext context;
    private final Log log;

    public PrefilterClassVisitor(final ClassVisitor cv, final TransformContext context, final Log log) {
        super(Opcodes.ASM5, cv);
        this.context = context;
        this.log = log;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String sig,
                      final String superName, final String[] interfaces) {
        this.context.setClassName(name);
        this.context.setSuperClassName(superName);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        this.log.d("---PrefilterClassVisitor#visitAnnotation---");
        if (Annotations.isNewRelicAnnotation(desc)) {
            this.log.d("---PrefilterClassVisitor#visitAnnotation Have Tag---");
            this.log.d(MessageFormat.format("[{0}] class has New Relic tag: {1}", this.context.getClassName(), desc));
            this.context.addTag(desc);
        }else{
            this.log.d("---PrefilterClassVisitor#visitAnnotation Have Not Tag---" + this.context.getClassName() + desc);
        }
        return null;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc,
                                     final String signature, final String[] exceptions) {
        this.log.d("---PrefilterClassVisitor#visitMethod---");
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return mv;
        /*
        final MethodVisitor methodVisitor = new MethodVisitor(access) {

            @Override
            public AnnotationVisitor visitAnnotationDefault() {
                return null;
            }

            @Override
            public AnnotationVisitor visitAnnotation(final String annotationDesc, final boolean visible) {
                if (annotationDesc.equals("Lcom/github/sgwhp/openapm/sample/instrumentation/Trace;")) {
                    PrefilterClassVisitor.this.context.getLog().d("---PrefilterClassVisitor addTraceMethod---");
                    PrefilterClassVisitor.this.context.addTracedMethod(name, desc);
                    return new TraceAnnotationVisitor(super.visitAnnotation(annotationDesc, visible),
                            name, PrefilterClassVisitor.this.context);
                }
                if (annotationDesc.equals("Lcom/github/sgwhp/openapm/sample/instrumentation/SkipTrace;")) {
                    PrefilterClassVisitor.this.context.getLog().d("---PrefilterClassVisitor addSkipTraceMethod---");
                    PrefilterClassVisitor.this.context.addSkippedMethod(name, desc);
                    return null;
                }
                return null;
            }

        };
        return methodVisitor;
        */

    }

}
