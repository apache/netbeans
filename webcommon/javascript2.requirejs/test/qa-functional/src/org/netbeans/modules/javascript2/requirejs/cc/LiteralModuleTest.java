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
public class LiteralModuleTest extends GeneralRequire {

    public LiteralModuleTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(LiteralModuleTest.class).addTest(
                        "openProject",
                        "testDateMethod",
                        "testLiteralMethod",
                        "testLiteralNested",
                        "testLiteralNested2",
                        "testLiteralProperty",
                        "testLiteralPropertyNested",
                        "testLiteralPropertyNested2",
                        "testModDateMethod",
                        "testModLiteralMethod",
                        "testModLiteralNested",
                        "testModLiteralNested2",
                        "testModLiteralProperty",
                        "testModLiteralPropertyNested",
                        "testModLiteralPropertyNested2"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        LiteralModuleTest.currentProject = "SimpleRequire";
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleRequire");
        openFile("index.html", "SimpleRequire");
        endTest();
    }

    public void testDateMethod() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", LiteralModuleTest.currentProject), 59);
        endTest();
    }

    public void testLiteralMethod() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", LiteralModuleTest.currentProject), 61);
        endTest();
    }

    public void testLiteralNested() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", LiteralModuleTest.currentProject), 63);
        endTest();
    }

    public void testLiteralNested2() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", LiteralModuleTest.currentProject), 65);
        endTest();
    }

    public void testLiteralProperty() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", LiteralModuleTest.currentProject), 67);
        endTest();
    }

    public void testLiteralPropertyNested() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", LiteralModuleTest.currentProject), 69);
        endTest();
    }

    public void testLiteralPropertyNested2() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", LiteralModuleTest.currentProject), 71);
        endTest();
    }

    public void testModDateMethod() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", LiteralModuleTest.currentProject), 59);
        endTest();
    }

    public void testModLiteralMethod() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", LiteralModuleTest.currentProject), 61);
        endTest();
    }

    public void testModLiteralNested() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", LiteralModuleTest.currentProject), 63);
        endTest();
    }

    public void testModLiteralNested2() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", LiteralModuleTest.currentProject), 65);
        endTest();
    }

    public void testModLiteralProperty() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", LiteralModuleTest.currentProject), 67);
        endTest();
    }

    public void testModLiteralPropertyNested() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", LiteralModuleTest.currentProject), 69);
        endTest();
    }

    public void testModLiteralPropertyNested2() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", LiteralModuleTest.currentProject), 71);
        endTest();
    }

    @Override
    public void tearDown() {
        if (!LiteralModuleTest.currentFile.equalsIgnoreCase("index.html")) {
            clearCurrentLine(new EditorOperator(LiteralModuleTest.currentFile));
        }
    }

}
