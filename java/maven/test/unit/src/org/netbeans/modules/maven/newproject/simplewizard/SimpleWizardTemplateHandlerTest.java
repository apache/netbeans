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
package org.netbeans.modules.maven.newproject.simplewizard;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class SimpleWizardTemplateHandlerTest extends NbTestCase {

    public SimpleWizardTemplateHandlerTest(String name) {
        super(name);
    }
    
    FileObject whereTo;
    
    private FileBuilder createProject() throws IOException {
        clearWorkDir();
        whereTo = FileUtil.createFolder(FileUtil.toFileObject(getWorkDir()), "whereTo");
        
        FileObject template = FileUtil.getConfigFile("Templates/Project/Maven2/JavaApp");
        FileBuilder bld = new FileBuilder(template, whereTo);
        bld.name("TestProject");
        bld.defaultMode(FileBuilder.Mode.COPY);

        bld.param("artifactId", "netbeans-test-project");
        bld.param("groupId", "org.netbeans.modules.maven");
        // leave out version
        bld.param("package", "org.netbeans.modules.maven.testproject");
        return bld;
    }
    
    private void checkProjectArtifact(String artId, String groupId, String version) throws IOException {
        FileObject projectDir = whereTo.getFileObject("TestProject");
        Project p = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(p);
        NbMavenProject nbmp = p.getLookup().lookup(NbMavenProject.class);
        assertNotNull(nbmp);
        
        MavenProject mp = nbmp.getMavenProject();
        assertEquals(artId, mp.getArtifactId());
        assertEquals(groupId, mp.getGroupId());
        assertEquals(version, mp.getVersion());
    }
    
    /**
     * Checks simple project creation: file with name derived from artifactId created in the
     * correct package.
     * @throws Exception 
     */
    public void testCreateSimpleInPackage() throws Exception {
        
        List<FileObject> files = createProject().build();
        assertFalse(files.isEmpty());
        
        FileObject projectDir = whereTo.getFileObject("TestProject");
        assertNotNull(projectDir);
        
        // ensure the first reported file is the project directory
        assertTrue(files.get(0).isFolder());
        assertNotNull(files.get(0).getFileObject("pom.xml") != null);
        
        Optional<FileObject> optF = files.stream().filter(f -> f.isData()).findFirst();
        assertTrue(optF.isPresent());
        
        FileObject src = optF.get();
        assertEquals("NetbeansTestProject.java", src.getNameExt());
        assertTrue(src.getPath().contains("org/netbeans/modules/maven/testproject"));
        
        checkProjectArtifact("netbeans-test-project", "org.netbeans.modules.maven", "1.0-SNAPSHOT");

        String content = src.asText();
        assertTrue(content.contains("package org.netbeans.modules.maven.testproject;"));
        assertTrue(content.contains("class NetbeansTestProject {"));
    }
    
    /**
     * Specifies an explicit Main Class name, should be among the created files.
     * @throws Exception 
     */
    public void testExplicitMainClass() throws Exception {
        FileBuilder b = createProject();
        b.param("mainClassName", "CustomAppName");
        List<FileObject> files = b.build();
        Optional<FileObject> optF = files.stream().filter(f -> f.isData()).findFirst();
        assertTrue(optF.isPresent());
        
        FileObject src = optF.get();
        assertEquals("CustomAppName.java", src.getNameExt());
    }
    
    /**
     * Checks that groupID is derived from the package name
     */
    public void testDeriveGroupId() throws Exception {
        FileBuilder b = createProject();
        b.param("artifactId", "testproject");
        b.param("groupId", null);
        b.param("package", "org.netbeans.modules.maven.testproject");
        
        List<FileObject> files = b.build();
        Optional<FileObject> optF = files.stream().filter(f -> f.isData()).findFirst();
        assertTrue(optF.isPresent());
        
        FileObject src = optF.get();
        assertTrue(src.getPath().contains("org/netbeans/modules/maven/testproject"));
        
        // check POM file contents
        checkProjectArtifact("testproject", "org.netbeans.modules.maven", "1.0-SNAPSHOT");
        
        String content = src.asText();
        assertTrue(content.contains("package org.netbeans.modules.maven.testproject;"));
        assertTrue(content.contains("class Testproject {"));
    }
    
    /**
     * With no package the handler cannot derive groupId and should fail.
     * @throws Exception 
     */
    public void testFailsWithoutGroupAndPackage() throws Exception {
        FileBuilder b = createProject();
        b.param("artifactId", "testproject");
        b.param("groupId", null);
        b.param("package", null);
        
        try  {
            List<FileObject> files = b.build();
            fail("Exception expected");
        } catch (IOException ex) {
            assertTrue(ex.getLocalizedMessage().contains("groupId"));
        }
    }

    /**
     * Files should be created in the default package.
     * @throws Exception 
     */
    public void testUseDefaultPackage() throws Exception {
        FileBuilder b = createProject();
        b.param("artifactId", "testproject");
        b.param("package", null);
        
        List<FileObject> files = b.build();
        assertFalse(files.isEmpty());
    }
}
