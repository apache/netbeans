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
package org.netbeans.modules.javascript2.jsdoc;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.jsdoc.model.DescriptionElement;
import org.netbeans.modules.javascript2.jsdoc.model.JsDocElement;
import org.netbeans.modules.javascript2.jsdoc.model.JsDocElementType;
import org.netbeans.modules.javascript2.jsdoc.model.JsDocElementUtils;
import org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Parses jsDoc comment blocks.
 * It can return map of these blocks, their end offset in the snapshot.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocParser {

    private static final Logger LOGGER = Logger.getLogger(JsDocParser.class.getName());

    /**
     * Parses given snapshot and returns map of all jsDoc blocks.
     * @param snapshot snapshot to parse
     * @return map of blocks, key is end offset of each block
     */
    public static Map<Integer, JsDocComment> parse(Snapshot snapshot) {
        Map<Integer, JsDocComment> blocks = new HashMap<>();

        if (snapshot == null || snapshot.getTokenHierarchy() == null) {
            return blocks;
        }

        TokenSequence tokenSequence = snapshot.getTokenHierarchy().tokenSequence(JsTokenId.javascriptLanguage());
        if (tokenSequence == null) {
            return blocks;
        }

        while (tokenSequence.moveNext()) {
            if (tokenSequence.token().id() == JsTokenId.DOC_COMMENT) {
                JsDocCommentType commentType = getCommentType(tokenSequence.token().text());
                LOGGER.log(Level.FINEST, "JsDocParser:comment block offset=[{0}-{1}],type={2},text={3}", new Object[]{
                    tokenSequence.offset(), tokenSequence.offset() + tokenSequence.token().length(), commentType, tokenSequence.token().text()});

                OffsetRange offsetRange = new OffsetRange(tokenSequence.offset(), tokenSequence.offset() + tokenSequence.token().length());
                if (commentType == JsDocCommentType.DOC_NO_CODE_START
                        || commentType == JsDocCommentType.DOC_NO_CODE_END
                        || commentType == JsDocCommentType.DOC_SHARED_TAG_END) {
                    blocks.put(offsetRange.getEnd(), new JsDocComment(offsetRange, commentType, Collections.<JsDocElement>emptyList()));
                } else {
                    blocks.put(offsetRange.getEnd(), parseCommentBlock(tokenSequence, offsetRange, commentType));
                }
            }
        }

        return blocks;
    }

    private static boolean isTextToken(Token<? extends JsDocumentationTokenId> token) {
        return (token.id() != JsDocumentationTokenId.ASTERISK && token.id() != JsDocumentationTokenId.COMMENT_DOC_START);
    }

    private static TokenSequence getEmbeddedJsDocTS(TokenSequence ts) {
        return ts.embedded(JsDocumentationTokenId.language());
    }

    private static JsDocComment parseCommentBlock(TokenSequence ts, OffsetRange range, JsDocCommentType commentType) {
        TokenSequence ets = getEmbeddedJsDocTS(ts);

        List<JsDocElement> jsDocElements = new ArrayList<>();
        Token<? extends JsDocumentationTokenId> token;
        JsDocElementType type = null;
        boolean afterDescription = false;
        StringBuilder sb = new StringBuilder();
        int offset = ts.offset();
        while (ets.moveNext()) {
            token = ets.token();
            if (!isTextToken(token)) {
                boolean isAsterixType = false;
                if (token.id() == JsDocumentationTokenId.ASTERISK) {
                    // we need to check type {*}
                    String currentText = sb.toString();
                    int curlyOpen = currentText.lastIndexOf('{');
                    if (curlyOpen > -1) {
                        String afterText = currentText.substring(curlyOpen + 1);
                        if (afterText.trim().isEmpty() && afterText.indexOf('\n') == -1) {
                            isAsterixType = true;
                        }
                    }
                }
                if (!isAsterixType) {
                    continue;
                }
            }

            CharSequence text = token.text();
            JsDocElementType elementType = getJsDocKeywordType(new StringBuilder(text.length()).append(text).toString());
            if ((token.id() == JsDocumentationTokenId.KEYWORD && elementType != JsDocElementType.UNKNOWN
                    && elementType != JsDocElementType.LINK) || token.id() == JsDocumentationTokenId.COMMENT_END) {
                if (sb.toString().trim().isEmpty()) {
                    // simple tag
                    if (type != null) {
                        jsDocElements.add(JsDocElementUtils.createElementForType(type, "", -1));
                    }
                } else {
                    // store first description
                    if (!afterDescription) {
                        //TODO - distinguish description and inline comments
                        jsDocElements.add(DescriptionElement.create(JsDocElementType.CONTEXT_SENSITIVE, sb.toString().trim()));
                    } else {
                        jsDocElements.add(JsDocElementUtils.createElementForType(type, sb.toString().trim(), offset));
                    }
                    sb = new StringBuilder();
                }

                while (ets.moveNext() && ets.token().id() == JsDocumentationTokenId.WHITESPACE) {
                }

                offset = ets.offset();
                if (token.id() != JsDocumentationTokenId.COMMENT_END) {
                    ets.movePrevious();
                }
                afterDescription = true;
                text = token.text();
                type = JsDocElementType.fromString(new StringBuilder(text.length()).append(text).toString());
            } else {
                sb.append(token.text());
            }
        }

        return new JsDocComment(range, commentType, jsDocElements);
    }

    private static JsDocCommentType getCommentType(CharSequence text) {
        //TODO - move that into some constatns holder
        if (startsWith(text, "/**#")) { //NOI18N
            if (textEquals(text, "/**#nocode+*/")) { //NOI18N
                return JsDocCommentType.DOC_NO_CODE_START;
            } else if (textEquals(text, "/**#nocode-*/")) {
                return JsDocCommentType.DOC_NO_CODE_END;
            } else if (startsWith(text, "/**#@+")) { //NOI18N
                return JsDocCommentType.DOC_SHARED_TAG_START;
            } else if (textEquals(text, "/**#@-*/")) { //NOI18N
                return JsDocCommentType.DOC_SHARED_TAG_END;
            }
        }
        return JsDocCommentType.DOC_COMMON;
    }

    private static JsDocElementType getJsDocKeywordType(String string) {
        return JsDocElementType.fromString(string);
    }

    private static boolean textEquals(CharSequence text1, CharSequence text2) {
        if (text1 == text2) {
            return true;
        }
        int len = text1.length();
        if (len == text2.length()) {
            for (int i = len - 1; i >= 0; i--) {
                if (text1.charAt(i) != text2.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean startsWith(CharSequence text, CharSequence prefix) {
        int p_length = prefix.length();
        if (p_length > text.length()) {
            return false;
        }
        for (int x = 0; x < p_length; x++) {
            if (text.charAt(x) != prefix.charAt(x))
                return false;
        }
        return true;
    }
}
