/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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