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

import java.io.ByteArrayOutputStream;

/**
 * Bas64 encode utility class.
 *
 * @author Maros Sandor
 */
public class Base64Encoder {

    private static final char [] characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    private Base64Encoder() {
    }

    public static String encode(byte [] data) {
        return encode(data, false);
    }
    
    public static String encode(byte [] data, boolean useNewlines) {
        int length = data.length;
        StringBuffer sb = new StringBuffer(data.length * 3 / 2);

        int end = length - 3;
        int i = 0;
        int lineCount = 0;

        while (i <= end) {
            int d = ((((int) data[i]) & 0xFF) << 16) | ((((int) data[i + 1]) & 0xFF) << 8) | (((int) data[i + 2]) & 0xFF);
            sb.append(characters[(d >> 18) & 0x3F]);
            sb.append(characters[(d >> 12) & 0x3F]);
            sb.append(characters[(d >> 6) & 0x3F]);
            sb.append(characters[d & 0x3F]);
            i += 3;
            
            if (useNewlines && lineCount++ >= 14) {
                lineCount = 0;
                sb.append(System.getProperty("line.separator"));
            }
        }

        if (i == length - 2) {
            int d = ((((int) data[i]) & 0xFF) << 16) | ((((int) data[i + 1]) & 0xFF) << 8);
            sb.append(characters[(d >> 18) & 0x3F]);
            sb.append(characters[(d >> 12) & 0x3F]);
            sb.append(characters[(d >> 6) & 0x3F]);
            sb.append("=");
        } else if (i == length - 1) {
            int d = (((int) data[i]) & 0xFF) << 16;
            sb.append(characters[(d >> 18) & 0x3F]);
            sb.append(characters[(d >> 12) & 0x3F]);
            sb.append("==");
        }
        return sb.toString();
    }
    
    public static byte [] decode(String s) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        decode(s, bos);
        return bos.toByteArray();
}
  
    private static void decode(String s, ByteArrayOutputStream bos) {
        int i = 0;
        int len = s.length();
        for (;;) {
            while (i < len && s.charAt(i) <= ' ') i++;
            if (i == len) break;
            int tri = (decode(s.charAt(i)) << 18) + (decode(s.charAt(i+1)) << 12) + (decode(s.charAt(i+2)) << 6) + (decode(s.charAt(i+3)));
            bos.write((tri >> 16) & 255);
            if (s.charAt(i+2) == '=') break;
            bos.write((tri >> 8) & 255);
            if (s.charAt(i+3) == '=') break;
            bos.write(tri & 255);
            i += 4;
        }
    }

    private static int decode(char c) {
        if (c >= 'A' && c <= 'Z') {
            return ((int) c) - 65;
        } else if (c >= 'a' && c <= 'z') {
            return ((int) c) - 97 + 26;
        } else if (c >= '0' && c <= '9') {
            return ((int) c) - 48 + 26 + 26;
        } else switch (c) {
            case '+': 
                return 62;
            case '/': 
                return 63;
            case '=': 
                return 0;
            default:
                throw new RuntimeException("Base64: unexpected code: " + c);
        }
    }
}
