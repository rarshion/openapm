package com.github.sgwhp.openapm.sample.Instrumentation;

/**
 * Created by rarshion on 16/8/11.
 */
import java.lang.annotation.*;

@Target({ ElementType.METHOD })
public @interface WrapReturn {
    String className();

    String methodName();

    String methodDesc();
}

/*
        REPLACE_CALL_SITE\:android/graphics/BitmapFactory.decodeResource(Landroid/content/res/Resources;ILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;=com/github/sgwhp/openapm/sample/instrumentation/BitmapFactoryInstrumentation.decodeResource(Landroid/content/res/Resources;ILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
        REPLACE_CALL_SITE\:com/google/gson/Gson.toJson(Ljava/lang/Object;Ljava/lang/Appendable;)V=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.toJson(Lcom/google/gson/Gson;Ljava/lang/Object;Ljava/lang/Appendable;)V
        REPLACE_CALL_SITE\:query(ZLjava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.query(Landroid/database/sqlite/SQLiteDatabase;ZLjava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;
        REPLACE_CALL_SITE\:com/google/gson/Gson.fromJson(Lcom/google/gson/stream/JsonReader;Ljava/lang/reflect/Type;)Ljava/lang/Object;=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.fromJson(Lcom/google/gson/Gson;Lcom/google/gson/stream/JsonReader;Ljava/lang/reflect/Type;)Ljava/lang/Object;
        REPLACE_CALL_SITE\:rawQueryWithFactory(Landroid/database/sqlite/SQLiteDatabase$CursorFactory;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.rawQueryWithFactory(Landroid/database/sqlite/SQLiteDatabase;Landroid/database/sqlite/SQLiteDatabase$CursorFactory;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;
        REPLACE_CALL_SITE\:setClient(Lretrofit/client/Client;)Lretrofit/RestAdapter$Builder;=com/github/sgwhp/openapm/sample/instrumentation/retrofit/RetrofitInstrumentation.setClient(Lretrofit/RestAdapter$Builder;Lretrofit/client/Client;)Lretrofit/RestAdapter$Builder;
        REPLACE_CALL_SITE\:replaceOrThrow(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.replaceOrThrow(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J
        REPLACE_CALL_SITE\:executeOnExecutor(Ljava/util/concurrent/Executor;[Ljava/lang/Object;)Landroid/os/AsyncTask;=com/github/sgwhp/openapm/sample/instrumentation/AsyncTaskInstrumentation.executeOnExecutor(Landroid/os/AsyncTask;Ljava/util/concurrent/Executor;[Ljava/lang/Object;)Landroid/os/AsyncTask;
        REPLACE_CALL_SITE\:rawQueryWithFactory(Landroid/database/sqlite/SQLiteDatabase$CursorFactory;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.rawQueryWithFactory(Landroid/database/sqlite/SQLiteDatabase;Landroid/database/sqlite/SQLiteDatabase$CursorFactory;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;
        REPLACE_CALL_SITE\:com/google/gson/Gson.toJson(Ljava/lang/Object;Ljava/lang/reflect/Type;)Ljava/lang/String;=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.toJson(Lcom/google/gson/Gson;Ljava/lang/Object;Ljava/lang/reflect/Type;)Ljava/lang/String;
        REPLACE_CALL_SITE\:build()Lcom/squareup/okhttp/Request;=com/github/sgwhp/openapm/sample/instrumentation/okhttp2/OkHttp2Instrumentation.build(Lcom/squareup/okhttp/Request$Builder;)Lcom/squareup/okhttp/Request;
        REPLACE_CALL_SITE\:android/graphics/BitmapFactory.decodeFile(Ljava/lang/String;)Landroid/graphics/Bitmap;=com/github/sgwhp/openapm/sample/instrumentation/BitmapFactoryInstrumentation.decodeFile(Ljava/lang/String;)Landroid/graphics/Bitmap;
        REPLACE_CALL_SITE\:execute([Ljava/lang/Object;)Landroid/os/AsyncTask;=com/github/sgwhp/openapm/sample/instrumentation/AsyncTaskInstrumentation.execute(Landroid/os/AsyncTask;[Ljava/lang/Object;)Landroid/os/AsyncTask;
        REPLACE_CALL_SITE\:execute(Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;Lorg/apache/http/client/ResponseHandler;Lorg/apache/http/protocol/HttpContext;)Ljava/lang/Object;=com/github/sgwhp/openapm/sample/instrumentation/HttpInstrumentation.execute(Lorg/apache/http/client/HttpClient;Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;Lorg/apache/http/client/ResponseHandler;Lorg/apache/http/protocol/HttpContext;)Ljava/lang/Object;
        REPLACE_CALL_SITE\:org/json/JSONObject.toString()Ljava/lang/String;=com/github/sgwhp/openapm/sample/instrumentation/JSONObjectInstrumentation.toString(Lorg/json/JSONObject;)Ljava/lang/String;
        REPLACE_CALL_SITE\:android/graphics/BitmapFactory.decodeByteArray([BIILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;=com/github/sgwhp/openapm/sample/instrumentation/BitmapFactoryInstrumentation.decodeByteArray([BIILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
        WRAP_METHOD\:java/net/URL.openConnection()Ljava/net/URLConnection;=com/github/sgwhp/openapm/sample/instrumentation/HttpInstrumentation.openConnection(Ljava/net/URLConnection;)Ljava/net/URLConnection;
        REPLACE_CALL_SITE\:query(Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.query(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;
        REPLACE_CALL_SITE\:android/graphics/BitmapFactory.decodeResource(Landroid/content/res/Resources;I)Landroid/graphics/Bitmap;=com/github/sgwhp/openapm/sample/instrumentation/BitmapFactoryInstrumentation.decodeResource(Landroid/content/res/Resources;I)Landroid/graphics/Bitmap;
        REPLACE_CALL_SITE\:org/json/JSONArray.<init>(Ljava/lang/String;)V=com/github/sgwhp/openapm/sample/instrumentation/JSONArrayInstrumentation.init(Ljava/lang/String;)Lorg/json/JSONArray;
        REPLACE_CALL_SITE\:com/google/gson/Gson.fromJson(Ljava/io/Reader;Ljava/lang/Class;)Ljava/lang/Object;=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.fromJson(Lcom/google/gson/Gson;Ljava/io/Reader;Ljava/lang/Class;)Ljava/lang/Object;
        REPLACE_CALL_SITE\:query(ZLjava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.query(Landroid/database/sqlite/SQLiteDatabase;ZLjava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;
        REPLACE_CALL_SITE\:updateWithOnConflict(Ljava/lang/String;Landroid/content/ContentValues;Ljava/lang/String;[Ljava/lang/String;I)I=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.updateWithOnConflict(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;Landroid/content/ContentValues;Ljava/lang/String;[Ljava/lang/String;I)I
        REPLACE_CALL_SITE\:execute(Lorg/apache/http/client/methods/HttpUriRequest;Lorg/apache/http/protocol/HttpContext;)Lorg/apache/http/HttpResponse;=com/github/sgwhp/openapm/sample/instrumentation/HttpInstrumentation.execute(Lorg/apache/http/client/HttpClient;Lorg/apache/http/client/methods/HttpUriRequest;Lorg/apache/http/protocol/HttpContext;)Lorg/apache/http/HttpResponse;
        WRAP_METHOD\:com/squareup/okhttp/OkHttpClient.open(Ljava/net/URL;Ljava/net/Proxy)Ljava/net/HttpURLConnection;=com/github/sgwhp/openapm/sample/instrumentation/OkHttpInstrumentation.openWithProxy(Ljava/net/HttpURLConnection;)Ljava/net/HttpURLConnection;
        REPLACE_CALL_SITE\:android/graphics/BitmapFactory.decodeStream(Ljava/io/InputStream;)Landroid/graphics/Bitmap;=com/github/sgwhp/openapm/sample/instrumentation/BitmapFactoryInstrumentation.decodeStream(Ljava/io/InputStream;)Landroid/graphics/Bitmap;
        REPLACE_CALL_SITE\:rawQuery(Ljava/lang/String;[Ljava/lang/String;)Landroid/database/Cursor;=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.rawQuery(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;[Ljava/lang/String;)Landroid/database/Cursor;
        REPLACE_CALL_SITE\:execute(Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;)Lorg/apache/http/HttpResponse;=com/github/sgwhp/openapm/sample/instrumentation/HttpInstrumentation.execute(Lorg/apache/http/client/HttpClient;Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;)Lorg/apache/http/HttpResponse;
        REPLACE_CALL_SITE\:queryWithFactory(Landroid/database/sqlite/SQLiteDatabase$CursorFactory;ZLjava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.queryWithFactory(Landroid/database/sqlite/SQLiteDatabase;Landroid/database/sqlite/SQLiteDatabase$CursorFactory;ZLjava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;
        REPLACE_CALL_SITE\:execute(Lorg/apache/http/client/methods/HttpUriRequest;Lorg/apache/http/client/ResponseHandler;Lorg/apache/http/protocol/HttpContext;)Ljava/lang/Object;=com/github/sgwhp/openapm/sample/instrumentation/HttpInstrumentation.execute(Lorg/apache/http/client/HttpClient;Lorg/apache/http/client/methods/HttpUriRequest;Lorg/apache/http/client/ResponseHandler;Lorg/apache/http/protocol/HttpContext;)Ljava/lang/Object;
        REPLACE_CALL_SITE\:com/google/gson/Gson.toJson(Lcom/google/gson/JsonElement;Ljava/lang/Appendable;)V=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.toJson(Lcom/google/gson/Gson;Lcom/google/gson/JsonElement;Ljava/lang/Appendable;)V
        REPLACE_CALL_SITE\:org/json/JSONArray.toString(I)Ljava/lang/String;=com/github/sgwhp/openapm/sample/instrumentation/JSONArrayInstrumentation.toString(Lorg/json/JSONArray;I)Ljava/lang/String;
        REPLACE_CALL_SITE\:insert(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.insert(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J
        REPLACE_CALL_SITE\:android/graphics/BitmapFactory.decodeFileDescriptor(Ljava/io/FileDescriptor;)Landroid/graphics/Bitmap;=com/github/sgwhp/openapm/sample/instrumentation/BitmapFactoryInstrumentation.decodeFileDescriptor(Ljava/io/FileDescriptor;)Landroid/graphics/Bitmap;
        REPLACE_CALL_SITE\:newCall(Lcom/squareup/okhttp/Request;)Lcom/squareup/okhttp/Call;=com/github/sgwhp/openapm/sample/instrumentation/okhttp2/OkHttp2Instrumentation.newCall(Lcom/squareup/okhttp/OkHttpClient;Lcom/squareup/okhttp/Request;)Lcom/squareup/okhttp/Call;
        REPLACE_CALL_SITE\:update(Ljava/lang/String;Landroid/content/ContentValues;Ljava/lang/String;[Ljava/lang/String;)I=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.update(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;Landroid/content/ContentValues;Ljava/lang/String;[Ljava/lang/String;)I
        REPLACE_CALL_SITE\:com/google/gson/Gson.fromJson(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;)Ljava/lang/Object;=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.fromJson(Lcom/google/gson/Gson;Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;)Ljava/lang/Object;
        REPLACE_CALL_SITE\:newBuilder()Lcom/squareup/okhttp/Response$Builder;=com/github/sgwhp/openapm/sample/instrumentation/okhttp2/OkHttp2Instrumentation.newBuilder(Lcom/squareup/okhttp/Response$Builder;)Lcom/squareup/okhttp/Response$Builder;
        REPLACE_CALL_SITE\:insertOrThrow(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.insertOrThrow(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J
        REPLACE_CALL_SITE\:android/graphics/BitmapFactory.decodeFileDescriptor(Ljava/io/FileDescriptor;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;=com/github/sgwhp/openapm/sample/instrumentation/BitmapFactoryInstrumentation.decodeFileDescriptor(Ljava/io/FileDescriptor;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
        REPLACE_CALL_SITE\:rawQuery(Ljava/lang/String;[Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.rawQuery(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;[Ljava/lang/String;Landroid/os/CancellationSignal;)Landroid/database/Cursor;
        WRAP_METHOD\:com/squareup/okhttp/OkUrlFactory.open(Ljava/net/URL;)Ljava/net/HttpURLConnection;=com/github/sgwhp/openapm/sample/instrumentation/OkHttpInstrumentation.urlFactoryOpen(Ljava/net/HttpURLConnection;)Ljava/net/HttpURLConnection;
        REPLACE_CALL_SITE\:android/graphics/BitmapFactory.decodeStream(Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;=com/github/sgwhp/openapm/sample/instrumentation/BitmapFactoryInstrumentation.decodeStream(Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
        REPLACE_CALL_SITE\:com/google/gson/Gson.fromJson(Ljava/lang/String;Ljava/lang/reflect/Type;)Ljava/lang/Object;=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.fromJson(Lcom/google/gson/Gson;Ljava/lang/String;Ljava/lang/reflect/Type;)Ljava/lang/Object;
        REPLACE_CALL_SITE\:body(Lcom/squareup/okhttp/ResponseBody;)Lcom/squareup/okhttp/Response$Builder;=com/github/sgwhp/openapm/sample/instrumentation/okhttp2/OkHttp2Instrumentation.body(Lcom/squareup/okhttp/Response$Builder;Lcom/squareup/okhttp/ResponseBody;)Lcom/squareup/okhttp/Response$Builder;
        REPLACE_CALL_SITE\:com/google/gson/Gson.fromJson(Lcom/google/gson/JsonElement;Ljava/lang/Class;)Ljava/lang/Object;=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.fromJson(Lcom/google/gson/Gson;Lcom/google/gson/JsonElement;Ljava/lang/Class;)Ljava/lang/Object;
        REPLACE_CALL_SITE\:com/google/gson/Gson.toJson(Ljava/lang/Object;)Ljava/lang/String;=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.toJson(Lcom/google/gson/Gson;Ljava/lang/Object;)Ljava/lang/String;
        REPLACE_CALL_SITE\:query(Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.query(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;
        REPLACE_CALL_SITE\:queryWithFactory(Landroid/database/sqlite/SQLiteDatabase$CursorFactory;ZLjava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.queryWithFactory(Landroid/database/sqlite/SQLiteDatabase;Landroid/database/sqlite/SQLiteDatabase$CursorFactory;ZLjava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;
        WRAP_METHOD\:com/squareup/okhttp/OkHttpClient.open(Ljava/net/URL;)Ljava/net/HttpURLConnection;=com/github/sgwhp/openapm/sample/instrumentation/OkHttpInstrumentation.open(Ljava/net/HttpURLConnection;)Ljava/net/HttpURLConnection;
        REPLACE_CALL_SITE\:com/google/gson/Gson.toJson(Ljava/lang/Object;Ljava/lang/reflect/Type;Ljava/lang/Appendable;)V=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.toJson(Lcom/google/gson/Gson;Ljava/lang/Object;Ljava/lang/reflect/Type;Ljava/lang/Appendable;)V
        REPLACE_CALL_SITE\:org/json/JSONObject.<init>(Ljava/lang/String;)V=com/github/sgwhp/openapm/sample/instrumentation/JSONObjectInstrumentation.init(Ljava/lang/String;)Lorg/json/JSONObject;
        REPLACE_CALL_SITE\:android/graphics/BitmapFactory.decodeByteArray([BII)Landroid/graphics/Bitmap;=com/github/sgwhp/openapm/sample/instrumentation/BitmapFactoryInstrumentation.decodeByteArray([BII)Landroid/graphics/Bitmap;
        REPLACE_CALL_SITE\:replace(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.replace(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;)J
        REPLACE_CALL_SITE\:android/graphics/BitmapFactory.decodeFile(Ljava/lang/String;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;=com/github/sgwhp/openapm/sample/instrumentation/BitmapFactoryInstrumentation.decodeFile(Ljava/lang/String;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
        REPLACE_CALL_SITE\:org/json/JSONArray.toString()Ljava/lang/String;=com/github/sgwhp/openapm/sample/instrumentation/JSONArrayInstrumentation.toString(Lorg/json/JSONArray;)Ljava/lang/String;
        REPLACE_CALL_SITE\:execSQL(Ljava/lang/String;[Ljava/lang/Object;)V=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.execSQL(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;[Ljava/lang/Object;)V
        REPLACE_CALL_SITE\:insertWithOnConflict(Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;I)J=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.insertWithOnConflict(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;Ljava/lang/String;Landroid/content/ContentValues;I)J
        REPLACE_CALL_SITE\:com/google/gson/Gson.toJson(Lcom/google/gson/JsonElement;Lcom/google/gson/stream/JsonWriter;)V=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.toJson(Lcom/google/gson/Gson;Lcom/google/gson/JsonElement;Lcom/google/gson/stream/JsonWriter;)V
        REPLACE_CALL_SITE\:org/json/JSONObject.toString(I)Ljava/lang/String;=com/github/sgwhp/openapm/sample/instrumentation/JSONObjectInstrumentation.toString(Lorg/json/JSONObject;I)Ljava/lang/String;
        REPLACE_CALL_SITE\:execute(Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;Lorg/apache/http/protocol/HttpContext;)Lorg/apache/http/HttpResponse;=com/github/sgwhp/openapm/sample/instrumentation/HttpInstrumentation.execute(Lorg/apache/http/client/HttpClient;Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;Lorg/apache/http/protocol/HttpContext;)Lorg/apache/http/HttpResponse;
        REPLACE_CALL_SITE\:com/google/gson/Gson.toJson(Lcom/google/gson/JsonElement;)Ljava/lang/String;=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.toJson(Lcom/google/gson/Gson;Lcom/google/gson/JsonElement;)Ljava/lang/String;
        REPLACE_CALL_SITE\:execSQL(Ljava/lang/String;)V=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.execSQL(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;)V
        REPLACE_CALL_SITE\:execute(Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;Lorg/apache/http/client/ResponseHandler;)Ljava/lang/Object;=com/github/sgwhp/openapm/sample/instrumentation/HttpInstrumentation.execute(Lorg/apache/http/client/HttpClient;Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;Lorg/apache/http/client/ResponseHandler;)Ljava/lang/Object;
        REPLACE_CALL_SITE\:com/google/gson/Gson.toJson(Ljava/lang/Object;Ljava/lang/reflect/Type;Lcom/google/gson/stream/JsonWriter;)V=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.toJson(Lcom/google/gson/Gson;Ljava/lang/Object;Ljava/lang/reflect/Type;Lcom/google/gson/stream/JsonWriter;)V
        REPLACE_CALL_SITE\:com/google/gson/Gson.fromJson(Ljava/io/Reader;Ljava/lang/reflect/Type;)Ljava/lang/Object;=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.fromJson(Lcom/google/gson/Gson;Ljava/io/Reader;Ljava/lang/reflect/Type;)Ljava/lang/Object;
        REPLACE_CALL_SITE\:execute(Lorg/apache/http/client/methods/HttpUriRequest;)Lorg/apache/http/HttpResponse;=com/github/sgwhp/openapm/sample/instrumentation/HttpInstrumentation.execute(Lorg/apache/http/client/HttpClient;Lorg/apache/http/client/methods/HttpUriRequest;)Lorg/apache/http/HttpResponse;
        WRAP_METHOD\:java/net/URL.openConnection(Ljava/net/Proxy;)Ljava/net/URLConnection;=com/github/sgwhp/openapm/sample/instrumentation/HttpInstrumentation.openConnectionWithProxy(Ljava/net/URLConnection;)Ljava/net/URLConnection;
        REPLACE_CALL_SITE\:com/google/gson/Gson.fromJson(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;=com/github/sgwhp/openapm/sample/instrumentation/GsonInstrumentation.fromJson(Lcom/google/gson/Gson;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
        REPLACE_CALL_SITE\:execute(Lorg/apache/http/client/methods/HttpUriRequest;Lorg/apache/http/client/ResponseHandler;)Ljava/lang/Object;=com/github/sgwhp/openapm/sample/instrumentation/HttpInstrumentation.execute(Lorg/apache/http/client/HttpClient;Lorg/apache/http/client/methods/HttpUriRequest;Lorg/apache/http/client/ResponseHandler;)Ljava/lang/Object;
        REPLACE_CALL_SITE\:open(Ljava/net/URL;)Ljava/net/HttpURLConnection;=com/github/sgwhp/openapm/sample/instrumentation/okhttp2/OkHttp2Instrumentation.open(Lcom/squareup/okhttp/OkUrlFactory;Ljava/net/URL;)Ljava/net/HttpURLConnection;
        REPLACE_CALL_SITE\:delete(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)I=com/github/sgwhp/openapm/sample/instrumentation/SQLiteInstrumentation.delete(Landroid/database/sqlite/SQLiteDatabase;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)I
        REPLACE_CALL_SITE\:android/graphics/BitmapFactory.decodeResourceStream(Landroid/content/res/Resources;Landroid/util/TypedValue;Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;=com/github/sgwhp/openapm/sample/instrumentation/BitmapFactoryInstrumentation.decodeResourceStream(Landroid/content/res/Resources;Landroid/util/TypedValue;Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
*/
