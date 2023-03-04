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
package org.netbeans.modules.test.refactoring;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.modules.test.refactoring.actions.FindUsagesAction;
import org.netbeans.modules.test.refactoring.operators.FindUsagesDialogOperator;
import org.netbeans.modules.test.refactoring.operators.RefactoringResultOperator;

/**
 *
 * @author Ralph.Ruijs@oracle.com, Jiri.Prox@oracle.com, Marian.Mirilovic@oracle.com
 */
public class FindUsagesMethodTest extends FindUsagesTestCase {

    public FindUsagesMethodTest(String name) {
        super(name);
    }

    public static Test suite() {
        return JellyTestCase.emptyConfiguration().
                addTest(FindUsagesMethodTest.class, "testFUMethod").
                addTest(FindUsagesMethodTest.class, "testFUMethodInComment").
                addTest(FindUsagesMethodTest.class, "testFUOverriding").
                addTest(FindUsagesMethodTest.class, "testFUOverridingAndUsages").
                addTest(FindUsagesMethodTest.class, "testFUOverridingAndUsagesInComment").
                addTest(FindUsagesMethodTest.class, "testAllOptions").
                addTest(FindUsagesMethodTest.class, "testRadioButtonsAvailable").
                addTest(FindUsagesMethodTest.class, "testFindOverridingMethodForStaticAvailable").
                addTest(FindUsagesMethodTest.class, "testTabName").
                addTest(FindUsagesMethodTest.class, "testFUConstructor").
                suite();
    }

    public void testFUMethod() {
        findUsages("fumethod", "Test", 47, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
    }
	
    public void testFUMethodInComment() {
        findUsages("fumethod", "Test", 47, 19, FIND_USAGES | SEARCH_IN_COMMENTS);
    }

    public void testFUOverriding() {
        findUsages("fumethod", "Test", 47, 19, FIND_OVERRIDING_METHODS | NOT_SEARCH_IN_COMMENTS);
    }

    public void testFUOverridingAndUsages() {
        findUsages("fumethod", "Test", 47, 19, FIND_USAGES_AND_OVERRIDING_METHODS | NOT_SEARCH_IN_COMMENTS);
    }

    public void testFUOverridingAndUsagesInComment() {
        findUsages("fumethod", "Test", 47, 19, FIND_USAGES_AND_OVERRIDING_METHODS | SEARCH_IN_COMMENTS);
    }

    public void testAllOptions() {
        final String fileName = "Test";
        openSourceFile("fumethod", fileName);
        EditorOperator editor = new EditorOperator(fileName);
        editor.setCaretPosition(47, 19);
        editor.select(47, 19, 20);

        new FindUsagesAction().performPopup(editor);
        new EventTool().waitNoEvent(1000);

        FindUsagesDialogOperator findUsagesClassOperator = new FindUsagesDialogOperator();
        try {
            findUsagesClassOperator.getFindUsages().setSelected(true);
            findUsagesClassOperator.getFindOverriddingMethods().setSelected(true);
            findUsagesClassOperator.getFindUsagesAndOverridingMethods().setSelected(true);
            new EventTool().waitNoEvent(500);
            assertEquals(true, findUsagesClassOperator.getFind().isEnabled());
        } finally {
            if (findUsagesClassOperator != null) {
                findUsagesClassOperator.cancel();
            }
        }
    }

    public void testRadioButtonsAvailable() {
        final String fileName = "Test";
        openSourceFile("fumethod", fileName);
        EditorOperator editor = new EditorOperator(fileName);
        editor.setCaretPosition(51, 22);
        editor.select(51, 22, 23);

        new FindUsagesAction().performPopup(editor);
        new EventTool().waitNoEvent(5000);

        FindUsagesDialogOperator findUsagesClassOperator = new FindUsagesDialogOperator();
        assertNotNull("[Find Usages] radio button is not available", findUsagesClassOperator.getFindUsages());
        assertNotNull("[Find Overriding Methods] radio button is not available", findUsagesClassOperator.getFindOverriddingMethods());
        assertNotNull("[Find Usages and Overriding Methods] radio button is not available", findUsagesClassOperator.getFindUsagesAndOverridingMethods());
        findUsagesClassOperator.cancel();
    }

    public void testFindOverridingMethodForStaticAvailable() {
        final String fileName = "Test";
        openSourceFile("fumethod", fileName);
        EditorOperator editor = new EditorOperator(fileName);
        editor.setCaretPosition(55, 28);
        editor.select(55, 28, 29);

        new FindUsagesAction().performPopup(editor);
        new EventTool().waitNoEvent(5000);

        FindUsagesDialogOperator findUsagesClassOperator = null;
        findUsagesClassOperator = new FindUsagesDialogOperator();
        JRadioButtonOperator findOverriddingMethods = null;
        String message = "";
        Timeouts timeouts = JemmyProperties.getCurrentTimeouts();
        long origTimeout = timeouts.getTimeout("ComponentOperator.WaitComponentTimeout");
        try {
            timeouts.setTimeout("ComponentOperator.WaitComponentTimeout", 3000); // false expectation, so shorten the time
            findOverriddingMethods = findUsagesClassOperator.getFindOverriddingMethods();
        } catch (Exception ex) {
            message = ex.getMessage();
            log("Log expected exception : " + message);
        } finally {
            timeouts.setTimeout("ComponentOperator.WaitComponentTimeout", origTimeout);
        }

        String expected_message = "Wait AbstractButton with text \"Find Overriding Methods\" loaded (ComponentOperator.WaitComponentTimeout)";
        assertTrue("[Find Overriding Methods] radio button is not available", expected_message.equalsIgnoreCase(message) && findOverriddingMethods == null);
        findUsagesClassOperator.cancel();
    }

    public void testTabName() {
        FindUsagesMethodTest.browseChild = false;
        findUsages("fumethod", "Test", 47, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
        findUsages("fumethod", "Test", 47, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
        FindUsagesMethodTest.browseChild = true;

        RefactoringResultOperator furo = RefactoringResultOperator.getFindUsagesResult();
        JTabbedPaneOperator tabbedPane = furo.getTabbedPane();
        assertNotNull(tabbedPane);

        String title = tabbedPane.getTitleAt(tabbedPane.getTabCount() - 1);
        ref(title);
        getRef().flush();
    }

    public void testFUConstructor() {
        findUsages("fumethod", "Test", 59, 13, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
    }
}
