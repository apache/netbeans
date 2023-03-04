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

package org.netbeans.modules.html.editor.completion;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author marekfukala
 */
public class HtmlCompletionProviderTest extends TestBase {

    public HtmlCompletionProviderTest(String testName) {
        super(testName);
    }

    public void testCheckOpenCompletion() throws BadLocationException {
        Document doc = createDocument();

        doc.insertString(0, "<", null);
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 1, "<"));

        doc.insertString(1, "div", null);
        assertFalse(HtmlCompletionProvider.checkOpenCompletion(doc, 4, "div"));

        doc.insertString(4, " ", null);
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 5, " "));

        doc.insertString(5, "/>", null);
        assertFalse(HtmlCompletionProvider.checkOpenCompletion(doc, 7, "/>"));

        doc.insertString(7, "</", null);
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 9, "</"));

        doc.insertString(9, "div> &", null);
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 15, "div> &"));

        //test end tag autocomplete
        doc.remove(0, doc.getLength());
        doc.insertString(0, "<div>", null);
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 5, ">"));

    }

    //Bug 203048 - code completion autopopup doesn't always work on space
    public void test_issue203048() throws BadLocationException {
        Document doc = createDocument();

        doc.insertString(0, "<div >", null);
        //                   012345
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 5, " "));
        
        doc = createDocument();
        doc.insertString(0, "<div    >", null);
        //                   012345
        assertTrue(HtmlCompletionProvider.checkOpenCompletion(doc, 6, " "));

    }

    //Bug 235048 - second tab activates suggestion in html editor 
    public void testDoNotOpenCompletionOnTabOrEnter() throws BadLocationException {
        Document doc = createDocument();

        doc.insertString(0, "<div >", null);
        //                   012345
        assertFalse(HtmlCompletionProvider.checkOpenCompletion(doc, 5, "    ")); //tab size 
        
    }
    
}
