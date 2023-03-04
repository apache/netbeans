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

package org.netbeans.lib.lexer.test.inc;

import java.io.PrintStream;
import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.lang.TestTokenId;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class TokenListUpdaterTest extends NbTestCase {
    
    public TokenListUpdaterTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

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
//        return super.logLevel();
    }

    public void testInsertUnfinishedLexing() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "abc+uv-xy";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);
        TokenSequence<?> ts;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Modify before last token
        doc.insertString(3, "x", null);
        ((AbstractDocument)doc).readLock();
        try {
            ts.moveNext();
            fail("Should not get there");
        } catch (ConcurrentModificationException e) {
            // Expected
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abcx", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 4);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "uv", 5);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 7);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "xy", 8);
            // Extra newline of the document is returned by DocumentUtilities.getText(doc) and lexed
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, "\n", 10);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testRemoveUnfinishedLexingZeroLookaheadToken() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "a+b";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        TokenSequence<?> ts;
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 1);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Remove "+"
        doc.remove(1, 1);
        ((AbstractDocument)doc).readLock();
        try {
            ts.moveNext();
            fail("Should not get there");
        } catch (ConcurrentModificationException e) {
            // Expected
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "ab", 0);
            // Extra ending '\n' of the document returned by DocumentUtilities.getText(doc) and lexed
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, "\n", 2);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testRemoveUnfinishedLexingRightAfterLastToken() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "a+b";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Remove "+"
        doc.remove(1, 1);
        ((AbstractDocument)doc).readLock();
        try {
            ts.moveNext();
            fail("Should not get there");
        } catch (ConcurrentModificationException e) {
            // Expected
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "ab", 0);
            // Extra ending '\n' of the document returned by DocumentUtilities.getText(doc) and lexed
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, "\n", 2);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testRemoveUnfinishedLexingAfterLastToken() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "a+b+";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Remove "b"
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.FINE); // Extra logging
        doc.remove(2, 1);
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.WARNING); // End of extra logging
        ((AbstractDocument)doc).readLock();
        try {
            ts.moveNext();
            fail("Should not get there");
        } catch (ConcurrentModificationException e) {
            // Expected
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            CharSequence tokenText = ts.token().text();
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 2);
            // Extra ending '\n' of the document returned by DocumentUtilities.getText(doc) and lexed
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, "\n", 3);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testReadAllInsertAtEnd() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "a+";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 1);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Insert "-"
        doc.insertString(2, "-", null);
        ((AbstractDocument)doc).readLock();
        try {
            ts.moveNext();
            fail("Should not get there");
        } catch (ConcurrentModificationException e) {
            // Expected
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 2);
            // Extra ending '\n' of the document returned by DocumentUtilities.getText(doc) and lexed
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, "\n", 3);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testReadOneInsertAtEnd() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "a+";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Insert "-"
        doc.insertString(2, "-", null);
        ((AbstractDocument)doc).readLock();
        try {
            ts.moveNext();
            fail("Should not get there");
        } catch (ConcurrentModificationException e) {
            // Expected
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 2);
            // Extra ending '\n' of the document returned by DocumentUtilities.getText(doc) and lexed
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, "\n", 3);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testReadNoneInsertAtEnd() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "a+";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        
        // Insert "-"
        doc.insertString(2, "-", null);

        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 2);
            // Extra ending '\n' of the document returned by DocumentUtilities.getText(doc) and lexed
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, "\n", 3);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

}
