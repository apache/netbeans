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
public class AnonymousModuleTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testAnonymous1",
        "testAnonymous2",
        "testAnonymous3",
        "testAnonymous4",
        "testAnonymous5",
        "testAnonymous6",
        "testAnonymous7",
        "testAnonymous8",
        "testAnonymous9",
        "testAnonymous10",
        "testAnonymous11",
        "testAnonymous12",
        "testAnonymous13"
    };

    public AnonymousModuleTest(String args) {
        super(args);
    }

    public static Test suite() {
       return createModuleTest(AnonymousModuleTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        openFile("cc|cc3.js", "SimpleNode");
        AnonymousModuleTest.currentFile = "cc3.js";
        endTest();
    }

    public void testAnonymous1() throws Exception {
        startTest();
        testCompletion(new EditorOperator(AnonymousModuleTest.currentFile), 77);
        endTest();
    }

    public void testAnonymous2() throws Exception {
        startTest();
        testCompletion(new EditorOperator(AnonymousModuleTest.currentFile), 79);
        endTest();
    }

    public void testAnonymous3() throws Exception {
        startTest();
        testCompletion(new EditorOperator(AnonymousModuleTest.currentFile), 81);
        endTest();
    }

    public void testAnonymous4() throws Exception {
        startTest();
        testCompletion(new EditorOperator(AnonymousModuleTest.currentFile), 83);
        endTest();
    }

    public void testAnonymous5() throws Exception {
        startTest();
        testCompletion(new EditorOperator(AnonymousModuleTest.currentFile), 85);
        endTest();
    }

    public void testAnonymous6() throws Exception {
        startTest();
        testCompletion(new EditorOperator(AnonymousModuleTest.currentFile), 87);
        endTest();
    }

    public void testAnonymous7() throws Exception {
        startTest();
        testCompletion(new EditorOperator(AnonymousModuleTest.currentFile), 89);
        endTest();
    }

    public void testAnonymous8() throws Exception {
        startTest();
        testCompletion(new EditorOperator(AnonymousModuleTest.currentFile), 91);
        endTest();
    }

    public void testAnonymous9() throws Exception {
        startTest();
        testCompletion(new EditorOperator(AnonymousModuleTest.currentFile), 93);
        endTest();
    }

    public void testAnonymous10() throws Exception {
        startTest();
        testCompletion(new EditorOperator(AnonymousModuleTest.currentFile), 95);
        endTest();
    }

    public void testAnonymous11() throws Exception {
        startTest();
        testCompletion(new EditorOperator(AnonymousModuleTest.currentFile), 97);
        endTest();
    }

    public void testAnonymous12() throws Exception {
        startTest();
        testCompletion(new EditorOperator(AnonymousModuleTest.currentFile), 99);
        endTest();
    }

    public void testAnonymous13() throws Exception {
        startTest();
        testCompletion(new EditorOperator(AnonymousModuleTest.currentFile), 101);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator(AnonymousModuleTest.currentFile);
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }
}
