package com.github.sgwhp.openapm.agent.visitor;

import com.github.sgwhp.openapm.agent.util.Log;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.text.MessageFormat;

/**
 * Created by user on 2016/8/7.
 */

public class AnnotatingClassVisitor extends ClassVisitor {
    private final TransformContext context;
    private final Log log;

    public AnnotatingClassVisitor(final ClassVisitor cv, final TransformContext context, final Log log) {
        super(Opcodes.ASM5, cv);
        this.context = context;
        this.log = log;
    }

    @Override
    public void visitEnd() {
        if (this.context.isClassModified()) {
            this.context.addUniqueTag("Lcom/github/sgwhp/openapm/sample/instrumentation/Instrumented;");
            super.visitAnnotation("Lcom/github/sgwhp/openapm/sample/instrumentation/Instrumented;", false);
            this.log.d(MessageFormat.format("[{0}] tagging as instrumented", this.context.getFriendlyClassName()));
        }
        super.visitEnd();
    }
}

