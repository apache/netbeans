/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven.refactoring.dependency;

import java.io.InputStream;
import java.io.OutputStream;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class MavenDependencyModifierImplTestBase extends NbTestCase {

    public MavenDependencyModifierImplTestBase(String name) {
        super(name);
    }
    
    protected FileObject projectDir;
    protected Project project;

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        clearWorkDir();
    }
    
    
    
    protected Project makeProject(String subdir, String alternateBuildscript) throws Exception {
        FileObject src = FileUtil.toFileObject(getDataDir()).getFileObject(subdir);
        FileObject dest = FileUtil.toFileObject(getWorkDir());
        dest.getFileSystem().runAtomicAction(() -> {
            projectDir = FileUtil.copyFile(src, dest, src.getNameExt());
            if (alternateBuildscript != null) {
                FileObject fo = src.getFileObject(alternateBuildscript);
                FileObject target = projectDir.getFileObject("pom.xml");
                try (InputStream is = fo.getInputStream(); OutputStream os = target.getOutputStream()) {
                    FileUtil.copy(is, os);
                }
                /*
                if (target != null) {
                    target.delete();
                }
                fo.copy(projectDir, "pom", "xml");
                */
            }
        });
        
        Project p = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(p);
        
        OpenProjects.getDefault().open(new Project[] { p }, true);
        OpenProjects.getDefault().openProjects().get();
        
        p.getLookup().lookup(NbMavenProjectImpl.class).fireProjectReload().waitFinished();

        this.project = p;
        return p;
    }
}
