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
