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

package org.netbeans.modules.php.api.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Parameters;

/**
 * Miscellaneous string utilities.
 * @author Tomas Mysik
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Return <code>true</code> if the String is not <code>null</code>
     * and has any character after trimming.
     * @param input input <tt>String</tt>, can be <code>null</code>.
     * @return <code>true</code> if the String is not <code>null</code>
     *         and has any character after trimming.
     * @see #isEmpty(String)
     */
    public static boolean hasText(String input) {
        return input != null && !input.trim().isEmpty();
    }

    /**
     * Return <code>true</code> if the String is <code>null</code>
     * or has no characters.
     * @param input input <tt>String</tt>, can be <code>null</code>
     * @return <code>true</code> if the String is <code>null</code>
     *         or has no characters
     * @see  #hasText(String)
     */
    public static boolean isEmpty(String input) {
        return input == null || input.isEmpty();
    }

    /**
     * Implode collection of strings to one string using delimiter.
     * @param items collection of strings to be imploded, can be empty (but not <code>null</code>)
     * @param delimiter delimiter to be used
     * @return one string of imploded strings using delimiter, never <code>null</code>
     * @see #explode(String, String)
     * @since 2.14
     */
    public static String implode(Collection<String> items, String delimiter) {
        Parameters.notNull("items", items);
        Parameters.notNull("delimiter", delimiter);

        if (items.isEmpty()) {
            return ""; // NOI18N
        }

        StringBuilder buffer = new StringBuilder(200);
        boolean first = true;
        for (String s : items) {
            if (!first) {
                buffer.append(delimiter);
            }
            buffer.append(s);
            first = false;
        }
        return buffer.toString();
    }

    /**
     * Explode the string using the delimiter.
     * @param string string to be exploded, can be <code>null</code>
     * @param delimiter delimiter to be used, cannot be empty string
     * @return list of exploded strings using delimiter
     * @see #implode(List, String)
     */
    public static List<String> explode(@NullAllowed String string, String delimiter) {
        Parameters.notEmpty("delimiter", delimiter); // NOI18N

        if (!hasText(string)) {
            return Collections.<String>emptyList();
        }
        assert string != null;
        return Arrays.asList(string.split(Pattern.quote(delimiter)));
    }

    /**
     * Get the case-insensitive {@link Pattern pattern} for the given <tt>String</tt>
     * or <code>null</code> if it does not contain any "?" or "*" characters.
     * <p>
     * This pattern is "unbounded", it means that the <tt>text</tt> can be anywhere
     * in the matching string. See {@link #getExactPattern(String)} for pattern matching the whole string.
     * @param text the text to get {@link Pattern pattern} for
     * @return the case-insensitive {@link Pattern pattern} or <code>null</code>
     *         if the <tt>text</tt> does not contain any "?" or "*" characters
     * @see #getExactPattern(String)
     */
    public static Pattern getPattern(String text) {
        Parameters.notNull("text", text); // NOI18N

        return getPattern0(text, ".*", ".*"); // NOI18N
    }

    /**
     * Get the case-insensitive {@link Pattern pattern} for the given <tt>String</tt>
     * or <code>null</code> if it does not contain any "?" or "*" characters.
     * <p>
     * This pattern exactly matches the string, it means that the <tt>text</tt> must be fully matched in the
     * matching string. See {@link #getPattern(String)} for pattern matching any substring in the matching string.
     * @param text the text to get {@link Pattern pattern} for
     * @return the case-insensitive {@link Pattern pattern} or <code>null</code>
     *         if the <tt>text</tt> does not contain any "?" or "*" characters
     * @see #getPattern(String)
     */
    public static Pattern getExactPattern(String text) {
        Parameters.notNull("text", text); // NOI18N

        return getPattern0(text, "^", "$"); // NOI18N
    }

    /**
     * Keep all digits and letters only; other characters are replaced with dash ("-"). All upper-cased letters
     * are replaced with dash ("-") and its lower-cased variants. No more than one dash ("-") is added at once.
     * <p>
     * Example: "My Super_Company1" is converted to "my-super-company1".
     * @param input text to be converted
     * @return lower-cased input string
     * @since 2.1
     */
    public static String webalize(String input) {
        StringBuilder sb = new StringBuilder(input.length() * 2);
        final char dash = '-'; // NOI18N
        char lastChar = 0;
        for (int i = 0; i < input.length(); ++i) {
            boolean addDash = false;
            char ch = input.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                if (Character.isUpperCase(ch)) {
                    addDash = true;
                    ch = Character.toLowerCase(ch);
                }
            } else {
                ch = dash;
            }
            if (ch == dash && (lastChar == dash || sb.length() == 0)) {
                continue;
            }
            if (addDash && lastChar != dash && sb.length() > 0) {
                sb.append(dash);
            }
            sb.append(ch);
            lastChar = ch;
        }
        if (lastChar == dash) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Capitalizes first character of the passed input.
     * <p>
     * Example: foobarbaz -> Foobarbaz
     * @param input text to be capitalized, never null or empty
     * @return capitalized input string, never null
     * @since 2.21
     */
    public static String capitalize(String input) {
        Parameters.notEmpty("input", input); //NOI18N
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * Decapitalizes first character of the passed input.
     * <p>
     * Example: Foobarbaz -> foobarbaz
     * @param input text to be decapitalized, never null or empty
     * @return decapitalized input string, never null
     * @since 2.33
     */
    public static String decapitalize(String input) {
        Parameters.notEmpty("input", input); //NOI18N
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    /**
     * Truncates the string with specific width. The trancated string contains
     * the marker's length.
     *
     * <p>
     * Example:<br>
     * "0123456789", start:1, width:5, marker: "..." -&gt; "12..."<br>
     * "0123456789", start:1, width:-4, marker: "..." -&gt; "12..."<br>
     * "0123456789", start:-9, width:5, marker: "..." -&gt; "12..."<br>
     * "0123456789", start:-9, width:-4, marker: "..." -&gt; "12..."<br>
     * "0123456789", start:-7, width:6, marker: "..." -&gt; "345..."<br>
     * "0123456789", start:0, width:-4, marker: "..." -&gt; "012..."<br>
     * "0123456789", start:4, width:6, marker: "..." -&gt; "456789"<br>
     *
     * @param string text to be truncated, never {@code null}
     * @param start the start position. if it's negative, the position from the
     * end of the string
     * @param width the width of the truncated string. it contains the marker's
     * length. if it's negative, truncates the width from the end of the string
     * @param marker the marker, can be null, if it's {@code null}, "..." is
     * used
     * @return the truncated string with specific width and the marker
     * @since 2.83
     */
    public static String truncate(@NonNull String string, int start, int width, @NullAllowed String marker) {
        Parameters.notNull("input", string); // NOI18N
        String trimMarker = "..."; // NOI18N
        if (marker != null) {
            trimMarker = marker;
        }
        int trimStart = start;
        if (trimStart < 0) {
            trimStart += string.length();
        }
        int trimWidth = width;
        if (trimWidth < 0) {
            trimWidth = string.length() + trimWidth - trimStart;
        }
        if (trimStart < 0
                || trimWidth < 0
                || string.length() < trimStart
                || trimWidth < trimMarker.length()) {
            // invalid range
            return string;
        }
        boolean addMarker = trimStart + trimWidth < string.length();
        int trimEnd = !addMarker ? string.length() : trimStart + trimWidth;
        String trimedString = string.substring(trimStart, trimEnd);
        if (addMarker) {
            trimedString = trimedString.substring(0, trimedString.length() - trimMarker.length()) + trimMarker;
        }
        return trimedString;
    }

    private static Pattern getPattern0(String text, String prefix, String suffix) {
        assert text != null;
        assert prefix != null;
        assert suffix != null;

        if (text.contains("?") || text.contains("*")) { // NOI18N
            String pattern = text.replace("\\", "") // remove regexp escapes first // NOI18N
                    .replace(".", "\\.") // NOI18N
                    .replace("-", "\\-") // NOI18N
                    .replace("(", "\\(") // NOI18N
                    .replace(")", "\\)") // NOI18N
                    .replace("[", "\\[") // NOI18N
                    .replace("]", "\\]") // NOI18N
                    .replace("?", ".") // NOI18N
                    .replace("*", ".*"); // NOI18N
            return Pattern.compile(prefix + pattern + suffix, Pattern.CASE_INSENSITIVE); // NOI18N
        }
        return null;
    }
}
