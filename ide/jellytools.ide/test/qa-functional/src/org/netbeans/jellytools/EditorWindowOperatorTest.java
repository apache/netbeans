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
package org.netbeans.jellytools;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.actions.CloneViewAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;

/**
 * Test of org.netbeans.jellytools.EditorWindowOperator.
 * Order of tests is important.
 * @author Jiri Skrivanek
 */
public class EditorWindowOperatorTest extends JellyTestCase {

    private static final String SAMPLE_CLASS_1 = "SampleClass1.java";
    private static final String SAMPLE_CLASS_2 = "SampleClass2.java";
    public static final String[] tests = new String[]{
        "testSelectPage",
        "testGetEditor",
        "testSelectDocument",
        "testJumpLeft",
        "testMoveTabsRight",
        "testMoveTabsLeft",
        "testVerify",
        "testCloseDiscard"
    };

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public EditorWindowOperatorTest(java.lang.String testName) {
        super(testName);
    }

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(EditorWindowOperatorTest.class, tests);
    }

    /** Redirect output to log files, wait before each test case. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }

    /** Test of selectPage method. */
    public void testSelectPage() {
        // next tests depends on this
        startTest();
        Node sourcePackagesNode = new Node(new ProjectsTabOperator().getProjectRootNode("SampleProject"), "Source Packages");
        Node sample1 = new Node(sourcePackagesNode, "sample1");  // NOI18N
        Node sample2 = new Node(sourcePackagesNode, "sample1.sample2");  // NOI18N
        Node sampleClass1 = new Node(sample1, SAMPLE_CLASS_1);
        OpenAction openAction = new OpenAction();
        openAction.performAPI(sampleClass1);
        // close all => it satisfies only sample classes are opened
        EditorWindowOperator.closeDiscard();
        openAction.performAPI(sampleClass1);
        Node sampleClass2 = new Node(sample2, SAMPLE_CLASS_2);
        openAction.performAPI(sampleClass2);
        EditorWindowOperator.selectPage(SAMPLE_CLASS_1);
        EditorWindowOperator.selectPage(SAMPLE_CLASS_2);
        assertTrue("Page " + SAMPLE_CLASS_2 + " not selected.",
                EditorWindowOperator.getEditor().getName().indexOf(SAMPLE_CLASS_2) != -1);
        EditorWindowOperator.selectPage(SAMPLE_CLASS_1);
        assertTrue("Page " + SAMPLE_CLASS_1 + " not selected.",
                EditorWindowOperator.getEditor().getName().indexOf(SAMPLE_CLASS_1) != -1);
        // finished succesfully
        endTest();
    }

    /** Test of txtEditorPane method. */
    public void testGetEditor() {
        // next tests depends on this
        startTest();
        assertEquals("Wrong editor pane found.", SAMPLE_CLASS_1, EditorWindowOperator.getEditor().getName());
        EditorWindowOperator.selectPage(SAMPLE_CLASS_2);
        assertEquals("Wrong editor pane found.", SAMPLE_CLASS_1, EditorWindowOperator.getEditor(0).getName());
        assertEquals("Wrong editor pane found.", SAMPLE_CLASS_2, EditorWindowOperator.getEditor(SAMPLE_CLASS_2).getName());
        // finished succesfully
        endTest();
    }

    /** Test of selectDocument methods. */
    public void testSelectDocument() {
        // this test depends on previous
        startTest();
        // but not block anything else
        clearTestStatus();

        EditorWindowOperator.selectDocument(SAMPLE_CLASS_1);
        assertEquals("Wrong document selected.", SAMPLE_CLASS_1, EditorWindowOperator.getEditor().getName());
        EditorWindowOperator.selectDocument(1);
        assertEquals("Wrong document selected.", SAMPLE_CLASS_2, EditorWindowOperator.getEditor().getName());
    }

    /** Test of jumpLeft method. */
    public void testJumpLeft() {
        // next tests depends on this
        startTest();
        // clones selected document several times to test control buttons
        for (int i = 0; i < 10; i++) {
            new CloneViewAction().performAPI();
        }
        // click on leftmost tab until is not fully visible
        int count = 0;
        while (EditorWindowOperator.jumpLeft() && count++ < 100);
        // if it is still possible to jump left, wait a little and do jumpLeft again
        if (EditorWindowOperator.jumpLeft()) {
            new EventTool().waitNoEvent(3000);
            while (EditorWindowOperator.jumpLeft() && count++ < 100);
        }
        assertFalse("Leftmost tab should not be partially hidden.", EditorWindowOperator.jumpLeft());
        // finished succesfully
        endTest();
    }

    /** Test of moveTabsRight method. */
    public void testMoveTabsRight() {
        // next tests depends on this
        startTest();
        EditorWindowOperator.moveTabsRight();
        assertTrue("Tabs were not moved to the right.", EditorWindowOperator.btLeft().isEnabled());
        // finished succesfully
        endTest();
    }

    /** Test of moveTabsLeft method. */
    public void testMoveTabsLeft() {
        // this test depends on previous
        startTest();
        // but not block anything else
        clearTestStatus();
        EditorWindowOperator.moveTabsLeft();
        assertFalse("Tabs were not moved to the left.", EditorWindowOperator.btLeft().isEnabled());
    }

    /** Test of verify method. */
    public void testVerify() {
        EditorWindowOperator.verify();
    }

    /** Test of closeDiscard method. */
    public void testCloseDiscard() {
        EditorWindowOperator.closeDiscard();
    }
}
