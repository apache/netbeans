/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.requirejs.navigate;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.javascript2.requirejs.GeneralRequire;

/**
 *
 * @author Vladimir Riha
 */
public class DefineTest extends GeneralRequire {

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(DefineTest.class).addTest(
                        "openProject",
                        "testStdModule",
                        "testFncModule",
                        "testNewFncModule",
                        "testLiteralModule",
                        "testLibrary",
                        "testStdModuleParam",
                        "testFncModuleParam",
                        "testNewFncModuleParam",
                        "testLiteralModuleParam",
                        "testLibraryParam"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public DefineTest(String arg0) {
        super(arg0);
    }

    public void openProject() throws Exception {
        startTest();
        DefineTest.currentProject = "SimpleRequire";
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleRequire");
        openFile("js|bbb|def.js", "SimpleRequire");
        endTest();
    }

    public void testLibrary() {
        startTest();
        navigate("def.js", "piwik.js", 1, 23, 1, 1);
        endTest();
    }

    public void testStdModule() {
        startTest();
        navigate("def.js", "stdModule.js", 1, 93, 1, 1);
        endTest();
    }

    public void testFncModule() {
        startTest();
        navigate("def.js", "function.js", 1, 34, 1, 1);
        endTest();
    }

    public void testNewFncModule() {
        startTest();
        navigate("def.js", "newFunction.js", 1, 52, 1, 1);
        endTest();
    }

    public void testLiteralModule() {
        startTest();
        navigate("def.js", "objectLiteral.js", 1, 76, 1, 1);
        endTest();
    }

    public void testLibraryParam() {
        startTest();
        navigate("def.js", "piwik.js", 2, 22, 1, 1);
        endTest();
    }

    public void testStdModuleParam() {
        startTest();
        navigate("def.js", "stdModule.js", 2, 50, 1, 1);
        endTest();
    }

    public void testFncModuleParam() {
        startTest();
        navigate("def.js", "function.js", 2, 27, 1, 1);
        endTest();
    }

    public void testNewFncModuleParam() {
        startTest();
        navigate("def.js", "newFunction.js", 2, 33, 1, 1);
        endTest();
    }

    public void testLiteralModuleParam() {
        startTest();
        navigate("def.js", "objectLiteral.js", 2, 42, 1, 1);
        endTest();
    }

    private void navigate(String fromFile, String toFile, int fromLine, int fromColumn, int toLine, int toColumn) {
        EditorOperator eo = new EditorOperator(fromFile);
        eo.setCaretPosition(fromLine, fromColumn);
        evt.waitNoEvent(200);
        new org.netbeans.jellytools.actions.Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_B, 2)).performShortcut(eo);
        evt.waitNoEvent(500);
        long defaultTimeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
            EditorOperator ed = new EditorOperator(toFile);
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", defaultTimeout);
            int position = ed.txtEditorPane().getCaretPosition();
            ed.setCaretPosition(toLine, toColumn);
            int expectedPosition = ed.txtEditorPane().getCaretPosition();
            assertTrue("Incorrect caret position. Expected position " + expectedPosition + " but was " + position, position == expectedPosition);
            if (!fromFile.equals(toFile)) {
                ed.close(false);
            }
        } catch (Exception e) {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", defaultTimeout);
            fail(e.getMessage());
        }

    }
}
