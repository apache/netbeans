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
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Test of invalid lexer's behavior.
 *
 * @author mmetelka
 */
public class InvalidLexerOperationTest extends TestCase {
    
    public InvalidLexerOperationTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testEarlyNullToken() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        InputAttributes attrs = new InputAttributes();
        doc.putProperty(InputAttributes.class, attrs);
        
        // Insert text into document
        String text = "abc";
        doc.insertString(0, text, null);
        // Put the language now into the document so that lexing starts from scratch
        doc.putProperty(Language.class, StateTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();

            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.A, "a", 0);
            assertEquals(LexerTestUtilities.lookahead(ts), 0);
            assertEquals(LexerTestUtilities.state(ts), StateLexer.AFTER_A);

            attrs.setValue(StateTokenId.language(), "returnNullToken", Boolean.TRUE, true);
            try {
                // Lexer will return null token too early
                assertTrue(ts.moveNext());
                fail("IllegalStateException not thrown when null token returned before input end.");
            } catch (IllegalStateException e) {
                // Expected fail of lexer
            }
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testBatchLexerRelease() throws Exception {
        String text = "ab";
        InputAttributes attrs = new InputAttributes();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, StateTokenId.language(),
                null, attrs);
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, StateTokenId.A, "a", 0);
        assertTrue(ts.moveNext());
        LanguagePath lp = LanguagePath.get(StateTokenId.language());
        assertFalse(Boolean.TRUE.equals(attrs.getValue(lp, "lexerRelease")));
        LexerTestUtilities.assertTokenEquals(ts, StateTokenId.BMULTI, "b", 1);
        assertFalse(ts.moveNext());
        assertTrue(Boolean.TRUE.equals(attrs.getValue(lp, "lexerRelease")));

    }

    public void testIncLexerRelease() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        InputAttributes attrs = new InputAttributes();
        doc.putProperty(InputAttributes.class, attrs);
        
        // Insert initial text into document
        String text = "ab";
        doc.insertString(0, text, null);
        // Put the language now into the document so that lexing starts from scratch
        doc.putProperty(Language.class, StateTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts;
        LanguagePath lp;
        
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();

            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.A, "a", 0);
            lp = LanguagePath.get(StateTokenId.language());
            assertFalse(Boolean.TRUE.equals(attrs.getValue(lp, "lexerRelease")));
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.BMULTI, "b", 1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.ERROR, "\n", 2);
            assertFalse(ts.moveNext());
            assertTrue(Boolean.TRUE.equals(attrs.getValue(lp, "lexerRelease")));
            attrs.setValue(lp, "lexerRelease", Boolean.FALSE, false);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        // Do modification and check lexer release after it
        doc.insertString(1, "b", null);
        assertTrue(Boolean.TRUE.equals(attrs.getValue(lp, "lexerRelease")));
    }

}
