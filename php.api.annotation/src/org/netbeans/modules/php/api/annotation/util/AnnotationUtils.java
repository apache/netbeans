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
package org.netbeans.modules.php.api.annotation.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.openide.util.Parameters;

/**
 * Utility class for handling annotations.
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 * @since 0.3
 */
public class AnnotationUtils {

    private static final String PARAM_TYPE_PATTERN = "\\s*=\\s*\\\"\\s*([\\w\\\\]+)\\s*\\\""; //NOI18N

    private static final Pattern INLINE_TYPE_PATTERN = Pattern.compile("@([\\w\\\\]+)"); //NOI18N

    private AnnotationUtils() {
    }

    /**
     * Checks whether the passed {@code lineToCheck} is a type annotation of {@code annotationName}.
     *
     * @param lineToCheck typically one line of a PHPDoc comment; never {@code null}
     * @param annotationName name of possible type annotation; never {@code null}
     * @return {@code true} if {@code lineToCheck} contains type annotation of name {@code annotationName}; {@code false} otherwise
     */
    public static boolean isTypeAnnotation(@NonNull final String lineToCheck, @NonNull final String annotationName) {
        Parameters.notNull("lineToCheck", lineToCheck); //NOI18N
        Parameters.notNull("annotationName", annotationName); //NOI18N
        return lineToCheck.toLowerCase().matches("\\\\?(\\w+\\\\)*" + annotationName.toLowerCase() + "\\s*"); //NOI18N
    }

    /**
     * Extracts type names and their offset ranges from passed {@code line} according to {@code parameterNameRegexs}.
     * <p>
     * Example:
     * <p>
     * To extract just {@code baz} from the line:
     * <code>Foo(bar = "baz", not = "really")</code>
     * <p>
     * use regex of:
     * <code>bar</code>
     * <p>
     * The right side of a regex for a whole parametter assignment (<code> = "baz"</code>) is handled automatically.
     *
     * @param line typically one line of a PHPDoc comment; never {@code null}
     * @param parameterNameRegexs regular expressions which describe parameter name (left side of a parameter assignment); never {@code null}
     * @return offset ranges and corresponding type names; never {@code null}
     */
    public static Map<OffsetRange, String> extractTypesFromParameters(@NonNull final String line, @NonNull final Set<String> parameterNameRegexs) {
        Parameters.notNull("line", line); //NOI18N
        Parameters.notNull("parameterNameRegexs", parameterNameRegexs); //NOI18N
        final Map<OffsetRange, String> result = new HashMap<OffsetRange, String>();
        for (String parameterNameRegex : parameterNameRegexs) {
            Pattern pattern = Pattern.compile(parameterNameRegex + PARAM_TYPE_PATTERN);
            final Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                int lastGroupId = matcher.groupCount();
                result.put(new OffsetRange(matcher.start(lastGroupId), matcher.end(lastGroupId)), matcher.group(lastGroupId));
            }
        }
        return result;
    }

    /**
     * Extracts inline annotations from a passed {@code line} which corresponds with {@code expectedAnnotations}.
     *
     * @param line typically one line of a PHPDoc comment; never {@code null}
     * @param expectedAnnotations annotation names which one expects in a passed PHPDoc {@code line} without leading "at" sign (@); never {@code null}
     * @return offset ranges and corresponding annotation names; never {@code null}
     */
    public static Map<OffsetRange, String> extractInlineAnnotations(@NonNull final String line, @NonNull final Set<String> expectedAnnotations) {
        Parameters.notNull("line", line); //NOI18N
        Parameters.notNull("expectedAnnotations", expectedAnnotations); //NOI18N
        final Map<OffsetRange, String> result = new HashMap<OffsetRange, String>();
        final Matcher matcher = INLINE_TYPE_PATTERN.matcher(line);
        while (matcher.find()) {
            if (isExpectedType(matcher.group(1), expectedAnnotations)) {
                result.put(new OffsetRange(matcher.start(1), matcher.end(1)), matcher.group(1));
            }
        }
        return result;
    }

    /**
     * Checks whether the passed {@code typeName} is one of {@code expectedTypes}.
     *
     * @param typeName type name to check; never {@code null}
     * @param expectedTypes types which one expects; never {@code null}
     * @return {@code true} if passed {@code typeName} is one of {@code expectedTypes}; {@code false} otherwise
     */
    private static boolean isExpectedType(@NonNull final String typeName, @NonNull final Set<String> expectedTypes) {
        Parameters.notNull("typeName", typeName); //NOI18N
        Parameters.notNull("expectedTypes", expectedTypes); //NOI18N
        boolean result = false;
        for (String annotation : expectedTypes) {
            if (typeName.toLowerCase().endsWith(annotation.toLowerCase())) {
                result = true;
                break;
            }
        }
        return result;
    }

}
