/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.editor.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.junit.Assert;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author marekfukala
 */
public class HtmlCompletionTestSupport {

    public static enum Match {

        EXACT, CONTAINS, DOES_NOT_CONTAIN, EMPTY, NOT_EMPTY;
    }

    public static void assertItems(Document doc, final String[] expectedItemsNames, final Match type, int expectedAnchor) throws BadLocationException, ParseException {
        String content = doc.getText(0, doc.getLength());

        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0 : "define caret position by pipe character in the document source!";

        //remove the pipe
        doc.remove(pipeOffset, 1);

        final HtmlCompletionQuery query = new HtmlCompletionQuery(doc, pipeOffset, false);
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);

        final HtmlParserResult[] result = new HtmlParserResult[1];
        Source source = Source.create(doc);
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                //try to find text/html parser result in embedded env.
                Snapshot snapshot = resultIterator.getSnapshot();
                if ("text/html".equals(snapshot.getMimeType())) {
                    result[0] = (HtmlParserResult) resultIterator.getParserResult();
                    return;
                }
                for (Embedding e : resultIterator.getEmbeddings()) {
                    run(resultIterator.getResultIterator(e));
                }
            }
        });

        Assert.assertNotNull(result[0]);

//        assertSame(getExpectedVersion(), result[0].getSyntaxAnalyzerResult().getHtmlVersion());

        final AtomicReference<HtmlCompletionQuery.CompletionResult> result_ref = new AtomicReference<>();
        doc.render(new Runnable() {

            @Override
            public void run() {
                result_ref.set(query.query(result[0]));
            }
            
        });
         HtmlCompletionQuery.CompletionResult completionResult = result_ref.get();

        if (type != Match.EMPTY) {
            Assert.assertNotNull("null completion query result", completionResult);
        }

        if (expectedItemsNames.length == 0 && completionResult == null) {
            //result may be null if we do not expect any result, nothing to test then
            return;
        }

        Collection<? extends CompletionItem> items = completionResult.getItems();
        Assert.assertNotNull(items);

        if (expectedAnchor > 0) {
            Assert.assertEquals(expectedAnchor, completionResult.getAnchor());
        }

        try {
            assertCompletionItemNames(expectedItemsNames, items, type);
        } catch (AssertionError e) {
            for (CompletionItem item : items) {
                System.out.println(((HtmlCompletionItem) item).getItemText());
            }
            throw e;
        }

    }

    public static void assertCompletedText(Document doc, String documentText, String itemToCompleteName, String expectedText) throws BadLocationException, ParseException {
        StringBuilder content = new StringBuilder(documentText);
        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0 : "define caret position by pipe character in the document source!";
        //remove the pipe
        content.deleteCharAt(pipeOffset);

        StringBuilder expectedContent = new StringBuilder(expectedText);
        final int expectedPipeOffset = expectedContent.indexOf("|");
        assert expectedPipeOffset >= 0 : "define caret position by pipe character in the expected text!";
        //remove the pipe
        expectedContent.deleteCharAt(expectedPipeOffset);

        doc.remove(0, doc.getLength());
        doc.insertString(0, content.toString(), null);

        HtmlCompletionQuery query = new HtmlCompletionQuery(doc, pipeOffset, false);
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);
        component.getCaret().setDot(pipeOffset);

        final HtmlParserResult[] result = new HtmlParserResult[1];
        Source source = Source.create(doc);
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                result[0] = (HtmlParserResult) resultIterator.getParserResult();
            }
        });

        Assert.assertNotNull(result[0]);

        HtmlCompletionQuery.CompletionResult completionResult = query.query(result[0]);

        Assert.assertNotNull(completionResult);
        Collection<? extends CompletionItem> items = completionResult.getItems();
        Assert.assertNotNull(items);

        CompletionItem item = null;
        for (CompletionItem htmlci : items) {
            String itemText = ((HtmlCompletionItem) htmlci).getItemText();
            if (itemToCompleteName.charAt(0) == '/') {
                //end tag should be completed
                if (htmlci instanceof HtmlCompletionItem.EndTag) {
                    if (itemText.equals(itemToCompleteName.substring(1))) {
                        item = htmlci; //found
                        break;
                    }
                } else {
                    continue;
                }
            }

            if (itemText.equals(itemToCompleteName)) {
                item = htmlci; //found
                break;
            }
        }

        Assert.assertNotNull(item);
        Assert.assertTrue(item instanceof HtmlCompletionItem);

        item.defaultAction(component); //complete

        Assert.assertEquals(expectedContent.toString(), doc.getText(0, doc.getLength()));
//        assertEquals(expectedPipeOffset, component.getCaret().getDot());

    }

    public static void assertCompletionItemNames(String[] expected, Collection<? extends CompletionItem> ccresult, Match type) {
        Collection<String> real = new ArrayList<>();
        for (CompletionItem ccp : ccresult) {
            //check only html items
            if (ccp instanceof HtmlCompletionItem) {
                HtmlCompletionItem htmlci = (HtmlCompletionItem) ccp;
                real.add(htmlci.getItemText());
            }
        }
        Collection<String> exp = new ArrayList<>(Arrays.asList(expected));

        if (type == Match.EXACT) {
            Assert.assertEquals(exp, real);
        } else if (type == Match.CONTAINS) {
            exp.removeAll(real);
            Assert.assertEquals(exp, Collections.EMPTY_LIST);
        } else if (type == Match.EMPTY) {
            Assert.assertEquals(0, real.size());
        } else if (type == Match.NOT_EMPTY) {
            Assert.assertTrue(real.size() > 0);
        } else if (type == Match.DOES_NOT_CONTAIN) {
            int originalRealSize = real.size();
            real.removeAll(exp);
            Assert.assertEquals(originalRealSize, real.size());
        }

    }
}
