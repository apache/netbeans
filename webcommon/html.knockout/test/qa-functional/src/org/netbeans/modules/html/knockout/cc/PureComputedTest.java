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
 * @author vriha
 */
public class PureComputedTest extends GeneralKnockout {

    public PureComputedTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(PureComputedTest.class).addTest(
                        "createApplication",
                        "testSimpleBinding",
                        "testMultipleBinding",
                        "testTemplate"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void createApplication() {
        try {
            startTest();
            openDataProjects("sample");
            openFile("purecomp.html", "sample");
            waitScanFinished();
            PureComputedTest.originalContent = new EditorOperator("purecomp.html").getText();
            endTest();
        } catch (IOException ex) {
            Logger.getLogger(PureComputedTest.class.getName()).log(Level.INFO, "Opening project", ex);
        }
    }

    private void testCompletion(int lineNumber, EditorOperator eo) {

        waitScanFinished();
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("<!--cc;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPosition(lineNumber + 1, Integer.parseInt(config[1]));

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
                    eo.pressKey(KeyEvent.VK_BACK_SPACE);
                    evt.waitNoEvent(50);
                }
            }
        }

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
                Logger.getLogger(PureComputedTest.class.getName()).log(Level.INFO, "", ex);
                fail(ex.getMessage());
            }
        }
        eo.pressKey(KeyEvent.VK_ESCAPE);

    }

    public void testSimpleBinding() {
        startTest();
        testCompletion(12, new EditorOperator("purecomp.html"));
        endTest();
    }

    public void testMultipleBinding() {
        startTest();
        testCompletion(15, new EditorOperator("purecomp.html"));
        endTest();
    }

    public void testTemplate() {
        startTest();
        testCompletion(18, new EditorOperator("purecomp.html"));
        endTest();
    }

    @Override
    public void tearDown() throws Exception {
        EditorOperator eo = new EditorOperator("purecomp.html");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(KeyEvent.VK_DELETE);
        eo.insert(PureComputedTest.originalContent);
        eo.save();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        evt.waitNoEvent(1000);
    }
}
