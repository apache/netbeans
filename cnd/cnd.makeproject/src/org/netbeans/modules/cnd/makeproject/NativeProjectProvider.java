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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectChangeSupport;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport.NativeExitStatus;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.TempEnv;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeLogicalViewModel;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.spi.configurations.IncludePathExpansionProvider;
import org.netbeans.modules.cnd.makeproject.spi.configurations.UserOptionsProvider;
import org.netbeans.modules.cnd.toolchain.support.ToolchainUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class NativeProjectProvider implements NativeProject, PropertyChangeListener, ChangeListener, NativeProjectChangeSupport {
    private static final boolean USE_STANDARD_HEADER_INDEX_FILES = CndUtils.getBoolean("cnd.use.headers.indexer", true); // NOI18N
    private static final boolean TRACE = false;
    private final Project project;
    private final String projectRoot;
    private final FileSystem fileSystem;
    private final ConfigurationDescriptorProviderImpl projectDescriptorProvider;
    private final Set<NativeProjectItemsListener> listeners = new HashSet<>();
    private static final RequestProcessor RPCC = new RequestProcessor("NativeProjectProvider.CheckConfiguration", 1); // NOI18N
    private NativeFileItem indexerC;
    private NativeFileItem indexerCpp;

    public NativeProjectProvider(Project project, RemoteProject remoteProject, ConfigurationDescriptorProviderImpl projectDescriptorProvider) {
        assert remoteProject != null;
        this.project = project;
        this.fileSystem = getFileSystem(remoteProject);
        this.projectRoot = getProjectRoot(remoteProject);
        this.projectDescriptorProvider = projectDescriptorProvider;
        ToolchainUtilities.addCodeAssistanceChangeListener(this);
    }

    @Override
    public void runOnProjectReadiness(NamedRunnable task) {
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        if (descriptor != null) {
            descriptor.getConfs().runOnProjectReadiness(task);
        }
    }

    private void addMyListeners() {
        projectDescriptorProvider.getConfigurationDescriptorImpl().getConfs().addPropertyChangeListener(this);
    }

    private void removeMyListeners() {
        projectDescriptorProvider.getConfigurationDescriptorImpl().getConfs().removePropertyChangeListener(this);
    }

    public MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        return projectDescriptorProvider.getConfigurationDescriptor();
    }

    public MakeConfiguration getMakeConfiguration() {
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        if (descriptor != null) {
            return descriptor.getActiveConfiguration();
        }
        return null;
    }

    @Override
    public Lookup.Provider getProject() {
        return project;
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    private static FileSystem getFileSystem(RemoteProject remoteProject) {
        FileSystem fileSystem;
        ExecutionEnvironment env = remoteProject.getSourceFileSystemHost();
        fileSystem = FileSystemProvider.getFileSystem(env);
        CndUtils.assertNotNull(fileSystem, "null file system"); //NOI18N
        return fileSystem;
    }

    @Override
    public List<String> getSourceRoots() {
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        if (descriptor != null) {
            return descriptor.getAbsoluteSourceRoots();
//            List<String> res = new ArrayList<String>(1);
//            res.add(descriptor.getBaseDir());
//            return res;
        }
        return Collections.<String>emptyList();
    }

    @Override
    public String getProjectRoot() {
        return projectRoot;
    }

    private static String getProjectRoot(RemoteProject remoteProject) {
        String projectRoot = remoteProject.getSourceBaseDir();
        CndUtils.assertNotNull(projectRoot, "null projectRoot"); //NOI18N
        return projectRoot;
    }

    @Override
    public String getProjectDisplayName() {
        return ProjectUtils.getInformation(project).getDisplayName();
    }

    @Override
    public List<NativeFileItem> getAllFiles() {
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        if (descriptor != null) {
            MakeConfiguration conf = descriptor.getActiveConfiguration();
            if (conf != null) {
                List<NativeFileItem> list = new ArrayList<>();
                Item[] items = descriptor.getProjectItems();
                for (Item item : items) {
                    ItemConfiguration itemConfiguration = item.getItemConfiguration(conf);
                    if (itemConfiguration != null) {
                        if (itemConfiguration.isCompilerToolConfiguration()) {
                            list.add(item);
                        } else if (item.hasHeaderOrSourceExtension(true, true)) {
                            list.add(item);
                        }
                    }
                }
                list.addAll(getStandardHeadersIndexers());
                return list;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<NativeFileItem> getStandardHeadersIndexers() {
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        if (descriptor != null) {
            MakeConfiguration conf = descriptor.getActiveConfiguration();
            if (conf != null) {
                List<NativeFileItem> list = new ArrayList<>();
                synchronized(this) {
                    if (USE_STANDARD_HEADER_INDEX_FILES && indexerC == null) {
                        indexerC = createIndexer(descriptor, this, NativeFileItem.Language.C);
                    }
                    if (indexerC != null) {
                        list.add(indexerC);
                    }
                    if (USE_STANDARD_HEADER_INDEX_FILES && indexerCpp == null) {
                        indexerCpp = createIndexer(descriptor, this, NativeFileItem.Language.CPP);
                    }
                    if (indexerCpp != null) {
                        list.add(indexerCpp);
                    }
                }
                return list;
            }
        }
        return Collections.emptyList();
    }

    private NativeFileItem createIndexer(MakeConfigurationDescriptor descriptor, NativeProjectProvider nativeProject, final NativeFileItem.Language language) {
        FileObject projectDir = descriptor.getProject().getProjectDirectory();
        FileObject indexer;
        if (language == NativeFileItem.Language.C) {
            indexer = projectDir.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER+"/"+StandardHeadersProjectMetadataFactory.C_STANDARD_HEADERS_INDEXER); //NOI18N
        } else {
            indexer = projectDir.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER+"/"+StandardHeadersProjectMetadataFactory.CPP_STANDARD_HEADERS_INDEXER); //NOI18N
        }
        if (indexer != null && indexer.isValid()) {
            return new NativeFileIndexer(nativeProject, indexer, language);
        }
        return null;
    }
    
    private Reference<List<NativeProject>> cachedDependency = new SoftReference<>(null);

    @Override
    public List<NativeProject> getDependences() {
        List<NativeProject> cachedList = cachedDependency.get();
        if (cachedList == null) {
            cachedList = new ArrayList<>(0);
            MakeConfiguration makeConfiguration = getMakeConfiguration();
            int size = 0;
            NativeProject oneOf = null;
            if (makeConfiguration != null) {
                for (Object lib : makeConfiguration.getSubProjects()) {
                    Project prj = (Project) lib;
                    NativeProject nativeProject = prj.getLookup().lookup(NativeProject.class);
                    if (nativeProject != null) {
                        cachedList.add(nativeProject);
                        size++;
                        oneOf = nativeProject;
                    }
                }
            }
            if (size == 0) {
                cachedList = Collections.<NativeProject>emptyList();
            } else if (size == 1) {
                cachedList = Collections.singletonList(oneOf);
            } else {
                cachedList = Collections.unmodifiableList(cachedList);
            }
            cachedDependency = new SoftReference<>(cachedList);
        }
        return cachedList;
    }

    @Override
    public void addProjectItemsListener(NativeProjectItemsListener listener) {
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                addMyListeners();
            }
            listeners.add(listener);
        }
    }

    @Override
    public void removeProjectItemsListener(NativeProjectItemsListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                removeMyListeners();
            }
        }
    }

    @Override
    public void fireFilesAdded(List<NativeFileItem> nativeFileIetms) {
        if (TRACE) {
            System.out.println("fireFileAdded "); // NOI18N
        }
        ArrayList<NativeFileItem> actualList = new ArrayList<>();
        // Remove non C/C++ items
        Iterator<NativeFileItem> iter = nativeFileIetms.iterator();
        while (iter.hasNext()) {
            final NativeFileItem nativeFileIetm = iter.next();
            if (nativeFileIetm == null) {
                continue;
            }
            if (nativeFileIetm instanceof Item) {
                PredefinedToolKind tool = ((Item) nativeFileIetm).getDefaultTool();
                if (tool == PredefinedToolKind.CustomTool
                        // check of mime type is better to support headers without extensions
                        && !MIMENames.HEADER_MIME_TYPE.equals(((Item) nativeFileIetm).getMIMEType())) {
                    continue; // IZ 87407
                }
            }
            actualList.add(nativeFileIetm);
            if (TRACE) {
                System.out.println("    " + nativeFileIetm.getAbsolutePath()); // NOI18N
            }
        }
        // Fire NativeProject change event
        if (!actualList.isEmpty()) {
            getListenersCopy().forEach((listener) -> {
                listener.filesAdded(actualList);
            });
        }
    }

    @Override
    public void fireFilesRemoved(List<NativeFileItem> nativeFileItems) {
        if (TRACE) {
            System.out.println("fireFilesRemoved "); // NOI18N
        }
        // Fire NativeProject change event
        if (!nativeFileItems.isEmpty()) {
            getListenersCopy().forEach((listener) -> {
                listener.filesRemoved(nativeFileItems);
            });
        }
    }

    @Override
    public void fireFileRenamed(String oldPath, NativeFileItem newNativeFileIetm) {
        getListenersCopy().forEach((listener) -> {
            listener.fileRenamed(oldPath, newNativeFileIetm);
        });
    }

    @Override
    public void fireFilesPropertiesChanged(List<NativeFileItem> fileItems) {
        //System.out.println("fireFilesPropertiesChanged " + fileItems);
        getListenersCopy().forEach((listener) -> {
            listener.filesPropertiesChanged(fileItems);
        });
    }

    @Override
    public void fireFilesPropertiesChanged() {
        if (TRACE) {
            new Exception().printStackTrace(System.err);
            System.out.println("fireFilesPropertiesChanged "); // NOI18N
        }
        getListenersCopy().forEach((listener) -> {
            listener.filesPropertiesChanged(this);
        });
    }

    private final AtomicBoolean fileOperationsProgress = new AtomicBoolean(false);
    @Override
    public void fireFileOperationsStarted() {
        if (TRACE) {
            new Exception().printStackTrace(System.err);
            System.out.println("fireFileOperationsStarted " + fileOperationsProgress); // NOI18N
        }
        if (fileOperationsProgress.compareAndSet(false, true)) {
            getListenersCopy().forEach((listener) -> {
                listener.fileOperationsStarted(this);
            });
        }
    }

    @Override
    public void fireFileOperationsFinished() {
        if (TRACE) {
            new Exception().printStackTrace(System.err);
            System.out.println("fireFileOperationsFinished " + fileOperationsProgress); // NOI18N
        }
        if (fileOperationsProgress.compareAndSet(true, false)) {
            getListenersCopy().forEach((listener) -> {
                listener.fileOperationsFinished(this);
            });
        }
    }

    public void fireProjectDeleted() {
        if (TRACE) {
            System.out.println("fireProjectDeleted "); // NOI18N
        }
        getListenersCopy().forEach((listener) -> {
            listener.projectDeleted(this);
        });
    }

    @SuppressWarnings("unchecked")
    private List<NativeProjectItemsListener> getListenersCopy() {
        synchronized (listeners) {
            return (listeners.isEmpty()) ? Collections.EMPTY_LIST : new ArrayList<>(listeners);
        }
    }

    @Override
    public NativeFileItem findFileItem(FileObject fileObject) {
        NativeFileItem out = null;
        if (projectDescriptorProvider.gotDescriptor()) {
            MakeConfigurationDescriptor descr = getMakeConfigurationDescriptor();
            if (descr != null) {
                out = (NativeFileItem) descr.findItemByFileObject(fileObject);
                if (out == null && fileObject != null) {
                    // could be standard headers indexer
                    List<NativeFileItem> standardHeadersIndexers = getStandardHeadersIndexers();
                    for (NativeFileItem standardHeadersIndexer : standardHeadersIndexers) {
                        if (fileObject.equals(standardHeadersIndexer.getFileObject())) {
                            out = standardHeadersIndexer;
                            break;
                        }
                    }
                }
            }
        }
        return out;
    }

    private void checkConfigurationChanged(final Configuration oldConf, final Configuration newConf) {
        if (TRACE) {
            new Exception().printStackTrace(System.err);
        }
        if (SwingUtilities.isEventDispatchThread()) {
            RPCC.post(() -> {
                checkConfigurationChangedWorker(oldConf, newConf);
            });
        } else {
            checkConfigurationChangedWorker(oldConf, newConf);
        }
    }

    private void checkConfigurationChangedWorker(Configuration oldConf, Configuration newConf) {
        MakeConfiguration oldMConf = (MakeConfiguration) oldConf;
        MakeConfiguration newMConf = (MakeConfiguration) newConf;
        List<NativeFileItem> list = new ArrayList<>();
        List<NativeFileItem> added = new ArrayList<>();
        List<NativeFileItem> deleted = new ArrayList<>();

        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
        }

        if (newConf == null) {
            // How can this happen?
            System.err.println("Nativeprojectprovider - checkConfigurationChanged - newConf is null!"); // NOI18N
            return;
        }

        if (!newConf.isDefault()) {
            return;
        }

        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        Item[] items = descriptor.getProjectItems();
        Project proj = descriptor.getProject();

        ConfigurationDescriptorProvider.recordMetrics(ConfigurationDescriptorProvider.USG_PROJECT_CONFIG_CND, descriptor);

        if (oldConf == null) {
            // What else can we do?
            firePropertiesChanged(items, true, true, true);
            MakeLogicalViewModel viewModel = getProject().getLookup().lookup(MakeLogicalViewModel.class);
            if (viewModel != null) {
                viewModel.checkForChangedViewItemNodes(null, null);
                viewModel.checkForChangedName();
            }
            return;
        }

        boolean toolColectionChanged = false;
        // Check compiler collection. Fire if different (IZ 131825)
        if (!oldMConf.getCompilerSet().getName().equals(newMConf.getCompilerSet().getName())
                || !oldMConf.getDevelopmentHost().getExecutionEnvironment().equals(newMConf.getDevelopmentHost().getExecutionEnvironment())) {
            MakeLogicalViewModel viewModel = getProject().getLookup().lookup(MakeLogicalViewModel.class);
            if (viewModel != null) {
                viewModel.checkForChangedViewItemNodes(null, null);
            }
            if (!oldMConf.getDevelopmentHost().getExecutionEnvironment().equals(newMConf.getDevelopmentHost().getExecutionEnvironment())) {
                if (viewModel != null) {
                    viewModel.checkForChangedName();
                }
            }
            toolColectionChanged = true;
        }

        if (toolColectionChanged && newConf.getName().equals(oldConf.getName())) {
            fireFilesPropertiesChanged();
            return;
        }

        CompilerSet oldCompilerSet = oldMConf.getCompilerSet().getCompilerSet();
        CompilerSet newCompilerSet = newMConf.getCompilerSet().getCompilerSet();

        // Check all items
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration oldItemConf = items[i].getItemConfiguration(oldMConf); //ItemConfiguration)oldMConf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            ItemConfiguration newItemConf = items[i].getItemConfiguration(newMConf); //ItemConfiguration)newMConf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            if (oldItemConf == null || newItemConf == null) {
                continue;
            }

            if ((newItemConf.getExcluded().getValue() ^ oldItemConf.getExcluded().getValue())
                    && (newItemConf.getTool() == PredefinedToolKind.CCompiler
                    || newItemConf.getTool() == PredefinedToolKind.CCCompiler
                    || items[i].hasHeaderOrSourceExtension(true, true))) {
                if (newItemConf.getExcluded().getValue()) {
                    // excluded
                    deleted.add(items[i]);
                } else {
                    // included
                    added.add(items[i]);
                }
                MakeLogicalViewModel viewModel = getProject().getLookup().lookup(MakeLogicalViewModel.class);
                if (viewModel != null) {
                    viewModel.checkForChangedViewItemNodes(null, items[i]);
                }
                continue;
            }

            if (newItemConf.getTool() == PredefinedToolKind.CCompiler) {
                if (oldItemConf.getTool() != PredefinedToolKind.CCompiler) {
                    list.add(items[i]);
                    continue;
                }
                if (oldCompilerSet == null || newCompilerSet == null) {
                    if (oldCompilerSet != null || newCompilerSet != null) {
                        list.add(items[i]);
                    }
                    continue;
                }
                if (!oldItemConf.getCCompilerConfiguration().getPreprocessorOptions(oldCompilerSet).equals(newItemConf.getCCompilerConfiguration().getPreprocessorOptions(newCompilerSet))) {
                    list.add(items[i]);
                    continue;
                }
                if (!oldItemConf.getCCompilerConfiguration().getIncludeDirectoriesOptions(oldCompilerSet).equals(newItemConf.getCCompilerConfiguration().getIncludeDirectoriesOptions(newCompilerSet))) {
                    list.add(items[i]);
                    continue;
                }
            } else if (newItemConf.getTool() == PredefinedToolKind.CCCompiler) {
                if (oldItemConf.getTool() != PredefinedToolKind.CCCompiler) {
                    list.add(items[i]);
                    continue;
                }
                if (oldCompilerSet == null || newCompilerSet == null) {
                    if (oldCompilerSet != null || newCompilerSet != null) {
                        list.add(items[i]);
                    }
                    continue;
                }
                if (!oldItemConf.getCCCompilerConfiguration().getPreprocessorOptions(oldCompilerSet).equals(newItemConf.getCCCompilerConfiguration().getPreprocessorOptions(newCompilerSet))) {
                    list.add(items[i]);
                    continue;
                }
                if (!oldItemConf.getCCCompilerConfiguration().getIncludeDirectoriesOptions(oldCompilerSet).equals(newItemConf.getCCCompilerConfiguration().getIncludeDirectoriesOptions(newCompilerSet))) {
                    list.add(items[i]);
                    continue;
                }
            }
        }
        fireFilesRemoved(deleted);
        fireFilesAdded(added);
        if (!list.isEmpty()) {
            fireFilesPropertiesChanged(list);
        }
        if (toolColectionChanged) {
            fireFilesPropertiesChanged();
        }
    }

    private void firePropertiesChanged(Item[] items, boolean cFiles, boolean ccFiles, boolean projectChanged) {
        MakeConfiguration conf = getMakeConfiguration();
        firePropertiesChanged(items, cFiles, ccFiles, projectChanged, conf, this);
    }

    public static void firePropertiesChanged(Item[] items, boolean cFiles, boolean ccFiles, boolean projectChanged,
            MakeConfiguration conf, NativeProjectChangeSupport nativeProjectChangeSupport) {
        if (nativeProjectChangeSupport == null) {
            return;
        }
        ArrayList<NativeFileItem> list = new ArrayList<>();
        ArrayList<NativeFileItem> deleted = new ArrayList<>();
        // Handle project and file level changes
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration itemConfiguration = items[i].getItemConfiguration(conf);
            if (itemConfiguration != null) { // prevent NPE for corrupted projects IZ#174350
                if (items[i].isExcluded()) {
                    deleted.add(items[i]);
                    continue;
                }
                if ((cFiles && itemConfiguration.getTool() == PredefinedToolKind.CCompiler)
                        || (ccFiles && itemConfiguration.getTool() == PredefinedToolKind.CCCompiler)
                        || items[i].hasHeaderOrSourceExtension(cFiles, ccFiles)) {
                    list.add(items[i]);
                }
            }
        }
        if (!deleted.isEmpty()) {
            nativeProjectChangeSupport.fireFilesRemoved(deleted);
        }
        if (projectChanged) {
            nativeProjectChangeSupport.fireFilesPropertiesChanged();
        } else if (!list.isEmpty()) {
            nativeProjectChangeSupport.fireFilesPropertiesChanged(list);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (TRACE) {
            System.out.println("propertyChange " + evt.getPropertyName()); // NOI18N
        }
        if (evt.getPropertyName().equals(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE)) {
            checkConfigurationChanged((Configuration) evt.getOldValue(), (Configuration) evt.getNewValue());
        }
    }

    /**
     * Returns a list <String> of compiler defined include paths used when parsing 'orpan' source files.
     * @return a list <String> of compiler defined include paths.
     * A path is always an absolute path.
     * Include paths are not prefixed with the compiler include path option (usually -I).
     */
    /*
     * Return C++ settings
     **/
    @Override
    public List<IncludePath> getSystemIncludePaths() {
        return getSystemIncludePaths(NativeFileItem.Language.CPP);
    }

    public List<IncludePath> getSystemIncludePaths(NativeFileItem.Language language) {
        ArrayList<IncludePath> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration == null) {
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler;
        CCCCompilerConfiguration compilerConfiguration;
        if (language == NativeFileItem.Language.C) {
            compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
            compilerConfiguration = makeConfiguration.getCCompilerConfiguration();
        } else {
            compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
            compilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
        }
        if (compilerConfiguration != null) {
            // Get include paths from compiler
            if (compiler != null && compiler.getPath() != null && compiler.getPath().length() > 0) {
                FileSystem fs = FileSystemProvider.getFileSystem(compiler.getExecutionEnvironment());
                if (makeConfiguration.isMakefileConfiguration()) {
                    vec.addAll(IncludePath.toIncludePathList(fs, compiler.getSystemIncludeDirectories(getImportantFlags(language))));
                } else {
                    String importantFlags = NativeProjectProvider.SPI_ACCESSOR.getImportantFlags(compilerConfiguration, compiler, makeConfiguration);
                    vec.addAll(IncludePath.toIncludePathList(fs, compiler.getSystemIncludeDirectories(importantFlags)));
                }
            }
        }
        return NativeProjectProvider.SPI_ACCESSOR.expandIncludePaths(vec, compilerConfiguration, compiler, makeConfiguration);
    }

    /**
     * Compiler pre-included system headers.
     * I.e. files that are included in any compilation unit.
     *
     * @return list <FSPath> of pre-included headers
     */
    /*
     * Return C++ settings
     **/
    @Override
    public List<FSPath> getSystemIncludeHeaders() {
        return getSystemIncludeHeaders(NativeFileItem.Language.CPP);
    }

    public List<FSPath> getSystemIncludeHeaders(NativeFileItem.Language language) {
        ArrayList<FSPath> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration == null) {
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler;
        CCCCompilerConfiguration compilerConfiguration;
        if (language == NativeFileItem.Language.C) {
            compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
            compilerConfiguration = makeConfiguration.getCCompilerConfiguration();
        } else {
            compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
            compilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
        }
        if (compiler != null) {
            FileSystem compilerFS = FileSystemProvider.getFileSystem(compiler.getExecutionEnvironment());
            vec.addAll(CndFileUtils.toFSPathList(compilerFS, compiler.getSystemIncludeHeaders()));
        }
        if (compilerConfiguration != null) {
            // Get include paths from compiler
            if (compiler != null && compiler.getPath() != null && compiler.getPath().length() > 0) {
                FileSystem fs = FileSystemProvider.getFileSystem(compiler.getExecutionEnvironment());
                if (makeConfiguration.isMakefileConfiguration()) {
                    vec.addAll(CndFileUtils.toFSPathList(fs, compiler.getSystemIncludeHeaders(getImportantFlags(language))));
                } else {
                    String importantFlags = NativeProjectProvider.SPI_ACCESSOR.getImportantFlags(compilerConfiguration, compiler, makeConfiguration);
                    vec.addAll(CndFileUtils.toFSPathList(fs, compiler.getSystemIncludeHeaders(importantFlags)));
                }
            }
        }
        return vec;
    }

    /**
     * Returns a list <String> of user defined include paths used when parsing 'orpan' source files.
     * @return a list <String> of user defined include paths.
     * A path is always an absolute path.
     * Include paths are not prefixed with the compiler include path option (usually -I).
     */
    /*
     * Return C++ settings
     **/
    @Override
    public List<IncludePath> getUserIncludePaths() {
        return getUserIncludePaths(NativeFileItem.Language.CPP);
    }

    public List<IncludePath> getUserIncludePaths(NativeFileItem.Language language) {
        ArrayList<IncludePath> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration == null) {
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler;
        CCCCompilerConfiguration compilerConfiguration;
        if (language == NativeFileItem.Language.C) {
            compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
            compilerConfiguration = makeConfiguration.getCCompilerConfiguration();
        } else {
            compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
            compilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
        }
        ArrayList<String> vec2 = new ArrayList<>();
        vec2.addAll(compilerConfiguration.getIncludeDirectories().getValue());
        ExecutionEnvironment env = compiler.getExecutionEnvironment();            
        MacroConverter macroConverter = null;
        // Convert all paths to absolute paths
        FileSystem compilerFS = FileSystemProvider.getFileSystem(env);
        FileSystem projectFS = fileSystem;
        List<IncludePath> result = new ArrayList<>();            
        for (String p : vec2) {
            boolean compilerContext = false;
            if (p.contains("$")) { // NOI18N
                // macro based path
                if (macroConverter == null) {
                    macroConverter = new MacroConverter(env);
                }
                p = macroConverter.expand(p);
                compilerContext = true;
            }
            if (p.startsWith("///")) { //NOI18N
                // It is absolute path onbuild host
                compilerContext = true;
            }
            if (compilerContext && CndPathUtilities.isPathAbsolute(compilerFS, p)) {
                result.add(IncludePath.toIncludePath(compilerFS, p));
                continue;
            }
            if (CndPathUtilities.isPathAbsolute(projectFS, p)) {
                result.add(IncludePath.toIncludePath(projectFS, p));
            } else {
                String absPath = CndPathUtilities.toAbsolutePath(projectDescriptorProvider.getConfigurationDescriptor().getBaseDirFileObject(), p);
                result.add(IncludePath.toIncludePath(projectFS, absPath));
            }
        }
        List<IncludePath> vec3 = new ArrayList<>();
        vec3 = NativeProjectProvider.SPI_ACCESSOR.getItemUserIncludePaths(vec3, compilerConfiguration, compiler, makeConfiguration);
        result.addAll(vec3);
        return NativeProjectProvider.SPI_ACCESSOR.expandIncludePaths(result, compilerConfiguration, compiler, makeConfiguration);
    }

    /*
     * Return C++ settings
     **/
    @Override
    public List<FSPath> getIncludeFiles() {
        return getIncludeFiles(NativeFileItem.Language.CPP);
    }

    public List<FSPath> getIncludeFiles(NativeFileItem.Language language) {
        ArrayList<FSPath> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration == null) {
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler;
        CCCCompilerConfiguration compilerConfiguration;
        if (language == NativeFileItem.Language.C) {
            compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
            compilerConfiguration = makeConfiguration.getCCompilerConfiguration();
        } else {
            compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
            compilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
        }
        ArrayList<String> vec2 = new ArrayList<>();
        vec2.addAll(compilerConfiguration.getIncludeFiles().getValue());
        ExecutionEnvironment env = compiler.getExecutionEnvironment();            
        FileSystem compilerFS = FileSystemProvider.getFileSystem(env);
        FileSystem projectFS = fileSystem;
        MacroConverter macroConverter = null;
        for (String p : vec2) {
            boolean compilerContext = false;
            if (p.contains("$")) { // NOI18N
                // macro based path
                if (macroConverter == null) {
                    macroConverter = new MacroConverter(env);
                }
                p = macroConverter.expand(p);
                compilerContext = true;
            }
            if (p.startsWith("///")) { //NOI18N
                // It is absolute path onbuild host
                compilerContext = true;
            }
            if (compilerContext && CndPathUtilities.isPathAbsolute(compilerFS, p)) {
                vec.add(new FSPath(compilerFS, p));
                continue;
            }
            if (CndPathUtilities.isPathAbsolute(projectFS, p)) {
                vec.add(new FSPath(projectFS, p));
            } else {
                String absPath = CndPathUtilities.toAbsolutePath(projectDescriptorProvider.getConfigurationDescriptor().getBaseDirFileObject(), p);
                vec.add(new FSPath(projectFS, absPath));
            }
        }
        return vec;
    }

    /**
     * Returns a list <String> of compiler defined macro definitions used when parsing 'orpan' source files.
     * @return a list <String> of compiler defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     */
    /*
     * Return C++ settings
     **/
    @Override
    public List<String> getSystemMacroDefinitions() {
        return getSystemMacroDefinitions(NativeFileItem.Language.CPP);
    }

    public List<String> getSystemMacroDefinitions(NativeFileItem.Language language) {
        List<String> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
            if (compilerSet == null) {
                return vec;
            }
            AbstractCompiler compiler;
            CCCCompilerConfiguration compilerConfiguration;
            if (language == NativeFileItem.Language.C) {
                compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
                compilerConfiguration = makeConfiguration.getCCompilerConfiguration();
            } else {
                compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
                compilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
            }
            if (compiler != null) {
                if (makeConfiguration.isMakefileConfiguration()) {
                     vec.addAll(compiler.getSystemPreprocessorSymbols(getImportantFlags(language)));
                } else {
                    String importantFlags = SPI_ACCESSOR.getImportantFlags(compilerConfiguration, compiler, makeConfiguration);
                    vec.addAll(compiler.getSystemPreprocessorSymbols(importantFlags));
                }
            }
        }
        List<String> undefinedMacros = getUndefinedMacros(language);
        if (undefinedMacros.size() > 0) {
            List<String> out = new ArrayList<>();
            for(String macro : vec) {
                boolean remove = true;
                for(String undef : undefinedMacros) {
                    if (macro.equals(undef) ||
                        macro.startsWith(undef+"=")) { //NOI18N
                        remove = false;
                        break;
                    }
                }
                if (remove) {
                    out.add(macro);
                }
            }
            vec = out;
        }
        return vec;
    }
    
    private String getImportantFlags(NativeFileItem.Language language) {
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration == null) {
            return "";
        }
        CompilerSet2Configuration compilerSetConfiguration = makeConfiguration.getCompilerSet();
        if (compilerSetConfiguration == null) { // that's probably a paranoia, the reason of NPE was
            return ""; // in incorrect comparison 5 lines above
        }
        CompilerSet compilerSet = compilerSetConfiguration.getCompilerSet();
        if (compilerSet == null) {
            return "";
        }
        AbstractCompiler compiler;
        CCCCompilerConfiguration compilerConfiguration;
        if (language == NativeFileItem.Language.C) {
            compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
            compilerConfiguration = makeConfiguration.getCCompilerConfiguration();
        } else {
            compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
            compilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
        }
        String res = "";
        if (makeConfiguration.isMakefileConfiguration()) {
            res = compilerConfiguration.getImportantFlags().getValue();
        } else {
            // Get include paths from compiler
            if (compiler != null && compiler.getPath() != null && compiler.getPath().length() > 0) {
                res = NativeProjectProvider.SPI_ACCESSOR.getImportantFlags(compilerConfiguration, compiler, makeConfiguration);
                if (res == null) {
                    res = "";
                }
            }
        }
        
        if (res.isEmpty()) {
            // important flags were lost or user set language standard
            // try to restore right important flag by standard
            NativeFileItem.LanguageFlavor languageFlavor = getLanguageFlavor(language);
            res = DefaultSystemSettingsImpl.getStdFlagsForFlavor(languageFlavor);
        } 
        return res;
    }

    /**
     * Returns a list <String> of user defined macro definitions used when parsing 'orpan' source files.
     * @return a list <String> of user defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     */
    /*
     * Return C++ settings
     **/
    @Override
    public List<String> getUserMacroDefinitions() {
        return getUserMacroDefinitions(NativeFileItem.Language.CPP);
    }

    public List<String> getUserMacroDefinitions(NativeFileItem.Language language) {
        List<String> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
            if (compilerSet == null) {
                return vec;
            }
            AbstractCompiler compiler;
            CCCCompilerConfiguration compilerConfiguration;
            if (language == NativeFileItem.Language.C) {
                compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
                compilerConfiguration = makeConfiguration.getCCompilerConfiguration();
            } else {
                compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
                compilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
            }
            vec.addAll(compilerConfiguration.getPreprocessorConfiguration().getValue());
            vec = NativeProjectProvider.SPI_ACCESSOR.getItemUserMacros(vec, compilerConfiguration, compiler, makeConfiguration);
        }
        return vec;
    }

    private List<String> getUndefinedMacros(NativeFileItem.Language language) {
        ArrayList<String> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            CCCCompilerConfiguration compilerConfiguration;
            if (language == NativeFileItem.Language.C) {
                compilerConfiguration = makeConfiguration.getCCompilerConfiguration();
            } else {
                compilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
            }
            vec.addAll(compilerConfiguration.getUndefinedPreprocessorConfiguration().getValue());
        }
        return vec;
    }

    /**
     * Returns language flavor for C or C++ language from project properties.
     * 
     * @param language
     * @return 
     */
    public NativeFileItem.LanguageFlavor getLanguageFlavor(NativeFileItem.Language language) {
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        if (descriptor != null) {
            MakeConfiguration conf = descriptor.getActiveConfiguration();
            if (conf != null) {
                if (language == NativeFileItem.Language.C) {
                    CCompilerConfiguration cCompilerConfiguration = conf.getCCompilerConfiguration();
                    if (cCompilerConfiguration != null) {
                        switch (cCompilerConfiguration.getCStandard().getValue()) {
                            case CCompilerConfiguration.STANDARD_C89:
                                return NativeFileItem.LanguageFlavor.C89;
                            case CCompilerConfiguration.STANDARD_C99:
                                return NativeFileItem.LanguageFlavor.C99;
                            case CCompilerConfiguration.STANDARD_C11:
                                return NativeFileItem.LanguageFlavor.C11;
                            case CCompilerConfiguration.STANDARD_C17:
                                return NativeFileItem.LanguageFlavor.C17;
                            case CCompilerConfiguration.STANDARD_C23:
                                return NativeFileItem.LanguageFlavor.C23;
                            default:
                                return NativeFileItem.LanguageFlavor.DEFAULT;
                        }
                    }
                } else {
                    CCCompilerConfiguration ccCompilerConfiguration = conf.getCCCompilerConfiguration();
                    if (ccCompilerConfiguration != null) {
                        switch (ccCompilerConfiguration.getCppStandard().getValue()) {
                            case CCCompilerConfiguration.STANDARD_CPP98:
                                return NativeFileItem.LanguageFlavor.CPP98;
                            case CCCompilerConfiguration.STANDARD_CPP11:
                                return NativeFileItem.LanguageFlavor.CPP11;
                            case CCCompilerConfiguration.STANDARD_CPP14:
                                return NativeFileItem.LanguageFlavor.CPP14;
                            case CCCompilerConfiguration.STANDARD_CPP17:
                                return NativeFileItem.LanguageFlavor.CPP17;
                            case CCCompilerConfiguration.STANDARD_CPP20:
                                return NativeFileItem.LanguageFlavor.CPP20;
                            case CCCompilerConfiguration.STANDARD_CPP23:
                                return NativeFileItem.LanguageFlavor.CPP23;
                            default:
                                return NativeFileItem.LanguageFlavor.DEFAULT;
                        }
                    }
                }
            }
        }
        return NativeFileItem.LanguageFlavor.DEFAULT;
    }

    @Override
    public String toString() {
        return getProjectDisplayName() + " " + getProjectRoot(); // NOI18N
    }

    private void clearCache() {
        cachedDependency.clear();
    }

    /*package*/ NativeExitStatus execute(String executable, String[] env, String... args) throws IOException {
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ExecutionEnvironment ev = makeConfiguration.getDevelopmentHost().getExecutionEnvironment();
        return execute(ev, executable, env, args);
    }

    /*package*/ static NativeExitStatus execute(ExecutionEnvironment ee, String executable, String[] env, String... args) throws IOException {
        ServerRecord record = ServerList.get(ee);
        if (!record.isOnline()) {
            return new NativeExitStatus(-1, "", getString("HOST_OFFLINE", ee.getHost()));
        }
        try {
            if (ee.isLocal()) {
                String exePath = Path.findCommand(executable);
                if (exePath == null) {
                    throw new IOException(getString("NOT_FOUND", executable));  // NOI18N
                }
                executable = exePath;
            }
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(ee);
            //npb.setCharset(Charset.forName("UTF-8")); // NOI18N
            npb.setExecutable(executable); //NOI18N
            npb.setArguments(args);
            if (env != null) {
                for(String envEntry: env) {
                    String[] varValuePair = envEntry.split("=");  // NOI18N
                    npb.getEnvironment().put(varValuePair[0], varValuePair[1]);
                }
            }
            ExitStatus exitStatus =  ProcessUtils.execute(npb);
            return new NativeExitStatus(exitStatus.exitCode, exitStatus.getOutputString(), exitStatus.getErrorString());
        } catch (Exception e) {
            return new NativeExitStatus(-1, "", e.getMessage());
        }
    }

    /*package*/String getPlatformName() {
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        String platformName = makeConfiguration.getDevelopmentHost().getBuildPlatformName();
        return platformName;
    }

    private static String getString(String s, String s2) {
        return NbBundle.getMessage(NativeProjectProvider.class, s, s2);
    }

    /*package*/NativeFileSearch getNativeFileSearch() {
        return Lookup.getDefault().lookup(NativeFileSearch.class);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilesPropertiesChanged();
    }
    
    public static final SpiAccessor SPI_ACCESSOR = new SpiAccessor();

    public static final class SpiAccessor {

        private Collection<? extends UserOptionsProvider> uoProviders;
        private Collection<? extends IncludePathExpansionProvider> ipeProviders;

        private synchronized Collection<? extends UserOptionsProvider> getUserOptionsProviders() {
            if (uoProviders == null) {
                uoProviders = Lookup.getDefault().lookupAll(UserOptionsProvider.class);
            }
            return uoProviders;
        }

        private synchronized Collection<? extends IncludePathExpansionProvider> getIncludePathExpansionProviders() {
            if (ipeProviders == null) {
                ipeProviders = Lookup.getDefault().lookupAll(IncludePathExpansionProvider.class);
            }
            return ipeProviders;
        }
        
        private SpiAccessor() {
        }

        public List<IncludePath> getItemUserIncludePaths(List<IncludePath> includes, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
            if(!getUserOptionsProviders().isEmpty()) {
                List<IncludePath> res = new ArrayList<>(includes);
                getUserOptionsProviders().forEach((provider) -> {
                    res.addAll(provider.getItemUserIncludePaths(includes, compilerOptions, compiler, makeConfiguration));
                });
                return res;
            } else {
                return includes;
            }
        }

        public List<String> getItemUserMacros(List<String> macros, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
            if(!getUserOptionsProviders().isEmpty()) {
                List<String> res = new ArrayList<>(macros);
                getUserOptionsProviders().forEach((provider) -> {
                    res.addAll(provider.getItemUserMacros(macros, compilerOptions, compiler, makeConfiguration));
                });
                return res;
            } else {
                return macros;
            }
        }

        public String getImportantFlags(AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
            if(!getUserOptionsProviders().isEmpty()) {
                for (UserOptionsProvider provider : getUserOptionsProviders()) {
                    String itemImportantFlags = provider.getItemImportantFlags(compilerOptions, compiler, makeConfiguration);
                    if (itemImportantFlags != null) {
                        return itemImportantFlags;
                    }
                }
            }
            return null;
        }
        
        public NativeFileItem.LanguageFlavor getLanguageFlavor(AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
            if(!getUserOptionsProviders().isEmpty()) {
                for (UserOptionsProvider provider : getUserOptionsProviders()) {
                    NativeFileItem.LanguageFlavor languageFlavor = provider.getLanguageFlavor(compilerOptions, compiler, makeConfiguration);
                    if(languageFlavor != null && languageFlavor != NativeFileItem.LanguageFlavor.UNKNOWN) {
                        return languageFlavor;
                    }
                }
                return NativeFileItem.LanguageFlavor.UNKNOWN;
            } else {
                return NativeFileItem.LanguageFlavor.UNKNOWN;
            }
        }
        
        public List<IncludePath> expandIncludePaths(List<IncludePath> includes, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
            for (IncludePathExpansionProvider provider : getIncludePathExpansionProviders()) {
                includes = provider.expandIncludePaths(includes, compilerOptions, compiler, makeConfiguration);
            }
            return includes;
        }
    }
    
    public static final class MacroConverter {

        private final MacroExpanderFactory.MacroExpander expander;
        private final Map<String, String> envVariables;

        public MacroConverter(ExecutionEnvironment env) {
            envVariables = new HashMap<>();
            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                    envVariables.putAll(hostInfo.getEnvironment());                    
                } catch (IOException | ConnectionManager.CancellationException ex) {
                    // should never == null occur if isHostInfoAvailable(env) => report
                    Exceptions.printStackTrace(ex);
                }                    
            }
            TempEnv.getInstance(env).addTemporaryEnv(envVariables);
            this.expander = (envVariables == null) ? null : MacroExpanderFactory.getExpander(env, false);
        }

        public String expand(String in) {
            try {
                return expander != null ? expander.expandMacros(in, envVariables) : in;
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            return in;
        }
    }
    
    public static class NativeFileIndexer implements NativeFileItem {

        private final FileObject indexer;
        private final NativeFileItem.Language language;
        private final NativeProjectProvider nativeProject;
        private final String normalizedAbsPath;

        public NativeFileIndexer(NativeProjectProvider nativeProject, FileObject indexer, NativeFileItem.Language language) {
            this.nativeProject = nativeProject;
            this.indexer = indexer;
            this.language = language;
            // #267980 - [newcodemodel] Start entry path should be absolute! 
            this.normalizedAbsPath = CndFileUtils.normalizePath(indexer);
        }

        @Override
        public NativeProject getNativeProject() {
            return nativeProject;
        }

        @Override
        public String getAbsolutePath() {
            return normalizedAbsPath;
        }

        @Override
        public String getName() {
            return indexer.getNameExt();
        }

        @Override
        public FileObject getFileObject() {
            return indexer;
        }

        @Override
        public List<IncludePath> getSystemIncludePaths() {
            return nativeProject.getSystemIncludePaths(language);
        }

        @Override
        public List<IncludePath> getUserIncludePaths() {
            return nativeProject.getUserIncludePaths(language);
        }

        @Override
        public List<FSPath> getSystemIncludeHeaders() {
            return nativeProject.getSystemIncludeHeaders(language);
        }

        @Override
        public List<FSPath> getIncludeFiles() {
            return nativeProject.getIncludeFiles(language);
        }

        @Override
        public List<String> getSystemMacroDefinitions() {
            return nativeProject.getSystemMacroDefinitions(language);
        }

        @Override
        public List<String> getUserMacroDefinitions() {
            return nativeProject.getUserMacroDefinitions(language);
        }

        @Override
        public NativeFileItem.Language getLanguage() {
            return language;
        }

        @Override
        public NativeFileItem.LanguageFlavor getLanguageFlavor() {
            return nativeProject.getLanguageFlavor(language);
        }

        @Override
        public boolean isExcluded() {
            return false;
        }
    }
}
