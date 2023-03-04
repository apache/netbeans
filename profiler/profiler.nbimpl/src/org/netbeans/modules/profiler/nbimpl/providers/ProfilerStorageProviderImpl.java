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
package org.netbeans.modules.profiler.nbimpl.providers;

import java.io.IOException;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.profiler.spi.ProfilerStorageProvider;
import org.netbeans.modules.profiler.utils.IDEUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider(service=ProfilerStorageProvider.class)
public final class ProfilerStorageProviderImpl extends ProfilerStorageProvider.Abstract {
    
    private static final String PROFILER_FOLDER = "NBProfiler/Config";  // NOI18N
    private static final String SETTINGS_FOLDER = "Settings";   // NOI18N
    private static final String SETTINGS_FOR_ATTR = "settingsFor"; // NOI18N

    @Override
    public FileObject getGlobalFolder(boolean create) throws IOException {
        FileObject folder = FileUtil.getConfigFile(PROFILER_FOLDER);
        FileObject settingsFolder = folder.getFileObject(SETTINGS_FOLDER, null);

        if ((settingsFolder == null) && create)
            settingsFolder = folder.createFolder(SETTINGS_FOLDER);

        return settingsFolder;
    }

    @Override
    public FileObject getProjectFolder(Lookup.Provider project, boolean create) throws IOException {
        Project p = (Project)project;
        FileObject nbproject = p.getProjectDirectory().getFileObject("nbproject"); // NOI18N
        FileObject d;
        if (nbproject != null) {
            // For compatibility, continue to use nbproject/private/profiler for Ant-based projects.
            d = create ? FileUtil.createFolder(nbproject, "private/profiler") : nbproject.getFileObject("private/profiler"); // NOI18N
        } else {
            // Maven projects, autoprojects, etc.
            d = ProjectUtils.getCacheDirectory(p, IDEUtils.class);
        }
        if (d != null) d.setAttribute(SETTINGS_FOR_ATTR, p.getProjectDirectory().toURL());
        return d;
    }

    @Override
    public Lookup.Provider getProjectFromFolder(FileObject settingsFolder) {
        Object o = settingsFolder.getAttribute(SETTINGS_FOR_ATTR);
        if (o instanceof URL) {
            FileObject d = URLMapper.findFileObject((URL) o);
            if (d != null && d.isFolder()) {
                try {
                    return ProjectManager.getDefault().findProject(d);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        Project p = FileOwnerQuery.getOwner(settingsFolder);
        try {
            if (p != null && getProjectFolder(p, false) == settingsFolder) return p;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
}
