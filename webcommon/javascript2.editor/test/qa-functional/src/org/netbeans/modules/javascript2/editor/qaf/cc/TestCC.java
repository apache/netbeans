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
package org.netbeans.modules.javascript2.editor.qaf.cc;

import java.awt.event.InputEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;

/**
 *
 * @author Vladimir Riha
 */
public class TestCC extends GeneralJavaScript {

    static final String[] tests = new String[]{
        "createApplication",
        "testSimplePrototype",
        "testObjectFunction",
        "testObjectLiteral",
        "testPrototypeInheritance",
        "testObjectLiteral",
        "testIssue215394",
        "testIssue215393",
        "testAllCompletionSingleFile",
        "testAllCompletionMultipleFiles",
        "testCallAndApply",
        "testLearning",
        "testSetterGetter"
    };

    public TestCC(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(TestCC.class, tests);
    }

    public void createApplication() {
        startTest();
        TestCC.NAME_ITERATOR++;
        createPhpApplication(TEST_BASE_NAME + "_" + NAME_ITERATOR);
        endTest();
    }

    public void testSimplePrototype() {
        startTest();

        TestCC.currentFile = "cc.js";
        EditorOperator eo = createWebFile("cc", TEST_BASE_NAME + "_" + NAME_ITERATOR, "JavaScript File");
        eo.setCaretPositionToLine(6);
        type(eo, "function Foo(){\n this.x=1; \n var foo = 2;");
        eo.setCaretPosition("}", false);
        type(eo, "\n Foo.prototype.add = function(i){\n this.x+=y;");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() + 1);
        type(eo, "\n obj = new Foo();\n obj.");

        try {
            waitScanFinished();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            evt.waitNoEvent(3000); // fallback
        }

        evt.waitNoEvent(1000);


        CompletionInfo completion = getCompletion();
        String[] res = {"add", "x"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        String[] res2 = {"foo"};
        checkCompletionDoesntContainItems(cjo, res2);
        completion.listItself.hideAll();

        cleanFile(eo);
        endTest();
    }

