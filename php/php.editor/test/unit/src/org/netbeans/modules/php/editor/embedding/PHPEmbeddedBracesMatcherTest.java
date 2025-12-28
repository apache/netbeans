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
package org.netbeans.modules.php.editor.embedding;

import java.time.Duration;
import java.util.Collection;
import java.util.EnumSet;
import javax.swing.text.BadLocationException;
import org.junit.Test;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.editor.bracesmatching.api.BracesMatchingTestUtils;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.csl.PHPLanguage;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.openide.util.Exceptions;

/**
 *
 * @author bhaidu
 */
public class PHPEmbeddedBracesMatcherTest extends CslTestBase {

    public static final String PHP_MIME_TEST = "text/x-php-test"; //NOI18N

    public PHPEmbeddedBracesMatcherTest(String testName) {
        super(testName);
        TestLanguageProvider.register(PhpEmbeddedTestTokenId.language());
        TestLanguageProvider.register(new PHPLanguage().getLexerLanguage());
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new PhpEmbeddedTestLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return PHP_MIME_TEST;
    }

    @Test
    public void testIssue7803() throws InterruptedException {
        BracesMatcherFactory factory = MimeLookup.getLookup(FileUtils.PHP_MIME_TYPE).lookup(BracesMatcherFactory.class);

        String testText = "{{(TestClass^:)}}"; //NOI18N
        int caretPos = testText.indexOf('^');
        testText = testText.substring(0, caretPos) + testText.substring(caretPos + 1);
        BaseDocument doc = getDocument(testText);

        MatcherContext context = BracesMatchingTestUtils.createMatcherContext(doc, caretPos, false, 1);
        BracesMatcher matcher = factory.createMatcher(context);

        try {
            matcher.findOrigin();
            matcher.findMatches();
            assertTrue("Passed Embedded Php braces matcher timeout", true);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected int timeOut() {
        return 3000;
    }

    public static enum PhpEmbeddedTestTokenId implements TokenId {

        ANY(null, null),
        PHP(null, null);

        private final String fixedText;

        private final String primaryCategory;

        PhpEmbeddedTestTokenId(String fixedText, String primaryCategory) {
            this.fixedText = fixedText;
            this.primaryCategory = primaryCategory;
        }

        @Override
        public String primaryCategory() {
            return primaryCategory;
        }

        public String fixedText() {
            return fixedText;
        }

        private static final Language<PhpEmbeddedTestTokenId> language = new LanguageHierarchy<PhpEmbeddedTestTokenId>() {

            @Override
            protected Lexer<PhpEmbeddedTestTokenId> createLexer(LexerRestartInfo<PhpEmbeddedTestTokenId> info) {
                return new PhpCustomLanguageLexer(info);
            }

            @Override
            protected Collection<PhpEmbeddedTestTokenId> createTokenIds() {
                return EnumSet.allOf(PhpEmbeddedTestTokenId.class);
            }

            @Override
            public String mimeType() {
                return PHP_MIME_TEST;
            }

            @Override
            protected LanguageEmbedding<?> embedding(Token<PhpEmbeddedTestTokenId> token,
                    LanguagePath languagePath, InputAttributes inputAttributes) {
                if (token.id().equals(PhpEmbeddedTestTokenId.PHP)) {
                    return LanguageEmbedding.create(PHPTokenId.languageInPHP(), 0, 0);
                }

                return null;
            }
        }.language();

        public static Language<PhpEmbeddedTestTokenId> language() {
            return language;
        }
    }

    public static class PhpEmbeddedTestLanguage extends DefaultLanguageConfig {

        @Override
        public Language<PhpEmbeddedTestTokenId> getLexerLanguage() {
            return PhpEmbeddedTestTokenId.language();
        }

        @Override
        public String getDisplayName() {
            return "Test language with embedded php"; //NOI18N
        }

    }

    public static class PhpCustomLanguageLexer implements Lexer<PhpEmbeddedTestTokenId> {

        private final LexerInput input;
        private final TokenFactory<PhpEmbeddedTestTokenId> factory;
        private boolean embeddedPhpState = true;

        public PhpCustomLanguageLexer(LexerRestartInfo<PhpEmbeddedTestTokenId> info) {
            this.input = info.input();
            this.factory = info.tokenFactory();
        }

        @Override
        public Token<PhpEmbeddedTestTokenId> nextToken() {
            if (input.read() == LexerInput.EOF) {
                return null;
            }

            input.read();

            if (input.readText().toString().startsWith("{{")) { //NOI18N
                embeddedPhpState = true;
                return factory.createToken(PhpEmbeddedTestTokenId.ANY);
            }

            if (embeddedPhpState && readUntil("}}")) { //NOI18N
                input.backup(2);
                embeddedPhpState = false;
                return factory.createToken(PhpEmbeddedTestTokenId.PHP);
            }

            return factory.createToken(PhpEmbeddedTestTokenId.ANY);
        }

        private boolean readUntil(String condition) {
            int read;

            while ((read = input.read()) != LexerInput.EOF && !input.readText().toString().endsWith(condition));

            return read != LexerInput.EOF;
        }

        @Override
        public Object state() {
            return null;
        }

        @Override
        public void release() {

        }

    }
}
