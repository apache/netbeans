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
import java.util.List;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.groovy.GeneralGroovy;

/**
 *
 * @author Vladimir Riha
 */
public class testCCCamelCase extends GeneralGroovy {

    static final String TEST_BASE_NAME = "groovycc_";
    static int name_iterator = 0;

    public testCCCamelCase(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCCamelCase.class).addTest(
                "CreateApplication",
                "ExtendsUpperCaseClass",
                "ExtendsMixedCaseClass",
                "EmptyLowerCaseClass",
                "EmptyMixedCaseClass").enableModules(".*").clusters(".*"));
    }

    public void CreateApplication() {
        startTest();
        createJavaApplication(TEST_BASE_NAME + name_iterator);
        testCCCamelCase.name_iterator++;
        endTest();
    }

    public void ExtendsUpperCaseClass() {
        startTest();

        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "AA");
        EditorOperator file = new EditorOperator("AA.groovy");
        file.setCaretPosition("AA ", false);
        waitScanFinished();
        type(file, "extends AC");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        GeneralGroovy.CompletionInfo completion = getCompletion();
        String[] res = {"AbstractCollection"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void ExtendsMixedCaseClass() {
        startTest();
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "BB");
        EditorOperator file = new EditorOperator("BB.groovy");
        file.setCaretPosition("BB ", false);
        waitScanFinished();
        type(file, "extends AbC");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        GeneralGroovy.CompletionInfo completion = getCompletion();
        String[] res = {"AbstractCollection"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();


        endTest();
    }

    public void EmptyLowerCaseClass() {
        startTest();
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "CC");
        EditorOperator file = new EditorOperator("CC.groovy");
        file.setCaretPosition("CC ", false);
        type(file, "extends os");
        waitScanFinished();
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        GeneralGroovy.CompletionInfo completion = getCompletion();
        List items = completion.listItems;
        assertTrue("Code completion offers something but should not", items.get(0).equals("No suggestions"));
        completion.listItself.hideAll();

        endTest();
    }

    public void EmptyMixedCaseClass() {
        startTest();
        EditorOperator file = new EditorOperator("CC.groovy");
        file.setCaretPosition("extends ", false);
        type(file, "D");
        waitScanFinished();
        file.setCaretPosition("Dos", false);
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        GeneralGroovy.CompletionInfo completion = getCompletion();
        List items = completion.listItems;
        assertTrue("Code completion offers something but should not", items.get(0).equals("No suggestions"));
        completion.listItself.hideAll();

        endTest();
    }
}
