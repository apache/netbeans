/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
