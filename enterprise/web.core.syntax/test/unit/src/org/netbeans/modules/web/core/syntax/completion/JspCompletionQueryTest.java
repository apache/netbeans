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
package org.netbeans.modules.web.core.syntax.completion;

import org.netbeans.modules.web.core.syntax.completion.api.JspCompletionItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.html.editor.NbReaderProvider;
import org.netbeans.modules.web.core.syntax.JspKit;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.test.web.core.syntax.TestBase;
import org.openide.filesystems.FileObject;

/**Html completion test
 * This class extends TestBase class which provides access to the html editor module layer
 *
 * @author Marek Fukala
 */
public class JspCompletionQueryTest extends TestBase {

    private static final String DATA_DIR_BASE = "testfiles/completion/";

    public static enum Match {
        EXACT, CONTAINS, DOES_NOT_CONTAIN, EMPTY, NOT_EMPTY;
    }

    public JspCompletionQueryTest() throws IOException, BadLocationException {
        super("JspCompletionQueryTest");
    }

    //test methods -----------
//    public void testIndexHtml() throws IOException, BadLocationException {
//        testCompletionResults("index.html");
//    }
//
//    public void testNetbeansFrontPageHtml() throws IOException, BadLocationException {
//        testCompletionResults("netbeans.org.html");
//    }

//    public void testJspTags() {
//        assertItems("<|", arr("jsp:useBean"), Match.CONTAINS);
//        assertItems("<x|", arr("jsp:useBean"), Match.DOES_NOT_CONTAIN);
//        assertItems("<j|", arr("jsp:useBean"), Match.CONTAINS);
//        assertItems("<jsp:|", arr("jsp:useBean"), Match.CONTAINS, 1);
//        assertItems("<jsp:useBean|", arr("jsp:useBean"), Match.CONTAINS, 1);
//    }
//
//    public void testJspEndTags() {
//        //           01234567
//        assertItems("<jsp:useBean></|", arr("/jsp:useBean"), Match.CONTAINS);
//        assertItems("<jsp:useBean></jsp:|", arr("/jsp:useBean"), Match.CONTAINS);
//    }

    public void testJspCommentDelimiterCompletion() throws BadLocationException {
        assertItems("|", arr(), Match.EMPTY);
        assertItems("<|", arr("<%--"), Match.CONTAINS);

        assertCompletedText("<|", "<%--", "<%--|");
    }

    public void testJspCommentEndDelimiterCompletion() throws BadLocationException {
        //unfinished comment
        assertItems("<%-- |", arr("--%>"), Match.DOES_NOT_CONTAIN);
        assertItems("<%-- -|", arr("--%>"), Match.EXACT);
        assertItems("<%-- --|", arr("--%>"), Match.EXACT);
        assertItems("<%-- --%|", arr("--%>"), Match.EXACT);

        //out of comment
        assertItems("<%-- --%> |", arr("--%>"), Match.DOES_NOT_CONTAIN);

        //inside comment
        assertItems("<%-- -| --%> ", arr("--%>"), Match.EXACT);
        assertItems("<%-- --| --%> |", arr("--%>"), Match.EXACT);
        assertItems("<%-- --%| --%> |", arr("--%>"), Match.EXACT);

        //inside comment with eol
        assertItems("<%-- -|\n --%> |", arr("--%>"), Match.EXACT);
        assertItems("<%-- --%|\n --%> |", arr("--%>"), Match.EXACT);

    }


    //helper methods ------------
    private void assertItems(String documentText, final String[] expectedItemsNames, final Match type)  {
        assertItems(documentText, expectedItemsNames, type, -1);
    }

    private void assertItems(String documentText, final String[] expectedItemsNames, final Match type, int expectedAnchor) {
        StringBuffer content = new StringBuffer(documentText);

        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0 : "define caret position by pipe character in the document source!";

        //remove the pipe
        content.deleteCharAt(pipeOffset);
        Document doc = createDocument(content.toString());
        assertNotNull(doc); 

        JspCompletionQuery query = JspCompletionQuery.instance();
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);
        JspCompletionQuery.CompletionResultSet result = new JspCompletionQuery.CompletionResultSet();
                
