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

package org.netbeans.modules.groovy.editor.completion.util;

import java.util.ArrayList;
import java.util.List;

/**
 * CamelCase support class. Might be used to compare if we are in the right place
 * to complete items based on CamelCase prefix.
 *
 * @author Martin Janicek
 */
public class CamelCaseUtil {

    /**
     * Finds out if the given camelCasePrefix match to longName string.
     *
     * For example if longName will be "StringBuilder", it will return true for these
     * camelCasePrefixes: "SB", "StrB", "SBu", "StBui", "StringB", "SBuilder" and so on
     *
     * @param longName
     * @param camelCasePrefix
     * @return
     */
    public static boolean compareCamelCase(String longName, String camelCasePrefix) {
        List<String> splittedClassName = splitByUpperCases(longName);
        List<String> splittedPrefixes = splitByUpperCases(camelCasePrefix);

        if (splittedPrefixes.size() > splittedClassName.size()) {
            return false;
        }

        for (int i = 0; i < splittedPrefixes.size(); i++) {
            if (splittedClassName.get(i).startsWith(splittedPrefixes.get(i)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the first word for the given longName.
     * For example if longName will be "StriBC", it will return "Stri". In general
     * it means it always return a substring from the beginning to the second upper
     * case character (which is 'B' in this case).
     *
     * @param longName whole type name
     * @return first camel case word as described above
     */
    public static String getCamelCaseFirstWord(String longName) {
        List<String> splittedPrefix = splitByUpperCases(longName);
        if (splittedPrefix.isEmpty()) {
            return "";
        } else {
            return splittedPrefix.get(0);
        }
    }

    /**
     * For the given prefix returns list of subPrefixes where each of them starts
     * with an uppercases letter. For example:
     *
     * If parameter will be "StrBui", this method will return list containing two Strings ("Str" and "Bui")
     * If parameter will be "NGC", this method will return three Strings ("N", "G", "C")
     *
     * @param prefix which we want to split
     * @return list of splitted prefixes
     */
    private static List<String> splitByUpperCases(String prefix) {
        List<String> splitedPrefixes = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < prefix.length(); i++) {
            char actualChar = prefix.charAt(i);
            if (Character.isUpperCase(actualChar)) {

                // We found an upper case letter - save current context if it's
                // not empty, clear builder cache and continue with iteration

                if (builder.length() != 0) {
                    splitedPrefixes.add(builder.toString());
                }

                builder.delete(0, builder.length());
            }
            builder.append(actualChar);
        }
        splitedPrefixes.add(builder.toString()); // Save the last 'word'

        return splitedPrefixes;
    }
}
