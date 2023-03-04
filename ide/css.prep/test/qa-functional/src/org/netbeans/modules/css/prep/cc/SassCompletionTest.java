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
package org.netbeans.modules.css.prep.cc;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.css.prep.GeneralCSSPrep;

/**
 *
 * @author Vladimir Riha (vriha)
 */
public class SassCompletionTest extends GeneralCSSPrep {

    private static final String PROJECT_NAME = "css_prep";
    
    public SassCompletionTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(SassCompletionTest.class).addTest(
                        "openProject",
                        "testCompletionL4",
                        "testCompletionL8",
                        "testCompletionL11",
                        "testCompletionL18",
                        "testCompletionL23",
                        "testCompletionL28",
                        "testCompletionL33",
                        "testCompletionL37",
                        "testCompletionL43",
//                        "testCompletionL50",
                        "testCompletionL52",
                        "testCompletionL64",
                        "testCompletionL68",
                        "testCompletionL70",
                        "testCompletionL72"
                ).enableModules(".*").clusters(".*"));
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects(SassCompletionTest.PROJECT_NAME);
        evt.waitNoEvent(3000);
        openFile("cc.scss", SassCompletionTest.PROJECT_NAME);
        endTest();
    }
    
    public void testCompletionL4() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 4);
        endTest();
    }
    
    public void testCompletionL8() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 8);
        endTest();
    }
    public void testCompletionL11() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 11);
        endTest();
    }
    public void testCompletionL18() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 18);
        endTest();
    }
    public void testCompletionL23() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 23);
        endTest();
    }
    public void testCompletionL28() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 28);
        endTest();
    }
    public void testCompletionL33() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 33);
        endTest();
    }
    public void testCompletionL37() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 37);
        endTest();
    }
    public void testCompletionL43() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 43);
        endTest();
    }
    public void testCompletionL50() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 50);
        endTest();
    }
    public void testCompletionL52() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 52);
        endTest();
    }
    public void testCompletionL64() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 64);
        endTest();
    }
    public void testCompletionL68() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 68);
        endTest();
    }
    public void testCompletionL70() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 70);
        endTest();
    }
    public void testCompletionL72() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.scss"), 72);
        endTest();
    }
    
    public void doTest(EditorOperator eo, int lineNumber) {
        waitScanFinished();
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("/*cc;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPositionToEndOfLine(Integer.parseInt(config[1]));
        type(eo, config[2]);

        String steps = config[4].substring(0, config[4].indexOf("*")).trim();
        int stop = Integer.parseInt(steps);
        for (int i = 0; i < stop; i++) {
            eo.pressKey(KeyEvent.VK_LEFT);
        }

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, config[3].split(","));
        completion.listItself.hideAll();
        eo.setCaretPositionToEndOfLine(eo.getLineNumber());
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length()-1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }
    }
}
