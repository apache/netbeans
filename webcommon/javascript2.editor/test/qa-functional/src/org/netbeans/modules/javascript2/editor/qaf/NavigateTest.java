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

import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author vriha
 */
public class NavigateTest extends GeneralJavaScript {

    private String projectName = "navigationTest";
    private static final Logger LOGGER = Logger.getLogger(NavigateTest.class.getName());

    public NavigateTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(NavigateTest.class).addTest(
                "openProject",
                "testDeclaration").enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects(projectName);
        evt.waitNoEvent(3000);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node sourceFiles = new Node(rootNode, "Source Files");
        for (String file : sourceFiles.getChildren()) {
            if (file.endsWith("goto.js")) {
                openFile(file);
            }
        }
        endTest();
    }

    public void openFile(String fileName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        LOGGER.log(Level.INFO, "Opening file {0}", fileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Source Files|" + fileName);
        node.select();
        node.performPopupAction("Open");
    }

    public void testDeclaration() throws Exception {
        startTest();
        openFile("goto.js");
        EditorOperator eo = new EditorOperator("goto.js");
        EditorOperator ed;
        int position;
        String line;
        int lineIterator = 1;
        String data[];
        int lines = eo.getText().split(System.getProperty("line.separator")).length;
        if(lines < 1){ // fallback if line.separator is not platform specific
            lines = eo.getText().split("\n").length;
        }
        int offset = 0;
        while (lineIterator <= lines) {
            eo.setCaretPositionToLine(lineIterator);
            line = eo.getText(lineIterator);

            if (line.contains("//")) {
                data = (line.substring(line.indexOf("//") + 2)).split(":");
                data[0] = data[0].trim();
                data[1] = data[1].trim();
                
                eo.setCaretPosition(offset+Integer.valueOf(data[0].trim()).intValue());
                new org.netbeans.jellytools.actions.Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_B, 2)).performShortcut(eo);
                try {
                    ed = new EditorOperator("test.js");
                    position = ed.txtEditorPane().getCaretPosition();
                    boolean result = false;
                    if (position == Integer.valueOf(data[1]).intValue()) {
                        result = true;
                    }

                    assertTrue("Incorrect caret position. Expected position " + data[1] + " but was " + position, result);
                    ed.close(false);
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }
             offset+=line.length();
            lineIterator++;
        }

        LOGGER.log(Level.INFO, "Number of lines: {0}", (lineIterator - 1));

        endTest();
    }
}
