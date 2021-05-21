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
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */
public class testCCInDetail extends cc {

    static final String TEST_PHP_NAME = "PhpProject_cc_0002";

    public testCCInDetail(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCInDetail.class).addTest(
                "CreateApplication",
                "CreatePHPFile",
                "testPhp54ArrayDereferencing",
                "DetailedCodeCompletionTestingPartOne",
                "DetailedCodeCompletionTestingPartTwo",
                "DetailedCodeCompletionTestingPartThree",
                "testPhp54Callable",
                "testPhp54AnonymousObject").enableModules(".*").clusters(".*") //.gui( true )
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

    private class CCompletionCase {

        public String sInitialLocation;
        public String sCode;
        public String sCompletionLocation;
        int iCompletionType;
        int iResultOffset;
        public String sResult;
        public int iCleanupOffset;
        public int iCleanupCount;
        public static final int COMPLETION_LIST = 0;
        public static final int COMPLETION_STRING = 1;

        public CCompletionCase(
                String sIL,
                String sC,
                String sCL,
                int iCT,
                int iR,
                String sR,
                int iCO,
                int iCC) {
            sInitialLocation = sIL;
            sCode = sC;
            sCompletionLocation = sCL;
            iCompletionType = iCT;
            iResultOffset = iR;
            sResult = sR;
            iCleanupOffset = iCO;
            iCleanupCount = iCC;
        }
    };

    private boolean CheckCodeCompletion(CCompletionCase cc, String fileName) {
        EditorOperator eoPHP = new EditorOperator(fileName);
        // Locate position
        eoPHP.setCaretPosition(cc.sInitialLocation, false);
        // Type cod
        TypeCode(eoPHP, "\n");
        int l = (cc.sCode.length());
        TypeCode(eoPHP, cc.sCode);
        // Locate completion position
        eoPHP.setCaretPosition(cc.sCompletionLocation, false);
        // Invoke completion
        eoPHP.typeKey(' ', InputEvent.CTRL_MASK);
        Sleep(5000);
        // Check result
        String[] asResult = cc.sResult.split("[|]");
        if (CCompletionCase.COMPLETION_LIST == cc.iCompletionType) {
            // Check completion list
            CompletionInfo completionInfo = GetCompletion();
            if (null == completionInfo) {
                fail("NPE instead of competion info.");
            }

            System.out.println("Looking for completion " + cc.sResult + " for " + cc.sCode);
            CheckCompletionItems(completionInfo.listItself, asResult);
            completionInfo.listItself.hideAll();
        } else if (CCompletionCase.COMPLETION_STRING == cc.iCompletionType) {
            Sleep(1000);
            // Check string(s)
            for (int i = 0; i < asResult.length; i++) {
                String sCode = eoPHP.getText(eoPHP.getLineNumber() - cc.iResultOffset + i);
                if (!sCode.matches("^[ \t]*" + asResult[i] + "[ \t\r\n]*$")) {
                    return false;
                }
                //fail( "Unable to find required string, found: \"" + sCode + "\", expected: \"" + asResult[ i ] + "\"" );
            }
        } else {
            fail("Invalid data for code completion case.");
        }
        // Cleanup
        int iLine = eoPHP.getLineNumber() + cc.iCleanupOffset;
        for (int i = 0; i < cc.iCleanupCount; i++) {
            eoPHP.deleteLine(iLine);
        }
        return true;
    }

    public void testPhp54ArrayDereferencing() {
        CreatePHPFile(TEST_PHP_NAME, "PHP File", "ArrayDeref");
        startTest();
        CCompletionCase test = new CCompletionCase("*/", "class MyClass {\n public $v;\n/**  @return MyClass[]     */\n public function getArray() {\n return array(new MyClass());\n }\n}\n\n $aa = new MyClass();\n $aa->getArray()[0]->", "$aa->getArray()[0]->", CCompletionCase.COMPLETION_LIST, 0, "v|getArray", -1, 12);
        boolean result = CheckCodeCompletion(test, "ArrayDeref.php");
        assertTrue("Failed Array Dereferencing test", result);
        endTest();
    }

    public void testPhp54AnonymousObject() {
        CreatePHPFile(TEST_PHP_NAME, "PHP File", "Anonymous.php");
        startTest();
        CCompletionCase test = new CCompletionCase("*/", "class MyClass {\n public $v;\n public $f;\n}\n\n (new MyClass())->", "(new MyClass())->", CCompletionCase.COMPLETION_LIST, 0, "v|f", -1, 12);
        boolean result = CheckCodeCompletion(test, "Anonymous.php");
        assertTrue("Failed Array AnonymousObject test", result);
        endTest();
    }

