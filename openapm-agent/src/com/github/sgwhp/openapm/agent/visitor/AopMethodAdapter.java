package com.github.sgwhp.openapm.agent.visitor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;

/**
 * Created by rarshion on 2016/8/5.
 */
public class AopMethodAdapter extends MethodVisitor {

    private TransformContext context;

    public AopMethodAdapter(String name, MethodVisitor mv, TransformContext context) {
        super(Opcodes.ASM5, mv);
        this.context = context;
    }

    @Override
    public void visitCode() {
        this.context.getLog().d("AopMethodAdapter visitCode");
        super.visitCode();
        this.visitMethodInsn(INVOKESTATIC, "com/github/sgwhp/openapm/sample/AopInteceptor", "before", "()V", false);
    }

    @Override
    public void visitInsn(int opcode) {
        this.context.getLog().d("AopMethodAdapter visitInsn");
        if (opcode >= 172 && opcode <= 177)// 在返回之前安插after代码。
            this.visitMethodInsn(INVOKESTATIC, "com/github/sgwhp/openapm/sample/AopInteceptor", "after", "()V", false);
        this.context.markModified();//这句话一定要加，不然重写类会失效
        super.visitInsn(opcode);
    }

}
