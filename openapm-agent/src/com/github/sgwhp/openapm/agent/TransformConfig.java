package com.github.sgwhp.openapm.agent;

import com.github.sgwhp.openapm.agent.util.Log;
import com.github.sgwhp.openapm.agent.util.StreamUtil;

import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by wuhongping on 15-11-23.
 */
public class TransformConfig {
    public static final String EXCEPTION = "EXCEPTION:";
    public static final String TARGET_PACKAGE = "targetPackage";
    private final HashSet<String> targetException;
    private final String targetPackage;

    public static final String WRAP_METHOD_IDENTIFIER = "WRAP_METHOD:";
    public static final String REPLACE_CALL_SITE_IDENTIFIER = "REPLACE_CALL_SITE:";

    private final Map<ClassMethod, ClassMethod> methodWrappers;
    private final Map<String, Collection<ClassMethod>> callSiteReplacements;

    public TransformConfig(Log log)
            throws ClassNotFoundException {
        Map<String, String> properties = parseProperties(log);
        targetException = parseException(properties, log);
        targetPackage = parseTargetPackage(properties, log);

        this.methodWrappers = getMethodWrappers(properties, log);
        this.callSiteReplacements = getCallSiteReplacements(properties, log);
    }

    public ClassMethod getMethodWrapper(final ClassMethod method) {
        return this.methodWrappers.get(method);
    }

    public String getTargetPackage(){
        return targetPackage;
    }

    private static String parseTargetPackage(Map<String, String> properties, Log log){
        return properties.get(TARGET_PACKAGE);
    }

    public HashSet<String> getExceptions(){
        return targetException;
    }

    private static HashSet<String> parseException(Map<String, String> properties, Log log){
        HashSet<String> result = new HashSet<>();
        properties.entrySet().stream().filter(entry -> entry.getKey().startsWith("")).forEach(
                entry -> result.add(entry.getKey().substring(EXCEPTION.length())));
        return result;
    }

    private static Map parseProperties(Log log) {
        Properties properties = new Properties();
        URL url = TransformConfig.class.getResource("/config.properties");
        if (url == null) {
            log.e("Unable to find the type map");
            System.exit(1);
        }
        InputStream is = null;
        try {
            is = url.openStream();
            properties.load(is);
        } catch (Exception e) {
            log.e("Unable to read the config file", e);
            System.exit(1);
        } finally {
            StreamUtil.closeInputStreamIgnoreException(is);
        }
        return properties;
    }

    public Collection<ClassMethod> getCallSiteReplacements(final String className, final String methodName, final String methodDesc) {
        final ArrayList<ClassMethod> methods = new ArrayList<ClassMethod>();
        Collection<ClassMethod> matches = this.callSiteReplacements.get(MessageFormat.format("{0}:{1}", methodName, methodDesc));
        if (matches != null) {
            methods.addAll(matches);
        }
        matches = this.callSiteReplacements.get(MessageFormat.format("{0}.{1}:{2}", className, methodName, methodDesc));
        if (matches != null) {
            methods.addAll(matches);
        }
        return methods;
    }

    private static Map<ClassMethod, ClassMethod> getMethodWrappers(final Map<String, String> remappings, final Log log) throws ClassNotFoundException {
        final HashMap<ClassMethod, ClassMethod> methodWrappers = new HashMap<ClassMethod, ClassMethod>();
        for (final Map.Entry<String, String> entry : remappings.entrySet()) {
            if (entry.getKey().startsWith("WRAP_METHOD:")) {
                final String originalSig = entry.getKey().substring("WRAP_METHOD:".length());
                final ClassMethod origClassMethod = ClassMethod.getClassMethod(originalSig);
                final ClassMethod wrappingMethod = ClassMethod.getClassMethod(entry.getValue());
                methodWrappers.put(origClassMethod, wrappingMethod);
            }
        }
        return methodWrappers;
    }


    private static Map<String, Collection<ClassMethod>> getCallSiteReplacements(final Map<String, String> remappings, final Log log) throws ClassNotFoundException {
        final HashMap<String, Set<ClassMethod>> temp = new HashMap<String, Set<ClassMethod>>();
        for (final Map.Entry<String, String> entry : remappings.entrySet()) {
            if (entry.getKey().startsWith("REPLACE_CALL_SITE:")) {
                final String originalSig = entry.getKey().substring("REPLACE_CALL_SITE:".length());
                if (originalSig.contains(".")) {
                    final ClassMethod origClassMethod = ClassMethod.getClassMethod(originalSig);
                    final ClassMethod replacement = ClassMethod.getClassMethod(entry.getValue());
                    final String key = MessageFormat.format("{0}.{1}:{2}", origClassMethod.getClassName(), origClassMethod.getMethodName(), origClassMethod.getMethodDesc());
                    Set<ClassMethod> replacements = temp.get(key);
                    if (replacements == null) {
                        replacements = new HashSet<ClassMethod>();
                        temp.put(key, replacements);
                    }
                    replacements.add(replacement);
                }
                else {
                    final String[] nameDesc = originalSig.split(":");
                    final int paren = originalSig.indexOf("(");
                    final String methodName = originalSig.substring(0, paren);
                    final String methodDesc = originalSig.substring(paren);
                    final String key2 = MessageFormat.format("{0}:{1}", methodName, methodDesc);
                    final ClassMethod replacement2 = ClassMethod.getClassMethod(entry.getValue());
                    Set<ClassMethod> replacements2 = temp.get(key2);
                    if (replacements2 == null) {
                        replacements2 = new HashSet<ClassMethod>();
                        temp.put(key2, replacements2);
                    }
                    replacements2.add(replacement2);
                }
            }
        }
        final HashMap<String, Collection<ClassMethod>> callSiteReplacements = new HashMap<String, Collection<ClassMethod>>();
        for (final Map.Entry<String, Set<ClassMethod>> entry2 : temp.entrySet()) {
            callSiteReplacements.put(entry2.getKey(), entry2.getValue());
        }
        return callSiteReplacements;
    }

}