    public void testPhp54Callable() {
        CreatePHPFile(TEST_PHP_NAME, "PHP File", "Callable");
        startTest();
        CCompletionCase test = new CCompletionCase("*/", "function name(ca","(ca", CCompletionCase.COMPLETION_LIST, 0, "callable", -1,1);
        boolean result = CheckCodeCompletion(test, "Callable.php");
        assertTrue("Failed Callable test", result);
        endTest();
    }

    public void DetailedCodeCompletionTestingPartOne() {
        startTest();
        CreatePHPFile(TEST_PHP_NAME, "PHP File", "Details1");
        CCompletionCase[] accTests = {
            new CCompletionCase("*/", "$test=1;\n$test1=$tes;", "$test1=$tes", CCompletionCase.COMPLETION_LIST, 0, "$test|$test1", -1, 2),
            new CCompletionCase("*/", "$test=1;\n$test1=\"a\";\n$newvar=$tes;", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "$test|$test1", -2, 3),
            new CCompletionCase("*/", "$test=1;\n$test1=\"$tes\";", "$test1=\"$tes", CCompletionCase.COMPLETION_LIST, 0, "$test|$test1", -1, 2),
            new CCompletionCase("*/", "$test=1;\nif ($test==1){\n$test1=\"a\";\n}\n$newvar=$tes;", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "$test|$test1", -4, 6),
            new CCompletionCase("*/", "$test=1;\nif ($test==1){\n$test1=\"a\";\n}\nif ($test==1){\n$newvar=$tes;\n}", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "$test|$test1", -5, 8),
            new CCompletionCase("*/", "$test=1;\nif ($test==1){\n$test1=\"a\";\nif ($test==1){\n$newvar=$tes;\n}\n}{{", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "$test|$test1", -4, 9),
            new CCompletionCase("*/", "$test=1;\n$test1=\"a\";\n// $newvar=$tes;", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -2, 3),
            new CCompletionCase("*/", "$test=1;\n/*  $test1=\"a\";\n$newvar=$tes;", "$newvar=$tes", CCompletionCase.COMPLETION_STRING, 0, "[*] [$]newvar=[$]test;", -2, 4),
            new CCompletionCase("*/", "$test=1;\n/**  $test1=\"a\";\n$newvar=$tes;", "$newvar=$tes", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -2, 4),
            new CCompletionCase("*/", "/**  @v\n", "@v", CCompletionCase.COMPLETION_LIST, 0, "@var|@version", -1, 3),};
        String sFailed = "";
        int iFailed = 0;
        for (CCompletionCase cc : accTests) {
            if (!CheckCodeCompletion(cc, "Details1.php")) {
                iFailed++;
                sFailed = sFailed + "|" + cc.sResult;
            }
        }
        if (0 != iFailed) {
            fail("" + iFailed + " test(s) failed, invalid results: \"" + sFailed + "\"");
        }

        endTest();
    }

    public void DetailedCodeCompletionTestingPartThree() {
        startTest();
        CreatePHPFile(TEST_PHP_NAME, "PHP File", "Details3");
        CCompletionCase[] accTests = {
            new CCompletionCase("?>", "<?php\n$test=1;\n?>\nText\n<?php\n$newvar=$tes\n?>", "$newvar=$tes", CCompletionCase.COMPLETION_STRING, 0, "[$]newvar=[$]test", -5, 7),
            new CCompletionCase("?>", "<?php\n$test=1;\n?>\nText\n<?=\n$newvar=$tes\n=>", "$newvar=$tes", CCompletionCase.COMPLETION_STRING, 0, "[$]newvar=[$]test", -5, 7),
            new CCompletionCase("*/", "func", "func", CCompletionCase.COMPLETION_LIST, 0, "func_get_arg|func_get_args|function_exists|function", 0, 1),
            new CCompletionCase("*/", "function func(){\nret", "ret", CCompletionCase.COMPLETION_STRING, 0, "return;", -1, 3),
            new CCompletionCase("*/", "function func($param){\n$newvar=$par", "$newvar=$par", CCompletionCase.COMPLETION_STRING, 0, "[$]newvar=[$]param", -1, 3),
            new CCompletionCase("*/", "function func(&$param){\n$newvar=$par", "$newvar=$par", CCompletionCase.COMPLETION_STRING, 0, "[$]newvar=[$]param", -1, 3),
            new CCompletionCase("*/", "function func($param){\n$newvar=$param;\n}\n$test=$newv\n{", "$test=$newv", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -3, 6),
            new CCompletionCase("*/", "function func($param){\n$newvar=$param;\n}\n$test=$par\n{", "$test=$par", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -3, 6)};
        String sFailed = "";
        int iFailed = 0;
        for (CCompletionCase cc : accTests) {
            if (!CheckCodeCompletion(cc, "Details3.php")) {
                iFailed++;
                sFailed = sFailed + "|" + cc.sResult;
            }
        }
        if (0 != iFailed) {
            fail("" + iFailed + " test(s) failed, invalid results: \"" + sFailed + "\"");
        }

        endTest();
    }

