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
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
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
public class TokenHierarchyEventTest extends NbTestCase {
    
    public TokenHierarchyEventTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testCreateEmbedding() throws Exception {
        Document doc = new ModificationTextDocument();
        String text = "abc def ghi";
        doc.insertString(0, text, null);
        // Assign a language to the document
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();

            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 3);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "def", 4);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 7);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "ghi", 8);
            // Extra newline at doc's end is contained in the DocumentUtilities.getText(doc)
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, "\n", 11);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Do insert
        doc.insertString(5, "x", null);
        
        ((AbstractDocument)doc).readLock();
        try {
            // Check the fired event
            TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
            assertNotNull(evt);
            TokenChange<?> tc = evt.tokenChange();
            assertNotNull(tc);
            assertEquals(2, tc.index());
            assertEquals(4, tc.offset());

            assertEquals(1, tc.removedTokenCount());
            TokenSequence<?> removedTS = tc.removedTokenSequence();
            assertTrue(removedTS.moveNext());
            LexerTestUtilities.assertTokenEquals(removedTS, TestTokenId.IDENTIFIER, 3, 4);

            assertEquals(1, tc.addedTokenCount());
            TokenSequence<?> currentTS = tc.currentTokenSequence();
            currentTS.moveIndex(tc.index());
            assertTrue(currentTS.moveNext());
            LexerTestUtilities.assertTokenEquals(currentTS, TestTokenId.IDENTIFIER, "dxef", 4);

            assertEquals(TestTokenId.language(), tc.language());
            assertEquals(0, tc.embeddedChangeCount());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
    }
    
}
