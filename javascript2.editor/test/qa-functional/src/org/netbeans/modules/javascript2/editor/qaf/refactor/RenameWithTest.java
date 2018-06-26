/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.qaf.refactor;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha
 */
public class RenameWithTest extends RenameTest {

    private String projectName = "completionTest";
    private static String fileContent;

    public RenameWithTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(RenameWithTest.class).addTest(
                "openProject",
                "testRenameWithL10",
                "testRenameWithL11",
                "testRenameWithL24",
                "testRenameWithL48",
                "testRenameWithL49",
                "testRenameWithL62",
                "testRenameWithL64",
                "testRenameWithL76",
                "testRenameWithL77",
                "testRenameWithL82",
                "testRenameWithL86",
                "testRenameWithL87",
                "testRenameWithL88",
                "testRenameWithL93"
                ).enableModules(".*").clusters(".*"));
    }

    public void testRenameWithL10() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 10);
        endTest();
    }

    public void testRenameWithL11() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 11);
        endTest();
    }

    public void testRenameWithL24() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 24);
        endTest();
    }

    public void testRenameWithL48() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 48);
        endTest();
    }

    public void testRenameWithL49() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 49);
        endTest();
    }

    public void testRenameWithL62() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 62);
        endTest();
    }

    public void testRenameWithL64() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 64);
        endTest();
    }

    public void testRenameWithL76() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 76);
        endTest();
    }

    public void testRenameWithL77() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 77);
        endTest();
    }

    public void testRenameWithL82() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 82);
        endTest();
    }

    public void testRenameWithL86() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 86);
        endTest();
    }

    public void testRenameWithL87() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 87);
        endTest();
    }

    public void testRenameWithL88() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 88);
        endTest();
    }

    public void testRenameWithL93() throws Exception {
        startTest();
        doRefactoring(new EditorOperator("rename_with.js"), 93);
        endTest();
    }

    @Override
    public void tearDown() {
        openFile("rename_with.js");
        EditorOperator eo = new EditorOperator("rename_with.js");
        if (RenameWithTest.fileContent == null) {
            RenameWithTest.fileContent = eo.getText();
            return;
        }
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(java.awt.event.KeyEvent.VK_DELETE);
        eo.insert(RenameWithTest.fileContent);
        eo.save();
    }

    public void doRefactoring(EditorOperator eo, int lineNumber) {
        waitScanFinished();
        evt.waitNoEvent(500);
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("//rename");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPosition(lineNumber, Integer.parseInt(config[1]));
        eo.typeKey('r', InputEvent.CTRL_MASK);
        type(eo, config[2]);
        eo.pressKey(KeyEvent.VK_ENTER);
        String[] expectedResult;
        String line;
        for (int i = 3; i < config.length; i++) {
            expectedResult = config[i].split(":");
            line = eo.getText(Integer.parseInt(expectedResult[0]));
            assertTrue("file not refactored - contains old value: \n" + line+"\n Not found: "+expectedResult[1], line.contains(expectedResult[1].trim()));
        }
    }
}
