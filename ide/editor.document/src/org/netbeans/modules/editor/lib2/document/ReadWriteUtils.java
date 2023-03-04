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
package org.netbeans.modules.editor.lib2.document;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Utilities related to reading/writing data from/to Reader/Writer
 * and line separator conversion utilities.
 *
 * @author Miloslav Metelka
 * @since 1.46
 */

public final class ReadWriteUtils {
    
    /**
     * Initial size of the read char array.
     */
    private static final int INITIAL_BUFFER_SIZE = 4096;
        
    private ReadWriteUtils() {
        // no instances
    }
    
    public static ReadWriteBuffer read(Reader r) throws IOException {
        char[] text = new char[INITIAL_BUFFER_SIZE];
        int length = 0;
        int readLen;
        while ((readLen = r.read(text, length, text.length - length)) != -1) {
            length += readLen;
            if (length == text.length) {
                char[] tmp = new char[text.length << 1];
                System.arraycopy(text, 0, tmp, 0, length);
                text = tmp;
            }
        }
        return new ReadWriteBuffer(text, length);
    }
    
    /**
     * Return first line separator - one of "\r", "\n" or "\r\n" or return null
     * if none of them is found.
     *
     * @param buffer read/write buffer to analyze.
     */
    public static String findFirstLineSeparator(ReadWriteBuffer buffer) {
        for (int i = 0; i < buffer.length(); i++) {
            switch (buffer.text[i]) {
                case '\n':
                    return "\n";
                case '\r':
                    if (i + 1 < buffer.length && buffer.text[i + 1] == '\n') {
                        return "\r\n";
                    } else {
                        return "\r";
                    }
            }
        }
        return null;
    }
    
    public static String getSystemLineSeparator() {
        return System.getProperty("line.separator"); // NOI18N
    }
    
    /**
     * Convert all occurrences of '\r' and '\r\n' in the text to '\n'.
     *
     * @param text text being converted
     * @return converted text with '\n' instead of '\r' or '\r\n'.
     */
    public static void convertToNewlines(ReadWriteBuffer buffer) {
        char[] text = buffer.text;
        int len = buffer.length;
        int j = 0; // Target index
        for (int i = 0; i < len; i++) {
            char ch = text[i];
            if (ch == '\r') {
                if (i + 1 < len && text[i + 1] == '\n') {
                    i++; // skip input '\n'
                }
                ch = '\n';
            }
            text[j++] = ch;
        }
        buffer.length = j;
    }

    /**
     * Convert all occurrences of '\r' and '\r\n' in the text to '\n'.
     *
     * @param text text being converted
     * @return either text.toString() for no '\r' or '\r\n' found or a new string with line separators converted.
     */
    public static String convertToNewlines(CharSequence text) {
        int textLen = text.length();
        for (int i = 0; i < textLen; i++) {
            char ch = text.charAt(i);
            if (ch == '\r') {
                char[] output = new char[textLen];
                int j;
                for (j = 0; j < i; j++) {
                    output[j] = text.charAt(j);
                }
                output[j++] = '\n';
                i++; // skip input '\r'
                if (i < textLen && text.charAt(i) == '\n') {
                    i++; // skip input '\n'
                }
                for (; i < textLen; i++) {
                    ch = text.charAt(i);
                    if (ch == '\r') {
                        if (i + 1 < textLen && text.charAt(i + 1) == '\n') {
                            i++; // skip input '\n'
                        }
                        ch = '\n';
                    }
                    output[j++] = ch;
                }
                return new String(output, 0, j);
            }
        }
        return text.toString();
    }

    /**
     * Convert all occurrences of '\n' to given line separator.
     *
     * @see #convertFromNewlines(java.lang.CharSequence, int, int, java.lang.String)
     */
    public static ReadWriteBuffer convertFromNewlines(CharSequence text, String lineSeparator) {
        return convertFromNewlines(text, 0, text.length(), lineSeparator);
    }

    /**
     * Convert all occurrences of '\n' to given line separator.
     *
     * @param text text being converted.
     * @param start start index in text.
     * @param end end index in text.
     * @param lineSeparator either '\n' or '\r' or '\r\n'.
     * @return converted text with '\n' instead of '\r' and '\r\n'.
     */
    public static ReadWriteBuffer convertFromNewlines(CharSequence text, int start, int end, String lineSeparator) {
        int textLen = end - start;
        char[] output;
        int j = 0; // Target index
        if ("\r\n".equals(lineSeparator)) {
            output = new char[textLen + (textLen >>> 3) + 4]; // Add 1/8 to cover the "\r\n".
            int outputSafeLen = output.length - 2;
            for (int i = 0; i < textLen; i++) {
                if (j >= outputSafeLen) {
                    char[] tmp = new char[output.length + (output.length >>> 1) + 4];
                    System.arraycopy(output, 0, tmp, 0, j);
                    output = tmp;
                    outputSafeLen = output.length - 2;
                }
                char ch = text.charAt(start + i);
                if (ch == '\n') {
                    output[j++] = '\r';
                }
                output[j++] = ch;
            }

        } else {
            output = new char[textLen];
            if ("\r".equals(lineSeparator)) {
                for (; j < textLen; j++) {
                    char ch = text.charAt(start + j);
                    if (ch == '\n') {
                        ch = '\r';
                    }
                    output[j] = ch;
                }
            } else { // '\n'
                for (; j < textLen; j++) {
                    output[j] = text.charAt(start + j);
                }
            }
        }
        return new ReadWriteBuffer(output, j);
    }

    public static void write(Writer w, ReadWriteBuffer buffer) throws IOException {
        w.write(buffer.text(), 0, buffer.length());
    }

}
