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
package org.netbeans.test.php.cc;

import java.awt.event.InputEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha
 */
public class testCCNamespaces extends cc {

    static final String TEST_PHP_NAME = "PhpProject_cc_testCCNamespaces";

    public testCCNamespaces(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCNamespaces.class).addTest(
                "CreateApplication",
                "testCCNamespaceSameFile",
                "testCCClassNamespaceSameFile",
                "testCCNamespaceDiffFile",
                "testCCClassNamespaceDiffFile"
                ).enableModules(".*").clusters(".*") //.gui( true )
                );
    }

    public void CreateApplication() {
        startTest();
        System.out.println("<<< "+TEST_PHP_NAME);
        CreatePHPApplicationInternal(TEST_PHP_NAME);
        endTest();
    }

    public void CreatePHPFile() {
        startTest();
        SetAspTags(TEST_PHP_NAME, true);
        CreatePHPFile(TEST_PHP_NAME, "PHP File", null);
        endTest();
    }

    public void testCCNamespaceSameFile() {
        startTest();
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "NNSFNamespace");
        new EventTool().waitNoEvent(1000);
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n namespace Foo\\Baz; \n class Test{\n");
        file.setCaretPosition("}", false);
        TypeCode(file, "\n namespace Foo\\Bas\\Bar; \n class TestBar{\n ");
        file.setCaretPosition("namespace Foo\\Bas\\Bar;", false);
        TypeCode(file, "\n  use Foo\\Ba");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        String[] ideal = {"Baz", "Bas"};
        new EventTool().waitNoEvent(1000);
        CompletionInfo jCompl = GetCompletion();
        CheckCompletionItems(jCompl.listItself, ideal);
        endTest();
    }

    public void testCCClassNamespaceSameFile() {
        startTest();
        EditorOperator file = new EditorOperator("NNSFNamespace");
        new EventTool().waitNoEvent(1000);
        file.setCaretPosition(" namespace Foo\\Baz;", false);
        TypeCode(file, "class Test2{ \n");
        file.setCaretPosition("use Foo\\Ba", false);
        TypeCode(file, "z; \n $a = new Foo\\Baz\\");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        String[] ideal = {"Test", "Test2"};
        new EventTool().waitNoEvent(1000);
        CompletionInfo jCompl = GetCompletion();
        CheckCompletionItems(jCompl.listItself, ideal);
        endTest();
    }

    public void testCCNamespaceDiffFile() {
        startTest();
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "DNDFNamespace");
        new EventTool().waitNoEvent(1000);
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n namespace Alfa\\Gamma; \n namespace Alfa\\Beta; \n class Test{\n public static $b; \n public static function foo(){\n");
        file.save();
        file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "DNDFNamespace2");
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n use Alfa\\");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        String[] ideal = {"Gamma", "Beta"};
        new EventTool().waitNoEvent(1000);
        CompletionInfo jCompl = GetCompletion();
        CheckCompletionItems(jCompl.listItself, ideal);
        file.save();
        endTest();
    }
    
      public void testCCClassNamespaceDiffFile() {
        startTest();
        EditorOperator file = new EditorOperator("DNDFNamespace2");
        new EventTool().waitNoEvent(1000);
        file.setCaretPosition("use Alfa\\", false);
        TypeCode(file, "Beta; \n include 'DNDFNamespace.php'; \n $b=new Beta\\Test::");
        

        file.typeKey(' ', InputEvent.CTRL_MASK);
        String[] ideal = {"foo", "$b"};
        new EventTool().waitNoEvent(1000);
        CompletionInfo jCompl = GetCompletion();
        CheckCompletionItems(jCompl.listItself, ideal);
        endTest();
    }

    
}
