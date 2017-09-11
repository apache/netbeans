/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.lexer.lang;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Language that changes dynamically to test Language.refresh().
 *
 * @author Miloslav Metelka
 */
public enum TestEmbeddingTokenId implements TokenId {
    
    TEXT, // any text
    A, // "a" - like always query
    C, // "c" - like cached first query
    N, // "n" - like none
    LINE_COMMENT; // "// ..."; added after change only

    private final String primaryCategory;

    TestEmbeddingTokenId() {
        this(null);
    }

    TestEmbeddingTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }
    
    private static Language<TestEmbeddingTokenId> language = 
        new LanguageHierarchy<TestEmbeddingTokenId>() {
            @Override
            protected Collection<TestEmbeddingTokenId> createTokenIds() {
                return EnumSet.allOf(TestEmbeddingTokenId.class);
            }

            @Override
            protected Map<String,Collection<TestEmbeddingTokenId>> createTokenCategories() {
                return null;
            }

            @Override
            protected Lexer<TestEmbeddingTokenId> createLexer(LexerRestartInfo<TestEmbeddingTokenId> info) {
                return new LexerImpl(info);
            }
            
            @Override
            protected LanguageEmbedding<?> embedding(Token<TestEmbeddingTokenId> token,
            LanguagePath languagePath, InputAttributes inputAttributes) {
                switch (token.id()) {
                    case A:
                        aEmbeddingQueryCount++;
                        return null; // Should be re-called even after returning null
                    case C:
                        cEmbeddingQueryCount++;
                        return null;
                    case N:
                        // Should never be reached due to embeddingPresence()
                        throw new IllegalStateException("Should never be reached.");
                    default:
                        return null;
                }
            }

            @Override
            protected EmbeddingPresence embeddingPresence(TestEmbeddingTokenId id) {
                switch (id) {
                    case A:
                        return EmbeddingPresence.ALWAYS_QUERY;
                    case C:
                        return EmbeddingPresence.CACHED_FIRST_QUERY;
                    case N:
                        return EmbeddingPresence.NONE;
                }
                return EmbeddingPresence.CACHED_FIRST_QUERY;
            }

            @Override
            protected String mimeType() {
                return MIME_TYPE;
            }
        }.language();


    public static Language<TestEmbeddingTokenId> language() {
        return language;
    }
    
    public static final String MIME_TYPE = "text/x-embedding";
    
    public static int aEmbeddingQueryCount;
    
    public static int cEmbeddingQueryCount;
    
    private static final class LexerImpl implements Lexer<TestEmbeddingTokenId> {
    
        // Copy of LexerInput.EOF
        private static final int EOF = LexerInput.EOF;

        private final LexerInput input;

        private final TokenFactory<TestEmbeddingTokenId> tokenFactory;

        LexerImpl(LexerRestartInfo<TestEmbeddingTokenId> info) {
            this.input = info.input();
            this.tokenFactory = info.tokenFactory();
            assert (info.state() == null); // never set to non-null value in state()
        }

        public Object state() {
            return null; // always in default state after token recognition
        }

        public Token<TestEmbeddingTokenId> nextToken() {
            while (true) {
                switch (input.read()) {
                    case '/':
                        if (input.readLength() > 1) {
                            input.backup(1);
                            return token(TEXT);
                        }
                        switch (input.read()) {
                            case '/': // in single-line comment
                                while (true) {
                                    switch (input.read()) {
                                        case '\r': input.consumeNewline();
                                        case '\n':
                                        case EOF:
                                            return token(LINE_COMMENT);
                                    }
                                }
                                //break;
                        }
                        break;
                        
                    case 'a':
                        if (input.readLength() > 1) {
                            input.backup(1);
                            return token(TEXT);
                        }
                        return token(A);

                    case 'c':
                        if (input.readLength() > 1) {
                            input.backup(1);
                            return token(TEXT);
                        }
                        return token(C);

                    case 'n':
                        if (input.readLength() > 1) {
                            input.backup(1);
                            return token(TEXT);
                        }
                        return token(N);

                    case EOF:
                        if (input.readLength() > 0) {
                            input.backup(1);
                            return token(TEXT);
                        }
                        return null;
                }
            }
        }
        
        private Token<TestEmbeddingTokenId> token(TestEmbeddingTokenId id) {
            return tokenFactory.createToken(id);
        }
        
        public void release() {
        }
    
    }
    
}
