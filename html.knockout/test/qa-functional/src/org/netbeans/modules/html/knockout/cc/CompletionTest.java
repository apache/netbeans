/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.knockout.cc;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.html.knockout.GeneralKnockout;

/**
 *
 * @author Vladimir Riha (vriha)
 */
public class CompletionTest extends GeneralKnockout {

  public static final int WAIT_AFTER_PASTE = 400;
    
  public CompletionTest(String args) {
    super(args);
  }

  public static Test suite() {
    return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(CompletionTest.class).addTest(
                    "createApplication",
                    "testBindingAttr",
                    "testBindingModel",
                    "testUsedVariable",
                    "testMultipleBindings",
                    "testMultipleModel",
                    "testCustomBindingModel",
                    "testBindingModelWith",
                    "testBindingForEach",
                    "testBindingForEachParent",
                    "testBindingForEachRoot",
                    "testBindingForEach2"
            ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
  }

  public void createApplication() {
    startTest();
    GeneralKnockout.NAME_ITERATOR = (int)System.currentTimeMillis();
    createSampleProject("Knockout", TEST_BASE_NAME + "_" + NAME_ITERATOR);
    evt.waitNoEvent(3000);
    openFile("models|gamelistviewmodels.js", TEST_BASE_NAME + "_" + NAME_ITERATOR);
    EditorOperator file = new EditorOperator("gamelistviewmodels.js");
    file.setCaretPositionToEndOfLine(18);
    file.pressKey(KeyEvent.VK_ENTER);
    file.insert("function SimpleMode(){this.name=\"Test\";var lastName=\"Simple\";var self=this;this.log=function(){"
            + "return\"log\"};this.printName=ko.computed(function(){return\"<b>\"+self.name+\" \"+lastName+\"</b>\"});"
            + "self.skills={speak:1,listen:2,point:function(){}};self.today=new Date();self.printLastname=ko.computed(function(){return lastName})}"
            + "ko.applyBindings(new SimpleMode());");
    file.save();
    openFile("0-iteratingwithdivs.html", TEST_BASE_NAME + "_" + NAME_ITERATOR);
    endTest();
  }

  public void testBindingAttr() {
    startTest();
    EditorOperator eo = new EditorOperator("0-iteratingwithdivs.html");
    eo.setCaretPositionToEndOfLine(6);
    eo.pressKey(KeyEvent.VK_ENTER);
    eo.insert("<div data-bind=\"\"></div>");
    evt.waitNoEvent(CompletionTest.WAIT_AFTER_PASTE);
    eo.setCaretPosition("\"\"", 0, true);
    eo.pressKey(KeyEvent.VK_RIGHT);

    eo.typeKey(' ', InputEvent.CTRL_MASK);
    checkCompletionItems(getBindingTypes(), new String[]{"text", "visible", "component", "textInput"});
    eo.pressKey(KeyEvent.VK_ESCAPE);

    type(eo, "v");
    eo.typeKey(' ', InputEvent.CTRL_MASK);
    assertFalse("Completion contains non-matching item", getBindingTypes().contains("text"));
    checkCompletionItems(getBindingTypes(), new String[]{"visible"});
    eo.pressKey(KeyEvent.VK_ESCAPE);
    cleanLine(eo);
    endTest();
  }

  public void testMultipleBindings() {
    startTest();
    EditorOperator eo = new EditorOperator("0-iteratingwithdivs.html");
    eo.setCaretPositionToEndOfLine(6);
    eo.pressKey(KeyEvent.VK_ENTER);
    eo.insert("<div data-bind=\"text: newVariable, \"></div>");
    evt.waitNoEvent(CompletionTest.WAIT_AFTER_PASTE);
    eo.setCaretPosition("le,", 0, true);
    eo.pressKey(KeyEvent.VK_RIGHT);
    eo.pressKey(KeyEvent.VK_RIGHT);
    eo.pressKey(KeyEvent.VK_RIGHT);

    eo.typeKey(' ', InputEvent.CTRL_MASK);
    checkCompletionItems(getBindingTypes(), new String[]{"text", "visible"});
    eo.pressKey(KeyEvent.VK_ESCAPE);

    type(eo, "v");
    eo.typeKey(' ', InputEvent.CTRL_MASK);
    assertFalse("Completion contains non-matching item", getBindingTypes().contains("text"));
    checkCompletionItems(getBindingTypes(), new String[]{"visible"});
    eo.pressKey(KeyEvent.VK_ESCAPE);
    cleanLine(eo);
    endTest();
  }

  public void testMultipleModel() {
    startTest();
    EditorOperator eo = new EditorOperator("0-iteratingwithdivs.html");
    eo.setCaretPositionToEndOfLine(6);
    eo.pressKey(KeyEvent.VK_ENTER);
    eo.insert("<div data-bind=\"text: newVariable, visible: \"></div>");
    evt.waitNoEvent(CompletionTest.WAIT_AFTER_PASTE);
    eo.setCaretPosition("ible:", 0, true);
    eo.pressKey(KeyEvent.VK_RIGHT);
    eo.pressKey(KeyEvent.VK_RIGHT);
    eo.pressKey(KeyEvent.VK_RIGHT);
    eo.pressKey(KeyEvent.VK_RIGHT);
    eo.pressKey(KeyEvent.VK_RIGHT);
    type(eo, " ");
    eo.typeKey(' ', InputEvent.CTRL_MASK);

    CompletionInfo completion = getCompletion();
    CompletionJListOperator cjo = completion.listItself;
    checkCompletionItems(cjo, new String[]{"gamesToPlay", "gamesCount", "name", "log", "printLastname", "printName", "window", "Math"});
    completion.listItself.hideAll();

    type(eo, "p");
    eo.typeKey(' ', InputEvent.CTRL_MASK);
    completion = getCompletion();
    cjo = completion.listItself;
    checkCompletionItems(cjo, new String[]{"printLastname", "printName"});
    checkCompletionDoesntContainItems(cjo, new String[]{"log", "name"});
    completion.listItself.hideAll();
    cleanLine(eo);
    endTest();
  }

  public void testBindingModel() {
    startTest();
    EditorOperator eo = new EditorOperator("0-iteratingwithdivs.html");
    eo.setCaretPositionToEndOfLine(6);
    eo.pressKey(KeyEvent.VK_ENTER);
    eo.insert("<div data-bind=\"text: \"></div>");
    evt.waitNoEvent(CompletionTest.WAIT_AFTER_PASTE);
    eo.setCaretPosition("t: ", 0, true);

    eo.pressKey(KeyEvent.VK_RIGHT);
    eo.pressKey(KeyEvent.VK_RIGHT);

    eo.typeKey(' ', InputEvent.CTRL_MASK);

    CompletionInfo completion = getCompletion();
    CompletionJListOperator cjo = completion.listItself;
    checkCompletionItems(cjo, new String[]{"gamesToPlay", "gamesCount", "name", "log", "printLastname", "printName", "window", "Math"});
    completion.listItself.hideAll();

    type(eo, "p");
    eo.typeKey(' ', InputEvent.CTRL_MASK);
    completion = getCompletion();
    cjo = completion.listItself;
    checkCompletionItems(cjo, new String[]{"printLastname", "printName"});
    checkCompletionDoesntContainItems(cjo, new String[]{"log", "name"});
    completion.listItself.hideAll();

    cleanLine(eo);
    endTest();
  }

  public void testCustomBindingModel() {
    startTest();
    EditorOperator eo = new EditorOperator("0-iteratingwithdivs.html");
    eo.setCaretPositionToEndOfLine(6);
    eo.pressKey(KeyEvent.VK_ENTER);
    eo.insert("<div data-bind=\"mybinding: \"></div>");
    evt.waitNoEvent(CompletionTest.WAIT_AFTER_PASTE);
    eo.setCaretPosition("g: ", 0, true);

    eo.pressKey(KeyEvent.VK_RIGHT);
    eo.pressKey(KeyEvent.VK_RIGHT);

    eo.typeKey(' ', InputEvent.CTRL_MASK);

    CompletionInfo completion = getCompletion();
    CompletionJListOperator cjo = completion.listItself;
    checkCompletionItems(cjo, new String[]{"gamesToPlay", "gamesCount", "name", "log", "printLastname", "printName", "window", "Math"});
    completion.listItself.hideAll();

    type(eo, "p");
    eo.typeKey(' ', InputEvent.CTRL_MASK);
    completion = getCompletion();
    cjo = completion.listItself;
    checkCompletionItems(cjo, new String[]{"printLastname", "printName"});
    checkCompletionDoesntContainItems(cjo, new String[]{"log", "name"});
    completion.listItself.hideAll();

    cleanLine(eo);
    endTest();
  }

  public void testBindingModelWith() {
    startTest();
    EditorOperator eo = new EditorOperator("0-iteratingwithdivs.html");
    eo.setCaretPositionToEndOfLine(6);
    eo.pressKey(KeyEvent.VK_ENTER);
    eo.insert("<div data-bind=\"with: skills \"><span data-bind=\"text: \"></span></div></div>");
    evt.waitNoEvent(CompletionTest.WAIT_AFTER_PASTE);
    eo.setCaretPosition("t: ", 0, true);

    eo.pressKey(KeyEvent.VK_RIGHT);
    eo.pressKey(KeyEvent.VK_RIGHT);

    eo.typeKey(' ', InputEvent.CTRL_MASK);

    CompletionInfo completion = getCompletion();
    CompletionJListOperator cjo = completion.listItself;
    checkCompletionItems(cjo, new String[]{"speak", "listen", "point"});
    completion.listItself.hideAll();

    cleanLine(eo);
    endTest();
  }

  public void testBindingForEach() throws Exception {
    startTest();
    JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
    openDataProjects("sample");
    openFile("index.html", "sample");
    waitScanFinished();

    EditorOperator eo = new EditorOperator("index.html");
    eo.setCaretPosition("t.", false);
    eo.pressMouse();
    evt.waitNoEvent(3000);
    eo.typeKey(' ', InputEvent.CTRL_MASK);

    CompletionInfo completion = getCompletion();
    CompletionJListOperator cjo = completion.listItself;
    checkCompletionItems(cjo, new String[]{"addPerson", "removePerson"});
    completion.listItself.hideAll();
    type(eo, "addPerson");
    endTest();
  }

  public void testBindingForEachParent() throws Exception {
    startTest();
    EditorOperator eo = new EditorOperator("index.html");
    eo.setCaretPositionToEndOfLine(6);
    eo.pressKey(KeyEvent.VK_ENTER);
    eo.insert("<div data-bind=\"text: $parents[0].\"></div>");
    evt.waitNoEvent(CompletionTest.WAIT_AFTER_PASTE);
    eo.setCaretPosition("[0].", false);
    eo.typeKey(' ', InputEvent.CTRL_MASK);

    CompletionInfo completion = getCompletion();
    CompletionJListOperator cjo = completion.listItself;
    checkCompletionItems(cjo, new String[]{"addPerson", "removePerson"});
    completion.listItself.hideAll();
    type(eo, "addPerson");
    endTest();
  }

  public void testBindingForEachRoot() throws Exception {
    startTest();
    EditorOperator eo = new EditorOperator("index.html");
    eo.setCaretPositionToEndOfLine(6);
    eo.pressKey(KeyEvent.VK_ENTER);
    eo.insert("<div data-bind=\"text: $root.\"></div>");
    evt.waitNoEvent(CompletionTest.WAIT_AFTER_PASTE);
    eo.setCaretPosition("ot.", false);
    eo.typeKey(' ', InputEvent.CTRL_MASK);

    CompletionInfo completion = getCompletion();
    CompletionJListOperator cjo = completion.listItself;
    checkCompletionItems(cjo, new String[]{"addPerson", "removePerson"});
    completion.listItself.hideAll();
    type(eo, "addPerson");
    endTest();
  }

  public void testBindingForEach2() throws Exception {
    startTest();
    EditorOperator eo = new EditorOperator("index.html");
    eo.setCaretPositionToEndOfLine(6);
    eo.pressKey(KeyEvent.VK_ENTER);
    eo.insert("<div data-bind=\"value: \"></div>");
    evt.waitNoEvent(CompletionTest.WAIT_AFTER_PASTE);
    eo.setCaretPosition("ue:", false);
    eo.typeKey(' ', InputEvent.CTRL_MASK);

    CompletionInfo completion = getCompletion();
    CompletionJListOperator cjo = completion.listItself;
    checkCompletionItems(cjo, new String[]{"$index", "$data", "$parents"});
    completion.listItself.hideAll();
    endTest();
  }

  public void testUsedVariable() {
    startTest();
    EditorOperator eo = new EditorOperator("0-iteratingwithdivs.html");
    eo.setCaretPositionToEndOfLine(6);
    eo.pressKey(KeyEvent.VK_ENTER);
    eo.insert("<div data-bind=\"text: newVariable\"></div>");
    eo.insert("<div data-bind=\"text: \"></div>");
    evt.waitNoEvent(CompletionTest.WAIT_AFTER_PASTE);
    eo.setCaretPosition("t: ", 1, true);
    eo.pressKey(KeyEvent.VK_RIGHT);
    eo.pressKey(KeyEvent.VK_RIGHT);

    eo.typeKey(' ', InputEvent.CTRL_MASK);

    CompletionInfo completion = getCompletion();
    CompletionJListOperator cjo = completion.listItself;
    checkCompletionItems(cjo, new String[]{"newVariable"});
    completion.listItself.hideAll();

    cleanLine(eo);
    endTest();
  }

}
