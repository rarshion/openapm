package com.github.sgwhp.openapm.agent.visitor;

import com.github.sgwhp.openapm.agent.util.Log;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.UUID;

/**
 * Created by rarshion on 16/8/12.
 */
public class NewRelicClassVisitor extends ClassVisitor {

    private static String buildId;
    private final TransformContext context;
    private final Log log;

    public NewRelicClassVisitor(final ClassVisitor cv, final TransformContext context, final Log log) {
        super(Opcodes.ASM5, cv);
        this.context = context;
        this.log = log;
    }

    public static String getBuildId() {
        if (NewRelicClassVisitor.buildId == null) {
            NewRelicClassVisitor.buildId = UUID.randomUUID().toString();
        }
        return NewRelicClassVisitor.buildId;
    }


    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {

        /*
        if (this.context.getClassName().equals("com/github/sgwhp/openapm/sample/NewRelic") && name.equals("isInstrumented")) {
            return new NewRelicMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
        }
        if (this.context.getClassName().equals("com/github/sgwhp/openapm/sample/harvest/crash/Crash") && name.equals("getBuildId")) {
            return new BuildIdMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
        }
        if (this.context.getClassName().equals("com/github/sgwhp/openapm/sample/AndroidAgentImpl") && name.equals("pokeCanary")) {
            return new CanaryMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
        }
        */


        if (this.context.getClassName().equals("com/github/sgwhp/openapm/sample/NewRelic")) {
            this.log.d("---NewRelicClassVisitor#visitMethod Newrelic---");
            return new NewRelicMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
        }
        if (this.context.getClassName().equals("com/github/sgwhp/openapm/sample/harvest/crash/Crash")) {
            this.log.d("---NewRelicClassVisitor#visitMethod BuildIdMethodVisitor---");
            return new BuildIdMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
        }
        if (this.context.getClassName().equals("com/github/sgwhp/openapm/sample/AndroidAgentImpl")) {
            this.log.d("---NewRelicClassVisitor#visitMethod CanaryMethodVisitor---");
            return new CanaryMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
        }

        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        //if (this.context.getClassName().equals("com/newrelic/agent/android/Agent") && name.equals("VERSION") && !value.equals(RewriterAgent.getVersion())) {
       //     throw new HaltBuildException("New Relic Error: Your agent and class rewriter versions do not match: agent = " + value + " class rewriter = " + RewriterAgent.getVersion() + ".  You probably need to update one of these components.  If you're using gradle and just updated, run gradle -stop to restart the daemon.");
       // }
        return super.visitField(access, name, desc, signature, value);
    }

    private final class BuildIdMethodVisitor extends GeneratorAdapter
    {
        public BuildIdMethodVisitor(final MethodVisitor mv, final int access, final String name, final String desc) {
            super(mv, access, name, desc);
        }

        @Override
        public void visitCode() {
            super.visitLdcInsn(NewRelicClassVisitor.getBuildId());
            super.visitInsn(176);
            NewRelicClassVisitor.this.log.d("[newrelic] Setting build identifier to " + NewRelicClassVisitor.getBuildId());
            NewRelicClassVisitor.this.context.markModified();
        }
    }

    private final class NewRelicMethodVisitor extends GeneratorAdapter
    {
        public NewRelicMethodVisitor(final MethodVisitor mv, final int access, final String name, final String desc) {
            super(mv, access, name, desc);
        }

        @Override
        public void visitCode() {
            super.visitInsn(4);
            super.visitInsn(172);
            NewRelicClassVisitor.this.log.d("[newrelic] Marking NewRelic agent as instrumented");
            NewRelicClassVisitor.this.context.markModified();
        }
    }

    private final class CanaryMethodVisitor extends GeneratorAdapter
    {
        private boolean foundCanaryAlive;

        public CanaryMethodVisitor(final MethodVisitor mv, final int access, final String name, final String desc) {
            super(mv, access, name, desc);
            this.foundCanaryAlive = false;
        }

        @Override
        public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, boolean flag) {
            if (name.equals("canaryMethod")) {
                this.foundCanaryAlive = true;
            }
        }

        @Override
        public void visitEnd() {
            if (this.foundCanaryAlive) {
                NewRelicClassVisitor.this.log.d("[newrelic] Found canary alive");
            }
            else {
                NewRelicClassVisitor.this.log.d("[newrelic] Evidence of Proguard detected, sending mapping.txt");
                //final Proguard proguard = new Proguard(NewRelicClassVisitor.this.log);
                //proguard.findAndSendMapFile();
            }
        }
    }



}
