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

package org.netbeans.modules.gradle.spi.actions;

import java.util.Map;
import java.util.Set;
import org.openide.util.Lookup;

/**
 * Plugins which would like to add more replace tokens inside the Gradle
 * command line evaluation process shall register implementations of this
 * interface into the project Lookup.
 *
 * @since 1.0
 * @author Laszlo Kishalmi
 */
public interface ReplaceTokenProvider {

    /**
     * The list of the tokens this class implements.
     * @return the list of the supported tokens
     */
    Set<String> getSupportedTokens();

    /**
     * The implementation shall provide values for the tokens evaluating them
     * in the given context.
     * 
     * @param action the id (name) of the action
     * @param context the context where the action is being called.
     * @return map of tokens and values evaluated in the given context.
     */
    Map<String, String> createReplacements(String action, Lookup context);

    /**
     * Replaces tokens in the given String. The format of the token marker is
     * the following: {@code ${<token_key>[,<default value>]}}
     * <p>
     * If there would be no value or default value provided the token marker
     * will be left untouched.
     *
     * @param line the input line with token markers
     * @param replaceMap key-value map for the replacement
     *
     * @return the line with replaced tokens
     *
     * @since 2.6
     */
    public static String replaceTokens(String line, Map<String, String> replaceMap) {
        StringBuilder sb = new StringBuilder(line);
        int start = sb.indexOf("${");
        while (start >= 0) {
            int end = sb.indexOf("}", start);
            int comma = sb.indexOf(",", start);
            int keyEnd = comma > start && comma < end ? comma : end;
            String key = sb.substring(start + 2, keyEnd);
            String defaultValue = comma > start && comma < end ? sb.substring(comma + 1, end) : null;
            String value = replaceMap.get(key);
            value = value != null ? value : defaultValue;
            if (value != null) {
                sb.replace(start, end + 1, value);
                start = sb.indexOf("${");
            } else {
                start = sb.indexOf("${", end);
            }
        }
        return sb.toString();
    }
}
