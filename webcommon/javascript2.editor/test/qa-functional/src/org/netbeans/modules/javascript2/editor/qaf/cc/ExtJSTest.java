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
package org.netbeans.modules.javascript2.editor.qaf.cc;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;

/**
 *
 * @author vriha
 */
public class ExtJSTest extends GeneralJavaScript {

    static final String[] tests = new String[]{
        "openProject",
        "testExt17",
        "testExt19",
        "testExt21",
        "testExt23",
        "testExt39",
        "testExt41",
        "testExt43",
        "testExt45"
    };

    public ExtJSTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(ExtJSTest.class, tests);
    }

    public void testExt17() {
        startTest();
        doTest(new EditorOperator("ext.js"), 17);
        endTest();
    }

    public void testExt19() {
        startTest();
        doTest(new EditorOperator("ext.js"), 19);
        endTest();
    }

    public void testExt21() {
        startTest();
        doTest(new EditorOperator("ext.js"), 21);
        endTest();
    }

    public void testExt23() {
        startTest();
        doTest(new EditorOperator("ext.js"), 23);
        endTest();
    }

    public void testExt39() {
        startTest();
        doTest(new EditorOperator("ext.js"), 39);
        endTest();
    }

    public void testExt41() {
        startTest();
        doTest(new EditorOperator("ext.js"), 41);
        endTest();
    }

    public void testExt43() {
        startTest();
        doTest(new EditorOperator("ext.js"), 43);
        endTest();
    }

    public void testExt45() {
        startTest();
        doTest(new EditorOperator("ext.js"), 45);
        endTest();
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("completionTest");
        evt.waitNoEvent(2000);
        openFile("ext.js", "completionTest");
        endTest();
    }

}
