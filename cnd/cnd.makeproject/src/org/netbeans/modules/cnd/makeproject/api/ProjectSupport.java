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

package org.netbeans.modules.cnd.makeproject.api;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.makeproject.MakeActionProviderImpl;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;

public class ProjectSupport {
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    private ProjectSupport() {
    }

    public static boolean saveAllProjects(String extraMessage) {
	boolean ok = true;
	Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
	for (int i = 0; i < openProjects.length; i++) {
	    MakeConfigurationDescriptor projectDescriptor = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(openProjects[i]);
	    if (projectDescriptor != null) {
                ok = ok && projectDescriptor.save(extraMessage);
            }
	}
	return ok;
    }

    public static Date lastModified(Project project) {
	FileObject projectFile = null;
	try {
            char separator = FileSystemProvider.getFileSeparatorChar(project.getProjectDirectory().getFileSystem());
	    projectFile = project.getProjectDirectory().getFileObject(MakeConfiguration.NBPROJECT_FOLDER + separator + MakeConfiguration.MAKEFILE_IMPL); // NOI18N
	}
	catch (Exception e) {
	    // happens if project is not a MakeProject
	}
	if (projectFile == null) {
            projectFile = project.getProjectDirectory();
        }
	return projectFile.lastModified();
    }

    public static void executeCustomAction(Project project, ProjectActionHandler customProjectActionHandler) {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        if (pdp == null) {
            return;
        }
        MakeConfigurationDescriptor projectDescriptor = pdp.getConfigurationDescriptor();
        MakeConfiguration conf = projectDescriptor.getActiveConfiguration();
        if (conf == null) {
            return;
        }

        MakeActionProviderImpl ap = project.getLookup().lookup(MakeActionProviderImpl.class );
        if (ap == null) {
            return;
        }

        ap.invokeCustomAction(projectDescriptor, conf, customProjectActionHandler);
    }

    public static MakeProjectOptions.PathMode getPathMode(Project project) {
        return MakeProjectOptions.getPathMode();
    }

    public static String toProperPath(FileObject base, FileObject path, Project project) {
        return toProperPath(base, path, getPathMode(project));
    }

    public static String toProperPath(FileObject base, String path, Project project) {
        return toProperPath(base, path, getPathMode(project));
    }

    public static String toProperPath(FSPath base, String path, Project project) {
        return toProperPath(base, path, getPathMode(project));
    }
    
    public static String toProperPath(FileObject base, FileObject path, MakeProjectOptions.PathMode pathMode) {
        switch (pathMode) {
            case REL_OR_ABS:
                return CndPathUtilities.toAbsoluteOrRelativePath(base, path);
            case REL:
                return CndPathUtilities.toRelativePath(base, path);
            case ABS:
                try {
                    return CndFileUtils.getCanonicalPath(path);
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                    return path.getPath();
                }
            default:
                throw new IllegalStateException("Unexpected path mode: " + pathMode); //NOI18N
        }
    }

    public static String toProperPath(FileObject base, String path, MakeProjectOptions.PathMode pathMode) {
        switch (pathMode) {
            case REL_OR_ABS:
                return CndPathUtilities.toAbsoluteOrRelativePath(base, path);
            case REL:
                return CndPathUtilities.toRelativePath(base, path);
            case ABS:
                return CndPathUtilities.toAbsolutePath(base, path);
            default:
                throw new IllegalStateException("Unexpected path mode: " + pathMode); //NOI18N
        }
    }

    public static String toProperPath(FSPath base, String path, MakeProjectOptions.PathMode pathMode) {
        switch (pathMode) {
            case REL_OR_ABS:
                return CndPathUtilities.toAbsoluteOrRelativePath(base.getPath(), path);
            case REL:
                return CndPathUtilities.toRelativePath(base.getPath(), path);
            case ABS:
                return CndPathUtilities.toAbsolutePath(base, path);
            default:
                throw new IllegalStateException("Unexpected path mode: " + pathMode); //NOI18N
        }
    }

    public static String convertWorkingDirToRemoteIfNeeded(ProjectActionEvent pae, String localDir) {
        ExecutionEnvironment execEnv = pae.getConfiguration().getDevelopmentHost().getExecutionEnvironment();
        if (!checkConnection(execEnv)) {
            return null;
        }
        if (execEnv.isRemote()) {
            RemoteSyncFactory remoteSyncFactory = pae.getConfiguration().getRemoteSyncFactory();
            PathMap mapper = remoteSyncFactory.getPathMap(execEnv);
            if (mapper != null) {
                String aLocalDir = mapper.getRemotePath(localDir, false);
                if (aLocalDir != null) {
                    localDir = aLocalDir;
                }
            } else {
                LOGGER.log(Level.SEVERE, "Path Mapper not found for project {0} - using local path {1}", new Object[]{pae.getProject(), localDir}); //NOI18N
            }
            return localDir;
        }
        return localDir;
    }

    public static boolean checkConnection(ExecutionEnvironment execEnv) {
        if (execEnv.isRemote()) {
            try {
                ConnectionManager.getInstance().connectTo(execEnv);
                ServerRecord record = ServerList.get(execEnv);
                if (record.isOffline()) {
                    record.validate(true);
                }
                return record.isOnline();
            } catch (IOException ex) {
                return false;
            } catch (CancellationException ex) {
                return false;
            }
        } else {
            return true;
        }
    }

}
