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
package org.netbeans.modules.javascript2.requirejs.cc;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.javascript2.requirejs.GeneralRequire;

/**
 *
 * @author Vladimir Riha
 */
public class DependenciesTest extends GeneralRequire {

    public DependenciesTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(DependenciesTest.class).addTest(
                        "openProject",
                        "testRequire",
                        "testDefine"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        DependenciesTest.currentProject = "SimpleRequire";
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleRequire");
        openFile("index.html", "SimpleRequire");
        endTest();
    }

    public void testRequire() throws Exception {
        doTest("js|main.js", 19, 105);
    }

    public void testDefine() throws Exception {
        doTest("js|bbb|fs.js", 1, 10);
    }

    private void doTest(String file, int rowNumber, int columnNumber) throws Exception {
        startTest();
        EditorOperator eo = openFile(file, DependenciesTest.currentProject);
        eo.setCaretPosition(rowNumber, columnNumber);

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"app", "bbb", "bbb2", "libs", "jquery", "piwik", "external"});
        checkCompletionDoesntContainItems(cjo, new String[]{"mymodule", "objectLiteral", "mymodule.js"});
        completion.listItself.hideAll();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        type(eo, "b");

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"bbb", "bbb2"});
        checkCompletionDoesntContainItems(cjo, new String[]{"app", "objectLiteral", "mymodule"});
        completion.listItself.hideAll();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        eo.pressKey(KeyEvent.VK_BACK_SPACE);

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        cjo.clickOnItem("app");
        eo.pressKey(KeyEvent.VK_ENTER);
        completion.listItself.hideAll();

        assertTrue("Wrong inserted file based completion", eo.getText(eo.getLineNumber()).trim().endsWith("\"app/\"],"));

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"function", "mymodule", "newFunction", "objectLiteral", "stdModule"});
        checkCompletionDoesntContainItems(cjo, new String[]{"app", "def", "piwik"});
        completion.listItself.hideAll();
        eo.pressKey(KeyEvent.VK_ESCAPE);

        type(eo, "m");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"mymodule", "mytest"});
        checkCompletionDoesntContainItems(cjo, new String[]{"function", "app", "def", "piwik", "newFunction"});
        completion.listItself.hideAll();
        eo.pressKey(KeyEvent.VK_ESCAPE);

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        cjo.clickOnItem("mymodule");
        eo.pressKey(KeyEvent.VK_ENTER);
        completion.listItself.hideAll();

        assertTrue("Wrong inserted file based completion", eo.getText(eo.getLineNumber()).trim().endsWith("\"app/mymodule\"],"));

        endTest();
    }

}
