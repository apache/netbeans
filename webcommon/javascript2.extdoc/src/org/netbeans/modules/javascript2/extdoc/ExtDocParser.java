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
package org.netbeans.modules.javascript2.extdoc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocDescriptionElement;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocElement;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocElementType;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocElementUtils;
import org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Parses ExtDoc comment blocks.
 * It can return map of these blocks, their start offset in the snapshot.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ExtDocParser {

    private static final Logger LOGGER = Logger.getLogger(ExtDocParser.class.getName());

    /**
     * Parses given snapshot and returns map of all extDoc blocks.
     * @param snapshot snapshot to parse
     * @return map of blocks, key is end offset of each block
     */
    public static Map<Integer, ExtDocComment> parse(Snapshot snapshot) {
        Map<Integer, ExtDocComment> blocks = new HashMap<Integer, ExtDocComment>();

        if (snapshot == null || snapshot.getTokenHierarchy() == null) {
            return blocks;
        }
        TokenSequence tokenSequence = snapshot.getTokenHierarchy().tokenSequence(JsTokenId.javascriptLanguage());
        if (tokenSequence == null) {
            return blocks;
        }

        while (tokenSequence.moveNext()) {
            if (tokenSequence.token().id() == JsTokenId.DOC_COMMENT) {
                LOGGER.log(Level.FINEST, "ExtDocParser:comment block offset=[{0}-{1}],text={2}", new Object[]{
                    tokenSequence.offset(), tokenSequence.offset() + tokenSequence.token().length(), tokenSequence.token().text()});
                OffsetRange offsetRange = new OffsetRange(tokenSequence.offset(), tokenSequence.offset() + tokenSequence.token().length());
                blocks.put(offsetRange.getEnd(), parseCommentBlock(tokenSequence, offsetRange));
            }
        }

        return blocks;
    }

    private static boolean isCommentImportantToken(Token<? extends JsDocumentationTokenId> token) {
        return (token.id() != JsDocumentationTokenId.ASTERISK && token.id() != JsDocumentationTokenId.COMMENT_DOC_START);
    }

    private static TokenSequence getEmbeddedExtDocTS(TokenSequence ts) {
        return ts.embedded(JsDocumentationTokenId.language());
    }

    private static ExtDocComment parseCommentBlock(TokenSequence ts, OffsetRange range) {
        TokenSequence ets = getEmbeddedExtDocTS(ts);

        List<ExtDocElement> sDocElements = new ArrayList<ExtDocElement>();
        StringBuilder sb = new StringBuilder();

        Token<? extends JsDocumentationTokenId> currentToken;
        boolean afterDescriptionEntry = false;

        ExtDocElementType lastType = null;
        int lastOffset = ts.offset();

        while (ets.moveNext()) {
            currentToken = ets.token();
            if (!isCommentImportantToken(currentToken)) {
                continue;
            }

            if (currentToken.id() == JsDocumentationTokenId.KEYWORD || currentToken.id() == JsDocumentationTokenId.COMMENT_END) {
                if (sb.toString().trim().isEmpty()) {
                    // simple tag
                    if (lastType != null) {
                        sDocElements.add(ExtDocElementUtils.createElementForType(lastType, "", -1));
                    }
                } else {
                    // store first description in the comment if any
                    if (!afterDescriptionEntry) {
                        sDocElements.add(ExtDocDescriptionElement.create(ExtDocElementType.DESCRIPTION, sb.toString().trim()));
                    } else {
                        sDocElements.add(ExtDocElementUtils.createElementForType(lastType, sb.toString().trim(), lastOffset));
                    }
                    sb = new StringBuilder();
                }

                while (ets.moveNext() && ets.token().id() == JsDocumentationTokenId.WHITESPACE) {
                    continue;
                }

                lastOffset = ets.offset();
                if (currentToken.id() != JsDocumentationTokenId.COMMENT_END) {
                    ets.movePrevious();
                }
                afterDescriptionEntry = true;
                CharSequence text = currentToken.text();
                lastType = ExtDocElementType.fromString(new StringBuilder(text.length()).append(text).toString());
            } else {
                // store all text which appears before next keyword or comment end
                sb.append(currentToken.text());
            }
        }

        return new ExtDocComment(range, sDocElements);
    }

}