    public void DetailedCodeCompletionTestingPartTwo() {
        startTest();
        CreatePHPFile(TEST_PHP_NAME, "PHP File", "Details2");
        CCompletionCase[] accTests = {
            new CCompletionCase("*/", "cla", "cla", CCompletionCase.COMPLETION_LIST, 0, "class_exists|class_implements|class", 0, 1),
            new CCompletionCase("*/", "class MyCla", "class MyCla", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", 0, 1),
            new CCompletionCase("*/", "class MyClass ext", "class MyClass ext", CCompletionCase.COMPLETION_STRING, 0, "class MyClass extends", 0, 1),
            new CCompletionCase("*/", "class MyClass extends MyCla", "class MyClass extends MyCla", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", 0, 1),
            new CCompletionCase("*/", "class MyClass {\npubl", "publ", CCompletionCase.COMPLETION_STRING, 0, "public ", -1, 3),
            new CCompletionCase("*/", "class MyClass {\npublic func", "public func", CCompletionCase.COMPLETION_LIST, 0, "function", -1, 3),
            new CCompletionCase("*/", "class MyClass {\npublic function func", "function func", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -1, 3),
            //            new CCompletionCase("*/", "class MyClass {\npublic function func(){\n$th\n}\n}\n{{", "$th", CCompletionCase.COMPLETION_STRING, 0, "[$]this->", -2, 8),
            new CCompletionCase("*/", "class MyClass {\npublic $test;\npublic function func(){\necho \"Hello\";\n}\n}\n$test= new MyClass();\n$test->\n{{", "$test->", CCompletionCase.COMPLETION_LIST, 0, "test|func", -6, 10),
            new CCompletionCase("*/", "class MyClass {\n public function func2(){}\n public function func(){\necho \"$this->\";\n}\n}\n{{", "$this->", CCompletionCase.COMPLETION_LIST, 0, "func|func2", -6, 9),
            //            new CCompletionCase("*/", "class MyClass {\nprotected $test;\nprotected function func(){\necho \"Hello\";\n}\n}\n$test=new MyClass();\n$test->\n{{", "$test->", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -6, 10),
        //            new CCompletionCase("*/", "class MyClass {\nprivate $test;\nprivate function func(){\necho \"Hello\";\n}\n}\n$test=new MyClass();\n$test->\n{{", "$test->", CCompletionCase.COMPLETION_LIST, 0, "$fu", -6, 10),
        //            new CCompletionCase("*/", "class MyClass {\npublic static $test;\npublic static function func(){\necho \"Hello\";\n}\n}\n$test=MyClass::\n{{", "MyClass::", CCompletionCase.COMPLETION_LIST, 0, "$test|func", -6, 10),
        //            new CCompletionCase("*/", "class MyClass {\nprotected static $test;\nprotected static function func(){\necho \"Hello\";\n}\n}\n$test=MyClass::\n{{", "MyClass::", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -6, 10),
        //            new CCompletionCase("*/", "class MyClass {\nprivate static $test;\nprivate static function func(){\necho \"Hello\";\n}\n}\n$test=MyClass::\n{{", "MyClass::", CCompletionCase.COMPLETION_LIST, 0, "No suggestions", -6, 10)
        };

        String sFailed = "";
        int iFailed = 0;
        for (CCompletionCase cc : accTests) {
            if (!CheckCodeCompletion(cc, "Details2.php")) {
                iFailed++;
                sFailed = sFailed + "|" + cc.sResult;
            }
        }
        if (0 != iFailed) {
            fail("" + iFailed + " test(s) failed, invalid results: \"" + sFailed + "\"");
        }

        endTest();
    }
}