        query.query(result, component, pipeOffset);
        assertNotNull(result);

        List<CompletionItem> items = result.getItems();
        assertNotNull(items);
        
        if(expectedAnchor > 0) {
            assertEquals(expectedAnchor, result.getAnchor());
        }

        assertCompletionItemNames(expectedItemsNames, items, type);

    }

    private void assertCompletedText(String documentText, String itemToCompleteName, String expectedText) throws BadLocationException {
        StringBuffer content = new StringBuffer(documentText);
        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0 : "define caret position by pipe character in the document source!";
        //remove the pipe
        content.deleteCharAt(pipeOffset);

        StringBuffer expectedContent = new StringBuffer(expectedText);
        final int expectedPipeOffset = expectedContent.indexOf("|");
        assert expectedPipeOffset >= 0 : "define caret position by pipe character in the expected text!";
        //remove the pipe
        expectedContent.deleteCharAt(expectedPipeOffset);

        Document doc = createDocument(content.toString());
 
        JspCompletionQuery query = JspCompletionQuery.instance();
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);
        component.getCaret().setDot(pipeOffset);
        JspCompletionQuery.CompletionResultSet result = new JspCompletionQuery.CompletionResultSet();

        query.query(result, component, pipeOffset);
        assertNotNull(result);
        List<CompletionItem> items = result.getItems();
        assertNotNull(items);

        CompletionItem item = null;
        for (CompletionItem ci : items) {
            if (ci instanceof JspCompletionItem) {
                JspCompletionItem htmlci = (JspCompletionItem) ci;
                if(htmlci.getItemText().equals(itemToCompleteName)) {
                    item = ci; //found
                    break;
                }
            }
        }

        assertNotNull(item);
//        assertTrue(item instanceof HtmlCompletionItem);

        item.defaultAction(component); //complete

        assertEquals(expectedContent.toString(), doc.getText(0, doc.getLength()));
//        assertEquals(expectedPipeOffset, component.getCaret().getDot());

    }

    private void assertCompletionItemNames(String[] expected, List<CompletionItem> ccresult, Match type) {
        Collection<String> real = new ArrayList<String>();
        for (CompletionItem ccp : ccresult) {
            //check only html items
            if (ccp instanceof JspCompletionItem) {
                JspCompletionItem htmlci = (JspCompletionItem) ccp;
                real.add(htmlci.getItemText());
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

    private String[] arr(String... args) {
        return args;
    }
 
    private void testCompletionResults(String testFile) throws IOException, BadLocationException {
        FileObject source = getTestFile(DATA_DIR_BASE + testFile);
        BaseDocument doc = getDocument(source);
        JspCompletionQuery query = JspCompletionQuery.instance();
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);

        StringBuffer output = new StringBuffer();
        for (int i = 0; i < doc.getLength(); i++) {
            JspCompletionQuery.CompletionResultSet result = new JspCompletionQuery.CompletionResultSet();
            
            query.query(result, component, i);
            if (result != null) {
                List<CompletionItem> items = result.getItems();
                output.append(i + ":");
                output.append('[');
                Iterator<CompletionItem> itr = items.iterator();
                while (itr.hasNext()) {
                    CompletionItem ci = itr.next();
                    if (ci instanceof JspCompletionItem) {
                        //test only html completion items
                        JspCompletionItem htmlci = (JspCompletionItem) ci;
                        output.append(htmlci.getItemText());
                        if (itr.hasNext()) {
                            output.append(',');
                        }
                    }
                }
                output.append(']');
                output.append('\n');
            }
        }

        assertDescriptionMatches(source, output.toString(), false, ".pass", true);

    }
}
