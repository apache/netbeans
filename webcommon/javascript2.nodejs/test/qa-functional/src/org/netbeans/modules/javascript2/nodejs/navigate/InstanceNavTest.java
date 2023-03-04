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
package org.netbeans.modules.javascript2.nodejs.navigate;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.javascript2.nodejs.GeneralNodeJs;

/**
 *
 * @author vriha
 */
public class InstanceNavTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testDirectProperty",
        "testDirectProperty2",
        "testDirectProperty3",
        "testDirectProperty4",
        "testDirectProperty5",
        "testPrototypeProperty",
        "testPrototypeProperty2",
        "testRefProperty",
        "testRefProperty2",
        "testRefProperty3",
        "testRefProperty4",
        "testRefProperty5",
        "testRefPrototypeProperty",
        "testRefPrototypeProperty2"
    };

    public InstanceNavTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(InstanceNavTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        openFile("app|maingt.js", "SimpleNode");
        endTest();
    }

    public void testDirectProperty() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 30);
        endTest();
    }

    public void testDirectProperty2() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 31);
        endTest();
    }

    public void testDirectProperty3() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 32);
        endTest();
    }

    public void testDirectProperty4() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 33);
        endTest();
    }

    public void testDirectProperty5() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 34);
        endTest();
    }

    public void testPrototypeProperty() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 35);
        endTest();
    }

    public void testPrototypeProperty2() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 36);
        endTest();
    }

    public void testRefPrototypeProperty() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 45);
        endTest();
    }

    public void testRefPrototypeProperty2() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 46);
        endTest();
    }

    public void testRefProperty() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 40);
        endTest();
    }

    public void testRefProperty2() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 41);
        endTest();
    }

    public void testRefProperty3() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 42);
        endTest();
    }

    public void testRefProperty4() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 43);
        endTest();
    }

    public void testRefProperty5() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 44);
        endTest();
    }
}
