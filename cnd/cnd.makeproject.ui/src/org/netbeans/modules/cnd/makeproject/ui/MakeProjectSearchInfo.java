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
package org.netbeans.modules.cnd.makeproject.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.lookup.ServiceProvider;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectLookupProvider;

/**
 *
 */
final class MakeProjectSearchInfo extends SearchInfoDefinition {
    @ServiceProvider(service = MakeProjectLookupProvider.class)
    public static class MakeProjectSearchInfoFactory implements MakeProjectLookupProvider {

        @Override
        public void addLookup(MakeProject owner, ArrayList<Object> ic) {
            ic.add(new MakeProjectSearchInfo(owner.getConfigurationDescriptorProvider()));
        }
    }
    
    private final ConfigurationDescriptorProvider projectDescriptorProvider;

    private MakeProjectSearchInfo(ConfigurationDescriptorProvider projectDescriptorProvider) {
        this.projectDescriptorProvider = projectDescriptorProvider;
    }

    @Override
    public boolean canSearch() {
        return true;
    }

    @Override
    public List<SearchRoot> getSearchRoots() {
        List<SearchRoot> roots = new ArrayList<>();
        if (projectDescriptorProvider.gotDescriptor()) {
            final MakeConfigurationDescriptor configurationDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
            if (configurationDescriptor != null) {
                FileObject baseDirFileObject = configurationDescriptor.getBaseDirFileObject();
                roots.add(new SearchRoot(baseDirFileObject, null));
                configurationDescriptor.getAbsoluteSourceRoots().forEach((root) -> {
                    try {
                        FileObject fo = new FSPath(baseDirFileObject.getFileSystem(), root).getFileObject();
                        if (fo != null && !baseDirFileObject.equals(fo)) {
                            roots.add(new SearchRoot(fo, null));
                        }
                    } catch (FileStateInvalidException ex) {
                    }
                });
            }
        }
        return roots;
    }

    @Override
    public Iterator<FileObject> filesToSearch(final SearchScopeOptions options, SearchListener listener, final AtomicBoolean terminated) {
        FolderSearchInfo.FileObjectNameMatcherImpl matcher = new FolderSearchInfo.FileObjectNameMatcherImpl(options, terminated);
        if (projectDescriptorProvider.gotDescriptor()) {
            MakeConfigurationDescriptor configurationDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
            if (configurationDescriptor != null) {
                Folder rootFolder = configurationDescriptor.getLogicalFolders();
                Set<FileObject> res = rootFolder.getAllItemsAsFileObjectSet(false, matcher);
                FileObject baseDirFileObject = projectDescriptorProvider.getConfigurationDescriptor().getBaseDirFileObject();
                final Item[] projectItems = projectDescriptorProvider.getConfigurationDescriptor().getProjectItems();
                for (Item item : projectItems) {
                    FileObject fo = item.getFileObject();
                    if (fo != null && (matcher == null || matcher.pathMatches(fo))) {
                        res.add(fo);
                    }
                }
                addFolder(res, baseDirFileObject.getFileObject(MakeConfiguration.NBPROJECT_FOLDER), matcher);
                //addFolder(res, baseDirFileObject.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER), matcher);
                return res.iterator();
            }
        }
        return new ArrayList<FileObject>().iterator();
    }

    private void addFolder(Set<FileObject> res, FileObject fo, Folder.FileObjectNameMatcher matcher) {
        if (fo != null && fo.isFolder() && fo.isValid()) {
            if (matcher.isTerminated()) {
                return;
            }
            for (FileObject f : fo.getChildren()) {
                if (matcher.isTerminated()) {
                    return;
                }
                if (f.isData() && matcher.pathMatches(f)) {
                    res.add(f);
                } else if (f.isFolder()) {
                    addFolder(res, f, matcher);
                }
            }
        }
    }
    
}
