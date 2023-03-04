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

import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.lang.TestTokenId;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class TokenHierarchySnapshotTest extends TestCase {
    
    public TokenHierarchySnapshotTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testSnapshot() throws Exception {
        if (true)
            return; // Snapshots will no longer function after #87014
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);
        TokenSequence<?> ts = hi.tokenSequence();
        assertFalse(ts.moveNext());
        
        // Insert text into document
        String text = "abc+def-xyz";
        doc.insertString(0, text, null);

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "def", 4);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "xyz", 8);
        assertFalse(ts.moveNext());
        LexerTestUtilities.incCheck(doc, false);

        // Create snapshot1 and check hierarchy
        String hi1text = doc.getText(0, doc.getLength());
        TokenHierarchy<?> hi1 = TokenHierarchy.create(hi1text,TestTokenId.language());
//        TokenHierarchy<?> snapshot1 = hi.createSnapshot();
//        assertEquals(snapshot1.snapshotOf(), hi);
//        assertFalse(snapshot1.isSnapshotReleased());
//
//        // Check that all the non-fly tokens are mutable
//        ts = snapshot1.tokenSequence();
//        assertEquals(0, ts.moveIndex(0));
//        assertTrue(ts.moveNext());
//        
//        LexerTestUtilities.assertTokenSequencesEqual(hi1.tokenSequence(), hi1,
//                snapshot1.tokenSequence(), snapshot1, false);
//
//        doc.insertString(4, "+", null);
//
//        // Check that the snapshot token sequence can be further operated.
//        assertEquals(0, ts.moveIndex(0));
//        assertTrue(ts.moveNext());
//        assertNotNull(ts.token());
//
//        // Check that the tokens except '+' are live
//        ts = snapshot1.tokenSequence();
//
//        LexerTestUtilities.assertTokenSequencesEqual(hi1.tokenSequence(), hi1,
//                snapshot1.tokenSequence(), snapshot1, true);
//
//        // Create snapshot2 and check hierarchy
//        String hi2text = doc.getText(0, doc.getLength());
//        TokenHierarchy<?> hi2 = TokenHierarchy.create(hi2text,TestTokenId.language());
//        TokenHierarchy<?> snapshot2 = hi.createSnapshot();
//        assertEquals(snapshot2.snapshotOf(), hi);
//
//        // Check that all the non-fly tokens are mutable
//        ts = snapshot2.tokenSequence();
//        assertEquals(0, ts.moveIndex(0));
//        assertTrue(ts.moveNext());
//
//        LexerTestUtilities.assertTokenSequencesEqual(hi2.tokenSequence(), hi2,
//                snapshot2.tokenSequence(), snapshot2, false);
//
//        doc.remove(8, 1);
//
//        LexerTestUtilities.assertTokenSequencesEqual(hi1.tokenSequence(), hi1,
//                snapshot1.tokenSequence(), snapshot1, false);
//        LexerTestUtilities.assertTokenSequencesEqual(hi2.tokenSequence(), hi2,
//                snapshot2.tokenSequence(), snapshot2, false);
    }
    
}
