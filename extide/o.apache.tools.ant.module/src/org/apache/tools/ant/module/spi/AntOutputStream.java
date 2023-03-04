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

package org.apache.tools.ant.module.spi;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import org.openide.filesystems.FileObject;

/** OutputStream for wrapping output of Ant task and capable of
 * parsing Ant output.
 *
 * @since 2.15
 * @deprecated This functionality is not recommended to be used and may result
 *             in loss of some Ant module features as of org.apache.tools.ant.module/3 3.12.
 */
@Deprecated
public abstract class AntOutputStream extends OutputStream {

    /** buffer which will be used for the next line */
    private StringBuffer buffer = new StringBuffer (1000);
    /** have we printed any lines yet? used to prevent initial blank line */
    private boolean hadFirst = false;

    @Override
    public final void close() throws IOException {
        flush ();
        handleClose();
    }

    /**
     * This method is called when the stream is closed and it allows
     * entensions of this class to do additional tasks.
     * For example, closing an underlying stream, etc.
     * The default implementation does nothing.
     */
    protected void handleClose() throws IOException {
    }

    @Override
    public final void flush() throws IOException {
        flushLines (true);
    }

    @Override
    public final void write(byte[] b) throws IOException {
        write (b, 0, b.length);
    }

    @Override
    public final void write(byte[] b, int offset, int length) throws IOException {
        buffer.append (new String (b, offset, length));
        // Will usually contain at least one newline:
        flushLines (false);
    }

    @Override
    public final void write(int b) throws IOException {
        buffer.append ((char) b);
        if ((char) b == '\n') {
            flushLines (false);
        }
    }

    private void flushLines (boolean flushEverything) throws IOException {
        // Not as efficient as it could be, surely, but keep it simple for now:
        //System.err.println("flushLines: buffer=" + buffer);
    MAIN:
        while (true) {
            int len = buffer.length ();
            for (int i = 0; i < len; i++) {
                if (buffer.charAt (i) == '\n') {
                    //System.err.println("flushing; i=" + i);
                    // For Windows:
                    int end = i;
                    if (end > 0 && buffer.charAt (end - 1) == '\r') {
                        end--;
                    }
                    flushLine (buffer.substring (0, end));
                    buffer.delete (0, i + 1);
                    continue MAIN;
                }
            }
            //System.err.println("not found");
            break MAIN;
        }
        if (flushEverything) {
            flushLine(buffer.substring(0));
            buffer.delete(0, buffer.length());
        }
    }
    
    private void flushLine (String l) throws IOException {
        //System.err.println("flushing: " + l);
        if (! hadFirst) {
            hadFirst = true;
            // Do not print an initial blank line.
            if (l.trim ().length () == 0) {
                return;
            }
        }
        writeLine(l);
    }

    /**
     * Write one line of the parsed text (<strong>must be overridden</strong>).
     * All line and column parameters can be -1 meaning
     * that the value was not available or parsing was not successful.
     * @param line original text of the line
     * @param file file location for which this line was generated
     * @param line1 starting line of the message
     * @param col1 starting column of the message
     * @param line2 ending line of the message
     * @param col2 ending column of the message
     * @param message message
     * @return must always return true
     * @since org.apache.tools.ant.module/3 3.10
     * @deprecated No longer called.
     */
    @Deprecated
    protected boolean writeLine(String line, URL file, int line1, int col1, int line2, int col2, String message) throws IOException {
        return false;
    }

    /** Write one line of the parsed text. All line and column parameters can be -1 what means
    * that value was not available or parsing was not successful.
    * @param line original text of the line
    * @param file file object for which this line was generated
    * @param line1 starting line of the message
    * @param col1 starting column of the message
    * @param line2 ending line of the message
    * @param col2 ending column of the message
    * @param message message 
     * @deprecated Please override the variant taking URL instead, since org.apache.tools.ant.module/3 3.10.
    */
    @Deprecated
    protected void writeLine(String line, FileObject file, int line1, int col1, int line2, int col2, String message) throws IOException {
        throw new IllegalStateException("writeLine(...URL...) must return true if writeLine(...FileObject...) is not implemented"); // NOI18N
    }

    /** Write one line of text which was not parsed.
     */
    protected abstract void writeLine(String line) throws IOException;
    
    /** Create well formated message from the parsed information.
     * @deprecated No longer used since org.apache.tools.ant.module/3 3.8.
     */
    @Deprecated
    protected String formatMessage(String fileName, String message, int line1, int col1, int line2, int col2) {
        return message;
    }
    
}
