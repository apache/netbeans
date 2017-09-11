/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
