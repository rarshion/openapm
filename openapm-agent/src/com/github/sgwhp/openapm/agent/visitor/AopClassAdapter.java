package com.github.sgwhp.openapm.agent.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by rarshion on 2016/8/5.
 */
public class AopClassAdapter extends ClassVisitor {

    private TransformContext context;

    public AopClassAdapter(ClassVisitor classVisitor, TransformContext context) {
        super(Opcodes.ASM5, classVisitor);
        this.context = context;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.startsWith("test")) {
            this.context.getLog().d("AopClassAdapter");
            mv = new AopMethodAdapter(name, mv, context);
        }
        return mv;
    }
}
