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

package org.netbeans.modules.cnd.makeproject.api.support;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.makeproject.FullRemoteExtension;
import org.netbeans.modules.cnd.makeproject.MakeBasedProjectFactorySingleton;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Utility class for misc make project related functions
 */
public class MakeProjectUtils {

    private MakeProjectUtils() {
    }

    public static ExecutionEnvironment getSourceFileSystemHost(Project project) {
        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        if (project != null) {
            RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
            if (remoteProject != null) {
                env = remoteProject.getSourceFileSystemHost();
            }
        }
        return env;
    }
    
    @Deprecated
    public static boolean canChangeHost(Project project, MakeConfiguration mk) {
        return isFullRemote(project) ? FullRemoteExtension.canChangeHost(mk) : true;
    }

    public static boolean canChangeHost(Project project) {
        return ! isFullRemote(project);
    }

    private static boolean isFullRemote(Project project) {
        if (project != null) {
            FileObject dir = project.getProjectDirectory();
            if (dir != null) { // paranoia
                ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(dir);
                if (env != null && env.isRemote()) {
                    return true;
                }
            }            
        }
        return false;
    }
    
    public static String getDiskFolderId(Project project, Folder folder) {
        String name;
        Set<String> names = new HashSet<>();
        for(Folder f : folder.getFolders()) {
            if (f.isDiskFolder()) {
                names.add(f.getName());
            }
        }
        int i = 0;
        while(true) {
            name = Integer.toString(i);
            if (!names.contains(name)) {
                break;
            }
            i++;
        }
        return name;
    }
    
    public static void forgetDeadProjectIfNeed(FileObject projectDirFO) {
        FileObject nbProjFO = projectDirFO.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        if (nbProjFO != null && nbProjFO.isValid()) {
            return;
        }
        try {
            Project prj = ProjectManager.getDefault().findProject(projectDirFO);
            if (prj != null) {
                MakeProjectHelper h = MakeBasedProjectFactorySingleton.getHelperFor(prj);
                if (h != null) {
                    h.notifyDeleted();
                }
            }                        
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
