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

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppStringTokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * based on JavaStringLexerText
 * 
 */
public class CppStringLexerTestCase extends NbTestCase {

    public CppStringLexerTestCase(String testName) {
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

    public void testZero() {
        String text = "\\0";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppStringTokenId.languageSingle());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.OCTAL_ESCAPE, "\\0");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testNextToken1() {
        String text = "t";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppStringTokenId.languageDouble());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TEXT, "t");
    }

    public void testNextToken2() {
        String text = "\"\\e\\t\\a\\b\\t \\tabc\\rsddfdsffffffffff\\uuuuAbcD\\377\"";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppStringTokenId.languageDouble());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.ANSI_COLOR, "\\e");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TAB, "\\t");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.BELL, "\\a");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.BACKSPACE, "\\b");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TAB, "\\t");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TAB, "\\t");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TEXT, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.CR, "\\r");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TEXT, "sddfdsffffffffff");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.UNICODE_ESCAPE, "\\uuuuAbcD");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.OCTAL_ESCAPE, "\\377");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.LAST_QUOTE, "\"");
    }

    public void testRawTokens() {
        // uR"*(This is a "raw UTF-16" string.)*)*"
        String text = "uR\"*(This is a \"raw UTF-16\" \\string.)*)*\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppStringTokenId.languageRawString());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_uR, "uR");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.START_DELIMETER, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.START_DELIMETER_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, "This is a ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.DOUBLE_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, "raw UTF-16");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.DOUBLE_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, " \\string.");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, ")*");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.END_DELIMETER_PAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.END_DELIMETER, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.LAST_QUOTE, "\"");

        assertFalse("No more tokens", ts.moveNext());
//        CndLexerUnitTest.dumpTokens(ts, "ts");
    }
}
