package com.github.sgwhp.openapm.sample.Instrumentation;

import com.github.sgwhp.openapm.sample.tracing.TraceMachine;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by user on 2016/8/6.
 */
public class GsonInstrumentation {
    private static final ArrayList<String> categoryParams;

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static String toJson(final Gson gson, final Object src) {
        TraceMachine.enterMethod("Gson#toJson", GsonInstrumentation.categoryParams);
        final String string = gson.toJson(src);
        TraceMachine.exitMethod();
        return string;
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static String toJson(final Gson gson, final Object src, final Type typeOfSrc) {
        TraceMachine.enterMethod("Gson#toJson", GsonInstrumentation.categoryParams);
        final String string = gson.toJson(src, typeOfSrc);
        TraceMachine.exitMethod();
        return string;
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static void toJson(final Gson gson, final Object src, final Appendable writer) throws JsonIOException {
        TraceMachine.enterMethod("Gson#toJson", GsonInstrumentation.categoryParams);
        gson.toJson(src, writer);
        TraceMachine.exitMethod();
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static void toJson(final Gson gson, final Object src, final Type typeOfSrc, final Appendable writer) throws JsonIOException {
        TraceMachine.enterMethod("Gson#toJson", GsonInstrumentation.categoryParams);
        gson.toJson(src, typeOfSrc, writer);
        TraceMachine.exitMethod();
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static void toJson(final Gson gson, final Object src, final Type typeOfSrc, final JsonWriter writer) throws JsonIOException {
        TraceMachine.enterMethod("Gson#toJson", GsonInstrumentation.categoryParams);
        gson.toJson(src, typeOfSrc, writer);
        TraceMachine.exitMethod();
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static String toJson(final Gson gson, final JsonElement jsonElement) {
        TraceMachine.enterMethod("Gson#toJson", GsonInstrumentation.categoryParams);
        final String string = gson.toJson(jsonElement);
        TraceMachine.exitMethod();
        return string;
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static void toJson(final Gson gson, final JsonElement jsonElement, final Appendable writer) throws JsonIOException {
        TraceMachine.enterMethod("Gson#toJson", GsonInstrumentation.categoryParams);
        gson.toJson(jsonElement, writer);
        TraceMachine.exitMethod();
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static void toJson(final Gson gson, final JsonElement jsonElement, final JsonWriter writer) throws JsonIOException {
        TraceMachine.enterMethod("Gson#toJson", GsonInstrumentation.categoryParams);
        gson.toJson(jsonElement, writer);
        TraceMachine.exitMethod();
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static <T> T fromJson(final Gson gson, final String json, final Class<T> classOfT) throws JsonSyntaxException {
        TraceMachine.enterMethod("Gson#fromJson", GsonInstrumentation.categoryParams);
        final T object = (T)gson.fromJson(json, (Class)classOfT);
        TraceMachine.exitMethod();
        return object;
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static <T> T fromJson(final Gson gson, final String json, final Type typeOfT) throws JsonSyntaxException {
        TraceMachine.enterMethod("Gson#fromJson", GsonInstrumentation.categoryParams);
        final T object = (T)gson.fromJson(json, typeOfT);
        TraceMachine.exitMethod();
        return object;
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static <T> T fromJson(final Gson gson, final Reader json, final Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
        TraceMachine.enterMethod("Gson#fromJson", GsonInstrumentation.categoryParams);
        final T object = (T)gson.fromJson(json, (Class)classOfT);
        TraceMachine.exitMethod();
        return object;
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static <T> T fromJson(final Gson gson, final Reader json, final Type typeOfT) throws JsonIOException, JsonSyntaxException {
        TraceMachine.enterMethod("Gson#fromJson", GsonInstrumentation.categoryParams);
        final T object = (T)gson.fromJson(json, typeOfT);
        TraceMachine.exitMethod();
        return object;
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static <T> T fromJson(final Gson gson, final JsonReader reader, final Type typeOfT) throws JsonIOException, JsonSyntaxException {
        TraceMachine.enterMethod("Gson#fromJson", GsonInstrumentation.categoryParams);
        final T object = (T)gson.fromJson(reader, typeOfT);
        TraceMachine.exitMethod();
        return object;
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static <T> T fromJson(final Gson gson, final JsonElement json, final Class<T> classOfT) throws JsonSyntaxException {
        TraceMachine.enterMethod("Gson#fromJson", GsonInstrumentation.categoryParams);
        final T object = (T)gson.fromJson(json, (Class)classOfT);
        TraceMachine.exitMethod();
        return object;
    }

    @ReplaceCallSite(scope = "com.google.gson.Gson")
    public static <T> T fromJson(final Gson gson, final JsonElement json, final Type typeOfT) throws JsonSyntaxException {
        TraceMachine.enterMethod("Gson#fromJson", GsonInstrumentation.categoryParams);
        final T object = (T)gson.fromJson(json, typeOfT);
        TraceMachine.exitMethod();
        return object;
    }

    static {
        categoryParams = new ArrayList<String>(Arrays.asList("category", MetricCategory.class.getName(), "JSON"));
    }
}
