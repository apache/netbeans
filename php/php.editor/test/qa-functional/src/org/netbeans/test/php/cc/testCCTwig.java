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
 * @author Martin Kanak
 */
public class testCCTwig extends cc {
    static final String TEST_PHP_NAME = "PhpProject_cc_twig";

    public testCCTwig(String arg0) {
        super(arg0);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCTwig.class).addTest(
                "CreateApplication",
                "testPhpTwigCodeCompletion",
                "testPhpTwigCodeCompletionAll",
                "testPhpTwigCodeTemplateCompletion").enableModules(".*").clusters(".*") //.gui( true )                
                );
    }
    
    public void CreateApplication() {
        startTest();
        CreatePHPApplicationInternal(TEST_PHP_NAME);
        endTest();
    }
    
    public void testPhpTwigCodeCompletion() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "Twig HTML file", null);
        startTest();
        file.setCaretPosition("#}", false);
        TypeCode(file, "{%el");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);
        CompletionInfo jCompl = GetCompletion();
        String[] ideal = {"else", "elseif", "else", "elseif"};
        CheckCompletionItems(jCompl.listItself, ideal);
        endTest();
    }
    
    public void testPhpTwigCodeCompletionAll() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "Twig HTML file", null);
        startTest();
        file.setCaretPosition("#}", false);
        TypeCode(file, "{% a");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        new EventTool().waitNoEvent(1000);
        CompletionInfo jCompl = GetCompletion();
        String[] ideal = {"autoescape", "abs", "attribute", "and", "as"};
        CheckCompletionItems(jCompl.listItself, ideal);
        endTest();
    }
    
    public void testPhpTwigCodeTemplateCompletion() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "Twig HTML file", null);
        startTest();
        file.setCaretPosition("#}", false);
        TypeCode(file, "\n{% ae");
        file.pushTabKey();
        new EventTool().waitNoEvent(1000);
        String ideal = "{% autoescape\n";
        CheckResult(file, ideal);//file.getLineNumber(2);
        endTest();
    }
}
