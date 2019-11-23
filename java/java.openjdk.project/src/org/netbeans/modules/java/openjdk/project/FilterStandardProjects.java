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
package org.netbeans.modules.java.openjdk.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.openjdk.common.BuildUtils;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=ProjectFactory.class, position=10)
public class FilterStandardProjects implements ProjectFactory {

    private static final boolean BLOCK_LANGTOOLS_PROJECT = Boolean.getBoolean("nb.jdk.project.block.langtools");
    
    @Override
    public boolean isProject(FileObject projectDirectory) {
        FileObject jdkRoot;
        return BuildUtils.getFileObject(projectDirectory, "nbproject/project.xml") != null &&
               (jdkRoot = BuildUtils.getFileObject(projectDirectory, "../../..")) != null &&
               (JDKProject.isJDKProject(jdkRoot) || BuildUtils.getFileObject(jdkRoot, "../modules.xml") != null) &&
               projectDirectory.getParent().equals(BuildUtils.getFileObject(jdkRoot, "make/netbeans")) &&
               "netbeans".equals(projectDirectory.getParent().getName());
    }

    public static final String MSG_FILTER = "This project cannot be load while the NetBeans JDK project is open.";
    
    @Override
    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        if (!isProject(projectDirectory)) return null;

        FileObject repository;
        String project2Repository;

        if ("langtools".equals(projectDirectory.getNameExt())) {
            if (!BLOCK_LANGTOOLS_PROJECT)
                return null;
            repository = BuildUtils.getFileObject(projectDirectory, "../../../../langtools");
            project2Repository = "../..";
        } else {
            repository = BuildUtils.getFileObject(projectDirectory, "../../..");
            project2Repository = "";
        }
        
        if (repository != null) {
            for (Project prj : OpenProjects.getDefault().getOpenProjects()) {
                if (repository.equals(BuildUtils.getFileObject(prj.getProjectDirectory(), project2Repository))) {
                    throw new IOException(MSG_FILTER);
                }
            }
        }

        return null;
    }

    @Override
    public void saveProject(Project project) throws IOException, ClassCastException {
    }

}
