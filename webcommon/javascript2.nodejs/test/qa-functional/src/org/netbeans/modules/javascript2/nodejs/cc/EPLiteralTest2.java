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
public class EPLiteralTest2 extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testExports1",
        "testExports2",
        "testExports3",
        "testExports4",
        "testExports5",
        "testExports6",
        "testExports7",
        "testExports8",
        "testExports9",
        "testExports10",
        "testExports11",
        "testExports12"
    };

    public EPLiteralTest2(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(EPLiteralTest2.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        openFile("exprop|ex4.js", "SimpleNode");
        EPLiteralTest2.currentFile = "ex4.js";
        endTest();
    }

    public void testExports1() throws Exception {
        startTest();
        testCompletion(new EditorOperator(EPLiteralTest2.currentFile), 5);
        endTest();
    }

    public void testExports2() throws Exception {
        startTest();
        testCompletion(new EditorOperator(EPLiteralTest2.currentFile), 7);
        endTest();
    }

    public void testExports3() throws Exception {
        startTest();
        testCompletion(new EditorOperator(EPLiteralTest2.currentFile), 9);
        endTest();
    }

    public void testExports4() throws Exception {
        startTest();
        testCompletion(new EditorOperator(EPLiteralTest2.currentFile), 11);
        endTest();
    }

    public void testExports5() throws Exception {
        startTest();
        openFile("exprop|ex5.js", "SimpleNode");
        EPLiteralTest2.currentFile = "ex5.js";
        testCompletion(new EditorOperator(EPLiteralTest2.currentFile), 5);
        endTest();
    }

    public void testExports6() throws Exception {
        startTest();
        testCompletion(new EditorOperator(EPLiteralTest2.currentFile), 7);
        endTest();
    }

    public void testExports7() throws Exception {
        startTest();
        testCompletion(new EditorOperator(EPLiteralTest2.currentFile), 9);
        endTest();
    }

    public void testExports8() throws Exception {
        startTest();
        testCompletion(new EditorOperator(EPLiteralTest2.currentFile), 11);
        endTest();
    }

    public void testExports9() throws Exception {
        startTest();
        openFile("exprop|ex6.js", "SimpleNode");
        EPLiteralTest2.currentFile = "ex6.js";
        testCompletion(new EditorOperator(EPLiteralTest2.currentFile), 5);
        endTest();
    }

    public void testExports10() throws Exception {
        startTest();
        testCompletion(new EditorOperator(EPLiteralTest2.currentFile), 7);
        endTest();
    }

    public void testExports11() throws Exception {
        startTest();
        testCompletion(new EditorOperator(EPLiteralTest2.currentFile), 9);
        endTest();
    }

    public void testExports12() throws Exception {
        startTest();
        testCompletion(new EditorOperator(EPLiteralTest2.currentFile), 11);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator(EPLiteralTest2.currentFile);
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }
}
