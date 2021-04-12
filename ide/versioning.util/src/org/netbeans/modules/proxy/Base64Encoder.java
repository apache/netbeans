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
package org.netbeans.modules.proxy;

import java.util.Base64;

/**
 * Bas64 encode utility class.
 *
 * @author Maros Sandor
 * @deprecated prefer java.util.Base64 instead
 */
@Deprecated
public class Base64Encoder {

    private Base64Encoder() {
    }

    public static String encode(byte[] data) {
        return encode(data, false);
    }

    public static String encode(byte[] data, boolean useNewlines) {
        final String encoded = Base64.getEncoder().encodeToString(data);
        if (useNewlines) {
            return wrapText(encoded, 60, System.getProperty("line.separator"));
        } else {
            return encoded;
        }
    }

    static String wrapText(String text, int length, String separator) {
        if (length > 0) {
            StringBuilder sb = new StringBuilder(text.length() + (((text.length() - 1) / length) * separator.length()));
            int idx = 0;
            while (idx < text.length()) {
                if (idx > 0) {
                    sb.append(separator);
                }
                sb.append(text.substring(idx, Math.min(idx + length, text.length())));
                idx += length;
            }
            return sb.toString();
        } else {
            return text;
        }
    }

    public static byte[] decode(String s) {
        return Base64.getDecoder().decode(s);
    }

}
