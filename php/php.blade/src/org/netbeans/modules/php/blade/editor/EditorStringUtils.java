/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.blade.editor;

/**
 *
 * @author bhaidu
 */
public final class EditorStringUtils {

    public static final String NAMESPACE_SEPARATOR = "\\"; // NOI18N

    private EditorStringUtils() {
    }

    public static boolean isQuotedString(String text) {
        if (text.length() < 2) {
            return false;
        }
        return (text.startsWith("'") && text.endsWith("'")) // NOI18N
                || (text.startsWith("\"") && text.endsWith("\"")); // NOI18N
    }

    public static String stripSurroundingQuotes(String text) {
        if (!isQuotedString(text)) {
            return text;
        }
        return text.substring(1, text.length() - 1);
    }

    public static String trimNamespace(String namespace) {
        assert namespace.length() > 2;
        int subOffset = namespace.startsWith(NAMESPACE_SEPARATOR) ? 1 : 0;
        int endOffset = namespace.endsWith(NAMESPACE_SEPARATOR) ? 1 : 0;
        return namespace.substring(subOffset, namespace.length() - endOffset);
    }
}
