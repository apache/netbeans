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

package org.netbeans.lib.editor.util;

/**
 * Provides some useful methods for character conversions.
 * 
 * @author Vita Stejskal
 * @since 1.21
 */
public final class CharacterConversions {

    /**
     * Unicode line feed (0x000A).
     */
    public static final char LF = '\n'; //NOI18N
    /**
     * Unicode carriage return (0x000D).
     */
    public static final char CR = '\r'; //NOI18N
    /**
     * Unicode line separator (0x2028).
     */
    public static final char LS = 0x2028;
    /**
     * Unicode paragraph separator (0x2029).
     */
    public static final char PS = 0x2029;
    
    /**
     * Converts line separators in text to line feed (<code>0x0A</code>)
     * character. It automatically detects all sorts of line separators such
     * as <code>CRLF</code>, <code>LF</code>, <code>LS</code> and <code>PS</code>.
     * The text can even contain a mixture of those separators.
     * 
     * @param text The text to convert line seprators in. This can be <code>null</code>,
     *   in which case an empty string is returned.
     * 
     * @return The text with line separators replaced by the line feed character.
     */
    public static String lineSeparatorToLineFeed(CharSequence text) {
        if (text == null || text.length() == 0) {
            return ""; //NOI18N
        }
        
        boolean lastCharCR = false;
        StringBuilder output = new StringBuilder(text.length());
        
        for(int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (lastCharCR && ch == LF) { // found CRLF sequence
                output.append(LF);
                lastCharCR = false;

            } else { // not CRLF sequence
                if (ch == CR) {
                    lastCharCR = true;
                } else if (ch == LS || ch == PS) { // Unicode LS, PS
                    output.append(LF);
                    lastCharCR = false;
                } else { // current char not CR
                    lastCharCR = false;
                    output.append(ch);
                }
            }
        }
        
        return output.toString();
    }
    
    /**
     * Converts line separators in text to line feed (<code>0x0A</code>)
     * character. This method will only look for the <code>lineSeparator</code>
     * characters and will ignore all other characters that could possibly be
     * used for separating lines in the text. If you want automatic detection
     * of all possible line separators use {@link #lineSeparatorToLineFeed(CharSequence)}.
     * 
     * @param text The text to convert line seprators in. This can be <code>null</code>,
     *   in which case an empty string is returned.
     * @param lineSeparator The line separator to look for and replace.
     * 
     * @return The text with <code>lineSeparator</code>s replaced by the
     *   line feed character.
     */
    public static String lineSeparatorToLineFeed(CharSequence text, CharSequence lineSeparator) {
        if (text == null || text.length() == 0) {
            return ""; //NOI18N
        }
        
        int lineSeparatorIdx = 0;
        StringBuilder output = new StringBuilder(text.length());
        
        for(int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            char lsCh = lineSeparator.charAt(lineSeparatorIdx);
            
            if (ch == lsCh) {
                if (lineSeparatorIdx == lineSeparator.length() - 1) { // last char from lineSeparator
                    output.append(LF);
                    lineSeparatorIdx = 0;
                } else {
                    lineSeparatorIdx++;
                }
            } else {
                if (lineSeparatorIdx > 0) {
                    output.append(lineSeparator, 0, lineSeparatorIdx);
                }
                
                output.append(ch);
                lineSeparatorIdx = 0;
            }
        }
        
        return output.toString();
    }
    
    /**
     * Converts line feed characters (<code>0x0A</code>) to a system default line
     * separator. The line separator for your system is determined by looking
     * at the "line.separator" system property of your JVM.
     * 
     * @param text The text to convert line feed characters in. Can be <code>null</code>,
     *   in which case an empty string is returned.
     * 
     * @return The text with line feed characters replaced by a line separator
     *   used on your system.
     */
    public static String lineFeedToLineSeparator(CharSequence text) {
        return lineFeedToLineSeparator(text, getSystemDefaultLineSeparator());
    }
    
    /**
     * Converts line feed characters (<code>0x0A</code>) to the specified line
     * separator.
     * 
     * @param text The text to convert line feed characters in. Can be <code>null</code>,
     *   in which case an empty string is returned.
     * @param lineSeparator The line separator to use as a replacement.
     * 
     * @return The text with line feed characters replaced by the <code>lineSeparator</code>.
     */
    public static String lineFeedToLineSeparator(CharSequence text, CharSequence lineSeparator) {
        if (text == null || text.length() == 0) {
            return ""; //NOI18N
        }
        
        StringBuilder output = new StringBuilder(text.length());
        
        for(int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == LF) {
                output.append(lineSeparator);
            } else {
                output.append(ch);
            }
        }
        
        return output.toString();
    }
    
    // -----------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------

    private static String systemDefaultLineSeparator = null;

    private static String getSystemDefaultLineSeparator() {
        if (systemDefaultLineSeparator == null) {
            systemDefaultLineSeparator = System.getProperty("line.separator"); // NOI18N
        }
        return systemDefaultLineSeparator;
    }

    // Just to prevent instantialization
    private CharacterConversions() {
        
    }
}
