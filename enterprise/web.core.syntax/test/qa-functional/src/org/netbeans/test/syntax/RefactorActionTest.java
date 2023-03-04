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

import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.test.web.RenameDialogOperator;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Jindrich Sedek
 */
public class RefactorActionTest extends J2eeTestCase {

    private static final String PROJECT_DIR_NAME = "RefactorTestProject";
    private static boolean firstRun = true;
    private static boolean unrelatedReferencesHTMLhasRun = false;
    private static boolean unrelatedReferencesCSShasRun = false;

    public RefactorActionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (firstRun) {
            File projectDir = new File(getDataDir(), PROJECT_DIR_NAME);
            openProjects(projectDir.getAbsolutePath());
            resolveServer(projectDir.getName());
            firstRun = false;
        }
        waitScanFinished();
    }

    @Override
    protected void tearDown() throws Exception {
        EditorOperator.closeDiscardAll();
        super.tearDown();
    }

    public static Test suite() {
        return createAllModulesServerSuite(J2eeTestCase.Server.ANY, RefactorActionTest.class);
    }

    public void testRefactorIDFromCSS() throws Exception {
        doRefactoring("metamorph_orange", "style.css", "#menu", "#menicko");
    }

    public void testRefactorIdFromHTML() throws Exception {
        doRefactoring("metamorph_orange", "index.html", "#logo", "#znak_firmy");
        
        EditorOperator index2 = openFile("wrestling/index2.html");
        assertTrue("unrelated not refactored", index2.getText().contains("logo"));
        assertFalse("unrelated not refactored", index2.getText().contains("simple_log"));
    }

    public void testRefactorClassFromCSS() throws Exception {
        doRefactoring("metamorph_orange", "style.css", ".dateleft", ".levydatum");
    }

    public void testRefactorClassFromHTML() throws Exception {
        doRefactoring("metamorph_orange", "index.html", ".dateright", ".pravydatum");
    }

    public void testIssue180213() throws Exception {
        doRefactoring("wrestling", "style.css", "#logo", "#simple_log");

        EditorOperator index2 = openFile("wrestling/index2.html");
        assertFalse("second html refactored", index2.getText().contains("logo"));
        assertTrue("second html refactored", index2.getText().contains("simple_log"));
    }

    public void testUnrelatedReferencesFromHTML() throws Exception {
        if (!RefactorActionTest.unrelatedReferencesCSShasRun) {
            doRefactoring("metamorph_orange", "index.html", "#header", "#hlavicka", false, true);

            EditorOperator index = openFile("wrestling/index.html");
            assertFalse("unrelated html refactored", index.getText().contains("header"));
            assertTrue("unrelated html refactored", index.getText().contains("hlavicka"));

            EditorOperator index2 = openFile("wrestling/index2.html");
            assertFalse("unrelated html refactored", index2.getText().contains("header"));
            assertTrue("unrelated html refactored", index2.getText().contains("hlavicka"));
        } else {
            doRefactoring("metamorph_orange", "index.html", "#zahlavi", "#hlavicka", false, true);

            EditorOperator index = openFile("wrestling/index.html");
            assertFalse("unrelated html refactored", index.getText().contains("zahlavi"));
            assertTrue("unrelated html refactored", index.getText().contains("hlavicka"));

            EditorOperator index2 = openFile("wrestling/index2.html");
            assertFalse("unrelated html refactored", index2.getText().contains("zahlavi"));
            assertTrue("unrelated html refactored", index2.getText().contains("hlavicka"));
        }

        RefactorActionTest.unrelatedReferencesHTMLhasRun = true;
    }

    public void testUnrelatedReferencesFromCSS() throws Exception {
        if (RefactorActionTest.unrelatedReferencesHTMLhasRun) {
            doRefactoring("metamorph_orange", "style.css", "#hlavicka", "#zahlavi", false, true);

            EditorOperator index = openFile("wrestling/index.html");
            assertFalse("unrelated html refactored", index.getText().contains("hlavicka"));
            assertTrue("unrelated html refactored", index.getText().contains("zahlavi"));

            EditorOperator index2 = openFile("wrestling/index2.html");
            assertFalse("unrelated html refactored", index2.getText().contains("hlavicka"));
            assertTrue("unrelated html refactored", index2.getText().contains("zahlavi"));
        } else {
            doRefactoring("metamorph_orange", "style.css", "#header", "#zahlavi", false, true);

            EditorOperator index = openFile("wrestling/index.html");
            assertFalse("unrelated html refactored", index.getText().contains("header"));
            assertTrue("unrelated html refactored", index.getText().contains("zahlavi"));

            EditorOperator index2 = openFile("wrestling/index2.html");
            assertFalse("unrelated html refactored", index2.getText().contains("header"));
            assertTrue("unrelated html refactored", index2.getText().contains("zahlavi"));
        }
        RefactorActionTest.unrelatedReferencesCSShasRun = true;
    }

    public void testIssue180215() throws Exception {// cycle reference
        doRefactoring("wrestling", "newcss.css", "root", "leaf", true, false);
    }
    
    private void doRefactoring(String directory, String refactoredFileName, String oldValue, String newValue) throws Exception {
        doRefactoring(directory, refactoredFileName, oldValue, newValue, false, false);
    }

    private void doRefactoring(String directory, String refactoredFileName, String oldValue, String newValue, boolean onlyThisCSSFile, boolean refactorUnrelated) throws Exception {
        EditorOperator eo = openFile(directory + "/" + refactoredFileName);
        String searchedValue = oldValue;
        if (refactoredFileName.endsWith("html")) {
            searchedValue = filterForHTML(searchedValue);
        }
        eo.setCaretPosition(searchedValue, 0, false);
        new ActionNoBlock("Refactor|Rename...", null).performMenu();
        RenameDialogOperator renameOperator = new RenameDialogOperator();
        assertEquals(oldValue, renameOperator.getNewName());
        renameOperator.setNewName(newValue);
        renameOperator.setEnabledUnrelatedOccurences(refactorUnrelated);
        renameOperator.refactor();
        Thread.sleep(5000);
        eo.close(true);

        EditorOperator css;
        if (onlyThisCSSFile) {
            css = openFile(directory + "/" + refactoredFileName);
        } else {
            css = openFile(directory + "/style.css");
        }
        assertFalse("css refactored: \n" + css.getText(), css.contains(oldValue));
        assertTrue("css refactored: \n" + css.getText(), css.contains(newValue));

        if (!onlyThisCSSFile) {
            EditorOperator index = openFile(directory + "/index.html");
            assertFalse("html refactored: \n" + index.getText(), index.contains(filterForHTML(oldValue)));
            assertTrue("html refactored: \n" + index.getText(), index.contains(filterForHTML(newValue)));
        }
    }

    private String filterForHTML(String value) {
        if (value.startsWith(".") || value.startsWith("#")) {
            return value.substring(1);
        }
        return value;
    }

    private EditorOperator openFile(String fileName) throws DataObjectNotFoundException {
        FileObject project = FileUtil.toFileObject(new File(getDataDir(), PROJECT_DIR_NAME));
        FileObject file = project.getFileObject("web").getFileObject(fileName);
        DataObject dataObj = DataObject.find(file);
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);
        ed.open();
        return new EditorOperator(file.getNameExt());
    }
}


