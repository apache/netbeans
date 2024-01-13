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

package org.netbeans.modules.cnd.meson.lexer;

import java.util.HashSet;
import java.util.Set;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

public class MesonOptionsLexer implements Lexer<MesonOptionsTokenId> {
    private static final Set<String> keywords = new HashSet<>();
    private static final Set<String> literals = new HashSet<>();

    static {
        keywords.add("choices"); // NOI18N
        keywords.add("deprecated"); // NOI18N
        keywords.add("description"); // NOI18N
        keywords.add("max"); // NOI18N
        keywords.add("min"); // NOI18N
        keywords.add("type"); // NOI18N
        keywords.add("value"); // NOI18N
        keywords.add("yield"); // NOI18N

        literals.add("false"); // NOI18N
        literals.add("true"); // NOI18N
    }

    private final LexerRestartInfo<MesonOptionsTokenId> info;

    MesonOptionsLexer(LexerRestartInfo<MesonOptionsTokenId> info) {
        this.info = info;
    }

    @Override
    public Token<MesonOptionsTokenId> nextToken () {
        LexerInput input = info.input ();
        int i = input.read ();
        switch (i) {
            case LexerInput.EOF:
                return null;
            case '+':
            case ',':
            case '(':
            case ')':
            case '{':
            case '}':
            case '[':
            case ']':
            case ':':
                return info.tokenFactory().createToken(MesonOptionsTokenId.OPERATOR);
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                do {
                    i = input.read();
                } while ((i == ' ') || (i == '\n') || (i == '\r') || (i == '\t'));
                if (i != LexerInput.EOF) {
                    input.backup(1);
                }
                return info.tokenFactory().createToken(MesonOptionsTokenId.WHITESPACE);
            case '#':
                do {
                    i = input.read();
                } while ((i != '\n') && (i != '\r') && (i != LexerInput.EOF));
                return info.tokenFactory().createToken(MesonOptionsTokenId.COMMENT);
            case '\'':
                // meson options files only support simple string literals like 'string'
                do {
                    i = input.read();
                } while ((i != '\'') && (i != '\n') && (i != '\r') && (i != LexerInput.EOF));
                return info.tokenFactory().createToken(MesonOptionsTokenId.STRING);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                // meson options files only support integer numeric literals
                do {
                    i = input.read();
                } while ((i >= '0') && (i <= '9'));
                input.backup(1);
                return info.tokenFactory().createToken(MesonOptionsTokenId.NUMBER);
            default:
                if (   (i >= 'a' && i <= 'z')
                    || (i >= 'A' && i <= 'Z')) {
                    do {
                        i = input.read();
                    } while (   ((i >= 'a') && (i <= 'z'))
                             || ((i >= 'A') && (i <= 'Z')));
                    input.backup(1);
                    final String token = input.readText().toString();
                    if ("option".equals(token)) {
                        return info.tokenFactory().createToken(MesonOptionsTokenId.OPTION);
                    }
                    else if (keywords.contains(token)) {
                        return info.tokenFactory().createToken(MesonOptionsTokenId.ARGUMENT_KEYWORD);
                    }
                    else if (literals.contains(token)) {
                        return info.tokenFactory().createToken(MesonOptionsTokenId.LITERAL);
                    }
                }
                return info.tokenFactory().createToken(MesonOptionsTokenId.ERROR);
        }
    }

    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {
    }
}