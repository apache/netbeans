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

package org.netbeans.modules.lexer.gen.util;

/**
 * Program that generates Unicode character ranges array
 * depending on the method being used.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class UnicodeRanges {

    public static final int IS_JAVA_IDENTIFIER_START = 1;

    public static final int IS_JAVA_IDENTIFIER_PART = 2;

    public static char[] findRanges(int testedMethod) {
        char[] ranges = new char[16]; // will grow very likely
        int rangesCount = 0;
        int rangeStart = -1;
        for (int i = 0; i < 65536; i++) {
            boolean valid = false;
            switch (testedMethod) {
                case IS_JAVA_IDENTIFIER_START:
                    valid = Character.isJavaIdentifierStart((char)i);
                    break;
                    
                case IS_JAVA_IDENTIFIER_PART:
                    valid = Character.isJavaIdentifierPart((char)i);
                    break;
                    
            }
            
            // The following code gets rid of post-handling code after for loop
            if (i == 65535 && valid) {
                if (rangeStart < 0) {
                    rangeStart = i;
                }
                i++;
                valid = false;
            }
                
            if (valid) {
                if (rangeStart < 0) {
                    rangeStart = i;
                }
                
            } else { // not valid
                if (rangeStart >= 0) {
                    // Check sufficient space in ranges array
                    if (ranges.length - rangesCount < 2) {
                        char[] tmp = new char[ranges.length * 2];
                        System.arraycopy(ranges, 0, tmp, 0, rangesCount);
                        ranges = tmp;
                    }
                    ranges[rangesCount++] = (char)rangeStart;
                    ranges[rangesCount++] = (char)(i - 1);

                    rangeStart = -1;
                }
            }
        }
        
        if (rangesCount < ranges.length) {
            char[] tmp = new char[rangesCount];
            System.arraycopy(ranges, 0, tmp, 0, rangesCount);
            ranges = tmp;
        }
        return ranges;
    }
    
    public static void appendUnicodeChar(StringBuffer sb, char ch, char quoteChar) {
        String ret = Integer.toHexString(ch);
        while (ret.length() < 4) {
            ret = "0" + ret;
        }
        sb.append(quoteChar);
        sb.append("\\u");
        sb.append(ret);
        sb.append(quoteChar);
    }
    
    public static void indent(StringBuffer sb, int indent) {
        while (indent-- > 0) {
            sb.append(' ');
        }
    }
    
    protected static String usage() {
        return "Prints ranges of characters belonging to selected category\n"
            + "arg0=Tested method:\n"
            + "        1 - Character.isJavaIdentifierStart()\n"
            + "        2 - Character.isJavaIdentifierPart()\n"
            + "arg1=Indentation e.g. 8\n";
    }
    
}
