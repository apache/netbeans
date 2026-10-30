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

package org.netbeans.modules.properties;

/**
 * Contains conversion utilities which allow reading and storing a properties file
 * while preserving formatting and comments that user may have entered.
 * <p>
 * Huge portions of this class marked by <i>// prj40 trunk compatability</i>
 * are there only for being able to run unmodified i18n module code in
 * prj40_prototype branch and trunk. Properties module is not
 * prj40 branched and it is updated by <tt>cvs up -f -rprj40_prototype</tt>.
 * <br>
 * 28th March 2003 Petr Kuzel
 *
 * @author Petr Jiricka
 * @author Petr Kuzel - simplification
 */
public class UtilConvert {

    private UtilConvert() {
    }

    /**
     * These characters play role as key-value sepsrators
     */
    public  static final String keyValueSeparators = "=: \t\r\n\f";

    public  static final String strictKeyValueSeparators = "=:";

    public  static final String whiteSpaceChars = " \t\r\n\f";

    /**
     * Converts encoded \\uxxxx to unicode chars
     * and changes special saved chars to their original forms
     */
    public static String loadConvert (String theString) {
        char aChar;
        final int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);

        main:
        for(int x=0; x<len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\' && x != len) {
                aChar = theString.charAt(x++);
                if(aChar == 'u') {
                    if (x > len - 4) {
                        outBuffer.append('\\').append('u');
                        continue main;
                    }
                    // Read the xxxx
                    int value=0;
                    for (int i=0; i<4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                        case '0': case '1': case '2': case '3': case '4':
                        case '5': case '6': case '7': case '8': case '9':
                            value = (value << 4) + aChar - '0';
                            break;
                        case 'a': case 'b': case 'c':
                        case 'd': case 'e': case 'f':
                            value = (value << 4) + 10 + aChar - 'a';
                            break;
                        case 'A': case 'B': case 'C':
                        case 'D': case 'E': case 'F':
                            value = (value << 4) + 10 + aChar - 'A';
                            break;
                        default:
                            /*
                             * Handle a malformed \\uxxxx encoding:
                             *
                             * We want to print "\\u" plus all the hexadecimal
                             * digits that passed the above switch.
                             * To achieve it, print ("\\u")...,
                             */
                            outBuffer.append('\\').append('u');

                            /* ... move 'x' back to character after "u"... */
                            x -= i + 1;

                            /* ... and continue with the main loop. */
                            continue main;
                        }
                    }
                    outBuffer.append((char)value);
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }

}
