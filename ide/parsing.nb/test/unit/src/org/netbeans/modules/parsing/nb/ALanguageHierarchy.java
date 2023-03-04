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
package org.netbeans.modules.parsing.nb;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

public class ALanguageHierarchy extends LanguageHierarchy<ALanguageHierarchy.ATokenId> {

    private List<ATokenId> tokenIds = Arrays.asList (new ATokenId[]{new ATokenId ("whitespace", 1), new ATokenId ("word", 2)});

    protected Collection<ATokenId> createTokenIds () {
        return tokenIds;
    }

    @Override
    protected Lexer<ATokenId> createLexer (LexerRestartInfo<ATokenId> info) {
        return new ALexer (info);
    }

    @Override
    protected String mimeType () {
        return "text/foo";
    }

    public static class ATokenId implements TokenId {

        private String          name;
        private int             ordinal;

        public ATokenId (String name, int ordinal) {
            this.name = name;
            this.ordinal = ordinal;
        }


        public String name () {
            return name;
        }

        public int ordinal () {
            return ordinal;
        }

        public String primaryCategory () {
            return "main";
        }
    }

    private class ALexer implements Lexer<ATokenId> {

        private LexerRestartInfo<ATokenId> info;

        public ALexer (LexerRestartInfo<ATokenId> info) {
            super ();
            this.info = info;
        }

        public Token<ATokenId> nextToken () {
            LexerInput lexerInput = info.input ();
            int i = lexerInput.read ();
            if (i == ' ') {
                do {
                    i = lexerInput.read ();
                } while (i == ' ');
                if (i != LexerInput.EOF) {
                    lexerInput.backup (1);
                }
                return info.tokenFactory ().createToken (tokenIds.get (0));
            } else {
                if (i == LexerInput.EOF) {
                    return null;
                } else {
                    do {
                        i = lexerInput.read ();
                    } while (i != ' ' && i != LexerInput.EOF);
                    if (i != LexerInput.EOF) {
                        lexerInput.backup (1);
                    }
                    return info.tokenFactory ().createToken (tokenIds.get (1));
                }
            }
        }

        public Object state () {
            return null;
        }

        public void release () {
        }
    }
}
