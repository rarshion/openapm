package com.github.sgwhp.openapm.sample.Instrumentation.httpclient;

import com.github.sgwhp.openapm.sample.Instrumentation.io.CountingInputStream;

import org.apache.http.*;

import java.io.*;

/**
 * Created by user on 2016/8/1.
 */
public class ContentBufferingResponseEntityImpl implements HttpEntity {
    final HttpEntity impl;
    private CountingInputStream contentStream;

    public ContentBufferingResponseEntityImpl(final HttpEntity impl) {
        if (impl == null) {
            throw new IllegalArgumentException("Missing wrapped entity");
        }
        this.impl = impl;
    }

    public void consumeContent() throws IOException {
        this.impl.consumeContent();
    }

    public InputStream getContent() throws IOException, IllegalStateException {
        if (this.contentStream != null) {
            return this.contentStream;
        }
        return this.contentStream = new CountingInputStream(this.impl.getContent(), true);
    }

    public Header getContentEncoding() {
        return this.impl.getContentEncoding();
    }

    public long getContentLength() {
        return this.impl.getContentLength();
    }

    public Header getContentType() {
        return this.impl.getContentType();
    }

    public boolean isChunked() {
        return this.impl.isChunked();
    }

    public boolean isRepeatable() {
        return this.impl.isRepeatable();
    }

    public boolean isStreaming() {
        return this.impl.isStreaming();
    }

    public void writeTo(final OutputStream outputStream) throws IOException {
        this.impl.writeTo(outputStream);
    }
}
