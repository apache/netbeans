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
public class RequireFncTest extends GeneralRequire {

    public RequireFncTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(RequireFncTest.class).addTest(
                        "openProject",
                        "testDateMethod",
                        "testStdModule",
                        "testFncPropModule",
                        "testNewFncModule",
                        "testLiteralModule",
                        "testDateMethod2",
                        "testLiteralNested2",
                        "testLiteralPropertyNested2",
                        "testArrayMethod",
                        "testDateMethod4",
                        "testDateProperty",
                        "testLiteralPropertyNested",
                        "testDateMethod3",
                        "testLiteralMethodNested",
                        "testArrayMethod2"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        RequireFncTest.currentProject = "SimpleRequire";
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleRequire");
        endTest();
    }

    public void testDateMethod() throws Exception {
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 20);
        endTest();
    }

    public void testStdModule() throws Exception {
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 22);
        endTest();
    }

    public void testFncPropModule() throws Exception {
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 26);
        endTest();
    }

    public void testNewFncModule() throws Exception {
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 28);
        endTest();
    }

    public void testLiteralModule() throws Exception {
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 30);
        endTest();
    }

    public void testDateMethod2() throws Exception {
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 59);
        endTest();
    }

    public void testLiteralNested2() throws Exception {
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 65);
        endTest();
    }

    public void testLiteralPropertyNested2() throws Exception {
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 71);
        endTest();
    }

    public void testArrayMethod() throws Exception {// 244923
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 40);
        endTest();
    }

    public void testDateMethod4() throws Exception { // 244923
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 42);
        endTest();
    }

    public void testLiteralPropertyNested() throws Exception {
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 56);
        endTest();
    }

    public void testDateMethod3() throws Exception {
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 75);
        endTest();
    }

    public void testLiteralMethodNested() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 79);
        endTest();
    }

    public void testArrayMethod2() throws Exception {// issue 244506
        startTest();
        testCompletion(openFile("js|bbb2|aa.js", RequireFncTest.currentProject), 83);
        endTest();
    }
    
    @Override
    public void tearDown(){
        if (RequireFncTest.currentFile!= null && !RequireFncTest.currentFile.equalsIgnoreCase("index.html")) {
            clearCurrentLine(new EditorOperator(RequireFncTest.currentFile));
        }
    }

}
