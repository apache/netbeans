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

package org.netbeans.modules.cnd.debugger.gdb2;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 *
 */
public class GdbUtils {
    private GdbUtils() {
    }
    
    public static String gdbToUserEncoding(String string, final String encoding) {
        // The first part transforms string to byte array
        char[] chars = string.toCharArray();
        char next;
        boolean escape = false;
        ArrayList<Byte> _bytes = new ArrayList<Byte>();
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            next = (i + 1) < chars.length ? chars[i + 1] : 0;
            if (escape) {
                // skip escaped char
                escape = false;
            } else if (ch == '\\') {
                if (Character.isDigit(next)) {
                    char[] charVal = {chars[++i], chars[++i], chars[++i]};
                    ch = (char) Integer.parseInt(String.valueOf(charVal), 8);
                } else {
                    escape = true;
                }
            }
            _bytes.add((byte) ch);
        }
        byte[] bytes = new byte[_bytes.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = _bytes.get(i);
        }

        // The second part performs encoding to current coding system
        try {
            string = new String(bytes, encoding);
        } catch (UnsupportedEncodingException e) {
        }
        return string;
    }
    
    public static GdbVersionPeculiarity.Version parseVersionString(String msg) throws NumberFormatException {
        int dot = msg.indexOf('.');

        int first = dot - 1;
        while (first > 0 && Character.isDigit(msg.charAt(first))) {
            first--;
        }
        first = Integer.parseInt(msg.substring(first + 1, dot));

        int last = dot + 1;
        while (last < msg.length() && Character.isDigit(msg.charAt(last))) {
            last++;
        }
        last = Integer.parseInt(msg.substring(dot + 1, last));

        return new GdbVersionPeculiarity.Version(first, last);
    }
}
