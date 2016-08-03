package com.github.sgwhp.openapm.agent.visitor;

import com.github.sgwhp.openapm.agent.*;
import com.github.sgwhp.openapm.agent.util.Log;

import java.util.*;

/**
 * Created by wuhongping on 15-11-23.
 */
public class TransformContext {
    private final TransformConfig config;
    private final Log log;
    private boolean modified;
    private String className;
    private String superClassName;

    private final ArrayList<String> tags;
    private HashMap<String, String> tracedMethods;
    private HashMap<String, String> skippedMethods;
    private final HashMap<String, ArrayList<String>> tracedMethodParameters;

    public TransformContext(TransformConfig config, Log log) {
        this.config = config;
        this.log = log;

        this.tags = new ArrayList<String>();
        this.tracedMethodParameters = new HashMap<String, ArrayList<String>>();
        this.tracedMethods = new HashMap<String, String>();
        this.skippedMethods = new HashMap<String, String>();
    }

    public Log getLog() {
        return this.log;
    }

    public void reset() {
        this.modified = false;
        this.className = null;
        this.superClassName = null;

        this.tags.clear();
    }

    public void markModified() {
        modified = true;
    }

    public boolean isClassModified() {
        return modified;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public String getFriendlyClassName() {
        return className.replaceAll("/", ".");
    }

    public String getFriendlySuperClassName() {
        return superClassName.replaceAll("/", ".");
    }

    public String getSimpleClassName() {
        if (className.contains("/"))
            return className.substring(className.lastIndexOf("/") + 1);
        return className;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public ClassData newClassData(byte[] data) {
        return new ClassData(data, isClassModified());
    }

    public HashSet<String> getExceptions(){
        return config.getExceptions();
    }

    public String getTargetPackage(){
        return config.getTargetPackage();
    }

    public void addTracedMethod(final String name, final String desc) {
        this.log.d("Will trace method " + this.className + "#" + name + ":" + desc + " as requested");
        this.tracedMethods.put(this.className + "#" + name, desc);
    }

    public void addSkippedMethod(final String name, final String desc) {
        this.log.d("Will skip all tracing in method " + this.className + "#" + name + ":" + desc + " as requested");
        this.skippedMethods.put(this.className + "#" + name, desc);
    }

    public void addTracedMethodParameter(final String methodName, final String parameterName, final String parameterClass, final String parameterValue) {
        this.log.d("Adding traced method parameter " + parameterName + " for method " + methodName);
        final String name = this.className + "#" + methodName;
        if (!this.tracedMethodParameters.containsKey(name)) {
            this.tracedMethodParameters.put(name, new ArrayList<String>());
        }
        final ArrayList<String> methodParameters = this.tracedMethodParameters.get(name);
        methodParameters.add(parameterName);
        methodParameters.add(parameterClass);
        methodParameters.add(parameterValue);
    }

    public ArrayList<String> getTracedMethodParameters(final String methodName) {
        return this.tracedMethodParameters.get(this.className + "#" + methodName);
    }

    public boolean isTracedMethod(final String name, final String desc) {
        return this.searchMethodMap(this.tracedMethods, name, desc);
    }

    public boolean isSkippedMethod(final String name, final String desc) {
        return this.searchMethodMap(this.skippedMethods, name, desc);
    }

    private boolean searchMethodMap(final Map<String, String> map, final String name, final String desc) {
        final String descToMatch = map.get(this.className + "#" + name);
        return descToMatch != null && desc.equals(desc);
    }

    public List<String> getTags() {
        return this.tags;
    }

    public boolean hasTag(final String tag) {
        return this.tags.contains(tag);
    }

    /*
    static {
        ANDROID_8_MISSING_CLASS_WHITE_LIST = new String[] { "android.view.View$AccessibilityDelegate", "android.view.accessibility.AccessibilityNodeProvider" };
        MISSING_CLASS_WHITE_LIST = new HashMap<Integer, Set<String>>() {
            {
                ((HashMap<Integer, HashSet<String>>)this).put(8, new HashSet<String>(Arrays.asList(InstrumentationContext.ANDROID_8_MISSING_CLASS_WHITE_LIST)));
            }
        };
    }
    */

}
