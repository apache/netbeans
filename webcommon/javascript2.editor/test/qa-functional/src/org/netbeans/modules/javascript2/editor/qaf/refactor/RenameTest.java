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
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;
import org.netbeans.modules.javascript2.editor.qaf.cc.TestJQuery;

/**
 *
 * @author vriha
 */
public class RenameTest extends GeneralJavaScript {

    private String projectName = "completionTest";

    public RenameTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(RenameTest.class).addTest(
                "openProject",
                "testRenameLocal",
                "testRenameFunction",
                "testRenameParameter",
                "testRenameSuperGlobal",
                "testRenameGlobal",
                "testRenamePublicProperty",
                "testRenamePublicMethod").enableModules(".*").clusters(".*"));
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects(projectName);
        evt.waitNoEvent(3000);
        // open all files
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node sourceFiles = new Node(rootNode, "Source Files");
        for (String file : sourceFiles.getChildren()) {
            openFile(file);
        }
        endTest();
    }

    public void openFile(String fileName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        Logger.getLogger(TestJQuery.class.getName()).info("Opening file " + fileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Source Files|" + fileName);
        node.select();
        node.performPopupAction("Open");
    }

    public void testRenameLocal() throws Exception {
        startTest();
        EditorOperator eo = new EditorOperator("rename.js");
        doRefactoring(eo, "data", "barr");
        endTest();
    }

    public void testRenameFunction() throws Exception {
        startTest();
        EditorOperator eo = new EditorOperator("rename.js");
        doRefactoring(eo, "drawResolvedFixedChart", "draw_ResolvedFixedChart");
        endTest();
    }

    public void testRenameParameter() throws Exception {
        startTest();
        EditorOperator eo = new EditorOperator("rename.js");
        doRefactoring(eo, "object", "foobar");
        endTest();

    }

    public void testRenameSuperGlobal() throws Exception {
        startTest();
        EditorOperator eo = new EditorOperator("rename.js");
        doRefactoring(eo, "superGlobal", "superlocal");
        endTest();

    }

    public void testRenameGlobal() throws Exception {
        startTest();
        EditorOperator eo = new EditorOperator("rename.js");
        doRefactoring(eo, "control", "setting");
        endTest();

    }

    public void testRenamePublicProperty() throws Exception {
        startTest();
        EditorOperator eo = new EditorOperator("rename.js");
        doRefactoring(eo, "realname", "username");
        endTest();

    }

    public void testRenamePublicMethod() throws Exception {
        startTest();
        EditorOperator eo = new EditorOperator("rename.js");
        doRefactoring(eo, "hello", "greeting");
        endTest();
    }

    private void doRefactoring(EditorOperator eo, String oldValue, String newValue) throws Exception {
        eo.setCaretPosition(oldValue, 0, false);
        eo.typeKey('r', InputEvent.CTRL_MASK);
        type(eo, newValue);
        eo.pressKey(KeyEvent.VK_ENTER);
        assertTrue("file not refactored - contains old value: \n" + eo.getText(), !eo.getText().contains(oldValue));
        assertTrue("file not refactored - does not contain new valuy: \n" + eo.getText(), eo.getText().contains(newValue));
    }
}
