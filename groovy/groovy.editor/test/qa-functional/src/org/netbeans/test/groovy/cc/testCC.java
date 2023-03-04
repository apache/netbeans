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
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.groovy.GeneralGroovy;

/**
 *
 * @author Vladimir Riha
 */
public class testCC extends GeneralGroovy {

    static final String TEST_BASE_NAME = "groovy_";
    static int name_iterator = 0;

    public testCC(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCC.class).addTest(
                "CreateApplication",
                "PrefixMatchesKeyword",
                "DifferentCCSamePosition",
                "PrefixedPackages",
                "VariableBasedOnType",
                "ConstructorDifferentFile",
                "MissingConstructor",
                "UndefinedField").enableModules(".*").clusters(".*")
                );
    }

    public void CreateApplication() {
        startTest();
        createJavaApplication(TEST_BASE_NAME + name_iterator);
        testCC.name_iterator++;
        endTest();
    }

    /**
     * issue #148861
     */
    public void MissingConstructor() {
        startTest();
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "Foo");
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "Bar");
        EditorOperator file = new EditorOperator("Bar.groovy");
        file.setCaretPosition("Bar {", false);
        type(file, "\n ");
        type(file, " Foo f = new F");
        waitScanFinished();
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        completion.listItself.clickOnItem("Foo");
        String[] res = {"Foo"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();
        endTest();
    }

    /**
     * issue #209500
     */
    public void UndefinedField() {
        startTest();
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "UndefinedField");
        EditorOperator file = new EditorOperator("UndefinedField.groovy");
        file.setCaretPosition("UndefinedField {", false);
        type(file, "\n ");
        type(file, " private Integ");
        waitScanFinished();
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        completion.listItself.clickOnItem("Integer");
        String[] res = {"Integer"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();
        endTest();
    }

    /**
     * issue #209455
     */
    public void ConstructorDifferentFile() {
        startTest();
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "AAA");
        EditorOperator file = new EditorOperator("AAA.groovy");
        file.setCaretPosition("AAA {", false);
        type(file, "\n ");
        type(file, "  AAA() { \n } \n AAA(int i) {\n");
        file.save();

        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "BBB");
        file = new EditorOperator("BBB.groovy");
        file.setCaretPosition("BBB {", false);
        type(file, "\n ");
        type(file, "AAA aaa = new AAA");
        waitScanFinished();
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        
        CompletionInfo completion = getCompletion();
        assertEquals(2, completion.listItems.size());
        String[] res = {"AAA", "AAA"}; // TODO: should be AAA() and AAA(int)
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();
        endTest();
    }

    /**
     * issue #137262
     */
    public void VariableBasedOnType() {
        startTest();
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "CCC");
        EditorOperator file = new EditorOperator("CCC.groovy");
        file.setCaretPosition("CCC {", false);
        type(file, "\n ");
        type(file, " String ");
        waitScanFinished();
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        String[] res = {"s", "string"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();
        endTest();
    }

    /**
     * issue #209453
     */
    public void PrefixedPackages() {
        startTest();
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "DDD");
        EditorOperator file = new EditorOperator("DDD.groovy");
        file.setCaretPosition("DDD {", false);
        type(file, "\n ");
        type(file, " in");
        waitScanFinished();
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();

        List t = completion.listItems;
        String item;
        for (int i = 0; i < t.size(); i++) {
            item = t.get(i).toString().toLowerCase();
            if(item.contains("codetemplatecompletionitem")){
                continue;
            }
            assertTrue("Package that does not start with in is offered "+item, item.startsWith("in"));
        }

        completion.listItself.hideAll();
        endTest();
    }

    /**
     * issue #148936
     */
    public void DifferentCCSamePosition() {
        startTest();
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "EEE");
        EditorOperator file = new EditorOperator("EEE.groovy");
        file.setCaretPosition("EEE {", false);
        type(file, "\n ");
        type(file, " String test(){\n");
        type(file, "String s = new String");
        waitScanFinished();
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        List listA = completion.listItems;
        type(file, "(\"aaa\"");
        waitScanFinished();
        file.setCaretPosition("(\"a", true);
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        CompletionInfo completion2 = getCompletion();
        List listB = completion2.listItems;
        assertEquals("Code completion list size differs", listA.size(), listB.size());

        ArrayList<String> lA = new ArrayList<String>();
        for (int i = 0; i < listA.size(); i++) {
            lA.add(listA.get(i).toString());
        }

        ArrayList<String> lB = new ArrayList<String>();
        for (int i = 0; i < listB.size(); i++) {
            lB.add(listB.get(i).toString());
        }
        for (int i = 0; i < lA.size(); i++) {
            assertTrue("Item is missing in the second cc list", lB.contains(lA.get(i)));
        }

        completion.listItself.hideAll();
        endTest();
    }

    /**
     * issue #150862
     */
    public void PrefixMatchesKeyword() {
        startTest();
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "GGG");
        EditorOperator file = new EditorOperator("GGG.groovy");
        file.setCaretPosition("GGG {", false);
        type(file, "\n ");
        type(file, " def String x");
        file.setCaretPosition("}", false);
        type(file, "\n ");
        type(file, "class Test2 {\n");
        type(file, "def test={\n");
        type(file, "new GGG().in");
        waitScanFinished();
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        String[] res = {"inject()", "inspect()", "invokeMethod()"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();
        endTest();
    }
}
