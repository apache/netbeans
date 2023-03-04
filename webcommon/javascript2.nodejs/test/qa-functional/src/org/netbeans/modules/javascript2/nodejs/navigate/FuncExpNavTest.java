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
public class FuncExpNavTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testDirectProperty",
        "testDirectProperty2",
        "testDirectProperty3"
    };

    public FuncExpNavTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(FuncExpNavTest.class, tests);
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
        testGoToDeclaration(new EditorOperator("maingt.js"), 67);
        endTest();
    }

    public void testDirectProperty2() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 68);
        endTest();
    }

    public void testDirectProperty3() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 69);
        endTest();
    }
}
