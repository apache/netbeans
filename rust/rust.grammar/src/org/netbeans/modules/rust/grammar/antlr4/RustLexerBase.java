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
 /*
    Copyright (c) 2010 The Rust Project Developers
    Copyright (c) 2020-2022 Student Main
    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
    documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
    rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
    persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice (including the next paragraph) shall be included in all copies or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
    WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
    COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
    OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
 /* This file was originally obtained from 
 * https://github.com/antlr/grammars-v4/blob/master/rust/Java/RustLexerBase.java,
 * the Rust Antlr4 grammar by the Rust Team, released under the MIT License.
 */
package org.netbeans.modules.rust.grammar.antlr4;

import java.util.logging.Logger;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

/**
 *
 * @see
 * <a href="https://github.com/antlr/grammars-v4/blob/master/rust/Java/RustLexerBase.java">RustLexerBase.java</a>
 */
public abstract class RustLexerBase extends Lexer {

    private static final Logger LOG = Logger.getLogger(RustLexerBase.class.getName());

    public RustLexerBase(CharStream input) {
        super(input);
    }

    Token lt1;
    Token lt2;

    @Override
    public Token nextToken() {
        Token next = null;

//        try {
            next = super.nextToken();
//        } catch (Throwable e) {
//            StringBuilder message = new StringBuilder();
//            message.append(
//                    String.format("RUSTLEXER: Unexpected exception (%s) in RustLexerBase.nextToken: \"%s\"%n",
//                            e.getClass().getName(),
//                            e.getMessage()));
//            if (this.lt1 != null) {
//                RustTokenID tid = RustTokenID.from(this.lt1);
//                message.append(String.format("RUSTLEXER: ... after token %s at line %d:%d in file %s%n",
//                        tid.name(),
//                        this.lt1.getLine(),
//                        this.lt1.getCharPositionInLine(),
//                        _input.getSourceName()
//                ));
//            }
//            LOG.log(Level.SEVERE, message.toString(), e);
//            return null;
//        }

        if (next.getChannel() == Token.DEFAULT_CHANNEL) {
            // Keep track of the last token on the default channel.
            this.lt2 = this.lt1;
            this.lt1 = next;
        }

        return next;
    }

    public boolean SOF() {
        return _input.index() == 0;
        // return _input.LA(-1) <= 0;
    }

    public boolean next(char expect) {
        return _input.LA(1) == expect;
    }

    public boolean floatDotPossible() {
        int next = _input.LA(1);
        // only block . _ identifier after float
        if (next == '.' || next == '_') {
            return false;
        }
        if (next == 'f') {
            // 1.f32
            if (_input.LA(2) == '3' && _input.LA(3) == '2') {
                return true;
            }
            //1.f64
            if (_input.LA(2) == '6' && _input.LA(3) == '4') {
                return true;
            }
            return false;
        }
        if (next >= 'a' && next <= 'z') {
            return false;
        }
        if (next >= 'A' && next <= 'Z') {
            return false;
        }
        return true;
    }

    public boolean floatLiteralPossible() {
        if (this.lt1 == null || this.lt2 == null) {
            return true;
        }
        if (this.lt1.getType() != RustLexer.DOT) {
            return true;
        }
        switch (this.lt2.getType()) {
            case RustLexer.CHAR_LITERAL:
            case RustLexer.STRING_LITERAL:
            case RustLexer.RAW_STRING_LITERAL:
            case RustLexer.BYTE_LITERAL:
            case RustLexer.BYTE_STRING_LITERAL:
            case RustLexer.RAW_BYTE_STRING_LITERAL:
            case RustLexer.INTEGER_LITERAL:
            case RustLexer.DEC_LITERAL:
            case RustLexer.HEX_LITERAL:
            case RustLexer.OCT_LITERAL:
            case RustLexer.BIN_LITERAL:

            case RustLexer.KW_SUPER:
            case RustLexer.KW_SELFVALUE:
            case RustLexer.KW_SELFTYPE:
            case RustLexer.KW_CRATE:
            case RustLexer.KW_DOLLARCRATE:

            case RustLexer.GT:
            case RustLexer.RCURLYBRACE:
            case RustLexer.RSQUAREBRACKET:
            case RustLexer.RPAREN:

            case RustLexer.KW_AWAIT:

            case RustLexer.NON_KEYWORD_IDENTIFIER:
            case RustLexer.RAW_IDENTIFIER:
            case RustLexer.KW_MACRORULES:
                return false;
            default:
                return true;
        }
    }
}
