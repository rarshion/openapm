package com.github.sgwhp.openapm.agent.visitor;

import com.github.sgwhp.openapm.agent.util.Log;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by user on 2016/8/3.
 */


public class TraceAnnotationClassVisitor extends ClassVisitor {

   private final TransformContext context;
   private final Log log;

   public TraceAnnotationClassVisitor(ClassVisitor classVisitor, final TransformContext context, final Log log) {
       super(Opcodes.ASM5, classVisitor);
       this.context = context;
       this.log = log;
   }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if(this.context.isTracedMethod(name, desc) & !this.context.isSkippedMethod(name, desc)) {
            this.context.markModified();
            return new TraceMethodVisitor(methodVisitor, access, name, desc, this.context);
        } else{
            this.log.d("----no enter TraceAnnotationClassVisitor---");
        }
        return methodVisitor;
    }

}

