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

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the Go lexer.
 *
 * <p>Contains regression tests for apache/netbeans#9654: typing a double
 * quote as the last characters of a document made the lexer report end of
 * input (a null token) while characters were still pending on the lexer
 * input, which the lexer infrastructure rejects with an
 * {@link IllegalStateException} ("returned null token but
 * lexerInput.readLength()=...").
 */
public class GoLangLexerTest {

    private static final Language<GoTokenId> LANGUAGE = new LanguageHierarchy<GoTokenId>() {
        @Override
        protected Collection<GoTokenId> createTokenIds() {
            return EnumSet.allOf(GoTokenId.class);
        }

        @Override
        protected Lexer<GoTokenId> createLexer(LexerRestartInfo<GoTokenId> info) {
            return new GoLangLexer(info);
        }

        @Override
        protected String mimeType() {
            return "text/x-go";
        }
    }.language();

    /**
     * The document shape from issue #9654: the typed quote leaves six
     * characters, {@code ")}, LF, {@code }}, LF, LF, untokenized at the end
     * of the document.
     */
    private static final String ISSUE_9654_TEXT = "package main\n"
            + "\n"
            + "import (\n"
            + "  \"fmt\"\n"
            + ")\n"
            + "\n"
            + "func main() {\n"
            + "  fmt.Println(\")\n"
            + "}\n"
            + "\n";

    /**
     * An unterminated interpreted string literal at the end of the text must
     * still receive a token instead of an end-of-input signal.
     */
    @Test
    public void testUnterminatedStringAtEndOfText() {
        Token<?> last = lastToken(ISSUE_9654_TEXT);
        assertEquals(GoTokenId.ERROR, last.id());
        assertEquals("\")\n}\n\n", last.text().toString());
    }

    /**
     * Issue #9654: typing {@code "} to declare a string variable, before the
     * literal is closed, used to throw while re-tokenizing the document.
     */
    @Test
    public void testTypedQuoteDoesNotThrow() {
        String text = "package main\n"
                + "\n"
                + "func main() {\n"
                + "  var foo string = \"\n"
                + "}\n";
        Token<?> last = lastToken(text);
        assertEquals(GoTokenId.ERROR, last.id());
        assertEquals("\"\n}\n", last.text().toString());
    }

    /** An unterminated rune literal at the end of the text. */
    @Test
    public void testUnterminatedRuneAtEndOfText() {
        Token<?> last = lastToken("package main\nvar x = '");
        assertEquals(GoTokenId.ERROR, last.id());
        assertEquals("'", last.text().toString());
    }

    /** An unterminated raw string literal at the end of the text. */
    @Test
    public void testUnterminatedRawStringAtEndOfText() {
        Token<?> last = lastToken("package main\nvar x = `raw");
        assertEquals(GoTokenId.ERROR, last.id());
        assertEquals("`raw", last.text().toString());
    }

    /** A closed string literal at the end of the text is still a STRING. */
    @Test
    public void testClosedStringAtEndOfText() {
        Token<?> last = lastToken("package main\nvar foo = \"bar\"");
        assertEquals(GoTokenId.STRING, last.id());
        assertEquals("\"bar\"", last.text().toString());
    }

    /**
     * Well formed text must be tokenized completely and without any ERROR
     * token: end of input without pending characters stays a null token.
     */
    @Test
    public void testWellFormedText() {
        String text = "package main\n"
                + "\n"
                + "import (\n"
                + "  \"fmt\"\n"
                + ")\n"
                + "\n"
                + "func main() {\n"
                + "  fmt.Println(\"hello world\")\n"
                + "}\n";
        TokenSequence<GoTokenId> seq = TokenHierarchy.create(text, LANGUAGE)
                .tokenSequence(LANGUAGE);
        boolean foundString = false;
        int count = 0;
        while (seq.moveNext()) {
            count++;
            Token<GoTokenId> token = seq.token();
            assertNotEquals("Unexpected ERROR token in:\n" + text,
                    GoTokenId.ERROR, token.id());
            if (token.id() == GoTokenId.STRING
                    && "\"hello world\"".equals(token.text().toString())) {
                foundString = true;
            }
        }
        assertTrue("No tokens recognized", count > 0);
        assertTrue("String literal was not tokenized", foundString);
    }

    /**
     * Lex the given text to its end. Fails with the IllegalStateException
     * from issue #9654 if the lexer returns a null token while characters
     * are still pending on the lexer input.
     */
    private static Token<?> lastToken(String text) {
        TokenSequence<GoTokenId> seq = TokenHierarchy.create(text, LANGUAGE)
                .tokenSequence(LANGUAGE);
        Token<?> last = null;
        while (seq.moveNext()) {
            last = seq.token();
        }
        assertNotNull("No tokens recognized", last);
        return last;
    }
}
