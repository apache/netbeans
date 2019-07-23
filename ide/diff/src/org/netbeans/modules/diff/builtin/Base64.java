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
package org.netbeans.modules.diff.builtin;

import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * Base64 utility methods.
 *
 * @author Maros Sandor
 */
class Base64 {
    
    private Base64() {
    }
    
    public static byte [] decode(List<String> ls) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (String s : ls) {
            decode(s, bos);
        }
        return bos.toByteArray();
    }
  
    private static void decode(String s, ByteArrayOutputStream bos) {
        int i = 0;
        int len = s.length();
        while (true) {
            while (i < len && s.charAt(i) <= ' ') i++;
            if (i == len) break;
            int tri = (decode(s.charAt(i)) << 18)
            + (decode(s.charAt(i+1)) << 12)
            + (decode(s.charAt(i+2)) << 6)
            + (decode(s.charAt(i+3)));
          
            bos.write((tri >> 16) & 255);
            if (s.charAt(i+2) == '=') break;
            bos.write((tri >> 8) & 255);
            if (s.charAt(i+3) == '=') break;
            bos.write(tri & 255);
          
            i += 4;
        }
    }

    private static int decode(char c) {
        if (c >= 'A' && c <= 'Z') return ((int) c) - 65;
        else if (c >= 'a' && c <= 'z') return ((int) c) - 97 + 26;
        else if (c >= '0' && c <= '9') return ((int) c) - 48 + 26 + 26;
        else {
            switch (c) {
                case '+': return 62;
                case '/': return 63;
                case '=': return 0;
                default:
                    throw new RuntimeException("unexpected code: " + c);
            }
        }
    }
    
}
