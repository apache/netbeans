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
public class NewFncModuleTest extends GeneralRequire {

    public NewFncModuleTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(NewFncModuleTest.class).addTest(
                        "openProject",
                        "testArrayMethod",
                        "testDateMethod",
                        "testDateMethod2",
                        "testLiteralMethod",
                        "testLiteralMethodNested",
                        "testLiteralMethodNested2",
                        "testDateProperty",
                        "testLiteralProperty",
                        "testLiteralPropertyNested",
                        "testModArrayMethod",
                        "testModDateMethod",
                        "testModDateMethod2",
                        "testModLiteralMethod",
                        "testModLiteralMethodNested",
                        "testModLiteralMethodNested2",
                        "testModDateProperty",
                        "testModLiteralProperty",
                        "testModLiteralPropertyNested"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        NewFncModuleTest.currentProject = "SimpleRequire";
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleRequire");
        openFile("index.html", "SimpleRequire");
        endTest();
    }

    public void testArrayMethod() throws Exception {// 244923
        startTest();
        testCompletion(openFile("js|main.js", NewFncModuleTest.currentProject), 40);
        endTest();
    }

    public void testDateMethod() throws Exception { // 244923
        startTest();
        testCompletion(openFile("js|main.js", NewFncModuleTest.currentProject), 42);
        endTest();
    }

    public void testDateMethod2() throws Exception {// 244923
        startTest();
        testCompletion(openFile("js|main.js", NewFncModuleTest.currentProject), 44);
        endTest();
    }

    public void testLiteralMethod() throws Exception {// 244923
        startTest();
        testCompletion(openFile("js|main.js", NewFncModuleTest.currentProject), 46);
        endTest();
    }

    public void testLiteralMethodNested() throws Exception {// 244923
        startTest();
        testCompletion(openFile("js|main.js", NewFncModuleTest.currentProject), 48);
        endTest();
    }

    public void testLiteralMethodNested2() throws Exception {// 244923
        startTest();
        testCompletion(openFile("js|main.js", NewFncModuleTest.currentProject), 50);
        endTest();
    }

    public void testDateProperty() throws Exception {// 244923
        startTest();
        testCompletion(openFile("js|main.js", NewFncModuleTest.currentProject), 52);
        endTest();
    }

    public void testLiteralProperty() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", NewFncModuleTest.currentProject), 54);
        endTest();
    }

    public void testLiteralPropertyNested() throws Exception {
        startTest();
        testCompletion(openFile("js|main.js", NewFncModuleTest.currentProject), 56);
        endTest();
    }

    public void testModArrayMethod() throws Exception {// 244923
        startTest();
        testCompletion(openFile("js|app|mymodule.js", NewFncModuleTest.currentProject), 40);
        endTest();
    }

    public void testModDateMethod() throws Exception { // 244923
        startTest();
        testCompletion(openFile("js|app|mymodule.js", NewFncModuleTest.currentProject), 42);
        endTest();
    }

    public void testModDateMethod2() throws Exception {// 244923
        startTest();
        testCompletion(openFile("js|app|mymodule.js", NewFncModuleTest.currentProject), 44);
        endTest();
    }

    public void testModLiteralMethod() throws Exception {// 244923
        startTest();
        testCompletion(openFile("js|app|mymodule.js", NewFncModuleTest.currentProject), 46);
        endTest();
    }

    public void testModLiteralMethodNested() throws Exception {// 244923
        startTest();
        testCompletion(openFile("js|app|mymodule.js", NewFncModuleTest.currentProject), 48);
        endTest();
    }

    public void testModLiteralMethodNested2() throws Exception {// 244923
        startTest();
        testCompletion(openFile("js|app|mymodule.js", NewFncModuleTest.currentProject), 50);
        endTest();
    }

    public void testModDateProperty() throws Exception {// 244923
        startTest();
        testCompletion(openFile("js|app|mymodule.js", NewFncModuleTest.currentProject), 52);
        endTest();
    }

    public void testModLiteralProperty() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", NewFncModuleTest.currentProject), 54);
        endTest();
    }

    public void testModLiteralPropertyNested() throws Exception {
        startTest();
        testCompletion(openFile("js|app|mymodule.js", NewFncModuleTest.currentProject), 56);
        endTest();
    }

    @Override
    public void tearDown() {
        if (!NewFncModuleTest.currentFile.equalsIgnoreCase("index.html")) {
            clearCurrentLine(new EditorOperator(NewFncModuleTest.currentFile));
        }
    }

}
