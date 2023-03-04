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
import org.netbeans.lib.lexer.lang.TestJavadocTokenId;
import junit.framework.TestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.simple.*;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class InputAttributesTest extends TestCase {

    public InputAttributesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    LanguagePath simpleLP = LanguagePath.get(TestTokenId.language());
    LanguagePath jdLP = LanguagePath.get(TestJavadocTokenId.language());
    LanguagePath nestedJDLP  = LanguagePath.get(simpleLP,TestJavadocTokenId.language());

    public void testGetSetValue() {
        InputAttributes attrs = new InputAttributes();
        assertNull(attrs.getValue(simpleLP, "version"));
        attrs.setValue(simpleLP, "version", Integer.valueOf(1), false);
        assertEquals(attrs.getValue(simpleLP, "version"), Integer.valueOf(1));
        
        attrs = new InputAttributes();
        attrs.setValue(simpleLP, "version", Integer.valueOf(1), true);
        assertEquals(attrs.getValue(simpleLP, "version"), Integer.valueOf(1));
    }

    public void testInheritance() {
        InputAttributes attrs = new InputAttributes();
        attrs.setValue(jdLP, "version", Integer.valueOf(1), false);
        assertNull(attrs.getValue(nestedJDLP, "version"));
        
        attrs = new InputAttributes();
        attrs.setValue(jdLP, "version", Integer.valueOf(1), true);
        assertEquals(attrs.getValue(nestedJDLP, "version"), Integer.valueOf(1));
    }

    public void testLexerInputAttributes() {
        String text = "public static private";

        // Default version recognizes "static" keyword
        TokenHierarchy<?> hi = TokenHierarchy.create(text,TestTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PUBLIC, "public", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 6);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.STATIC, "static", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 13);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PRIVATE, "private", 14);
        assertFalse(ts.moveNext());

        // Version 1 recognizes "static" as identifier
        InputAttributes attrs = new InputAttributes();
        attrs.setValue(TestTokenId.language(), "version", Integer.valueOf(1), false);
        hi = TokenHierarchy.create(text, false,TestTokenId.language(), null, attrs);
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PUBLIC, "public", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 6);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "static", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 13);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PRIVATE, "private", 14);
        assertFalse(ts.moveNext());
    }
    
}
