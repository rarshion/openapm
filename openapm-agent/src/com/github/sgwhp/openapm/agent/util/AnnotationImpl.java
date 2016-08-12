package com.github.sgwhp.openapm.agent.util;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rarshion on 16/8/12.
 */
public class AnnotationImpl extends AnnotationVisitor {

    private final String name;
    private Map<String, Object> attributes;
    private final AnnotationVisitor avt;

    public AnnotationImpl(final AnnotationVisitor avt, final String name) {
        super(Opcodes.ASM5, avt);
        this.avt = avt;
        this.name = name;
    }

    @Override
    public void visitEnum(final String name, final String desc, final String value) {
        if (this.attributes == null) {
            this.attributes = new HashMap<String, Object>();
        }
        this.attributes.put(name, value);
    }

    @Override
    public void visitEnd() {
    }

    @Override
    public AnnotationVisitor visitArray(final String name) {
        return new ArrayVisitor(avt, name);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        return null;
    }

    @Override
    public void visit(final String name, final Object value) {
        if (this.attributes == null) {
            this.attributes = new HashMap<String, Object>();
        }
        this.attributes.put(name, value);
    }

    public String getName() {
        return this.name;
    }

    public Map<String, Object> getAttributes() {
        return (this.attributes == null) ? Collections.emptyMap() : this.attributes;
    }


    private final class ArrayVisitor extends AnnotationVisitor {
        private final String name;
        private final ArrayList<Object> values;

        public ArrayVisitor(final AnnotationVisitor avt, final String name) {
            super(Opcodes.ASM5, avt);
            this.values = new ArrayList<Object>();
            this.name = name;
        }

        @Override
        public void visit(final String name, final Object value) {
            this.values.add(value);
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String arg0, final String arg1) {
            return null;
        }

        @Override
        public AnnotationVisitor visitArray(final String name) {
            return null;
        }

        @Override
        public void visitEnd() {
            AnnotationImpl.this.visit(this.name, this.values.toArray(new String[0]));
        }

        @Override
        public void visitEnum(final String arg0, final String arg1, final String arg2) {
        }
    }
}
