/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 2008-2009 Sun
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
package org.netbeans.modules.editor.url;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Jan Lahoda
 */
public final class Parser {

    private enum STATE {
        START, H, HT, F, HTT_FT, 
        HTTP_FTP,
        HTTPS,
        HTTPC, // // (ht|f)tp(s?)
        HTTPCS, // (ht|f)tp(s?):/
        FI,
        FIL,
        FILE,
        END // (ht|f)tp(s?)://
    }

    private Parser() {}

    public static Iterable<int[]> recognizeURLs(CharSequence text) {
        List<int[]> result = new LinkedList<int[]>();
        STATE state = STATE.START;
        int lastURLStart = -1;
        
        OUTER: for (int cntr = 0; cntr < text.length(); cntr++) {
            char ch = text.charAt(cntr);

            if (state == STATE.END) {
                if (Character.isLetterOrDigit(ch)) {
                    continue OUTER;
                }

                switch (ch) {
                    case '/': case '.': case '?': case '+': //NOI18N
                    case '%': case '_': case '~': case '=': //NOI18N
                    case '\\':case '&': case '$': case '-': //NOI18N
                    case '#': case ',': case ':': case ';': //NOI18N
                    case '!': case '(': case ')': //NOI18N
                        continue OUTER;
                }

                assert lastURLStart != (-1);
                result.add(new int[] {lastURLStart, cntr});

                lastURLStart = (-1);
                state = STATE.START;
                continue OUTER;
            }

            switch (ch) {
                case 'h': //NOI18N
                    if (state == STATE.START) {
                        lastURLStart = cntr;
                        state = STATE.H;
                        continue OUTER;
                    }
                    break;
                case 't': //NOI18N
                    if (state == STATE.H) {
                        state = STATE.HT;
                        continue OUTER;
                    } else if (state == STATE.HT) {
                            state = STATE.HTT_FT;
                            continue OUTER;
                    } else if (state == STATE.F) {
                        state = STATE.HTT_FT;
                        continue OUTER;
                    }
                    break;
                case 'f': //NOI18N
                    if (state == STATE.START) {
                        lastURLStart = cntr;
                        state = STATE.F;
                        continue OUTER;
                    }
                    break;
                case 'i': //NOI18N
                    if (state == STATE.F) {
                        state = STATE.FI;
                        continue OUTER;
                    }
                case 'l': //NOI18N
                    if (state == STATE.FI) {
                        state = STATE.FIL;
                        continue OUTER;
                    }
                case 'e': //NOI18N
                    if (state == STATE.FIL) {
                        state = STATE.FILE;
                        continue OUTER;
                    }
                case 'p': //NOI18N
                    if (state == STATE.HTT_FT) {
                        state = STATE.HTTP_FTP;
                        continue OUTER;
                    }
                    break;
                case 's': //NOI18N
                    if (state == STATE.HTTP_FTP) {
                        state = STATE.HTTPS;
                        continue OUTER;
                    }
                    break;
                case ':': //NOI18N
                    if (state == STATE.HTTP_FTP || state == STATE.HTTPS || state == STATE.FILE) {
                        state = STATE.HTTPC;
                        continue OUTER;
                    }
                    break;
                case '/' : //NOI18N
                    if (state == STATE.HTTPC) {
                        state = STATE.HTTPCS;
                        continue OUTER;
                    } else if (state == STATE.HTTPCS) {
                        state = STATE.END;
                        continue OUTER;
                    }
                    break;
            }

            state = STATE.START;
            lastURLStart = (-1);
        }

        if (lastURLStart != (-1) && state == STATE.END) {
            result.add(new int[] {lastURLStart, text.length()});
        }
        
        return result;
    }
    
    private static final Pattern URL_PATTERN = Pattern.compile("(ht|f)(tp(s?)|ile)://[0-9a-zA-Z/.?%+_~=\\\\&$\\-#,:!/(/)]*"); //NOI18N

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
