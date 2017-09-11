/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
