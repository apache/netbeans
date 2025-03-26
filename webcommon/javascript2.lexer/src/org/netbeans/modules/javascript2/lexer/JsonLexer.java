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

package org.netbeans.modules.javascript2.lexer;

import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.javascript2.json.api.JsonOptionsQuery;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.openide.filesystems.FileObject;

import static org.netbeans.modules.javascript2.json.parser.JsonLexer.*;

/**
 *
 * @author Petr Hejl, Dusan Balek
 */
public class JsonLexer implements Lexer<JsTokenId> {

    private static final Logger LOGGER = Logger.getLogger(JsonLexer.class.getName());

    private final org.netbeans.modules.javascript2.json.parser.JsonLexer scanner;

    private TokenFactory<JsTokenId> tokenFactory;

    private JsonLexer(LexerRestartInfo<JsTokenId> info) {
        tokenFactory = info.tokenFactory();
        NbLexerCharStream charStream = new NbLexerCharStream(info);
        FileObject fo = (FileObject) info.getAttributeValue(FileObject.class);
        boolean allowComments = fo != null ? JsonOptionsQuery.getOptions(fo).isCommentSupported() : false;
        scanner = new org.netbeans.modules.javascript2.json.parser.JsonLexer(charStream, allowComments, true);
        if (info.state() instanceof LexerState) {
            scanner.setLexerState((LexerState) info.state());
        }
    }

    public static JsonLexer create(LexerRestartInfo<JsTokenId> info) {
        synchronized(JsonLexer.class) {
            return new JsonLexer(info);
        }
    }

    @Override
    public Token<JsTokenId> nextToken() {
        org.antlr.v4.runtime.Token nextToken = scanner.nextToken();
        Token<JsTokenId> token = null;
        if (nextToken.getType() != org.netbeans.modules.javascript2.json.parser.JsonLexer.EOF) {            
            token = tokenFactory.createToken(tokenId(nextToken.getType()));
            LOGGER.log(Level.FINEST, "Lexed token is {0}", token.id());
            return token;
        }
        return null;
    }

    @Override
    public Object state() {
        return scanner.getState();
    }

    @Override
    public void release() {
    }
    
    private static JsTokenId tokenId(int type) {
        switch (type) {
            case COLON: return JsTokenId.OPERATOR_COLON;
            case COMMA: return JsTokenId.OPERATOR_COMMA;
            case DOT: return JsTokenId.OPERATOR_DOT;
            case PLUS: return JsTokenId.OPERATOR_PLUS;
            case MINUS: return JsTokenId.OPERATOR_MINUS;
            case LBRACE: return JsTokenId.BRACKET_LEFT_CURLY;
            case RBRACE: return JsTokenId.BRACKET_RIGHT_CURLY;
            case LBRACKET: return JsTokenId.BRACKET_LEFT_BRACKET;
            case RBRACKET: return JsTokenId.BRACKET_RIGHT_BRACKET;
            case TRUE: return JsTokenId.KEYWORD_TRUE;
            case FALSE: return JsTokenId.KEYWORD_FALSE;
            case NULL: return JsTokenId.KEYWORD_NULL;
            case NUMBER: return JsTokenId.NUMBER;
            case STRING: return JsTokenId.STRING;
            case LINE_COMMENT: return JsTokenId.LINE_COMMENT;
            case COMMENT: return JsTokenId.BLOCK_COMMENT;
            case WS: return JsTokenId.WHITESPACE;
            default: return JsTokenId.ERROR;
        }
    }
}
