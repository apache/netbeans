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
package org.netbeans.modules.search.matcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.CharsetDecoder;
import org.openide.util.Exceptions;

/**
 * Reader that reads lines (and some info about them) from a file.
 *
 * @author jhavlin
 */
class LineReader {

    private static final int LINE_LENGHT_LIMIT = 5 * (1 << 20); // 5 MB
    int lastChar = 0;
    int pos = 0;
    int line = 1;
    InputStreamReader isr;
    BufferedReader br;

    LineReader(CharsetDecoder decoder, InputStream stream) throws IOException {

        isr = new InputStreamReader(stream, decoder);
        try {
            br = new BufferedReader(isr);
        } catch (Throwable t) {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            throw new IOException(t);
        }
    }

    /**
     * Read next line from the file.
     *
     * @return Object with line info, or null if no more lines exit.
     * @throws IOException
     */
    LineInfo readNext() throws IOException {

        int ch;
        LineInfo nel = new LineInfo(pos, line);

        while ((ch = br.read()) != -1) {
            pos++;
            if (ch == '\n' && lastChar == '\r') {                       //NOI18N
                nel = new LineInfo(pos, line);
            } else if (isLineTerminator(ch)) {
                line++;
                lastChar = ch;
                nel.close();
                return nel;
            } else {
                nel.appendCharacter(ch);
            }
            lastChar = ch;
        }
        if (nel.isNotEmpty()) {
            nel.close();
            return nel;
        } else {
            return null;
        }
    }

    private boolean isLineTerminator(int ch) {
        return ch == BufferedCharSequence.UnicodeLineTerminator.LF
                || ch == BufferedCharSequence.UnicodeLineTerminator.CR
                || ch == BufferedCharSequence.UnicodeLineTerminator.LS
                || ch == BufferedCharSequence.UnicodeLineTerminator.NEL
                || ch == BufferedCharSequence.UnicodeLineTerminator.PS;
    }

    /**
     * Close the reader and related resources.
     */
    void close() {
        if (br != null) {
            try {
                br.close();
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
            }
        }
        if (isr != null) {
            try {
                isr.close();
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
            }
        }
    }

    /**
     * Info about a non-empty line.
     *
     * It contains its number in the file, file offsets of its first and last
     * characters, length and value.
     */
    static class LineInfo {

        private int start;
        private int length = 0;
        private int number;
        private StringBuilder sb = new StringBuilder();
        private String string = null;

        private LineInfo(int start, int number) {
            this.start = start;
            this.number = number;
        }

        private void appendCharacter(int c) throws IOException {
            sb.append((char) c);
            length++;
            if (length > LINE_LENGHT_LIMIT) {
                throw new IOException("Line is too long: " + number);
            }
        }

        String getString() {
            return this.string;
        }

        /**
         * Line number in the file.
         */
        int getNumber() {
            return number;
        }

        /**
         * File offset of the first character.
         */
        int getFileStart() {
            return start;
        }

        /**
         * File offset of the last character.
         */
        int getFileEnd() {
            return start + length;
        }

        /**
         * Test if the line is non-empty.
         */
        private boolean isNotEmpty() {
            return length > 0;
        }

        /**
         * Get lenght of the line.
         */
        int getLength() {
            return length;
        }

        /**
         * Close this line for modifications.
         */
        void close() {
            this.string = this.sb.toString();
            this.sb = null;
        }
    }
}
