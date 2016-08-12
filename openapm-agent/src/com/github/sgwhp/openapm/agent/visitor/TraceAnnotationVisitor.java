package com.github.sgwhp.openapm.agent.visitor;

import com.github.sgwhp.openapm.agent.util.AnnotationImpl;
import com.github.sgwhp.openapm.agent.util.Log;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

/**
 * Created by rarshion on 16/8/12.
 */
public class TraceAnnotationVisitor extends AnnotationImpl {

    final Log log;
    final TransformContext context;

    public TraceAnnotationVisitor(final AnnotationVisitor avt, final String name, final TransformContext context) {
        super(avt, name);
        this.context = context;
        this.log = context.getLog();
    }

    @Override
    public void visitEnum(final String parameterName, final String desc, final String value) {
        super.visitEnum(parameterName, desc, value);
        final String className = Type.getType(desc).getClassName();
        this.context.addTracedMethodParameter(this.getName(), parameterName, className, value);
    }

    @Override
    public void visit(final String parameterName, final Object value) {
        super.visit(parameterName, value);
        final String className = value.getClass().getName();
        this.context.addTracedMethodParameter(this.getName(), parameterName, className, value.toString());
    }


}
