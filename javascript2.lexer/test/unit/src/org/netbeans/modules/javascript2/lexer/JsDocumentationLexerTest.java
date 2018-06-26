/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
