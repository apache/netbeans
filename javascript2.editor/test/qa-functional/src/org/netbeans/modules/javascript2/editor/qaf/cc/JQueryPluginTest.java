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
package org.netbeans.modules.javascript2.editor.qaf.cc;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;

/**
 *
 * @author vriha
 */
public class JQueryPluginTest extends GeneralJavaScript {

    static final String[] tests = new String[]{
        "openProject",
        "testPluginObj",
        "testPluginProp",
        "testPluginNestedProp",
        "testPluginObjectProp",
        "testPluginCustomProp",
        "testPluginObjectPropNested"
    };

    public JQueryPluginTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return createModuleTest(JQueryPluginTest.class, tests);
    }

    public void openProject() throws Exception {
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("completionTest");
        evt.waitNoEvent(10000);
        openFile("plugin.js");
    }

    public void openFile(String fileName) {
        Logger.getLogger(JQueryPluginTest.class.getName()).info("Opening file " + fileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode("completionTest");
        Node node = new Node(rootNode, "Source Files|" + fileName);
        node.select();
        node.performPopupAction("Open");
    }

    public void testPluginObj() {
        startTest();
        doTest(new EditorOperator("plugin.js"), 60);
        endTest();
    }

    public void testPluginProp() {
        startTest();
        doTest(new EditorOperator("plugin.js"), 62);
        endTest();
    }

    public void testPluginNestedProp() {
        startTest();
        doTest(new EditorOperator("plugin.js"), 64);
        endTest();
    }

    public void testPluginObjectProp() {
        startTest();
        doTest(new EditorOperator("plugin.js"), 66);
        endTest();
    }

    public void testPluginCustomProp() {
        startTest();
        doTest(new EditorOperator("plugin.js"), 68);
        endTest();
    }

    public void testPluginObjectPropNested() {
        startTest();
        doTest(new EditorOperator("plugin.js"), 70);
        endTest();
    }

    @Override
    public void doTest(EditorOperator eo, int lineNumber) {
        waitScanFinished();
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("//cc;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPositionToEndOfLine(Integer.parseInt(config[1]));
        type(eo, config[2]);
        eo.pressKey(KeyEvent.VK_ESCAPE);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, config[3].split(","));
        eo.pressKey(KeyEvent.VK_ESCAPE);
        completion.listItself.hideAll();
        eo.setCaretPositionToEndOfLine(eo.getLineNumber());
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }
    }

}
