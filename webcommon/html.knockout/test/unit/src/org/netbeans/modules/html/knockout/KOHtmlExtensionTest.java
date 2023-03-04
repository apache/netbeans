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
package org.netbeans.modules.html.knockout;

import javax.swing.text.BadLocationException;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.html.editor.completion.HtmlCompletionTestSupport;
import org.netbeans.modules.html.editor.completion.HtmlCompletionTestSupport.Match;
import org.netbeans.modules.html.editor.gsf.HtmlLanguage;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class KOHtmlExtensionTest extends CslTestBase {
    
    public KOHtmlExtensionTest(String name) {
        super(name);
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/html";
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new HtmlLanguage();
    }
    
    public void testCompletionWithPrefix() {
        assertCC("<div data-bind=\"t|", Match.EXACT, "text", "textInput", "template");
        assertCC("<div data-bind=\"tex|", Match.EXACT, "text", "textInput");
        assertCC("<div data-bind=\"text|", Match.EXACT, "text", "textInput");
        assertCC("<div data-bind=\"text|:value", Match.EXACT, "text", "textInput");
        assertCC("<div data-bind=\"text:value, v|", Match.EXACT, "visible", "value");
    }
    
    public void testCompletionWithoutPrefix() {
        assertCC("<div data-bind=\"|", Match.CONTAINS, "text");
        assertCC("<div data-bind=\"  |", Match.CONTAINS, "text");
        assertCC("<div data-bind=\"text:value,|", Match.CONTAINS, "text");
        assertCC("<div data-bind=\"text:value, |", Match.CONTAINS, "text");
    }
    
    private void assertCC(String documentText, Match type, String... expectedItemsNames)  {
        try {
            HtmlCompletionTestSupport.assertItems(getDocument(documentText), expectedItemsNames, type, -1);
        } catch (BadLocationException | ParseException ex) {
            throw new AssertionFailedErrorException(ex);
        }
    }
    
}