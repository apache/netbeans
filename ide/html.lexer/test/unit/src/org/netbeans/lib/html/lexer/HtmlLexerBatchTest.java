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

package org.netbeans.lib.html.lexer;

import junit.framework.TestCase;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test HTML lexer analyzis
 *
 * @author Marek Fukala
 */
public class HtmlLexerBatchTest extends TestCase {

    public HtmlLexerBatchTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testJspTags() {
        String text = "<jsp:useBean name=\"pkg.myBean\"/><!--comment-->abc&gt;def<tag attr=\"value\"></tag>";
                
        TokenHierarchy<?> hi = TokenHierarchy.create(text, HTMLTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN_SYMBOL, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN, "jsp:useBean");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.WS, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.ARGUMENT, "name");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.VALUE, "\"pkg.myBean\"");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_CLOSE_SYMBOL, "/>");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.BLOCK_COMMENT, "<!--comment-->");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TEXT, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.CHARACTER, "&gt;");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TEXT, "def");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN_SYMBOL, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN, "tag");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.WS, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.ARGUMENT, "attr");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.VALUE, "\"value\"");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_CLOSE_SYMBOL, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN_SYMBOL, "</");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_CLOSE, "tag");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_CLOSE_SYMBOL, ">");
    }
    
}
