package com.github.sgwhp.openapm.agent;

import org.objectweb.asm.commons.Method;

/**
 * Created by rarshion on 2016/8/3.
 */

public final class ClassMethod  {

    private final String className;
    private final String methodName;
    private final String methodDesc;

    public ClassMethod(final String className, final String methodName, final String methodDesc) {
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    static ClassMethod getClassMethod(final String signature) {
        try {
            int descIndex = signature.lastIndexOf(40);
            String methodDesc;
            if (descIndex == -1) {
                descIndex = signature.length();
                methodDesc = "";
            }
            else {
                methodDesc = signature.substring(descIndex);
            }
            final String beforeMethodDesc = signature.substring(0, descIndex);
            final int methodIndex = beforeMethodDesc.lastIndexOf(46);
            return new ClassMethod(signature.substring(0, methodIndex), signature.substring(methodIndex + 1, descIndex), methodDesc);
        }
        catch (Exception ex) {
            throw new RuntimeException("Error parsing " + signature, ex);
        }
    }

    Method getMethod() {
        return new Method(this.methodName, this.methodDesc);
    }

    public String getClassName() {
        return this.className;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String getMethodDesc() {
        return this.methodDesc;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.className == null) ? 0 : this.className.hashCode());
        result = 31 * result + ((this.methodDesc == null) ? 0 : this.methodDesc.hashCode());
        result = 31 * result + ((this.methodName == null) ? 0 : this.methodName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ClassMethod other = (ClassMethod)obj;
        if (this.className == null) {
            if (other.className != null) {
                return false;
            }
        }
        else if (!this.className.equals(other.className)) {
            return false;
        }
        if (this.methodDesc == null) {
            if (other.methodDesc != null) {
                return false;
            }
        }
        else if (!this.methodDesc.equals(other.methodDesc)) {
            return false;
        }
        if (this.methodName == null) {
            if (other.methodName != null) {
                return false;
            }
        }
        else if (!this.methodName.equals(other.methodName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.className + '.' + this.methodName + this.methodDesc;
    }

}
