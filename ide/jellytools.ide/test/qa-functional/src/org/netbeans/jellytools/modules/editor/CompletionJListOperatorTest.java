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
package org.netbeans.jellytools.modules.editor;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.Operator.StringComparator;

/**
 * Tests if CompletionJListOperator works properly when the completion list
 * is invoked by typing ".", pressing Ctrl+Space and by 
 * calling  CompletionJListOperator.showCompletion().
 *
 * @author Vojtech Sigler
 */
public class CompletionJListOperatorTest extends JellyTestCase {

    public static String[] tests = new String[]{
        "testCompletionCtrlSpace", "testCompletionDot", "testCompletionInvoke"
    };
    private static EditorOperator eo;

    public CompletionJListOperatorTest(String isTestName) {
        super(isTestName);
    }

    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        Node sourcePackagesNode = new Node(new ProjectsTabOperator().getProjectRootNode("SampleProject"), "Source Packages");
        Node node = new Node(sourcePackagesNode, "sample1|SampleClass1.java"); // NOI18N
        new OpenAction().perform(node);
        eo = new EditorOperator("SampleClass1.java");   // NOI18N
        eo.setCaretPositionToEndOfLine(55);
        eo.insert("System.out");
    }

    @Override
    protected void tearDown() {
        EditorOperator.closeDiscardAll();
    }

    public static Test suite() {
        return createModuleTest(CompletionJListOperatorTest.class, tests);
    }

    public void testCompletionDot() throws Exception {
        eo.makeComponentVisible();
        eo.typeKey('.');

        CompletionJListOperator lrComplOp = new CompletionJListOperator();
        lrComplOp.getCompletionItems();

        lrComplOp.clickOnItem("println()", new StringComparator() {

            @Override
            public boolean equals(String caption, String match) {
                return caption.contains(match);
            }
        }, 2); //doubleclick

        assertTrue("The line does not contain the clicked item from CompletionList!",
                eo.contains("System.out.println()"));

    }

    public void testCompletionInvoke() throws Exception {
        eo.makeComponentVisible();
        eo.insert(".");

        CompletionJListOperator lrComplOp = CompletionJListOperator.showCompletion();
        lrComplOp.getCompletionItems();

        lrComplOp.clickOnItem("println()", new StringComparator() {

            @Override
            public boolean equals(String caption, String match) {
                return caption.contains(match);
            }
        }, 2); //doubleclick

        assertTrue("The line does not contain the clicked item from CompletionList!",
                eo.contains("System.out.println()"));
    }

    public void testCompletionCtrlSpace() throws Exception {
        eo.insert(".");

        eo.requestFocus();
        eo.txtEditorPane().pushKey(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK); //press ctrl+space

        CompletionJListOperator lrComplOp = new CompletionJListOperator();
        lrComplOp.getCompletionItems();

        lrComplOp.clickOnItem("println()", new StringComparator() {

            @Override
            public boolean equals(String caption, String match) {
                return caption.contains(match);
            }
        }, 2); //doubleclick

        assertTrue("The line does not contain the clicked item from CompletionList!",
                eo.contains("System.out.println()"));
    }
}
