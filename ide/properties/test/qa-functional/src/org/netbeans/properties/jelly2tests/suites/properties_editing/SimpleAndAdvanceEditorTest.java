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

package org.netbeans.properties.jelly2tests.suites.properties_editing;

import lib.PropertiesEditorTestCase;
import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.PropertiesNode;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Admin
 */
public class SimpleAndAdvanceEditorTest extends PropertiesEditorTestCase{
    
    ProjectRootNode prn;
    ProjectsTabOperator pto;
    PropertiesNode pn;
    String bundleName = "Test";

    public SimpleAndAdvanceEditorTest(String name) {
        super(name);
    }
     
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(SimpleAndAdvanceEditorTest.class).addTest(
                "testCreateNewBundle",
                "testOpenningSimpleEditorFromFileExplorer",
                "testOpenningAdvanceEditorFromFileExplorer",
                "testCreateNewBundleInDefaultPackage",
                "testOpenningSimpleEditorFromProjectTab",
                "testOpenningAdvanceEditorFromProjectTab").enableModules(".*").clusters(".*"));
    }
    
    //This test create new bundle file in the root of the project
    public void testCreateNewBundle() throws Exception{
        //open default project
        openDefaultProject();
        
        //create new bundle in default package
        pto = new ProjectsTabOperator().invoke();
        prn = new ProjectRootNode(new JTreeOperator(pto), DEFAULT_PROJECT_NAME);
        createNewPropertiesFile(prn);
        
        //check that new properties file was created and is accessible via file tab
        boolean result = existsFileInFilesTab(WIZARD_DEFAULT_PROPERTIES_FILE_NAME);
        
        if (result) {
           log(WIZARD_DEFAULT_PROPERTIES_FILE_NAME + ".properties file was created and was found in File explorer");
        } else {
            throw new Exception(WIZARD_DEFAULT_PROPERTIES_FILE_NAME + ".properties file was not found in File explorer");
        }
        
        //close Simple editor
        closePropertiesFile(WIZARD_DEFAULT_PROPERTIES_FILE_NAME);
    }
    
    public void testOpenningSimpleEditorFromFileExplorer() throws Exception{
        pn=getNodeFilesTab(DEFAULT_PROJECT_NAME, WIZARD_DEFAULT_PROPERTIES_FILE_NAME+".properties");
        pn.edit();
        
        //Check if properties file was opened in simple editor
        boolean result=existsFileInEditor(WIZARD_DEFAULT_PROPERTIES_FILE_NAME);
        
        if (result) {
            log(WIZARD_DEFAULT_PROPERTIES_FILE_NAME + " was opened in simple editor");
        }else {
            throw new Exception(WIZARD_DEFAULT_PROPERTIES_FILE_NAME +  " was not opened in simple editor");
        }
    }
    
    public void testOpenningAdvanceEditorFromFileExplorer() throws Exception{
        pn=getNodeFilesTab(DEFAULT_PROJECT_NAME, WIZARD_DEFAULT_PROPERTIES_FILE_NAME+".properties");
        pn.open();
        
        //Check if properties file was opened in advance editor
        boolean result=existsFileInAdvanceEditor(WIZARD_DEFAULT_PROPERTIES_FILE_NAME);
        
        if (result) {
            log(WIZARD_DEFAULT_PROPERTIES_FILE_NAME + " was opened in advance editor");
        }else {
            throw new Exception(WIZARD_DEFAULT_PROPERTIES_FILE_NAME +  " was not opened in advance editor");
        }
    }
    
    public void testCreateNewBundleInDefaultPackage() throws Exception{
        pto = new ProjectsTabOperator().invoke();
        prn = pto.getProjectRootNode(DEFAULT_PROJECT_NAME);
        prn.select();
        
        //Create new properties file
        createNewPropertiesFile(prn, defaultPackageDir, bundleName);
        
        //Check if bundle was created and opened
        boolean result = existsFileInExplorer(defaultPackage, bundleName);
        
        if (result) {
            log("Properties file" + bundleName + ".properties was created in default package");
        } else {
            throw new Exception("Properties file" + bundleName + ".properties is not reachable in Project Tree");
        }
        
        //Check if bundle was automatically opened in simple editor
        result = closePropertiesFile(bundleName);
        
        if (result) {
            log("Properties file" + bundleName + ".properties was automatically opened in simple editor and finally closed");
        } else {
            throw new Exception("Properties file" + bundleName + ".properties was not automatically opened in simple editor while creating");
        }
 
    }
    
    public void testOpenningSimpleEditorFromProjectTab() throws Exception{
        pn = getNode(DEFAULT_PROJECT_NAME, rootPackageName + TREE_SEPARATOR + defaultPackage + TREE_SEPARATOR + bundleName + ".properties");
        pn.edit();
        
        //Check if bundle was opened in simple editor
        boolean result = closePropertiesFile(bundleName);
        
        if (result) {
            log("Properties file" + bundleName + ".properties was automatically opened in simple editor and finally closed");
        } else {
            throw new Exception("Properties file" + bundleName + ".properties was not automatically opened in simple editor while creating");
        }
    }
    
    public void testOpenningAdvanceEditorFromProjectTab() throws Exception{
        pn = getNode(DEFAULT_PROJECT_NAME, rootPackageName + TREE_SEPARATOR + defaultPackage + TREE_SEPARATOR + bundleName + ".properties");
        pn.open();
        
        //Check if properties file was opened in advance editor
        boolean result=existsFileInAdvanceEditor(bundleName);
        
        if (result) {
            log("Properties file" + bundleName + " was opened in advance editor");
        }else {
            throw new Exception("Properties file" + bundleName + " was not opened in advance editor");
        }
    }

}
