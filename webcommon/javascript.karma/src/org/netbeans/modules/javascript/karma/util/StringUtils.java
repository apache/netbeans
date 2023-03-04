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
package org.netbeans.modules.javascript.karma.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Parameters;

// XXX copied from PHP
/**
 * Miscellaneous string utilities.
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

}
