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

package org.netbeans.modules.html.angular.cc;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.html.angular.GeneralAngular;

/**
 *
 * @author vriha
 */
public class CtrlModuleTest extends GeneralAngular{
    
    static final String[] tests = new String[]{
        "openProject",
        "testNgClick",
        "testExpression2",
        "testDirective6",
        "testDirective7",
        "testDirective8",
        "testDirective9",
        "testExpression12",
        "testExpression14",
        "testExpression16",
        "testExpression18",
        "testDirective20",
        "testNgBind",
        "testNgModel",
        "testGoTo28",
        "testGoTo29",
        "testGoTo30",
        "testGoTo31",
        //                        "testGoTo32",
        "testGoTo33",
        //                        "testGoTo34",
        //                        "testGoTo35",
        //                        "testGoTo36",
        "testGoTo37",
        "testGoToController"
    };

    public CtrlModuleTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(CtrlModuleTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("ctrlmodule");
        evt.waitNoEvent(2000);
        openFile("partials|partial2.html", "ctrlmodule");
        waitScanFinished();
        CtrlModuleTest.originalContent = new EditorOperator("partial2.html").getText();
        endTest();
    }

    public void testExpression2() throws Exception {
        startTest();
        testCompletionWithNegativeCheck(new EditorOperator("partial2.html"), 2);
        endTest();
    }

    public void testDirective6() throws Exception {
        startTest();
        testCompletionWithNegativeCheck(new EditorOperator("partial2.html"), 6);
        endTest();
    }

    public void testDirective7() throws Exception {
        startTest();
        testCompletionWithNegativeCheck(new EditorOperator("partial2.html"), 7);
        endTest();
    }

    public void testDirective8() throws Exception {
        startTest();
        testCompletionWithNegativeCheck(new EditorOperator("partial2.html"), 8);
        endTest();
    }

    public void testDirective9() throws Exception {
        startTest();
        testCompletionWithNegativeCheck(new EditorOperator("partial2.html"), 9);
        endTest();
    }

    public void testExpression12() throws Exception {
        startTest();
        testCompletionWithNegativeCheck(new EditorOperator("partial2.html"), 12);
        endTest();
    }

    public void testExpression14() throws Exception {
        startTest();
        testCompletionWithNegativeCheck(new EditorOperator("partial2.html"), 14);
        endTest();
    }

    public void testExpression16() throws Exception {
        startTest();
        testCompletionWithNegativeCheck(new EditorOperator("partial2.html"), 16);
        endTest();
    }

    public void testExpression18() throws Exception {
        startTest();
        testCompletionWithNegativeCheck(new EditorOperator("partial2.html"), 18);
        endTest();
    }

    public void testDirective20() throws Exception {
        startTest();
        testCompletionWithNegativeCheck(new EditorOperator("partial2.html"), 20);
        endTest();
    }

    public void testNgClick() throws Exception {
        startTest();
        testCompletionWithNegativeCheck(new EditorOperator("partial2.html"), 1);
        endTest();
    }

    public void testNgBind() throws Exception {
        startTest();
        testCompletionWithNegativeCheck(new EditorOperator("partial2.html"), 24);
        endTest();
    }

    public void testNgModel() throws Exception {
        startTest();
        testCompletionWithNegativeCheck(new EditorOperator("partial2.html"), 23);
        endTest();
    }


    public void testGoTo28() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("partial2.html"), 28);
        endTest();
    }

    public void testGoTo29() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("partial2.html"), 29);
        endTest();
    }

    public void testGoTo30() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("partial2.html"), 30);
        endTest();
    }

    public void testGoTo31() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("partial2.html"), 31);
        endTest();
    }

    public void testGoTo32() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("partial2.html"), 32);
        endTest();
    }

    public void testGoTo33() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("partial2.html"), 33);
        endTest();
    }

    public void testGoTo34() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("partial2.html"), 34);
        endTest();
    }

    public void testGoTo35() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("partial2.html"), 35);
        endTest();
    }

    public void testGoTo36() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("partial2.html"), 36);
        endTest();
    }

    public void testGoTo37() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("partial2.html"), 37);
        endTest();
    }

 
    public void testGoToController() {
        openFile("js|app.js", "ctrlmodule");
        EditorOperator app = new EditorOperator("app.js");
        app.setCaretPosition("rl2", true);
        new org.netbeans.jellytools.actions.Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_B, 2)).performShortcut(app);
        evt.waitNoEvent(500);
        EditorOperator ed = new EditorOperator("controllers.js");
        int position = ed.txtEditorPane().getCaretPosition();
        ed.setCaretPosition(13, 22);
        int expectedPosition = ed.txtEditorPane().getCaretPosition();
        assertTrue("Incorrect caret position. Expected position " + expectedPosition + " but was " + position, position == expectedPosition);
    }

    @Override
    public void tearDown() throws Exception {
        EditorOperator eo = new EditorOperator("partial2.html");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(KeyEvent.VK_DELETE);
        eo.insert(CtrlModuleTest.originalContent);
//        eo.save();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        evt.waitNoEvent(1500);
    }
}
