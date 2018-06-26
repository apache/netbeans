/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
        completion.listItself.hideAll();

        type(eo, "add().");
        evt.waitNoEvent(100);

        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        completion.listItself.hideAll();

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
        completion.listItself.hideAll();

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
        completion.listItself.hideAll();
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
