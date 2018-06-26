/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.groovy.gsp.lexer;

import junit.framework.TestCase;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import static org.netbeans.lib.lexer.test.LexerTestUtilities.assertTokenEquals;

/**
 * Test GSP lexer.
 *
 * @author Martin Adamek
 * @author Martin Janicek
 */
public class GspLexerTest extends TestCase {

    public GspLexerTest(String testName) {
        super(testName);
    }

    public void testOnlyHTML() {
        String text = "<html>"
                        + "<body>"
                            + "<h1>Sample line</h1>"
                        + "</body>"
                    + "</html>";
        TokenSequence<?> sequence = createTokenSequence(text);

        checkNext(sequence, GspTokenId.HTML, "<html>");
        checkNext(sequence, GspTokenId.HTML, "<body>");
        checkNext(sequence, GspTokenId.HTML, "<h1>");
        checkNext(sequence, GspTokenId.HTML, "Sample line</h1>");
        checkNext(sequence, GspTokenId.HTML, "</body>");
        checkNext(sequence, GspTokenId.HTML, "</html>");
    }

    public void testPairGTag() {
        String text = "<g:if>"
                    + "</g:if>";
        TokenSequence<?> sequence = createTokenSequence(text);

        checkNext(sequence, GspTokenId.GTAG_OPENING_START, "<g:");
        checkNext(sequence, GspTokenId.GTAG_OPENING_NAME, "if");
        checkNext(sequence, GspTokenId.GTAG_OPENING_END, ">");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_START, "</g:");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_NAME, "if");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_END, ">");
    }

    public void testPairGTagWithExpression() {
        String text = "<g:if test=\"${}\">"
                    + "</g:if>";
        TokenSequence<?> sequence = createTokenSequence(text);

        checkNext(sequence, GspTokenId.GTAG_OPENING_START, "<g:");
        checkNext(sequence, GspTokenId.GTAG_OPENING_NAME, "if");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_NAME, " test=");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_VALUE, "\"");
        checkNext(sequence, GspTokenId.GSTRING_START, "${");
        checkNext(sequence, GspTokenId.GSTRING_END, "}");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_VALUE, "\"");
        checkNext(sequence, GspTokenId.GTAG_OPENING_END, ">");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_START, "</g:");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_NAME, "if");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_END, ">");
    }

    // See issue 202243
    public void testPairGTagWithExtendExpression() {
        String text = "<g:if test=\"\\${}\">"
                    + "</g:if>";
        TokenSequence<?> sequence = createTokenSequence(text);

        checkNext(sequence, GspTokenId.GTAG_OPENING_START, "<g:");
        checkNext(sequence, GspTokenId.GTAG_OPENING_NAME, "if");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_NAME, " test=");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_VALUE, "\"");
        checkNext(sequence, GspTokenId.GSTRING_START, "\\${");
        checkNext(sequence, GspTokenId.GSTRING_END, "}");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_VALUE, "\"");
        checkNext(sequence, GspTokenId.GTAG_OPENING_END, ">");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_START, "</g:");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_NAME, "if");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_END, ">");
    }

    public void testIndependentGTag() {
        String text = "<g:if/>";
        TokenSequence<?> sequence = createTokenSequence(text);

        checkNext(sequence, GspTokenId.GTAG_OPENING_START, "<g:");
        checkNext(sequence, GspTokenId.GTAG_OPENING_NAME, "if");
        checkNext(sequence, GspTokenId.GTAG_INDEPENDENT_END, "/>");
    }

    public void testInnerGTag() {
        String text = "<g:if>"
                        + "<g:if test=\"\">"
                        + "</g:if>"
                    + "</g:if>";
        TokenSequence<?> sequence = createTokenSequence(text);

        checkNext(sequence, GspTokenId.GTAG_OPENING_START, "<g:");
        checkNext(sequence, GspTokenId.GTAG_OPENING_NAME, "if");
        checkNext(sequence, GspTokenId.GTAG_OPENING_END, ">");
        checkNext(sequence, GspTokenId.GTAG_OPENING_START, "<g:");
        checkNext(sequence, GspTokenId.GTAG_OPENING_NAME, "if");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_NAME, " test=");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_VALUE, "\"\"");
        checkNext(sequence, GspTokenId.GTAG_OPENING_END, ">");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_START, "</g:");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_NAME, "if");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_END, ">");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_START, "</g:");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_NAME, "if");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_END, ">");
    }

    public void testHTMLandGTagCombination() {
        String text =
                "<html>"
                + "<g:if test=\"${t}\">"
                    + "<div class=\"e\">"
                        + "<g:renderErrors bean=\"${f.u}\" />"
                    + "</div>"
                + "</g:if>"
                + "<div class=\"s\">${e.s}</div>"
              + "</html>";
        TokenSequence<?> sequence = createTokenSequence(text);

        checkNext(sequence, GspTokenId.HTML, "<html>");
        checkNext(sequence, GspTokenId.GTAG_OPENING_START, "<g:");
        checkNext(sequence, GspTokenId.GTAG_OPENING_NAME, "if");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_NAME, " test=");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_VALUE, "\"");
        checkNext(sequence, GspTokenId.GSTRING_START, "${");
        checkNext(sequence, GspTokenId.GSTRING_CONTENT, "t");
        checkNext(sequence, GspTokenId.GSTRING_END, "}");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_VALUE, "\"");
        checkNext(sequence, GspTokenId.GTAG_OPENING_END, ">");
        checkNext(sequence, GspTokenId.HTML, "<div class=\"e\">");
        checkNext(sequence, GspTokenId.GTAG_OPENING_START, "<g:");
        checkNext(sequence, GspTokenId.GTAG_OPENING_NAME, "renderErrors");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_NAME, " bean=");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_VALUE, "\"");
        checkNext(sequence, GspTokenId.GSTRING_START, "${");
        checkNext(sequence, GspTokenId.GSTRING_CONTENT, "f.u");
        checkNext(sequence, GspTokenId.GSTRING_END, "}");
        checkNext(sequence, GspTokenId.GTAG_ATTRIBUTE_VALUE, "\"");
        checkNext(sequence, GspTokenId.GTAG_INDEPENDENT_END, " />");
        checkNext(sequence, GspTokenId.HTML, "</div>");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_START, "</g:");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_NAME, "if");
        checkNext(sequence, GspTokenId.GTAG_CLOSING_END, ">");
        checkNext(sequence, GspTokenId.HTML, "<div class=\"s\">");
        checkNext(sequence, GspTokenId.GSTRING_START, "${");
        checkNext(sequence, GspTokenId.GSTRING_CONTENT, "e.s");
        checkNext(sequence, GspTokenId.GSTRING_END, "}");
        checkNext(sequence, GspTokenId.HTML, "</div>");
        checkNext(sequence, GspTokenId.HTML, "</html>");
    }

    public void testGStringInHTML() {
        String text = "<a class=\"home\" href=\"${createLinkTo(dir:'')}\">Home</a>";
        TokenSequence<?> sequence = createTokenSequence(text);

        checkNext(sequence, GspTokenId.HTML, "<a class=\"home\" href=\"");
        checkNext(sequence, GspTokenId.GSTRING_START, "${");
        checkNext(sequence, GspTokenId.GSTRING_CONTENT, "createLinkTo(dir:'')");
        checkNext(sequence, GspTokenId.GSTRING_END, "}");
        checkNext(sequence, GspTokenId.HTML, "\">");
        checkNext(sequence, GspTokenId.HTML, "Home</a>");
    }

    public void testGspDirective() {
        String text =
                "<%@page import=\"org.grails.bookmarks.*\" %>";
        TokenSequence<?> sequence = createTokenSequence(text);

        checkNext(sequence, GspTokenId.PAGE_DIRECTIVE_START, "<%@");
        checkNext(sequence, GspTokenId.PAGE_DIRECTIVE_NAME, "page");
        checkNext(sequence, GspTokenId.PAGE_ATTRIBUTE_NAME, " import=");
        checkNext(sequence, GspTokenId.PAGE_ATTRIBUTE_VALUE, "\"org.grails.bookmarks.*\" ");
        checkNext(sequence, GspTokenId.PAGE_DIRECTIVE_END, "%>");
    }

    public void testCommentHTMLstyle() {
        String text = "<!--\n"
                    + "A b c\n"
                    + "-->\n";
        TokenSequence<?> sequence = createTokenSequence(text);
        checkNext(sequence, GspTokenId.COMMENT_HTML_STYLE_START, "<!--");
        checkNext(sequence, GspTokenId.COMMENT_HTML_STYLE_CONTENT, "\nA b c\n");
        checkNext(sequence, GspTokenId.COMMENT_HTML_STYLE_END, "-->");
    }

    public void testCommentGSPstyle() {
        String text = "%{--\n"
                    + "A b c\n"
                    + "--}%\n";
        TokenSequence<?> sequence = createTokenSequence(text);
        checkNext(sequence, GspTokenId.COMMENT_GSP_STYLE_START, "%{--");
        checkNext(sequence, GspTokenId.COMMENT_GSP_STYLE_CONTENT, "\nA b c\n");
        checkNext(sequence, GspTokenId.COMMENT_GSP_STYLE_END, "--}%");
    }

    public void testCommentJSPstyle() {
        String text = "<%--\n"
                    + "A b c\n"
                    + "--%>\n";
        TokenSequence<?> sequence = createTokenSequence(text);
        checkNext(sequence, GspTokenId.COMMENT_JSP_STYLE_START, "<%--");
        checkNext(sequence, GspTokenId.COMMENT_JSP_STYLE_CONTENT, "\nA b c\n");
        checkNext(sequence, GspTokenId.COMMENT_JSP_STYLE_END, "--%>");
    }

    public void testGspDefaultTempate() {
        String text = "<!--\n"
                    + "To change this template, choose Tools | Templates\n"
                    + "and open the template in the editor.\n"
                    + "-->\n"

                    + "<%@ page contentType=\"text/html;charset=UTF-8\" %>"

                    + "<html>"
                    +     "<body>"
                    +     "</body>"
                    + "</html>";

        TokenSequence<?> sequence = createTokenSequence(text);
        checkNext(sequence, GspTokenId.COMMENT_HTML_STYLE_START, "<!--");
        checkNext(sequence, GspTokenId.COMMENT_HTML_STYLE_CONTENT, "\n"
                    + "To change this template, choose Tools | Templates\n"
                    + "and open the template in the editor.\n");
        checkNext(sequence, GspTokenId.COMMENT_HTML_STYLE_END, "-->");
        checkNext(sequence, GspTokenId.WHITESPACE, "\n");
        checkNext(sequence, GspTokenId.PAGE_DIRECTIVE_START, "<%@");
        checkNext(sequence, GspTokenId.PAGE_DIRECTIVE_NAME, " page");
        checkNext(sequence, GspTokenId.PAGE_ATTRIBUTE_NAME, " contentType=");
        checkNext(sequence, GspTokenId.PAGE_ATTRIBUTE_VALUE, "\"text/html;charset=UTF-8\" ");
        checkNext(sequence, GspTokenId.PAGE_DIRECTIVE_END, "%>");
        checkNext(sequence, GspTokenId.HTML, "<html>");
        checkNext(sequence, GspTokenId.HTML, "<body>");
        checkNext(sequence, GspTokenId.HTML, "</body>");
        checkNext(sequence, GspTokenId.HTML, "</html>");
    }

    private TokenSequence createTokenSequence(String text) {
        return TokenHierarchy.create(text, GspLexerLanguage.getLanguage()).tokenSequence();
    }

    private void checkNext(TokenSequence<?> sequence, GspTokenId gspTokenId, String expectedContent) {
        assertTrue(sequence.moveNext());
        assertTokenEquals(sequence, gspTokenId, expectedContent, -1);
    }
}
