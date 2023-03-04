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
public class LessCompletionTest extends GeneralCSSPrep {

    private static final String PROJECT_NAME = "css_prep";

    public LessCompletionTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(LessCompletionTest.class).addTest(
                        "openProject",
                        "testCompletionL4",
                        "testCompletionL8",
                        "testCompletionL11",
                        "testCompletionL18",
                        "testCompletionL30",
                        "testCompletionL35",
                        "testCompletionL41",
                        "testCompletionL45",
                        "testCompletionL47"
                ).enableModules(".*").clusters(".*"));
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects(LessCompletionTest.PROJECT_NAME);
        evt.waitNoEvent(3000);
        openFile("cc.less", LessCompletionTest.PROJECT_NAME);
        endTest();
    }

    public void testCompletionL4() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 4);
        endTest();
    }

    public void testCompletionL8() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 8);
        endTest();
    }

    public void testCompletionL11() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 11);
        endTest();
    }

    public void testCompletionL18() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 18);
        endTest();
    }

    public void testCompletionL30() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 30);
        endTest();
    }

    public void testCompletionL35() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 35);
        endTest();
    }

    public void testCompletionL41() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 41);
        endTest();
    }

    public void testCompletionL45() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 45);
        endTest();
    }

    public void testCompletionL47() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 47);
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
        
        int stop = Integer.parseInt(config[4].trim());
        for(int i=0; i < stop; i++){
            eo.pressKey(KeyEvent.VK_LEFT);
        }
        
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, config[3].split(","));
        completion.listItself.hideAll();
       for(int i =0; i < config[2].length();i++){
           eo.pressKey(KeyEvent.VK_BACK_SPACE);
       }
    }

}
