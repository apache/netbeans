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
package org.netbeans.modules.css.editor.csl;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.test.TestBase;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * @author marek.fukala@sun.com
 */
public class CssBracketCompleterTest extends TestBase {

    private Document doc;
    private JEditorPane pane;
    private BaseAction defaultKeyTypedAction;
    private BaseAction backspaceAction;
    private BaseAction deleteAction;

    public CssBracketCompleterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupEditor();
        CssParserResult.IN_UNIT_TESTS = true;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanUpEditor();
    }

    private void setupEditor() throws IOException {
        // this doesn't work since the JTextPane doesn't like our Kits since they aren't StyleEditorKits.
        //            Document doc = createDocument();
        //            JTextPane pane = new JTextPane((StyledDocument)doc);
        //            EditorKit kit = CloneableEditorSupport.getEditorKit("text/css");
        //            pane.setEditorKit(kit);

        File tmpFile = new File(getWorkDir(), "bracketCompleterTest.css");
        tmpFile.createNewFile();
        FileObject fo = FileUtil.createData(tmpFile);
        DataObject dobj = DataObject.find(fo);
        EditorCookie ec = dobj.getCookie(EditorCookie.class);
        this.doc = ec.openDocument();
        ec.open();
        this.pane = ec.getOpenedPanes()[0];

        this.defaultKeyTypedAction = (BaseAction) pane.getActionMap().get(NbEditorKit.defaultKeyTypedAction);
        this.backspaceAction = (BaseAction) pane.getActionMap().get(NbEditorKit.deletePrevCharAction);
        this.deleteAction = (BaseAction) pane.getActionMap().get(NbEditorKit.deleteNextCharAction);
    }

    private void cleanUpEditor() {
        this.pane.setVisible(false);
        this.pane = null;
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testCurlyBrackets() throws BadLocationException, IOException {
        //test pair autocomplete
        type('{');
        assertEquals("{}", getText());

        //test generated pair autodelete
        backspace();
        assertEquals("", getText());

        clear(doc);

        //test not generated pair NOT deleted
        doc.insertString(0, "{}", null);
        //                   01
        pane.setCaretPosition(1);
        backspace();
        assertEquals("}", getText());

        clear(doc);

        //test skipping closing curly bracket
        String text = "h1 { color: red; } ";
        //             0123456789012345678        
        doc.insertString(0, text , null);

        pane.setCaretPosition(17);
        type('}');

        assertEquals(text, getText()); //no change in the text
        assertEquals(18, pane.getCaretPosition()); //+1

    }
    
    //https://netbeans.org/bugzilla/show_bug.cgi?id=239848
    public void testQuotesNoTextPostfix() throws BadLocationException, IOException {
        doc.insertString(0, "div { background-image: url(); }", null);
        //                   0123456789012345678901234567890123456789
        //                   0         1         2         3

        //test pair autocomplete
        pane.setCaretPosition(28);
        type('"');
        assertEquals("div { background-image: url(\"\"); }", getText());

        //test generated pair autodelete
        backspace();
        assertEquals("div { background-image: url(); }", getText());

        type('"');
        assertEquals("div { background-image: url(\"\"); }", getText());
        
        delete();
        assertEquals("div { background-image: url(\"); }", getText());

    }
    
    //https://netbeans.org/bugzilla/show_bug.cgi?id=239848
    public void testQuotesTextPostfix() throws BadLocationException, IOException {
        doc.insertString(0, "div { background-image: url(myimg.png); }", null);
        //                   0123456789012345678901234567890123456789
        //                   0         1         2         3

        //no pair quote added if text after caret is not a symbol
        pane.setCaretPosition(28);
        type('"');
        assertEquals("div { background-image: url(\"myimg.png); }", getText());

        //no pair added either
        pane.setCaretPosition(38);
        type('"');
        assertEquals("div { background-image: url(\"myimg.png\"); }", getText());
        
    }
    
    public void testQuoteAutocompletion() throws BadLocationException, IOException {
        //test pair autocomplete
        clear(doc);

        //test skipping closing curly bracket
        String text = "h1 { color:  } ";
        //             0123456789012345678        
        doc.insertString(0, text , null);

        pane.setCaretPosition(12);
        type('"');

        assertEquals("h1 { color: \"\" } ", getText()); //no change in the text
        //             0123456789012345678        
        assertEquals(13, pane.getCaretPosition()); //+1

    }
    
    public void testLogicalRanges() throws ParseException {
        assertLogicalRanges("h1 { col|or: red; }", new int[][]{{5,10}, {5,15}, {0,18}});
        //                   01234567 89012345678

        assertLogicalRanges("h1 { color: re|d; }", new int[][]{{12,15}, {5,15}, {0,18}});
        //                   01234567890123 45678

        assertLogicalRanges("h1 {| color: red; }", new int[][]{{0,18}});
        //                   0123 456789012345678

        assertLogicalRanges("@media page { h1 { col|or: red; } }", new int[][]{{19, 24}, {19, 29}, {14, 32}, {0, 34}});
        //                   0123456789012345678901 234567890123456789
        //                   0         1         2          3
        
        assertLogicalRanges("h1, h|2 { color: red; }", new int[][]{{4, 6}, {0, 6}, {0, 22}});
        //                   01234 567890123456789012

        assertLogicalRanges("@me|dia page { h1 { } }", new int[][]{{0,22}});
        //                   012 34567890123456789012

    }


    // Bug 189711 -  Disabling Autocompletion Quotes and Tags
    public void testDoNotAutocompleteQuteInHtmlAttribute() throws DataObjectNotFoundException, IOException, BadLocationException {
        FileObject fo = getTestFile("testfiles/test.html");
        assertEquals("text/html", fo.getMIMEType());

        DataObject dobj = DataObject.find(fo);
        EditorCookie ec = dobj.getCookie(EditorCookie.class);
        Document document = ec.openDocument();
        ec.open();
        JEditorPane jep = ec.getOpenedPanes()[0];
        BaseAction type = (BaseAction) jep.getActionMap().get(NbEditorKit.defaultKeyTypedAction);
        //find the pipe
        String text = document.getText(0, document.getLength());

        int pipeIdx = text.indexOf('|');
        assertTrue(pipeIdx != -1);

        //delete the pipe
        document.remove(pipeIdx, 1);

        jep.setCaretPosition(pipeIdx);
        
        //type "
        ActionEvent ae = new ActionEvent(doc, 0, "\"");
        type.actionPerformed(ae, jep);

        //check the document content
        String beforeCaret = document.getText(pipeIdx, 2);
        assertEquals("\" ", beforeCaret);

    }

    private void assertLogicalRanges(String sourceText, int[][] expectedRangesLeaveToRoot) throws ParseException {
         //find caret position in the source text
        StringBuffer content = new StringBuffer(sourceText);

        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0 : "define caret position by pipe character in the document source!";

        //remove the pipe
        content.deleteCharAt(pipeOffset);
        sourceText = content.toString();

        //fatal parse error on such input, AST root == null
        Document document = getDocument(sourceText);
        Source source = Source.create(document);
        final Result[] _result = new Result[1];
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                _result[0] = resultIterator.getParserResult();
            }
        });

        Result result = _result[0];
        assertNotNull(result);
        assertTrue(result instanceof CssParserResult);

        CssParserResult cssResult = (CssParserResult)result;
        NodeUtil.dumpTree(cssResult.getParseTree());
        
        assertNotNull(cssResult.getParseTree());
        assertEquals(0, cssResult.getDiagnostics().size()); //no errors
        
        KeystrokeHandler handler = getPreferredLanguage().getKeystrokeHandler();
        assertNotNull(handler);

        List<OffsetRange> ranges = handler.findLogicalRanges(cssResult, pipeOffset);
        assertNotNull(ranges);

        String expectedRanges = expectedRangesToString(ranges);
        assertEquals("Unexpected number of logical ranges; existing ranges=" + expectedRanges , expectedRangesLeaveToRoot.length, ranges.size());

        for(int i = 0; i < ranges.size(); i++) {
            OffsetRange or = ranges.get(i);

            int expectedStart = expectedRangesLeaveToRoot[i][0];
            int expectedEnd = expectedRangesLeaveToRoot[i][1];

            assertEquals("Invalid logical range (" + or.toString() + ") start offset", expectedStart, or.getStart());
            assertEquals("Invalid logical range (" + or.toString() + ") end offset", expectedEnd, or.getEnd());
        }

    }
    
    
    //------- utilities -------

    private String expectedRangesToString(List<OffsetRange> ranges) {
        StringBuffer buf = new StringBuffer();
        for(OffsetRange range : ranges) {
            buf.append("{" + range.getStart() + ", " + range.getEnd() + "}, ");
        }
        return buf.toString();
    }

    private void type(char ch) {
        ActionEvent ae = new ActionEvent(doc, 0, ""+ch);
        defaultKeyTypedAction.actionPerformed(ae, pane);
    }

    private void backspace() {
        backspaceAction.actionPerformed(new ActionEvent(pane, 0, null));
    }
    
    private void delete() {
        deleteAction.actionPerformed(new ActionEvent(pane, 0, null));
    }

    private String getText() throws BadLocationException {
        return doc.getText(0, doc.getLength());
    }

    private void clear(Document doc) throws BadLocationException {
        doc.remove(0, doc.getLength());
    }
    
}
