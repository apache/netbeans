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
public class PEParameterTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testParam1",
        "testParam2",
        "testParam3",
        "testParam4",
        "testParam5"
    };

    public PEParameterTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(PEParameterTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        openFile("doc|pe.js", "SimpleNode");
        endTest();
    }

    public void testParam1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("pe.js"), 5);
        endTest();
    }

    public void testParam2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("pe.js"), 8);
        endTest();
    }

    public void testParam3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("pe.js"), 11);
        endTest();
    }

    public void testParam4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("pe.js"), 13);
        endTest();
    }

    public void testParam5() throws Exception {
        startTest();
        testCompletion(new EditorOperator("pe.js"), 35);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator("pe.js");
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }
}
