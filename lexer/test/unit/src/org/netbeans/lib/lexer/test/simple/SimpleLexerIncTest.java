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

import java.io.PrintStream;
import java.util.logging.Level;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Token;
import org.netbeans.lib.lexer.lang.TestTokenId;
import java.util.ConcurrentModificationException;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class SimpleLexerIncTest extends NbTestCase {
    
    public SimpleLexerIncTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    @Override
    public PrintStream getLog() {
        return System.out;
//        return super.getLog();
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
//        return super.logLevel();;
    }

    public void test() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,TestTokenId.language());
        LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);

        // Check insertion of text that produces token with LA=0
        doc.insertString(0, "+", null);
        LexerTestUtilities.incCheck(doc, false);
        doc.remove(0, doc.getLength());
        LexerTestUtilities.incCheck(doc, false);
        
        TokenSequence<?> ts;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, TestTokenId.WHITESPACE, "\n", 0);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Insert text into document
        String commentText = "/* test comment  */";
        //             0123456789
        String text = "abc+uv-xy +-+";
        int commentStartOffset = text.length();
        text += commentText + "def";
        doc.insertString(0, text, null);

        ((AbstractDocument)doc).readLock();
        // Last token sequence should throw exception - new must be obtained
        try {
            ts.moveNext();
            fail("TokenSequence.moveNext() did not throw exception as expected.");
        } catch (ConcurrentModificationException e) {
            // Expected exception
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
            assertEquals(1, LexerTestUtilities.lookahead(ts));
            assertEquals(null, LexerTestUtilities.state(ts));
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
            assertEquals(1, LexerTestUtilities.lookahead(ts)); // la=1 because may be "+-+"
            assertEquals(null, LexerTestUtilities.state(ts));
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "uv", 4);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 6);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "xy", 7);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 9);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS_MINUS_PLUS, "+-+", 10);
            assertEquals(0, LexerTestUtilities.lookahead(ts));
            assertEquals(null, LexerTestUtilities.state(ts));
            assertTrue(ts.moveNext());
            int offset = commentStartOffset;
            int commentIndex = ts.index();
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.BLOCK_COMMENT, commentText, offset);
            offset += commentText.length();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "def", offset);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, "\n", offset + 3);
            assertFalse(ts.moveNext());
            LexerTestUtilities.incCheck(doc, false);

            // Check TokenSequence.move()
            int relOffset = ts.move(50); // past the end of all tokens
            assertEquals(relOffset, 50 - (offset + 4));
            assertTrue(ts.movePrevious());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, "\n", offset + 3);

            relOffset = ts.move(6); // right at begining of "-"
            assertEquals(relOffset, 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 6);

            relOffset = ts.move(-5); // to first token "abc"
            assertEquals(relOffset, -5);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);

            relOffset = ts.move(5); // to "uv"
            assertEquals(relOffset, 1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "uv", 4);

            // Check embedded sequence
            ts.moveIndex(commentIndex);
            ts.moveNext();
            TokenSequence<?> embedded = ts.embedded();
            assertNotNull("Null embedded sequence", embedded);
            assertTrue(embedded.moveNext());
            int commentOffset = commentStartOffset + 2; // skip "/*"
            LexerTestUtilities.assertTokenEquals(embedded,TestPlainTokenId.WHITESPACE, " ", commentOffset);
            commentOffset += 1;
            assertTrue(embedded.moveNext());
            LexerTestUtilities.assertTokenEquals(embedded,TestPlainTokenId.WORD, "test", commentOffset);
            commentOffset += 4;
            assertTrue(embedded.moveNext());
            LexerTestUtilities.assertTokenEquals(embedded,TestPlainTokenId.WHITESPACE, " ", commentOffset);
            commentOffset += 1;
            assertTrue(embedded.moveNext());
            LexerTestUtilities.assertTokenEquals(embedded,TestPlainTokenId.WORD, "comment", commentOffset);
            commentOffset += 7;
            assertTrue(embedded.moveNext());
            LexerTestUtilities.assertTokenEquals(embedded,TestPlainTokenId.WHITESPACE, "  ", commentOffset);
            assertFalse(embedded.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        // Modify comment
        doc.insertString(commentStartOffset + 4, "r", null);

        ((AbstractDocument)doc).readLock();
        try {
            TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
            assertNotNull(evt);
            TokenChange<?> tc = evt.tokenChange();
            assertNotNull(tc);
            // Check top-level TC
            assertEquals(7, tc.index());
            assertEquals(13, tc.offset());
            assertEquals(1, tc.addedTokenCount());
            assertEquals(1, tc.removedTokenCount());
            assertEquals(TestTokenId.language(), tc.language());
            assertTrue(tc.isBoundsChange());
            assertEquals(1, tc.embeddedChangeCount());
            // Check added token
            TokenSequence<?> tsAdded = tc.currentTokenSequence();
            assertTrue(tsAdded.moveNext());
            LexerTestUtilities.assertTokenEquals(tsAdded,TestTokenId.BLOCK_COMMENT, "/* trest comment  */", commentStartOffset);

            // Check inner token change
            assertEquals(1, tc.embeddedChangeCount());
            TokenChange<?> tcInner = tc.embeddedChange(0);
            assertNotNull(tcInner);
            // Check top-level TC
            assertEquals(1, tcInner.index());
            assertEquals(16, tcInner.offset());
            assertEquals(1, tcInner.addedTokenCount());
            assertEquals(1, tcInner.removedTokenCount());
            assertEquals(TestPlainTokenId.language(), tcInner.language());
            assertTrue(tcInner.isBoundsChange());
            assertEquals(0, tcInner.embeddedChangeCount());
            // Check added token
            tsAdded = tcInner.currentTokenSequence();
            assertTrue(tsAdded.moveNext());
            LexerTestUtilities.assertTokenEquals(tsAdded,TestPlainTokenId.WORD, "trest", commentStartOffset + 3);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.insertString(2, "d", null); // should be "abdc"

        ((AbstractDocument)doc).readLock();
        try {
        // Last token sequence should throw exception - new must be obtained
            ts.moveNext();
            fail("TokenSequence.moveNext() did not throw exception as expected.");
        } catch (ConcurrentModificationException e) {
            // Expected exception
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        LexerTestUtilities.incCheck(doc, false);
        
        
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abdc", 0);
            LexerTestUtilities.incCheck(doc, false);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Remove added 'd' to become "abc" again
        doc.remove(2, 1); // should be "abc" again
        LexerTestUtilities.incCheck(doc, false);
        

        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
            LexerTestUtilities.incCheck(doc, false);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        
        // Now insert right at the end of first token - identifier with lookahead 1
        doc.insertString(3, "x", null); // should become "abcx"
        LexerTestUtilities.incCheck(doc, false);
        
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abcx", 0);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.remove(3, 1); // return back to "abc"
        LexerTestUtilities.incCheck(doc, false);

        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        
        // Now insert right at the end of "+" token - operator with lookahead 1 (because of "+-+" operator)
        doc.insertString(4, "z", null); // should become "abc" "+" "zuv"
        LexerTestUtilities.incCheck(doc, false);
        
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "zuv", 4);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 7);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        doc.remove(4, 1); // return back to "abc" "+" "uv"
        LexerTestUtilities.incCheck(doc, false);

        // Now insert right after "-" - operator with lookahead 0
        doc.insertString(7, "z", null);
        LexerTestUtilities.incCheck(doc, false);
        
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "uv", 4);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 6);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "zxy", 7);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.remove(7, 1); // return back to "abc" "+" "uv"
        LexerTestUtilities.incCheck(doc, false);

        // Now insert between "+-" and "+" in "+-+" - operator with lookahead 0
        doc.insertString(12, "z", null);
        LexerTestUtilities.incCheck(doc, false);
        doc.remove(12, 1);
        LexerTestUtilities.incCheck(doc, false);

        // Now insert "-" at the end of the document
        doc.insertString(doc.getLength(), "-", null);
        LexerTestUtilities.incCheck(doc, false);
        // Insert again "-" at the end of the document (now lookahead of preceding is zero)
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.FINE); // Extra logging
        doc.insertString(doc.getLength(), "-", null);
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.WARNING); // End of extra logging
        LexerTestUtilities.incCheck(doc, false);
        // Insert again "+-+" at the end of the document (now lookahead of preceding is zero)
        doc.insertString(doc.getLength(), "+-+", null);
        LexerTestUtilities.incCheck(doc, false);
        // Remove the ending "+" so that "+-+" becomes "+" "-"
        doc.remove(doc.getLength() - 1, 1);
        LexerTestUtilities.incCheck(doc, false);
        
        doc.insertString(5, "a+b-c", null); // multiple tokens
        LexerTestUtilities.incCheck(doc, false);
        doc.insertString(7, "-++", null); // complete PLUS_MINUS_PLUS
        LexerTestUtilities.incCheck(doc, false);
    }


    public void testTokenToString() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);
        CharSequence tokenText;

        // Check insertion of text that produces token with LA=0
        String id1 = "abcdefghij";
        String id2 = "abcdefghijklmnopqrstuvwxyz0123456789";
        assert (id2.length() > 30); // DefaultToken.INPUT_SOURCE_SUBSEQUENCE_THRESHOLD
        doc.insertString(0, "+" + id1 + "+" + id2 + "+", null);
