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
package org.netbeans.modules.web.clientproject.api.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.openide.util.Parameters;

/**
 * Miscellaneous utility methods for strings.
 * @since 1.20
 */
public final class StringUtilities {

    private StringUtilities() {
    }

    /**
     * Return {@code true} if the String is not {@code null}
     * and has any character after trimming.
     * @param input input <tt>String</tt>, can be {@code null}.
     * @return {@code true} if the String is not {@code null}
     *         and has any character after trimming.
     * @see #isEmpty(String)
     */
    public static boolean hasText(String input) {
        return input != null && !input.trim().isEmpty();
    }

    /**
     * Return {@code true} if the String is {@code null}
     * or has no characters.
     * @param input input <tt>String</tt>, can be {@code null}
     * @return {@code true} if the String is {@code null}
     *         or has no characters
     * @see  #hasText(String)
     */
    public static boolean isEmpty(String input) {
        return input == null || input.isEmpty();
    }

    /**
     * Implode list of strings to one string using delimiter.
     * @param items list of strings to be imploded, can be empty (but not {@code null})
     * @param delimiter delimiter to be used
     * @return one string of imploded strings using delimiter, never {@code null}
     * @see #explode(String, String)
     */
    public static String implode(List<String> items, String delimiter) {
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
     * @param string string to be exploded, can be {@code null}
     * @param delimiter delimiter to be used
     * @return list of exploded strings using delimiter
     * @see #implode(List, String)
     */
    public static List<String> explode(String string, String delimiter) {
        Parameters.notNull("delimiter", delimiter); // NOI18N

        if (!hasText(string)) {
            return Collections.<String>emptyList();
        }
        return Arrays.asList(string.split(Pattern.quote(delimiter)));
    }

}
