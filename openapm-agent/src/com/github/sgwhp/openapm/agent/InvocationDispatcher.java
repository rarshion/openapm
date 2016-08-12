package com.github.sgwhp.openapm.agent;

import com.github.sgwhp.openapm.agent.util.Log;
import com.github.sgwhp.openapm.agent.util.StreamUtil;
import com.github.sgwhp.openapm.agent.visitor.*;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;

/**
 * Created by wuhongping on 15-11-18.
 */
public class InvocationDispatcher implements InvocationHandler {
    private final TransformContext context;
    private final TransformConfig config;
    private final Log log;
    HashMap<String, InvocationHandler> invocationHandlerFactory = new HashMap<>();

    public InvocationDispatcher(Log log) throws ClassNotFoundException {
        config = new TransformConfig(log);
        context = new TransformContext(config, log);
        this.log = log;
        invocationHandlerFactory.put(TransformAgent.genDispatcherKey("java/lang/ProcessBuilder", "start")
                , new ProcessBuilderInvocationHandler(this, log));
        invocationHandlerFactory.put(TransformAgent.genDispatcherKey("com/android/dx/command/dexer/Main", "processClass")
                , new DexerMainInvocationHandler(this, log));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        @SuppressWarnings("SuspiciousMethodCalls")
        InvocationHandler invocationHandler = invocationHandlerFactory.get(proxy);
        if (invocationHandler == null) {
            log.e("Unsupported transform target: " + proxy);
            return null;
        }
        try {
            return invocationHandler.invoke(proxy, method, args);
        } catch (Exception e) {
            log.e("Error:" + e.getMessage(), e);
        }
        return null;
    }

    private boolean skip(String className) {
        for (String str : TransformAgent.skip) {
            if (className.contains(str)) return true;
        }
        return false;
    }

    public ClassData transform(byte[] classByte) {
        String className = "an unknown class";
        try {
            ClassReader cr = new ClassReader(classByte);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            context.reset();
            cr.accept(new InitContextClassVisitor(context, log), ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG | ClassReader.SKIP_CODE);
            className = this.context.getClassName();//获取当前改写的类名
            //log.d("invoke transform: " + className);
            ClassVisitor cv = cw;
            if (skip(context.getClassName())) // monitor不需要改写
                return null;


            //cv = new PrefilterClassVisitor(cv, this.context, this.log);//进行最先改写

            if(className.startsWith("com/github/sgwhp/openapm/sample/Instrumentation")){
                cv = new WrapMethodClassVisitor(cv, this.context, this.log);
            }

            if(className.startsWith("com/github/sgwhp/openapm/sample/Hello")){
                log.d("invoke transform: Hello" + className);
                cv = new AopClassAdapter(cv, context);
            }

/*
            if (this.context.getClassName().startsWith("com/github/sgwhp/openapm/sample")) {
                //cv = new NewRelicClassVisitor(cv, this.context, this.log);
            }
            else if (this.context.getClassName().startsWith("android/support/")) {
                //cv = new ActivityClassVisitor(cv, this.context, this.log);
            }
            else {
                log.d("!!!!!!");
                if (this.isExcludedPackage(this.context.getClassName())) {
                    return null;
                }
                //cv = new AnnotatingClassVisitor(cv, this.context, this.log);
                //cv = new ActivityClassVisitor(cv, this.context, this.log);
                //cv = new AsyncTaskClassVisitor(cv, this.context, this.log);
                //cv = new TraceAnnotationClassVisitor(cv, this.context, this.log);
                cv = new WrapMethodClassVisitor(cv, this.context, this.log);
            }

*/

/*
            if (!this.context.hasTag("Lcom/github/sgwhp/openapm/sample/instrumentation/Instrumented;")) {
                //ClassVisitor cv = cw;
                if (this.context.getClassName().startsWith("com/github/sgwhp/openapm/sample")) {
                    cv = new NewRelicClassVisitor(cv, this.context, this.log);
                }
                else if (this.context.getClassName().startsWith("android/support/")) {
                    //cv = new ActivityClassVisitor(cv, this.context, this.log);
                }
                else {
                    if (this.isExcludedPackage(this.context.getClassName())) {
                        return null;
                    }
                    //cv = new AnnotatingClassVisitor(cv, this.context, this.log);
                    //cv = new ActivityClassVisitor(cv, this.context, this.log);
                    //cv = new AsyncTaskClassVisitor(cv, this.context, this.log);
                    //cv = new TraceAnnotationClassVisitor(cv, this.context, this.log);
                    cv = new WrapMethodClassVisitor(cv, this.context, this.log);
                }
                //cv = new ContextInitializationClassVisitor(cv, this.context);
                //cr.accept(cv, 12);
            }
            else {
                this.log.d(MessageFormat.format("[{0}] class is already instrumented! skipping ...", this.context.getFriendlyClassName()));
            }
*/

            //if (skip(context.getClassName()))
                //return null;
            //ClassVisitor cv = cw;

            /*
            if (context.getTargetPackage() == null || className.startsWith(context.getTargetPackage())) {
                //log.d("invoke from ExceptionLogClassAdapter");
                cv = new ExceptionLogClassAdapter(cw, context);
            }else{
                //log.e("no invoke transform: ExceptionLogClassAdapter");
            }
            if (className.startsWith("com/github/sgwhp/openapm/sample/testMesurement")) {
                log.d("invoke transform: testMesurement" + className);
                cv = new MeClassAdapter(cv, context);
            } else{
                //log.e("no invoke transform: testMesurement");
            }

            //cv = new AnnotatingClassVisitor(cv, this.context, this.log);
            //cv = new ActivityClassVisitor(cv, this.context, this.log);
            //cv = new AsyncTaskClassVisitor(cv, this.context, this.log);
            //cv = new TraceAnnotationClassVisitor(cv, this.context, this.log);
            //cv = new WrapMethodClassVisitor(cv, this.context, this.log);
            */

            cr.accept(new ContextClassVisitor(cv, context), ClassReader.EXPAND_FRAMES | ClassReader.SKIP_FRAMES);
            //将转换的类字节写到文件中以便观察
            StreamUtil.writeToFile(cw.toByteArray(), cv.getClass().toString());
            return context.newClassData(cw.toByteArray());

        } catch (TransformedException e) {
            return null;
        } catch (Exception e) {
            log.e("An error occurred while transforming " + className
                    + ".\n" + e.getMessage(), e);
        }
        return new ClassData(classByte, false);
    }

    public TransformContext getContext() {
        return context;
    }


    private boolean isExcludedPackage(final String packageName) {
        for (final String name : TransformAgent.exclude_packages) {
            if (packageName.contains(name)) {
                return true;
            }
        }
        return false;
    }



}
