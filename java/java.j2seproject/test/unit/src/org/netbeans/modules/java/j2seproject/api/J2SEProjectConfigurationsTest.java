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

package org.netbeans.modules.java.j2seproject.api;

import java.io.File;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.test.MockLookup;

/**
 * Test of class org.netbeans.modules.java.j2seproject.api.J2SEProjectConfigurations
 * 
 * @author Milan Kubec
 */
public class J2SEProjectConfigurationsTest extends NbTestCase {
    
    public J2SEProjectConfigurationsTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of createConfigurationFiles method
     */
    public void testCreateConfigurationFiles() throws Exception {
        
        System.out.println("createConfigurationFiles");
        
        File proj = getWorkDir();
        clearWorkDir();
        MockLookup.setLayersAndInstances();
        
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));
        AntProjectHelper aph = J2SEProjectGenerator.createProject(proj, "TestProject", null, "manifest.mf", null, false);
        
        Project prj = ProjectManager.getDefault().findProject(aph.getProjectDirectory());
        
        String configName = "TestConfig";
        
        Properties sharedProps = new Properties();
        sharedProps.put("sharedPropName", "sharedPropValue");
        sharedProps.put("$sharedPropNameSpecial", "sharedPropValueSpecial");
        sharedProps.put("sharedPropName2", "${sharedPropName}");
        
        Properties privateProps = new Properties();
        privateProps.put("privatePropName", "privatePropValue");
        privateProps.put("privatePropName2", "${privatePropName}");
        
        J2SEProjectConfigurations.createConfigurationFiles(prj, configName, sharedProps, privateProps);
        
        FileObject prjDirFO = prj.getProjectDirectory();
        
        FileObject sharedPropsFO = prjDirFO.getFileObject("nbproject/configs/" + configName + ".properties");
        Properties loadedSharedProps = new Properties();
        loadedSharedProps.load(sharedPropsFO.getInputStream());
        assertEquals(sharedProps, loadedSharedProps);
        
        FileObject privatePropsFO = prjDirFO.getFileObject("nbproject/private/configs/" + configName + ".properties");
        Properties loadedPrivateProps = new Properties();
        loadedPrivateProps.load(privatePropsFO.getInputStream());
        assertEquals(privateProps, loadedPrivateProps);
        
        configName = "Test_Config2";
        
        EditableProperties sharedProps2 = new EditableProperties(true);
        sharedProps2.put("sharedPropName", "sharedPropValue");
        sharedProps2.put("$sharedPropNameSpecial", "sharedPropValueSpecial");
        sharedProps2.put("sharedPropName2", "${sharedPropName}");
        
        J2SEProjectConfigurations.createConfigurationFiles(prj, configName, sharedProps2, null);
        
        sharedPropsFO = prjDirFO.getFileObject("nbproject/configs/" + configName + ".properties");
        loadedSharedProps = new Properties();
        loadedSharedProps.load(sharedPropsFO.getInputStream());
        assertEquals(sharedProps2, loadedSharedProps);
        
        privatePropsFO = prjDirFO.getFileObject("nbproject/private/configs/" + configName + ".properties");
        assertNull(privatePropsFO);
        
    }
    
}
