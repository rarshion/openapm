package com.github.sgwhp.openapm.sample.Instrumentation;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.TypedValue;

import com.github.sgwhp.openapm.sample.tracing.TraceMachine;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by user on 2016/8/6.
 */
public class BitmapFactoryInstrumentation {

    private static final ArrayList<String> categoryParams = new ArrayList<String>(Arrays.asList("category", MetricCategory.class.getName(), "IMAGE"));

    @ReplaceCallSite(isStatic = true, scope = "android.graphics.BitmapFactory")
    public static Bitmap decodeFile(final String pathName, final BitmapFactory.Options opts) {
        TraceMachine.enterMethod("BitmapFactory#decodeFile", BitmapFactoryInstrumentation.categoryParams);
        final Bitmap bitmap = BitmapFactory.decodeFile(pathName, opts);
        TraceMachine.exitMethod();
        return bitmap;
    }

    @ReplaceCallSite(isStatic = true, scope = "android.graphics.BitmapFactory")
    public static Bitmap decodeFile(final String pathName) {
        TraceMachine.enterMethod("BitmapFactory#decodeFile", BitmapFactoryInstrumentation.categoryParams);
        final Bitmap bitmap = BitmapFactory.decodeFile(pathName);
        TraceMachine.exitMethod();
        return bitmap;
    }

    @ReplaceCallSite(isStatic = true, scope = "android.graphics.BitmapFactory")
    public static Bitmap decodeResourceStream(final Resources res, final TypedValue value, final InputStream is, final Rect pad, final BitmapFactory.Options opts) {
        TraceMachine.enterMethod("BitmapFactory#decodeResourceStream", BitmapFactoryInstrumentation.categoryParams);
        final Bitmap bitmap = BitmapFactory.decodeResourceStream(res, value, is, pad, opts);
        TraceMachine.exitMethod();
        return bitmap;
    }

    @ReplaceCallSite(isStatic = true, scope = "android.graphics.BitmapFactory")
    public static Bitmap decodeResource(final Resources res, final int id, final BitmapFactory.Options opts) {
        TraceMachine.enterMethod("BitmapFactory#decodeResource", BitmapFactoryInstrumentation.categoryParams);
        final Bitmap bitmap = BitmapFactory.decodeResource(res, id, opts);
        TraceMachine.exitMethod();
        return bitmap;
    }

    @ReplaceCallSite(isStatic = true, scope = "android.graphics.BitmapFactory")
    public static Bitmap decodeResource(final Resources res, final int id) {
        TraceMachine.enterMethod("BitmapFactory#decodeResource", BitmapFactoryInstrumentation.categoryParams);
        final Bitmap bitmap = BitmapFactory.decodeResource(res, id);
        TraceMachine.exitMethod();
        return bitmap;
    }

    @ReplaceCallSite(isStatic = true, scope = "android.graphics.BitmapFactory")
    public static Bitmap decodeByteArray(final byte[] data, final int offset, final int length, final BitmapFactory.Options opts) {
        TraceMachine.enterMethod("BitmapFactory#decodeByteArray", BitmapFactoryInstrumentation.categoryParams);
        final Bitmap bitmap = BitmapFactory.decodeByteArray(data, offset, length, opts);
        TraceMachine.exitMethod();
        return bitmap;
    }

    @ReplaceCallSite(isStatic = true, scope = "android.graphics.BitmapFactory")
    public static Bitmap decodeByteArray(final byte[] data, final int offset, final int length) {
        TraceMachine.enterMethod("BitmapFactory#decodeByteArray", BitmapFactoryInstrumentation.categoryParams);
        final Bitmap bitmap = BitmapFactory.decodeByteArray(data, offset, length);
        TraceMachine.exitMethod();
        return bitmap;
    }

    @ReplaceCallSite(isStatic = true, scope = "android.graphics.BitmapFactory")
    public static Bitmap decodeStream(final InputStream is, final Rect outPadding, final BitmapFactory.Options opts) {
        TraceMachine.enterMethod("BitmapFactory#decodeStream", BitmapFactoryInstrumentation.categoryParams);
        final Bitmap bitmap = BitmapFactory.decodeStream(is, outPadding, opts);
        TraceMachine.exitMethod();
        return bitmap;
    }

    @ReplaceCallSite(isStatic = true, scope = "android.graphics.BitmapFactory")
    public static Bitmap decodeStream(final InputStream is) {
        TraceMachine.enterMethod("BitmapFactory#decodeStream", BitmapFactoryInstrumentation.categoryParams);
        final Bitmap bitmap = BitmapFactory.decodeStream(is);
        TraceMachine.exitMethod();
        return bitmap;
    }

    @ReplaceCallSite(isStatic = true, scope = "android.graphics.BitmapFactory")
    public static Bitmap decodeFileDescriptor(final FileDescriptor fd, final Rect outPadding, final BitmapFactory.Options opts) {
        TraceMachine.enterMethod("BitmapFactory#decodeFileDescriptor", BitmapFactoryInstrumentation.categoryParams);
        final Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd, outPadding, opts);
        TraceMachine.exitMethod();
        return bitmap;
    }

    @ReplaceCallSite(isStatic = true, scope = "android.graphics.BitmapFactory")
    public static Bitmap decodeFileDescriptor(final FileDescriptor fd) {
        TraceMachine.enterMethod("BitmapFactory#decodeFileDescriptor", BitmapFactoryInstrumentation.categoryParams);
        final Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd);
        TraceMachine.exitMethod();
        return bitmap;
    }

}
