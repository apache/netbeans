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
