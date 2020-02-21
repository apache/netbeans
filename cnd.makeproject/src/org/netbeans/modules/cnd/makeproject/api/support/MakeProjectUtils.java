/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
