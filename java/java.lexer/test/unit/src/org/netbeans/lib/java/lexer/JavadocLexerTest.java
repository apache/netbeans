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

import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author Jan Lahoda
 */
public class JavadocLexerTest extends NbTestCase {

    public JavadocLexerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testNextToken1() {
        String text = "@param aaa <code>aaa</code> xyz {@link org.Aaa#aaa()}";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "aaa");
        assertTrue((Boolean) ts.token().getProperty("javadoc-identifier"));
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "<code>");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "aaa");
        assertNull((Boolean) ts.token().getProperty("javadoc-identifier"));
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "</code>");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "xyz");
        assertNull((Boolean) ts.token().getProperty("javadoc-identifier"));
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " {");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@link");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "org");
        assertNull((Boolean) ts.token().getProperty("javadoc-identifier"));
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.DOT, ".");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "Aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HASH, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "aaa");
        assertNull((Boolean) ts.token().getProperty("javadoc-identifier"));
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "()}");
    }

    public void testNextToken2() {
        String text = "abc @foo xyz\n@deprecated";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " @");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "xyz");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@deprecated");
    }

    public void testNextBrokenHTML1() {
        String text = "<code\n @param";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "<code");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@param");
    }

    public void testNextBrokenHTML2() {
        String text = "<code\n * @param\n<code\n ** @param";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "<code");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "\n * ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "<code\n ** @param");
    }


    public void testNextBrokenHTML3() {
        String text = "<code <code\n @param";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "<code ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "<code");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@param");
    }

    public void test233097() {
        String text = "{@code Foo<Bar>}";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@code");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "Foo");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "Bar");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "}");
    }

    public void test233097b() {
        String text = "{@code null}";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@code");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "null");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "}");
    }

    public void testMarkdown() {
        String text = "///@see\n/// @see";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "///");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@see");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "\n/// ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@see");

        assertFalse(ts.moveNext());
    }

//    public void testModification1() throws Exception {
//        PlainDocument doc = new PlainDocument();
//        doc.putProperty(Language.class, JavadocTokenId.language());
//        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
//        
//        {
//            TokenSequence<?> ts = hi.tokenSequence();
//            ts.moveStart();
//            assertFalse(ts.moveNext());
//        }
//        
//        doc.insertString(0, "@", null);
//    }
}
