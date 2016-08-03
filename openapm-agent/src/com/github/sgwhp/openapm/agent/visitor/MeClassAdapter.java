package com.github.sgwhp.openapm.agent.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by user on 2016/8/1.
 */
public class MeClassAdapter extends ClassVisitor {

    private TransformContext context;

    public MeClassAdapter(ClassVisitor classVisitor, TransformContext context) {
        super(Opcodes.ASM5, classVisitor);
        this.context = context;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if(name.equals("calculate"))
            return new MeMethodAdapter(super.visitMethod(access, name, desc, signature, exceptions),
                access, name, desc, this.context.getLog(), context);
        else
            return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
