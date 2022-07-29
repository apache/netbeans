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

package org.netbeans.lib.editor.util;

/**
 * Utility methods related to character sequences.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CharSequenceUtilities {

    private CharSequenceUtilities() {
        // no instances
    }

    /**
     * Compute {@link String}-like hashcode over given {@link CharSequence}.
     *
     * @param text character sequence for which the hashcode is being computed.
     * @return hashcode of the given character sequence.
     */
    public static int stringLikeHashCode(CharSequence text) {
        int len = text.length();

        int h = 0;
        for (int i = 0; i < len; i++) {
            h = 31 * h + text.charAt(i);
        }
        return h;
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
        if (text == o) {
            return true;
        }

        if (text != null && o instanceof CharSequence) { // both non-null
            return textEquals(text, (CharSequence)o);
        }
        return false;
    }
    
    /**
     * Test whether whether the given character sequences
     * represent the same text.
     * <br>
     * The match is successful if the contained characters
     * of the two character sequences are the same.
     *
     * @param text1 first character sequence being compared.
     *  It must not be <code>null</code>.
     * @param text2 second character sequence being compared.
     *  It must not be <code>null</code>.
     * @return true if both parameters are equal in String-like manner.
     */
    public static boolean textEquals(CharSequence text1, CharSequence text2) {
        if (text1 == text2) {
            return true;
        }
        int len = text1.length();
        if (len == text2.length()) {
            for (int i = len - 1; i >= 0; i--) {
                if (text1.charAt(i) != text2.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Create a string from the given character sequence by first creating
     * a <code>StringBuilder</code> and appending the whole character sequence
     * to it.
     * <br>
     * The method does not call <code>toString()</code> on the given character
     * sequence.
     *
     * @param text character sequence for which the <code>String</code> form
     *  should be created.
     * @return string representation of the character sequence.
     */
    public static String toString(CharSequence text) {
        StringBuilder sb = new StringBuilder(text.length());
        sb.append(text);
        return sb.toString();
    }

    /**
     * Create string for the given portion of the character sequence.
     *
     * @param text non-null text.
     * @param start &gt;=0 and &lt;text.length() index of the first character
     *  to be present in the returned string.
     * @param end &gt;=start and &lt;text.length() index after the last character
     *  to be present in the returned string.
     */
    public static String toString(CharSequence text, int start, int end) {
        checkIndexesValid(text, start, end);
        StringBuilder sb = new StringBuilder(end - start);
        sb.append(text, start, end);
        return sb.toString();
    }
    
    /**
     * Append character sequence to the given string buffer.
     * <br>
     * This method is no longer needed in JDK 1.5 where the implementation
     * does not create an extra java.lang.String instance.
     */
    public static void append(StringBuffer sb, CharSequence text) {
        sb.append(text); // Only assume to run on 1.5
    }
    
    /**
     * Append part of the character sequence to the given string buffer.
     * <br>
     * This method is no longer needed in JDK 1.5 where the implementation
     * of the same functionality is available in the StringBuffer directly.
     */
    public static void append(StringBuffer sb, CharSequence text, int start, int end) {
        checkIndexesValid(text, start, end);
        while (start < end) {
            sb.append(text.charAt(start++));
        }
    }

    /**
     * Implementation of {@link String#indexOf(int)} for character sequences.
     */
    public static int indexOf(CharSequence text, int ch) {
	return indexOf(text, ch, 0);
    }

    /**
     * Implementation of {@link String#indexOf(int,int)} for character sequences.
     */
    public static int indexOf(CharSequence text, int ch, int fromIndex) {
	int length = text.length();
	while (fromIndex < length) {
	    if (text.charAt(fromIndex) == ch) {
		return fromIndex;
	    }
            fromIndex++;
	}
	return -1;
    }
    
    /**
     * Implementation of {@link String#indexOf(String)} for character sequences.
     */
    public static int indexOf(CharSequence text, CharSequence seq) {
        return indexOf(text, seq, 0);
    }
    
    /**
     * Implementation of {@link String#indexOf(String,int)} for character sequences.
     */
    public static int indexOf(CharSequence text, CharSequence seq, int fromIndex) {
        int textLength = text.length();
        int seqLength = seq.length();
	if (fromIndex >= textLength) {
            return (seqLength == 0 ? textLength : -1);
	}
    	if (fromIndex < 0) {
    	    fromIndex = 0;
    	}
	if (seqLength == 0) {
	    return fromIndex;
	}

        char first = seq.charAt(0);
        int max = textLength - seqLength;

        for (int i = fromIndex; i <= max; i++) {
            // look for first character
            if (text.charAt(i) != first) {
                while (++i <= max && text.charAt(i) != first);
            }

            // found first character, now look at the rest of seq
            if (i <= max) {
                int j = i + 1;
                int end = j + seqLength - 1;
                for (int k = 1; j < end && text.charAt(j) == seq.charAt(k); j++, k++);
                if (j == end) {
                    // found whole sequence
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * Implementation of {@link String#lastIndexOf(String)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, CharSequence seq) {
        return lastIndexOf(text, seq, text.length());
    }
    
    /**
     * Implementation of {@link String#lastIndexOf(String,int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, CharSequence seq, int fromIndex) {
        int textLength = text.length();
        int seqLength = seq.length();
        int rightIndex = textLength - seqLength;
	if (fromIndex < 0) {
	    return -1;
	}
	if (fromIndex > rightIndex) {
	    fromIndex = rightIndex;
	}
	// empty string always matches
	if (seqLength == 0) {
	    return fromIndex;
	}

        int strLastIndex = seqLength - 1;
	char strLastChar = seq.charAt(strLastIndex);
	int min = seqLength - 1;
	int i = min + fromIndex;

    startSearchForLastChar:
	while (true) {
	    while (i >= min && text.charAt(i) != strLastChar) {
		i--;
	    }
            
	    if (i < min) {
		return -1;
	    }
	    int j = i - 1;
	    int start = j - (seqLength - 1);
	    int k = strLastIndex - 1;

	    while (j > start) {
	        if (text.charAt(j--) != seq.charAt(k--)) {
		    i--;
		    continue startSearchForLastChar;
		}
	    }
	    return start + 1;
	}
    }
    
    /**
     * Implementation of {@link String#lastIndexOf(int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, int ch) {
	return lastIndexOf(text, ch, text.length() - 1);
    }

    /**
     * Implementation of {@link String#lastIndexOf(int,int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, int ch, int fromIndex) {
        if (fromIndex > text.length() - 1) {
            fromIndex = text.length() - 1;
        }
	while (fromIndex >= 0) {
	    if (text.charAt(fromIndex) == ch) {
		return fromIndex;
	    }
            fromIndex--;
	}
	return -1;
    }

    /**
     * Implementation of {@link String#startsWith(String)} for character sequences.
     */
    public static boolean startsWith(CharSequence text, CharSequence prefix) {
        int p_length = prefix.length();
        if (p_length > text.length()) {
            return false;
        }
        for (int x = 0; x < p_length; x++) {
            if (text.charAt(x) != prefix.charAt(x))
                return false;
        }
        return true;
    }
    
    /**
     * Implementation of {@link String#endsWith(String)} for character sequences.
     */
    public static boolean endsWith(CharSequence text, CharSequence suffix) {
        int s_length = suffix.length();
        int text_length = text.length();
        if (s_length > text_length) {
            return false;
        }
        for (int x = 0; x < s_length; x++) {
            if (text.charAt(text_length - s_length + x) != suffix.charAt(x))
                return false;
        }
        return true;
    }
    
    /**
     * Implementation of {@link String#trim()} for character sequences.
     */
    public static CharSequence trim(CharSequence text) {
        int length = text.length();
        if (length == 0)
            return text;
        int start = 0;
        int end = length - 1;
        while (start < length && text.charAt(start) <= ' ') {
            start++;
        }
        if (start == length)
            return text.subSequence(0, 0);
        while (end > start && text.charAt(end) <= ' ') {
            end--;
        }
        return text.subSequence(start, end + 1);
    }

    /**
     * Append the character description to the given string buffer
     * translating the special characters (and '\') into escape sequences.
     *
     * @param sb non-null string buffer to append to.
     * @param ch character to be debugged.
     */
    public static void debugChar(StringBuffer sb, char ch) {
        switch (ch) {
            case '\n':
                sb.append("\\n"); // NOI18N
                break;
            case '\r':
                sb.append("\\r"); // NOI18N
                break;
            case '\t':
                sb.append("\\t"); // NOI18N
                break;
            case '\b':
                sb.append("\\b"); // NOI18N
                break;
            case '\f':
                sb.append("\\f"); // NOI18N
                break;
            case '\\':
                sb.append("\\\\"); // NOI18N
                break;
            default:
                sb.append(ch);
                break;
        }
    }
    
    /**
     * Append the character description to the given string builder
     * translating the special characters (and '\') into escape sequences.
     *
     * @param sb non-null string buffer to append to.
     * @param ch character to be debugged.
     */
    public static void debugChar(StringBuilder sb, char ch) {
        switch (ch) {
            case '\n':
                sb.append("\\n"); // NOI18N
                break;
            case '\r':
                sb.append("\\r"); // NOI18N
                break;
            case '\t':
                sb.append("\\t"); // NOI18N
                break;
            case '\b':
                sb.append("\\b"); // NOI18N
                break;
            case '\f':
                sb.append("\\f"); // NOI18N
                break;
            case '\\':
                sb.append("\\\\"); // NOI18N
                break;
            default:
                sb.append(ch);
                break;
        }
    }
    
    /**
     * Return the text description of the given character
     * translating the special characters (and '\') into escape sequences.
     *
     * @param ch char to debug.
     * @return non-null debug text.
     */
    public static String debugChar(char ch) {
        StringBuilder sb = new StringBuilder();
        debugChar(sb, ch);
        return sb.toString();
    }
    
    /**
     * Append the text description to the given string buffer
     * translating the special characters (and '\') into escape sequences.
     *
     * @param sb non-null string buffer to append to.
     * @param text non-null text to be debugged.
     */
    public static void debugText(StringBuffer sb, CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            debugChar(sb, text.charAt(i));
        }
    }
    
    /**
     * Append the text description to the given string builder
     * translating the special characters (and '\') into escape sequences.
     *
     * @param sb non-null string builder to append to.
     * @param text non-null text to be debugged.
     */
    public static void debugText(StringBuilder sb, CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            debugChar(sb, text.charAt(i));
        }
    }
    
    /**
     * Create text description as String
     * translating the special characters (and '\') into escape sequences.
     *
     * @param text non-null text to be debugged.
     * @return non-null string containing the debug text.
     */
    public static String debugText(CharSequence text) {
        StringBuilder sb = new StringBuilder();
        debugText(sb, text);
        return sb.toString();
    }
    
    /**
     * Ensure that the given index is &gt;=0 and lower than the given length.
     * @throws IndexOutOfBoundsException if the index is not within bounds.
     */
    public static void checkIndexNonNegative(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index=" + index + " < 0"); // NOI18N
        }
    }

    /**
     * Ensure that the given index is &gt;=0 and lower than the given length.
     * @throws IndexOutOfBoundsException if the index is not within bounds.
     */
    public static void checkIndexValid(int index, int length) {
        checkIndexNonNegative(index);
        if (index >= length) {
            throw new IndexOutOfBoundsException("index=" + index // NOI18N
                + " >= length()=" + length); // NOI18N
        }
    }

    /**
     * Ensure that the given start and end parameters are valid indices
     * of the given text.
     * @param start must be &gt;=0 and &lt;=end.
     * @param end must be &gt;=start and &lt;=textLength.
     * @param length total length of a charsequence.
     * @throws IndexOutOfBoundsException if the start or end are not within bounds.
     */
    public static void checkIndexesValid(int start, int end, int length) {
        if (start < 0) {
            throw new IndexOutOfBoundsException("start=" + start + " < 0"); // NOI18N
        }
        if (end < start) {
            throw new IndexOutOfBoundsException("end=" + end + " < start=" + start); // NOI18N
        }
        if (end > length) {
            throw new IndexOutOfBoundsException("end=" + end + " > length()=" + length); // NOI18N
        }
    }

    /**
     * Ensure that the given start and end parameters are valid indices
     * of the given text.
     * @param text non-null char sequence.
     * @param start must be &gt;=0 and &lt;=end.
     * @param end must be &gt;=start and &lt;=<code>text.length()</code>.
     * @throws IndexOutOfBoundsException if the start or end are not within bounds
     *  of the given text.
     */
    public static void checkIndexesValid(CharSequence text, int start, int end) {
        checkIndexesValid(start, end, text.length());
    }

}
