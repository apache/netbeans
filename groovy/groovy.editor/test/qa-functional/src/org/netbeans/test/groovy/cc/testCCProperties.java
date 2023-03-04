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
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.groovy.GeneralGroovy;

/**
 *
 * @author Vladimir Riha
 */
public class testCCProperties extends GeneralGroovy {

    static final String TEST_BASE_NAME = "groovyprop_";
    static int name_iterator = 0;

    public testCCProperties(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCProperties.class).addTest(
                "CreateApplication",
                "GroovyFieldsSameFile" ,
                "GroovyFieldsDifferentFile"
                ).enableModules(".*").clusters(".*"));
    }

    public void CreateApplication() {
        startTest();
        createJavaApplication(TEST_BASE_NAME + name_iterator);
        testCCProperties.name_iterator++;
        endTest();
    }

    public void GroovyFieldsSameFile() {
        startTest();
        
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "AA");
        EditorOperator file = new EditorOperator("AA.groovy");
        file.setCaretPosition("AA {", false);
        type(file, "\n  def String x");
        type(file, "\n  def String xx");
        type(file, "\n  def function1 = {\n");

        file.setCaretPosition("class AA {", true);
        type(file, "\n ");
        file.setCaretPositionToLine(file.getLineNumber() - 1);
        type(file, "class BB {\n def BB(){ \n");
        waitScanFinished();
        type(file, "foo = new AA().");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        completion.listItself.clickOnItem("x");
        String[] res = {"x", "xx", "function1"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void GroovyFieldsDifferentFile() {
        startTest();

        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "DD");
        EditorOperator file = new EditorOperator("DD.groovy");
        file.setCaretPosition("DD {", false);
        type(file, "\n  def String x");
        type(file, "\n  def String xx");
        type(file, "\n  def function1 = {\n");

        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "CC");
        file = new EditorOperator("CC.groovy");
        file.setCaretPosition("CC {", false);
        type(file, "\n def CC(){ \n");
        waitScanFinished();
        type(file, "foo = new DD().");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        completion.listItself.clickOnItem("x");
        String[] res = {"x", "xx", "function1"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();

        endTest();
    }    
}
