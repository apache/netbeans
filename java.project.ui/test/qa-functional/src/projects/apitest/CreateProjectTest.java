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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006s Sun
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
