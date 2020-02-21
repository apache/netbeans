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

package org.netbeans.modules.cnd.remote.support;

import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem.ProjectItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.remote.sync.SharabilityFilter;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Misc project related utility functions
 */
public class RemoteProjectSupport {

    private RemoteProjectSupport() {
    }

    public static ExecutionEnvironment getExecutionEnvironment(Lookup.Provider project) {
        MakeConfiguration mk = ConfigurationSupport.getProjectActiveConfiguration(project);
        if (mk != null) {
            return mk.getDevelopmentHost().getExecutionEnvironment();
        }
        return null;
    }

    public static boolean projectExists(Lookup.Provider project) {
        RemoteProject rp = project.getLookup().lookup(RemoteProject.class);
        if (rp == null) {
            return false;
        }
        final FileObject projDirFO = rp.getSourceBaseDirFileObject();
        if(projDirFO != null && projDirFO.isValid()) {
            FileObject nbprojectFO = projDirFO.getFileObject("nbproject"); //NOI18N
            if (nbprojectFO != null && nbprojectFO.isValid()) {
                return true;
            }
        }
        return false;
    }

    public static FileObject getPrivateStorage(Lookup.Provider project) {
        if (project == null) {
            return null;
        }
        RemoteProject rp = project.getLookup().lookup(RemoteProject.class);
        if (rp == null) {
            return null;
        }
        FileObject baseDir = rp.getSourceBaseDirFileObject();
        if (baseDir == null) {
            return null;
        }
        try {
            return FileUtil.createFolder(baseDir, "nbproject/private"); //NOI18N
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.INFO, ex.getMessage(), ex); //NOI18N
            return null;
        }
    }

    public static List<FSPath> getBuildResults(Lookup.Provider project) {
        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        if (mcd != null) {
            MakeConfiguration conf = mcd.getActiveConfiguration();
            return getBuildResults(conf);
        }
        return Collections.<FSPath>emptyList();
    }

    public static List<FSPath> getBuildResults(MakeConfiguration conf) {
        String binaryPath = (conf == null) ? null : conf.getAbsoluteOutputValue();
        if (binaryPath != null) {
            return Collections.singletonList(new FSPath(conf.getFileSystem(), binaryPath));
        } else {
            return Collections.<FSPath>emptyList();
        }
    }
    
    public static List<FSPath> getProjectSourceDirs(Lookup.Provider project, AtomicReference<String> runDir) {
        MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project);
        RemoteProject rp = project.getLookup().lookup(RemoteProject.class);
        if (rp == null) {
            return Collections.<FSPath>emptyList();
        }
        FileObject baseDir = rp.getSourceBaseDirFileObject();
        if (baseDir == null){
            return Collections.<FSPath>emptyList();
        }
        if (conf == null) {
            return Collections.singletonList(FSPath.toFSPath(baseDir));
        } else {
            return getProjectSourceDirs(project, conf, runDir);
        }
    }

    public static List<FSPath> getProjectSourceDirs(Lookup.Provider project, MakeConfiguration conf, AtomicReference<String> runDir) {
        RemoteProject rp = project.getLookup().lookup(RemoteProject.class);
        if (rp == null) {
            return Collections.<FSPath>emptyList();
        }
        FileObject baseDir = rp.getSourceBaseDirFileObject();
        if (baseDir == null) {
            return Collections.<FSPath>emptyList();
        }
        if (conf == null) {
            return Collections.singletonList(FSPath.toFSPath(baseDir));
        }
        if (runDir != null) {
            String d = conf.getMakefileConfiguration().getBuildCommandWorkingDirValue();
            FileSystem fs = conf.getBaseFSPath().getFileSystem();
            if (!CndPathUtilities.isPathAbsolute(d)) {
                d = conf.getBaseFSPath().getPath() + FileSystemProvider.getFileSeparatorChar(fs) + d;
            }
            d = FileSystemProvider.normalizeAbsolutePath(d, fs);
            runDir.set(d);
        }
        // the project itself
        Set<FSPath> sourceFilesAndDirs = new HashSet<>();
        if (!conf.isMakefileConfiguration()) {
            sourceFilesAndDirs.add(FSPath.toFSPath(baseDir));
        }

        MakeConfigurationDescriptor mcs = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        if (mcs == null) {
            return Collections.<FSPath>emptyList();
        }
        if (!mcs.getBaseDirFileObject().isValid()) {
            // no disk files
            return new ArrayList<>(sourceFilesAndDirs);
        }
        for(String soorceRoot : mcs.getSourceRoots()) {
            // not sure whether they are absolute and normalized, so:
            FileObject fo = FileSystemProvider.getFileObject(baseDir, soorceRoot);
            if (fo != null) {
                sourceFilesAndDirs.add(FSPath.toFSPath(fo));
            }
        }
        addExtraFiles(mcs, sourceFilesAndDirs);
        List<Project> subProjects = new ArrayList<>(conf.getSubProjects());
        // required projects are different - see #194997
        for (ProjectItem requiredProject : conf.getRequiredProjectsConfiguration().getValue() ) {
            Project p = requiredProject.getProject(conf.getBaseFSPath());
            if (p != null) {
                subProjects.add(p);                
            }
        }        
        // Then go trough open subprojects and add their external source roots
        for (Project subProject : subProjects) {
            if (subProject.getProjectDirectory().isValid()) {
                sourceFilesAndDirs.add(FSPath.toFSPath(subProject.getProjectDirectory()));
                MakeConfigurationDescriptor subMcs =
                        MakeConfigurationDescriptor.getMakeConfigurationDescriptor(subProject);
                for(String soorceRoot : mcs.getSourceRoots()) {
                    FileObject fo = FileSystemProvider.getFileObject(subProject.getProjectDirectory(), soorceRoot);
                    if (fo != null) {
                        sourceFilesAndDirs.add(FSPath.toFSPath(fo));
                    }
                }
                if (subMcs != null) {
                    addExtraFiles(subMcs, sourceFilesAndDirs);
                }
            }
        }
        return new ArrayList<>(sourceFilesAndDirs);
    }

    private static void addExtraFiles(MakeConfigurationDescriptor subMcs, Set<FSPath> filesToSync) {
        addExtraFiles(subMcs, filesToSync, subMcs.getProjectItems());
        addExtraFiles(subMcs, filesToSync, subMcs.getExternalFileItemsAsArray());
    }
    
    private static void addExtraFiles(MakeConfigurationDescriptor subMcs, Set<FSPath> filesToSync, Item[] items) {
        SharabilityFilter filter = new SharabilityFilter();
        for (Item item : items) {
            FileObject fo = item.getFileObject();
            if (fo == null || !fo.isValid()) {
                // it won't be added while recursin into directories
                filesToSync.add(item.getFSPath());
            } else if (!filter.accept(fo)) {
                // !normFO.isValid() is for inexistent file - we won't get it via directories recursion
                // !filter.accept(normFO) means that we'll filter it out, but it's explicitely in project
                filesToSync.add(item.getFSPath());
            } else if (!isContained(item.getNormalizedPath(), filesToSync)) {
                // directory containing file is not yet added => copy it
                filesToSync.add(item.getFSPath());
            }
        }
    }

    private static boolean isContained(String itemAbsPath, Set<FSPath> paths) {
        for (FSPath dir : paths) {
            String alreadyAddedPath = dir.getPath();
            if (itemAbsPath.startsWith(alreadyAddedPath) && itemAbsPath.length() > alreadyAddedPath.length()) {
                char c = itemAbsPath.charAt(alreadyAddedPath.length());
                if (c == '\\' || c == '/') {
                    return true;
                }
            }
        }
        return false;
    }
}
