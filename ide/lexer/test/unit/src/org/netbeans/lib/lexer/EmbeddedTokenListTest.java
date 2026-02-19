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
package org.netbeans.lib.lexer;

import java.util.Arrays;
import java.util.Collection;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.lang.TestEmbeddingTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.lang.TestJavadocTokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Jan Lahoda
 */
public class EmbeddedTokenListTest extends TestCase {
    
    public EmbeddedTokenListTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testUpdateStartOffset() throws Exception {
        Document d = new PlainDocument();
        
        d.putProperty(Language.class,TestTokenId.language());
        
        d.insertString(0, "ident ident /** @see X */", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        ((AbstractDocument)d).readLock();
        try {
            TokenSequence<?> ts = h.tokenSequence();

            LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.IDENTIFIER, "ident");
            assertEquals(0, ts.offset());

            LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.WHITESPACE, " ");
            assertEquals(5, ts.offset());

            LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.IDENTIFIER, "ident");
            assertEquals(6, ts.offset());

            LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.WHITESPACE, " ");
            assertEquals(11, ts.offset());

            LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.JAVADOC_COMMENT, "/** @see X */");
            assertEquals(12, ts.offset());

            TokenSequence<?> inner = ts.embedded();

            assertNotNull(inner);

            LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.OTHER_TEXT, " ");
            assertEquals(15, inner.offset());

            LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.TAG, "@see");
            assertEquals(16, inner.offset());

            LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.OTHER_TEXT, " ");
            assertEquals(20, inner.offset());

            LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.IDENT, "X");
            assertEquals(21, inner.offset());

            LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.OTHER_TEXT, " ");
            assertEquals(22, inner.offset());
        } finally {
            ((AbstractDocument)d).readUnlock();
        }
    }

    public void testSnapshots() throws Exception {
        Document d = new PlainDocument();
        
        d.putProperty(Language.class,TestTokenId.language());
        
        d.insertString(0, "ident ident /** @see X */", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        ((AbstractDocument)d).readLock();
        try {
            TokenSequence<?> ts = h.tokenSequence();

            LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.IDENTIFIER, "ident");
            assertEquals(0, ts.offset());

            LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.WHITESPACE, " ");
            assertEquals(5, ts.offset());

            LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.IDENTIFIER, "ident");
            assertEquals(6, ts.offset());

            LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.WHITESPACE, " ");
            assertEquals(11, ts.offset());

            LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.JAVADOC_COMMENT, "/** @see X */");
            assertEquals(12, ts.offset());

            TokenSequence<?> inner = ts.embedded();

            assertNotNull(inner);

            LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.OTHER_TEXT, " ");
            assertEquals(15, inner.offset());

            LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.TAG, "@see");
            assertEquals(16, inner.offset());

            LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.OTHER_TEXT, " ");
            assertEquals(20, inner.offset());

            LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.IDENT, "X");
            assertEquals(21, inner.offset());

            LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.OTHER_TEXT, " ");
            assertEquals(22, inner.offset());
        } finally {
            ((AbstractDocument)d).readUnlock();
        }
            
        
        
