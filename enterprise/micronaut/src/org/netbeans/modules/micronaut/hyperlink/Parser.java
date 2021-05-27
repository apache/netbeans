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
package org.netbeans.modules.micronaut.hyperlink;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Jan Lahoda
 */
public final class Parser {
    private Parser() {}

    private static final Pattern URL_PATTERN = Pattern.compile("(https?|ftps?|file|nbfs)://[0-9a-zA-Z/.?%+_~=\\\\&@$\\-#,:!/(/)]*"); //NOI18N

    public static Iterable<int[]> recognizeURLs(CharSequence text) {
        return recognizeURLsREBased(text);
    }

    public static Iterable<int[]> recognizeURLsREBased(CharSequence text) {
        Matcher m = URL_PATTERN.matcher(text);
        List<int[]> result = new LinkedList<int[]>();

        while (m.find()) {
            result.add(new int[] {m.start(), m.start() + m.group(0).length()});
        }
        
        return result;
    }

    private static final Pattern CHARSET = Pattern.compile("charset=([^;]+)(;|$)", Pattern.MULTILINE);//NOI18N
    public static String decodeContentType(String contentType) {
        if (contentType == null) return null;

        if (contentType != null) {
            Matcher m = CHARSET.matcher(contentType);

            if (m.find()) {
                return m.group(1);
            }
        }

        return null;
    }

}
