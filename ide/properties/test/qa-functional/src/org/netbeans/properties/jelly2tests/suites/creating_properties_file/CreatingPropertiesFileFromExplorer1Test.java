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
 * File CreatingPropertiesFileFromExplorer1.java
 *
 * This is autometed test for netBeans version 40.
 *
 * Created on 16. September 2002
 *
 */

package org.netbeans.properties.jelly2tests.suites.creating_properties_file;

import org.netbeans.jellytools.*;
import lib.PropertiesEditorTestCase;
import org.netbeans.jellytools.nodes.Node;
import junit.framework.Test;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbModuleSuite;


/**
 *
 * @author  Petr Felenda - QA Engineer ( petr.felenda@sun.com )
 */
public class CreatingPropertiesFileFromExplorer1Test extends PropertiesEditorTestCase {
    
    /*
     * Definition of member variables and objects
     */
    final String FILE_NAME = "testFileExplorer1" ;
    final String PACKAGE_PATH = "samples";
    
    
    
    /**
     *  Constructor - creates a new instance of CreatingPropertiesFileFromExplorer1
     */
    public CreatingPropertiesFileFromExplorer1Test(String name) {
        super(name);
    }
    
     /** Creates suite from particular test cases. You can define order of testcases here. */
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CreatingPropertiesFileFromExplorer1Test.class).addTest("testCreatingPropertiesFileFromExplorer1").enableModules(".*").clusters(".*"));
    }
    
    /**
     * This method contain body of test
     * @return void
     */
    public void testCreatingPropertiesFileFromExplorer1() {
        
        
        // open project
        openDefaultProject();
        
        /*
         * 1st step of testcase
         * In explorer create new properties file. Right click on any directory and
         * select in appeared context menu New|Other|Properties File. 
         */
        log(PACKAGE_PATH);
        SourcePackagesNode spn = new SourcePackagesNode(DEFAULT_PROJECT_NAME);
        Node node = new Node(spn,PACKAGE_PATH);
        
        node.select();
        node.callPopup().pushMenuNoBlock("New"+menuSeparator+"Other...",menuSeparator);
        NewFileWizardOperator newWizard = new NewFileWizardOperator();
        newWizard.selectCategory(WIZARD_CATEGORY_FILE);
        newWizard.selectFileType(WIZARD_FILE_TYPE);
        newWizard.next();
        
        /*
         * 2nd step of testcase
         * Type name to appeared wizard.
         */
        NewJavaFileNameLocationStepOperator nameStepOper = new NewJavaFileNameLocationStepOperator();
        nameStepOper.setObjectName(FILE_NAME);
        
        
        /*
         * 3rd step of testcase
         * Confirm wizard. Press Finish button.
         */
        newWizard.finish();
        
        /*
         * Result
         * Should be created new file in explorer and opened in editor.
         */
        if ( ! existsFileInEditor(FILE_NAME) )
            fail("File "+ FILE_NAME +" not found in Editor window");
        if ( ! existsFileInExplorer("samples",FILE_NAME) )
            fail("File "+ FILE_NAME +" not found in explorer");
    }
    
    public void tearDown() {
        log("Teardown");
        closeOpenedProjects();
      //  closePropertiesFile(FILE_NAME); 
    }
    
    
    
    
}
