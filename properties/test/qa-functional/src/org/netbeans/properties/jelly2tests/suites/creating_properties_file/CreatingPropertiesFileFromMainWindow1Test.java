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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * CreatingPropertiesFileFromMainWindow1.java
 *
 * This is autometed test for netBeans version 40.
 *
 * 1. Open New Wizard. Use menu File|New ... from main window.
 * 2. Select from wizard Templates|Other|Properties File and click Next button.
 * 3. There is set default file name and package name. Do not change these values.
 * 4. Confirm wizard.
 * 5. Wait to properties file appeared in Explorer.
 * RESULT: New properties file will be add (with default name - properties.properties) to adequate place in Explorer and opened in editor.
 *
 * Created on 16. September 2002
 */
package org.netbeans.properties.jelly2tests.suites.creating_properties_file;
import junit.framework.Test;
import org.netbeans.jellytools.*;
import lib.PropertiesEditorTestCase;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author  Petr Felenda - QA Engineer ( petr.felenda@sun.com )
 */
public class CreatingPropertiesFileFromMainWindow1Test extends PropertiesEditorTestCase {

    /*
     * Definition of member variables
     */
    /**
     * Constructor - Creates a new instance of this class
     */
    public CreatingPropertiesFileFromMainWindow1Test(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CreatingPropertiesFileFromMainWindow1Test.class).addTest("testCreatingPropertiesFileFromMainWindow1").enableModules(".*").clusters(".*"));
    }

    /**
     * This method contains body of test
     * @return void
     */
    public void testCreatingPropertiesFileFromMainWindow1() {

        // open project
        openDefaultProject();

        /*
         * 1st step of testcase ( here is used toolbar's icon for opening wizard )
         * There will be opened New Wizard from Main Window Toolbar ( icon 'New' from toolbar 'System' )
         */

        MainWindowOperator mainWindowOp = MainWindowOperator.getDefault();
        mainWindowOp.menuBar().pushMenuNoBlock("File" + menuSeparator + "New File...", menuSeparator);

        /*
         * 2nd step of testcase
         * Select from wizard Other|Properties File and click next button.
         */
        NewFileWizardOperator newWizard = new NewFileWizardOperator();
        newWizard.selectCategory(WIZARD_CATEGORY_FILE);
        newWizard.selectFileType(WIZARD_FILE_TYPE);
        newWizard.next();


        /*
         * 3rd step of testcase
         * (here is nothing happen)
         * There is set default name and package. Do not change these values
         * ( package must be added because autotemed tests add jars and mount file-
         * systems witch are don't have deterministic order
         */
        // it must be selected a Folder to place the file ( is this a bug ? )
        NewJavaFileNameLocationStepOperator nameStepOper = new NewJavaFileNameLocationStepOperator();
        JTextFieldOperator jtfo = new JTextFieldOperator(nameStepOper, 2);
        jtfo.setText("src");

        /*
         * 4th step of testcase
         * Confirm wizard
         */
        newWizard.finish();


        /*
         *  Result
         * Should be added new properties file (with default name) to adequate place in
         * explorer and opened in editor.
         */
        if (!existsFileInEditor(WIZARD_DEFAULT_PROPERTIES_FILE_NAME)) {
            fail("File " + WIZARD_DEFAULT_PROPERTIES_FILE_NAME + " not found in Editor window");
        }
        if (!existsFileInExplorer("<default package>", WIZARD_DEFAULT_PROPERTIES_FILE_NAME + ".properties")) {
            fail("File " + WIZARD_DEFAULT_PROPERTIES_FILE_NAME + " not found in explorer");
        }

    }

    public void tearDown() {
        log("Teardown");
        closeOpenedProjects();
    //closePropertiesFile(); 
    }
}
