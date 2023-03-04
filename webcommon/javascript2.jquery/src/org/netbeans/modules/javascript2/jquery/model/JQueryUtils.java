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
package org.netbeans.modules.javascript2.jquery.model;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;

/**
 *
 * @author Petr Pisl, Martin Fousek
 */
public class JQueryUtils {

    public static final String JQUERY$ = "$";      //NOI18N
    public static final String JQUERY = "jQuery";  //NOI18N
    
    public static boolean isJQuery(ParserResult parserResult, int offset) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(parserResult.getSnapshot().getTokenHierarchy(), offset);
        if (ts == null) {
            return false;
        }
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }
        
        if (ts.token().id() == JsTokenId.EOL || ts.token().id() == JsTokenId.WHITESPACE) {
            ts.movePrevious();
        }
        Token<? extends JsTokenId> lastToken = ts.token();
        Token<? extends JsTokenId> token = lastToken;
        JsTokenId tokenId = token.id();
        while (tokenId != JsTokenId.EOL
                && tokenId != JsTokenId.WHITESPACE
                && !(lastToken.id() == JsTokenId.IDENTIFIER && (JQueryUtils.JQUERY$.equals(lastToken.text().toString()) || JQUERY.equals(lastToken.text().toString())))
                && ts.movePrevious()) {
            lastToken = token;
            token = ts.token();
            tokenId = token.id();
        }
        return (lastToken.id() == JsTokenId.IDENTIFIER
                && (JQueryUtils.JQUERY$.equals(lastToken.text().toString()) || JQUERY.equals(lastToken.text().toString()))
                || (!ts.movePrevious()
                && (JQueryUtils.JQUERY$.equals(token.text().toString()) || JQUERY.equals(token.text().toString()))));
    }

    public static boolean isInJQuerySelector(ParserResult parserResult, int offset) {
        if (isJQuery(parserResult, offset)) {
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(parserResult.getSnapshot().getTokenHierarchy(), offset, JsTokenId.javascriptLanguage());
            if (ts == null) {
                return false;
            }
            ts.move(offset);
            if (!(ts.moveNext() && ts.movePrevious())) {
                return false;
            }
            return ts.token().id() == JsTokenId.STRING || ts.token().id() == JsTokenId.STRING_BEGIN;
//            boolean leftBracket = false;
//            while (!isEndToken(ts.token().id()) && ts.token().id() != JsTokenId.BRACKET_RIGHT_PAREN && ts.movePrevious()) {
//                if (ts.token().id() == JsTokenId.BRACKET_LEFT_PAREN) {
//                    leftBracket = true;
//                    break;
//                }
//            }
//            if (!leftBracket) {
//                return false;
//            } else {
//                ts.move(offset);
//                if (!(ts.moveNext() && ts.movePrevious())) {
//                    return false;
//                }
//            }
//            while (!isEndToken(ts.token().id()) && ts.token().id() != JsTokenId.BRACKET_LEFT_PAREN && ts.moveNext()) {
//                if (ts.token().id() == JsTokenId.BRACKET_RIGHT_PAREN) {
//                    return true;
//                }
//            }
        }
        return false;
    }

    private static boolean isEndToken(JsTokenId token) {
        return token == JsTokenId.EOL || token == JsTokenId.OPERATOR_SEMICOLON;
    }
//
//    public String getPrefix(ParserResult info, int caretOffset) {
//        BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
//        if (doc == null) {
//            return null;
//        }
//
//        caretOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
//        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(info.getSnapshot(), caretOffset);
//        if (ts == null) {
//            return null;
//        }
//
//        ts.move(caretOffset);
//
//        if (!ts.moveNext() && !ts.movePrevious()) {
//            return null;
//        }
//
//        Token token  = ts.token();
//        while (token.id() == JsTokenId.OPERATOR_COLON)
//    }
}
