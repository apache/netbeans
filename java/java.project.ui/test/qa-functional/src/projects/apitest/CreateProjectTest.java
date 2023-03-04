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

package projects.apitest;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;

public class CreateProjectTest extends JellyTestCase {
    
    public CreateProjectTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CreateProjectTest.class).
                addTest("testCreateAndOpenProject_API_1", 
                        "testReopenAndCloseProject_API_1",
                        "testCreateAndOpenProject_API_2",
                        "testCloseProject_API_2",
                        "testReopenAndCloseProject_API_2").
                enableModules(".*").clusters(".*"));
    }
  
    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######"); // NOI18N
    }
    
    public void testCreateAndOpenProject_API_1() throws Exception {
        String projName = "testCreateAndOpenProject_API_1";
        String mainClass = "MyMain" + projName; // NOI18N
        File projectDir = new File(getWorkDir(), projName);
        projectDir.mkdir();
        J2SEProjectGenerator.createProject(projectDir, projName, mainClass, null, null, true);
        assertNotNull(Utilities.openProject(projectDir));
    }
    
//    public void testCloseProject_API_1() throws Exception {
//        assertTrue(Utilities.closeProject(projName1));
//    }
//    
   
     public void testReopenAndCloseProject_API_1() throws Exception {
        String projName = "testCreateAndOpenProject_API_1";
        String mainClass = "MyMain" + projName; // NOI18N
        File projectDir = new File(getWorkDir(), projName);
        projectDir.mkdir();
        J2SEProjectGenerator.createProject(projectDir, projName, mainClass, null, null, true);
        Utilities.openProject(projectDir);
        assertNotNull(Utilities.closeProject(projName));
    }
     
//     public void testReopenAndDeleteProjectFolder_API_1() throws Exception {
//        String mainClass = "MyMain" + projName1; // NOI18N
//        File projectDir = new File(getWorkDir(), projName1);
//        projectDir.mkdir();
//        AntProjectHelper project = org.netbeans.modules.java.j2seproject.J2SEProjectGenerator.createProject(projectDir, projName1, mainClass, null);
//        Utilities.waitScanFinished();
//        Utilities.openProject(projectDir);
//        assertTrue(Utilities.deleteProjectFolder(project.getProjectDirectory().getPath()));
//    }
 
     
    public void testCreateAndOpenProject_API_2() throws Exception {
        File  projectDir = createProject("testCreateAndOpenProject_API_2");
        assertNotNull(Utilities.openProject(projectDir));
    }
  
    public File createProject(String prjName) throws IOException {
        File projectDir = new File(getWorkDir(), prjName);
        projectDir.mkdir();
        
        File[] sourceFolders = new File[2];
        File src1 = new File(projectDir, "src1");
        src1.mkdirs();
        File src2 = new File(projectDir, "src2");
        src2.mkdirs();
        sourceFolders[0] = src1;
        sourceFolders[1] = src2;
        
        File[] testFolders = new File[2];
        File test1 = new File(projectDir, "test1");
        test1.mkdirs();
        File test2 = new File(projectDir, "test2");
        test2.mkdirs();
        testFolders[0] = test1;
        testFolders[1] = test2;
        J2SEProjectGenerator.createProject(projectDir,prjName, sourceFolders, testFolders, null, null, null);
        return projectDir;
    }
    public void testCloseProject_API_2() throws Exception {
        String prjName = "testCloseProject_API_2";
        File f = createProject(prjName);
        assertTrue("File is folder",f.isDirectory());
        Utilities.openProject(f);
        new org.netbeans.jemmy.EventTool().waitNoEvent(3000);
        assertTrue(Utilities.closeProject(prjName));
    }

    public void testReopenAndCloseProject_API_2() throws Exception {
        String prjName =  "testReopenAndCloseProject_API_2";
        File projectDir = new File(getWorkDir(), prjName);
        projectDir.mkdir();
        
        File[] sourceFolders = new File[2];
        File src1 = new File(projectDir, "src1");
        src1.mkdirs();
        File src2 = new File(projectDir, "src2");
        src2.mkdirs();
        sourceFolders[0] = src1;
        sourceFolders[1] = src2;
        
        File[] testFolders = new File[2];
        File test1 = new File(projectDir, "test1");
        test1.mkdirs();
        File test2 = new File(projectDir, "test2");
        test2.mkdirs();
        testFolders[0] = test1;
        testFolders[1] = test2;
        
        J2SEProjectGenerator.createProject(projectDir, prjName, sourceFolders, testFolders, null, null, null);
        Utilities.openProject(projectDir);
        new org.netbeans.jemmy.EventTool().waitNoEvent(3000);
        assertTrue(Utilities.closeProject(prjName));

    }
//    public void testReopenAndDeleteProjectFolder_API_2() throws Exception {
//        File projectDir = new File(getWorkDir(), projName2);
//        projectDir.mkdir();
//        
//        File[] sourceFolders = new File[2];
//        File src1 = new File(projectDir, "src1");
//        src1.mkdirs();
//        File src2 = new File(projectDir, "src2");
//        src2.mkdirs();
//        sourceFolders[0] = src1;
//        sourceFolders[1] = src2;
//        
//        File[] testFolders = new File[2];
//        File test1 = new File(projectDir, "test1");
//        test1.mkdirs();
//        File test2 = new File(projectDir, "test2");
//        test2.mkdirs();
//        testFolders[0] = test1;
//        testFolders[1] = test2;
//        
//        AntProjectHelper project = org.netbeans.modules.java.j2seproject.J2SEProjectGenerator.createProject(projectDir, projName2, sourceFolders, testFolders, null);
//        Utilities.waitScanFinished();
//        Utilities.openProject(projectDir);
//        assertTrue(Utilities.deleteProjectFolder(project.getProjectDirectory().getPath()));
//    }
}
