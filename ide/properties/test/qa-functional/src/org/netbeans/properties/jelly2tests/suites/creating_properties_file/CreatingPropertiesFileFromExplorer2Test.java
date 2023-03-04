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
 * CreatingPropertiesFileFromExplorer2.java
 *
 * This is autometed test for netBeans version 40.
 *
 * Created on 19. brezen 2002, 11:07
 *
 */

package org.netbeans.properties.jelly2tests.suites.creating_properties_file;

import org.netbeans.jellytools.*;
import lib.PropertiesEditorTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;


/**
 *
 * @author  Petr Felenda - QA Engineer
 */
public class CreatingPropertiesFileFromExplorer2Test extends PropertiesEditorTestCase {

    /*
     * Definition of member variables and objects
     */
    final String PACKAGE_PATH = "examples";
    final String FILE_NAME = "testFileExplorer2";
    
    
    /**
     * Constructor - Creates a new instance of CreatingPropertiesFileFromExplorer2
     */
    public CreatingPropertiesFileFromExplorer2Test(String name) {
        super(name);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CreatingPropertiesFileFromExplorer2Test.class).addTest("testCreatingPropertiesFileFromExplorer2").enableModules(".*").clusters(".*"));
    }
    
    /**
     * This method contain body of test
     * @return void
     */
    public void testCreatingPropertiesFileFromExplorer2() {
        
        
        // open project
        openDefaultProject();
        
        /*
         * 1st step of testcase
         * In explorer create new properties file. Right click on any directory and
         * select in appeared context menu New|Other|Properties File.
         */
        Node node = new Node(new SourcePackagesNode(DEFAULT_PROJECT_NAME),PACKAGE_PATH);
        node.select();
        node.callPopup().pushMenuNoBlock("New"+menuSeparator+"Other...",menuSeparator);
        NewFileWizardOperator newWizard = new NewFileWizardOperator();
        newWizard.selectCategory(WIZARD_CATEGORY_FILE);
        newWizard.selectFileType(WIZARD_FILE_TYPE);
        newWizard.next();
        //type class name
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.setObjectName(FILE_NAME);
        //push finish
        newWizard.finish();
        
       
        /*
         * 2nd step of testcase
         * In explorer create new properties file. Right click on any directory and select
         * in appeared context menu New|Other|Properties File
         */
        node.select();
        node.callPopup().pushMenuNoBlock("New"+menuSeparator+"Other...",menuSeparator);
        newWizard = new NewFileWizardOperator();
        newWizard.selectCategory(WIZARD_CATEGORY_FILE);
        newWizard.selectFileType(WIZARD_FILE_TYPE);
        newWizard.next();
        
        /*
         * 3th step of testcase
         * Type name to appeared wizard.(as same name as previous case)
         */
        nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.setObjectName(FILE_NAME);
        
        
        /*
         * 4th step of testcase - Result
         * Try confirm wizard. 'Finish' button should be disabled.
         */
        if ( nfnlso.btFinish().isEnabled() == true )
            fail("Button finish is enabled and should be disabled.Because file with this name exist.");
        else
            log("Button is disabled. (Ok)");
        
        
        /*
         * 5th step of testcase
         * Cancel wizard. Click to 'Cancel' button.
         */
        nfnlso.btCancel().push();
        
    }
    
    public void tearDown() {
        log("Teardown");
        //closePropertiesFileWithoutCheck(FILE_NAME); 
        closeOpenedProjects();
    }
    
    
    
}
