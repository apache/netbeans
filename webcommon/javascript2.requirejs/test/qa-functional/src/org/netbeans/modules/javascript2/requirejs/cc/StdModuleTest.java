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
public class StdModuleTest extends GeneralRequire {

    public StdModuleTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(StdModuleTest.class).addTest(
                        "openProject",
                        "testDateMethod",
                        "testLiteralMethod",
                        "testLiteralMethodNested",
                        "testLiteralMethodNested2",
                        "testArrayMethod",
                        "testLiteralProperty",
                        "testLiteralPropertyNested",
                        "testModDateMethod",
                        "testModLiteralMethod",
                        "testModLiteralMethodNested",
                        "testModLiteralMethodNested2",
                        "testModArrayMethod",
                        "testModLiteralProperty",
                        "testModLiteralPropertyNested"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        StdModuleTest.currentProject = "SimpleRequire";
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleRequire");
        openFile("index.html", "SimpleRequire");
        endTest();
    }

    public void testDateMethod() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 75);
        endTest();
    }

    public void testLiteralMethod() throws Exception { // issue 244506
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 77);
        endTest();
    }

    public void testLiteralMethodNested() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 79);
        endTest();
    }

    public void testLiteralMethodNested2() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 81);
        endTest();
    }

    public void testArrayMethod() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 83);
        endTest();
    }

    public void testLiteralProperty() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 85);
        endTest();
    }

    public void testLiteralPropertyNested() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", StdModuleTest.currentProject), 87);
        endTest();
    }

    public void testModDateMethod() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 75);
        endTest();
    }

    public void testModLiteralMethod() throws Exception { // issue 244506
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 77);
        endTest();
    }

    public void testModLiteralMethodNested() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 79);
        endTest();
    }

    public void testModLiteralMethodNested2() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 81);
        endTest();
    }

    public void testModArrayMethod() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 83);
        endTest();
    }

    public void testModLiteralProperty() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 85);
        endTest();
    }

    public void testModLiteralPropertyNested() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", StdModuleTest.currentProject), 87);
        endTest();
    }

    @Override
    public void tearDown() {
        if (!StdModuleTest.currentFile.equalsIgnoreCase("index.html")) {
            clearCurrentLine(new EditorOperator(StdModuleTest.currentFile));
        }
    }
}
