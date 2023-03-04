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

package org.netbeans.core.execution;

import java.io.Writer;
import java.io.PrintStream;
import java.io.IOException;

final class OutputStreamWriter extends Writer {

    private PrintStream out;

    /**
     * Create an OutputStreamWriter that uses the default character encoding.
     *
     * @param  out  An OutputStream
     */
    public OutputStreamWriter(PrintStream out) {
        super(out);
        if (out == null)
            throw new NullPointerException();
        this.out = out;
    }

    /** Check to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (out == null)
            throw new IOException("Stream closed");
    }
    
    /**
     * Write a single character.
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Override
    public void write(int c) throws IOException {
        char cbuf[] = new char[1];
        cbuf[0] = (char) c;
        write(cbuf, 0, 1);
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param  cbuf  Buffer of characters
     * @param  off   Offset from which to start writing characters
     * @param  len   Number of characters to write
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if ((off == 0) && (len == cbuf.length)) {
                out.print(cbuf);
            } else {
                char[] chars = new char[len];
                System.arraycopy(cbuf, off, chars, 0, len);
                out.print(chars);
            }
        }
    }

    /**
     * Write a portion of a string.
     *
     * @param  str  A String
     * @param  off  Offset from which to start writing characters
     * @param  len  Number of characters to write
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Override
    public void write(String str, int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (off == 0 && len == str.length()) {
                out.print(str);
            } else {
                char[] chars = new char[len];
                str.getChars(off, off + len, chars, 0);
                out.print(chars);
            }
        }
    }

    /**
     * Flush the stream.
     */
    public void flush() {
        synchronized (lock) {
            if (out == null)
                return;
            out.flush();
        }
    }

    /**
     * Close the stream.
     */
    public void close() {
        synchronized (lock) {
            if (out == null)
                return;
            flush();
            out.close();
            out = null;
        }
    }

}
