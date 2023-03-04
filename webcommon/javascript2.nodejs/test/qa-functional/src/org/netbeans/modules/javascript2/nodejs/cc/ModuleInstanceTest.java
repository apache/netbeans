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
public class ModuleInstanceTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testInstance1",
        "testInstance2",
        "testInstance3",
        "testInstance4",
        "testInstance5",
        "testInstance6",
        "testInstance7",
        "testInstance8",
        "testInstance9",
        "testReference1",
        "testReference2",
        "testReference3",
        "testReference4",
        "testReference5",
        "testReference6",
        "testReference7",
        "testReference8",
        "testReference9"
    };

    public ModuleInstanceTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(ModuleInstanceTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        openFile("cc|cc3.js", "SimpleNode");
        endTest();
    }

    public void testInstance1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 31);
        endTest();
    }

    public void testInstance2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 33);
        endTest();
    }

    public void testInstance3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 35);
        endTest();
    }

    public void testInstance4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 37);
        endTest();
    }

    public void testInstance5() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 39);
        endTest();
    }

    public void testInstance6() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 41);
        endTest();
    }

    public void testInstance7() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 43);
        endTest();
    }

    public void testInstance8() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 45);
        endTest();
    }

    public void testInstance9() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 47);
        endTest();
    }

    public void testReference1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 51);
        endTest();
    }

    public void testReference2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 53);
        endTest();
    }

    public void testReference3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 55);
        endTest();
    }

    public void testReference4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 57);
        endTest();
    }

    public void testReference5() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 59);
        endTest();
    }

    public void testReference6() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 61);
        endTest();
    }

    public void testReference7() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 63);
        endTest();
    }

    public void testReference8() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 65);
        endTest();
    }

    public void testReference9() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 67);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator("cc3.js");
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }

}
