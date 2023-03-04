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
package org.netbeans.modules.languages.neon.completion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class CompletionUtils {
    private static final Pattern TYPE_NAME_PATTERN = Pattern.compile("([a-zA-Z0-9_\\\\]+)::[a-zA-Z0-9_]*");
    private static final Pattern METHOD_PREFIX_PATTERN = Pattern.compile("[a-zA-Z0-9_\\\\]+::([a-zA-Z0-9_]*)");

    private CompletionUtils() {
    }

    public static boolean startsWith(String theString, String prefix) {
        return prefix.length() == 0 ? true : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    public static String extractTypeName(String prefix) {
        String result = null;
        Matcher matcher = TYPE_NAME_PATTERN.matcher(prefix);
        if (matcher.matches()) {
            result = matcher.group(1);
        }
        return result;
    }

    public static String extractMethodPrefix(String prefix) {
        String result = null;
        Matcher matcher = METHOD_PREFIX_PATTERN.matcher(prefix);
        if (matcher.matches()) {
            result = matcher.group(1);
        }
        return result;
    }

}
