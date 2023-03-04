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
public class ExportsModuleTest extends GeneralNodeJs {

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
        "testExports24",
        "testExports25",
        "testExports26",
        "testExports27",
        "testExports28"
    };

    public ExportsModuleTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(ExportsModuleTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        openFile("cc|cc1.js", "SimpleNode");
        endTest();
    }

    public void testExports1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 12);
        endTest();
    }

    public void testExports2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 14);
        endTest();
    }

    public void testExports3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 16);
        endTest();
    }

    public void testExports4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 18);
        endTest();
    }

    public void testExports5() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 20);
        endTest();
    }

    public void testExports6() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 22);
        endTest();
    }

    public void testExports7() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 25);
        endTest();
    }

    public void testExports8() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 27);
        endTest();
    }

    public void testExports9() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 29);
        endTest();
    }

    public void testExports10() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 31);
        endTest();
    }

    public void testExports11() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 33);
        endTest();
    }

    public void testExports12() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 35);
        endTest();
    }

    public void testExports13() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 37);
        endTest();
    }

    public void testExports14() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 39);
        endTest();
    }

    public void testExports15() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 41);
        endTest();
    }

    public void testExports16() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 46);
        endTest();
    }

    public void testExports17() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 48);
        endTest();
    }

    public void testExports18() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 50);
        endTest();
    }

    public void testExports19() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 52);
        endTest();
    }

    public void testExports20() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 54);
        endTest();
    }

    public void testExports21() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 56);
        endTest();
    }

    public void testExports22() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 58);
        endTest();
    }

    public void testExports23() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 60);
        endTest();
    }

    public void testExports24() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 63);
        endTest();
    }

    public void testExports25() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 67);
        endTest();
    }

    public void testExports26() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 69);
        endTest();
    }

    public void testExports27() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 71);
        endTest();
    }

    public void testExports28() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 73);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator("cc1.js");
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }

}
