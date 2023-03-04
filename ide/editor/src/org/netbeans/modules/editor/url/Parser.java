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
        N, NB, NBF_F, NBFS,
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
                    case '!': case '(': case ')': case '@': //NOI18N
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
                case 'n': //NOI18N
                    if (state == STATE.START) {
                        lastURLStart = cntr;
                        state = STATE.N;
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
                case 'b': //NOI18N
                    if (state == STATE.N) {
                        state = STATE.NB;
                        continue OUTER;
                    }
                    break;
                case 'f': //NOI18N
                    if (state == STATE.START) {
                        lastURLStart = cntr;
                        state = STATE.F;
                        continue OUTER;
                    } else if (state == STATE.NB) {
                        state = STATE.NBF_F;
                        continue OUTER;
                    }
                    break;
                case 'i': //NOI18N
                    if (state == STATE.F || state == STATE.NBF_F) {
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
                    } else if (state == STATE.NBF_F) {
                        state = STATE.NBFS;
                        continue OUTER;
                    }
                    break;
                case ':': //NOI18N
                    if (state == STATE.HTTP_FTP || state == STATE.HTTPS || state == STATE.FILE || state == STATE.NBFS) {
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
                    } else if (state == STATE.NBFS) {
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

    private static final Pattern URL_PATTERN = Pattern.compile("(ht|f|n)(tp(s?)|ile|bfs)://[0-9a-zA-Z/.?%+_~=\\\\&@$\\-#,:!/(/)]*"); //NOI18N

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
