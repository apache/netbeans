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
package org.netbeans.modules.css.editor.typinghooks;

import org.netbeans.modules.css.editor.test.TestBase;

/**
 *
 * @author marekfukala
 */
public class CssTypingHooksTest extends TestBase {

    public CssTypingHooksTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CssTypedTextInterceptor.inTest = true;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        CssTypedTextInterceptor.inTest = false;
    }

    protected Typing typing(String code) {
        return new Typing(getEditorKit("text/css"), code);
    }

    public void testAddCurlyBracePair() {
        Typing t = typing("div|");
        t.typeChar(' ');
        t.typeChar('{');
        t.assertDocumentTextEquals("div {|}");
    }

    public void testRemoveAddedCurlyBracePair() {
        Typing t = typing("div|");
        t.typeChar(' ');
        t.typeChar('{');
        t.assertDocumentTextEquals("div {|}");
        t.typeChar('\b');
        t.assertDocumentTextEquals("div |");
    }

    public void testAddSingleQuotePair() {
        Typing t = typing("div { color: |}");
        t.typeChar('\'');
        t.assertDocumentTextEquals("div { color: '|'}");
    }

    public void testRemoveAddedSingleQuotePair() {
        Typing t = typing("div { color: |}");
        t.typeChar('\'');
        t.assertDocumentTextEquals("div { color: '|'}");
        t.typeChar('\b');
        t.assertDocumentTextEquals("div { color: |}");
    }

    public void testAddDoubleQuotePair() {
        Typing t = typing("div { color: |}");
        t.typeChar('"');
        t.assertDocumentTextEquals("div { color: \"|\"}");
    }

    public void testRemoveAddedDoubleQuotePair() {
        Typing t = typing("div { color: |}");
        t.typeChar('"');
        t.assertDocumentTextEquals("div { color: \"|\"}");
        t.typeChar('\b');
        t.assertDocumentTextEquals("div { color: |}");
    }

    public void testAddRemovePairInEmbeddedCss() {
        Typing t = new Typing(getEditorKit("text/html"), "<div><style> .clz | </style></div>");
        t.typeChar('{');
        t.assertDocumentTextEquals("<div><style> .clz {|} </style></div>");
        t.typeChar('\b');
        t.assertDocumentTextEquals("<div><style> .clz | </style></div>");
    }

    public void testSmartEnter() {
        Typing t = typing("div {|}");
        t.typeChar('\n');
        t.assertDocumentTextEquals("div {\n    |\n}");
    }

    public void testSmartEnterInContext() {
        Typing t = typing(".x {\n"
                + "div {|}\n"
                + "}");
        t.typeChar('\n');
        t.assertDocumentTextEquals(".x {\n"
                + "    div {\n"
                + "        |\n"
                + "    }\n"
                + "}");
    }

    public void testSkipGeneratedBrace() {
        Typing t = typing("div |");
        t.typeChar('{');
        t.assertDocumentTextEquals("div {|}");
        t.typeChar('}');
        t.assertDocumentTextEquals("div {}|");
    }

    public void testSkipGeneratedQuote() {
        Typing t = typing("div |");
        t.typeChar('"');
        t.assertDocumentTextEquals("div \"|\"");
        t.typeChar('"');
        t.assertDocumentTextEquals("div \"\"|");
    }

    public void testReformatOnClosingBrace() {
        Typing t = typing("  div {\n|\n");
        t.typeChar('}');
        t.assertDocumentTextEquals("  div {\n  }|\n");
    }

}
