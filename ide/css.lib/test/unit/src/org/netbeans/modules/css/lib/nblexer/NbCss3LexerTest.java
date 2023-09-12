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
package org.netbeans.modules.css.lib.nblexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.css.lib.Css3Parser;
import org.netbeans.modules.css.lib.api.CssTokenId;

/**
 * @author  marek.fukala@sun.com
 */
public class NbCss3LexerTest extends NbTestCase {

    public NbCss3LexerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
    }

    public void testAllANTLRTokensHasNbTokenIds() {
        for (String tokenName : Css3Parser.tokenNames) {
            char first = tokenName.charAt(0);
            switch (first) {
                case '<':
                case '\'':
                    continue;
                default:
                    assertNotNull(CssTokenId.valueOf(tokenName));

            }
        }
    }

    //http://www.netbeans.org/issues/show_bug.cgi?id=161642
    public void testIssue161642() throws Exception {
        String input = "/* c */;";
        TokenHierarchy<String> th = TokenHierarchy.create(input, CssTokenId.language());
        TokenSequence<?> ts = th.tokenSequence();
        ts.moveStart();

        assertTrue(ts.moveNext());
        assertEquals("/* c */", ts.token().text().toString());
        assertEquals(CssTokenId.COMMENT, ts.token().id());
        assertEquals("comments", ts.token().id().primaryCategory());

        assertTrue(ts.moveNext());
        assertEquals(";", ts.token().text().toString());
        assertEquals(CssTokenId.SEMI, ts.token().id());
    }

    public void testOnlyAtSymbolLexing() throws Exception {
        String input = "@";
        TokenHierarchy<String> th = TokenHierarchy.create(input, CssTokenId.language());
        TokenSequence<CssTokenId> ts = th.tokenSequence(CssTokenId.language());
        ts.moveStart();

        assertTrue(ts.moveNext());
        Token<CssTokenId> token = ts.token();

        assertNotNull(token);
        assertEquals(CssTokenId.ERROR, token.id());

    }

    public void testBasicLexing() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testBasic.css.txt",
                CssTokenId.language());
    }

    public void testLexingOfMissingTokens() throws Exception {
        String code = "a {\n"
                + " @ color: red; \n"
                + " background: red; \n"
                + "}";

        TokenHierarchy<String> th = TokenHierarchy.create(code, CssTokenId.language());
        TokenSequence<?> ts = th.tokenSequence();
        ts.moveStart();

        while (ts.moveNext()) {
//            System.out.println(ts.offset() + "-" + (ts.token().length() + ts.offset()) + ": " + ts.token().text() + "(" + ts.token().id() + ")");
        }

    }

    public void testErrorCase1() throws Exception {
        /*
        java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 4
        at java.util.Vector.get(Vector.java:694)
        at org.netbeans.modules.css.lib.nblexer.NbLexerCharStream.rewind(NbLexerCharStream.java:131)
        at org.antlr.runtime.DFA.predict(DFA.java:149)
        at org.netbeans.modules.css.lib.Css3Lexer.mNUMBER(Css3Lexer.java:7440)
         */
        String source = "padding: .5em; ";

        TokenHierarchy<String> th = TokenHierarchy.create(source, CssTokenId.language());
        TokenSequence<CssTokenId> ts = th.tokenSequence(CssTokenId.language());
        ts.moveStart();

        assertToken("padding", CssTokenId.IDENT, ts);
        assertToken(":", CssTokenId.COLON, ts);
        assertToken(" ", CssTokenId.WS, ts);
        assertToken(".5em", CssTokenId.EMS, ts);

    }

    public void testCounterStyle() throws Exception {
        String source = "@counter-style x { }";

        TokenHierarchy<String> th = TokenHierarchy.create(source, CssTokenId.language());
        TokenSequence<CssTokenId> ts = th.tokenSequence(CssTokenId.language());
        ts.moveStart();

        assertToken("@counter-style", CssTokenId.COUNTER_STYLE_SYM, ts);
        assertToken(" ", CssTokenId.WS, ts);
    }

    public void testLexingOfSemicolonAtTheEndOfFile() throws Exception {
        String source = "div:";

        TokenHierarchy<String> th = TokenHierarchy.create(source, CssTokenId.language());
        TokenSequence<CssTokenId> ts = th.tokenSequence(CssTokenId.language());
        ts.moveStart();

        assertToken("div", CssTokenId.IDENT, ts);
        assertToken(":", CssTokenId.COLON, ts);
    }

    private void assertToken(String expectedImage, CssTokenId expectedType, TokenSequence<CssTokenId> ts) {
        assertTrue(ts.moveNext());
        Token<CssTokenId> token = ts.token();
        assertNotNull(token);
        assertEquals(expectedType, token.id());
        assertEquals(expectedImage, token.text().toString());
    }

    public void testLexing_Netbeans_org() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/netbeans.css",
                CssTokenId.language());
    }

    ;

    public void testNamespaces() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/namespaces.css",
                CssTokenId.language());
    }

    ;

    public void testInput() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testInputGeneratedCode.css.txt",
                CssTokenId.language());
    }

    public void testImportsLexing() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testImportsLexing.css.txt",
                CssTokenId.language());
    }

    public void testIssue240881() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testIssue240881.css.txt",
                CssTokenId.language());
    }

    public void testIssue240757() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/less/testIssue240757.less.txt",
                CssTokenId.language());
    }

    public void testIssue240701() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/less/testIssue240701.less.txt",
                CssTokenId.language());
    }
    
    public void testIssue238864() throws Exception {
//        LexerTestUtilities.checkTokenDump(this, "testfiles/scss/large_empty.scss.txt",
//                CssTokenId.language());
        int LINES = 10 * 1000;
        
        StringBuilder source = new StringBuilder();
        for(int i = 0; i < LINES; i++) {
            source.append('\n');
        }
        
        TokenHierarchy<StringBuilder> th = TokenHierarchy.create(source, CssTokenId.language());
        TokenSequence<CssTokenId> ts = th.tokenSequence(CssTokenId.language());
        ts.moveStart();
        
        assertTrue(ts.moveNext());
        Token<CssTokenId> token = ts.token();
        
        assertEquals(token.id(), CssTokenId.NL);
        assertEquals(ts.offset(), 0);
        assertEquals(token.length(), LINES);
        
        assertFalse(ts.moveNext());
       
    }

    public void testLexingUrange() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/urange.css",
                CssTokenId.language());
    }
}
