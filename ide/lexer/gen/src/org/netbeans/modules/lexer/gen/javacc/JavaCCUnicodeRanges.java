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

package org.netbeans.modules.lexer.gen.javacc;

import org.netbeans.modules.lexer.gen.util.UnicodeRanges;

/**
 * Program that writes Unicode character ranges to the output
 * depending on the method being used.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavaCCUnicodeRanges extends UnicodeRanges {

    public static String findRangesDescription(int testedMethod, int indent) {
        StringBuffer sb = new StringBuffer();
        char[] ranges = findRanges(testedMethod);
        for (int i = 0; i < ranges.length;) {
            if (i > 0) {
                sb.append(",\n");
            }
            indent(sb, indent);
            int rangeStart = ranges[i++];
            int rangeEnd = ranges[i++];
            appendUnicodeChar(sb, (char)rangeStart, '"');
            if (rangeStart < rangeEnd) { // interval
                sb.append(" - ");
                appendUnicodeChar(sb, (char)(rangeEnd), '"');
            }
        }
        
        return sb.toString();
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(usage());
        }

        System.out.println(findRangesDescription(
            Integer.parseInt(args[0]),
            Integer.parseInt(args[1])
        ));
    }

}
