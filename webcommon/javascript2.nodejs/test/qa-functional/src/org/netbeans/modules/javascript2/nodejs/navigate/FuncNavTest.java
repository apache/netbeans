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
public class FuncNavTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testDirectProperty",
        "testDirectProperty2",
        "testDirectProperty3",
        "testDirectProperty4",
        "testDirectProperty5",
        "testInstanceProperty",
        "testInstanceProperty2",
        "testAnonymProperty",
        "testModule",
        "testPrototypeProperty",
        "testPrototypeProperty2"
    };

    public FuncNavTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(FuncNavTest.class, tests);
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
        testGoToDeclaration(new EditorOperator("maingt.js"), 16);
        endTest();
    }

    public void testDirectProperty2() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 17);
        endTest();
    }

    public void testDirectProperty3() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 18);
        endTest();
    }

    public void testDirectProperty4() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 19);
        endTest();
    }

    public void testDirectProperty5() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 20);
        endTest();
    }

    public void testInstanceProperty() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 23);
        endTest();
    }

    public void testInstanceProperty2() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 24);
        endTest();
    }

    public void testAnonymProperty() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 25);
        endTest();
    }

    public void testPrototypeProperty() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 26);
        endTest();
    }

    public void testPrototypeProperty2() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 27);
        endTest();
    }

    public void testModule() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 14);
        endTest();
    }

}
