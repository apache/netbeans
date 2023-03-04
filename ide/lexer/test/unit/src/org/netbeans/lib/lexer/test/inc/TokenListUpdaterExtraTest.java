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

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.lang.TestSaveTokensInLATokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Tests of token list behavior in certain special situations.
 *
 * @author mmetelka
 */
public class TokenListUpdaterExtraTest extends TestCase {
    
    public TokenListUpdaterExtraTest(String testName) {
        super(testName);
    }

    public void testSaveTokensWithinLookahead() throws Exception {
        Document doc = new ModificationTextDocument();
        String text = "aabc";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class, TestSaveTokensInLATokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            ts.moveEnd(); // Force creation of all tokens
            LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
            
        doc.remove(1, 1);

        ((AbstractDocument)doc).readLock();
        try {
            TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
            TokenChange<?> change = evt.tokenChange();
            assertEquals(1, change.addedTokenCount());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
    }

    public void testEmbeddedModInStartSkipLength() throws Exception {
        Document doc = commentDoc();
        doc.insertString(1, "*", null);
    }
    
    public void testEmbeddedModInEndSkipLength()  throws Exception {
        Document doc = commentDoc();
        doc.insertString(7, "*", null);
    }
    
    private Document commentDoc() throws BadLocationException {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "/**abc*/";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            assertNotNull(ts.embedded());
            return doc;
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testTokenCountWasCalledInUpdater() throws Exception {
        Document doc = new ModificationTextDocument();
        String text = "+/* */";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class, TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", -1);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.remove(1, 3); // Remove "/* "
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            ts.moveEnd();
            // Extra ending '\n' of the document returned by DocumentUtilities.getText(doc) and lexed
            assertTrue(ts.movePrevious());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, "\n", -1);
            assertTrue(ts.movePrevious());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.DIV, "/", -1);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }


}
