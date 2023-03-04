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
