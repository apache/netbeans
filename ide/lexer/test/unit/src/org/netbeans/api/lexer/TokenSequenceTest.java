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

package org.netbeans.api.lexer;

import org.netbeans.lib.lexer.lang.TestTokenId;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.WrapTokenIdCache;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.token.DefaultToken;
import org.netbeans.lib.lexer.token.TextToken;

/**
 * Test methods of token sequence.
 *
 * @author mmetelka
 */
public class TokenSequenceTest extends NbTestCase {
    
    public TokenSequenceTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
    public void testMove() {
        String text = "abc+defg";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,TestTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        assertNull(ts.token());

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);

        // Check movePrevious()
        assertFalse(ts.movePrevious()); // cannot go below first token
        
        assertTrue(ts.moveNext()); // on "+"
        assertTrue(ts.moveNext()); // on "defg"
        assertFalse(ts.moveNext());
        
        assertEquals(ts.tokenCount(), 3);
        
        assertEquals(0, ts.move(0));
        assertNull(ts.token());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        assertEquals(2, ts.move(2));
        assertNull(ts.token());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        assertEquals(-1, ts.move(-1));
        assertNull(ts.token());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        assertEquals(0, ts.move(3));
        assertNull(ts.token());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
        assertEquals(0, ts.move(4));
        assertNull(ts.token());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "defg", 4);
        assertEquals(1, ts.move(5));
        assertNull(ts.token());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "defg", 4);
        assertEquals(1, ts.move(9));
        assertNull(ts.token());
        assertFalse(ts.moveNext());
        assertTrue(ts.movePrevious());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "defg", 4);
        assertEquals(92, ts.move(100));
        assertNull(ts.token());
        assertFalse(ts.moveNext());
        assertTrue(ts.movePrevious());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "defg", 4);

        
        // Test subsequences
        TokenSequence<?> sub = ts.subSequence(1, 6);
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.IDENTIFIER, "abc", 0);
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.PLUS, "+", 3);
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.IDENTIFIER, "defg", 4);
        
        sub = ts.subSequence(1, 6);
        assertEquals(2, sub.move(6));

        sub = ts.subSequence(3);
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.PLUS, "+", 3);
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.IDENTIFIER, "defg", 4);

        sub = ts.subSequence(3, 3);
        assertFalse(sub.moveNext());
        
        sub = ts.subSequence(2, 2);
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.IDENTIFIER, "abc", 0);
        assertFalse(sub.moveNext());

        sub = ts.subSequence(7, 7);
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.IDENTIFIER, "defg", 4);
        assertFalse(sub.moveNext());

        sub = ts.subSequence(8, 8);
        assertFalse(sub.moveNext());
    }
    
    public void testMoveNextPrevious() {
        String text = "abc+defg-";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,TestTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertEquals(0, ts.move(4));
        assertNull(ts.token());
        //assertEquals(4, ts.offset());
        assertTrue(ts.movePrevious());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
        
        ts = hi.tokenSequence();
        ts.move(5);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "defg", 4);
        assertTrue(ts.movePrevious());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);

        // Move single char before token's end
        ts = hi.tokenSequence();
        ts.move(7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "defg", 4);
        assertTrue(ts.movePrevious());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
    
        // Move past all tokens
        ts = hi.tokenSequence();
        ts.move(text.length());
        assertTrue(ts.movePrevious());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", text.length() - 1);

        // Move at the begining
        ts = hi.tokenSequence();
        ts.move(0);
        assertFalse(ts.movePrevious());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
    }
    
    public void testMoveIndex() {
        String text = "abc+defg-";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,TestTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertEquals(0, ts.index());
        assertNull(ts.token());
        //assertEquals(4, ts.offset());
        assertTrue(ts.moveNext());
        assertEquals(0, ts.offset());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
        assertEquals(3, ts.offset());
        ts.move(2);
        assertEquals(0, ts.index());
        assertTrue(ts.moveNext());
        assertEquals(0, ts.offset());
        assertEquals(0, ts.index());
        ts.moveIndex(1);
        assertEquals(1, ts.index());
        assertTrue(ts.moveNext());
        assertEquals(1, ts.index());
        assertEquals(3, ts.offset());
    }

    public void testMoveSkipTokens() {
        String text = "-abc+defg--hi";
        Set<TestTokenId> skipTokenIds = 
                EnumSet.of(TestTokenId.PLUS,TestTokenId.MINUS);
        
        assertTrue(skipTokenIds.contains(TestTokenId.PLUS));
        assertTrue(skipTokenIds.contains(TestTokenId.MINUS));
        assertFalse(skipTokenIds.contains(TestTokenId.IDENTIFIER));

        TokenHierarchy<?> hi = TokenHierarchy.create(text, false,TestTokenId.language(), skipTokenIds, null);
        TokenSequence<?> ts = hi.tokenSequence();
        
        // Fail if no "move*" method called yet
        assertNull(ts.token());

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "defg", 5);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "hi", 11);
        assertFalse(ts.moveNext());
        assertEquals(3, ts.tokenCount());
        assertEquals(0, ts.moveIndex(0));
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 1);
        assertTrue(ts.moveNext());
        assertEquals(0, ts.moveIndex(ts.tokenCount()));
        assertTrue(ts.movePrevious());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "hi", 11);
        assertFalse(ts.moveNext());

        // Check movePrevious()
        assertTrue(ts.movePrevious()); // over "defg"
        assertTrue(ts.movePrevious()); // over "abc"
        assertFalse(ts.movePrevious()); // cannot go below first token
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 1);
        
        assertEquals(-1, ts.move(0)); // below filtered-out token
        assertNull(ts.token());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 1);
        assertEquals(0, ts.move(1));
        assertNull(ts.token());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 1);
        assertEquals(1, ts.move(2));
        assertNull(ts.token());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 1);
        assertEquals(3, ts.move(4));
        assertNull(ts.token());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 1);
        assertEquals(0, ts.move(5));
        assertNull(ts.token());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "defg", 5);
        assertEquals(1, ts.move(12));
        assertNull(ts.token());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "hi", 11);
        
        
        // Test subsequences
        TokenSequence<?> sub = ts.subSequence(1, 6);
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.IDENTIFIER, "abc", 1);
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.IDENTIFIER, "defg", 5);
        assertFalse(sub.moveNext());
        
        // Test moves to first and last tokens on subsequence
        sub = ts.subSequence(1, 6);
        assertEquals(0, sub.moveIndex(sub.tokenCount()));
        assertEquals(2, sub.tokenCount()); // "abc" and "defg" (others filtered out
        assertTrue(sub.movePrevious());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.IDENTIFIER, "defg", 5);
        assertEquals(0, sub.moveIndex(0));
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.IDENTIFIER, "abc", 1);
        
        sub = ts.subSequence(4);
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.IDENTIFIER, "defg", 5);
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.IDENTIFIER, "hi", 11);
        assertFalse(sub.moveNext());

        sub = ts.subSequence(12, 15);
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.IDENTIFIER, "hi", 11);

        sub = ts.subSequence(12, 15);
        assertEquals(-2, sub.move(9));
        assertNull(sub.token());
        assertTrue(sub.moveNext());
        LexerTestUtilities.assertTokenEquals(sub,TestTokenId.IDENTIFIER, "hi", 11);

        sub = ts.subSequence(13, 15);
        assertFalse(sub.moveNext());
        assertEquals(0, sub.moveIndex(0));
        assertNull(sub.token());
        assertFalse(sub.moveNext());
        assertEquals(0, sub.moveIndex(sub.tokenCount()));
        assertNull(sub.token());
        assertEquals(0, sub.tokenCount());

        sub = ts.subSequence(-3, 1);
        assertFalse(sub.moveNext());

        sub = ts.subSequence(13, 15);
        assertEquals(5, sub.move(5));

        sub = ts.subSequence(-3, 1);
        assertEquals(1, sub.move(1));

        // Reversed bounds => should be empty token sequence
        sub = ts.subSequence(6, 1);
        assertFalse(sub.moveNext());
        sub = ts.subSequence(6, 1);
        assertEquals(6, sub.move(6));
    }

    public void DtestMoveSkipTokens2() throws IOException {
        String text = "abc+defg--hi";
        Set<TestTokenId> skipTokenIds =
                EnumSet.of(TestTokenId.IDENTIFIER);
        
        assertFalse(skipTokenIds.contains(TestTokenId.PLUS));
        assertFalse(skipTokenIds.contains(TestTokenId.MINUS));
        assertTrue(skipTokenIds.contains(TestTokenId.IDENTIFIER));
        
        Reader r = new StringReader(text);
        
        try {
            TokenHierarchy<?> hi = TokenHierarchy.create(r,TestTokenId.language(), skipTokenIds, null);
            TokenSequence<?> ts = hi.tokenSequence();
            
            ts.tokenCount();
        } finally {
            r.close();
        }
    }
    
    public void testMoveEmpty() {
        String text = "";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,TestTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();

        // Expect no tokens
        assertFalse(ts.moveNext());
        assertNull(ts.token());
        
        assertEquals(0, ts.move(0));
        assertEquals(10, ts.move(10));
        
        // Test subsequences
        TokenSequence<?> sub = ts.subSequence(1, 6);
        assertFalse(sub.moveNext());
        sub = ts.subSequence(1, 6);
        assertEquals(1, sub.move(1));
        
        sub = ts.subSequence(0);
        assertFalse(sub.moveNext());
        sub = ts.subSequence(0);
        assertEquals(0, sub.move(0));

        sub = ts.subSequence(1);
        assertFalse(sub.moveNext());
        sub = ts.subSequence(1);
        assertEquals(0, sub.move(0));
    }
    
    public void testTokenSize() {
        String text = "abc+def";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,TestTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "def", 4);
        assertFalse(ts.moveNext());
        
        TokenList tokenList = LexerTestUtilities.tokenList(ts);
        ts.moveIndex(0); // move before "abc"
        assertTrue(ts.moveNext());
        // Test DefaultToken size
        Token<?> token = ts.token();
        // Exclude TokenLength since it should be cached - verify later
        assertSame(DefaultToken.class, token.getClass());
        WrapTokenIdCache<TestTokenId> cache = WrapTokenIdCache.get(TestTokenId.language());
        assertSize("Token instance too big", Collections.singletonList(token), 24,
                new Object[] { tokenList, TestTokenId.IDENTIFIER, cache.plainWid(TestTokenId.IDENTIFIER) });

        // Test TextToken size
        assertTrue(ts.moveNext());
        token = ts.token();
        assertSame(TextToken.class, token.getClass());
        assertSize("Token instance too big", Collections.singletonList(token), 24,
                new Object[] { tokenList, TestTokenId.PLUS, cache.plainWid(TestTokenId.PLUS), "+" });

        // Test DefaultToken size
        assertTrue(ts.moveNext());
        token = ts.token();
        assertSame(DefaultToken.class, token.getClass());
    }

    public void testSubSequenceInUnfinishedTH() throws Exception {
        Document doc = new ModificationTextDocument();
        //             012345678
        String text = "ab cd efg";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);

        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());

            ts = ts.subSequence(2, 6);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 2);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "cd", 3);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 5);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testSubSequenceInvalidOffset137564() throws Exception {
        Document doc = new ModificationTextDocument();
        //             012345678
        String text = "ab cd efg";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());

            ts = ts.subSequence(-2, -1);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

}
