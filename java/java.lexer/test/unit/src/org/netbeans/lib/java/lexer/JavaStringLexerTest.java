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
package org.netbeans.lib.java.lexer;

import org.netbeans.api.java.lexer.JavaStringTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author Jan Lahoda
 */
public class JavaStringLexerTest extends NbTestCase {

    public JavaStringLexerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testNextToken1() {
        String text = "t";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaStringTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TEXT, "t");
    }
    
    public void testNextToken2() {
        String text = "\\t\\b\\b\\t \\tabc\\rsddfdsffffffffff\\uuuuAbcD\\377";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaStringTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TAB, "\\t");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.BACKSPACE, "\\b");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.BACKSPACE, "\\b");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TAB, "\\t");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TAB, "\\t");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TEXT, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.CR, "\\r");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TEXT, "sddfdsffffffffff");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.UNICODE_ESCAPE, "\\uuuuAbcD");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.OCTAL_ESCAPE, "\\377");
    }
    
}