    public void testPrototypeInheritance() {
        startTest();

        EditorOperator eo = new EditorOperator(TestCC.currentFile);
        cleanFile(eo);

        eo.setCaretPositionToLine(1);
        type(eo, "var A = function(){\n this.value=1; ");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() + 1);
        type(eo, "\n A.prototype.constructor = A; \n A.prototype.test = function () {\n ");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() + 1);
        type(eo, "\n var B = function () {\n A.call(this);");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() + 1);
        type(eo, "\n B.prototype = new A; \n B.prototype.constructor = B; \n");
        type(eo, "B.prototype.test = function () {\n  A.prototype.test.call(this);");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() + 1);
        type(eo, "\n var b = new B(); \n");
        type(eo, "b.\n");// workaround for #215394
        eo.setCaretPosition("b.", false);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);
        CompletionInfo completion = getCompletion();
        String[] res = {"test", "value"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void testCallAndApply() {
        startTest();

        EditorOperator eo = new EditorOperator(TestCC.currentFile);
        cleanFile(eo);

        eo.setCaretPositionToLine(1);
        type(eo, "function f(){\n alert(this.msg); \n");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() + 1);
        type(eo, "\n \n");
        eo.setCaretPositionToLine(eo.getLineNumber() - 1);
        type(eo, "f.");
        evt.waitNoEvent(100);

        CompletionInfo completion = getCompletion();
        String[] res = {"call", "apply"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void testLearning() {
        startTest();

        EditorOperator eo = new EditorOperator(TestCC.currentFile);
        cleanFile(eo);

        eo.setCaretPositionToLine(1);
        type(eo, "var person = {};\n person.learn = function(){}; \n");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber());
        type(eo, "\n \n");
        eo.setCaretPositionToLine(eo.getLineNumber() - 1);
        type(eo, "person.");
        evt.waitNoEvent(100);

        CompletionInfo completion = getCompletion();
        String[] res = {"learn"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void testSetterGetter(){
        startTest();

        EditorOperator eo = new EditorOperator(TestCC.currentFile);
        cleanFile(eo);

        eo.setCaretPositionToLine(1);
        type(eo, "var person = { get name(){return this.myname;}, set name(n){this.myname=n;}}; ");
        type(eo, "person.");
        evt.waitNoEvent(100);

        CompletionInfo completion = getCompletion();
        String[] res = {"name", "myname"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void testDOMReferences() {
        startTest();

        EditorOperator eo = new EditorOperator(TestCC.currentFile);
        cleanFile(eo);

        eo.setCaretPositionToLine(1);
        type(eo, " document.");
        type(eo, "\n"); // workaround for #215394
        eo.setCaretPosition("document.", false);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);

        CompletionInfo completion = getCompletion();
        String[] res = {"firstChild", "removeChild"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void testIssue215394() {
        try {
            startTest();

            EditorOperator eo = new EditorOperator(TestCC.currentFile);
            cleanFile(eo);

            eo.setCaretPositionToLine(1);
            type(eo, " document.");
            evt.waitNoEvent(100);

            CompletionInfo completion = getCompletion();
            CompletionJListOperator cjo = completion.listItself;
            assertTrue("", (cjo.getCompletionItems().size() > 2 ? true : false));
            completion.listItself.hideAll();

            endTest();
        } catch (Exception ex) {
            fail("Fail 215394 " + ex.getMessage());
        }
    }

    public void testObjectLiteral() {
        startTest();

        EditorOperator eo = new EditorOperator(TestCC.currentFile);
        cleanFile(eo);
        type(eo, "var foo = {\n value:0,\nincrement: function(inc){\nthis.value += typeof inc === 'number' ? inc : 1;");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() + 2);
        type(eo, ";\n\n");
        eo.setCaretPositionToLine(eo.getLineNumber() - 1);
        type(eo, "foo.");
        evt.waitNoEvent(100);

        CompletionInfo completion = getCompletion();
        String[] res = {"value", "increment"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        completion.listItself.hideAll();


        endTest();
    }

    public void testObjectFunction() {
        startTest();

        EditorOperator eo = new EditorOperator(TestCC.currentFile);
        cleanFile(eo);
        type(eo, "function Foo(param1){\n this.name = ");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);

        // cc for parameters
        CompletionInfo completion = getCompletion();
        String[] res = {"param1"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        completion.listItself.hideAll();

        type(eo, "param1;\n var pr = 1;\n this.start = function(){\n");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() + 1);
        type(eo, ";\n function secret(){\n");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() + 1);
        type(eo, "\n ");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);

        // cc inside function
        completion = getCompletion();
        String[] res5 = {"name", "start", "pr", "param1", "secret"};
        cjo = completion.listItself;
        checkCompletionItems(cjo, res5);
        completion.listItself.hideAll();

        type(eo, "\n Foo.prototype.setName = function(n){\n this.");

        // cc inside function's prototype
        evt.waitNoEvent(100);
        completion = getCompletion();
        String[] res4 = {"name", "start", "setName"};
        cjo = completion.listItself;
        checkCompletionItems(cjo, res4);
        completion.listItself.hideAll();
        type(eo, "name;");

        eo.setCaretPositionToEndOfLine(eo.getLineNumber() + 2);
        type(eo, "\n\n");
        eo.setCaretPositionToLine(eo.getLineNumber() - 1);
        type(eo, "var o = new ");

        // constructor function
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);
        completion = getCompletion();
        String[] res6 = {"Foo"};
        cjo = completion.listItself;
        checkCompletionItems(cjo, res6);
        completion.listItself.hideAll();

        type(eo, "Foo();\n o.");
        // public variable & method & prototype
        evt.waitNoEvent(100);
        completion = getCompletion();
        String[] res2 = {"name", "start", "setName"};
        cjo = completion.listItself;
        checkCompletionItems(cjo, res2);
        completion.listItself.hideAll();

        // private variable & method
        String[] res3 = {"secret", "pr"};
        checkCompletionDoesntContainItems(cjo, res3);
        completion.listItself.hideAll();

        endTest();
    }

    public void testIssue215393() {
        startTest();

        EditorOperator eo = new EditorOperator(TestCC.currentFile);
        cleanFile(eo);

        eo.setCaretPositionToLine(1);
        type(eo, "var panel = document.getElementById('panel'+course); \n");
        type(eo, " panel.\n");
        eo.setCaretPosition("panel.", false);// workaround for #215394
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);

        CompletionInfo completion = getCompletion();
        String[] res = {"insertBefore"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void testAllCompletionSingleFile() {
        startTest();

        EditorOperator eo = new EditorOperator(TestCC.currentFile);
        cleanFile(eo);

        eo.setCaretPositionToLine(1);
        type(eo, "var aa = 1; \nvar bb = 2;\n function A(){\n");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() + 1);
        type(eo, "\n\n");
        eo.setCaretPositionToLine(eo.getLineNumber() - 1);
        type(eo, "var c = new A(); \n c.");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);

        CompletionInfo completion = getCompletion();
        String[] res2 = {"aa", "bb"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res2);
        completion.listItself.hideAll();
        type(eo, "aa");
        eo.save();

        endTest();
    }

    public void testAllCompletionMultipleFiles() {
        startTest();
        TestCC.currentFile = "other.js";
        EditorOperator eo = createWebFile("other", TEST_BASE_NAME + "_" + NAME_ITERATOR, "JavaScript File");
        cleanFile(eo);
        eo.setCaretPositionToLine(1);
        type(eo, "var cc = 1; \nvar dd = 2;\n function AA(){\n");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() + 1);
        type(eo, "\n\n");
        eo.setCaretPositionToLine(eo.getLineNumber() - 1);
        type(eo, "var ccc = new AA(); \n ccc.");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);

        CompletionInfo completion = getCompletion();
        String[] res2 = {"aa", "bb", "cc", "dd"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res2);
        completion.listItself.hideAll();

        endTest();
    }
}
