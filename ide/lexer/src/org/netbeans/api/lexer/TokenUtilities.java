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

package org.netbeans.api.lexer;

import org.netbeans.lib.editor.util.CharSequenceUtilities;

/**
 * Various utility methods related to token text.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenUtilities {

    private TokenUtilities() {
        // no instances
    }

    /**
     * Test whether the given character sequences represent
     * the same text content.
     *
     * @param text1 non-null text to be compared to the other text parameter.
     * @param text2 non-null text to be compared to the previous text parameter.
     * @return <code>true</code> if the given character sequences represent
     *  the same text content.
     */
    public static boolean textEquals(CharSequence text1, CharSequence text2) {
        return CharSequenceUtilities.textEquals(text1, text2);
    }
    
    /**
     * Compare character sequence to another object.
     * The match is successful if the second object is a character sequence as well
     * and both character sequences contain the same characters (or if both objects are null).
     *
     * @param text character sequence being compared to the given object.
     *  It may be <code>null</code>.
     * @param o object to be compared to the character sequence.
     *  It may be <code>null</code>.
     * @return true if both parameters are null or both are non-null
     *  and they contain the same text.
     */
    public static boolean equals(CharSequence text, Object o) {
        return CharSequenceUtilities.equals(text, o);
    }
    
    /**
     * Implementation of {@link String#indexOf(int)} for character sequences.
     */
    public static int indexOf(CharSequence text, int ch) {
        return CharSequenceUtilities.indexOf(text, ch);
    }

    /**
     * Implementation of {@link String#indexOf(int,int)} for character sequences.
     */
    public static int indexOf(CharSequence text, int ch, int fromIndex) {
        return CharSequenceUtilities.indexOf(text, ch, fromIndex);
    }
    
    /**
     * Implementation of {@link String#indexOf(String)} for character sequences.
     */
    public static int indexOf(CharSequence text, CharSequence seq) {
        return CharSequenceUtilities.indexOf(text, seq);
    }

    /**
     * Implementation of {@link String#indexOf(String,int)} for character sequences.
     */
    public static int indexOf(CharSequence text, CharSequence seq, int fromIndex) {
        return CharSequenceUtilities.indexOf(text, seq, fromIndex);
    }

    /**
     * Implementation of {@link String#lastIndexOf(String)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, CharSequence seq) {
        return CharSequenceUtilities.lastIndexOf(text, seq);
    }
    
    /**
     * Implementation of {@link String#lastIndexOf(String,int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, CharSequence seq, int fromIndex) {
        return CharSequenceUtilities.lastIndexOf(text, seq, fromIndex);
    }

    /**
     * Implementation of {@link String#lastIndexOf(int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, int ch) {
	return CharSequenceUtilities.lastIndexOf(text, ch);
    }

    /**
     * Implementation of {@link String#lastIndexOf(int,int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, int ch, int fromIndex) {
        return CharSequenceUtilities.lastIndexOf(text, ch, fromIndex);
    }

    /**
     * Implementation of {@link String#startsWith(String)} for character sequences.
     */
    public static boolean startsWith(CharSequence text, CharSequence prefix) {
        return CharSequenceUtilities.startsWith(text, prefix);
    }
    
    /**
     * Implementation of {@link String#endsWith(String)} for character sequences.
     */
    public static boolean endsWith(CharSequence text, CharSequence suffix) {
        return CharSequenceUtilities.endsWith(text, suffix);
    }

    /**
     * Implementation of {@link String#trim()} for character sequences.
     */
    public static CharSequence trim(CharSequence text) {
        return CharSequenceUtilities.trim(text);
    }

    /**
     * Return the given text as String
     * translating the special characters (and '\') into escape sequences.
     *
     * @param text non-null text to be debugged.
     * @return non-null string containing the debug text.
     */
    public static String debugText(CharSequence text) {
        return CharSequenceUtilities.debugText(text);
    }

}
