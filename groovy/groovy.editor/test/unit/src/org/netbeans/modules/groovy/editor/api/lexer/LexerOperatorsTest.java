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

package org.netbeans.modules.groovy.editor.api.lexer;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 *
 * @author Martin Janicek
 */
public class LexerOperatorsTest extends GroovyTestBase {

    public LexerOperatorsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }
    
    public void testRegexpFindOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("\"123\" =~ \"1\"");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"123\"");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.REGEX_FIND, "=~");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"1\"");
    }
    
    public void testRegexpMatchOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("\"1\" ==~ /\\d+/");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"1\"");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.REGEX_MATCH, "==~");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        // Not sure if Lexer is returning correct token here, shouldn't this be something like REGEXP_LITERAL ??
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "/\\d+/");
    }
    
    public void testElvisOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("abc?:");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.ELVIS_OPERATOR, "?:");
    }
     
    public void testIdentityOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("0 <=> 1");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.NUM_INT, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.COMPARE_TO, "<=>");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.NUM_INT, "1");
    }
    
    public void testSafeNavigationOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("abc?.");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.OPTIONAL_DOT, "?.");
    }
    
    public void testMethodReferenceOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("abc.&");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.MEMBER_POINTER, ".&");
    }
    
    public void testSpreadOperatorOnMap() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("foo(1:\"a\", *:x)");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.NUM_INT, "1");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.COLON, ":");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"a\"");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        
        // Not sure why Star + Colon, I would expected Spread_Map_Arg token instead
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STAR, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.COLON, ":");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.RPAREN, ")");
    }
    
    public void testJavaFieldOverrideOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("abc.@");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.DOT, ".");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.AT, "@");
    }
    
    // Interestingly enough no token is comming from Groovy Lexer for Spread Java Field  
    // Instead of such expected behavior there are two tokens SPREAD_DOT + AT
    public void testSpreadJavaFieldOverrideOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("abc*.@");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.SPREAD_DOT, "*.");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.AT, "@");
    }
    
    private TokenSequence<GroovyTokenId> createTokenSequenceFor(String text) {
        return TokenHierarchy.create(text, GroovyTokenId.language()).tokenSequence(GroovyTokenId.language());
    }
}
