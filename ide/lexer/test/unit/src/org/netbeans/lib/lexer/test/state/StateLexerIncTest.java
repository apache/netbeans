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

package org.netbeans.lib.lexer.test.state;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class StateLexerIncTest extends TestCase {
    
    public StateLexerIncTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void test() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        InputAttributes attrs = new InputAttributes();
        doc.putProperty(InputAttributes.class, attrs);
        doc.putProperty(Language.class, StateTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            // Extra newline at the end of document returned in DocumentUtilities.getText(doc) is lexed too
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.ERROR, "\n", 0);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Insert text into document
        String text = "abc";
        doc.insertString(0, text, null);

        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.A, "a", 0);
            assertEquals(LexerTestUtilities.lookahead(ts), 0);
            assertEquals(LexerTestUtilities.state(ts), StateLexer.AFTER_A);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.BMULTI, "b", 1);
            assertEquals(LexerTestUtilities.state(ts), StateLexer.AFTER_B);
            assertEquals(LexerTestUtilities.lookahead(ts), 1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.ERROR, "c", 2);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.ERROR, "\n", 3);
            assertEquals(LexerTestUtilities.state(ts), null);
            assertFalse(ts.moveNext());

            LexerTestUtilities.incCheck(doc, false);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Should relex "b" so restart state should be AFTER_A
        attrs.setValue(StateTokenId.language(), "restartState", StateLexer.AFTER_A, true);
        doc.insertString(2, "b", null);
        
    }

}
