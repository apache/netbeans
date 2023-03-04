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
package org.netbeans.api.search;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 * Helper for creating regular expression patterns for file-name patterns that
 * are specified in search options.
 *
 * @author jhavlin
 */
public final class RegexpUtil {

    private RegexpUtil() {
    }

    /**
     * Translates the given simple pattern (or several patterns) to a single
     * regular expression.
     *
     * @param simplePatternList pattern list to be translated
     * @return regular expression corresponding to the simple pattern (or to the
     * list of simple patterns)
     */
    private static String makeMultiRegexp(String simplePatternList) {
        if (simplePatternList.length() == 0) {              //trivial case
            return simplePatternList;
        }

        if (Pattern.matches("[a-zA-Z0-9]*", simplePatternList)) {       //NOI18N
            return simplePatternList;                       //trivial case
        }

        StringBuilder buf = new StringBuilder(simplePatternList.length() + 16);
        boolean lastWasSeparator = false;
        boolean quoted = false;
        boolean starPresent = false;
        for (char c : simplePatternList.toCharArray()) {
            if (quoted) {
                if ((c == 'n') || isSpecialCharacter(c)) {
                    buf.append('\\');
                }
                buf.append(c);
                quoted = false;
            } else if ((c == ',') || (c == ' ')) {
                if (starPresent) {
                    buf.append('.').append('*');
                    starPresent = false;
                }
                lastWasSeparator = true;
            } else {
                if (lastWasSeparator && (buf.length() != 0)) {
                    buf.append('|');
                }
                if (c == '?') {
                    buf.append('.');
                } else if (c == '*') {
                    starPresent = true;
                } else {
                    if (starPresent) {
                        buf.append('.').append('*');
                        starPresent = false;
                    }
                    if (c == '\\') {
                        quoted = true;
                    } else {
                        if (isSpecialCharacter(c)) {
                            buf.append('\\');
                        }
                        buf.append(c);
                    }
                }
                lastWasSeparator = false;
            }
        }
        if (quoted) {
            buf.append('\\').append('\\');
        } else if (starPresent) {
            buf.append('.').append('*');
        }
        return buf.toString();
    }

    /**
     * Translates the file name pattern to a regular expression pattern and
     * compiles it. The compiled pattern is stored to field {@link #fileNamePattern}.
     */
    private static Pattern compileSimpleFileNamePattern(String expr)
            throws PatternSyntaxException {

        assert expr != null;

        return Pattern.compile(makeMultiRegexp(expr),
                Pattern.CASE_INSENSITIVE);

    }

    /**
     * Compile file name regular expression pattern, if it is not null. On
     * success, field fileNamePattern is set to newly compiled pattern.
     */
    private static Pattern compileRegexpFileNamePattern(String expr)
            throws PatternSyntaxException {

        assert expr != null;
        return Pattern.compile(expr, Pattern.CASE_INSENSITIVE);
    }

    private static boolean isSpecialCharacter(char c) {
        return (c > 0x20) && (c < 0x80) && !isAlnum(c);
    }

    private static boolean isAlnum(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private static boolean isAlpha(char c) {
        c |= 0x20;  //to lower case
        return (c >= 'a') && (c <= 'z');
    }

    private static boolean isDigit(char c) {
        return (c >= '0') && (c <= '9');
    }

    /**
     * Compile file name pattern for search options.
     *
     * @param searchScopeOptions Search scope options containing file name
     * pattern specification.
     * @return Pattern for matching file names or file paths.
     * @throws PatternSyntaxException Thrown if file name pattern is invalid.
     */
    public static Pattern makeFileNamePattern(
            @NonNull SearchScopeOptions searchScopeOptions)
            throws PatternSyntaxException {

        Parameters.notNull("searchScopeOptions", searchScopeOptions);   //NOI18N
        if (searchScopeOptions.isRegexp()) {
            return compileRegexpFileNamePattern(searchScopeOptions.getPattern());
        } else {
            return compileSimpleFileNamePattern(searchScopeOptions.getPattern());
        }
    }
}
