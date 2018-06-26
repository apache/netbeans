/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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


