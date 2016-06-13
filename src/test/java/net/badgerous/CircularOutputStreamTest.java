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

import java.io.IOException;
import java.io.OutputStream;
import org.junit.Test;

/**
 * Tests for {@link CircularOutputStream}.
 *
 * @author Alun Evans
 * @version $Id$
 * @since 1.0
 */
public final class CircularOutputStreamTest {

    /**
     * Can write data to the stream without exception.
     * @throws IOException In case of problem
     */
    @Test
    public void canWriteData() throws IOException {
        int idx = 0;
        try (final OutputStream output = new CircularOutputStream()) {
            final byte[] data = new byte[64];
            for (idx = 0; idx < 10000; ++idx) {
                output.write(data);
                output.write(0);
                output.write(data, 0, data.length);
            }
        }
    }

}
