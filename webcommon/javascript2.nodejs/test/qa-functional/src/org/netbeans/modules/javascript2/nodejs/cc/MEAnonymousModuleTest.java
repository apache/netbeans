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
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.javascript2.nodejs.GeneralNodeJs;

/**
 *
 * @author vriha
 */
public class MEAnonymousModuleTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testAnonymousME1",
        "testAnonymousME5",
        "testAnonymousME6",
        "testAnonymousME11",
        "testAnonymousME12",
        "testAnonymousME13",
        "testAnonymousME21",
        "testAnonymousME25",
        "testAnonymousME26",
        "testAnonymousME211",
        "testAnonymousME212",
        "testAnonymousME213"
    };

    public MEAnonymousModuleTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(MEAnonymousModuleTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        MEAnonymousModuleTest.currentFile = "cc3.js";
        openFile("modex|cc|cc3.js", "SimpleNode");
        endTest();
    }

    public void testAnonymousME1() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest.currentFile), 77);
        endTest();
    }

    public void testAnonymousME5() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest.currentFile), 85);
        endTest();
    }

    public void testAnonymousME6() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest.currentFile), 87);
        endTest();
    }

    public void testAnonymousME11() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest.currentFile), 97);
        endTest();
    }

    public void testAnonymousME12() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest.currentFile), 99);
        endTest();
    }

    public void testAnonymousME13() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest.currentFile), 101);
        endTest();
    }

    public void testAnonymousME25() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest.currentFile), 85);
        endTest();
    }

    public void testAnonymousME26() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest.currentFile), 87);
        endTest();
    }

    public void testAnonymousME211() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest.currentFile), 97);
        endTest();
    }

    public void testAnonymousME212() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest.currentFile), 99);
        endTest();
    }

    public void testAnonymousME213() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MEAnonymousModuleTest.currentFile), 101);
        endTest();
    }

    public void testAnonymousME21() throws Exception {
        startTest();
        openFile("modex|cc|cc14.js", "SimpleNode");
        MEAnonymousModuleTest.currentFile = "cc14.js";
        testCompletion(new EditorOperator(MEAnonymousModuleTest.currentFile), 77);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator(MEAnonymousModuleTest.currentFile);
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }
}
