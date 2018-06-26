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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
