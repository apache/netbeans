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
public class ModuleLiteralTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testLiteral1",
        "testLiteral2",
        "testLiteral3",
        "testLiteral4",
        "testLiteral5",
        "testLiteral6",
        "testLiteralRef1",
        "testLiteralRef2",
        "testLiteralRef3",
        "testLiteralRef4",
        "testLiteralRef5",
        "testLiteralRef6"
    };

    public ModuleLiteralTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(ModuleLiteralTest.class, tests);
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

    public void testLiteral1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 4);
        endTest();
    }

    public void testLiteral2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 6);
        endTest();
    }

    public void testLiteral3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 8);
        endTest();
    }

    public void testLiteral4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 10);
        endTest();
    }

    public void testLiteral5() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 12);
        endTest();
    }

    public void testLiteral6() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 14);
        endTest();
    }

    public void testLiteralRef1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 17);
        endTest();
    }

    public void testLiteralRef2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 19);
        endTest();
    }

    public void testLiteralRef3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 21);
        endTest();
    }

    public void testLiteralRef4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 23);
        endTest();
    }

    public void testLiteralRef5() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 25);
        endTest();
    }

    public void testLiteralRef6() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 27);
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
