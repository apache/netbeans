/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
