/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.lexer.gen.util;

/**
 * Implementation of some lengthy methods in LexerUtil.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

class LexerGenUtilitiesImpl {

    private static final char[] HEX_DIGIT = {'0','1','2','3','4','5','6','7',
         '8','9','A','B','C','D','E','F'};


    /** Convert string from the shape as it appears
     * in the source file into regular java string.
     */
    public static String fromSource(String s) {
        StringBuffer sb = new StringBuffer();

        int len = s.length();
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (ch == '\\') {
                if (++i >= len) {
                    throwIAE(s);
                }
                
                ch = s.charAt(i);
                switch (ch) {
                    case 'n':
                        ch = '\n';
                        break;

                    case 'r':
                        ch = '\r';
                        break;

                    case 't':
                        ch = '\t';
                        break;

                    case 'b':
                        ch = '\b';
                        break;

                    case 'f':
                        ch = '\f';
                        break;
                        
                    case 'u':
                        int uEnd = ++i + 4;
                        if (uEnd > len) {
                            throwIAE(s);
                        }
                        
                        int val = 0;
                        while (i < uEnd) {
                            ch = s.charAt(i++);
                            if (ch >= '0' && ch <= '9') {
                                val = (val << 4) + ch - '0';
                                
                            } else {
                                ch = Character.toLowerCase(ch);
                                if (ch >= 'a' && ch <= 'f') {
                                    val = (val << 4) + 10 + ch - 'a';
                                    
                                } else {
                                    throw new IllegalArgumentException(
                                        "Malformed \\uxxxx encoding in string='"
                                        + s + "'");
                                }
                            }
                        }
                        ch = (char)val;
                        break;
                        
                    default:
                        // Convert octal constants
                        if (ch >= '0' && ch <= '7') {
                            val = ch - '0';
                            int maxDigCnt = (val < 4) ? 2 : 1;
                            while (maxDigCnt-- > 0) {
                                ch = s.charAt(++i);
                                if (ch < '0' || ch > '7') {
                                    i--;
                                    break;
                                }
                                val = (val << 3) + (val - '0');
                            }
                            ch = (char)val;
                        }
                }
            }
            
            sb.append(ch);
        }
        
        return sb.toString();
    }

    private static void throwIAE(String s) {
        throw new IllegalArgumentException("Unexpected end of string s='" + s + "'");
    }

    static String toElementContent(String text) {
        if (text == null) {
            throw new NullPointerException();
        }
        
        if (checkContentCharacters(text)) return text;
        
        StringBuffer buf = new StringBuffer();
                
        for (int i = 0; i<text.length(); i++) {
            char ch = text.charAt(i);
            if ('<' == ch) {
                buf.append("&lt;");
                continue;
            } else if ('&' == ch) {
                buf.append("&amp;");
                continue;
            } else if ('>' == ch && i>1 && text.charAt(i-2) == ']' && text.charAt(i-1) == ']') {
                buf.append("&gt;");
                continue;
            }
            buf.append(ch);            
        }
        return buf.toString();        
    }

    /**
     * Check if all passed characters match XML expression [2].
     * @return true if no escaping necessary
     * @throws IllegalArgumentException if contains invalid chars
     */
    private static boolean checkContentCharacters(String chars) {
        boolean escape = false;
        for (int i = 0; i<chars.length(); i++) {
            char ch = chars.charAt(i);
            if (((int)ch) <= 93) { // we are UNICODE ']'
                switch (ch) {
                    case 0x9:
                    case 0xA:
                    case 0xD:
                        continue;
                    case '>':       // only ]]> is dangerous
                        if (escape) continue;
                        escape =  i > 0 && (chars.charAt(i - 1) == ']');
                        continue;
                    case '<':
                    case '&':
                        escape = true;
                        continue;                        
                    default:
                        if (((int) ch) < 0x20) {
                            throw new IllegalArgumentException(
                                "Invalid XML character &#" + ((int)ch) + ";.");
                        }
                }
            }
        }
        return escape == false;
    }

}
