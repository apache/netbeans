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

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.javascript2.requirejs.GeneralRequire;

/**
 *
 * @author Vladimir Riha
 */
public class ModulesTest extends GeneralRequire {

    public ModulesTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ModulesTest.class).addTest(
                        "openProject",
                        "testStdModule",
                        "testFncModule",
                        "testFncPropModule",
                        "testNewFncModule",
                        "testLiteralModule",
                        "testModStdModule",
                        "testModFncModule",
                        "testModFncPropModule",
                        "testModNewFncModule",
                        "testModLiteralModule"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        ModulesTest.currentProject = "SimpleRequire";
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleRequire");
        openFile("index.html", "SimpleRequire");
        endTest();
    }

    public void testStdModule() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", ModulesTest.currentProject), 22);
        endTest();
    }

    public void testFncModule() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", ModulesTest.currentProject), 24);
        endTest();
    }

    public void testFncPropModule() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", ModulesTest.currentProject), 26);
        endTest();
    }

    public void testNewFncModule() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", ModulesTest.currentProject), 28);
        endTest();
    }

    public void testLiteralModule() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", ModulesTest.currentProject), 30);
        endTest();
    }

    public void testModStdModule() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", ModulesTest.currentProject), 22);
        endTest();
    }

    public void testModFncModule() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", ModulesTest.currentProject), 24);
        endTest();
    }

    public void testModFncPropModule() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", ModulesTest.currentProject), 26);
        endTest();
    }

    public void testModNewFncModule() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", ModulesTest.currentProject), 28);
        endTest();
    }

    public void testModLiteralModule() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", ModulesTest.currentProject), 30);
        endTest();
    }

    @Override
    public void tearDown() {
        if (!ModulesTest.currentFile.equalsIgnoreCase("index.html")) {
            clearCurrentLine(new EditorOperator(ModulesTest.currentFile));
        }
    }

}
