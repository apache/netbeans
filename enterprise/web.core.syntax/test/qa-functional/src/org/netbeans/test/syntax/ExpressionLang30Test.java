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
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha
 */
public class ExpressionLang30Test extends GeneralJSP {

    public static String originalContent;

    public ExpressionLang30Test(String args) {
        super(args);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(ExpressionLang30Test.class);
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        return NbModuleSuite.create(
                conf.addTest(
                "testOpenProject",
//                "testNoErrors", commented out due to JSP parser from GlassFish, see issue #228029
                "testCollection",
                "testChainedCall",
                "testCollectionDetailed",
                "testCollectionConstructor").enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void testOpenProject() throws Exception {
        startTest();
        ExpressionLang30Test.current_project = "sampleJSP";
        openProject(ExpressionLang30Test.current_project);
        openFile("el30.jsp", ExpressionLang30Test.current_project);
        EditorOperator eo = new EditorOperator("el30.jsp");
        ExpressionLang30Test.originalContent = eo.getText();
        resolveServer(ExpressionLang30Test.current_project);
        endTest();
    }

    public void testNoErrors() {
        startTest();
        EditorOperator eo = new EditorOperator("el30.jsp");
        evt.waitNoEvent(1000);
        Object[] annotations = getAnnotations(eo, 0);
        assertEquals("Unexpected number of annotations", 1, annotations.length);
        endTest();
    }

    public void testCollection() {
        startTest();
        EditorOperator eo = new EditorOperator("el30.jsp");
        eo.setCaretPositionToEndOfLine(12);
        type(eo, "\n ${[1,2].");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(2000);
        String t = eo.getText(eo.getLineNumber());
        assertTrue("Incorrect autocompletion", t.indexOf("${[1,2].stream()") > -1);
        this.clearLine(eo);
        endTest();
    }

    public void testCollectionDetailed() {
        startTest();
        EditorOperator eo = new EditorOperator("el30.jsp");
        eo.setCaretPositionToEndOfLine(12);
        type(eo, "\n ${[1,2].stream().");
        eo.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"allMatch", "average", "distinct"});
        type(eo, "m");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"map", "max", "min"});
        checkCompletionDoesntContainItems(cjo, new String[]{"distinct"});
//        type(eo, "ap.().");
//        eo.typeKey(' ', InputEvent.CTRL_MASK);
//        evt.waitNoEvent(1000);
//        completion = getCompletion();
//        cjo = completion.listItself;
//        checkCompletionItems(cjo, new String[]{"map", "average", "distinct"});
        this.clearLine(eo);
        endTest();
    }

    public void testCollectionConstructor() {
        startTest();
        EditorOperator eo = new EditorOperator("el30.jsp");
        eo.setCaretPositionToEndOfLine(12);
        type(eo, "\n  ${v = {\"one\":1, \"two\":2, \"three\":3}}");
        eo.pressKey(java.awt.event.KeyEvent.VK_LEFT);
        type(eo, "; v.");
        eo.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        assertTrue("Incorrect autocompletion: "+eo.getText(), eo.getText().indexOf("v.stream()") > -1);
        this.clearLine(eo);
        endTest();
    }

    public void testChainedCall() {
        startTest();
        EditorOperator eo = new EditorOperator("el30.jsp");
        eo.setCaretPositionToEndOfLine(12);
        type(eo, "\n ${simplebean[\"setMsg\"](\"test\");simplebean.");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"ib", "msg", "print"});
        this.clearLine(eo);
        endTest();
    }

    @Override
    public void tearDown() {
        openFile("el30.jsp", ExpressionLang30Test.current_project);
        EditorOperator eo = new EditorOperator("el30.jsp");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(java.awt.event.KeyEvent.VK_DELETE);
        eo.insert(ExpressionLang30Test.originalContent);
    }
}
