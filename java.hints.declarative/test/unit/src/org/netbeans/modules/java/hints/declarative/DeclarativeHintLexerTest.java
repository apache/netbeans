/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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

}