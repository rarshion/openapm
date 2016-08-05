package com.github.sgwhp.openapm.sample.Instrumentation.io;

import com.github.sgwhp.openapm.sample.Agent;
import com.github.sgwhp.openapm.sample.logging.AgentLog;
import com.github.sgwhp.openapm.sample.logging.AgentLogManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by user on 2016/8/1.
 */
public class CountingInputStream extends InputStream implements StreamCompleteListenerSource {

    private final InputStream impl;
    private long count;
    private final StreamCompleteListenerManager listenerManager;
    private final ByteBuffer buffer;
    private boolean enableBuffering;
    private static final AgentLog log;

    public CountingInputStream(final InputStream impl) {
        this.count = 0L;
        this.listenerManager = new StreamCompleteListenerManager();
        this.enableBuffering = false;
        this.impl = impl;
        if (this.enableBuffering) {
            this.buffer = ByteBuffer.allocate(Agent.getResponseBodyLimit());
            this.fillBuffer();
        } else {
            this.buffer = null;
        }
    }

    public CountingInputStream(final InputStream impl, final boolean enableBuffering) {
        this.count = 0L;
        this.listenerManager = new StreamCompleteListenerManager();
        this.enableBuffering = false;
        this.impl = impl;
        this.enableBuffering = enableBuffering;
        if (enableBuffering) {
            this.buffer = ByteBuffer.allocate(Agent.getResponseBodyLimit());
            this.fillBuffer();
        }
        else {
            this.buffer = null;
        }
    }

    @Override
    public void addStreamCompleteListener(final StreamCompleteListener streamCompleteListener) {
        this.listenerManager.addStreamCompleteListener(streamCompleteListener);
    }

    @Override
    public void removeStreamCompleteListener(final StreamCompleteListener streamCompleteListener) {
        this.listenerManager.removeStreamCompleteListener(streamCompleteListener);
    }

