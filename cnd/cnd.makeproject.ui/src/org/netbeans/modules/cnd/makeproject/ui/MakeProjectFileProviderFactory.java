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

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.actions.Editable;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectFileProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.spi.configurations.UserOptionsProvider;
import org.netbeans.modules.cnd.makeproject.api.ui.ItemEx;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.netbeans.spi.jumpto.file.FileProvider;
import org.netbeans.spi.jumpto.file.FileProviderFactory;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.jumpto.file.FileProviderFactory.class, position=1000)
public class MakeProjectFileProviderFactory extends MakeProjectFileProvider implements FileProviderFactory {

    private static final Logger LOG = Logger.getLogger(MakeProjectFileProviderFactory.class.getName());

    @Override
    public String name() {
        return "CND FileProviderFactory"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return name();
    }

    @Override
    public FileProvider createFileProvider() {
        return new FileProviderImpl();
    }

    
    public final static class FileProviderImpl extends NativeFileSearchImpl implements FileProvider {
        private final AtomicBoolean cancel = new AtomicBoolean();
        private final Set<SearchContext> searchedProjects = new HashSet<>();
        private Context lastContext = null;
        
        public FileProviderImpl() {
        }

        @Override
        public boolean computeFiles(Context context, Result result) {
            cancel.set(false);
            Project project = context.getProject();
            if (project == null) {
                LOG.log(Level.FINE, "ComputeFiles: no project for {0}", context.getRoot());// NOI18N
                return false;
            }
            ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (provider == null) {
                LOG.log(Level.FINE, "ComputeFiles: no make project for {0}", context.getRoot());// NOI18N
                return false;
            }
            SearchContext searchContext = new SearchContext(context.getText(), context.getSearchType(), project);
            // check if anything have changed in context, just compare context instances
            if (context != lastContext) {
                lastContext = context;
                searchedProjects.clear();
            }
            if (searchedProjects.add(searchContext)) {
                if (provider.gotDescriptor()) {
                    MakeConfigurationDescriptor descriptor = provider.getConfigurationDescriptor();
                    Sources srcs = ProjectUtils.getSources(project);
                    final SourceGroup[] genericSG = srcs.getSourceGroups(Sources.TYPE_GENERIC);
                    if (genericSG != null && genericSG.length > 0) {
                        for(SourceGroup group : genericSG) {
                            if (group.getRootFolder().equals(context.getRoot())) {
                                NameMatcher matcher = NameMatcherFactory.createNameMatcher(context.getText(), context.getSearchType());
                                computeFiles(project, descriptor, matcher, result);
                            }
                        }
                    }
                } else {
                    LOG.log(Level.FINE, "ComputeFiles: skip search because project is not ready yet {0}", searchContext);// NOI18N
                }
            } else {
                LOG.log(Level.FINE, "ComputeFiles: skip already searched context {0}", searchContext);// NOI18N
            }
            // notify infrastructure that project related source root is handled
            return true;
        }


        @Override
        public void cancel() {
            cancel.set(true);
        }

        private void computeFiles(Project project, MakeConfigurationDescriptor descriptor, NameMatcher matcher, Result result) {
            FileObject projectDirectoryFO = project.getProjectDirectory();
            // track configuration && generated files
            if (projectDirectoryFO != null) {
                FileObject nbFO = projectDirectoryFO.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
                computeFOs(nbFO, matcher, result);
            }
            for (Item item : descriptor.getExternalFileItemsAsArray()) {
                if (cancel.get()) {
                    return;
                }
                if (matcher.accept(item.getName())) {
                    result.addFileDescriptor(new ItemFD((ItemEx)item, project));
                }
            }
            for (Item item : descriptor.getProjectItems()) {
                if (cancel.get()) {
                    return;
                }
                if (matcher.accept(item.getName())) {
                    result.addFileDescriptor(new ItemFD((ItemEx)item, project));
                }
            }
            Map<Folder,List<CharSequence>> projectSearchBase = searchBase.get(project);
            if (projectSearchBase != null) {
                synchronized (projectSearchBase) {
                    projectSearchBase = new HashMap<>(projectSearchBase);
                }
                String baseDir = descriptor.getBaseDir();
                for (Map.Entry<Folder, List<CharSequence>> entry : projectSearchBase.entrySet()) {
                    if (cancel.get()) {
                        return;
                    }
                    Folder folder = entry.getKey();
                    List<CharSequence> files = entry.getValue();
                    if (files != null) {
                        synchronized(files) {
                            for(CharSequence name : files) {
                                if (cancel.get()) {
                                    return;
                                }
                                if (matcher.accept(name.toString())) {
                                    result.addFileDescriptor(new OtherFD(name.toString(), project, baseDir, folder));
                                }
                            }
                        }
                    }
                }
            }
        }

        private void computeFOs(FileObject nbFO, NameMatcher matcher, Result result) {
            if (nbFO != null) {
                assert nbFO.isFolder();
                for (FileObject fileObject : nbFO.getChildren()) {
                    if (fileObject.isFolder()) {
                        computeFOs(fileObject, matcher, result);
                    } else if (matcher.accept(fileObject.getNameExt())) {
                        result.addFile(fileObject);
                    }
                }
            }
        }
        
