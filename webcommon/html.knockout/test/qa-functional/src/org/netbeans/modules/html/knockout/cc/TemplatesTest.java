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
package org.netbeans.modules.html.knockout.cc;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.html.knockout.GeneralKnockout;

/**
 *
 * @author vriha
 */
public class TemplatesTest extends GeneralKnockout {

    public TemplatesTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(TemplatesTest.class).addTest(
                        "openProject",
                        "testBindedObject",
                        "testBindedForeach",
                        "testBindedNested",
                        "testBindedInner",
                        "testBindedInner2",
                        "testBindedNested2"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("sample");
        openFile("template.html", "sample");
        openFile("template.js", "sample");
        TemplatesTest.originalContent = new EditorOperator("template.html").getText();
        waitScanFinished();
        endTest();
    }

    public void testBindedObject() {
        startTest();
        doTest(17, 34, new String[]{"name", "credits", "date", "test", "$context", "Date", "Number", "Math"});
        endTest();
    }

    public void testBindedForeach() {
        startTest();
        doTest(24, 34, new String[]{"name1", "credits1", "date1", "test1", "$context", "Date", "Number", "Math"});
        endTest();
    }

    public void testBindedNested() {
        startTest();
        doTest(40, 36, new String[]{"season", "month", "$context", "Date", "Number", "Math"});
        endTest();
    }

    public void testBindedInner() {
        startTest();
        doTest(49, 35, new String[]{"getDay", "UTC"});
        endTest();
    }

    public void testBindedInner2() {
        startTest();
        doTest(55, 35, new String[]{"a", "create"});
        endTest();
    }

    public void testBindedNested2() {
        startTest();
        doTest(65, 34, new String[]{"name", "lastName"});
        endTest();
    }

    public void doTest(int lineNumber, int columnNumber, String[] result) {
        EditorOperator eo = new EditorOperator("template.html");
        eo.setCaretPosition(lineNumber, columnNumber);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(500);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, result);
    }

    @Override
    public void tearDown() throws Exception {
        EditorOperator eo = new EditorOperator("template.html");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(KeyEvent.VK_DELETE);
        eo.insert(TemplatesTest.originalContent);
        eo.save();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        eo.clickMouse();
        evt.waitNoEvent(2500);
    }

}
