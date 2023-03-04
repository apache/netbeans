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
