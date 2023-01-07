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
package org.netbeans.lib.lexer.test.join;

import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestJoinTextTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.lang.TestJoinTopTokenId;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Test embedded sections that should be lexed together.
 *
 * @author Miloslav Metelka
 */
public class JoinSectionsMod1Test extends NbTestCase {
    
    public JoinSectionsMod1Test(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
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

    public void testRemoveContent() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a<x>";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinTopTokenId.language());
        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();

            LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        doc.remove(0, doc.getLength());

        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testCreateEmbedding() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "x{a(y}<z>{)}zc";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinTopTokenId.language());
        final TokenSequence<?> ts;
        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            ts  = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TEXT, "x", -1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.BRACES, "{a(y}", -1);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                ts.createEmbedding(TestPlainTokenId.language(), 1, 1, true);
            }
        });

        ((AbstractDocument)doc).readLock();
        try {
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TAG, "<z>", -1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.BRACES, "{)}", -1);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                ts.createEmbedding(TestPlainTokenId.language(), 1, 1, true);
            }
        });
    }

    public void testCreateEmptyEmbedding() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "{a}x{}y{}";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinTopTokenId.language());
        TokenHierarchy<?> hi;
        final TokenSequence<?> ts;
        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created

            hi = TokenHierarchy.get(doc);
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.BRACES, "{a}", -1);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                ts.createEmbedding(TestPlainTokenId.language(), 1, 1, true);
            }
        });

        ((AbstractDocument)doc).readLock();
        try {
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TEXT, "x", -1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.BRACES, "{}", -1);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                ts.createEmbedding(TestPlainTokenId.language(), 1, 1, true);
            }
        });

        ((AbstractDocument)doc).readLock();
        try {
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TEXT, "y", -1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.BRACES, "{}", -1);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                ts.createEmbedding(TestPlainTokenId.language(), 1, 1, true);
            }
        });
    }

    public void testRemoveFirstSection() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "<a[x y]b><c[z]>";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinTopTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created

            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TAG, "<a[x y]b>", -1);
                TokenSequence<?> ts1 = ts.embedded();
                assertTrue(ts1.moveNext());
                LexerTestUtilities.assertTokenEquals(ts1,TestJoinTextTokenId.TEXT, "a", -1);
                assertTrue(ts1.moveNext());
                LexerTestUtilities.assertTokenEquals(ts1,TestJoinTextTokenId.BRACKETS, "[x y]", -1);
                    TokenSequence<?> ts2 = ts1.embedded();
                    assertTrue(ts2.moveNext());
                    LexerTestUtilities.assertTokenEquals(ts2,TestPlainTokenId.WORD, "x", -1);
                    assertTrue(ts2.moveNext());
                    LexerTestUtilities.assertTokenEquals(ts2,TestPlainTokenId.WHITESPACE, " ", -1);
                    assertTrue(ts2.moveNext());
                    LexerTestUtilities.assertTokenEquals(ts2,TestPlainTokenId.WORD, "y", -1);
                    assertFalse(ts2.moveNext());
                assertTrue(ts1.moveNext());
                LexerTestUtilities.assertTokenEquals(ts1,TestJoinTextTokenId.TEXT, "b", -1);
                assertFalse(ts2.moveNext());

            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TAG, "<c[z]>", -1);
            // Extra ending newline of the document gets lexed too
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TEXT, "\n", -1);
            assertFalse(ts.moveNext());

            LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.remove(0, 9);

        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testShortDocMod() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "xay<b>zc";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinTopTokenId.language());
        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.remove(6, 1);

        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
            //             000000000011111111112222222222
            //             012345678901234567890123456789
            //     text = "xay<b>c";
            //                   \yz<uv>hk
        doc.insertString(6, "yz<uv>hk", null);

        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

            //             000000000011111111112222222222
            //             012345678901234567890123456789
            //     text = "xay<b>yz<uv>hkc";
        doc.remove(12, 3);

        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

            //             000000000011111111112222222222
            //             012345678901234567890123456789
            //     text = "xay<b>yz<uv>";
        doc.insertString(12, "hkc", null);

        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

            //             000000000011111111112222222222
            //             012345678901234567890123456789
            //     text = "xay<b>yz<uv>hkc";
        doc.insertString(7, "{", null);

        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

            //             000000000011111111112222222222
            //             012345678901234567890123456789
            //     text = "xay<b>y{z<uv>hkc";
        doc.insertString(16, "}", null);

        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

            //             000000000011111111112222222222
            //             012345678901234567890123456789
            //     text = "xay<b>y{z<uv>hkc}";
    //        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.FINE); // Extra logging
        doc.insertString(9, "}", null);

        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.incCheck(doc, true);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

            //             000000000011111111112222222222
            //             012345678901234567890123456789
            //     text = "xay<b>y{z}<uv>hkc}";

    }

    public void testJoinSections() throws Exception {
        // Turn on detailed checking
//        Logger.getLogger(TokenHierarchyOperation.class.getName()).setLevel(Level.FINEST);

        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a(b<cd>e)f<gh>i(j<kl>m)n";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class,TestJoinTopTokenId.language());
        LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts;
        List<TokenSequence<?>> tsList;
        LanguagePath innerLP;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TEXT, "a(b", -1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TAG, "<cd>", -1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TEXT, "e)f", -1);

            // Get embedded tokens within TEXT tokens. There should be "a" then BRACES start "{b" then BRACES end "e}|" then "f"
            innerLP = LanguagePath.get(TestJoinTopTokenId.language()).
                    embedded(TestJoinTextTokenId.language);
            tsList = hi.tokenSequenceList(innerLP, 0, Integer.MAX_VALUE);
            checkInitialTokens(tsList);


            // Use iterator for fetching token sequences
            int i = 0;
            for (TokenSequence<?> ts2 : tsList) {
                assertSame(ts2, tsList.get(i++));
            }

            LexerTestUtilities.assertConsistency(hi);

            // Check tokenSequenceList() with explicit offsets
            // Check correct TSs bounds
            tsList = hi.tokenSequenceList(innerLP, 0, 7);
            assertEquals(1, tsList.size());
            tsList = hi.tokenSequenceList(innerLP, 0, 8);
            assertEquals(2, tsList.size());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        
        // Do modifications
        // Remove second closing brace ')'

