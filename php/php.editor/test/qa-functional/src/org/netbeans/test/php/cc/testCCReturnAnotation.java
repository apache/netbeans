/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.php.GeneralPHP;

/**
 *
 * @author mmolda@netbeans.org
 */
public class testCCReturnAnotation extends cc {

    static final String TEST_PHP_NAME = "PhpProject_cc_ReturnAnotations";

    public testCCReturnAnotation(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCReturnAnotation.class).addTest(
                "CreateApplication",
                "CreatePHPSourceFile",
                "testReturnSelf",
                "testReturnStatic",
                "testReturnThis").enableModules(".*").clusters(".*") //.gui( true )
                );


    }

    public void CreateApplication() {
        startTest();

        CreatePHPApplicationInternal(TEST_PHP_NAME);

        endTest();
    }
   
    public void CreatePHPSourceFile() {
        startTest();

        CreatePHPFile(TEST_PHP_NAME, "PHP File", null);

        endTest();
    }

    public void testReturnSelf() {
        startTest();
        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("*/\n", false);
        waitScanFinished();
        eoPHP.insert("\nclass Box {\n" +
                    "    public $var;\n" +
                    "    /**\n" +
                    "     * @return self\n" +
                    "     */\n" +
                    "    public static function getBox() {\n" +
                    "        \n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "Box::getBox()->");
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);

        // Check code completion list
        try {
            GeneralPHP.CompletionInfo completionInfo = GetCompletion();
            if (null == completionInfo) {
                fail("NPE instead of competion info.");
            }

            // Check some completions
            String[] completions = {
                "var",
                "getBox"
            };
            CheckCompletionItems(completionInfo.listItself, completions);
            completionInfo.listItself.hideAll();
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            fail("Completion check failed: \"" + ex.getMessage() + "\"");
        }

        // Clean up
        eoPHP.deleteLine(eoPHP.getLineNumber());

        endTest();
    }
    
    public void testReturnStatic() {
        startTest();
        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("*/\n", false);
        waitScanFinished();
        eoPHP.insert("\nclass BoxB {\n" +
                    "    public $var;\n" +
                    "    /**\n" +
                    "     * @return self\n" +
                    "     */\n" +
                    "    public static function getBox() {\n" +
                    "        \n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "BoxB::getBox()->");
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);

        // Check code completion list
        try {
            GeneralPHP.CompletionInfo completionInfo = GetCompletion();
            if (null == completionInfo) {
                fail("NPE instead of competion info.");
            }

            // Check some completions
            String[] completions = {
                "var",
                "getBox"
            };
            CheckCompletionItems(completionInfo.listItself, completions);
            completionInfo.listItself.hideAll();
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            fail("Completion check failed: \"" + ex.getMessage() + "\"");
        }

        // Clean up
        eoPHP.deleteLine(eoPHP.getLineNumber());

        endTest();
    }
    
    public void testReturnThis() {
        startTest();
        EditorOperator eoPHP = new EditorOperator("newEmptyPHP.php");
        eoPHP.setCaretPosition("*/\n", false);
        waitScanFinished();
        eoPHP.insert("\nclass BoxC {\n" +
                    "    public $var;\n" +
                    "    /**\n" +
                    "     * @return self\n" +
                    "     */\n" +
                    "    public static function getBox() {\n" +
                    "        \n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "BoxC::getBox()->");
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);

        // Check code completion list
        try {
            GeneralPHP.CompletionInfo completionInfo = GetCompletion();
            if (null == completionInfo) {
                fail("NPE instead of competion info.");
            }

            // Check some completions
            String[] completions = {
                "var",
                "getBox"
            };
            CheckCompletionItems(completionInfo.listItself, completions);
            completionInfo.listItself.hideAll();
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            fail("Completion check failed: \"" + ex.getMessage() + "\"");
        }

        // Clean up
        eoPHP.deleteLine(eoPHP.getLineNumber());

        endTest();
    }
}