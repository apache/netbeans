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

import junit.framework.TestCase;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 */
public class EscapeLineTestCase extends TestCase {

    public EscapeLineTestCase(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    public void testLineComment() {
        String text =  "//comm\\\n" + 
                       "ent\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LINE_COMMENT, "//comm\\\nent");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    } 
    
    public void testBlockComment() {
        String text =  "/*comm\\\n" + 
                       "ent\n" +
                       "*/\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BLOCK_COMMENT, "/*comm\\\nent\n*/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testString() {
        String text =  "\"string\\\n" + 
                       "\" \"str\\\n" +
                       "ing\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"string\\\n\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"str\\\ning\"");

        assertFalse("No more tokens", ts.moveNext());
    } 
    
    public void test1() {
        String text =  "in\\\n" + 
                       "t\\\n" + 
                       " a\\\r" + 
                       "\\\r\n" +
                       "; \\\r a\n" +
                       " \\\n b\n" +
                       "  \\\r\n c";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT, "in\\\nt");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_LINE, "\\\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_LINE, "\\\r");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_LINE, "\\\r\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SEMICOLON, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_WHITESPACE, " \\\r ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_WHITESPACE, " \\\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "b");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_WHITESPACE, "  \\\r\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "c");

        assertFalse("No more tokens", ts.moveNext());
    }
   
}
