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

package org.netbeans.modules.bugtracking.commons;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.net.URL;
import org.openide.awt.HtmlBrowser;
import javax.swing.text.StyledDocument;
import java.awt.event.MouseAdapter;
import java.util.logging.Level;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.net.URI;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import static org.netbeans.modules.bugtracking.commons.WebUrlHyperlinkSupport.SearchMachine.State.*;

/**
 * Finds http(s) addresses in a text pane and makes hyperlinks out of them.
 *
 * @author Marian Petras
 */
class WebUrlHyperlinkSupport {

    static void register(final JTextPane pane) {
        final StyledDocument doc = pane.getStyledDocument();
        String text = "";
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
            Support.LOG.log(Level.SEVERE, null, ex);
        }
        final int[] boundaries = findBoundaries(text);
        if ((boundaries != null) && (boundaries.length != 0)) {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Style defStyle = StyleContext.getDefaultStyleContext()
                                     .getStyle(StyleContext.DEFAULT_STYLE);
                    final Style hlStyle = doc.addStyle("regularBlue-url", defStyle);      //NOI18N
                    hlStyle.addAttribute(HyperlinkSupport.URL_ATTRIBUTE, new UrlAction());
                    StyleConstants.setForeground(hlStyle, UIUtils.getLinkColor());
                    StyleConstants.setUnderline(hlStyle, true);
                    for (int i = 0; i < boundaries.length; i+=2) {
                        doc.setCharacterAttributes(boundaries[i], boundaries[i + 1] - boundaries[i], hlStyle, true);
                    }
                    pane.removeMouseListener(getUrlMouseListener());
                    pane.addMouseListener(getUrlMouseListener());
                }
            });
        }
    }

    private static MouseAdapter urlListener;

    private static MouseAdapter getUrlMouseListener() {
        if (urlListener == null) {
            urlListener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            JTextPane pane = (JTextPane) e.getSource();
                            StyledDocument doc = pane.getStyledDocument();
                            Element elem = doc.getCharacterElement(pane.viewToModel(e.getPoint()));
                            AttributeSet as = elem.getAttributes();

                            UrlAction urlAction = (UrlAction) as.getAttribute(HyperlinkSupport.URL_ATTRIBUTE);
                            if (urlAction != null) {
                                int startOffset = elem.getStartOffset();
                                int endOffset = elem.getEndOffset();
                                int length = endOffset - startOffset;
                                String hyperlinkText = doc.getText(startOffset, length);
                                urlAction.openUrlHyperlink(hyperlinkText);
                                return;
                            }
                        }
                    } catch (Exception ex) {
                        Support.LOG.log(Level.SEVERE, null, ex);
                    }
                }
            };
        }
        return urlListener;
    }

    private static class UrlAction {
        void openUrlHyperlink(String hyperlinkText) {
            try {
                URL url = new URI(hyperlinkText).toURL();
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            } catch (Exception ex) {
                assert false;
                Support.LOG.log(Level.WARNING,
                                              "Could not open URL: "    //NOI18N
                                                      + hyperlinkText);
            }
        }
    }
    
    static int[] findBoundaries(String text) {
        return new SearchMachine(text).findBoundaries();
    }

    static final class SearchMachine {

        enum State {
            WAITING_FOR_INIT_CHAR,
            WAITING_FOR_URI_END_CHAR,
            INIT,
            SCHEME,
            HIER_START,
            HIER_SLASH,
            USERINFO_OR_HOST_START,
            USERINFO_OR_PORT_NUMBER,
            USERINFO,
            HOST_START,
            USERINFO_OR_IPV4_OR_REGNAME,
            USERINFO_OR_REGNAME,
            IP_LITERAL_START,
            IPV6,
            IPV6_DDOT,
            IPV6_QDOT_1,
            IPV6_QDOT_2,
            IP_FUTURE_START,
            IP_FUTURE_VER_NUM,
            IP_FUTURE_ADDR,
            IP_FUTURE_ADDR_START,
            IPV4_OR_REGNAME,
            PORT_NUMBER_OR_AUTHORITY_END,
            PORT_NUMBER,
            REGNAME,
            PATH,
            QUERY,
            FRAGMENT
        }

        private static final String URI_INIT_CHARS = " <[(\"'";         //NOI18N
        private static final String GEN_DELIMS = ":/?#[]@";             //NOI18N
        private static final String SUB_DELIMS = "!$&'()*+,;=";         //NOI18N
        private static final String RESERVED = GEN_DELIMS + SUB_DELIMS;
        private static final String PUNCT_CHARS = ".,:;()[]{}";         //NOI18N
        private static final String[] SUPPORTED_SCHEMES
                                      = new String[] {"http", "https", "mailto"}; //NOI18N

        private static final int LOWER_A = 'a';     //automatic conversion to int
        private static final int LOWER_F = 'f';     //automatic conversion to int
        private static final int LOWER_Z = 'z';     //automatic conversion to int

        private State state = INIT;

        private final CharSequence text;

        int[] result;
        int start;
        int end;

        SearchMachine(CharSequence text) {
            this.text = text;

            reset();
        }

        private void reset() {
            state = INIT;

            start = -1;
            end = -1;

            result = null;
        }

        int[] findBoundaries() {

            final int length = text.length();

            if (length == 0) {
                return new int[0];
            }

            int schemeStart = -1;
            int authorityStart = -1;
            int hostStart = -1;
            int portStart = -1;
            int pathStart = -1;
            int queryStart = -1;
            int fragmentStart = -1;

            int remainingHexaChars = 0;

            int ipv4Parts = 0;
            int ipv4Digits = 0;
            int ipv4PartAddr = 0;

            int ipv6Parts = 0;
            int ipv6Digits = 0;
            boolean ipv6QDotPresent = false;

            boolean wasValidUri = false;
            boolean reuseCharInNextRound = false;
            int bracketsDepth = 0;

            for (int i = 0; i < length; i++) {

                boolean isValidUri = false;

                if (reuseCharInNextRound) {
                    i--;
                    reuseCharInNextRound = false;
                }

                final int c = text.charAt(i);

                State newState = null;
                if (remainingHexaChars != 0) {
                    if (isHexDigit(c)) {
                        remainingHexaChars--;
                        newState = state;
                    }
                } else {
                    switch (state) {
                        case WAITING_FOR_URI_END_CHAR:
                            if (isUriEndChar(c)) {
                                if (isInitChar(c)) {
                                    newState = INIT;
                                    reuseCharInNextRound = true;
                                } else {
                                    newState = WAITING_FOR_INIT_CHAR;
                                }
                            } else {
                                newState = WAITING_FOR_URI_END_CHAR;
                            }
                            break;
                        case WAITING_FOR_INIT_CHAR:
                            if (isInitChar(c)) {
                                newState = INIT;
                                reuseCharInNextRound = true;
                            } else {
                                newState = WAITING_FOR_INIT_CHAR;
                            }
                            break;
                        case INIT:
                            if (isLetter(c)) {
                                newState = SCHEME;
                                schemeStart = i;
                                rememberIsStart(i);
                            } else if (isInitChar(c)) {
                                newState = INIT;
                            } else {
                                newState = WAITING_FOR_INIT_CHAR;
                            }
                            break;
                        case SCHEME:
                            if (c == ':') {
                                newState = HIER_START;
                                isValidUri = true;    //no authority, empty path
                            } else if (isSchemeChar(c)) {
                                newState = SCHEME;
                            }
                            break;
                        case HIER_START:
                            if (c == '/') {
                                pathStart = i;
                                newState = HIER_SLASH;
                                isValidUri = true;     //no authority, root path
                            } else if (isPathSegmentChar(c)) {
                                if (c == '%') {
                                    remainingHexaChars = 2;
                                } else if (c == '(') {
                                    bracketsDepth++;
                                }
                                pathStart = i;
                                newState = PATH;
                                isValidUri = true; //no authority, rootless path
                            }
                            break;
                        case HIER_SLASH:
                            if (c == '/') {
                                pathStart = -1;
                                authorityStart = i + 1;
                                newState = USERINFO_OR_HOST_START;  //authority
                                isValidUri = true;     //empty host, empty path
                            } else if (isPathSegmentChar(c)) {
                                if (c == '%') {
                                    remainingHexaChars = 2;
                                } else if (c == '(') {
                                    bracketsDepth++;
                                }
                                newState = PATH;
                                isValidUri = true;      //empty host, root path
                            }
                            break;
                        case USERINFO_OR_HOST_START:      //authority
                            if (c == ':') {
                                portStart = i + 1;
                                newState = USERINFO_OR_PORT_NUMBER;
                                isValidUri = true;      //empty host, empty port
                            } else if (isDigit(c)) {
                                ipv4Parts = 1;
                                ipv4Digits = 1;
                                ipv4PartAddr = c - '0';
                                hostStart = i;
                                newState = USERINFO_OR_IPV4_OR_REGNAME;
                            } else if (isRegnameChar(c)) {
                                if (c == '%') {
                                    remainingHexaChars = 2;
                                } else if (c == '(') {
                                    bracketsDepth++;
                                }
                                hostStart = i;
                                newState = USERINFO_OR_REGNAME;
                                isValidUri = true;      //reg. name, empty path
                            } else if (c == '@') {
                                hostStart = i + 1;
                                newState = HOST_START;
                                isValidUri = true; //empty reg. name, empty path
                            } else if (c == '[') {
                                hostStart = i;
                                newState = IP_LITERAL_START;
                            } else if (c == '/') {      //empty registered name
                                pathStart = i;
                                newState = PATH;
                                isValidUri = true;  //empty reg. name, root path
                            } else if (c == '?') {      //empty registered name
                                queryStart = i + 1;
                                newState = QUERY;
                                isValidUri = true;   //empty reg. name, no path
                            } else if (c == '#') {      //empty registered name
                                fragmentStart = i + 1;
                                newState = FRAGMENT;
                                isValidUri = true;   //empty reg. name, no path
                            }
                            break;
                        case USERINFO_OR_IPV4_OR_REGNAME:
                            if (c == ':') {
                                portStart = i + 1;
                                newState = USERINFO_OR_PORT_NUMBER;
                                isValidUri = true;      //registered name
                            } else if (isDigit(c)) {
                                ipv4Digits++;
                                if (ipv4Digits > 3) {
                                    newState = USERINFO_OR_REGNAME;
                                } else if (ipv4Digits < 3) {
                                    newState = USERINFO_OR_IPV4_OR_REGNAME;
                                } else {
                                    ipv4PartAddr = 10 * ipv4PartAddr
                                                   + (c - '0');
                                    if (ipv4PartAddr <= 255) {
                                        newState = USERINFO_OR_IPV4_OR_REGNAME;
                                    } else {
                                        newState = USERINFO_OR_REGNAME;
                                    }
                                }
                                isValidUri = true; //registered name or IP-addr.
                            } else if (c == '.') {
                                ++ipv4Parts;
                                if (ipv4Parts > 4) {
                                    newState = USERINFO_OR_REGNAME;
                                } else if (ipv4Digits == 0) {    //two dots
                                    newState = USERINFO_OR_REGNAME;
                                } else {
                                    ipv4Digits = 0;
                                    ipv4PartAddr = 0;
                                    newState = USERINFO_OR_IPV4_OR_REGNAME;
                                }
                                isValidUri = true;      //registered name
                            } else if (isRegnameChar(c)) {
                                if (c == '%') {
                                    remainingHexaChars = 2;
                                } else if (c == '(') {
                                    assert bracketsDepth == 0;
                                    bracketsDepth++;
                                }
                                newState = USERINFO_OR_REGNAME;
                                isValidUri = true;      //registered name
                            } else if (c == '@') {
                                hostStart = i + 1;
                                newState = HOST_START;
                                isValidUri = true;   //empty registered name
                            } else if (c == '/') {
                                pathStart = i;
                                newState = PATH;
                                isValidUri = true;  //registered name, root path
                            } else if (c == '?') {
                                queryStart = i + 1;
                                newState = QUERY;
                                isValidUri = true;  //registered name, no path
                            } else if (c == '#') {
                                fragmentStart = i + 1;
                                newState = FRAGMENT;
                                isValidUri = true;  //registered name, no path
                            }
                            break;
                        case USERINFO_OR_REGNAME:
                            if (c == ':') {
                                bracketsDepth = 0;
                                portStart = i + 1;
                                newState = USERINFO_OR_PORT_NUMBER;
                                isValidUri = true;      //empty port number
                            } else if (isRegnameChar(c)) {
                                if (c == '%') {
                                    remainingHexaChars = 2;
                                } else if (c == '(') {
                                    bracketsDepth++;
                                } else if (c == ')') {
                                    bracketsDepth--;
                                }
                                newState = USERINFO_OR_REGNAME;
                                isValidUri = true;  //registered name
                            } else if (c == '@') {
                                bracketsDepth = 0;
                                hostStart = i + 1;
                                newState = HOST_START;
                                isValidUri = true; //empty reg. name, empty path
                            } else if (c == '/') {
                                bracketsDepth = 0;
                                pathStart = i;
                                newState = PATH;
                                isValidUri = true;  //registered name, root path
                            } else if (c == '?') {
                                bracketsDepth = 0;
                                queryStart = i + 1;
                                newState = QUERY;
                                isValidUri = true;   //registered name, no path
                            } else if (c == '#') {
                                bracketsDepth = 0;
                                fragmentStart = i + 1;
                                newState = FRAGMENT;
                                isValidUri = true;   //registered name, no path
                            }
                            break;
                        case USERINFO_OR_PORT_NUMBER:
                            if (isDigit(c)) {
                                newState = USERINFO_OR_PORT_NUMBER;
                                isValidUri = true;              //port number
                            } else if (isUserInfoChar(c)) {
                                hostStart = -1;
                                portStart = -1;
                                newState = USERINFO;
                            } else if (c == '@') {
                                hostStart = i + 1;
                                portStart = -1;
                                newState = HOST_START;
                                isValidUri = true;  //empty reg. name, no path
                            } else if (c == '/') {
                                newState = PATH;
                                isValidUri = true;   //port number, root path
                            } else if (c == '?') {
                                newState = QUERY;
                                isValidUri = true;   //port number, no path
                            } else if (c == '#') {
                                newState = FRAGMENT;
                                isValidUri = true;   //port number, no path
                            }
                            break;
                        case USERINFO:
                            if (isUserInfoChar(c)) {
                                newState = USERINFO;
                            } else if (c == '@') {
                                hostStart = i + 1;
                                newState = HOST_START;
                                isValidUri = true;  //empty reg. name, no path
                            }
                            break;
                        case HOST_START:
                            if (isDigit(c)) {
                                ipv4Parts = 1;
                                ipv4Digits = 1;
                                ipv4PartAddr = c - '0';
                                hostStart = i;
                                newState = IPV4_OR_REGNAME;
                                isValidUri = true;   //registered name, no path
                            } else if (isRegnameChar(c)) {
                                if (c == '%') {
                                    remainingHexaChars = 2;
                                } else if (c == '(') {
                                    assert bracketsDepth == 0;
                                    bracketsDepth++;
                                }
                                hostStart = i;
                                newState = REGNAME;
                                isValidUri = true;   //registered name, no path
                            } else if (c == '[') {
                                hostStart = i;
                                newState = IP_LITERAL_START;
                            } else if (c == '/') {      //empty registered name
                                pathStart = i;
                                newState = PATH;
                                isValidUri = true;  //empty reg. name, root path
                            } else if (c == '?') {      //empty registered name
                                queryStart = i + 1;
                                newState = QUERY;
                                isValidUri = true;  //empty reg. name, no path
                            } else if (c == '#') {      //empty registered name
                                fragmentStart = i + 1;
                                newState = FRAGMENT;
                                isValidUri = true;  //empty reg. name, no path
                            }
                            break;
                        case IPV4_OR_REGNAME:
                            if (isDigit(c)) {
                                ipv4Digits++;
                                if (ipv4Digits > 3) {
                                    newState = REGNAME;
                                } else if (ipv4Digits < 3) {
                                    newState = IPV4_OR_REGNAME;
                                } else {
                                    ipv4PartAddr = 10 * ipv4PartAddr
                                                   + (c - '0');
                                    if (ipv4PartAddr <= 255) {
                                        newState = IPV4_OR_REGNAME;
                                    } else {
                                        newState = REGNAME;
                                    }
                                }
                                isValidUri = true; //registered name or IP-addr.
                            } else if (c == '.') {
                                ++ipv4Parts;
                                if (ipv4Parts > 4) {
                                    newState = REGNAME;
                                } else if (ipv4Digits == 0) {    //two dots
                                    newState = REGNAME;
                                } else {
                                    ipv4Digits = 0;
                                    ipv4PartAddr = 0;
                                    newState = IPV4_OR_REGNAME;
                                }
                                isValidUri = true;  //registered name, no path
                            } else if (isRegnameChar(c)) {
                                if (c == '%') {
                                    remainingHexaChars = 2;
                                } else if (c == '(') {
                                    assert bracketsDepth == 0;
                                    bracketsDepth++;
                                }
                                newState = REGNAME;
                                isValidUri = true;  //registered name, no path
                            } else if (c == '/') {
                                pathStart = i;
                                newState = PATH;
                                isValidUri = true;  //registered name, root path
                            } else if (c == '?') {
                                queryStart = i + 1;
                                newState = QUERY;
                                isValidUri = true;  //registered name, no path
                            } else if (c == '#') {
                                fragmentStart = i + 1;
                                newState = FRAGMENT;
                                isValidUri = true;  //registered name, no path
                            }
                            break;
                        case REGNAME:
                            if (isRegnameChar(c)) {
                                if (c == '%') {
                                    remainingHexaChars = 2;
                                } else if (c == '(') {
                                    bracketsDepth++;
                                } else if (c == ')') {
                                    bracketsDepth--;
                                }
                                newState = REGNAME;
                                isValidUri = true;  //registered name, no path
                            } else if (c == '/') {
                                bracketsDepth = 0;
                                pathStart = i;
                                newState = PATH;
                                isValidUri = true;  //registered name, root path
                            } else if (c == '?') {
                                bracketsDepth = 0;
                                queryStart = i + 1;
                                newState = QUERY;
                                isValidUri = true;  //registered name, no path
                            } else if (c == '#') {
                                bracketsDepth = 0;
                                fragmentStart = i + 1;
                                newState = FRAGMENT;
                                isValidUri = true;  //registered name, no path
                            }
                            break;
                        case IP_LITERAL_START:
                            if ((c == 'v') || (c == 'V')) {
                                newState = IP_FUTURE_START;
                            } else if (c == ':') {
                                newState = IPV6_QDOT_1;
                                ipv6Parts = 0;
                                ipv6Digits = 0;
                            } else if (isHexDigit(c)) {
                                ipv6Parts = 1;
                                ipv6Digits = 1;
                                newState = IPV6;
                            }
                            break;
                        case IPV6:
                            if (c == ':') {
                                int maxParts = ipv6QDotPresent ? 7 : 8;
                                if (ipv6Parts < maxParts) {
                                    newState = IPV6_DDOT;
                                }
                            } else if (isHexDigit(c)) {
                                ipv6Digits++;
                                if (ipv6Digits <= 4) {
                                    newState = IPV6;
                                }
                            } else if (c == ']') {
                                boolean ipv6PartsOk = ipv6QDotPresent
                                                      ? (ipv6Parts < 8)
                                                      : (ipv6Parts == 8);
                                if (ipv6PartsOk) {
                                    newState = PORT_NUMBER_OR_AUTHORITY_END;
                                    isValidUri = true;  //IPv6 address, no path
                                }
                            }
                            break;
                        case IPV6_DDOT:
                            if (c == ':') {
                                if (!ipv6QDotPresent) {
                                    ipv6QDotPresent = true;
                                    newState = IPV6_QDOT_2;
                                }
                            } else if (isHexDigit(c)) {
                                ipv6Parts++;
                                ipv6Digits = 1;
                                newState = IPV6;
                            }
                            break;
                        case IPV6_QDOT_1:
                            if (c == ':') {
                                ipv6QDotPresent = true;
                                newState = IPV6_QDOT_2;
                            }
                            break;
                        case IPV6_QDOT_2:
                            if (isHexDigit(c)) {
                                ipv6Parts++;
                                ipv6Digits = 1;
                                newState = IPV6;
                            } else if (c == ']') {
                                boolean ipv6PartsOk = ipv6QDotPresent
                                                      ? (ipv6Parts < 8)
                                                      : (ipv6Parts == 8);
                                if (ipv6PartsOk) {
                                    newState = PORT_NUMBER_OR_AUTHORITY_END;
                                }
                            }
                            break;
                        case IP_FUTURE_START:
                            if (isHexDigit(c)) {
                                newState = IP_FUTURE_VER_NUM;
                            }
                            break;
                        case IP_FUTURE_VER_NUM:
                            if (c == '.') {
                                newState = IP_FUTURE_ADDR_START;
                            } else if (isHexDigit(c)) {
                                newState = IP_FUTURE_VER_NUM;
                            }
                            break;
                        case IP_FUTURE_ADDR_START:
                            if (isIpFutureAddrChar(c)) {
                                newState = IP_FUTURE_ADDR;
                            }
                            break;
                        case IP_FUTURE_ADDR:
                            if (isIpFutureAddrChar(c)) {
                                newState = IP_FUTURE_ADDR;
                            } else if (c == ']') {
                                newState = PORT_NUMBER_OR_AUTHORITY_END;
                                isValidUri = true; //IPv-future address, no path
                            }
                            break;
                        case PORT_NUMBER_OR_AUTHORITY_END:
                            if (c == ':') {
                                newState = PORT_NUMBER;
                                isValidUri = true;      //empty port number
                            } else if (c == '/') {
                                pathStart = i;
                                newState = PATH;
                                isValidUri = true;   //no port number, root path
                            } else if (c == '?') {
                                queryStart = i + 1;
                                newState = QUERY;
                                isValidUri = true;   //no port number, no path
                            } else if (c == '#') {
                                fragmentStart = i + 1;
                                newState = FRAGMENT;
                                isValidUri = true;   //no port number, no path
                            }
                            break;
                        case PORT_NUMBER:
                            if (isDigit(c)) {
                                newState = PORT_NUMBER;
                                isValidUri = true;    //port number
                            } else if (c == '/') {
                                pathStart = i;
                                newState = PATH;
                                isValidUri = true;    //port number, root path
                            } else if (c == '?') {
                                queryStart = i + 1;
                                newState = QUERY;
                                isValidUri = true;    //port number, no path
                            } else if (c == '#') {
                                fragmentStart = i + 1;
                                newState = FRAGMENT;
                                isValidUri = true;    //port number, no path
                            }
                            break;
                        case PATH:
                            if (c == '?') {
                                bracketsDepth = 0;
                                queryStart = i + 1;
                                newState = QUERY;
                                isValidUri = true;       //empty query
                            } else if (c == '#') {
                                bracketsDepth = 0;
                                fragmentStart = i + 1;
                                newState = FRAGMENT;
                                isValidUri = true;       //empty fragment
                            } else if ((c == '/') || isPathSegmentChar(c)) {
                                if (c == '%') {
                                    remainingHexaChars = 2;
                                } else if (c == '(') {
                                    bracketsDepth++;
                                } else if (c == ')') {
                                    bracketsDepth--;
                                }
                                newState = PATH;
                                isValidUri = true;      //non-empty path
                            }
                            break;
                        case QUERY:
                            if (c == '#') {
                                bracketsDepth = 0;
                                fragmentStart = i + 1;
                                newState = FRAGMENT;
                                isValidUri = true;
                            } else if (isQueryChar(c)) {
                                if (c == '%') {
                                    remainingHexaChars = 2;
                                } else if (c == '(') {
                                    bracketsDepth++;
                                } else if (c == ')') {
                                    bracketsDepth--;
                                }
                                newState = QUERY;
                                isValidUri = true;
                            }
                            break;
                        case FRAGMENT:
                            if (isFragmentChar(c)) {
                                if (c == '%') {
                                    remainingHexaChars = 2;
                                } else if (c == '(') {
                                    bracketsDepth++;
                                } else if (c == ')') {
                                    bracketsDepth--;
                                }
                                newState = FRAGMENT;
                                isValidUri = true;
                            }
                            break;
                        default:
                            assert false;
                            break;
                    }

                    isValidUri &= (remainingHexaChars == 0);

                    if (newState == null) {
                        assert !reuseCharInNextRound;

                        if (isUriEndChar(c)) {
                            if (wasValidUri) {
                                assert (start != -1);
                                checkAndStoreResult(start, i, bracketsDepth);
                            }
                            newState = isInitChar(c) ? INIT
                                                     : WAITING_FOR_INIT_CHAR;
                        } else {
                            newState = WAITING_FOR_URI_END_CHAR;
                        }

                        start = -1;
                    }

                    if ((state != INIT) && (newState == INIT)) {
                        schemeStart = -1;
                        authorityStart = -1;
                        hostStart = -1;
                        portStart = -1;
                        pathStart = -1;
                        queryStart = -1;
                        fragmentStart = -1;

                        remainingHexaChars = 0;

                        bracketsDepth = 0;
                    }

                    wasValidUri = isValidUri;

                    state = newState;
                }
            } //for-cycle

            if (wasValidUri) {
                assert (start != -1);
                if (authorityStart != -1 || text.subSequence(start, length).toString().startsWith("mailto")) {
                    checkAndStoreResult(start, length, bracketsDepth);
                }
            }

            return result;
        }

        private void rememberIsStart(int pos) {
            start = pos;
        }

        private void checkAndStoreResult(int start, int end, int bracketsDepth) {
            char lastChar = text.charAt(end - 1);
            if ((lastChar == '.') || (lastChar == ',')
                    || (lastChar == ':') || (lastChar == ';')) {
                lastChar = text.charAt(--end - 1);
            }
            if ((bracketsDepth < 0) && (lastChar == ')')) {
                lastChar = text.charAt(--end - 1);
            }

            if (checkUrl(start, end)) {
                storeResult(start, end);
            }
        }

        private boolean checkUrl(int start, int end) {
            String hyperlink = text.subSequence(start, end).toString();
            try {
                URI uri = new URI(hyperlink);
                if (!uri.isAbsolute()) {
                    return false;
                }
                String scheme = uri.getScheme();
                if (uri.isOpaque() && !scheme.equals("mailto")) {
                    return false;
                }
                if (!scheme.equals("http") && !scheme.equals("https") && !scheme.equals("mailto")) {//NOI18N
                    return false;
                }
                uri.toURL();             //just a check
            } catch (Exception ex) {
                return false;
            }
            return true;
        }

        private void storeResult(int start, int end) {
            assert (start != -1);
            if (result == null) {
                result = new int[] {start, end};
            } else {
                int[] newResult = new int[result.length + 2];
                System.arraycopy(result, 0, newResult, 0, result.length);
                newResult[result.length    ] = start;
                newResult[result.length + 1] = end;
                result = newResult;
            }
        }

        private static boolean isSupportedScheme(String scheme) {
            for (String suppScheme : SUPPORTED_SCHEMES) {
                if (scheme.equals(suppScheme)) {
                    return true;
                }
            }
            return false;
        }

        private static State getInitialState(int c) {
            return isInitChar(c) ? INIT : WAITING_FOR_INIT_CHAR;
        }

        private static boolean isInitChar(int c) {
            return (c < 0x20) || (URI_INIT_CHARS.indexOf(c) != -1)
                   || (c > 0xff) && Character.isSpaceChar(c);
        }

        private static boolean isUriEndChar(int c) {
            return (c < 0x20) || (c == ' ') || (c == '>') || (c == '"')
                   || (c > 0xff) && Character.isSpaceChar(c);
        }

        private static boolean isSchemeChar(int c) {
            if (c >= 0xff) {
                return false;
            }
            return isLetter(c) || isDigit(c)
                   || (c == '+') || (c == '-') || (c == '.');
        }

        private static boolean isIpFutureAddrChar(int c) {
            return isUnreserved(c) || isSubDelim(c) || (c == ':');
        }

        private static boolean isUserInfoChar(int c) {
            return isUnreserved(c) || isPctEncoded(c) || isSubDelim(c)
                   || (c == ':');
        }

        private static boolean isRegnameChar(int c) {
            return isUnreserved(c) || isPctEncoded(c) || isSubDelim(c);
        }

        private static boolean isPathSegmentChar(int c) {
            return isUnreserved(c) || isPctEncoded(c) || isSubDelim(c)
                   || (c == ':') || (c == '@');
        }

        private static boolean isQueryChar(int c) {
            return isPathSegmentChar(c) || (c == '/') || (c == '?');
        }

        private static boolean isFragmentChar(int c) {
            return isPathSegmentChar(c) || (c == '/') || (c == '?');
        }

        private static boolean isPctEncoded(int c) {
            return c == '%';
        }

        private static boolean isGenDelim(int c) {
            return GEN_DELIMS.indexOf(c) != -1;
        }

        private static boolean isSubDelim(int c) {
            return SUB_DELIMS.indexOf(c) != -1;
        }

        private static boolean isReserved(int c) {
            return RESERVED.indexOf(c) != -1;
        }

        private static boolean isUnreserved(int c) {
            return isLetter(c) || isDigit(c)
                   || (c == '-') || (c == '.') || (c == '_') || (c == '~');
        }

        private static boolean isLetterOrDigit(int c) {
            return isLetter(c) || isDigit(c);
        }

        private static boolean isLetter(int c) {
            c |= 0x20;
            return ((c >= LOWER_A) && (c <= LOWER_Z));
        }

        private static boolean isHexDigit(int c) {
            if (isDigit(c)) {
                return true;
            }
            c |= 0x20;
            return ((c >= LOWER_A) && (c <= LOWER_F));
        }

        private static boolean isDigit(int c) {
            return ((c >= '0') && (c <= '9'));
        }

        private static boolean isSpaceOrPunct(int c) {
            return (c == '\r') || (c == '\n')
                   || Character.isSpaceChar(c) || isPunct(c);
        }

        private static boolean isPunct(int c) {
            return PUNCT_CHARS.indexOf(c) != -1;
        }

    }

}
