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
public class MELiteralTest extends GeneralNodeJs {

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
        "testExports12",
        "testExports13",
        "testExports14",
        "testExports15",
        "testExports16",
        "testExports17",
        "testExports18",
        "testExports19",
        "testExports20",
        "testExports21",
        "testExports22",
        "testExports23",
        "testExports24"
    };

    public MELiteralTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(MELiteralTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        openFile("modex|cc|cc1.js", "SimpleNode");
        MELiteralTest.currentFile = "cc1.js";
        endTest();
    }

    public void testExports1() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 4);
        endTest();
    }

    public void testExports2() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 6);
        endTest();
    }

    public void testExports3() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 8);
        endTest();
    }

    public void testExports4() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 10);
        endTest();
    }

    public void testExports5() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 12);
        endTest();
    }

    public void testExports6() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 14);
        endTest();
    }

    public void testExports7() throws Exception {
        startTest();
        openFile("modex|cc|cc5.js", "SimpleNode");
        MELiteralTest.currentFile = "cc5.js";
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 4);
        endTest();
    }

    public void testExports8() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 6);
        endTest();
    }

    public void testExports9() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 8);
        endTest();
    }

    public void testExports10() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 10);
        endTest();
    }

    public void testExports11() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 12);
        endTest();
    }

    public void testExports12() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 14);
        endTest();
    }

    public void testExports13() throws Exception {
        startTest();
        openFile("modex|cc|cc6.js", "SimpleNode");
        MELiteralTest.currentFile = "cc6.js";
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 4);
        endTest();
    }

    public void testExports14() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 6);
        endTest();
    }

    public void testExports15() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 8);
        endTest();
    }

    public void testExports16() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 10);
        endTest();
    }

    public void testExports17() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 12);
        endTest();
    }

    public void testExports18() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 14);
        endTest();
    }

    public void testExports19() throws Exception {
        startTest();
        openFile("modex|cc|cc7.js", "SimpleNode");
        MELiteralTest.currentFile = "cc7.js";
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 4);
        endTest();
    }

    public void testExports20() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 6);
        endTest();
    }

    public void testExports21() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 8);
        endTest();
    }

    public void testExports22() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 10);
        endTest();
    }

    public void testExports23() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 12);
        endTest();
    }

    public void testExports24() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 14);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator(MELiteralTest.currentFile);
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }

}
