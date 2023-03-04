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
package org.netbeans.modules.javascript2.lexer;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId;

/**
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocumentationLexerTest extends NbTestCase {

    public JsDocumentationLexerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    public void testCommonBlockComment01() {
        String text = "/* comment */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_BLOCK_START, "/*");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "comment");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testCommonBlockComment02() {
        String text = "/*comment*/";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_BLOCK_START, "/*");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "comment");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testCommonBlockComment03() {
        String text = "/* \n\n */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_BLOCK_START, "/*");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testCommonDocComment01() {
        String text = "/** comment */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "comment");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testCommonDocComment02() {
        String text = "/**comment*/";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "comment");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testCommonDocComment03() {
        String text = "/** \n\n */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testCommonDocComment04() {
        String text = "/**   @  */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, "   ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.AT, "@");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testCommonDocComment05() {
        String text = "/** @p */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.KEYWORD, "@p");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testCommonDocComment06() {
        String text = "/** \n * @param */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.ASTERISK, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.KEYWORD, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testCommonDocComment07() {
        String text = "/** \n *@param */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.ASTERISK, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.KEYWORD, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testCommonDocComment08() {
        String text = "/** \n *@ */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.ASTERISK, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.AT, "@");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testCommonDocComment09() {
        String text = "/**\n * @param {String, Date} [myDate] Specifies the date, if applicable. */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.ASTERISK, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.KEYWORD, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.BRACKET_LEFT_CURLY, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "String");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "Date");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.BRACKET_RIGHT_CURLY, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.BRACKET_LEFT_BRACKET, "[");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "myDate");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.BRACKET_RIGHT_BRACKET, "]");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "Specifies");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "the");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "date");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "applicable.");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testUnfinishedComment01() {
        String text = "/* \n var Carrot = {";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_BLOCK_START, "/*");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "var");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "Carrot");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.BRACKET_LEFT_CURLY, "{");
    }

    public void testUnfinishedComment02() {
        String text = "/** getColor: function () {}, ";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "getColor:");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "function");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "()");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.BRACKET_LEFT_CURLY, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.BRACKET_RIGHT_CURLY, "}");
    }

    public void testHtmlComment01() {
        String text = "/** <b>text</b> */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.HTML, "<b>");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "text");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.HTML, "</b>");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testHtmlComment02() {
        String text = "/** <a href=\"mailto:marfous@netbeans.org\">href</a> */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.HTML, "<a href=\"mailto:marfous@netbeans.org\">");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "href");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.HTML, "</a>");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testHtmlComment03() {
        String text = "/** <a> */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.HTML, "<a>");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testHtmlComment04() {
        String text = "/** </a> ";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.HTML, "</a>");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
    }

    public void testCommentWithString() {
        String text = "/** @param ident \"cokoliv\" \n @param */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.KEYWORD, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "ident");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.STRING, "cokoliv");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.STRING_END, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.KEYWORD, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testIssue223107_1() {
        String text = "/** Start if a < 0 */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "Start");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testIssue223107_2() {
        String text = "/** Start if a < 0 <a href=\"\"> */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "Start");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.HTML, "<a href=\"\">");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }

    public void testIssue223107_3() {
        String text = "/** Start if a < 0 or a > 10 <a href=\"\"> */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsDocumentationTokenId.language());
        TokenSequence<?extends JsDocumentationTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_DOC_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "Start");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "or");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.OTHER, "10");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.HTML, "<a href=\"\">");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsDocumentationTokenId.COMMENT_END, "*/");
    }
}
