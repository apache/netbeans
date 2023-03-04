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

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author fzamboj@netbeans.org
 */
public class testCCPhpDoc extends cc {

    static final String TEST_PHP_NAME = "PhpProject_cc_PHPDoc";

    public testCCPhpDoc(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCPhpDoc.class).addTest(
                "automaticCommentGenerationOnFunction",
                "automaticCommentGenerationOnClassVariable") 
//                "automaticCommentGenerationOnGlobalVariable")
                .enableModules(".*").clusters(".*") //.gui( true )
                );
    }

    public void CreateApplication() {
//    startTest( );

        CreatePHPApplicationInternal();

//    endTest( );
    }



    public void automaticCommentGenerationOnGlobalVariable() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("index.php");
        if (!DeleteFileContent(eoPHP))
            fail("File not empty - can't continue. Text: " + eoPHP.getText());
        Sleep(1000);

        eoPHP.insert(
                "<?php\n"
                + "$GLOBALS['_myvar'] = 6;\n"
                + "?>");

        // Locate comment
        eoPHP.setCaretPosition("$GLOBALS['_myvar'] = 6;", true);
        // Add new line
        eoPHP.insert("\n");
        eoPHP.setCaretPositionToLine(eoPHP.getLineNumber() - 1);
        Sleep(1000);
        // Press Ctrl+Space
        eoPHP.insert("/**");
        eoPHP.pushKey(KeyEvent.VK_ENTER);

        Sleep(1000);
        // Check code completion list

        String text = eoPHP.getText();
        String idealText =
                "<?php\n"
                + "/**\n"
                + " *\n"
                + " * @global type $GLOBALS['_myvar']\n"
                + " * @name _myvar \n"
                + " */\n"
                + "$GLOBALS['_myvar'] = 6;\n"
                + "?>";

        assertEquals("\nIssue https://netbeans.org/bugzilla/show_bug.cgi?id=182381 (P4 PHP editor) may be still opened\n"
                +"expected: \n" + idealText +"\n"
                + "was:" + text, idealText, text + "\n");



        endTest();
    }

    /**
     * Automatic generation of var <type> testCCPhpDoc tag on PHP class variable
     * @throws Exception
     */
    public void automaticCommentGenerationOnClassVariable() throws Exception {
        startTest();

        EditorOperator eoPHP = new EditorOperator("index.php");
        if (!DeleteFileContent(eoPHP))
            fail("File not empty - can't continue. Text: " + eoPHP.getText());
        Sleep(1000);

        eoPHP.insert(
                "<?php\n"
                + "class babyclass extends myclass {\n"
                + "var $secondvar = 42;\n"
                + "var $thirdvar;\n"
                + "function babyclass() {\n"
                + "parent::myclass();\n"
                + "$this->firstvar++;\n"
                + "}\n"
                + "function parentfunc($paramie) {\n"
                + "return new myclass;\n"
                + "}\n"
                + "}\n"
                + "?>");

        // Locate comment
        eoPHP.setCaretPosition("var $secondvar = 42;", true);
        // Add new line
        eoPHP.insert("\n");
        eoPHP.setCaretPositionToLine(eoPHP.getLineNumber() - 1);
        Sleep(1000);
        // Press Ctrl+Space
        eoPHP.insert("/**");
        eoPHP.pushKey(KeyEvent.VK_ENTER);

        Sleep(1000);
        // Check code completion list

        String text = eoPHP.getText();
        String idealText =
                "<?php\n"
                + "class babyclass extends myclass {\n"
                + "/**\n"
                + " *\n"
                + " * @var type \n"
                + " */\n"
                + "var $secondvar = 42;\n"
                + "var $thirdvar;\n"
                + "function babyclass() {\n"
                + "parent::myclass();\n"
                + "$this->firstvar++;\n"
                + "}\n"
                + "function parentfunc($paramie) {\n"
                + "return new myclass;\n"
                + "}\n"
                + "}\n"
                + "?>";

        assertEquals("expected: " + idealText
                + " was " + text, idealText, text);


        eoPHP.setCaretPosition("var $thirdvar;", true);
        // Add new line
        eoPHP.insert("\n");
        eoPHP.setCaretPositionToLine(eoPHP.getLineNumber() - 1);
        Sleep(1000);
        // Press Ctrl+Space
        eoPHP.insert("/**");
        eoPHP.pushKey(KeyEvent.VK_ENTER);
        Sleep(1000);

        text = eoPHP.getText();
        idealText =
                "<?php\n"
                + "class babyclass extends myclass {\n"
                + "/**\n"
                + " *\n"
                + " * @var type \n"
                + " */\n"
                + "var $secondvar = 42;\n"
                + "/**\n"
                + " *\n"
                + " * @var type \n"
                + " */\n"
                + "var $thirdvar;\n"
                + "function babyclass() {\n"
                + "parent::myclass();\n"
                + "$this->firstvar++;\n"
                + "}\n"
                + "function parentfunc($paramie) {\n"
                + "return new myclass;\n"
                + "}\n"
                + "}\n"
                + "?>";

        assertEquals("expected: " + idealText
                + " was " + text, idealText, text);


        endTest();
    }

    /**
     * Automatic generation of testCCPhpDoc tags on Function outside class definition
     * @throws Exception
     */
    public void automaticCommentGenerationOnFunction() throws Exception {
        startTest();

        this.CreateApplication();

        // Get editor
        EditorOperator eoPHP = new EditorOperator("index.php");
        if (!DeleteFileContent(eoPHP))
            fail("File not empty - can't continue. Text: " + eoPHP.getText());
        Sleep(1000);

        eoPHP.insert(
                "<?php\n"
                + "function firstFunc($param1, $param2 = 'optional') {\n"
                + "static $staticvar = 7;\n"
                + "global $_myvar;\n"
                + "return $staticvar;\n"
                + "}\n"
                + "?>");

        // Locate comment
        eoPHP.setCaretPosition("function", true);
        // Add new line
        eoPHP.insert("\n");
        eoPHP.setCaretPositionToLine(eoPHP.getLineNumber() - 1);
        Sleep(1000);
        // Press Ctrl+Space
        eoPHP.insert("/**");
        eoPHP.pushKey(KeyEvent.VK_ENTER);

        Sleep(1000);
        // Check code completion list

        String text = eoPHP.getText();
        String idealText = "<?php\n"
                + "/**\n"
                + " * \n"
                + " * @global type $_myvar\n"
                + " * @staticvar int $staticvar\n"
                + " * @param type $param1\n"
                + " * @param type $param2\n"
                + " * @return int\n"
                + " */\n"
                + "function firstFunc($param1, $param2 = 'optional') {\n"
                + "static $staticvar = 7;\n"
                + "global $_myvar;\n"
                + "return $staticvar;\n"
                + "}\n"
                + "?>";

        assertEquals("expected: " + idealText
                + " was " + text, idealText, text);
 
        endTest();
    }
}