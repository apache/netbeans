/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.util.io;

import java.io.*;


/**
* This class convert Reader to InputStream. It works by converting
* the characters to the encoding specified in constructor parameter.
*
* @author   Petr Hamernik, David Strupl
*/
public class ReaderInputStream extends InputStream {
    /** Input Reader class. */
    private Reader reader;
    private PipedOutputStream pos;
    private PipedInputStream pis;
    private OutputStreamWriter osw;

    /** Creates new input stream from the given reader.
     * Uses the platform default encoding.
    * @param reader Input reader
    * @throws IOException on IO failure
    */
    public ReaderInputStream(Reader reader) throws IOException {
        this.reader = reader;
        pos = new PipedOutputStream();
        pis = new PipedInputStream(pos);
        osw = new OutputStreamWriter(pos);
    }

    /** Creates new input stream from the given reader and encoding.
     * @param reader Input reader
     * @param encoding encoding
     * @throws IOException on IO failure
     */
    public ReaderInputStream(Reader reader, String encoding)
    throws IOException {
        this.reader = reader;
        pos = new PipedOutputStream();
        pis = new PipedInputStream(pos);
        osw = new OutputStreamWriter(pos, encoding);
    }

    public int read() throws IOException {
        if (pis.available() > 0) {
            return pis.read();
        }

        int c = reader.read();

        if (c == -1) {
            return c;
        }

        osw.write(c);
        osw.flush();
        pos.flush();

        if (pis.available() > 0) {
            return pis.read();
        } else {
            throw new IOException("Cannot encode input data using " + osw.getEncoding() + " encoding.");  // NOI18N
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }

        int c = read();

        if (c == -1) {
            return -1;
        }

        b[off] = (byte) c;

        int i = 1;

        // Don't try to fill up the buffer if the reader is waiting.
        for (; (i < len) && reader.ready(); i++) {
            c = read();

            if (c == -1) {
                return i;
            }

            b[off + i] = (byte) c;
        }

        return i;
    }

    @Override
    public int available() throws IOException {
        int i = pis.available();

        if (i > 0) {
            return i;
        }

        if (reader.ready()) {
            // Char must produce at least one byte.
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
        osw.close();
        pis.close();
    }
}
