/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.libs.graalsdk.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stream backed by a Writer. Uses UTF-8 to decode characters from the stream.
 */
class WriterOutputStream extends OutputStream {
    private static final Logger LOG = Logger.getLogger(WriterOutputStream.class.getName());

    private boolean writeImmediately = true;

    private final CharsetDecoder decoder;
    private final ByteBuffer decoderIn = ByteBuffer.allocate(128);
    private final CharBuffer decoderOut;
    private Writer writer;

    public WriterOutputStream(Writer out) {
        this.writer = out;
        this.decoder = StandardCharsets.UTF_8.
                newDecoder().
                onMalformedInput(CodingErrorAction.REPLACE).
                onUnmappableCharacter(CodingErrorAction.REPLACE).
                replaceWith("?"); //NOI18N
        this.decoderOut = CharBuffer.allocate(2048);
    }

    public void setWriter(Writer out) {
        try {
            flush();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        writer = out;
    }

    public Writer getWriter() {
        return writer;
    }

    @Override
    public void write(int b) throws IOException {
        decoderIn.put((byte) b);
        processInput(false);
        if (writeImmediately) {
            flushOutput();
        }
    }

    @Override
    public void write(final byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            final int c = Math.min(len, decoderIn.remaining());
            decoderIn.put(b, off, c);
            processInput(false);
            len -= c;
            off += c;
        }
        if (writeImmediately) {
            flushOutput();
        }
    }

    private void flushOutput() throws IOException {
        if (decoderOut.position() > 0) {
            writer.write(decoderOut.array(), 0, decoderOut.position());
            decoderOut.rewind();
        }
    }

    @Override
    public void close() throws IOException {
        processInput(true);
        flushOutput();
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        flushOutput();
        writer.flush();
    }

    private void processInput(final boolean endOfInput) throws IOException {
        // Prepare decoderIn for reading
        decoderIn.flip();
        CoderResult coderResult;
        while (true) {
            coderResult = decoder.decode(decoderIn, decoderOut, endOfInput);
            if (coderResult.isOverflow()) {
                flushOutput();
            } else if (coderResult.isUnderflow()) {
                break;
            } else {
                    // The decoder is configured to replace malformed input and unmappable characters,
                // so we should not get here.
                throw new IOException("Unexpected coder result"); //NOI18N
            }
        }
        // Discard the bytes that have been read
        decoderIn.compact();
    }
}
