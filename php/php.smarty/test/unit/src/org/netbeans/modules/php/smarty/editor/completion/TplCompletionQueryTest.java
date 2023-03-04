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
package org.netbeans.modules.php.smarty.editor.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import junit.framework.AssertionFailedError;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.php.smarty.editor.TplDataLoader;
import org.netbeans.modules.php.smarty.editor.gsf.TplLanguage;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplCompletionQueryTest extends CslTestBase {

    public TplCompletionQueryTest(String name) {
        super(name);
    }

    // 
    public void testTagAttributeValues() throws Exception {
        // Original test fails because completion data were not donated.
        // assertItems("{|", arr("append", "if", "section"), Match.CONTAINS);
        assertItems("{|", arr(), Match.EMPTY);
    }

    public void testIssue22376() throws Exception {
        // Original test fails because completion data were not donated.
        // assertItems("{|\n", arr("append", "if", "section"), Match.CONTAINS);
        assertItems("{|\n", arr(), Match.EMPTY);
    }

    protected void assertItems(String documentText, final String[] expectedItemsNames, final Match type) throws Exception {
        assertItems(documentText, expectedItemsNames, type, -1);
    }

    protected void assertItems(String documentText, final String[] expectedItemsNames, final Match type, int expectedAnchor) throws Exception {
        assertItems(getDocument(documentText), expectedItemsNames, type, expectedAnchor);
    }

    protected void assertItems(Document doc, final String[] expectedItemsNames, final Match type, int expectedAnchor) throws Exception {
        String content = doc.getText(0, doc.getLength());

        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0 : "define caret position by pipe character in the document source!";

        //remove the pipe
        doc.remove(pipeOffset, 1);

        TplCompletionQuery query = new TplCompletionQuery(doc);
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);

        TplCompletionQuery.CompletionResult completionResult = query.query();

        if (type != Match.EMPTY) {
            assertNotNull("null completion query result", completionResult);
        }

        if (expectedItemsNames.length == 0 && completionResult == null) {
            //result may be null if we do not expect any result, nothing to test then
            return;
        }

        ArrayList<TplCompletionItem> items = completionResult.getFunctions();
        items.addAll(completionResult.getVariableModifiers());
        assertNotNull(items);

        try {
            assertCompletionItemNames(expectedItemsNames, items, type);
        } catch (AssertionFailedError e) {
            for (CompletionItem item : items) {
                System.out.println(((TplCompletionItem) item).getItemText());
            }
            throw e;
        }
    }

    private void assertCompletionItemNames(String[] expected, Collection<? extends CompletionItem> ccresult, Match type) {
        Collection<String> real = new ArrayList<String>();
        for (CompletionItem ccp : ccresult) {
            //check only html items
            if (ccp instanceof TplCompletionItem) {
                TplCompletionItem tplci = (TplCompletionItem) ccp;
                real.add(tplci.getItemText());
            }
        }
        Collection<String> exp = new ArrayList<String>(Arrays.asList(expected));

        if (type == Match.EXACT) {
            assertEquals(exp, real);
        } else if (type == Match.CONTAINS) {
            exp.removeAll(real);
            assertEquals(exp, Collections.EMPTY_LIST);
        } else if (type == Match.EMPTY) {
            assertEquals(0, real.size());
        } else if (type == Match.NOT_EMPTY) {
            assertTrue(real.size() > 0);
        } else if (type == Match.DOES_NOT_CONTAIN) {
            int originalRealSize = real.size();
            real.removeAll(exp);
            assertEquals(originalRealSize, real.size());
        }
    }

    public static enum Match {
        EXACT, CONTAINS, DOES_NOT_CONTAIN, EMPTY, NOT_EMPTY;
    }

    protected String[] arr(String... args) {
        return args;
    }

    @Override
    protected String getPreferredMimeType() {
        return TplDataLoader.MIME_TYPE;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new TplLanguage();
    }
}
