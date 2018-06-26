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
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;

/**
 *
 * @author Vladimir Riha
 */
public class EmbeddedHTMLTest extends GeneralJavaScript {

    static final String[] tests = new String[]{
        "createApplication",
        "testSimplePrototype",
        "testObjectFunction",
        "testObjectLiteral",
        "testPrototypeInheritance",
        "testAllCompletionMultipleFiles",
        "testLearning",
        "testDOMReferences",
        "testSetterGetter"
    };
    private static String originalContent;

    public EmbeddedHTMLTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(EmbeddedHTMLTest.class, tests);
    }

    public void createApplication() {
        startTest();
        EmbeddedHTMLTest.NAME_ITERATOR++;
        createPhpApplication(TEST_BASE_NAME + NAME_ITERATOR);

        EditorOperator eo = createWebFile("cc", TEST_BASE_NAME + NAME_ITERATOR, "HTML File");
        EmbeddedHTMLTest.currentFile = "cc.html";
        eo.setCaretPosition("</body>", true);
        type(eo, "\n <script>\n \n </script>");
        EmbeddedHTMLTest.originalContent = eo.getText();
        endTest();
    }

    public void testSimplePrototype() {
        startTest();

        EditorOperator eo = new EditorOperator(EmbeddedHTMLTest.currentFile);

        cleanFile(eo);
        type(eo, "function Foo(){ this.x=1; var foo = 2; }");
        type(eo, "\n Foo.prototype.add = function(i){ this.x+=y;};\n");
        type(eo, "obj = new Foo();\n obj.");

        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        String[] res = {"add", "x"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        String[] res2 = {"foo"};
        checkCompletionDoesntContainItems(cjo, res2);
        completion.listItself.hideAll();

        endTest();
    }

    public void testPrototypeInheritance() {
        startTest();

        EditorOperator eo = new EditorOperator(EmbeddedHTMLTest.currentFile);
        cleanFile(eo);

        type(eo, "var A = function(){ this.value=1; }");
        type(eo, "\n A.prototype.constructor = A; \n A.prototype.test = function () {}; ");
        type(eo, "\n var B = function () {A.call(this);}");
        type(eo, "\n B.prototype = new A; \n B.prototype.constructor = B; \n");
        type(eo, "B.prototype.test = function () {A.prototype.test.call(this);}\n");
        type(eo, "\n var b = new B(); \n");
        type(eo, "b.\n");// workaround for #215394
        eo.setCaretPosition("b.", false);
        evt.waitNoEvent(400);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(200);
        CompletionInfo completion = getCompletion();
        String[] res = {"test", "value"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void testLearning() {
        startTest();
        EditorOperator eo = new EditorOperator(EmbeddedHTMLTest.currentFile);
        cleanFile(eo);

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

    public void testSetterGetter() {
        startTest();

        EditorOperator eo = new EditorOperator(EmbeddedHTMLTest.currentFile);
        cleanFile(eo);

        type(eo, "var person = { get name(){return this.myname;}, set name(n){this.myname=n;}}");
        type(eo, ";\n person.");
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

        EditorOperator eo = new EditorOperator(EmbeddedHTMLTest.currentFile);
        cleanFile(eo);

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

    public void testObjectLiteral() {
        startTest();

        EditorOperator eo = new EditorOperator(EmbeddedHTMLTest.currentFile);
        cleanFile(eo);
        type(eo, "var foo = { value:0,increment: function(inc){this.value += typeof inc === 'number' ? inc : 1;}}; ");
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

        EditorOperator eo = new EditorOperator(EmbeddedHTMLTest.currentFile);
        cleanFile(eo);
        type(eo, "function Foo(param1){ }");
        eo.setCaretPosition(" }", true);
        type(eo, "this.name = ");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);

        // cc for parameters
        CompletionInfo completion = getCompletion();
        String[] res = {"param1"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res);
        completion.listItself.hideAll();

        type(eo, "param1; var pr = 1; this.start = function(){}; ");
        type(eo, " function secret(){};\n  ");
        eo.setCaretPosition("}", true);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);

        // cc inside function
        completion = getCompletion();
        String[] res5 = {"name", "start", "pr", "param1", "secret"};
        cjo = completion.listItself;
        checkCompletionItems(cjo, res5);
        completion.listItself.hideAll();

        type(eo, "Foo.prototype.setName = function(n){ this.;}");

        // cc inside function's prototype
        eo.setCaretPosition(";}", true);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);
        completion = getCompletion();
        String[] res4 = {"name", "start"};
        cjo = completion.listItself;
        checkCompletionItems(cjo, res4);
        completion.listItself.hideAll();
        type(eo, "name");

        eo.setCaretPosition("</script>", true);
        type(eo, " \n");
        type(eo, "var o = new ");

        // constructor function
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);
        completion = getCompletion();
        String[] res6 = {"Foo"};
        cjo = completion.listItself;
        checkCompletionItems(cjo, res6);
        completion.listItself.hideAll();

        type(eo, "Foo(); o. ");
        eo.setCaretPosition("Foo(); o.", false);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
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

    public void testAllCompletionMultipleFiles() {
        startTest();
        EditorOperator eo = createWebFile("other", TEST_BASE_NAME + NAME_ITERATOR, "JavaScript File");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(java.awt.event.KeyEvent.VK_DELETE);
        eo.setCaretPositionToLine(1);
        type(eo, "var cc = 1; \nvar dd = 2;\n function AA(){\n");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() + 1);
        type(eo, "\n\n");
        eo.setCaretPositionToLine(eo.getLineNumber() - 1);
        eo.save();
        evt.waitNoEvent(100);


        eo = new EditorOperator(EmbeddedHTMLTest.currentFile);
        cleanFile(eo);

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);

        CompletionInfo completion = getCompletion();
        String[] res2 = {"cc", "dd"};
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, res2);
        completion.listItself.hideAll();

        endTest();
    }

    @Override
    protected void cleanFile(EditorOperator eo) {
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(java.awt.event.KeyEvent.VK_DELETE);
        eo.insert(EmbeddedHTMLTest.originalContent);
        eo.setCaretPosition("<script>", false);
        type(eo, "\n ");
    }
}
