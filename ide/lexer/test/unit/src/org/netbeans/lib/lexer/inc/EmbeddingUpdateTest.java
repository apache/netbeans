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

package org.netbeans.lib.lexer.inc;

import javax.swing.text.AbstractDocument;
import org.netbeans.lib.lexer.lang.TestTokenId;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.test.simple.*;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.spi.lexer.MutableTextInput;
import org.netbeans.spi.lexer.TokenHierarchyControl;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class EmbeddingUpdateTest extends NbTestCase {
    
    public EmbeddingUpdateTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testEmbeddingUpdate() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,TestTokenId.language());
        doc.insertString(0, "a/*abc def*/", null);
        LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.BLOCK_COMMENT, "/*abc def*/", 1);
            TokenSequence<?> ets = ts.embedded();
            assertNotNull(ets);
            assertTrue(ts.moveNext());
            assertTrue(ets.moveNext());
            LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WORD, "abc", 3);
            assertTrue(ets.moveNext());
            LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WHITESPACE, " ", 6);
            assertTrue(ets.moveNext());
            LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WORD, "def", 7);
            assertFalse(ets.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        // Make "axbc" inside the comment
        doc.insertString(4, "x", null);
        
        ((AbstractDocument)doc).readLock();
        try {
            TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
            assertNotNull(evt);
            TokenChange<?> tc = evt.tokenChange();
            assertNotNull(tc);
            assertEquals(1, tc.index());
            assertEquals(1, tc.offset());
            assertEquals(1, tc.addedTokenCount());
            assertEquals(1, tc.removedTokenCount());
            assertEquals(TestTokenId.language(), tc.language());
            assertEquals(1, tc.embeddedChangeCount());
            TokenChange<?> etc = tc.embeddedChange(0);
            assertEquals(0, etc.index());
            assertEquals(3, etc.offset());
            assertEquals(1, etc.addedTokenCount()); // 0 to allow for lazy lexing where this would be unknowns
            assertEquals(1, etc.removedTokenCount());
            assertEquals(TestPlainTokenId.language(), etc.language());
            assertEquals(0, etc.embeddedChangeCount());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        doc.remove(3, 8); // there will be empty /**/ so test empty embedded sequence
        doc.insertString(3, "x", null); // there will be empty /**/
    }        
        
    public void testEmbeddingActivityChange() throws Exception {
        ModificationTextDocument doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,TestTokenId.language());
        doc.insertString(0, "a/*abc def*/", null);
        LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.BLOCK_COMMENT, "/*abc def*/", 1);
            TokenSequence<?> ets = ts.embedded();
            assertNotNull(ets);
            assertTrue(ts.moveNext());
            assertTrue(ets.moveNext());
            LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WORD, "abc", 3);
            assertTrue(ets.moveNext());
            LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WHITESPACE, " ", 6);
            assertTrue(ets.moveNext());
            LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WORD, "def", 7);
            assertFalse(ets.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        MutableTextInput input = (MutableTextInput) doc.getProperty(MutableTextInput.class);
        final TokenHierarchyControl control = input.tokenHierarchyControl();
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                control.setActive(false);
                control.setActive(true);
            }
        });

    }

}
