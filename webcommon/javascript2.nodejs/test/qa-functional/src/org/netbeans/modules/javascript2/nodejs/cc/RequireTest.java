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
public class RequireTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testCoreModules",
        "testCurrentFolder",
        "testParentFolder",
        "testSiblingFolder"
    };

    public RequireTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(RequireTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        downloadGlobalNodeJS();
        evt.waitNoEvent(8000);
        openFile("cc|cc1.js", "SimpleNode");
        endTest();
    }

    public void testCoreModules() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 76);
        endTest();
    }

    public void testCurrentFolder() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 78);
        endTest();
    }

    public void testParentFolder() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 80);
        endTest();
    }

    public void testSiblingFolder() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc1.js"), 82);
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
