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
package org.netbeans.modules.javascript2.nodejs.cc;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.javascript2.nodejs.GeneralNodeJs;

/**
 *
 * @author vriha
 */
public class MEAnonymousModuleTest2 extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testAnonymousME31",
        "testAnonymousME35",
        "testAnonymousME36",
        "testAnonymousME311",
        "testAnonymousME312",
        "testAnonymousME313",
        "testAnonymousME41",
        "testAnonymousME45",
        "testAnonymousME46",
        "testAnonymousME411",
        "testAnonymousME412",
        "testAnonymousME413"
    };

    public MEAnonymousModuleTest2(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(MEAnonymousModuleTest2.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        MEAnonymousModuleTest2.currentFile = "cc15.js";
        openFile("modex|cc|cc15.js", "SimpleNode");
        endTest();
    }

    public void testAnonymousME35() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 85);
        endTest();
    }

    public void testAnonymousME36() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 87);
        endTest();
    }

    public void testAnonymousME311() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 97);
        endTest();
    }

    public void testAnonymousME312() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 99);
        endTest();
    }

    public void testAnonymousME313() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 101);
        endTest();
    }

    public void testAnonymousME45() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 85);
        endTest();
    }

    public void testAnonymousME46() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 87);
        endTest();
    }

    public void testAnonymousME411() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 97);
        endTest();
    }

    public void testAnonymousME412() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 99);
        endTest();
    }

    public void testAnonymousME413() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 101);
        endTest();
    }

    public void testAnonymousME31() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 77);
        endTest();
    }

    public void testAnonymousME41() throws Exception {
        startTest();
        openFile("modex|cc|cc16.js", "SimpleNode");
        MEAnonymousModuleTest2.currentFile = "cc16.js";
        testCompletion(new EditorOperator(MEAnonymousModuleTest2.currentFile), 77);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator(MEAnonymousModuleTest2.currentFile);
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }
}
