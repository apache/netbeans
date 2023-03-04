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
public class testCCTraits extends cc {

    static final String TEST_PHP_NAME = "PhpProject_cc_0003";

    public testCCTraits(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCTraits.class).addTest(
                "CreateApplication",
                "testPhp54TraitsSameFile",
                "testPhp54TraitsDifferentFile").enableModules(".*").clusters(".*") //.gui( true )
                );
    }

    public void CreateApplication() {
        startTest();
        CreatePHPApplicationInternal(TEST_PHP_NAME);
        endTest();
    }

    public void CreatePHPFile() {
        startTest();
        SetAspTags(TEST_PHP_NAME, true);
        CreatePHPFile(TEST_PHP_NAME, "PHP File", null);
        endTest();
    }

    public void testPhp54TraitsSameFile() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", null);
        startTest();
        file.setCaretPosition("*/", false);
        TypeCode(file, "trait Test{ \n public function test(){} \n } \n class Foo{ \n use Test; \n function bar(){\n $this->");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);
        CompletionInfo jCompl = GetCompletion();
        String[] ideal = {"test", "bar"};
        CheckCompletionItems(jCompl.listItself, ideal);
        endTest();
    }

    public void testPhp54TraitsDifferentFile() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "TraitTest");
        startTest();
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n namespace testA; \n trait Test{ \n public function test(){}");
        file.save();
        file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "TraitTest2");
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n class Bar{ \n use testA\\Test; \n public function testfoo(){\n $this->");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);
        CompletionInfo jCompl = GetCompletion();
        String[] ideal = {"test", "testfoo"};
        CheckCompletionItems(jCompl.listItself, ideal);
        endTest();
    }
}
