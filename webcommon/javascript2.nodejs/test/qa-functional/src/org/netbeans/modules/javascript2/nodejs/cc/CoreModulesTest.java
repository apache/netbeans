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
public class CoreModulesTest extends GeneralNodeJs {

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
        "testCore16",
        "testCore17",
        "testCore18",
        "testCore19",
        "testCore20",
        "testCore21",
        "testCore22",
        "testCore23",
        "testCore24",
        "testCore25",
        "testCore26",
        "testCore27",
        "testCore28",
        "testCore29"
    };

    public CoreModulesTest(String args) {
        super(args);
    }

    public static Test suite() {       
        return createModuleTest(CoreModulesTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        downloadGlobalNodeJS();
        evt.waitNoEvent(8000);
        openFile("cc|cc2.js", "SimpleNode");
        endTest();
    }

    public void testCore1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 11);
        endTest();
    }

    public void testCore2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 13);
        endTest();
    }

    public void testCore3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 15);
        endTest();
    }

    public void testCore4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 18);
        endTest();
    }

    public void testCore5() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 21);
        endTest();
    }

    public void testCore6() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 24);
        endTest();
    }

    public void testCore7() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 27);
        endTest();
    }

    public void testCore8() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 30);
        endTest();
    }

    public void testCore9() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 33);
        endTest();
    }

    public void testCore10() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 36);
        endTest();
    }

    public void testCore11() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 39);
        endTest();
    }

    public void testCore12() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 42);
        endTest();
    }

    public void testCore13() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 45);
        endTest();
    }

    public void testCore14() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 48);
        endTest();
    }

    public void testCore15() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 51);
        endTest();
    }

    public void testCore16() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 54);
        endTest();
    }

    public void testCore17() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 57);
        endTest();
    }

    public void testCore18() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 60);
        endTest();
    }

    public void testCore19() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 63);
        endTest();
    }

    public void testCore20() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 66);
        endTest();
    }

    public void testCore21() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 69);
        endTest();
    }

    public void testCore22() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 76);
        endTest();
    }

    public void testCore23() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 79);
        endTest();
    }

    public void testCore24() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 82);
        endTest();
    }

    public void testCore25() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 85);
        endTest();
    }

    public void testCore26() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 88);
        endTest();
    }

    public void testCore27() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 91);
        endTest();
    }

    public void testCore28() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 94);
        endTest();
    }

    public void testCore29() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 97);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator("cc2.js");
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }
}
