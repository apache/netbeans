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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import static junit.framework.Assert.fail;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.php.GeneralPHP;
import static org.netbeans.test.php.cc.testCC.TEST_PHP_NAME;

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