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

/*
 * CreatingPropertiesFileFromMainWindow2.java
 *
 * This is autometed test for netBeans version 40.
 *
 * Created on 18. September 2002
 */
package org.netbeans.properties.jelly2tests.suites.creating_properties_file;

import java.io.File;
import lib.PropertiesEditorTestCase;
import org.netbeans.jellytools.*;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author  Petr Felenda - QA Engineer (petr.felenda@sun.com)
 */
public class CreatingPropertiesFileFromMainWindow2Test extends PropertiesEditorTestCase {

    /*
     * Definition of member variables and objects
     */
    final String FILE_NAME = "testPropertiesFile";
    public String PROJECT_NAME = "properties_test2";

    /**
     * Constructor - Creates a new instance of this class
     */
    public CreatingPropertiesFileFromMainWindow2Test(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CreatingPropertiesFileFromMainWindow2Test.class).addTest("testCreatingPropertiesFileFromMainWindow2").enableModules(".*").clusters(".*"));
    }

    /**
     * This method contain body of test
     * @return void
     */
    public void testCreatingPropertiesFileFromMainWindow2() {

        // open project
        openProject(PROJECT_NAME);
        //openDefaultProject();


        /*
         * 1st step of testcase ( here is used toolbar's icon for opening wizard )
         * There will be opened New Wizard from Main Window Toolbar ( icon 'New File...' from toolbar 'System' )
         */
        MainWindowOperator mainWindowOp = MainWindowOperator.getDefault();
        mainWindowOp.getToolbarButton(mainWindowOp.getToolbar("File"), "New File...").pushNoBlock();


        /*
         * 2nd step of testcase
         * Select from wizard Other|Properties File and click next button.
         */
        NewFileWizardOperator nwo = new NewFileWizardOperator();
        nwo.selectProject(PROJECT_NAME);
        nwo.selectCategory(WIZARD_CATEGORY_FILE);
        nwo.selectFileType(WIZARD_FILE_TYPE);
        nwo.next();

        /*
         * 3rd step of testcase
         * Type name and select directory.
         */
        NewJavaFileNameLocationStepOperator nfnlsp = new NewJavaFileNameLocationStepOperator();
        nfnlsp.setObjectName(FILE_NAME);
        JTextFieldOperator jtfo = new JTextFieldOperator(nfnlsp, 2);
        jtfo.setText("src" + File.separator + "examples");



        /*
         * 4th step of testcase
         * Confirm wizard
         */
        nfnlsp.finish();


        /*
         *  Result
         * Should be added new properties file to adequate place in explorer and opened in editor
         */
        if (!existsFileInEditor(FILE_NAME)) {
            fail("File " + FILE_NAME + " not found in Editor window");
        }
        if (!existsFileInExplorer("examples", FILE_NAME)) {
            fail("File " + FILE_NAME + " not found in explorer");
        }


    }

    public void tearDown() {
        log("Teardown");
        closeOpenedProjects();
        //closePropertiesFile(FILE_NAME); 
    }
}
