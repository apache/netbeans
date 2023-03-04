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
package org.netbeans.test.syntax;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Tests sanity support for embedded CSS and JS in JSP files
 *
 * @author Vladimir Riha
 */
public class EmbeddedSanityTest extends GeneralJSP {

    public static final String JS_COMMENT_MARK = "//cc;";
    public static final String CSS_COMMENT_MARK = "/*;";

    public EmbeddedSanityTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(EmbeddedSanityTest.class);
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        return NbModuleSuite.create(conf.addTest(
                "openProject",
                "testJsLocalVariables",
                "testJsPublicMembers",
                "testJsPrivateMembers",
                "testJsLiteralMembers",
                "testJsObjectMembers",
                "testJsObjectMembers2",
                "testJsPublicObjectMembers",
                "testMatchingProperties",
                "testCssSelectors",
                "testCssClassSelectors",
                "testCssProperties",
                "testCssValues",
                "testCssSelectors",
                "testCssVendors"
        ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        EmbeddedSanityTest.current_project = "sampleJSP";
        openProject(EmbeddedSanityTest.current_project);
        resolveServer(EmbeddedSanityTest.current_project);
        openFile("embedded.jsp", EmbeddedSanityTest.current_project);
        EditorOperator eo = new EditorOperator("embedded.jsp");
        EmbeddedSanityTest.original_content = eo.getText();
        endTest();
    }

    public void testJsLocalVariables() throws Exception {
        startTest();
        testCompletion(new EditorOperator("embedded.jsp"), 47, EmbeddedSanityTest.JS_COMMENT_MARK);
        endTest();
    }

    public void testJsPublicMembers() throws Exception {
        startTest();
        testCompletion(new EditorOperator("embedded.jsp"), 49, EmbeddedSanityTest.JS_COMMENT_MARK);
        endTest();
    }

    public void testJsPrivateMembers() throws Exception {
        startTest();
        testCompletion(new EditorOperator("embedded.jsp"), 56, EmbeddedSanityTest.JS_COMMENT_MARK);
        endTest();
    }

    public void testJsLiteralMembers() throws Exception {
        startTest();
        testCompletion(new EditorOperator("embedded.jsp"), 58, EmbeddedSanityTest.JS_COMMENT_MARK);
        endTest();
    }

    public void testJsObjectMembers() throws Exception {
        startTest();
        testCompletion(new EditorOperator("embedded.jsp"), 62, EmbeddedSanityTest.JS_COMMENT_MARK);
        endTest();
    }

    public void testJsObjectMembers2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("embedded.jsp"), 67, EmbeddedSanityTest.JS_COMMENT_MARK);
        endTest();
    }

    public void testJsPublicObjectMembers() throws Exception {
        startTest();
        testCompletion(new EditorOperator("embedded.jsp"), 69, EmbeddedSanityTest.JS_COMMENT_MARK);
        endTest();
    }

    public void testCssSelectors() throws Exception {
        startTest();
        testCompletion(new EditorOperator("embedded.jsp"), 15, EmbeddedSanityTest.CSS_COMMENT_MARK);
        endTest();
    }

    public void testCssClassSelectors() throws Exception {
        startTest();
        testCompletion(new EditorOperator("embedded.jsp"), 17, EmbeddedSanityTest.CSS_COMMENT_MARK);
        endTest();
    }

    public void testCssProperties() throws Exception {
        startTest();
        testCompletion(new EditorOperator("embedded.jsp"), 20, EmbeddedSanityTest.CSS_COMMENT_MARK);
        endTest();
    }

    public void testCssValues() throws Exception {
        startTest();
        testCompletion(new EditorOperator("embedded.jsp"), 24, EmbeddedSanityTest.CSS_COMMENT_MARK);
        endTest();
    }

    public void testMatchingProperties() throws Exception {
        startTest();
        testCompletion(new EditorOperator("embedded.jsp"), 12, EmbeddedSanityTest.CSS_COMMENT_MARK);
        evt.waitNoEvent(1000);
        endTest();
    }

    public void testCssVendors() throws Exception {
        startTest();
        testCompletion(new EditorOperator("embedded.jsp"), 28, EmbeddedSanityTest.CSS_COMMENT_MARK);
        endTest();
    }

    public void testCompletion(EditorOperator eo, int lineNumber, String commentMark) throws Exception {
        waitScanFinished();
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("//cc;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        lineNumber = Integer.parseInt(config[1]);
        eo.setCaretPositionToEndOfLine(lineNumber);
        type(eo, config[2]);
        eo.pressKey(KeyEvent.VK_ESCAPE);
        int back = Integer.parseInt(config[3]);
        for (int i = 0; i < back; i++) {
            eo.pressKey(KeyEvent.VK_LEFT);
        }

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, config[4].split(","));

        if (config[5].length() > 0) {
            checkCompletionDoesntContainItems(cjo, config[5].split(","));
        }

        completion.listItself.hideAll();

        if (config[6].length() > 0) {
            String prefix = Character.toString(config[6].charAt(0));
            type(eo, prefix);
            eo.typeKey(' ', InputEvent.CTRL_MASK);
            completion = getCompletion();
            cjo = completion.listItself;
            checkCompletionMatchesPrefix(cjo.getCompletionItems(), prefix);

            evt.waitNoEvent(500);
            eo.pressKey(KeyEvent.VK_ENTER);
            assertTrue("Wrong completion result", eo.getText(lineNumber).contains(config[6].replaceAll("|", "")));

        }

        eo.setCaretPositionToEndOfLine(eo.getLineNumber());
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

    }

    @Override
    public void tearDown() throws Exception {
        EditorOperator eo = new EditorOperator("embedded.jsp");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(KeyEvent.VK_DELETE);
        eo.insert(EmbeddedSanityTest.original_content);
        eo.save();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        evt.waitNoEvent(800);
    }

}
