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
