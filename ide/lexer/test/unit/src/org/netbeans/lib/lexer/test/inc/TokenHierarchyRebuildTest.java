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

import java.util.ConcurrentModificationException;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.spi.lexer.MutableTextInput;

/**
 *
 * @author Miloslav Metelka
 */
public class TokenHierarchyRebuildTest extends NbTestCase {
    
    /** Creates a new instance of DocumentUpdateTest */
    public TokenHierarchyRebuildTest(String name) {
        super(name);
    }
    
    public void testRebuild() throws Exception {
        final Document doc = new ModificationTextDocument();
        doc.putProperty(Language.class,TestTokenId.language());
        doc.insertString(0, "abc def", null);
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts;
        Token<?> t;
        String tText;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            t = ts.token();
            assertNotNull(t);
            tText = t.text().toString();
            LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        ((ModificationTextDocument)doc).runAtomic(new Runnable() {
            @Override
            public void run() {
                MutableTextInput input = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                assertNotNull(input);
                input.tokenHierarchyControl().rebuild();
                LexerTestUtilities.initLastDocumentEventListening(doc);
            }
        });
        
        // Check the fired token hierarchy event
        int docLen = doc.getLength();
        TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
        assertEquals(0, evt.affectedStartOffset());
        // Extra newline contained in DocumentUtilities.getText(doc) being relexed
        assertEquals(docLen+1, evt.affectedEndOffset());
        
        ((AbstractDocument)doc).readLock();
        try { // ts should no longer work
            ts.moveNext();
            fail("ConcurrentModificationException not thrown.");
        } catch (ConcurrentModificationException e) {
            // Expected
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        TokenHierarchy<?> hi2 = TokenHierarchy.get(doc);
        assertSame(hi, hi2);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts2 = hi2.tokenSequence();
            assertTrue(ts2.moveNext());
            Token<?> t2  = ts2.token();

            assertNotSame(t, t2);
            assertSame(t.id(), t2.id());
            assertTrue(TokenUtilities.equals(tText, t2.text()));
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
    }
    
}
