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
