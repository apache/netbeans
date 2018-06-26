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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.syntax;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jemmy.JemmyProperties;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Jindrich Sedek
 */
public class TagAlignmentTest extends J2eeTestCase {

    private static File projectDir;
    private boolean debug = false;
    private BaseDocument doc;
    private static boolean projectsOpened = false;

     public TagAlignmentTest() {
        super("IndentationTesting");
     }
     
     public TagAlignmentTest(String name) {
        super(name);
    }
   
    public static Test suite() {
        return createAllModulesServerSuite(J2eeTestCase.Server.ANY, TagAlignmentTest.class);
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
            IndentCasesTest.setIndent(5);
            projectsOpened = true;
            openFile("tagIndent.jsp");
            openFile("tagIndent.html");
            Thread.sleep(10000);
        }
    }

    public void testHTMLEndTag() throws Exception{
        testHTML(21, 21, "</td>", 21, 22);
    }

    public void testHTMLStartTag() throws Exception{
        testHTML(21, 21, "<td>", 21, 21);
    }
// issue 160651
//    public void testScript() throws Exception{
//        testHTML(5, 13, "<script>", 5, 17);
//    }
//
//    public void testStyle() throws Exception{
//        testHTML(5, 13, "<style>", 5, 16);
//    }

    public void testJSPEndTag() throws Exception{
        testJSP(25, 21, "</td>", 25, 22);
    }

    public void testJSPStartTag() throws Exception{
        testJSP(25, 21, "<td>", 25, 21);
    }

    private void testJSP(int lineNum, int offset, String text, int endLineNum, int endOffset) throws Exception {
        test("tagIndent.jsp", lineNum, offset, text, endLineNum, endOffset);
    }

    private void testHTML(int lineNum, int offset, String text, int endLineNum, int endOffset) throws Exception {
        test("tagIndent.html", lineNum, offset, text, endLineNum, endOffset);
    }

    private void test(String fileName, int lineNum, int offset, String text, int endLineNum, int endOffset) throws Exception {
        EditorOperator.closeDiscardAll();
        EditorOperator op = openFile(fileName);
        op.setCaretPositionToLine(lineNum);
        op.setCaretPositionRelative(offset - 1);
        if (debug) {
            Thread.sleep(3000); // to be visible ;-)
        }
        for (char c : text.toCharArray()) {
            op.typeKey(c);
        }
        CompletionTest.waitTypingFinished(doc);
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
        EditorOperator operator = new EditorOperator(fileName);
        return operator;
    }
}
