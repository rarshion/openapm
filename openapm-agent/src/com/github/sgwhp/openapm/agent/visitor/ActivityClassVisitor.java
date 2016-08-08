package com.github.sgwhp.openapm.agent.visitor;

import com.github.sgwhp.openapm.agent.util.Log;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * Created by user on 2016/8/3.
 */
public class ActivityClassVisitor extends EventHookClassVisitor{

    static final ImmutableSet<String> ACTIVITY_CLASS_NAMES;
    static final Type applicationStateMonitorType;
    public static final ImmutableMap<String, String> traceMethodMap;
    public static final ImmutableSet<String> startTracingOn;

    public ActivityClassVisitor(final ClassVisitor cv, final TransformContext context, final Log log) {
        super(cv, context, log, ActivityClassVisitor.ACTIVITY_CLASS_NAMES, ImmutableMap.of(new Method("onStart", "()V"), new Method("activityStarted", "()V"), new Method("onStop", "()V"), new Method("activityStopped", "()V")));
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, String[] interfaces) {
        if (this.baseClasses.contains(superName)) {
            interfaces = TraceClassDecorator.addInterface(interfaces);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    protected void injectCodeIntoMethod(final GeneratorAdapter generatorAdapter, final Method method, final Method monitorMethod) {
        generatorAdapter.invokeStatic(ActivityClassVisitor.applicationStateMonitorType, new Method("getInstance", ActivityClassVisitor.applicationStateMonitorType, new Type[0]));
        generatorAdapter.invokeVirtual(ActivityClassVisitor.applicationStateMonitorType, monitorMethod);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        if (ActivityClassVisitor.ACTIVITY_CLASS_NAMES.contains(this.context.getClassName())) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
        if (this.instrument && ActivityClassVisitor.traceMethodMap.containsKey(name) && ActivityClassVisitor.traceMethodMap.get(name).equals(desc)) {
            final MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
            final TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(methodVisitor, access, name, desc, this.context);
            if (ActivityClassVisitor.startTracingOn.contains(name)) {
                traceMethodVisitor.setStartTracing();
            }
            return traceMethodVisitor;
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    static {
        ACTIVITY_CLASS_NAMES = ImmutableSet.of("android/app/Activity", "android/app/Fragment", "android/accounts/AccountAuthenticatorActivity", "android/app/ActivityGroup", "android/app/TabActivity", "android/app/AliasActivity", "android/app/ExpandableListActivity", "android/app/ListActivity", "android/app/LauncherActivity", "android/preference/PreferenceActivity", "android/app/NativeActivity", "android/support/v4/app/FragmentActivity", "android/support/v4/app/Fragment", "android/support/v4/app/DialogFragment", "android/support/v4/app/ListFragment", "android/support/v7/app/ActionBarActivity");
        applicationStateMonitorType = Type.getObjectType("com/newrelic/agent/android/background/ApplicationStateMonitor");
        traceMethodMap = ImmutableMap.of("onCreate", "(Landroid/os/Bundle;)V", "onCreateView", "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;");
        startTracingOn = ImmutableSet.of("onCreate");
    }
}



