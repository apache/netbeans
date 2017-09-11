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
package org.netbeans.modules.html.editor.typinghooks;

import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.css.editor.indent.CssIndentTaskFactory;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.test.TestBase2;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;

/**
 *
 * @author marekfukala
 */
public class HtmlTypingHooksTest extends TestBase2 {

    public HtmlTypingHooksTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        HtmlTypedTextInterceptor.adjust_quote_type_after_eq = false;

        HtmlVersion.DEFAULT_VERSION_UNIT_TESTS_OVERRIDE = HtmlVersion.HTML41_TRANSATIONAL;
        AbstractIndenter.inUnitTestRun = true;

        CssIndentTaskFactory cssFactory = new CssIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/css"), cssFactory, CssTokenId.language());
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HtmlKit("text/html"), HTMLTokenId.language());
    }
    
    @Override
    protected void configureIndenters(Document document, Formatter formatter, boolean indentOnly, String mimeType) {
        // override it because I've already done in setUp()
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testHandleEmptyTagCloseSymbol() throws Exception {
        insertChar("<div^", '/', "<div/>^");
        insertChar("<div/>^", '>', "<div/>^");
    }

    public void testHandleEmptyTagCloseSymbolAfterWS() throws Exception {
        insertChar("<div ^", '/', "<div />^");
        insertChar("<div />^", '>', "<div />^");
    }

    public void testHandleEmptyTagCloseSymbolAfterAttribute() throws Exception {
        insertChar("<div align='center'^", '/', "<div align='center'/>^");
        insertChar("<div align='center'/>^", '>', "<div align='center'/>^");
    }

    //Bug 234153 - automatic tag close attempted inside attribute value
    public void testCloseTagSymbolAutocomplete() throws Exception {
        insertChar("<applet code=\"com^example/MyApplet.class\"/>", '/', "<applet code=\"com/^example/MyApplet.class\"/>");
    }

    public void testQuoteAutocompletionInHtmlAttribute() throws Exception {
        insertChar("<a href=\"javascript:bigpic(^)\">", '"', "<a href=\"javascript:bigpic(\"^)\">");
        insertChar("<a href=\"javascript:bigpic(\"^)\">", '"', "<a href=\"javascript:bigpic(\"\"^)\">");
    }

    public void testSkipClosingQuoteInEmptyAttr() throws Exception {
        insertChar("<a href=\"^\">", '"', "<a href=\"\"^>");
    }

    public void testSkipClosingQuoteInNonEmpty() throws Exception {
        insertChar("<a href=\"x^\">", '"', "<a href=\"x\"^>");
    }

    public void testSkipClosingQuoteInEmptyClassAndId() throws Exception {
        insertChar("<a class=\"^\">", '"', "<a class=\"\"^>");
        insertChar("<a id=\"^\">", '"', "<a id=\"\"^>");
    }

    public void testSkipClosingQuoteInNonEmptyClassAndId() throws Exception {
        insertChar("<a class=\"xx^\">", '"', "<a class=\"xx\"^>");
        insertChar("<a id=\"yy^\">", '"', "<a id=\"yy\"^>");
    }

    //XXX fixme - <div + "> => will autopopup the closing tag, but once completed,
    //the closing tag is not indented properly -- fix in HtmlTypedBreakInterceptor
    public void testDoubleQuoteAutocompleteAfterEQ() throws Exception {
        insertChar("<a href^", '=', "<a href=\"^\"");
        insertChar("<a href=\"^\"", 'v', "<a href=\"v^\"");
        insertChar("<a href=\"v^\"", 'a', "<a href=\"va^\"");
        insertChar("<a href=\"va^\"", 'l', "<a href=\"val^\"");
    }

    public void testDoubleQuoteAutocompleteAfterEQInCSSAttribute() throws Exception {
        insertChar("<a class^", '=', "<a class=\"^\"");
        insertChar("<a class=\"^\"", 'v', "<a class=\"v^\"");
        insertChar("<a class=\"v^\"", 'a', "<a class=\"va^\"");
        insertChar("<a class=\"va^\"", 'l', "<a class=\"val^\"");
    }

    public void testDoubleQuoteAfterQuotedClassAttribute() throws Exception {
        insertChar("<a class=\"val^", '"', "<a class=\"val\"^");
    }

    public void testDoubleQuoteAfterUnquotedClassAttribute() throws Exception {
        insertChar("<a class=val^", '"', "<a class=val\"^");
    }

    public void testSingleQuoteAutocompleteAfterEQ() throws Exception {
        insertChar("<a href=^", '\'', "<a href='^'");
        insertChar("<a href='^'", 'v', "<a href='v^'");
        insertChar("<a href='v^'", 'a', "<a href='va^'");
        insertChar("<a href='va^'", 'l', "<a href='val^'");
        insertChar("<a href='val^'", '\'', "<a href='val'^");
    }

    public void testQuoteChange() throws Exception {
        insertChar("<a href^", '=', "<a href=\"^\"");
        insertChar("<a href=\"^\"", '\'', "<a href='^'");
        insertChar("<a href='^'", 'v', "<a href='v^'");
        insertChar("<a href='v^'", 'a', "<a href='va^'");
        insertChar("<a href='va^'", 'l', "<a href='val^'");
        insertChar("<a href='val^'", '\'', "<a href='val'^");
    }

    public void testTypeSingleQuoteInUnquoteClassAttr() throws Exception {
        insertChar("<a class=^", 'v', "<a class=v^");
        insertChar("<a class=v^", 'a', "<a class=va^");
        insertChar("<a class=va^", 'l', "<a class=val^");
        insertChar("<a class=val^", '\'', "<a class=val'^");
    }

    public void testAutocompleteDoubleQuoteOnlyAfterEQ() throws Exception {
        insertChar("<a align^", '=', "<a align=\"^\"");
        insertChar("<a align=\"^\"", 'x', "<a align=\"x^\"");
        insertChar("<a align=\"x^\"", '"', "<a align=\"x\"^");
        insertChar("<a align=\"x\"^", '"', "<a align=\"x\"\"^");
    }

    public void testAutocompleteDoubleQuoteOnlyAfterEQInClass() throws Exception {
        insertChar("<a class^", '=', "<a class=\"^\"");
        insertChar("<a class=\"^\"", 'x', "<a class=\"x^\"");
    }

    public void testAutocompleteSingleQuoteOnlyAfterEQ() throws Exception {
        insertChar("<a align^", '=', "<a align=\"^\"");
        insertChar("<a align=\"^\"", '\'', "<a align='^'");
        insertChar("<a align='^'", 'x', "<a align='x^'");
        insertChar("<a align='x^'", '\'', "<a align='x'^");
        insertChar("<a align='x'^", '\'', "<a align='x''^");
    }

    public void testSwitchAutocompletedQuoteTypeClass() throws Exception {
        insertChar("<a class^", '=', "<a class=\"^\"");
        insertChar("<a class=\"^\"", '\'', "<a class='^'");
    }

    public void testDeleteAutocompletedQuote() throws Exception {
        insertChar("<a class^", '=', "<a class=\"^\"");
        deleteChar("<a class=\"^\"", "<a class=^");
    }

    public void testDeleteQuote() throws Exception {
        deleteChar("<a class=\"^\"", "<a class=^");
    }

    public void testDeleteQuoteWithWSAfter() throws Exception {
        deleteChar("<a class=\"^\" ", "<a class=^ ");
    }

    public void testDeleteSingleQuote() throws Exception {
        deleteChar("<a class='^'", "<a class=^");

        //but do not delete if there's a text after the caret
        deleteChar("<a class='^x'", "<a class=^x'");

    }

    public void testDoNotAutocompleteQuoteInValue() throws Exception {
        deleteChar("<a x=\"^test\"", "<a x=^test\"");
        //do not autocomplete in this case
        insertChar("<a x=^test\"", '"', "<a x=\"^test\"");

        //different quotes
        deleteChar("<a x=\"^test\"", "<a x=^test\"");
        //do not autocomplete in this case
        insertChar("<a x=^test\"", '\'', "<a x=\'^test\"");

        //no closing quote
        deleteChar("<a x=\"^test", "<a x=^test");
        //do not autocomplete in this case
        insertChar("<a x=^test", '\'', "<a x=\'^test");
    }

    public void testInClassDoNotAutocompleteQuoteInValue() throws Exception {
        deleteChar("<a class=\"^test\"", "<a class=^test\"");
        //do not autocomplete in this case
        insertChar("<a class=^test\"", '"', "<a class=\"^test\"");

        //different quotes
        deleteChar("<a class=\"^test\"", "<a class=^test\"");
        //do not autocomplete in this case
        insertChar("<a class=^test\"", '\'', "<a class=\'^test\"");

        //no closing quote
        deleteChar("<a class=\"^test", "<a class=^test");
        //do not autocomplete in this case
        insertChar("<a class=^test", '\'', "<a class=\'^test");
    }

    public void testAdjustQuoteTypeAfterEQ() throws Exception {
        HtmlTypedTextInterceptor.adjust_quote_type_after_eq = true;
        try {
            //default type
            assertEquals('"', HtmlTypedTextInterceptor.default_quote_char_after_eq);

            insertChar("<a class^", '=', "<a class=\"^\"");
            insertChar("<a class=\"^\"", '\'', "<a class='^'");
            //now should be switched to single quote type
            assertEquals('\'', HtmlTypedTextInterceptor.default_quote_char_after_eq);

            insertChar("<a class^", '=', "<a class='^'");
            insertChar("<a class='^'", '"', "<a class=\"^\"");
            //now should be switched back to the default double quote type
            assertEquals('"', HtmlTypedTextInterceptor.default_quote_char_after_eq);

        } finally {
            HtmlTypedTextInterceptor.adjust_quote_type_after_eq = false;
        }
    }

    public void testIndentLineInOpenTag() throws Exception {
        insertChar("<div>\n<div^\n</div>", '>',
                "<div>\n    <div>^\n</div>");

    }

    public void testIndentLineInCloseTag() throws Exception {
        insertChar("<div>\n    <div>\n        text\n        </div^\n</div>", '>',
                "<div>\n    <div>\n        text\n    </div>^\n</div>");
    }

    private void insertChar(String original, char insertText, String expected) throws Exception {
        insertChar(original, insertText, expected, null, false);
    }

}
