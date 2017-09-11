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

package org.netbeans.lib.lexer.test.simple;

import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class SimpleLexerBatchTest extends TestCase {

    public SimpleLexerBatchTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void test() {
        String commentText = "/* test comment  */";
        String text = "abc+ " + commentText + "def public publica publi static x";
        int commentTextStartOffset = 5;
        TokenHierarchy<?> hi = TokenHierarchy.create(text,TestTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 4);
        assertTrue(ts.moveNext());
        int offset = commentTextStartOffset;
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.BLOCK_COMMENT, commentText, offset);
        offset += commentText.length();
        int commentIndex = ts.index();

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "def", offset);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PUBLIC, "public", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "publica", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "publi", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.STATIC, "static", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "x", -1);
        assertFalse(ts.moveNext());

        // Go back to block comment
        assertEquals(0, ts.moveIndex(commentIndex));
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.BLOCK_COMMENT, commentText, commentTextStartOffset);

        // Test embedded token sequence
        TokenSequence<?> embedded = ts.embedded();
        assertNotNull("Null embedded sequence", embedded);
        assertTrue(embedded.moveNext());
        offset = commentTextStartOffset + 2; // skip "/*"
        LexerTestUtilities.assertTokenEquals(embedded,TestPlainTokenId.WHITESPACE, " ", offset);
        offset += 1;
        assertTrue(embedded.moveNext());
        LexerTestUtilities.assertTokenEquals(embedded,TestPlainTokenId.WORD, "test", offset);
        offset += 4;
        assertTrue(embedded.moveNext());
        LexerTestUtilities.assertTokenEquals(embedded,TestPlainTokenId.WHITESPACE, " ", offset);
        offset += 1;
        assertTrue(embedded.moveNext());
        LexerTestUtilities.assertTokenEquals(embedded,TestPlainTokenId.WORD, "comment", offset);
        offset += 7;
        assertTrue(embedded.moveNext());
        LexerTestUtilities.assertTokenEquals(embedded,TestPlainTokenId.WHITESPACE, "  ", offset);
        assertFalse(embedded.moveNext());

    }
    
    public void testPerf() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7000; i++) {
            sb.append("public static x + y /* test comment */ abc * def\n");
        }
        String text = sb.toString();

        long tm;
        Language<TestTokenId> language = TestTokenId.language();
        tm = System.currentTimeMillis();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language);
        tm = System.currentTimeMillis() - tm;
        assertTrue("Timeout tm = " + tm + "msec", tm < 100); // Should be fast
        
        tm = System.currentTimeMillis();
        TokenSequence<?> ts = hi.tokenSequence();
        tm = System.currentTimeMillis() - tm;
        assertTrue("Timeout tm = " + tm + "msec", tm < 100); // Should be fast
        
        // Fetch 2 initial tokens - should be lexed lazily
        tm = System.currentTimeMillis();
        ts.moveNext();
        ts.token();
        ts.moveNext();
        ts.token();
        tm = System.currentTimeMillis() - tm;
        assertTrue("Timeout tm = " + tm + "msec", tm < 100); // Should be fast
        
        tm = System.currentTimeMillis();
        ts.moveIndex(0);
        int cntr = 1; // On the first token
        while (ts.moveNext()) {
            Token t = ts.token();
            cntr++;
        }
        tm = System.currentTimeMillis() - tm;
        assertTrue("Timeout tm = " + tm + "msec", tm < 1000); // Should be fast
        System.out.println("SimpleLexerBatchTest.testPerf(): Lexed input " + text.length()
                + " chars long and created " + cntr + " tokens in " + tm + " ms.");
    }
    
}
