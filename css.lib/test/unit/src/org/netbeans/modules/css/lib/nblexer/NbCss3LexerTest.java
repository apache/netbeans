/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.nblexer;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.css.lib.Css3Lexer;
import org.netbeans.modules.css.lib.Css3Parser;
import org.netbeans.modules.css.lib.ExtCss3Lexer;
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
        TokenHierarchy th = TokenHierarchy.create(input, CssTokenId.language());
        TokenSequence ts = th.tokenSequence();
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
        TokenHierarchy th = TokenHierarchy.create(input, CssTokenId.language());
        TokenSequence ts = th.tokenSequence();
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

        TokenHierarchy th = TokenHierarchy.create(code, CssTokenId.language());
        TokenSequence ts = th.tokenSequence();
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

        TokenHierarchy th = TokenHierarchy.create(source, CssTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        assertToken("padding", CssTokenId.IDENT, ts);
        assertToken(":", CssTokenId.COLON, ts);
        assertToken(" ", CssTokenId.WS, ts);
        assertToken(".5em", CssTokenId.EMS, ts);

    }

    public void testCounterStyle() throws Exception {
        String source = "@counter-style x { }";

        TokenHierarchy th = TokenHierarchy.create(source, CssTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        assertToken("@counter-style", CssTokenId.COUNTER_STYLE_SYM, ts);
        assertToken(" ", CssTokenId.WS, ts);
    }

    public void testLexingOfSemicolonAtTheEndOfFile() throws Exception {
        String source = "div:";

        TokenHierarchy th = TokenHierarchy.create(source, CssTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        assertToken("div", CssTokenId.IDENT, ts);
        assertToken(":", CssTokenId.COLON, ts);
    }

    private void assertToken(String expectedImage, CssTokenId expectedType, TokenSequence ts) {
        assertTrue(ts.moveNext());
        Token token = ts.token();
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
        
        TokenHierarchy th = TokenHierarchy.create(source, CssTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();
        
        assertTrue(ts.moveNext());
        Token token = ts.token();
        
        assertEquals(token.id(), CssTokenId.NL);
        assertEquals(ts.offset(), 0);
        assertEquals(token.length(), LINES);
        
        assertFalse(ts.moveNext());
       
    }
    
}
