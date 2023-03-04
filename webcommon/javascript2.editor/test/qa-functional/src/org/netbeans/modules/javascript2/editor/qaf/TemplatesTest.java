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
package org.netbeans.modules.javascript2.editor.qaf;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha
 */
public class TemplatesTest extends GeneralJavaScript {

    private static String fileContent;

    public TemplatesTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(TemplatesTest.class).addTest(
                        "openProject",
                        "testPropertyLiteral",
                        "testPropertyFunction",
                        "testFunction",
                        "testAuthor",
                        "testPropertyLiteralEmb",
                        "testPropertyFunctionEmb",
                        "testFunctionEmb",
                        "testAuthorEmb"
                ).enableModules(".*").clusters(".*"));
    }

    public void openProject() throws Exception {
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("completionTest");
        evt.waitNoEvent(5000);
        TemplatesTest.currentFile = "templates.js";
        openFile("templates.js");
    }

    public void openFile(String fileName) {
        Logger.getLogger(TemplatesTest.class.getName()).info("Opening file " + fileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode("completionTest");
        Node node = new Node(rootNode, "Source Files|" + fileName);
        node.select();
        node.performPopupAction("Open");
    }

    public void testPropertyLiteral() {
        startTest();
        openFile("templates.js");
        doTemplateTest(new EditorOperator("templates.js"), 2);
        endTest();
    }

    public void testPropertyLiteralEmb() {
        TemplatesTest.currentFile = "templates.html";
        openFile("templates.html");
        TemplatesTest.fileContent = new EditorOperator("templates.html").getText();
        startTest();
        openFile("templates.html");
        doTemplateTest(new EditorOperator("templates.html"), 14);
        endTest();
    }

    public void testPropertyFunction() {
        startTest();
        openFile("templates.js");
        doTemplateTest(new EditorOperator("templates.js"), 7);
        endTest();
    }

    public void testPropertyFunctionEmb() {
        startTest();
        openFile("templates.html");
        doTemplateTest(new EditorOperator("templates.html"), 19);
        endTest();
    }

    public void testFunction() {
        startTest();
        openFile("templates.js");
        doTemplateTest(new EditorOperator("templates.js"), 11);
        endTest();
    }

    public void testFunctionEmb() {
        startTest();
        openFile("templates.html");
        doTemplateTest(new EditorOperator("templates.html"), 23);
        endTest();
    }

    public void testAuthor() {
        startTest();
        openFile("templates.js");
        doTemplateTest(new EditorOperator("templates.js"), 14);
        endTest();
    }

    public void testAuthorEmb() {
        startTest();
        openFile("templates.html");
        doTemplateTest(new EditorOperator("templates.html"), 26);
        endTest();
    }

    @Override
    public void tearDown() {
        openFile(TemplatesTest.currentFile);
        EditorOperator eo = new EditorOperator(TemplatesTest.currentFile);
        if (TemplatesTest.fileContent == null) {
            TemplatesTest.fileContent = eo.getText();
            return;
        }
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(java.awt.event.KeyEvent.VK_DELETE);
        eo.insert(TemplatesTest.fileContent);
        eo.save();
    }

    private void doTemplateTest(EditorOperator eo, int lineNumber) {

        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("//tt;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPositionToEndOfLine(Integer.parseInt(config[1]));
        type(eo, config[2]);
        eo.pressKey(KeyEvent.VK_TAB);
        evt.waitNoEvent(500);

        assertTrue("Incorrect template", eo.getText().contains(config[3]));

    }

}