//        LexerTestUtilities.incCheck(doc, false);

        TokenSequence<?> ts;
        int offset;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            offset = 0;
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, TestTokenId.PLUS, "+", offset);
            offset++;
            assertTrue(ts.moveNext());
    //        LexerTestUtilities.assertTokenEquals(ts, TestTokenId.IDENTIFIER, id1, offset);
    //        checkTokenText(ts.token(), id1);
            tokenText = ts.token().text();
            assert (!(tokenText instanceof String)) : "Should not be a String here"; // beware of e.g. token.toString() call during debugging
            checkText(tokenText, id1, false);
            offset += id1.length();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, TestTokenId.PLUS, "+", offset);
            offset++;
            assertTrue(ts.moveNext());
    //        LexerTestUtilities.assertTokenEquals(ts, TestTokenId.IDENTIFIER, id2, offset);
    //        checkTokenText(ts.token(), id2);
            Token token = ts.token();
            tokenText = token.text();
            assert (!(tokenText instanceof String)) : "Should not be a String here"; // beware of e.g. token.toString() call during debugging
    //        checkText(tokenText, id2, false);
            offset += id2.length();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, TestTokenId.PLUS, "+", offset);
            offset++;
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, TestTokenId.WHITESPACE, "\n", offset);
            offset++;
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        

        doc.insertString(0, id1, null); // Insert extra chars

        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            offset = 0;
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, TestTokenId.IDENTIFIER, id1, 0);
            offset += id1.length();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, TestTokenId.PLUS, "+", offset);
            offset++;
            assertTrue(ts.moveNext());
    //        LexerTestUtilities.assertTokenEquals(ts, TestTokenId.IDENTIFIER, id1, offset);
            checkTokenText(ts.token(), id1);
    //        tokenText = ts.token().text();
    //        assert (!(tokenText instanceof String)) : "Should not be a String here"; // beware of e.g. token.toString() call during debugging
    //        checkText(tokenText, id1, false);
            offset += id1.length();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, TestTokenId.PLUS, "+", offset);
            offset++;
            assertTrue(ts.moveNext());
    //        LexerTestUtilities.assertTokenEquals(ts, TestTokenId.IDENTIFIER, id2, offset);
            checkTokenText(ts.token(), id2);
    //        tokenText = ts.token().text();
    //        assert (!(tokenText instanceof String)) : "Should not be a String here"; // beware of e.g. token.toString() call during debugging
    //        checkText(tokenText, id2, false);
            offset += id2.length();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, TestTokenId.PLUS, "+", offset);
            offset++;
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, TestTokenId.WHITESPACE, "\n", offset);
            offset++;
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }
    
    private static void checkTokenText(Token token, String expectedText) {
        for (int i = 0; i < 10; i++) { // Reach token text caching
            CharSequence tokenText = token.text();
            checkText(tokenText, expectedText, true);
//            tokenText = token.text(); // Debug current state of text()
            checkText(tokenText.toString(), expectedText, false); // Check tokenText.toString()
        }
    }

    private static void checkText(CharSequence tokenText, String expectedText, boolean checkSub) {
        int len = tokenText.length();
        TestCase.assertEquals("Token text length", expectedText.length(), len);
        for (int j = 0; j < len; j++) {
            TestCase.assertEquals("tokenText.charAt(" + j + ")", expectedText.charAt(j), tokenText.charAt(j));
            if (checkSub) {
                checkText(tokenText.subSequence(0, j), expectedText.substring(0, j), false);
                checkText(tokenText.subSequence(j, len), expectedText.substring(j, len), false);
            }
        }
    }
    
}
