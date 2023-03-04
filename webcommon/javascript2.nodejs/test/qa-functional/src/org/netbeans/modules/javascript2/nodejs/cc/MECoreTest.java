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
public class MECoreTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testCore1",
        "testCore2",
        "testCore3",
        "testCore4",
        "testCore5",
        "testCore6",
        "testCore7",
        "testCore8",
        "testCore9",
        "testCore10",
        "testCore11",
        "testCore12",
        "testCore13",
        "testCore14",
        "testCore15",
        "testCore16"
    };

    public MECoreTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(MECoreTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        downloadGlobalNodeJS();
        evt.waitNoEvent(8000);
        openFile("modex|cc|cc2.js", "SimpleNode");
        MECoreTest.currentFile = "cc2.js";
        endTest();
    }

    public void testCore1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 8);
        endTest();
    }

    public void testCore2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 10);
        endTest();
    }

    public void testCore3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 12);
        endTest();
    }

    public void testCore4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 14);
        endTest();
    }

    public void testCore5() throws Exception {
        startTest();
        openFile("modex|cc|cc8.js", "SimpleNode");
        MECoreTest.currentFile = "cc8.js";
        testCompletion(new EditorOperator("cc8.js"), 8);
        endTest();
    }

    public void testCore6() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc8.js"), 10);
        endTest();
    }

    public void testCore7() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc8.js"), 12);
        endTest();
    }

    public void testCore8() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc8.js"), 14);
        endTest();
    }

    public void testCore9() throws Exception {
        startTest();
        openFile("modex|cc|cc9.js", "SimpleNode");
        MECoreTest.currentFile = "cc9.js";
        testCompletion(new EditorOperator("cc9.js"), 8);
        endTest();
    }

    public void testCore10() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc9.js"), 10);
        endTest();
    }

    public void testCore11() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc9.js"), 12);
        endTest();
    }

    public void testCore12() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc9.js"), 14);
        endTest();
    }

    public void testCore13() throws Exception {
        startTest();
        openFile("modex|cc|cc10.js", "SimpleNode");
        MECoreTest.currentFile = "cc10.js";
        testCompletion(new EditorOperator("cc10.js"), 8);
        endTest();
    }

    public void testCore14() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc10.js"), 10);
        endTest();
    }

    public void testCore15() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc10.js"), 12);
        endTest();
    }

    public void testCore16() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc10.js"), 14);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator(MECoreTest.currentFile);
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }
}
