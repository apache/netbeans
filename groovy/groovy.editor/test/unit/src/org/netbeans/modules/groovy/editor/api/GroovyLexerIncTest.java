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

package org.netbeans.modules.groovy.editor.api;

import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;

/**
 *
 * @author Martin Adamek
 */
public class GroovyLexerIncTest extends NbTestCase {

    public GroovyLexerIncTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        // uncomment to enable logging from GroovyLexer
//        Logger.getLogger("org.netbeans.modules.groovy.editor.lexer.GroovyLexer").setLevel(Level.FINEST);
    }

    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }
    
    public void test1() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,GroovyTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);
        TokenSequence<?> ts = hi.tokenSequence();
        // contains \n
        assertTrue(ts.moveNext());
        assertEquals(GroovyTokenId.NLS, ts.token().id());

        assertFalse(ts.moveNext());

        // Insert text into document
        String text = "println \"Hello $name\"";
        doc.insertString(0, text, null);

//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.FINE); // Extra logging
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenHierarchyUpdate.class.getName()).setLevel(Level.FINE); // Extra logging
        // Last token sequence should throw exception - new must be obtained
        try {
            ts.moveNext();
            fail("TokenSequence.moveNext() did not throw exception as expected.");
        } catch (ConcurrentModificationException e) {
            // Expected exception
        }

        ts = hi.tokenSequence();

        next(ts, GroovyTokenId.IDENTIFIER, "println", 0);
        assertEquals(1, LexerTestUtilities.lookahead(ts));
        next(ts, GroovyTokenId.WHITESPACE, " ", 7);
        next(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $", 8);
        next(ts, GroovyTokenId.IDENTIFIER, "name", 16);
        next(ts, GroovyTokenId.STRING_LITERAL, "\"", 20);

        // contains \n
        assertTrue(ts.moveNext());
        assertEquals(GroovyTokenId.NLS, ts.token().id());
        
        assertFalse(ts.moveNext());

        LexerTestUtilities.incCheck(doc, false);

        int offset = text.length() - 1;

        // Check TokenSequence.move()
        int relOffset = ts.move(50); // past the end of all tokens
        assertEquals(relOffset, 50 - (offset + 2));
        assertTrue(ts.movePrevious());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.NLS, "\n", offset + 1);
        assertTrue(ts.movePrevious());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"", offset);

        relOffset = ts.move(8); // right at begining of gstring
        assertEquals(relOffset, 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $", 8);

        relOffset = ts.move(-5); // to first token "println"
        assertEquals(relOffset, -5);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.IDENTIFIER, "println", 0);

        relOffset = ts.move(10);
        assertEquals(relOffset, 2);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $", 8);

        doc.insertString(5, "x", null); // should be "printxln"

        // Last token sequence should throw exception - new must be obtained
        try {
            ts.moveNext();
            fail("TokenSequence.moveNext() did not throw exception as expected.");
        } catch (ConcurrentModificationException e) {
            // Expected exception
        }

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.IDENTIFIER, "printxln", 0);
        LexerTestUtilities.incCheck(doc, false);

        // Remove added 'x' to become "println" again
        doc.remove(5, 1); // should be "println" again

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.IDENTIFIER, "println", 0);
        LexerTestUtilities.incCheck(doc, false);

        // Now insert right at the end of first token - identifier with lookahead 1
        doc.insertString(7, "x", null); // should become "printlnx"

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.IDENTIFIER, "printlnx", 0);
        LexerTestUtilities.incCheck(doc, false);

        doc.remove(7, 1); // return back to "println"

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, GroovyTokenId.IDENTIFIER, "println", 0);
        LexerTestUtilities.incCheck(doc, false);

        doc.insertString(offset, " ", null);

        ts = hi.tokenSequence();
        next(ts, GroovyTokenId.IDENTIFIER, "println", 0);
        next(ts, GroovyTokenId.WHITESPACE, " ", 7);
        next(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $", 8);
        next(ts, GroovyTokenId.IDENTIFIER, "name", 16);

        next(ts, GroovyTokenId.STRING_LITERAL, " \"", 20);
        assertTrue(ts.moveNext());
        assertEquals(GroovyTokenId.NLS, ts.token().id());
        assertFalse(ts.moveNext());
        LexerTestUtilities.incCheck(doc, false);
    }

    public void test2() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,GroovyTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);
        TokenSequence<?> ts = hi.tokenSequence();
        // contains \n
        assertTrue(ts.moveNext());
        assertEquals(GroovyTokenId.NLS, ts.token().id());

        assertFalse(ts.moveNext());

        // Insert text into document
        String text = "println \"Hello ${name} !\"";
        doc.insertString(0, text, null);

        // Last token sequence should throw exception - new must be obtained
        try {
            ts.moveNext();
            fail("TokenSequence.moveNext() did not throw exception as expected.");
        } catch (ConcurrentModificationException e) {
            // Expected exception
        }

        ts = hi.tokenSequence();

        next(ts, GroovyTokenId.IDENTIFIER, "println", 0);
        next(ts, GroovyTokenId.WHITESPACE, " ", 7);
        next(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $", 8);
        next(ts, GroovyTokenId.LBRACE, "{", 16);
        next(ts, GroovyTokenId.IDENTIFIER, "name", 17);
        next(ts, GroovyTokenId.RBRACE, "}", 21);
        next(ts, GroovyTokenId.STRING_LITERAL, " !\"", 22);

        // contains \n
        assertTrue(ts.moveNext());
        assertEquals(GroovyTokenId.NLS, ts.token().id());

        assertFalse(ts.moveNext());

        LexerTestUtilities.incCheck(doc, false);

        int offset = text.length() - 3;

        doc.insertString(offset, " ", null);

        ts = hi.tokenSequence();
        next(ts, GroovyTokenId.IDENTIFIER, "println", 0);
        next(ts, GroovyTokenId.WHITESPACE, " ", 7);
        next(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $", 8);
        next(ts, GroovyTokenId.LBRACE, "{", 16);
        next(ts, GroovyTokenId.IDENTIFIER, "name", 17);
        next(ts, GroovyTokenId.RBRACE, "}", 21);
        next(ts, GroovyTokenId.STRING_LITERAL, "  !\"", 22);

        // contains \n
        assertTrue(ts.moveNext());
        assertEquals(GroovyTokenId.NLS, ts.token().id());

        assertFalse(ts.moveNext());

        LexerTestUtilities.incCheck(doc, false);

        offset = text.length() - 3;

        doc.insertString(offset, " ", null);

        ts = hi.tokenSequence();
        next(ts, GroovyTokenId.IDENTIFIER, "println", 0);
        next(ts, GroovyTokenId.WHITESPACE, " ", 7);
        next(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $", 8);
        next(ts, GroovyTokenId.LBRACE, "{", 16);
        next(ts, GroovyTokenId.IDENTIFIER, "name", 17);
        next(ts, GroovyTokenId.RBRACE, "}", 21);
        next(ts, GroovyTokenId.STRING_LITERAL, "   !\"", 22);

        // contains \n
        assertTrue(ts.moveNext());
        assertEquals(GroovyTokenId.NLS, ts.token().id());

        assertFalse(ts.moveNext());

        LexerTestUtilities.incCheck(doc, false);
    }

    public void test3() throws BadLocationException {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,GroovyTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);
        TokenSequence<?> ts = hi.tokenSequence();
        // contains \n
        assertTrue(ts.moveNext());
        assertEquals(GroovyTokenId.NLS, ts.token().id());

        assertFalse(ts.moveNext());

        // Insert text into document
        String text = "class Foo {  }";
        doc.insertString(0, text, null);

        // Last token sequence should throw exception - new must be obtained
        try {
            ts.moveNext();
            fail("TokenSequence.moveNext() did not throw exception as expected.");
        } catch (ConcurrentModificationException e) {
            // Expected exception
        }

        ts = hi.tokenSequence();
        next(ts, GroovyTokenId.LITERAL_class, "class", 0);
        next(ts, GroovyTokenId.WHITESPACE, " ", 5);
        next(ts, GroovyTokenId.IDENTIFIER, "Foo", 6);
        next(ts, GroovyTokenId.WHITESPACE, " ", 9);
        next(ts, GroovyTokenId.LBRACE, "{", 10);
        next(ts, GroovyTokenId.WHITESPACE, "  ", 11);
        next(ts, GroovyTokenId.RBRACE, "}", 13);

        // contains \n
        assertTrue(ts.moveNext());
        assertEquals(GroovyTokenId.NLS, ts.token().id());

        assertFalse(ts.moveNext());

        LexerTestUtilities.incCheck(doc, false);

        int offset = text.length() - 2;
        doc.insertString(offset, "d", null);
        doc.insertString(offset + 1, "e", null);
        doc.insertString(offset + 2, "f", null);

        ts = hi.tokenSequence();
        next(ts, GroovyTokenId.LITERAL_class, "class", 0);
        next(ts, GroovyTokenId.WHITESPACE, " ", 5);
        next(ts, GroovyTokenId.IDENTIFIER, "Foo", 6);
        next(ts, GroovyTokenId.WHITESPACE, " ", 9);
        next(ts, GroovyTokenId.LBRACE, "{", 10);
        next(ts, GroovyTokenId.WHITESPACE, " ", 11);
        next(ts, GroovyTokenId.LITERAL_def, "def", 12);
        next(ts, GroovyTokenId.WHITESPACE, " ", 15);
        next(ts, GroovyTokenId.RBRACE, "}", 16);

        // contains \n
        assertTrue(ts.moveNext());
        assertEquals(GroovyTokenId.NLS, ts.token().id());

        assertFalse(ts.moveNext());

        LexerTestUtilities.incCheck(doc, false);
    }

    void next(TokenSequence<?> ts, GroovyTokenId id, String fixedText, int offset){
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, id, fixedText, offset);
    }

}