//        h = TokenHierarchy.get(d).createSnapshot();
//        ts = h.tokenSequence();
//        
//        LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.IDENTIFIER, "ident");
//        assertEquals(0, ts.offset());
//        
//        LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.WHITESPACE, " ");
//        assertEquals(5, ts.offset());
//        
//        LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.IDENTIFIER, "ident");
//        assertEquals(6, ts.offset());
//        
//        LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.WHITESPACE, " ");
//        assertEquals(11, ts.offset());
//        
//        LexerTestUtilities.assertNextTokenEquals(ts,TestTokenId.JAVADOC_COMMENT, "/** @see X */");
//        assertEquals(12, ts.offset());
//        
//        inner = ts.embedded();
//        
//        assertNotNull(inner);
//        
//        LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.OTHER_TEXT, " ");
//        assertEquals(15, inner.offset());
//        
//        LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.TAG, "@see");
//        assertEquals(16, inner.offset());
//        
//        LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.OTHER_TEXT, " ");
//        assertEquals(20, inner.offset());
//        
//        LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.IDENT, "X");
//        assertEquals(21, inner.offset());
//        
//        LexerTestUtilities.assertNextTokenEquals(inner,TestJavadocTokenId.OTHER_TEXT, " ");
//        assertEquals(22, inner.offset());
    }
    
    public void testEmbeddingPresence() throws Exception {
        Document d = new PlainDocument();
        d.putProperty(Language.class,TestEmbeddingTokenId.language());
        d.insertString(0, " acnacn", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        ((AbstractDocument)d).readLock();
        try {
            TokenSequence<TestEmbeddingTokenId> ts = h.tokenSequence(TestEmbeddingTokenId.language());
            TokenSequence<?> inner;

            LexerTestUtilities.assertNextTokenEquals(ts,TestEmbeddingTokenId.TEXT, " ");
            inner = ts.embedded();
            LexerTestUtilities.assertNextTokenEquals(ts,TestEmbeddingTokenId.A, "a");
            inner = ts.embedded();
            LexerTestUtilities.assertNextTokenEquals(ts,TestEmbeddingTokenId.C, "c");
            inner = ts.embedded();
            LexerTestUtilities.assertNextTokenEquals(ts,TestEmbeddingTokenId.N, "n");
            inner = ts.embedded();
            LexerTestUtilities.assertNextTokenEquals(ts,TestEmbeddingTokenId.A, "a");
            inner = ts.embedded();
            LexerTestUtilities.assertNextTokenEquals(ts,TestEmbeddingTokenId.C, "c");
            inner = ts.embedded();
            LexerTestUtilities.assertNextTokenEquals(ts,TestEmbeddingTokenId.N, "n");
            inner = ts.embedded();

            assertEquals(1, TestEmbeddingTokenId.cEmbeddingQueryCount);
            assertEquals(2, TestEmbeddingTokenId.aEmbeddingQueryCount);
        } finally {
            ((AbstractDocument)d).readUnlock();
        }
    }

    public void testEmbeddingModification() throws Exception {
        Document d = new PlainDocument();
        d.putProperty(Language.class, EmbeddingModificationTopTokenId.language);
        d.insertString(0, "  'foo bar test'  ", null);

        TokenHierarchy<?> h = TokenHierarchy.get(d);
        ((AbstractDocument)d).readLock();
        try {
            TokenSequence<EmbeddingModificationTopTokenId> ts = h.tokenSequence(EmbeddingModificationTopTokenId.language);

            LexerTestUtilities.assertNextTokenEquals(ts, EmbeddingModificationTopTokenId.OTHER, "  ");
            LexerTestUtilities.assertNextTokenEquals(ts, EmbeddingModificationTopTokenId.STRING, "'foo bar test'");
            LexerTestUtilities.assertNextTokenEquals(ts, EmbeddingModificationTopTokenId.OTHER, "  \n");

            ts.moveIndex(1);

            assertTrue(ts.moveNext());
            assertTrue(ts.createEmbedding(EmbeddingModificationEmbeddedTokenId.language, 1, 1));

            TokenSequence<?> nested = ts.embedded();

            LexerTestUtilities.assertNextTokenEquals(nested, EmbeddingModificationEmbeddedTokenId.WORD, "foo");
            LexerTestUtilities.assertNextTokenEquals(nested, EmbeddingModificationEmbeddedTokenId.OTHER, " ");
            LexerTestUtilities.assertNextTokenEquals(nested, EmbeddingModificationEmbeddedTokenId.WORD, "bar");
            LexerTestUtilities.assertNextTokenEquals(nested, EmbeddingModificationEmbeddedTokenId.OTHER, " ");
            LexerTestUtilities.assertNextTokenEquals(nested, EmbeddingModificationEmbeddedTokenId.WORD, "test");
        } finally {
            ((AbstractDocument)d).readUnlock();
        }

        d.insertString(9, "a", null);

        ((AbstractDocument)d).readLock();
        try {
            TokenSequence<EmbeddingModificationTopTokenId> ts = h.tokenSequence(EmbeddingModificationTopTokenId.language);

            LexerTestUtilities.assertNextTokenEquals(ts, EmbeddingModificationTopTokenId.OTHER, "  ");
            LexerTestUtilities.assertNextTokenEquals(ts, EmbeddingModificationTopTokenId.STRING, "'foo baar test'");
            LexerTestUtilities.assertNextTokenEquals(ts, EmbeddingModificationTopTokenId.OTHER, "  \n");

            ts.moveIndex(1);

            assertTrue(ts.moveNext());

            TokenSequence<?> nested = ts.embedded();

            LexerTestUtilities.assertNextTokenEquals(nested, EmbeddingModificationEmbeddedTokenId.WORD, "foo");
            LexerTestUtilities.assertNextTokenEquals(nested, EmbeddingModificationEmbeddedTokenId.OTHER, " ");
            LexerTestUtilities.assertNextTokenEquals(nested, EmbeddingModificationEmbeddedTokenId.WORD, "baar");
            LexerTestUtilities.assertNextTokenEquals(nested, EmbeddingModificationEmbeddedTokenId.OTHER, " ");
            LexerTestUtilities.assertNextTokenEquals(nested, EmbeddingModificationEmbeddedTokenId.WORD, "test");
        } finally {
            ((AbstractDocument)d).readUnlock();
        }
    }

    private enum EmbeddingModificationTopTokenId implements TokenId {
        STRING,
        OTHER;

        @Override
        public String primaryCategory() {
            return "text";
        }
        private static Language<EmbeddingModificationTopTokenId> language =
            new LanguageHierarchy<EmbeddingModificationTopTokenId>() {
            @Override
            protected Collection<EmbeddingModificationTopTokenId> createTokenIds() {
                return Arrays.asList(EmbeddingModificationTopTokenId.values());
            }

            @Override
            protected Lexer<EmbeddingModificationTopTokenId> createLexer(LexerRestartInfo<EmbeddingModificationTopTokenId> info) {
                class Impl implements Lexer<EmbeddingModificationTopTokenId> {
                    @Override
                    public Token<EmbeddingModificationTopTokenId> nextToken() {
                        if (info.input().read() == '\'') {
                            int r;
                            while ((r = info.input().read()) != '\'' && r != LexerInput.EOF)
                                ;
                            if (r == LexerInput.EOF) {
                                info.input().backup(1);
                                return info.tokenFactory().createToken(STRING, info.input().readLength(), PartType.START);
                            }
                            return info.tokenFactory().createToken(STRING);
                        }
                        int r;
                        while ((r = info.input().read()) != '\'' && r != LexerInput.EOF)
                            ;
                        info.input().backup(1);
                        return info.tokenFactory().createToken(OTHER);
                    }
                    @Override
                    public Object state() {
                        return null;
                    }
                    @Override
                    public void release() {
                    }
                }
                return new Impl();
            }

            @Override
            protected String mimeType() {
                return "text/top-level";
            }
        }.language();
    }

    private enum EmbeddingModificationEmbeddedTokenId implements TokenId {
        WORD,
        OTHER;

        @Override
        public String primaryCategory() {
            return "text";
        }
        private static Language<EmbeddingModificationEmbeddedTokenId> language =
            new LanguageHierarchy<EmbeddingModificationEmbeddedTokenId>() {
            @Override
            protected Collection<EmbeddingModificationEmbeddedTokenId> createTokenIds() {
                return Arrays.asList(EmbeddingModificationEmbeddedTokenId.values());
            }

            @Override
            protected Lexer<EmbeddingModificationEmbeddedTokenId> createLexer(LexerRestartInfo<EmbeddingModificationEmbeddedTokenId> info) {
                class Impl implements Lexer<EmbeddingModificationEmbeddedTokenId> {
                    @Override
                    public Token<EmbeddingModificationEmbeddedTokenId> nextToken() {
                        if (Character.isLetter(info.input().read())) {
                            int r;
                            while (Character.isLetter(r = info.input().read()) && r != LexerInput.EOF)
                                ;
                            info.input().backup(1);
                            return info.tokenFactory().createToken(WORD);
                        }
                        int r;
                        while (!Character.isLetter(r = info.input().read()) && r != LexerInput.EOF)
                            ;
                        info.input().backup(1);
                        if (info.input().readLength() == 0) {
                            return null;
                        }
                        return info.tokenFactory().createToken(OTHER);
                    }
                    @Override
                    public Object state() {
                        return null;
                    }
                    @Override
                    public void release() {
                    }
                }
                return new Impl();
            }

            @Override
            protected String mimeType() {
                return "text/embedded";
            }
        }.language();
    }
}
