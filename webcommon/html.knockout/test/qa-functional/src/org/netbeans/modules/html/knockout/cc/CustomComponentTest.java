/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.html.knockout.GeneralKnockout;

/**
 *
 * @author Vladimir Riha (vriha)
 */
public class CustomComponentTest extends GeneralKnockout {

    public CustomComponentTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CustomComponentTest.class).addTest(
                        "createApplication",
                        "testComponentCont",
                        "testComponent",
//                        "testParams"
                        "testAttributesCont",
                        "testCompBinding",
                        "testCompBindingParam",
                        "testCompBindingName",
                        "testCompBindingName2",
                        "testCompBindingParams",
                        "testCompBindingNameCont",
                        "testCompBindingName2Cont",
                        "testParamsCompletion1",
                        "testParamsCompletion2"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void createApplication() {
        try {
            startTest();
            openDataProjects("sample");
            openFile("component.html", "sample");
            waitScanFinished();
            CustomComponentTest.originalContent = new EditorOperator("component.html").getText();
            endTest();
        } catch (IOException ex) {
            Logger.getLogger(CustomComponentTest.class.getName()).log(Level.INFO, "Opening project", ex);
        }
    }

    public void testComponentCont() {
        startTest();
        testHtmlCompletion(9, new EditorOperator("component.html"), true, false);
        endTest();
    }

    public void testAttributesCont() {
        startTest();
        testHtmlCompletion(12, new EditorOperator("component.html"), true, false);
        endTest();
    }

    public void testComponent() {
        startTest();
        testHtmlCompletion(9, new EditorOperator("component.html"), false, false);
        endTest();
    }

    public void testParams() {
        startTest();
        testJsCompletion(14, new EditorOperator("component.html"));
        endTest();
    }

    public void testCompBinding() {
        startTest();
        testHtmlCompletion(16, new EditorOperator("component.html"), true, true);
        endTest();
    }

    public void testCompBindingParam() {
        startTest();
        testHtmlCompletion(18, new EditorOperator("component.html"), true, true);
        endTest();
    }

    public void testCompBindingName() {
        startTest();
        testHtmlCompletion(20, new EditorOperator("component.html"), false, true);
        endTest();
    }
    
    public void testParamsCompletion1() {
        startTest();
        testHtmlCompletion(27, new EditorOperator("component.html"), false, false);
        endTest();
    }
    
    public void testParamsCompletion2() {
        startTest();
        testHtmlCompletion(29, new EditorOperator("component.html"), false, true);
        endTest();
    }

    public void testCompBindingName2() {
        startTest();
        EditorOperator eo = new EditorOperator("component.html");

        int lineNumber = 22;
        waitScanFinished();
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("<!--cc;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPosition(lineNumber + 1, Integer.parseInt(config[1]));
        eo.insert(config[2]);

        int back = Integer.parseInt(config[3]);
        for (int i = 0; i < back; i++) {
            eo.pressKey(KeyEvent.VK_LEFT);
        }

        evt.waitNoEvent(100);

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionHtmlItems(cjo, config[4].split(","));
        String negResults = config[7].trim();
        negResults = negResults.substring(0, negResults.lastIndexOf("--"));
        checkCompletionDoesntContainHtmlItems(cjo, negResults.split(","));

        if (config[5].length() > 0 && config[6].length() > 0) {
            try {
                String prefix = Character.toString(config[5].charAt(0));
                type(eo, prefix);

                completion = getCompletion();
                cjo = completion.listItself;
                checkCompletionHtmlMatchesPrefix(cjo.getCompletionItems(), prefix);
                int index = findItemIndex(cjo, config[5]);
                for (int j = 0; j < index; j++) {
                    eo.pressKey(KeyEvent.VK_DOWN);
                }

                eo.pressKey(KeyEvent.VK_ENTER);

                assertTrue("Wrong completion result", eo.getText(lineNumber).contains(config[6].replaceAll("\\|", "")));
            } catch (Exception ex) {
                Logger.getLogger(CustomComponentTest.class.getName()).log(Level.INFO, "", ex);
                fail(ex.getMessage());
            }
        }

        endTest();
    }

    public void testCompBindingNameCont() {
        startTest();
        testHtmlCompletion(20, new EditorOperator("component.html"), true, true);
        endTest();
    }

    public void testCompBindingName2Cont() {
        startTest();
        testHtmlCompletion(22, new EditorOperator("component.html"), true, true);
        endTest();
    }

    public void testCompBindingParams() {
        startTest();
        testHtmlCompletion(24, new EditorOperator("component.html"), false, true);
        endTest();
    }

    private void testHtmlCompletion(int lineNumber, EditorOperator eo, boolean continuous, boolean fixQuotes) {

        waitScanFinished();
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("<!--cc;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPosition(lineNumber + 1, Integer.parseInt(config[1]));
        boolean isSingleMatch = false;
        if (fixQuotes) {
            int iLimit = config[2].length();
            char currentChar;
            String currentLetter;
            String currentLine;
            boolean isClosing;

            for (int i = 0; i < iLimit; i++) {
                currentChar = config[2].charAt(i);
                currentLetter = Character.toString(currentChar);
                eo.typeKey(currentChar);
                if (currentLetter.equalsIgnoreCase("'") || currentLetter.equalsIgnoreCase("\"")) {
                    currentLine = eo.getText(lineNumber + 1).trim();
                    isClosing = Character.toString(currentLine.charAt(currentLine.length() - 2)).equals(currentLetter);
                    if (isClosing) {
                        eo.setCaretPositionToEndOfLine(lineNumber + 1);
                        evt.waitNoEvent(30);
                        eo.pressKey(KeyEvent.VK_BACK_SPACE);
                        evt.waitNoEvent(50);
                    }
                }
            }
        } else {
            type(eo, config[2]);
        }

        int back = Integer.parseInt(config[3]);
        for (int i = 0; i < back; i++) {
            eo.pressKey(KeyEvent.VK_LEFT);
        }

        evt.waitNoEvent(100);

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionHtmlItems(cjo, config[4].split(","));

        String negResults = config[7].trim();
        negResults = negResults.substring(0, negResults.lastIndexOf("--"));
        checkCompletionDoesntContainHtmlItems(cjo, negResults.split(","));

        if (config[5].length() > 0 && config[6].length() > 0) {
            try {
                String prefix = Character.toString(config[5].charAt(0));
                type(eo, prefix);
                if (!continuous) {
                    eo.pressKey(KeyEvent.VK_ESCAPE);
                    evt.waitNoEvent(100);
                    eo.typeKey(' ', InputEvent.CTRL_MASK);
                    isSingleMatch = isSingleOption(prefix, cjo);
                }

                if (!isSingleMatch) {
                    completion = getCompletion();
                    cjo = completion.listItself;
                    checkCompletionHtmlMatchesPrefix(cjo.getCompletionItems(), prefix);
                    int index = findItemIndex(cjo, config[5]);
                    for (int j = 0; j < index; j++) {
                        eo.pressKey(KeyEvent.VK_DOWN);
                    }

                    eo.pressKey(KeyEvent.VK_ENTER);
                }
                assertTrue("Wrong completion result", eo.getText(lineNumber).contains(config[6].replaceAll("\\|", "")));
            } catch (Exception ex) {
                Logger.getLogger(CustomComponentTest.class.getName()).log(Level.INFO, "", ex);
                fail(ex.getMessage());
            }
        }

    }

    private void testJsCompletion(int lineNumber, EditorOperator eo) {

        waitScanFinished();
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("<!--cc;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPosition(lineNumber + 1, Integer.parseInt(config[1]));
        type(eo, config[2]);
        evt.waitNoEvent(100);
        eo.pressKey(KeyEvent.VK_ESCAPE);
        evt.waitNoEvent(100);
        int back = Integer.parseInt(config[3]);
        for (int i = 0; i < back; i++) {
            eo.pressKey(KeyEvent.VK_LEFT);
        }

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, config[4].split(","));
        checkCompletionDoesntContainItems(cjo, config[7].split(","));

        String negResults = config[7].trim();
        negResults = negResults.substring(0, negResults.lastIndexOf("--"));
        checkCompletionDoesntContainItems(cjo, negResults.split(","));

        if (config[5].length() > 0 && config[6].length() > 0) {
            try {
                String prefix = Character.toString(config[5].charAt(0));
                type(eo, prefix);
                completion = getCompletion();
                cjo = completion.listItself;
                checkCompletionMatchesPrefix(cjo.getCompletionItems(), prefix);
                evt.waitNoEvent(500);
                cjo.clickOnItem(config[5]);
                eo.pressKey(KeyEvent.VK_ENTER);
                assertTrue("Wrong completion result", eo.getText(lineNumber + 1).contains(config[6].replaceAll("|", "")));
                completion.listItself.hideAll();
            } catch (Exception ex) {
                Logger.getLogger(CustomComponentTest.class.getName()).log(Level.INFO, "", ex);
                fail(ex.getMessage());
            }
        }
        eo.pressKey(KeyEvent.VK_ESCAPE);

    }

    @Override
    public void tearDown() throws Exception {
        EditorOperator eo = new EditorOperator("component.html");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(KeyEvent.VK_DELETE);
        eo.insert(CustomComponentTest.originalContent);
        eo.save();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        evt.waitNoEvent(1000);
    }
}
