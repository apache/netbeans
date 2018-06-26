/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
