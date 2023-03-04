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
package org.netbeans.test.groovy.cc;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.groovy.GeneralGroovy;

/**
 *
 * @author Vladimir Riha
 */
public class testCCMixed extends GeneralGroovy {

    static final String TEST_BASE_NAME = "groovymi_";
    static int name_iterator = 0;

    public testCCMixed(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCMixed.class).addTest(
                "CreateApplication",
                "GroovyConstructorInJava",
                "GroovyPropertiesInJava",
                "GroovyClassesNoImportInJava",
                "GroovyClassesWithImportInJava",
                "JavaSimpleInGroovy",
                "ImportPackage",
                "JavaClassesInGroovy",
                "JavaPropertiesInGroovy").enableModules(".*").clusters(".*"));
    }

    public void CreateApplication() {
        startTest();
        createJavaApplication(TEST_BASE_NAME + name_iterator);
        testCCMixed.name_iterator++;
        endTest();
    }

    public void GroovyConstructorInJava() {
        startTest();
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "BBB");
        EditorOperator file = new EditorOperator("BBB.groovy");
        file.setCaretPosition("BBB {", false);
        type(file, "\n final String thing = \"sdsds\"");
        type(file, "\n  def String x");
        type(file, "\n  def String xx");
        type(file, "\n def BBB(){\n");
        file.setCaretPositionToLine(file.getLineNumber() + 2);
        type(file, "\n def BBB(int a){\n");
        file.setCaretPositionToLine(file.getLineNumber() + 2);
        type(file, "\n  def function1(){\n");
        file.save();
        file = new EditorOperator(TEST_BASE_NAME + (name_iterator - 1));
        file.setCaretPosition("args) {", false);
        waitScanFinished();
        evt.waitNoEvent(300);
        type(file, "\n BBB b = new BBB");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        String[] res = {"public BBB()", "public BBB(int a)"};
        checkCompletionItems(completion.listItself, res);
        assertEquals("Incorrect cc size - probably BBB$Trial, check #210670", 2, completion.listItems.size());
        completion.listItself.hideAll();

        endTest();
    }

    public void GroovyPropertiesInJava() {
        startTest();

        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "BBBB");
        EditorOperator file = new EditorOperator("BBBB.groovy");
        file.setCaretPosition("BBBB {", false);
        type(file, "\n final String thing = \"sdsds\"");
        type(file, "\n  def String x");
        type(file, "\n  def String xx");
        type(file, "\n def BBBB(){\n");
        file.setCaretPositionToLine(file.getLineNumber() + 2);
        type(file, "\n def BBBB(int a){\n");
        file.setCaretPositionToLine(file.getLineNumber() + 2);
        type(file, "\n  def function1(){\n");
        file.save();
        file = new EditorOperator(TEST_BASE_NAME + (name_iterator - 1));
        file.setCaretPosition("args) {", false);
        waitScanFinished();
        type(file, "\n BBBB b = new BBBB");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        String[] res = {"public BBBB()", "public BBBB(int a)"};
        checkCompletionItems(completion.listItself, res);
        assertEquals("Incorrect cc size - probably BBBB$Trial, check #210670", 2, completion.listItems.size());
        completion.listItself.hideAll();

        endTest();
    }

    public void GroovyClassesNoImportInJava() {
        startTest();
        
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "FooBar");
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "FooBar2", "foo.bar.test");

        EditorOperator file = new EditorOperator("FooBar2");
        file.setCaretPosition("}", false);
        type(file, "\n class FooBar3{\n");

        file = new EditorOperator(TEST_BASE_NAME + (name_iterator - 1));
        file.setCaretPosition("args) {", false);
        waitScanFinished();
        type(file, "\n F");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);
        file.pressKey(KeyEvent.VK_BACK_SPACE);
        CompletionInfo completion = getCompletion();
        String[] res = {"FooBar"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void GroovyClassesWithImportInJava() {
        startTest();
        EditorOperator file = new EditorOperator(TEST_BASE_NAME + (name_iterator - 1));
        file.setCaretPositionToEndOfLine(8);
        type(file, "import foo.bar.test.*;");
        file.setCaretPosition("args) {", false);
        type(file, "\n F");
        waitScanFinished();
        file.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        String[] res = {"FooBar", "FooBar2", "FooBar3"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void JavaSimpleInGroovy() {
        startTest();
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "EE");
        EditorOperator file = new EditorOperator("EE.groovy");
        file.setCaretPosition("EE {", false);
        type(file, "\n  def EE(){\n");
        waitScanFinished();
        type(file, "\n  def d = new java.util.Date() \n d.");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        String[] res = {"UTC()"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void ImportPackage() {
        startTest();
    
        createJavaFile(TEST_BASE_NAME + (name_iterator - 1), "Java Class", "Foo", "org2.netbeans.groovy");
        EditorOperator file = new EditorOperator("Foo.java");
        file.setCaretPosition("Foo {", false);
        type(file, "\n public void test(int a){\n");
        file.setCaretPositionToLine(file.getLineNumber() + 2);
        type(file, "\n public static int testInt(){\n return 0;");
        new EventTool().waitNoEvent(1000);
        file.save();
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "EEE", "org.netbens.groovy");
        file = new EditorOperator("EEE.groovy");
        file.setCaretPosition("package org.netbens.groovy", false);
        waitScanFinished();
        file.pressKey(KeyEvent.VK_ENTER);
        type(file, "import o");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        String[] res = {"org", "org2"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void JavaClassesInGroovy() {
        startTest();
        EditorOperator file = new EditorOperator("EEE.groovy");
        file.setCaretPosition("import o", false);
        type(file, "rg2.netbeans.groovy.Foo");

        file.setCaretPosition("EEE {", false);
        type(file, "\n  def EEE(){\n");
        waitScanFinished();
        type(file, "\n  def d = new F");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        String[] res = {"Foo"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void JavaPropertiesInGroovy() {
        startTest();

        EditorOperator file = new EditorOperator("EEE.groovy");
        file.setCaretPosition("def d = new F", false);
        waitScanFinished();
        type(file, "oo() \n d.");
        file.typeKey(' ', InputEvent.CTRL_MASK);        
        new EventTool().waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        String[] res = {"test()", "testInt()"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();

        endTest();
    }
}
