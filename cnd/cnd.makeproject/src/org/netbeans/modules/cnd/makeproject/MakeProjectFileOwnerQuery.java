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

package org.netbeans.modules.cnd.makeproject;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.utils.CndVisibilityQuery;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Provider;

/**
 * FileOwnerQuery dealing with files that are not in the project directory.
 * A typical situation for CND project created from existing sources.
 *
 */
public class MakeProjectFileOwnerQuery implements FileOwnerQueryImplementation {
    private static final String PATH_SEPARATOR = "/"; //NOI18N
    private static final class Cache {
        private FileObject lastFO;
        private Project lastProject;

        public boolean getLastProject(FileObject fo, Project out[]) {
            if (fo == lastFO) {
                out[0] = lastProject;
                return true;
            }
            return false;
        }

        public void cacheQuery(FileObject fo, Project prj) {
            lastFO = fo;
            lastProject = prj;
        }
    }

    private final ThreadLocal<Reference<Cache>> cache = new ThreadLocal<Reference<Cache>>() {

        @Override
        protected Reference<Cache> initialValue() {
            return new WeakReference<>(new Cache());
        }
    };

    @Override
    public Project getOwner(URI uri) {
        return getOwner(toFileObject(uri));
    }
    
    private FileObject toFileObject(URI uri) {
        try {
            URL url =  uri.toURL();
            return URLMapper.findFileObject(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public Project getOwner(FileObject fo) {
        if (fo == null) {
            return null;
        }
        Reference<Cache> ref = cache.get();
        Cache cachedValue = ref.get();
        Project out[] = new Project[] { null };
        if (cachedValue != null && cachedValue.getLastProject(fo, out)) {
            return out[0];
        } else {
            cachedValue = new Cache();
            cache.set(new WeakReference<>(cachedValue));
        }
        FileSystem fs;
        try {
            fs = fo.getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }        
        String path = CndPathUtilities.normalizeSlashes(fo.getPath());

        final List<MakeConfigurationDescriptor> descr = new ArrayList<>(10);

        for (NativeProject nativeProject : NativeProjectRegistry.getDefault().getOpenProjects()) {
            Provider project = nativeProject.getProject();
            if (project instanceof Project) {
                if (!fs.equals(RemoteFileUtil.getProjectSourceFileSystem((Project) project))) {
                    continue;
                }
                ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
                if (provider != null && provider.gotDescriptor()) {
                    MakeConfigurationDescriptor descriptor = provider.getConfigurationDescriptor();
                    if (descriptor != null) {
                        if (fo.equals(descriptor.getProjectDirFileObject())) {
                            cachedValue.cacheQuery(fo, (Project)project);
                            return (Project) project;
                        }
                        descr.add(descriptor);
                    }
                }
            }
        }

        Project project;
        for (MakeConfigurationDescriptor descriptor : descr) {
            project = descriptor.getProject();
            boolean mine = false;
            if (fo.isData()) {
                mine = descriptor.findProjectItemByPath(path) != null || descriptor.findExternalItemByPath(path) != null;
            } else if (fo.isFolder()) {
                mine = descriptor.findFolderByPath(path) != null;
            }
            if (!mine) {
                // usually all files are registered in project and are detected by find*Path,
                // but new added files or smth. created from template might be not yet registered
                // To recognize such files check them by source/test roots
                if (isMine(descriptor.getAbsoluteSourceRoots(), fo, path)) {
                    mine = true;
                } else if (isMine(descriptor.getAbsoluteTestRoots(), fo, path)) {
                    mine = true;
                }
                CndVisibilityQuery folderVisibilityQuery = descriptor.getFolderVisibilityQuery();
                if (mine && folderVisibilityQuery != null) {
                    // make sure it is not path from Project's user-ignored folders
                    FileObject toCheck;
                    if (fo.isFolder()) {
                        toCheck = fo;
                    } else {
                        toCheck = fo.getParent();
                    }
                    while (toCheck != null && !toCheck.isRoot()) {
                        if (folderVisibilityQuery.isIgnored(toCheck)) {
                            mine = false;
                            break;
                        }
                        toCheck = toCheck.getParent();
                    }
                }
            }
            if (mine) {
                cachedValue.cacheQuery(fo, (Project) project);
                return (Project) project;
            }
        }
        // either do not cache failed result or implement listening about opened projects
        if (false) cachedValue.cacheQuery(fo, null);
        return null;
    }
    
    private boolean isMine(List<String> list, FileObject fo, String path) {
        if (!list.isEmpty()) {
            if (fo.isFolder()) {
                if (!path.endsWith(PATH_SEPARATOR)) {
                    path = path + PATH_SEPARATOR;
                }
            }
            for (String srcPath : list) {
                srcPath = CndPathUtilities.normalizeSlashes(srcPath);
                if (!srcPath.endsWith(PATH_SEPARATOR)) {
                    srcPath = srcPath + PATH_SEPARATOR;
                }
                if (path.startsWith(srcPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    // see bug #240182
    @org.openide.util.lookup.ServiceProvider(service = org.netbeans.spi.project.FileOwnerQueryImplementation.class, position = 98)
    public static class HighPriorityProvider extends MakeProjectFileOwnerQuery {

        @Override
        public Project getOwner(FileObject fo) {
            if (fo == null || !MIMENames.isCndMimeType(fo.getMIMEType())) {
                return null;
            }
            return super.getOwner(fo);
        }

    }

    @org.openide.util.lookup.ServiceProvider(service = org.netbeans.spi.project.FileOwnerQueryImplementation.class, position = 102)
    public static class LowPriorityProvider extends MakeProjectFileOwnerQuery {

    }
}
