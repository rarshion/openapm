package com.github.sgwhp.openapm.agent.visitor;

import com.github.sgwhp.openapm.agent.util.Log;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Created by huangshunbin on 2016/8/1.
 */

public class MeMethodAdapter extends AdviceAdapter {

    private static final Type THROWABLE_TYPE = Type.getType(Throwable.class);
    private String name;
    private Label methodStartLabel;
    private String pinName;
    private String appId;
    private String module;
    private TransformContext context;
    private Log log;

    public MeMethodAdapter(MethodVisitor mv, int acc, String name, String desc, Log log,TransformContext context) {
        super(ASM5, mv, acc, name, desc);
        this.name = name;
        this.context = context;
        this.log = log;
        this.appId = "hello";
        this.module = "MeMethodAdapter";
        this.pinName = "rarshion";
    }

    @Override
    protected void onMethodEnter() {
        log.d("-----MeMethodAdapter-------onMethodEnter");
        super.onMethodEnter();
        onMethodEnter_internal();
        methodStartLabel = new Label();
        mv.visitLabel(methodStartLabel);
    }

    private void onMethodEnter_internal() {
        log.d("-----MeMethodAdapter-------onMethodEnter_internal");
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitVarInsn(LSTORE, 1);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 3);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        // 这个指令是调用系列指令中的一个。其目的是调用对象类的方法。后面需要给上父类的方法完整签名。
        // “#8”的意思是 .class 文件常量表中第8个元素。值为：“java/lang/Object."<init>"
        // :()V”。结合ALOAD_0。这两个指令可以翻译为：“super()”。其含义是调用自己的父类构造方法。
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitLdcInsn("start: ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(LLOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    private void onMethodExit_internal() {
        log.d("-----MeMethodAdapter-------onMethodExit_internal");
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitVarInsn(LSTORE, 4);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitLdcInsn("end: ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(LLOAD, 4);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitLdcInsn( "[" + appId + "<->" + module + "<->" + pinName + "]\n" + "time cost :");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(LLOAD, 4);
        mv.visitVarInsn(LLOAD, 1);
        mv.visitInsn(LSUB);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
        mv.visitLdcInsn(" has error: ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Z)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        log.d("-----MeMethodAdapter-------visitMaxs");
        Label endFinallyLabel = new Label();
        mv.visitTryCatchBlock(methodStartLabel, endFinallyLabel, endFinallyLabel, THROWABLE_TYPE.getInternalName());
        mv.visitLabel(endFinallyLabel);
        mv.visitInsn(ICONST_1);
        mv.visitVarInsn(ISTORE, 3);
        onFinally(ATHROW);
        visitInsn(ATHROW);
        mv.visitMaxs(maxStack, maxLocals);
    }

    @Override
    protected void onMethodExit(int opcode) {
        log.d("-----MeMethodAdapter-------onMethodExit");
        if (opcode != ATHROW) {
            onFinally(opcode);
        }
        this.context.markModified();
    }

    private void onFinally(int opcode) {
        log.d("-----MeMethodAdapter-------onFinally");
        onMethodExit_internal();
    }


}
