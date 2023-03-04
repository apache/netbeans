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

import java.awt.Dialog;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Jindrich Sedek
 */
public class CommentActionTest extends J2eeTestCase {

    private static final Boolean GENERATE_GOLDEN_FILES = false;
    private static final String PROJECT_DIR_NAME = "CommentTestProject";
    private static boolean firstRun = true;

    public CommentActionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (firstRun) {
            File projectDir = new File(getDataDir(), PROJECT_DIR_NAME);
            openProjects(projectDir.getAbsolutePath());
            firstRun = false;
            Dialog d = JDialogOperator.getTopModalDialog();
            while (d != null){
                d.setVisible(false);
                d.dispose();
                d = JDialogOperator.getTopModalDialog();
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (GENERATE_GOLDEN_FILES) {
            CompletionTest.generateGoldenFiles(this);
        } else {
            compareReferenceFiles();
        }
    }

    public static Test suite() {
        return createAllModulesServerSuite(J2eeTestCase.Server.ANY, CommentActionTest.class);
    }

    public void testHTMLSection() throws Exception {
        doSectionTest("testHTML.html", 7, 10);
    }

    public void testHTMLOneLine() throws Exception {
        doLineTest("testHTML.html", 8, 1);
    }

    public void testHTMLInsideOneLine() throws Exception {
        doLineTest("testHTML.html", 8, 10);
    }

    public void testJSPSection() throws Exception {
        doSectionTest("testJSP.jsp", 3, 4);
    }

    public void testJSPOneLine() throws Exception {
        doLineTest("testJSP.jsp", 10, 1);
    }

    public void testJSPInsideOneLine() throws Exception {
        doLineTest("testJSP.jsp", 10, 10);
    }

    public void testCSSSection() throws Exception {
        doSectionTest("testCSS.css", 20, 31);
    }

    public void testCSSOneLine() throws Exception {
        doLineTest("testCSS.css", 24, 1);
    }

    public void testCSSInsideOneLine() throws Exception {
        doLineTest("testCSS.css", 9, 10);
    }

    private void doSectionTest(String fileName, int beginLineNubmer, int endLineNumber) throws DataObjectNotFoundException {
        EditorOperator op = openFile(fileName);
        op.select(beginLineNubmer, endLineNumber);
        op.pushKey(KeyEvent.VK_SLASH, InputEvent.CTRL_MASK);
        op.waitModified(true);
        ref(op.getText());
        EditorOperator.closeDiscardAll();
    }

    private void doLineTest(String fileName, int lineNumber, int column) throws DataObjectNotFoundException {
        EditorOperator op = openFile(fileName);
        op.setCaretPosition(lineNumber, column);
        op.pushKey(KeyEvent.VK_SLASH, InputEvent.CTRL_MASK);
        op.waitModified(true);
        ref(op.getText());
        EditorOperator.closeDiscardAll();
    }

    private EditorOperator openFile(String fileName) throws DataObjectNotFoundException {
        FileObject project = FileUtil.toFileObject(new File(getDataDir(), PROJECT_DIR_NAME));
        FileObject file = project.getFileObject("web").getFileObject(fileName);
        DataObject dataObj = DataObject.find(file);
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);
        ed.open();
        return new EditorOperator(fileName);
    }
}


