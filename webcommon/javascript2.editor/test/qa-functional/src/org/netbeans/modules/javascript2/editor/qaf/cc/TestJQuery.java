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
package org.netbeans.modules.javascript2.editor.qaf.cc;

import java.awt.event.InputEvent;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;

/**
 *
 * @author Vladimir Riha
 */
public class TestJQuery extends GeneralJavaScript {

    static final String[] tests = new String[]{
        "openProject",
        "testChainCC",
        "testCCMethod",
        "testSimpleClassSelector",
        "testSimpleIdSelector",
        "testtestSimpleIdSelectorJQuery",
        "testSimpleClassSelectorJQuery",
        "testElementSelector",
        "testElementSelectorjQuery",
        "testMultipleSelectors",
        "testMultipleSelectorsJQuery",
        "testHelpWindow"
    };
    private String projectName = "completionTest";

    public TestJQuery(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(TestJQuery.class, tests);
    }

    public void openProject() throws Exception {

        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects(projectName);
        evt.waitNoEvent(10000);
        // open all files
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node sourceFiles = new Node(rootNode, "Source Files");
        for (String file : sourceFiles.getChildren()) {
            openFile(file);
        }
    }

    public void openFile(String fileName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        Logger.getLogger(TestJQuery.class.getName()).info("Opening file " + fileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Source Files|" + fileName);
        node.select();
        node.performPopupAction("Open");
    }

    public void testChainCC() {
        startTest();

        EditorOperator eo = new EditorOperator("test.js");
        cleanFile(eo);
        type(eo, "\n\n");
        eo.setCaretPositionToLine(1);
        type(eo, "$(\"\").");

        evt.waitNoEvent(100);

        CompletionInfo completion = getCompletion();
        String[] res = {"add", "addClass", "ajaxComplete", "ajaxError", "ajaxSend", "ajaxStart", "animate", "before"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        CompletionJListOperator.hideAll();

        type(eo, "add().");
        evt.waitNoEvent(100);

        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        CompletionJListOperator.hideAll();

        endTest();
    }

    public void testCCMethod() {
        startTest();
        EditorOperator eo = new EditorOperator("test.js");
        cleanFile(eo);
        type(eo, "\n\n");
        eo.setCaretPositionToLine(1);
        type(eo, "$(\"\").b");

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);

        CompletionInfo completion = getCompletion();
        String[] res = {"before", "bind", "blur"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        String[] res2 = {"add"};
        checkCompletionDoesntContainItems(cjo, res2);
        CompletionJListOperator.hideAll();

        endTest();
    }

    public void testSimpleClassSelector() {
        startTest();
        simpleCase("$(\".", new String[]{".root", ".bar"}, new String[]{});
        endTest();
    }

    public void testSimpleIdSelector() {
        startTest();
        simpleCase("$(\"#", new String[]{"#foo", "#foobar"}, new String[]{});
        endTest();
    }

    public void testtestSimpleIdSelectorJQuery() {
        startTest();
        simpleCase("\njQuery(\"#", new String[]{"#foo", "#foobar"}, new String[]{});
        endTest();
    }

    public void testSimpleClassSelectorJQuery() {
        startTest();
        simpleCase("\njQuery(\".", new String[]{".root", ".bar"}, new String[]{});
        endTest();
    }

    public void testJQuery() {
    }

    public void testElementSelectorjQuery() {
        startTest();
        simpleCase("\njQuery(\"", new String[]{"div", "span"}, new String[]{});
        endTest();
    }

    public void testElementSelector() {
        startTest();
        simpleCase("$().first().children(\"", new String[]{"div", "span"}, new String[]{});
        endTest();
    }

    public void simpleCase(String toType, String[] toFind, String[] notToFind) {

        EditorOperator eo = new EditorOperator("test.js");
        cleanFile(eo);
        type(eo, "\n");
        eo.setCaretPositionToLine(1);
        type(eo, toType);

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);

        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, toFind);
        if (notToFind.length > 0) {
            checkCompletionDoesntContainItems(cjo, notToFind);
        }
        CompletionJListOperator.hideAll();
    }

    public void testMultipleSelectors() {
        startTest();
        simpleCase("$(\".root ", new String[]{".root", ".bar", "#foo", "#foobar", "div"}, new String[]{});
        endTest();
    }

    public void testMultipleSelectorsJQuery() {
        startTest();
        simpleCase("\njQuery(\"#foo .", new String[]{".root", ".bar"}, new String[]{"div"});
        endTest();
    }

    public void testHelpWindow() {
        startTest();

        EditorOperator eo = new EditorOperator("test.js");
        cleanFile(eo);
        type(eo, "\n\n");
        eo.setCaretPositionToLine(1);
        type(eo, "$(\"\").b");

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);
        CompletionInfo completion = getCompletion();
        completion.listItself.clickOnItem("blur");

        WindowOperator jdDoc = new WindowOperator(0);
        JEditorPaneOperator jeEdit = new JEditorPaneOperator(jdDoc);
        String sCompleteContent = jeEdit.getText();
        String toFind = "event does not bubble in Internet Explorer";
        if (-1 == sCompleteContent.indexOf(toFind)) {
            System.out.println(">>>" + sCompleteContent + "<<<");
            fail("Unable to find part of required documentation: \"" + toFind + "\"");
        }

        endTest();
    }
}
