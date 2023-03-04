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

package org.netbeans.jellytools.modules.junit.testcases;

import java.util.ArrayList;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;

/**
 * Class with helpers for easy creating jemmy/jelly tests
 *
 * @author Jiri Vagner, Pavel Pribyl
 */
public abstract class ExtJellyTestCaseForJunit3 extends JellyTestCase {

    private static int MY_WAIT_MOMENT = 500;

    public static String TEST_PROJECT_NAME = "JUnit3TestProject"; // NOI18N
    public static String TEST_PACKAGE_NAME = "junit3testproject"; // NOI18N
    public static String DELETE_OBJECT_CONFIRM = "Confirm Object Deletion"; // NOI18N
    /* Skip file (JFrame,Frame, JDialog, ...) delete in the end of each test */
    public Boolean DELETE_FILES = false;

    /** Constructor required by JUnit */
    public ExtJellyTestCaseForJunit3(String testName) {
        super(testName);
    }

    
    /**
     * Find a substring in a string
     * Test fail() method is called, when code string doesnt contain stringToFind.
     * @param stringToFind string to find
     * @param string to search
     */
    private void findStringInCode(String stringToFind, String code) {
        if (!code.contains(stringToFind)) {
            fail("Missing string \"" + stringToFind + "\" in code."); // NOI18N
        }
    }

    /**
     * Find a strings in a code
     * @param lines array list of strings to find
     * @param designer operator "with text"
     */
    public void findInCode(ArrayList<String> lines, EditorOperator editor) {
        String code = editor.getText();

        for (String line : lines) {
            findStringInCode(line, code);
        }
    }

    /**
     * Find a string in a code
     * @param lines array list of strings to find
     * @param designer operator "with text"
     */
    public void findInCode(String stringToFind, EditorOperator editor) {
        findStringInCode(stringToFind, editor.getText());
    }


    /**
     * Miss a string in a code
     * Test fail() method is called, when code contains stringToFind string
     * @param stringToFind
     * @param designer operator "with text"
     */
    public void missInCode(String stringToFind, EditorOperator editor) {
        if (editor.getText().contains(stringToFind)) {
            fail("String \"" + stringToFind + "\" found in code."); // NOI18N
        }
    }

    /**
     * Calls Jelly waitNoEvent()
     * @param quiet time (miliseconds)
     */
    public static void waitNoEvent(long waitTimeout) {
        new org.netbeans.jemmy.EventTool().waitNoEvent(waitTimeout);
    }

    /**
     * Calls Jelly waitNoEvent() with MY_WAIT_MOMENT
     */
    public static void waitAMoment() {
        waitNoEvent(MY_WAIT_MOMENT);
    }
    
     /**
     * Sets all checkboxes inside Junit create tests dialog to checked
     */
    public static void checkAllCheckboxes(NbDialogOperator ndo) {
        for(int i = 0; i < 7; i++) {
            new JCheckBoxOperator(ndo, i).setSelected(true);
        }
    }

}
