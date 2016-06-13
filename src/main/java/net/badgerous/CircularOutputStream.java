/**
 * Copyright (c) 2016, Alun Evans.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are NOT permitted.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.badgerous;

import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * An output stream that continuously writes to a circular buffer.
 *
 * @author Alun Evans.
 * @version $Id$
 * @since 1.0
 */
public final class CircularOutputStream extends OutputStream{

    /**
     * 4KB, best guess... What we really care about is going over cache lines,
     * I wonder if we care about forcing TLB entries.
     */
    private static final int PAGE_SIZE = 4 << 10;

    /**
     * Memory to write into.
     */
    private final transient ByteBuffer buffer;

    /**
     * Ctor.
     */
    public CircularOutputStream() {
        this.buffer = ByteBuffer.allocate(CircularOutputStream.PAGE_SIZE);
    }

    /** Discards the specified byte. */
    @Override
    public void write(final int octet) {
        if (!this.buffer.hasRemaining()) {
            this.buffer.clear();
        }
        this.buffer.put((byte) octet);
    }

    /** Discards the specified byte array. */
    @Override
    public void write(final byte[] bytes, final int off, final int len) {
        int pos = 0;
        while (pos < len) {
            final int write = Math.min(this.buffer.remaining(), len - pos);
            this.buffer.put(bytes, pos, write);
            pos += write;
            if (pos != len) {
                this.buffer.clear();
            }
        }
    }

}