//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.FINE); // Extra logging
        doc.remove(8, 1);
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.WARNING); // End of extra logging
        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.assertConsistency(hi);
            LexerTestUtilities.incCheck(doc, true);
            //             000000000011111111112222222222
            //             012345678901234567890123456789
            // before:    "a(b<cd>e)f<gh>i(j<kl>m)n";
            // after:     "a(b<cd>ef<gh>i(j<kl>m)n";
            //             i0     i1    i2     i3

            // Check the fired event
            TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
            assertNotNull(evt);
            TokenChange<?> tc = evt.tokenChange();
            assertNotNull(tc);
            // Check top-level TC
            assertEquals(2, tc.index());
            assertEquals(7, tc.offset());
            assertEquals(1, tc.addedTokenCount());
            assertEquals(1, tc.removedTokenCount());
            assertEquals(TestJoinTopTokenId.language(), tc.language());
            assertTrue(tc.isBoundsChange());
            assertEquals(1, tc.embeddedChangeCount());

            // Check top-level TC
            TokenChange<?> tcInner = tc.embeddedChange(0);
            TokenSequence<?> tsAdded = tcInner.currentTokenSequence();
            assertTrue(tsAdded.moveNext());
            LexerTestUtilities.assertTokenEquals(tsAdded,TestJoinTextTokenId.PARENS, "(befi(jm)", 1);
            assertEquals(1, tcInner.index());
            assertEquals(1, tcInner.offset());
            assertEquals(1, tcInner.addedTokenCount());
            assertEquals(3, tcInner.removedTokenCount());
            assertEquals(TestJoinTextTokenId.language, tcInner.language());
            assertEquals(0, tcInner.embeddedChangeCount());

            tsList = hi.tokenSequenceList(innerLP, 0, Integer.MAX_VALUE);
            assertEquals(4, tsList.size()); // 2 sections

            // 1.section "a(b"
            ts = tsList.get(0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.TEXT, "a", -1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.PARENS, "(b", -1);
            Token<?> token = ts.token();
            assertEquals(PartType.START, token.partType());
            assertFalse(ts.moveNext());

            // 2.section "ef"
            ts = tsList.get(1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.PARENS, "ef", -1);
            token = ts.token();
            assertEquals(PartType.MIDDLE, token.partType());
            assertFalse(ts.moveNext());

            // 3.section "i(j"
            ts = tsList.get(2);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.PARENS, "i(j", -1);
            token = ts.token();
            assertEquals(PartType.MIDDLE, token.partType());
            assertFalse(ts.moveNext());

            // 4.section "m)n"
            ts = tsList.get(3);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.PARENS, "m)", -1);
            token = ts.token();
            assertEquals(PartType.END, token.partType());
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.TEXT, "n\n", -1);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        

        // Re-add second closing paren ')'
        doc.insertString(8, ")", null);
        
        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.assertConsistency(hi);
            //             000000000011111111112222222222
            //             012345678901234567890123456789
            // before:    "a(b<cd>ef<gh>i(j<kl>m)n";
            // after:     "a(b<cd>e)f<gh>i(j<kl>m)n";
            tsList = hi.tokenSequenceList(innerLP, 0, Integer.MAX_VALUE);
            checkInitialTokens(tsList);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        doc.remove(0, doc.getLength());

        ((AbstractDocument)doc).readLock();
        try {
            LexerTestUtilities.assertConsistency(hi);
            ts = hi.tokenSequence();
            // Extra newline contained in DocumentUtilities.getText(doc) that gets lexed
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TEXT, "\n", -1);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.insertString(0, text, null);
        LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        
    }

    private void checkInitialTokens(List<TokenSequence<?>> tsList) {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        // text:      "a(b<cd>e)f<gh>i(j<kl>m)n";
        assertEquals(4, tsList.size()); // 4 sections

        // 1.section
        TokenSequence<?> ts = tsList.get(0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.TEXT, "a", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.PARENS, "(b", -1);
        Token<?> token = ts.token();
        assertEquals(PartType.START, token.partType());
        assertFalse(ts.moveNext());
        
        // 2.section
        ts = tsList.get(1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.PARENS, "e)", -1);
        token = ts.token();
        assertEquals(PartType.END, token.partType());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.TEXT, "f", -1);
        assertFalse(ts.moveNext());
        
        // 3.section
        ts = tsList.get(2);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.TEXT, "i", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.PARENS, "(j", -1);
        token = ts.token();
        assertEquals(PartType.START, token.partType());
        assertFalse(ts.moveNext());
        
        // 4.section
        ts = tsList.get(3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.PARENS, "m)", -1);
        token = ts.token();
        assertEquals(PartType.END, token.partType());
        assertTrue(ts.moveNext());
        // Includes extra ending newline
        LexerTestUtilities.assertTokenEquals(ts,TestJoinTextTokenId.TEXT, "n\n", -1);
        assertFalse(ts.moveNext());
    }

}
