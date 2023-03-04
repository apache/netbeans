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
package org.netbeans.modules.maven.newproject;

import java.io.IOException;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach
 */
public class ArchetypeWizardUtilsTest extends NbTestCase {
   
    public ArchetypeWizardUtilsTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(F.class);
    }
    
    
    
    public void testFindsAllDirectories() throws IOException {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileUtil.createData(fs.getRoot(), "MyPrj/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/a/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/a/b/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/a/b/c/pom.xml");
        FileObject root = fs.findResource("MyPrj");
        
        Set<FileObject> res = ArchetypeWizardUtils.openProjects(root, null);
        
        assertEquals("Four projects found: " + res, 4, res.size());
    }
    
    public void testOpeningOfProjectsSkipsTargetDirectory() throws IOException {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileUtil.createData(fs.getRoot(), "MyPrj/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/a/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/b/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/c/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/target/x/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/target/y/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/target/z/pom.xml");
        FileObject root = fs.findResource("MyPrj");
        
       Set<FileObject> res = ArchetypeWizardUtils.openProjects(root, null);
        
        assertEquals("Four projects found: " + res, 4, res.size());
    }
    
    public static class F implements ProjectFactory {
        @Override
        public boolean isProject(FileObject projectDirectory) {
            return projectDirectory.getFileObject("pom.xml") != null;
        }

        @Override
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            throw new IOException();
        }

        @Override
        public void saveProject(Project project) throws IOException, ClassCastException {
            throw new IOException();
        }
    }
}
