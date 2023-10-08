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

package org.netbeans.lib.java.lexer;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import junit.framework.TestCase;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class JavaLexerStateTest extends TestCase {

    public JavaLexerStateTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    public void testTemplates1() throws BadLocationException {
        Document doc = new PlainDocument();

        doc.putProperty(Language.class, JavaTokenId.language());
        doc.insertString(0, "\"\";", null);

        String text = "\\{1 + \"\\{a}\".length()}";
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);

        for (int i = 0; i < text.length(); i++) {
            TokenSequence<?> ts = hi.tokenSequence();

            while (ts.moveNext());
            doc.insertString(1 + i, "" + text.charAt(i), null);
        }

        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\\{");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "1");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.PLUS, "+");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\\{");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "}\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOT, ".");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "length");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "}\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SEMICOLON, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, "\n");
        assertFalse(ts.moveNext());
    }

}
