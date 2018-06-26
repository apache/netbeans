/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.api.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
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
