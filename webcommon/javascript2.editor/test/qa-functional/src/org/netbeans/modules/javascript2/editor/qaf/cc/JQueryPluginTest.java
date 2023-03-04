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
