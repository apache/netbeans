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
package org.netbeans.test.syntax;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JSpinner;
import junit.framework.Test;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JSpinnerOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Jindrich Sedek
 */
public class IndentCasesTest extends J2eeTestCase {

    private static File projectDir;
    private boolean debug = false;
    private BaseDocument doc;
    private static boolean projectsOpened = false;

     public IndentCasesTest() {
        super("IndentationTesting");
     }
     
     public IndentCasesTest(String name) {
        super(name);
    }
   
    public static Test suite() {
        return createAllModulesServerSuite(J2eeTestCase.Server.ANY, IndentCasesTest.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (!projectsOpened){
            JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
            File dataDir = getDataDir();
            projectDir = new File(dataDir, "IndentationTestProjects/IndentationTest");
            projectDir = projectDir.getAbsoluteFile();
            openProjects(projectDir.getAbsolutePath());
            resolveServer(projectDir.getName());
            Thread.sleep(10000);
            setIndent(2);
            projectsOpened = true;
            openFile("indentationTest.jsp");
            openFile("indentationTest.html");
            openFile("WEB-INF/tags/indentationTest.tag");
            Thread.sleep(10000);
        }
    }

    public static void setIndent(int number){
        OptionsOperator options = OptionsOperator.invoke();
        options.selectEditor();
        new JTabbedPaneOperator(options).selectPage("Formatting");
        JLabelOperator label = new JLabelOperator(options, "Number");
        JSpinner spinner = (JSpinner) label.getLabelFor();
        JSpinnerOperator spinnerOp = new JSpinnerOperator(spinner);
        spinnerOp.getNumberSpinner().scrollToValue(number);
        options.ok();
    }

    public void testJSPFirstLineIndent() throws Exception {
        testJSP(5, 1, 6, 1);
    }

    public void testJSPTagEndLine() throws Exception {
        testJSP(5, 7, 6, 3);
    }

    public void testJSPAttribute() throws Exception {
        testJSP(8, 15, 9, 11);
    }

    public void testJSPAttribute2() throws Exception {
        testJSP(8, 41, 9, 15);
    }

    public void testJSPSmartEnter() throws Exception {
        testJSP(22, 21, 23, 17);
    }

    public void testJSPOpenTagIndent() throws Exception {
        testJSP(23, 21, 24, 19);
    }

    public void testJSPEmbeddedCSS1() throws Exception {
        testJSP(10, 16, 11, 15);
    }

    public void testJSPEmbeddedCSS2() throws Exception {
        testJSP(11, 30, 12, 17);
    }

    public void testJSPScriptletStart() throws Exception {
        testJSP(29, 11, 30, 11);
    }

    public void testJSPScriptletIfBlock() throws Exception {
        testJSP(30, 19, 31, 11);
    }

    public void testJSPScriptletForBlock() throws Exception {
        testJSP(31, 44, 32, 15);
    }

    public void testJSPScriptletClosingBracket() throws Exception {
        testJSP(33, 14, 34, 13);
    }

    public void testHTMLFirstLineIndent() throws Exception {
        testHTML(1, 1, 2, 1);
    }

    public void testHTMLTagEndLine() throws Exception {
        testHTML(1, 7, 2, 3);
    }

    public void testHTMLAttribute() throws Exception {
        testHTML(4, 15, 5, 11);
    }

    public void testHTMLAttribute2() throws Exception {
        testHTML(4, 41, 5, 15);
    }

    public void testHTMLSmartEnter() throws Exception {
        testHTML(14, 21, 15, 17);
    }

    public void testHTMLOpenTagIndent() throws Exception {
        testHTML(19, 21, 20, 19);
    }

    public void testHTMLEmbeddedCSS1() throws Exception {
        testHTML(6, 16, 7, 15);
    }

    public void testHTMLEmbeddedCSS2() throws Exception {
        testHTML(7, 30, 8, 17);
    }

    public void testIssue120813() throws Exception {
        testTag(8, 15, 9, 5);
    }

    private void testJSP(int lineNum, int offset, int endLineNum, int endOffset) throws Exception {
        test("indentationTest.jsp", lineNum, offset, endLineNum, endOffset);
    }

    private void testTag(int lineNum, int offset, int endLineNum, int endOffset) throws Exception {
        test("WEB-INF/tags/indentationTest.tag", lineNum, offset, endLineNum, endOffset);
    }

    private void testHTML(int lineNum, int offset, int endLineNum, int endOffset) throws Exception {
        test("indentationTest.html", lineNum, offset, endLineNum, endOffset);
    }

    private void test(String fileName, int lineNum, int offset, int endLineNum, int endOffset) throws Exception {
        EditorOperator.closeDiscardAll();
        EditorOperator op = openFile(fileName);
        op.setCaretPositionToLine(lineNum);
        op.setCaretPositionRelative(offset - 1);
        if (debug) {
            Thread.sleep(3000); // to be visible ;-)
        }
        op.pressKey(KeyEvent.VK_ENTER);
        op.waitModified(true);
        int newPossition = op.txtEditorPane().getCaretPosition();
        int newLine = Utilities.getLineOffset(doc, newPossition) + 1;
        int newOffset = newPossition - Utilities.getRowStart(doc, newPossition);
        if (debug) {
            Thread.sleep(3000); // to be visible ;-)
        }
        assertEquals("FINAL POSSITION", endLineNum, newLine);
        assertEquals("FINAL POSSITION", endOffset - 1, newOffset);
    }
    
    private EditorOperator openFile(String fileName) throws DataObjectNotFoundException, IOException {
        File file = new File(new File(projectDir, "web"), fileName);
        DataObject dataObj = DataObject.find(FileUtil.toFileObject(file));
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);
        doc = (BaseDocument) ed.openDocument();
        ed.open();
        EditorOperator operator = new EditorOperator(file.getName());
        return operator;
    }

}
