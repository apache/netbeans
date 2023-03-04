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

package org.netbeans.modules.maven;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * factory of maven projects
 * @author  Milos Kleint
 */
@ServiceProvider(service=ProjectFactory.class, position=666)
public class NbMavenProjectFactory implements ProjectFactory2 {
    
    private static final AtomicBoolean atLeastOneMavenProjectAround = new AtomicBoolean(false);
    
    /**
     * a simple way to tell if at least one maven project was loaded, to be used for
     * performance optimizations in global services.
     * @return 
     * @since
     */
    public static boolean isAtLeastOneMavenProjectAround() {
        return atLeastOneMavenProjectAround.get();
    }
    
    
    public @Override boolean isProject(FileObject fileObject) {
        File projectDir = FileUtil.toFile(fileObject); //guard that we only recognize projects on local filesystem. Maven won't be able to work with anything else.
        if (projectDir == null) {
            return false;
        }
        File project = new File(projectDir, "pom.xml"); // NOI18N
        if (!project.isFile()) {
            return false;
        }
        if (project.getAbsolutePath().contains("resources" + File.separator + "archetype-resources")) { //NOI18N
            //this is an archetype resource, happily ignore..
            return false;
        }
        String projectDirName = projectDir.getName();
        if (projectDirName.equals("nbproject")) {
            return false; // XXX why?
        }
        if (projectDirName.equals("target") && new File(projectDir.getParentFile(), "pom.xml").isFile()) {
            return false;
        }
        return true;
    }

    public @Override ProjectManager.Result isProject2(FileObject projectDirectory) {
        if (isProject(projectDirectory)) {
            return new ProjectManager.Result(
                    null, NbMavenProject.TYPE, 
                    ImageUtilities.loadImageIcon("org/netbeans/modules/maven/resources/Maven2Icon.gif", true)); //NOI18N
        }
        return null;
    }

    public @Override Project loadProject(FileObject fileObject, ProjectState projectState) throws IOException { 
        if (!isProject(fileObject)) {
            return null;
        }
        FileObject projectFile = fileObject.getFileObject("pom.xml"); //NOI18N
        if (projectFile == null || !projectFile.isData()) {
            return null;

        }
        atLeastOneMavenProjectAround.set(true);
        return new NbMavenProjectImpl(fileObject, projectFile, projectState);
    }
    
    public @Override void saveProject(Project project) throws IOException {
        // what to do here??
    }    
}