        private static final class SearchContext {

            private final String searchText;
            private final SearchType searchType;
            private final Project project;

            public SearchContext(String searchText, SearchType searchType, Project project) {
                this.searchText = searchText;
                this.searchType = searchType;
                this.project = project;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final SearchContext other = (SearchContext) obj;
                if ((this.searchText == null) ? (other.searchText != null) : !this.searchText.equals(other.searchText)) {
                    return false;
                }
                if (this.searchType != other.searchType) {
                    return false;
                }
                if (this.project != other.project && (this.project == null || !this.project.equals(other.project))) {
                    return false;
                }
                return true;
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 43 * hash + (this.searchText != null ? this.searchText.hashCode() : 0);
                hash = 43 * hash + (this.searchType != null ? this.searchType.hashCode() : 0);
                hash = 43 * hash + (this.project != null ? this.project.hashCode() : 0);
                return hash;
            }

            @Override
            public String toString() {
                return "SearchContext{" + "searchText=" + searchText + ", searchType=" + searchType + ", project=" + project + '}'; // NOI18N
            }
        }
    }

    private static abstract class FDImpl extends FileDescriptor {
        private final String fileName;
        private final Project project;

        public FDImpl(String fileName, Project project) {
            this.fileName = fileName;
            this.project = project;
        }

        @Override
        public final String getFileName() {
            return fileName;
        }
        
        @Override
        public final String getProjectName() {
            ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);
            return info.getDisplayName();
        }   
        
        @Override
        public final Icon getProjectIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/resources/makeProject.gif", true); // NOI18N
        }       
        
        @Override
        public final void open() {
            DataObject od = getDataObject();
            if (od != null) {
                // comment out trick
//                // use trick due to CR7002932
//                EditorCookie erc = od.getCookie(EditorCookie.class);
//                if (erc != null) {
//                    try {
//                        try {
//                            erc.openDocument();
//                        } catch (UserQuestionException e) {
//                            e.confirmed();
//                            erc.openDocument();
//                        }
//                    } catch (IOException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//                    erc.open();
//                } else {
                    Editable ec = od.getLookup().lookup(Editable.class);
                    if (ec != null) {
                        ec.edit();
                    } else {
                        Openable oc = od.getLookup().lookup(Openable.class);
                        if (oc != null) {
                            oc.open();
                        }
                    }
//                }
            }
        }
        
        @Override
        public final Icon getIcon() {
            DataObject od = getDataObject();
            if (od != null) {
                Image i = od.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
                return new ImageIcon(i);
            }
            return null;
        }
        
        protected abstract DataObject getDataObject();
        
    }
    
    private static final class ItemFD extends FDImpl {
        
        private static final boolean VISUALIZE_LINK = true;
        private final ItemEx item;

        public ItemFD(ItemEx item, Project project) {
            super(item.getName(), project);
            this.item = item;
        }

        @Override
        public String getOwnerPath() {
            StringBuilder out = new StringBuilder();
            Folder parent = item.getFolder();
            while (parent != null && parent.getParent() != null) {
                if (out.length() > 0) {
                    out.insert(0, "/"); // NOI18N
                }
                out.insert(0, parent.getDisplayName());
                parent = parent.getParent();
            }
            if (VISUALIZE_LINK) {
                String canPath = item.getCanonicalPath();
                String path = item.getAbsolutePath();
                if (!canPath.equals(path)) {
                    // This is unicode up arrow
                    return "\u2191"+out.toString(); //NOI18N 
                }
            }
            return out.toString();
        }

        @Override
        public FileObject getFileObject() {
            return item.getFileObject();
        }

        @Override
        protected DataObject getDataObject() {
            return item.getDataObject();
        }

        @Override
        public String getFileDisplayPath() {
            return item.getNormalizedPath();
        }
    }

    private static final class OtherFD extends FDImpl {

        private final String name;
        private final Folder folder;
        private final String baseDir;
        public OtherFD(String name, Project project, String baseDir, Folder folder) {
            super(name, project);
            this.name = name;
            this.folder = folder;
            this.baseDir = baseDir;
        }

        @Override
        public String getOwnerPath() {
            return folder.getPath();
        }

        @Override
        protected DataObject getDataObject(){
            try {
                FileObject fo = getFileObject();
                if (fo != null && fo.isValid()) {
                    return DataObject.find(fo);
                }
            } catch (DataObjectNotFoundException e) {
            }
            return null;
        }

        @Override
        public FileObject getFileObject() {
            FileObject fileObject = RemoteFileUtil.getFileObject(folder.getConfigurationDescriptor().getBaseDirFileObject(), folder.getRootPath()+"/"+name); //NOI18N
            if (fileObject != null && fileObject.isValid()) {
                return fileObject;
            }
            return null;
        }
        
        @Override
        public String getFileDisplayPath() {
            return folder.getRootPath()+"/"+name; //NOI18N
        }
    }
}
