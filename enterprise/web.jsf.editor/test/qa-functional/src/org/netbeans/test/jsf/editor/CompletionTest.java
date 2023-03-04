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
package org.netbeans.test.jsf.editor;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha (vriha)
 */
public class CompletionTest extends GeneralJSF {

    public CompletionTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(CompletionTest.class);
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        return NbModuleSuite.create(
                conf.addTest(
                        "testOpenProject",
                        "testUIInclude",
                        "testComponentAttributesForm",
                        "testComponentAttributesAnchor",
                        "testUIIncludeFolder",
                        "testComposite"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void testOpenProject() throws Exception {
        startTest();
        CompletionTest.current_project = "sampleJSF22";
        openProject(CompletionTest.current_project);
        resolveServer(CompletionTest.current_project);
        openFile("completion.xhtml", CompletionTest.current_project);

        // workaround for issue 239455
        EditorOperator eo = new EditorOperator("completion.xhtml");
        eo.setCaretPosition(4, 46);
        type(eo, " ");
        new org.netbeans.jellytools.actions.Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_S, 2)).performShortcut(eo);
        evt.waitNoEvent(500);
        endTest();
    }

    public void testUIInclude() throws Exception {
        startTest();
        doTest(new EditorOperator("completion.xhtml"), 12, 27, "", 0, new String[]{"WEB-INF/", "partial/", "resources/",
            "completion.xhtml", "el30.xhtml", "index.xhtml", "navigate.xhtml", "ns.xhtml"}, "pa", "partial/");
        endTest();
    }

    public void testComponentAttributesForm() throws Exception {
        startTest();
        doTest(new EditorOperator("completion.xhtml"), 14, 9, "<form jsf: >", 2, new String[]{"accept", "binding"}, "bin", "jsf:binding=");
        endTest();
    }

    public void testComponentAttributesAnchor() throws Exception {
        startTest();
        doTest(new EditorOperator("completion.xhtml"), 15, 9, "<a jsf: >", 2, new String[]{"disableClientWindow", "binding"}, "", "");
        endTest();
    }

    public void testComposite() throws Exception {
        startTest();
        openFile("resources|ezcomp|out.xhtml", CompletionTest.current_project);
        EditorOperator eo = new EditorOperator("out.xhtml");
        eo.setCaretPosition(8, 41);

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;

        checkCompletionItemsJSF(cjo, new String[]{"test", "other"}, 20);
        completion.listItself.hideAll();

        type(eo, "te");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(300);

        type(eo, ".");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItemsJSF(cjo, new String[]{"InnerBean", "SimpleBean"}, 20);

        type(eo, "In");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(300);
        assertTrue(eo.getText(8).contains("InnerBean"));

        // cc for attribute
        eo.setCaretPosition(13, 41);
        eo.insert("#{cc.attrs.test.}");
        eo.pressKey(KeyEvent.VK_LEFT);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItemsJSF(cjo, new String[]{"innerMsg"}, 20);

        endTest();
    }

    public void testUIIncludeFolder() throws Exception {
        startTest();
        doTest(new EditorOperator("completion.xhtml"), 13, 37, "", 0, new String[]{"../", "ezcomp/"}, "", "");
        endTest();
    }

    public void doTest(EditorOperator file, int line, int column, String text, int moveLeft, String[] result, String textForAutocomplete, String textToFind) throws Exception {
        file.setCaretPosition(line, column);
        type(file, text);
        evt.waitNoEvent(1000);
        file.pressKey(KeyEvent.VK_ESCAPE);

        for (int i = 0; i < moveLeft; i++) {
            file.pressKey(KeyEvent.VK_LEFT);
        }

        file.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;

        if (result.length > 0) {
            checkCompletionItemsJSF(cjo, result, 20);
        }
        completion.listItself.hideAll();

        if (textForAutocomplete.length() > 0) {
            type(file, textForAutocomplete);
            file.typeKey(' ', InputEvent.CTRL_MASK);
            evt.waitNoEvent(300);
            assertTrue(textToFind + " not found. Unexpected line content: " + file.getText(line), file.getText(line).contains(textToFind));
        }

        // clear line
        int stop = file.getText(line).length() - 1;
        file.setCaretPositionToEndOfLine(line);
        for (int i = 0; i < stop; i++) {
            file.pressKey(KeyEvent.VK_BACK_SPACE);
        }

    }

}