    @Override
    public int read() throws IOException {
        if (this.enableBuffering) {
            synchronized (this.buffer) {
                if (this.bufferHasBytes(1L)) {
                    final int n = this.readBuffer();
                    if (n >= 0) {
                        ++this.count;
                    }
                    return n;
                }
            }
        }
        try {
            final int n = this.impl.read();
            if (n >= 0) {
                ++this.count;
            }
            else {
                this.notifyStreamComplete();
            }
            return n;
        }
        catch (IOException e) {
            this.notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public int read(final byte[] b) throws IOException {
        int n = 0;
        int numBytesFromBuffer = 0;
        int inputBufferRemaining = b.length;
        if (this.enableBuffering) {
            synchronized (this.buffer) {
                if (this.bufferHasBytes(inputBufferRemaining)) {
                    n = this.readBufferBytes(b);
                    if (n >= 0) {
                        this.count += n;
                        return n;
                    }
                    throw new IOException("readBufferBytes failed");
                }
                else {
                    final int remaining = this.buffer.remaining();
                    if (remaining > 0) {
                        numBytesFromBuffer = this.readBufferBytes(b, 0, remaining);
                        if (numBytesFromBuffer < 0) {
                            throw new IOException("partial read from buffer failed");
                        }
                        inputBufferRemaining -= numBytesFromBuffer;
                        this.count += numBytesFromBuffer;
                    }
                }
            }
        }

        try {
            n = this.impl.read(b, numBytesFromBuffer, inputBufferRemaining);
            if (n >= 0) {
                this.count += n;
                return n + numBytesFromBuffer;
            }

            if (numBytesFromBuffer <= 0) {
                this.notifyStreamComplete();
                return n;
            }
            return numBytesFromBuffer;
        }
        catch (IOException e) {
            CountingInputStream.log.error(e.toString());
            System.out.println("NOTIFY STREAM ERROR: " + e);
            e.printStackTrace();
            this.notifyStreamError(e);
            throw e;
        }
    }


    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        int n = 0;
        int numBytesFromBuffer = 0;
        int inputBufferRemaining = len;
        if (this.enableBuffering) {
            synchronized (this.buffer) {
                if (this.bufferHasBytes(inputBufferRemaining)) {
                    n = this.readBufferBytes(b, off, len);
                    if (n >= 0) {
                        this.count += n;
                        return n;
                    }
                    throw new IOException("readBufferBytes failed");
                }
                else {
                    final int remaining = this.buffer.remaining();
                    if (remaining > 0) {
                        numBytesFromBuffer = this.readBufferBytes(b, off, remaining);
                        if (numBytesFromBuffer < 0) {
                            throw new IOException("partial read from buffer failed");
                        }
                        inputBufferRemaining -= numBytesFromBuffer;
                        this.count += numBytesFromBuffer;
                    }
                }
            }
        }
        try {
            n = this.impl.read(b, off + numBytesFromBuffer, inputBufferRemaining);
            if (n >= 0) {
                this.count += n;
                return n + numBytesFromBuffer;
            }
            if (numBytesFromBuffer <= 0) {
                this.notifyStreamComplete();
                return n;
            }
            return numBytesFromBuffer;
        }
        catch (IOException e) {
            this.notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public long skip(final long byteCount) throws IOException {
        long toSkip = byteCount;
        if (this.enableBuffering) {
            synchronized (this.buffer) {
                if (this.bufferHasBytes(byteCount)) {
                    this.buffer.position((int)byteCount);
                    this.count += byteCount;
                    return byteCount;
                }
                toSkip = byteCount - this.buffer.remaining();
                if (toSkip <= 0L) {
                    throw new IOException("partial read from buffer (skip) failed");
                }
                this.buffer.position(this.buffer.remaining());
            }
        }
        try {
            final long n = this.impl.skip(toSkip);
            this.count += n;
            return n;
        }
        catch (IOException e) {
            this.notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public int available() throws IOException {
        int remaining = 0;
        if (this.enableBuffering) {
            remaining = this.buffer.remaining();
        }
        try {
            return remaining + this.impl.available();
        }
        catch (IOException e) {
            this.notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.impl.close();
            this.notifyStreamComplete();
        }
        catch (IOException e) {
            this.notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public void mark(final int readlimit) {
        if (!this.markSupported()) {
            return;
        }
        this.impl.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return this.impl.markSupported();
    }

    @Override
    public void reset() throws IOException {
        if (!this.markSupported()) {
            return;
        }
        try {
            this.impl.reset();
        }
        catch (IOException e) {
            this.notifyStreamError(e);
            throw e;
        }
    }

    private int readBuffer() {
        if (this.bufferEmpty()) {
            return -1;
        }
        return this.buffer.get();
    }

    private int readBufferBytes(final byte[] bytes) {
        return this.readBufferBytes(bytes, 0, bytes.length);
    }

    private int readBufferBytes(final byte[] bytes, final int offset, final int length) {
        if (this.bufferEmpty()) {
            return -1;
        }
        final int remainingBefore = this.buffer.remaining();
        this.buffer.get(bytes, offset, length);
        return remainingBefore - this.buffer.remaining();
    }

    private boolean bufferHasBytes(final long num) {
        return this.buffer.remaining() >= num;
    }

    private boolean bufferEmpty() {
        return !this.buffer.hasRemaining();
    }

    public void fillBuffer() {
        if (this.buffer != null) {
            if (!this.buffer.hasArray()) {
                return;
            }
            synchronized (this.buffer) {
                int bytesRead = 0;
                try {
                    bytesRead = this.impl.read(this.buffer.array(), 0, this.buffer.capacity());
                }
                catch (IOException e) {
                    CountingInputStream.log.error(e.toString());
                }
                if (bytesRead <= 0) {
                    this.buffer.limit(0);
                }
                else if (bytesRead < this.buffer.capacity()) {
                    this.buffer.limit(bytesRead);
                }
            }
        }
    }

    private void notifyStreamComplete() {
        if (!this.listenerManager.isComplete()) {
            this.listenerManager.notifyStreamComplete(new StreamCompleteEvent(this, this.count));
        }
    }

    private void notifyStreamError(final Exception e) {
        if (!this.listenerManager.isComplete()) {
            this.listenerManager.notifyStreamError(new StreamCompleteEvent(this, this.count, e));
        }
    }

    public void setBufferingEnabled(final boolean enableBuffering) {
        this.enableBuffering = enableBuffering;
    }

    public String getBufferAsString() {
        if (this.buffer != null) {
            synchronized (this.buffer) {
                final byte[] buf = new byte[this.buffer.limit()];
                for (int i = 0; i < this.buffer.limit(); ++i) {
                    buf[i] = this.buffer.get(i);
                }
                return new String(buf);
            }
        }
        return "";
    }

    static {
        log = AgentLogManager.getAgentLog();
    }
}
