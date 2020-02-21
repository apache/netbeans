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
package org.netbeans.modules.cnd.lexer;

import org.netbeans.cnd.api.lexer.DoxygenTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * based on JavadocLexerTest
 */
public class DoxygenLexerTestCase extends NbTestCase {

    public DoxygenLexerTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testNextToken() {
        String text = "< @param aaa <code>aaa</code> xyz {@link org.Aaa#aaa()} \\tag_object \\ not_tag @ not_tag2";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, DoxygenTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.POINTER_MARK, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.TAG, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.IDENT, "aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.HTML_TAG, "<code>");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.IDENT, "aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.HTML_TAG, "</code>");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.IDENT, "xyz");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.OTHER_TEXT, " {");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.TAG, "@link");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.IDENT, "org");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.DOT, ".");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.IDENT, "Aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.HASH, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.IDENT, "aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.OTHER_TEXT, "()} ");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.TAG, "\\tag_object");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.OTHER_TEXT, "\\");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.IDENT, "not_tag");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.OTHER_TEXT, "@");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, DoxygenTokenId.IDENT, "not_tag2");
    }
}
