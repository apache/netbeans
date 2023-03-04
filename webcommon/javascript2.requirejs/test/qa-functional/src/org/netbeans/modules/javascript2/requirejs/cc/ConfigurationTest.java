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
public class ConfigurationTest extends GeneralRequire {

    private static String originalContent;
    private static String uncommentedContent;
    private static boolean setup = false;
    private static int numberOfTests = 17;

    public ConfigurationTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ConfigurationTest.class).addTest(
                        "openProject",
                        "testConfOptions",
                        "testBaseUrl",
                        "testContext",
                        "testScriptType",
                        "testUrlArgs",
                        "testEnforceDefine",
                        "testNodeIdCompat",
                        "testSkipDataMain",
                        "testXhtml",
                        "testBundles",
                        "testConfig",
                        "testPaths",
                        "testMap",
                        "testShim",
                        "testDeps",
                        "testPackages"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        ConfigurationTest.currentProject = "SimpleRequire";
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleRequire");
        openFile("js|main2.js", "SimpleRequire");
        ConfigurationTest.originalContent = openFile("js|main2.js", "SimpleRequire").getText();
        endTest();
    }

    public void testConfOptions() throws Exception {
        startTest();
        testCompletion(openFile("js|main2.js", "SimpleRequire"), 3);
        endTest();
    }

    public void testBaseUrl() throws Exception {
        startTest();
        testString(openFile("js|main2.js", "SimpleRequire"), "baseUrl");
        endTest();
    }

    public void testContext() throws Exception {
        startTest();
        testString(openFile("js|main2.js", "SimpleRequire"), "context");
        endTest();
    }

    public void testScriptType() throws Exception {
        startTest();
        testString(openFile("js|main2.js", "SimpleRequire"), "scriptType");
        endTest();
    }

    public void testUrlArgs() throws Exception {
        startTest();
        testString(openFile("js|main2.js", "SimpleRequire"), "urlArgs");
        endTest();
    }

    public void testEnforceDefine() throws Exception {
        startTest();
        testBoolean(openFile("js|main2.js", "SimpleRequire"), "enforceDefine");
        endTest();
    }

    public void testNodeIdCompat() throws Exception {
        startTest();
        testBoolean(openFile("js|main2.js", "SimpleRequire"), "nodeIdCompat");
        endTest();
    }

    public void testSkipDataMain() throws Exception {
        startTest();
        testBoolean(openFile("js|main2.js", "SimpleRequire"), "skipDataMain");
        endTest();
    }

    public void testXhtml() throws Exception {
        startTest();
        testBoolean(openFile("js|main2.js", "SimpleRequire"), "xhtml");
        endTest();
    }

    public void testBundles() throws Exception {
        startTest();
        testObject(openFile("js|main2.js", "SimpleRequire"), "bundles");
        endTest();
    }

    public void testPaths() throws Exception {
        startTest();
        testObject(openFile("js|main2.js", "SimpleRequire"), "paths");
        endTest();
    }

    public void testConfig() throws Exception {
        startTest();
        testObject(openFile("js|main2.js", "SimpleRequire"), "config");
        endTest();
    }

    public void testMap() throws Exception {
        startTest();
        testObject(openFile("js|main2.js", "SimpleRequire"), "map");
        endTest();
    }

    public void testShim() throws Exception {
        startTest();
        testObject(openFile("js|main2.js", "SimpleRequire"), "shim");
        endTest();
    }

    public void testDeps() throws Exception {
        startTest();
        testArray(openFile("js|main2.js", "SimpleRequire"), "deps");
        endTest();
    }

    public void testPackages() throws Exception {
        startTest();
        testArray(openFile("js|main2.js", "SimpleRequire"), "packages");
        endTest();
    }

    private void insertConfiguration(EditorOperator eo, String configuration) {
        eo.setCaretPosition(4, 5);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        evt.waitNoEvent(500);
        cjo.clickOnItem(configuration);
        eo.pressKey(KeyEvent.VK_ENTER);
        evt.waitNoEvent(200);
    }

    private void testString(EditorOperator eo, String configuration) {
        insertConfiguration(eo, configuration);
        assertEquals("Unexpected configuration completion", configuration + ": ''", eo.getText(eo.getLineNumber()).trim());
    }

    private void testBoolean(EditorOperator eo, String configuration) {
        insertConfiguration(eo, configuration);
        assertEquals("Unexpected configuration completion", configuration + ": false", eo.getText(eo.getLineNumber()).trim());
    }

    private void testObject(EditorOperator eo, String configuration) {
        insertConfiguration(eo, configuration);
        assertEquals("Unexpected configuration completion", configuration + ": {", eo.getText(eo.getLineNumber()).trim());
        assertEquals("Unexpected configuration completion", "}", eo.getText(eo.getLineNumber() + 1).trim());
    }

    private void testArray(EditorOperator eo, String configuration) {
        insertConfiguration(eo, configuration);
        assertEquals("Unexpected configuration completion", configuration + ": [", eo.getText(eo.getLineNumber()).trim());
        assertEquals("Unexpected configuration completion", "]", eo.getText(eo.getLineNumber() + 1).trim());
    }

    @Override
    public void tearDown() {

        ConfigurationTest.numberOfTests--;

        if (ConfigurationTest.numberOfTests > 0 && ConfigurationTest.uncommentedContent == null) {
            return;
        }
        EditorOperator eo = openFile("js|main2.js", "SimpleRequire");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(KeyEvent.VK_DELETE);

        if (ConfigurationTest.numberOfTests == 0) {
            eo.insert(ConfigurationTest.originalContent);
        } else {
            eo.insert(ConfigurationTest.uncommentedContent);
        }
        eo.save();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        evt.waitNoEvent(1000);

    }

    @Override
    public void setUp() {
        if (ConfigurationTest.originalContent != null && !ConfigurationTest.setup) {
            EditorOperator eo = openFile("js|main2.js", "SimpleRequire");
            eo.typeKey('a', InputEvent.CTRL_MASK);
            eo.typeKey('/', InputEvent.CTRL_MASK);
            eo.clickMouse();
            eo.save();
            evt.waitNoEvent(1000);
            ConfigurationTest.setup = true;
            ConfigurationTest.uncommentedContent = eo.getText();
        }
    }

}
