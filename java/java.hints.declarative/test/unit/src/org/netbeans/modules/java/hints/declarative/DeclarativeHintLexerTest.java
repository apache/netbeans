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

package org.netbeans.modules.java.hints.declarative;

import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

import static org.netbeans.modules.java.hints.declarative.DeclarativeHintTokenId.*;
import static org.netbeans.modules.java.hints.declarative.TestUtils.*;

/**
 *
 * @author lahvac
 */
public class DeclarativeHintLexerTest {

    public DeclarativeHintLexerTest() {
    }

    @Test
    public void testSimple() {
        String text = " \'test\': 1 + 1 => \'fix\': 1 + 1;;";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, CHAR_LITERAL, "\'test\'");
        assertNextTokenEquals(ts, COLON, ":");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, CHAR_LITERAL, "\'fix\'");
        assertNextTokenEquals(ts, COLON, ":");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testSimpleNoDisplayNames() {
        String text = " 1 + 1 => 1 + 1;;";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testWhitespaceAtTheEnd() {
        String text = " 1 + 1 => 1 + 1;; ";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");
        assertNextTokenEquals(ts, WHITESPACE, " ");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testVariable() {
        String text = " $1 + 1 => 1 + $1;; ";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, WHITESPACE, " ");//TODO
        assertNextTokenEquals(ts, VARIABLE, "$1");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " + 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + ");
        assertNextTokenEquals(ts, VARIABLE, "$1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");
        assertNextTokenEquals(ts, WHITESPACE, " ");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testCondition() {
        String text = " 1 + 1 :: $1 instanceof something && $test instanceof somethingelse => 1 + 1;; ";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, DOUBLE_COLON, "::");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, VARIABLE, "$1");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, INSTANCEOF, "instanceof");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " something ");
        assertNextTokenEquals(ts, AND, "&&");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, VARIABLE, "$test");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, INSTANCEOF, "instanceof");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " somethingelse ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");
        assertNextTokenEquals(ts, WHITESPACE, " ");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testMultiple() {
        String text = "'test': 1 + 1 => 1 + 1;;'test2': 1 + 1 => 1 + 1;;";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, CHAR_LITERAL, "'test'");
        assertNextTokenEquals(ts, COLON, ":");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");
        assertNextTokenEquals(ts, CHAR_LITERAL, "'test2'");
        assertNextTokenEquals(ts, COLON, ":");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testNot() {
        String text = "!";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, NOT, "!");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testComments1() {
        String text = "/*=>*/'test': 1 + 1 => 1 + 1;;/*;;*/'test2': 1 + 1 => 1 + 1;;";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, BLOCK_COMMENT, "/*=>*/");
        assertNextTokenEquals(ts, CHAR_LITERAL, "'test'");
        assertNextTokenEquals(ts, COLON, ":");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");
        assertNextTokenEquals(ts, BLOCK_COMMENT, "/*;;*/");
        assertNextTokenEquals(ts, CHAR_LITERAL, "'test2'");
        assertNextTokenEquals(ts, COLON, ":");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testComments2() {
        String text = "//=>\n'test': 1 + 1 => 1 + 1;;//;;\n'test2': 1 + 1 => 1 + 1;;";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, LINE_COMMENT, "//=>\n");
        assertNextTokenEquals(ts, CHAR_LITERAL, "'test'");
        assertNextTokenEquals(ts, COLON, ":");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");
        assertNextTokenEquals(ts, LINE_COMMENT, "//;;\n");
        assertNextTokenEquals(ts, CHAR_LITERAL, "'test2'");
        assertNextTokenEquals(ts, COLON, ":");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testComments3() {
        String text = "'test': 1 /*=>;;::*/+ 1 => 1 + 1;;'test2': 1 + 1 => 1 + 1;;";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, CHAR_LITERAL, "'test'");
        assertNextTokenEquals(ts, COLON, ":");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 ");
        assertNextTokenEquals(ts, BLOCK_COMMENT, "/*=>;;::*/");
        assertNextTokenEquals(ts, JAVA_SNIPPET, "+ 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");
        assertNextTokenEquals(ts, CHAR_LITERAL, "'test2'");
        assertNextTokenEquals(ts, COLON, ":");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testLexerSanity() {
        String code = "'Use of assert'://\n" +
                      "   assert /**/ $1 : $2; :: //\n $1 instanceof boolean && $2 instanceof java.lang.Object\n" +
                      "=> if (!$1) throw new /**/ IllegalStateException($2);\n" +
                      ";;//\n";

        for (int cntr = 0; cntr < code.length(); cntr++) {
            String currentpath = code.substring(0, cntr);
            TokenHierarchy<?> hi = TokenHierarchy.create(currentpath, language());
            TokenSequence<?> ts = hi.tokenSequence();

            while (ts.moveNext())
                ;
        }
    }

    @Test
    public void testDisplayNameWithKeyword() {
        String text = "'test instanceof':";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, CHAR_LITERAL, "'test instanceof'");
        assertNextTokenEquals(ts, COLON, ":");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testImportsAndPredicates() {
        String text = "<?import java.util.List;?>'test': 1 + 1 => 1 + 1;;<?private boolean doTest() {return false;}?>'test2': 1 + 1 => 1 + 1 :: otherwise;;";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, JAVA_BLOCK, "<?import java.util.List;?>");
        assertNextTokenEquals(ts, CHAR_LITERAL, "'test'");
        assertNextTokenEquals(ts, COLON, ":");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");
        assertNextTokenEquals(ts, JAVA_BLOCK, "<?private boolean doTest() {return false;}?>");
        assertNextTokenEquals(ts, CHAR_LITERAL, "'test2'");
        assertNextTokenEquals(ts, COLON, ":");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, DOUBLE_COLON, "::");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, OTHERWISE, "otherwise");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testOptions() {
        String text = "<!option1=\"value1,value2\",option2=true>'test': 1 + 1 <!option1=\"value1,value2\",option2=true>=> 1 + 1;;";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, OPTIONS, "<!option1=\"value1,value2\",option2=true>");
        assertNextTokenEquals(ts, CHAR_LITERAL, "'test'");
        assertNextTokenEquals(ts, COLON, ":");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1 ");
        assertNextTokenEquals(ts, OPTIONS, "<!option1=\"value1,value2\",option2=true>");
        assertNextTokenEquals(ts, LEADS_TO, "=>");
        assertNextTokenEquals(ts, JAVA_SNIPPET, " 1 + 1");
        assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testRegressionLeadsToAndSemicolon() {
        String text = "()=>!";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        TestUtils.assertNextTokenEquals(ts, JAVA_SNIPPET, "()");
        TestUtils.assertNextTokenEquals(ts, LEADS_TO, "=>");
        TestUtils.assertNextTokenEquals(ts, NOT, "!");

        assertFalse(ts.moveNext());
    }

    @Test
    public void testDoubleColonDisamgibuation() {
        {
            String text = "System.err::println;;";
            TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
            TokenSequence<?> ts = hi.tokenSequence();
            TestUtils.assertNextTokenEquals(ts, JAVA_SNIPPET, "System.err::println");
            TestUtils.assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");

            assertFalse(ts.moveNext());
        }
        {
            String text = "System.err::!println();;";
            TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
            TokenSequence<?> ts = hi.tokenSequence();
            TestUtils.assertNextTokenEquals(ts, JAVA_SNIPPET, "System.err");
            TestUtils.assertNextTokenEquals(ts, DOUBLE_COLON, "::");
            TestUtils.assertNextTokenEquals(ts, NOT, "!");
            TestUtils.assertNextTokenEquals(ts, JAVA_SNIPPET, "println()");
            TestUtils.assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");

            assertFalse(ts.moveNext());
        }
        {
            String text = "$1.isDirectory <!suppress-warnings=isDirectory> :: $1 instanceof java.io.File ;;";
            TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
            TokenSequence<?> ts = hi.tokenSequence();
            TestUtils.assertNextTokenEquals(ts, VARIABLE, "$1");
            TestUtils.assertNextTokenEquals(ts, JAVA_SNIPPET, ".isDirectory ");
            TestUtils.assertNextTokenEquals(ts, OPTIONS, "<!suppress-warnings=isDirectory>");
            TestUtils.assertNextTokenEquals(ts, WHITESPACE, " ");
            TestUtils.assertNextTokenEquals(ts, DOUBLE_COLON, "::");
            TestUtils.assertNextTokenEquals(ts, WHITESPACE, " ");
            TestUtils.assertNextTokenEquals(ts, VARIABLE, "$1");
            TestUtils.assertNextTokenEquals(ts, WHITESPACE, " ");
            TestUtils.assertNextTokenEquals(ts, INSTANCEOF, "instanceof");
            TestUtils.assertNextTokenEquals(ts, JAVA_SNIPPET, " java.io.File ");
            TestUtils.assertNextTokenEquals(ts, DOUBLE_SEMICOLON, ";;");

            assertFalse(ts.moveNext());
        }
    }

}