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
package org.netbeans.modules.javascript2.nodejs.editor;

import java.util.Arrays;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;

/**
 *
 * @author Petr Pisl
 */
public enum NodeJsContext {

    MODULE_PATH,
    ASSIGN_LISTENER,
    AFTER_ASSIGNMENT,
    GLOBAL,
    UNKNOWN;

    public static NodeJsContext findContext(TokenSequence<? extends JsTokenId> ts, final int offset) {
        ts.move(offset);
        if (ts.moveNext() || ts.movePrevious()) {
            Token<? extends JsTokenId> token = ts.token();
            JsTokenId tokenId = token.id();
            if (tokenId == JsTokenId.STRING || tokenId == JsTokenId.STRING_END) {
                token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT,
                        JsTokenId.STRING_BEGIN, JsTokenId.STRING, JsTokenId.STRING_END, JsTokenId.OPERATOR_COMMA));
                tokenId = token.id();
                if (tokenId == JsTokenId.BRACKET_LEFT_PAREN && ts.movePrevious()) {
                    token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT));
                    tokenId = token.id();
                    if (tokenId == JsTokenId.IDENTIFIER) {
                        if (NodeJsUtils.REQUIRE_METHOD_NAME.equals(token.text().toString())) {
                            return MODULE_PATH;
                        }
                        if (NodeJsUtils.ON_METHOD_NAME.equals(token.text().toString())) {
                            return ASSIGN_LISTENER;
                        }
                    }
                }
            } else {
                if ((tokenId == JsTokenId.EOL || tokenId == JsTokenId.OPERATOR_SEMICOLON || tokenId == JsTokenId.OPERATOR_ASSIGNMENT
                        || tokenId == JsTokenId.WHITESPACE || tokenId == JsTokenId.IDENTIFIER
                        || tokenId == JsTokenId.BRACKET_LEFT_CURLY) && ts.movePrevious()) {
                    token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT, JsTokenId.EOL));
                    tokenId = token.id();
                }
                if (tokenId.isKeyword() && ts.offset() == offset) {
                    return GLOBAL;
                }
                if (!ts.movePrevious()) {
                    return GLOBAL;
                }
                if (tokenId == JsTokenId.IDENTIFIER && NodeJsUtils.REQUIRE_METHOD_NAME.startsWith(token.text().toString())) {
                    token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT, JsTokenId.EOL));
                    tokenId = token.id();
                    if (!ts.movePrevious()) {
                        return GLOBAL;
                    }
                }
                if (tokenId == JsTokenId.OPERATOR_ASSIGNMENT) {
                    // offer require()
                    return AFTER_ASSIGNMENT;
                }
                if (tokenId == JsTokenId.OPERATOR_SEMICOLON || tokenId == JsTokenId.BRACKET_LEFT_CURLY || tokenId == JsTokenId.BRACKET_RIGHT_CURLY) {
                    return GLOBAL;
                }

            }
        }
        return UNKNOWN;
    }

    public static String getEventEmiterName(TokenSequence<? extends JsTokenId> ts, final int offset) {
        if (findContext(ts, offset) == ASSIGN_LISTENER) {
            if (ts.movePrevious() && ts.token().id() == JsTokenId.OPERATOR_DOT) {
                if (ts.movePrevious() && ts.token().id() == JsTokenId.IDENTIFIER) {
                    return ts.token().text().toString();
                }
            }
        }
        return null;
    }
}
