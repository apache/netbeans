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
package org.netbeans.modules.javascript2.editor.qaf.cc;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;

/**
 *
 * @author vriha
 */
public class ParamDefSameTest extends GeneralJavaScript {

    static final String[] tests = new String[]{
        "openProject",
        "testSimpleTop",
        "testSimpleMethod",
        "testSimpleNested",
        "testDotTop",
        "testDotMethod",
        "testDotNested",
        "testTildaTop",
        "testTildaMethod",
        "testTildaNested",
        "testPropTop",
        "testPropTop2",
        "testPropNested"
    };

    public ParamDefSameTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return createModuleTest(ParamDefSameTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralJavaScript.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("completionTest");
        evt.waitNoEvent(2000);
        openFile("same.js", "completionTest");
        endTest();
    }

    public void testSimpleTop() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 192);
        endTest();
    }

    public void testSimpleMethod() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 276);
        endTest();
    }

    public void testSimpleNested() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 289);
        endTest();
    }

    public void testDotTop() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 204);
        endTest();
    }

    public void testDotMethod() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 278);
        endTest();
    }

    public void testDotNested() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 291);
        endTest();
    }

    public void testTildaTop() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 216);
        endTest();
    }

    public void testTildaMethod() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 280);
        endTest();
    }

    public void testTildaNested() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 293);
        endTest();
    }

    public void testPropTop() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 226);
        endTest();
    }

    public void testPropTop2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 237);
        endTest();
    }

    public void testPropNested() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 295);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralJavaScript.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator("same.js");
        eo.setCaretPositionToEndOfLine(GeneralJavaScript.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(500);

    }
}
