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
package org.netbeans.modules.languages.go;

import org.antlr.parser.golang.GoLexer;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LexerRestartInfo;

import static org.antlr.parser.golang.GoLexer.*;
import org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge;

import static org.netbeans.modules.languages.go.GoTokenId.*;
/**
 *
 * @author lkishalmi
 */
public final class GoLangLexer extends AbstractAntlrLexerBridge<GoLexer, GoTokenId> {

    public GoLangLexer(LexerRestartInfo<GoTokenId> info) {
        super(info, GoLexer::new);
    }

    @Override
    protected Token<GoTokenId> mapToken(org.antlr.v4.runtime.Token antlrToken) {
        switch (antlrToken.getType()) {
            case BREAK:
            case DEFAULT:
            case FUNC:
            case INTERFACE:
            case SELECT:
            case CASE:
            case DEFER:
            case GO:
            case MAP:
            case STRUCT:
            case CHAN:
            case ELSE:
            case GOTO:
            case PACKAGE:
            case SWITCH:
            case CONST:
            case FALLTHROUGH:
            case IF:
            case RANGE:
            case TYPE:
            case CONTINUE:
            case FOR:
            case IMPORT:
            case RETURN:
            case VAR:
            case NIL_LIT:
                return token(KEYWORD);

            case GoLexer.IDENTIFIER:
                return token(GoTokenId.IDENTIFIER);

            case L_PAREN:
            case R_PAREN:
            case L_CURLY:
            case R_CURLY:
            case L_BRACKET:
            case R_BRACKET:
            case ASSIGN:
            case COMMA:
            case SEMI:
            case COLON:
            case DOT:
            case PLUS_PLUS:
            case MINUS_MINUS:
            case DECLARE_ASSIGN:
            case ELLIPSIS:
                return token(SEPARATOR);

            case LOGICAL_OR:
            case LOGICAL_AND:
            case EQUALS:
            case NOT_EQUALS:
            case LESS:
            case LESS_OR_EQUALS:
            case GREATER:
            case GREATER_OR_EQUALS:
            case OR:
            case DIV:
            case MOD:
            case LSHIFT:
            case RSHIFT:
            case BIT_CLEAR:
            case EXCLAMATION:
            case PLUS:
            case MINUS:
            case CARET:
            case STAR:
            case AMPERSAND:
            case RECEIVE:
            case UNDERLYING:
                return token(OPERATOR);

            case RAW_STRING_LIT:
            case INTERPRETED_STRING_LIT:
            case RUNE_LIT:
                return token(STRING);

            case DECIMAL_LIT:
            case BINARY_LIT:
            case OCTAL_LIT:
            case HEX_LIT:
            case FLOAT_LIT:
            case DECIMAL_FLOAT_LIT:
            case HEX_FLOAT_LIT:
            case IMAGINARY_LIT:
                return token(NUMBER);

            case WS:
            case WS_NLSEMI:
            case TERMINATOR:
                return token(WHITESPACE);

            case GoLexer.COMMENT:
            case LINE_COMMENT:
            case LINE_COMMENT_NLSEMI:
                return  token(GoTokenId.COMMENT);
            case EOS:
                String text = antlrToken.getText();
                if (Character.isWhitespace(text.codePointAt(0))) {
                    return token(WHITESPACE);
                }
                if (text.startsWith("/*")) {
                    return token(GoTokenId.COMMENT);
                }
                if (text.equals(";")) {
                    return token(SEPARATOR);
                }
            default:
                return token(ERROR);
        }
    }

}
