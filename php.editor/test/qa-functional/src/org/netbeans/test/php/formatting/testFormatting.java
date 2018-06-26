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
package org.netbeans.test.php.formatting;

import java.awt.event.KeyEvent;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */
public class testFormatting extends formatting {

    static final String TEST_PHP_NAME = "PhpProject_formatting_0001";

    public testFormatting(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testFormatting.class).addTest(
                "CreateApplication",
                "Create_a_PHP_web_page",
                "Format_default_code_of_PHP_web_page",
                "Undo_Formatting_of_PHP_web_page",
                "Create_a_PHP_file",
                "Format_default_code_of_PHP_file",
                "Undo_Formatting_of_PHP_file",
                "Check_formatting_options_count",
                "bug181787").enableModules(".*").clusters(".*") //.gui( true )
                );
    }

    public void CreateApplication() {
        startTest();

        CreatePHPApplicationInternal(TEST_PHP_NAME);

        endTest();
    }

    /**
     * TODO finish
     */
    public void bug177251() {
        startTest();
        //check default
        EditorOperator eoPHP = new EditorOperator("EmptyPHP.php");
        DeleteFileContent(eoPHP);
        eoPHP.insert("<?php \n"
                + "class G_Check {\n"
                + "private static $sizeUnits = array(\n"
                + "\"item\" => array(\n"
                + "\"item\" => array(\n"
                + ")\n"
                + "));\n"
                + "}\n"
                + "?>");

        String sTextOriginal = eoPHP.getText();
        Sleep(1000);
        eoPHP.clickForPopup();
        Sleep(1000);
        JPopupMenuOperator menu = new JPopupMenuOperator();
        menu.pushMenu("Format");
        String sTextFormatted = eoPHP.getText();
        String sTextIdeal = "<?php\n\n"
                + "class G_Check {\n\n"
                + "    private static $sizeUnits = array(\n"
                + "    \"item\" => array(\n"
                + "        \"item\" => array(\n"
                + "            \"blbblas\"\n"
                + "       )\n"
                + "    ));\n"
                + "}\n"
                + "?>";
        setPHPIndentation(0, 8, 4);

        endTest();
    }

    public void bug181787() {
        startTest();
        if (getPlatform() == 4096) {
            fail("Not implemented for MAC OS X yet!"); 
        }
        setMethodParametersWrappingOptions(1);

        EditorOperator eoPHP = new EditorOperator("EmptyPHP.php");
        DeleteFileContent(eoPHP);
        eoPHP.insert("<?php\n "
                + "function testFunction($a, $b, $c) {}"
                + "\n?>");
        String sTextOriginal = eoPHP.getText();
        Sleep(1000);
        eoPHP.clickForPopup();
        Sleep(1000);
        JPopupMenuOperator menu = new JPopupMenuOperator();
        menu.pushMenu("Format");
        String sTextFormatted = eoPHP.getText();

        String sTextIdeal = "<?php\n\n"
                + "function testFunction($a,\n        $b,\n        $c) {\n    \n}"
                + "\n\n?>";
        sTextFormatted = sTextFormatted.replaceAll("[\r]", "");
        assertTrue("Strings differ",sTextFormatted.equals(sTextIdeal));
        
        //tests for If Long
        setMethodParametersWrappingOptions(2);
        DeleteFileContent(eoPHP);
        eoPHP.insert("<?php\n "
                + "function testFunction($firstLongParameter = \"bdlkfjdsa fhjjkdshafjd a\" , $secondLongParameter, $thirdLongParameter) { }"
                + "\n ?>");
        sTextOriginal = eoPHP.getText();
        eoPHP.clickForPopup();
        menu = new JPopupMenuOperator();
        menu.pushMenu("Format");
        sTextFormatted = eoPHP.getText();

        sTextIdeal = "<?php\n\n"
                + "function testFunction($firstLongParameter = \"bdlkfjdsa fhjjkdshafjd a\",\n        $secondLongParameter, $thirdLongParameter) {\n    \n}"
                + "\n\n?>";

        sTextFormatted = sTextFormatted.replaceAll("[\r]", "");
        assertTrue("Strings differ",sTextFormatted.equals(sTextIdeal));

        //test for never
        setMethodParametersWrappingOptions(0);
        DeleteFileContent(eoPHP);
        eoPHP.insert("<?php \n "
                + "function testFunction($firstLongParameter, $secondLongParameter, $thirdLongParameter) { }"
                + "\n ?>");
        sTextOriginal = eoPHP.getText();
        eoPHP.clickForPopup();
        menu = new JPopupMenuOperator();
        menu.pushMenu("Format");
        sTextFormatted = eoPHP.getText();

        sTextIdeal = "<?php\n\n"
                + "function testFunction($firstLongParameter, $secondLongParameter, $thirdLongParameter) {\n    \n}"
                + "\n\n?>";

        sTextFormatted = sTextFormatted.replaceAll("[\r]", "");
        assertTrue("Strings differ",sTextFormatted.equals(sTextIdeal));

        endTest();
    }

    public void Check_formatting_options_count() throws InterruptedException {
        
        startTest();
        
            JDialogOperator window = selectPHPFromEditorOptions(0, getPlatform());
//        JDialogOperator window = selectPHPFromEditorOptions(0);

        //categories - check if they are all present
        JComboBoxOperator category = new JComboBoxOperator(window, 2);
        Sleep(5000);



        int count = category.getItemCount();
//        window.close();
        window.pressKey(KeyEvent.VK_ENTER);
        assertEquals(7, count); // +1 for uses
        endTest();

    }

    public void Create_a_PHP_web_page() {
        startTest();

        CreatePHPFile(TEST_PHP_NAME, "PHP Web Page", null);

        endTest();
    }

    public void Format_default_code_of_PHP_web_page() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("EmptyPHPWebPage.php");
        String sTextOriginal = eoPHP.getText();
        Sleep(1000);
        eoPHP.clickForPopup();
        Sleep(1000);
        JPopupMenuOperator menu = new JPopupMenuOperator();
        menu.pushMenu("Format");
        String sTextFormatted = eoPHP.getText();

        if (!sTextOriginal.equals(sTextFormatted)) {
            fail("Default formatting is not valid.");
        }

        endTest();
    }

    public void Undo_Formatting_of_PHP_web_page() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("EmptyPHPWebPage.php");
        String sTextOriginal = eoPHP.getText();
        eoPHP.setCaretPosition(0);
        eoPHP.insert("                          ");
        String sTextChanged = eoPHP.getText();
        Sleep(1000);
        eoPHP.clickForPopup();
        Sleep(1000);
        JPopupMenuOperator menu = new JPopupMenuOperator();
        menu.pushMenu("Format");
        String sTextFormatted = eoPHP.getText();

        if (!sTextOriginal.equals(sTextFormatted)) {
            fail("Default formatting is not valid.");
        }

        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Undo");
        Sleep(5000);
        String sTextUndo = eoPHP.getText();
        if (!sTextChanged.equals(sTextUndo)) {
            fail("Undo formatting is not valid. Expected: \n " + sTextChanged + " \n but was \n" + sTextUndo);
        }

        endTest();
    }

    public void Create_a_PHP_file() {
        startTest();

        CreatePHPFile(TEST_PHP_NAME, "PHP File", null);

        endTest();
    }

    public void Format_default_code_of_PHP_file() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("EmptyPHP.php");
        String sTextOriginal = eoPHP.getText();
        Sleep(1000);
        eoPHP.clickForPopup();
        Sleep(1000);
        JPopupMenuOperator menu = new JPopupMenuOperator();
        menu.pushMenu("Format");
        String sTextFormatted = eoPHP.getText();

        if (!sTextOriginal.equals(sTextFormatted)) {
            fail("Default formatting is not valid. BUG 181339 may be still valid.");
        }

        endTest();
    }

    public void Undo_Formatting_of_PHP_file() {
        startTest();

        EditorOperator eoPHP = new EditorOperator("EmptyPHP.php");
        String sTextOriginal = eoPHP.getText();
        eoPHP.setCaretPosition(0);
        eoPHP.insert("                          ");
        String sTextChanged = eoPHP.getText();
        Sleep(1000);
        eoPHP.clickForPopup();
        Sleep(1000);
        JPopupMenuOperator menu = new JPopupMenuOperator();
        menu.pushMenu("Format");
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Undo");
        Sleep(5000);
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Undo");
        Sleep(5000);
        String sTextUndo = eoPHP.getText();
        if (!sTextOriginal.equals(sTextUndo)) {
            fail("Undo formatting is not valid. Expected: \n" + sTextOriginal + " but was \n" + sTextUndo);
        }

        endTest();
    }
}
