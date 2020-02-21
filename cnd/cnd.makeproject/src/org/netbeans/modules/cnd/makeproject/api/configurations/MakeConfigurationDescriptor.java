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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectChangeSupport;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.api.utils.CndVisibilityQuery;
import org.netbeans.modules.cnd.makeproject.FullRemoteExtension;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.MakeProjectImpl;
import org.netbeans.modules.cnd.makeproject.MakeProjectTypeImpl;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectUtils;
import org.netbeans.modules.cnd.makeproject.MakeSources;
import org.netbeans.modules.cnd.makeproject.NativeProjectProvider;
import org.netbeans.modules.cnd.makeproject.api.LogicalFolderItemsInfo;
import org.netbeans.modules.cnd.makeproject.api.LogicalFoldersInfo;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectCustomizer;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider.Delta;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider.SnapShot;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item.ItemFactory;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.modules.cnd.makeproject.configurations.CommonConfigurationXMLCodec;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationMakefileWriter;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationXMLWriter;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport;
import org.netbeans.modules.cnd.makeproject.uiapi.LongOperation;
import org.netbeans.modules.cnd.spi.utils.CndNotifier;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.toolchain.support.ToolchainChangeSupport;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.FileObjectFilter;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.dlight.libs.common.PerformanceLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class MakeConfigurationDescriptor extends ConfigurationDescriptor implements ChangeListener {
    
    public static final String EXTERNAL_FILES_FOLDER = "ExternalFiles"; // NOI18N
    public static final String TEST_FILES_FOLDER = "TestFiles"; // NOI18N
    public static final String ROOT_FOLDER = "root"; // NOI18N
    public static final String SOURCE_FILES_FOLDER = "SourceFiles"; // NOI18N
    public static final String HEADER_FILES_FOLDER = "HeaderFiles"; // NOI18N
    public static final String RESOURCE_FILES_FOLDER = "ResourceFiles"; // NOI18N
    public static final String ICON = "org/netbeans/modules/cnd/makeproject/resources/makeProject.gif"; // NOI18N
    public static final Icon MAKEFILE_ICON = ImageUtilities.loadImageIcon(ICON, false); // NOI18N
    public static final String DEFAULT_IGNORE_FOLDERS_PATTERN = "^(nbproject|build|test|tests)$"; // NOI18N
    public static final String DEFAULT_IGNORE_FOLDERS_PATTERN_EXISTING_PROJECT = "^(nbproject)$"; // NOI18N
    public static final String DEFAULT_NO_IGNORE_FOLDERS_PATTERN = "^$"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    private Project project = null;
    
    private final RequestProcessor RP;
    private final RequestProcessor RP_LISTENER;
    
    /*
     * For full remote, configuration base and project base might be different -
     * in 7.0 project base is local (shadow project), configuration base is remote.
     * For any other project, or full remote 7.0.1+ they are the same
     */
    private final FileObject baseDirFO;
    private final FileSystem baseDirFS;
    private final FileObject projectDirFO;
    
    private boolean modified = false;
    private Folder externalFileItems = null;
    private Folder sourceFileItems = null;
    private Folder headerFileItems = null;
    private Folder resourceFileItems = null;
    private Folder testItems = null;
    private Folder rootFolder = null;
    private Map<String, Item> projectItems = null;
    private final List<String> sourceRoots = new ArrayList<>();
    private final List<String> testRoots = new ArrayList<>();
    private final Set<ChangeListener> projectItemsChangeListeners = new HashSet<>();
    private volatile NativeProjectChangeSupport nativeProjectChangeSupport = null;
    public static final String DEFAULT_PROJECT_MAKFILE_NAME = "Makefile"; // NOI18N
    private String projectMakefileName = DEFAULT_PROJECT_MAKFILE_NAME;
    private Task initTask = null;
    private CndVisibilityQuery folderVisibilityQuery = null;
    
    private static ConcurrentHashMap<String, AtomicBoolean> projectWriteLocks = new ConcurrentHashMap<>();

    public MakeConfigurationDescriptor(FileObject projectDirFO) {
        this(null, projectDirFO, projectDirFO);
    }

    public MakeConfigurationDescriptor(FileObject projectDirFO, FileObject baseDirFO) {
        this(null, projectDirFO, baseDirFO);
    }
    
    public MakeConfigurationDescriptor(Project project, FileObject projectDirFO, FileObject baseDirFO) {
        Parameters.notNull("projectDirFO", projectDirFO);
        Parameters.notNull("baseDirFO", baseDirFO);
        this.project = project;
        if (project != null) {
            this.baseDirFO = project.getProjectDirectory();
        } else {
            this.baseDirFO = baseDirFO;
        }
        if (this.baseDirFO == null) {
            throw new IllegalStateException("Exception when getting project folder object"); //NOI18N
        }
        try {
            baseDirFS = baseDirFO.getFileSystem();
        } catch (FileStateInvalidException ex) {
            throw new IllegalStateException("Exception when getting file system for project folder object", ex); //NOI18N
        }
        this.projectDirFO = projectDirFO;
        RP = new RequestProcessor("MakeConfigurationDescriptor " + projectDirFO.getPath(), 1); // NOI18N
        RP_LISTENER =  new RequestProcessor("Add listeners " + projectDirFO.getPath(), 1); // NOI18N
        rootFolder = new Folder(this, null, "root", "root", true, Folder.Kind.ROOT); // NOI18N
        projectItems = new ConcurrentHashMap<>();
        setModified();
    }

    void opened(Interrupter interrupter) {
        if (interrupter != null && interrupter.cancelled()) {
            return;
        }
        ToolchainChangeSupport.addCompilerSetModifiedListener(this);
        //for (Item item : getProjectItems()) {
        //    if (interrupter != null && interrupter.cancelled()) {
        //        return;
        //    }
        //    item.onOpen();
        //}        
        //Task foldersTask = this.initFoldersTask;
        //if (foldersTask != null) {
        //    foldersTask.schedule(0);
        //}
    }

    /*
     * Called when project is being closed
     */
    @Override
    public void closed() {
        ToolchainChangeSupport.removeCompilerSetModifiedListener(this);
        for (Item item : getProjectItems()) {
            item.onClose();
        }
        closed(rootFolder);
    }

    private void closed(Folder folder) {
        if (folder != null) {
            for (Folder f : folder.getAllFolders(false)) {
                f.detachListener();
                f.onClose();
            }
            folder.detachListener();
            folder.onClose();
        }
    }

    public void clean() {
        Configurations confs = getConfs();
        if (confs != null) {
            for (Configuration conf : confs.toArray()) {
                if (conf != null) {
                    conf.clear();
                }
            }
        }
        projectItems.entrySet().forEach((entry) -> {
            entry.getValue().onClose();
        });
        projectItems.clear();
        synchronized (sourceRoots) {
            sourceRoots.clear();
        }
        testRoots.clear();
        rootFolder = new Folder(this, null, "root", "root", true, Folder.Kind.ROOT); // NOI18N;
        sourceFileItems = null;
        headerFileItems = null;
        resourceFileItems = null;
        testItems = null;
        externalFileItems = null;
    }

    public static MakeConfigurationDescriptor getMakeConfigurationDescriptor(Lookup.Provider project) {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp != null) {
            MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
            return makeConfigurationDescriptor;
        } else {
            return null;
        }
    }

    /** NPE-safe method for getting active configuration */
    public MakeConfiguration getActiveConfiguration() {
        Configurations confs = getConfs();
        if (confs != null) {
            MakeConfiguration conf = (MakeConfiguration) confs.getActive();
            if (conf == null) {
                LOGGER.log(Level.FINE, "There are no active configuration in the project descriptor MakeConfigurationDescriptor@{0} for project {1}", new Object[]{System.identityHashCode(this), getBaseDir()}); // NOI18N
            }
            return conf;
        } else {
            LOGGER.log(Level.FINE, "There are no configurations in the project descriptor MakeConfigurationDescriptor@{0} for project {1}", new Object[]{System.identityHashCode(this), getBaseDir()}); // NOI18N
        }
        return null;
    }

    /*
     * One of the compiler sets have changed.
     * Mark project modified. This will trigger all makefiles to be regenerated.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        setModified();
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        if (project == null) {
            try {
                // convert base path into file object
                // we can't use canonical path here, because descriptor created with path like
                // /set/ide/mars/... will be changed by canonization into
                // /net/endif/export/home1/deimos/dev/...
                // and using the canonical path based FileObject in the ProjectManager.getDefault().findProject(fo);
                // will cause creating new MakeProject project
                // because there are no opened /net/endif/export/home1/deimos/dev/... project in system
                // there is only /set/ide/mars/... project in system
                //
                // in fact ProjectManager should solve such problems in more general way
                // because even for java it's possible to open the same project from two different
                // locations /set/ide/mars/... and /net/endif/export/home1/deimos/dev/...
                project = ProjectManager.getDefault().findProject(projectDirFO);
            } catch (Exception e) {
                // Should not happen
                System.err.println("Cannot find project in '" + projectDirFO + "' " + e); // FIXUP // NOI18N
            }
        }
        return project;
    }

    public void init(Configuration def) {
        super.init(new Configuration[]{def}, 0);
        setModified();
    }

    public void setInitTask(Task task) {
        initTask = task;
    }

    /*package-local*/ synchronized void waitInitTask() {
        if (initTask == null) {
            return;
        }
        initTask.waitFinished();
        initTask = null;
    }

    public void initLogicalFolders(Iterator<? extends SourceFolderInfo> sourceFileFolders, boolean createLogicalFolders,
            Iterator<? extends SourceFolderInfo> testFileFolders, Iterator<LogicalFoldersInfo> logicalFolders, Iterator<LogicalFolderItemsInfo> logicalFolderItems, Iterator<String> importantItems, 
            String mainFilePath, PredefinedToolKind mainFileTool, boolean addGeneratedMakefileToLogicalView) {
        if (createLogicalFolders) {
            sourceFileItems = rootFolder.addNewFolder(SOURCE_FILES_FOLDER, getString("SourceFilesTxt"), true, Folder.Kind.SOURCE_LOGICAL_FOLDER);
            headerFileItems = rootFolder.addNewFolder(HEADER_FILES_FOLDER, getString("HeaderFilesTxt"), true, Folder.Kind.SOURCE_LOGICAL_FOLDER);
            resourceFileItems = rootFolder.addNewFolder(RESOURCE_FILES_FOLDER, getString("ResourceFilesTxt"), true, Folder.Kind.SOURCE_LOGICAL_FOLDER);
            testItems = rootFolder.addNewFolder(TEST_FILES_FOLDER, getString("TestsFilesTxt"), false, Folder.Kind.TEST_LOGICAL_FOLDER);
        }
        externalFileItems = rootFolder.addNewFolder(EXTERNAL_FILES_FOLDER, getString("ImportantFilesTxt"), false, Folder.Kind.IMPORTANT_FILES_FOLDER);
//        if (sourceFileFolders != null)
//            setExternalFileItems(sourceFileFolders); // From makefile wrapper wizard
        if (!addGeneratedMakefileToLogicalView) {
            if (!getProjectMakefileName().isEmpty()) {
                externalFileItems.addItem(ItemFactory.getDefault().createInFileSystem(baseDirFS, getProjectMakefileName())); // NOI18N
            }
        }
        if (logicalFolderItems != null) {
            while (logicalFolderItems.hasNext()) {
                LogicalFolderItemsInfo logicalFolderInfo = logicalFolderItems.next();
                Folder f = rootFolder.findFolderByName(logicalFolderInfo.getLogicalFolderName());
                if (f != null) {
                    f.addItem(ItemFactory.getDefault().createInFileSystem(baseDirFS, logicalFolderInfo.getItemPath()));
                }
            }
        }
        if (logicalFolders != null) {
            while (logicalFolders.hasNext()) {
                LogicalFoldersInfo logicalFoldersInfo = logicalFolders.next();
                Folder f;
                if (logicalFoldersInfo.getLogicalFolderName().equals("root")) { // NOI18N
                    f = rootFolder;
                } else {
                    f = rootFolder.findFolderByName(logicalFoldersInfo.getLogicalFolderName());
                }
                if (f != null) {
                    Folder newFolder = new Folder(this, f.getParent(), logicalFoldersInfo.getFolderName(), logicalFoldersInfo.getFolderName(), true, Folder.Kind.SOURCE_LOGICAL_FOLDER);
                    f.addFolder(newFolder, false);
                }
            }
        }
        if (importantItems != null) {
            while (importantItems.hasNext()) {
                externalFileItems.addItem(ItemFactory.getDefault().createInFileSystem(baseDirFS, importantItems.next()));
            }
        }
//        addSourceFilesFromFolders(sourceFileFolders, false, false, true
        // Add main file
        if (mainFilePath != null) {
            Folder srcFolder = rootFolder.findFolderByName(MakeConfigurationDescriptor.SOURCE_FILES_FOLDER);
            if (srcFolder != null) {
                Item added = srcFolder.addItem(ItemFactory.getDefault().createInFileSystem(baseDirFS, mainFilePath));
                PredefinedToolKind defaultToolForItem = added.getDefaultTool(); //Item.getDefaultToolForItem(mainFileTemplate, added);
                if (mainFileTool == PredefinedToolKind.CCCompiler) {
                    //C++ compiler
                    defaultToolForItem = PredefinedToolKind.CCCompiler;
                } else if (mainFileTool == PredefinedToolKind.CCompiler) {
                    //C compiler
                    defaultToolForItem = PredefinedToolKind.CCompiler;
                }
                for (ItemConfiguration ic : added.getItemConfigurations()) {
                    ic.setTool(defaultToolForItem);
                }
            }
        }
        // Handle test folders
        if (testFileFolders != null) {
            while (testFileFolders.hasNext()) {
                SourceFolderInfo sourceFolderInfo = testFileFolders.next();
                addTestRoot(sourceFolderInfo.getFolderName());
            }
        }
        // Handle source root folders
        if (sourceFileFolders != null) {
            while (sourceFileFolders.hasNext()) {
                SourceFolderInfo sourceFolderInfo = sourceFileFolders.next();
                addFilesFromRoot(getLogicalFolders(), sourceFolderInfo.getFileObject(), null, null, false, Folder.Kind.SOURCE_DISK_FOLDER, null);
            }
        }
        setModified();
    }

    public String getProjectMakefileName() {
        return projectMakefileName;
    }

    public void setProjectMakefileName(String projectMakefileName) {
        CndUtils.assertNotNull(projectMakefileName, "project makefile name should not be null"); //NOI18N
        this.projectMakefileName = projectMakefileName;
    }

    /**
     * @deprecated Use org.netbeans.modules.cnd.api.project.NativeProject interface instead.
     */
    @Deprecated
    public void addProjectItemsChangeListener(ChangeListener cl) {
        synchronized (projectItemsChangeListeners) {
            projectItemsChangeListeners.add(cl);
        }
    }

    /**
     * @deprecated Use org.netbeans.modules.cnd.api.project.NativeProject interface instead.
     */
    @Deprecated
    public void removeProjectItemsChangeListener(ChangeListener cl) {
        synchronized (projectItemsChangeListeners) {
            projectItemsChangeListeners.remove(cl);
        }
    }

    public void fireProjectItemsChangeEvent(Item item, int action) {
        Iterator<ChangeListener> it;

        synchronized (projectItemsChangeListeners) {
            it = new HashSet<>(projectItemsChangeListeners).iterator();
        }
        ChangeEvent ev = new ProjectItemChangeEvent(this, item, action);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    public Set<ChangeListener> getProjectItemsChangeListeners() {
        synchronized (projectItemsChangeListeners) {
            return new HashSet<>(projectItemsChangeListeners);
        }
    }

    public void setProjectItemsChangeListeners(Set<ChangeListener> newChangeListeners) {
        synchronized (this.projectItemsChangeListeners) {
            this.projectItemsChangeListeners.clear();
            this.projectItemsChangeListeners.addAll(newChangeListeners);
        }
    }

    @Override
    public String getBaseDir() {
        return baseDirFO.getPath();
    }

    public FileObject getBaseDirFileObject() {
        return baseDirFO;
    }
    
    public FileSystem getBaseDirFileSystem() {
        return baseDirFS;
    }

    public String getProjectDir() {
        return projectDirFO.getPath();
    }

    public FileObject getProjectDirFileObject() {
        return projectDirFO;
    }
    
    public FileObject getNbprojectFileObject() {
        if (projectDirFO != null) {
            try {
                return FileUtil.createFolder(projectDirFO, MakeConfiguration.NBPROJECT_FOLDER);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }    

    public FileObject getNbPrivateProjectFileObject() {
        if (projectDirFO != null) {
            try {
                return FileUtil.createFolder(projectDirFO, MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }    

//    public void setBaseDirFileObject(FileObject baseDirFO) {
//        CndUtils.assertNotNull(baseDirFO, "null base dir file object"); //NOI18N
//        this.baseDirFO = baseDirFO;
//    }

    public Map<String, Item> getProjectItemsMap() {
        return projectItems;
    }

    public void setProjectItemsMap(Map<String, Item> projectItems) {
        this.projectItems = projectItems;
    }

    public void init(Configuration[] confs) {
        super.init(confs, 0);
    }

    @Override
    public Icon getIcon() {
        return MAKEFILE_ICON;
    }

    @Override
    public Configuration defaultConf(String name, int type, String customizerId) {
        String defaultHost = CppUtils.getDefaultDevelopmentHost(projectDirFO);
        Project proj = getProject();
        if (proj != null) {
            ExecutionEnvironment sourceHost = MakeProjectUtils.getSourceFileSystemHost(project);
            if (sourceHost.isRemote()) {
                defaultHost= ExecutionEnvironmentFactory.toUniqueID(sourceHost);
            }
        }
        
        MakeConfiguration c = MakeConfiguration.createConfiguration(FSPath.toFSPath(baseDirFO), name, type, customizerId, defaultHost);
        Item[] items = getProjectItems();
        for (int i = 0; i < items.length; i++) {
            c.addAuxObject(new ItemConfiguration(c, items[i]));
        }
        return c;
    }

    // External File Items
    public void setExternalFileItems(List<String> items) {
        externalFileItems.reset();
        items.forEach((s) -> {
            externalFileItems.addItem(ItemFactory.getDefault().createInFileSystem(baseDirFS, s));
        });
    }

    public void setExternalFileItems(Folder folder) {
        externalFileItems = folder;
    }

    public Folder getExternalFileItems() {
        return externalFileItems;
    }

    public Item[] getExternalFileItemsAsArray() {
        if (externalFileItems != null) {
            return externalFileItems.getItemsAsArray();
        }
        return new Item[]{};
    }

    public Folder getExternalItemFolder() {
        return externalFileItems;
    }

    // Logical Folders
    public Folder getLogicalFolders() {
        return rootFolder;
    }

    public void setLogicalFolders(Folder logicalFolders) {
        this.rootFolder = logicalFolders;
    }

    // Project Files
    public Item[] getProjectItems() {
        List<Item> res = new ArrayList<>(projectItems.values());
        return res.toArray(new Item[res.size()]);
    }

    public Item findItemByPathSlowly(String path) {
        Collection<Item> coll = projectItems.values();
        Iterator<Item> it = coll.iterator();
        Item canonicalItem = null;
        while (it.hasNext()) {
            Item item = it.next();
            if (item.getNormalizedPath().equals(path)) {
                return item;
            }
            if (canonicalItem == null) {
                if (item.getCanonicalPath().equals(path)) {
                    canonicalItem = item;
                }
            }
        }
        return canonicalItem;
    }

    public Item findItemByFileObject(FileObject fileObject) {
        return findProjectItemByPath(fileObject.getPath());
    }

    public Item findProjectItemByPath(String path) {
        // Try first as-is
        path = CndPathUtilities.normalizeSlashes(path);
        Item item = projectItems.get(path);
        if (item == null) {
            // Then try absolute if relative or relative if absolute
            String newPath;
            if (CndPathUtilities.isPathAbsolute(path)) {
                newPath = CndPathUtilities.toRelativePath(getBaseDir(), path);
            } else {
                newPath = CndPathUtilities.toAbsolutePath(getBaseDirFileObject(), path);
            }
            newPath = CndPathUtilities.normalizeSlashes(newPath);
            item = projectItems.get(newPath);
        }
        return item;
    }

    public Item findExternalItemByPath(String path) {
        // Try first as-is
        if (externalFileItems == null) {
            return null;
        }
        path = CndPathUtilities.normalizeSlashes(path);
        Item item = externalFileItems.findItemByPath(path);
        if (item == null) {
            // Then try absolute if relative or relative if absolute
            String newPath;
            if (CndPathUtilities.isPathAbsolute(path)) {
                newPath = CndPathUtilities.toRelativePath(getBaseDir(), path);
            } else {
                newPath = CndPathUtilities.toAbsolutePath(getBaseDirFileObject(), path);
            }
            newPath = CndPathUtilities.normalizeSlashes(newPath);
            item = externalFileItems.findItemByPath(newPath);
        }
        return item;
    }

    public Folder findFolderByPath(String path) {
        return getLogicalFolders().findFolderByPath(path);
    }

    public void addProjectItem(Item item) {
        projectItems.put(item.getPath(), item);
        fireProjectItemsChangeEvent(item, ProjectItemChangeEvent.ITEM_ADDED);
        //getNativeProject().fireFileAdded(item);
    }

    public void fireFilesAdded(List<NativeFileItem> fileItems) {
        if (getNativeProjectChangeSupport() != null) { // once not null, it never becomes null
            getNativeProjectChangeSupport().fireFilesAdded(fileItems);
        }
    }

    public void removeProjectItem(Item item) {
        projectItems.remove(item.getPath());
        fireProjectItemsChangeEvent(item, ProjectItemChangeEvent.ITEM_REMOVED);
        //getNativeProject().fireFileRemoved(item);
    }

    public void fireFilesRemoved(List<NativeFileItem> fileItems) {
        if (getNativeProjectChangeSupport() != null) { // once not null, it never becomes null
            getNativeProjectChangeSupport().fireFilesRemoved(fileItems);
        }
    }

    public void fireFileRenamed(String oldPath, NativeFileItem newFileItem) {
        if (getNativeProjectChangeSupport() != null) { // once not null, it never becomes null
            getNativeProjectChangeSupport().fireFileRenamed(oldPath, newFileItem);
        }
    }

    public void checkForChangedItems(Project project, Folder folder, Item item) {
        if (getNativeProjectChangeSupport() != null) { // once not null, it never becomes null
            checkForChangedItems2(folder, item);
        }
        MakeLogicalViewModel viewModel = project.getLookup().lookup(MakeLogicalViewModel.class);
        if (viewModel != null) {
            viewModel.checkForChangedViewItemNodes(folder, item);
        }
    }

    private void checkForChangedItems2(final Folder folder, final Item item) {
        if (SwingUtilities.isEventDispatchThread()) {
            RP.post(() -> {
                checkForChangedItemsWorker(folder, item);
            });
        } else {
            checkForChangedItemsWorker(folder, item);
        }
    }
    
    private void checkForChangedItemsWorker(Folder folder, Item item) {
        MakeConfiguration makeConfiguration = getActiveConfiguration();
        boolean cFiles = false;
        boolean ccFiles = false;
        boolean libsChanged = false;
        boolean projectChanged = false;
        VectorConfiguration<String> cIncludeDirectories = null;
        BooleanConfiguration cInheritIncludes = null;
        VectorConfiguration<String> cIncludeFiles = null;
        BooleanConfiguration cInheritFiles = null;
        VectorConfiguration<String> cPreprocessorOption = null;
        BooleanConfiguration cInheritMacros = null;
        VectorConfiguration<String> cPreprocessorUndefinedOption = null;
        BooleanConfiguration cInheritUndefinedMacros = null;
        VectorConfiguration<String> ccIncludeDirectories = null;
        BooleanConfiguration ccInheritIncludes = null;
        VectorConfiguration<String> ccIncludeFiles = null;
        BooleanConfiguration ccInheritFiles = null;
        VectorConfiguration<String> ccPreprocessorOption = null;
        BooleanConfiguration ccInheritMacros = null;
        VectorConfiguration<String> ccPreprocessorUndefinedOption = null;
        BooleanConfiguration ccInheritUndefinedMacros = null;
        Item[] items;
        MakeConfigurationDescriptor descriptor = this;

        // Check first whether the development host has changed
        if (makeConfiguration.getDevelopmentHost().getDirty()) {
            makeConfiguration.getDevelopmentHost().setDirty(false);
            items = descriptor.getProjectItems();
            firePropertiesChanged(items, true, true, true);
            return;
        } else if (makeConfiguration.getCompilerSet().getDirty()) {
            // Next, check whether the compiler set has changed
            makeConfiguration.getCompilerSet().setDirty(false);
            items = descriptor.getProjectItems();
            firePropertiesChanged(items, true, true, true);
            return;
        }

        if (folder != null) {
            FolderConfiguration folderConfiguration = folder.getFolderConfiguration(makeConfiguration);
            if (folderConfiguration == null) {
                return;
            }
            cIncludeDirectories = folderConfiguration.getCCompilerConfiguration().getIncludeDirectories();
            cInheritIncludes = folderConfiguration.getCCompilerConfiguration().getInheritIncludes();
            cIncludeFiles = folderConfiguration.getCCompilerConfiguration().getIncludeFiles();
            cInheritFiles = folderConfiguration.getCCompilerConfiguration().getInheritFiles();
            cPreprocessorOption = folderConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
            cInheritMacros = folderConfiguration.getCCompilerConfiguration().getInheritPreprocessor();
            cPreprocessorUndefinedOption = folderConfiguration.getCCompilerConfiguration().getUndefinedPreprocessorConfiguration();
            cInheritUndefinedMacros = folderConfiguration.getCCompilerConfiguration().getInheritUndefinedPreprocessor();
            ccIncludeDirectories = folderConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
            ccInheritIncludes = folderConfiguration.getCCCompilerConfiguration().getInheritIncludes();
            ccIncludeFiles = folderConfiguration.getCCCompilerConfiguration().getIncludeFiles();
            ccInheritFiles = folderConfiguration.getCCCompilerConfiguration().getInheritFiles();
            ccPreprocessorOption = folderConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
            ccInheritMacros = folderConfiguration.getCCCompilerConfiguration().getInheritPreprocessor();
            ccPreprocessorUndefinedOption = folderConfiguration.getCCCompilerConfiguration().getUndefinedPreprocessorConfiguration();
            ccInheritUndefinedMacros = folderConfiguration.getCCCompilerConfiguration().getInheritUndefinedPreprocessor();
            items = folder.getAllItemsAsArray();
            if (folderConfiguration.getCCompilerConfiguration().isCStandardChanged()) {
                folderConfiguration.getCCompilerConfiguration().getCStandard().setDirty(false);
                cFiles = true;
            }
            if (folderConfiguration.getCCCompilerConfiguration().isCppStandardChanged()) {
                folderConfiguration.getCCCompilerConfiguration().getCppStandard().setDirty(false);
                cFiles = true;
            }
        } else if (item != null) {
            ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration);
            if (itemConfiguration == null) {
                return;
            }
            if (itemConfiguration.isToolDirty()) {
                itemConfiguration.setToolDirty(false);
                ccFiles = true;
                cFiles = true;
            }
            if (itemConfiguration.getTool() == PredefinedToolKind.CCompiler) {
                cIncludeDirectories = itemConfiguration.getCCompilerConfiguration().getIncludeDirectories();
                cInheritIncludes = itemConfiguration.getCCompilerConfiguration().getInheritIncludes();
                cIncludeFiles = itemConfiguration.getCCompilerConfiguration().getIncludeFiles();
                cInheritFiles = itemConfiguration.getCCompilerConfiguration().getInheritFiles();
                cInheritMacros = itemConfiguration.getCCompilerConfiguration().getInheritPreprocessor();
                cPreprocessorOption = itemConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
                cPreprocessorUndefinedOption = itemConfiguration.getCCompilerConfiguration().getUndefinedPreprocessorConfiguration();
                cInheritUndefinedMacros = itemConfiguration.getCCompilerConfiguration().getInheritUndefinedPreprocessor();
                if (itemConfiguration.getCCompilerConfiguration().getCommandLineConfiguration().getDirty()){
                    itemConfiguration.getCCompilerConfiguration().getCommandLineConfiguration().setDirty(false);
                    cFiles = true;
                }
                if (!cFiles && itemConfiguration.getCCompilerConfiguration().isCStandardChanged()) {
                    itemConfiguration.getCCompilerConfiguration().getCStandard().setDirty(false);
                    cFiles = true;
                }
                if (!cFiles && itemConfiguration.getCCompilerConfiguration().getSixtyfourBits().getDirty()) {
                    itemConfiguration.getCCompilerConfiguration().getSixtyfourBits().setDirty(false);
                    cFiles = true;
                }                
            }
            if (itemConfiguration.getTool() == PredefinedToolKind.CCCompiler) {
                ccIncludeDirectories = itemConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
                ccInheritIncludes = itemConfiguration.getCCCompilerConfiguration().getInheritIncludes();
                ccIncludeFiles = itemConfiguration.getCCCompilerConfiguration().getIncludeFiles();
                ccInheritFiles = itemConfiguration.getCCCompilerConfiguration().getInheritFiles();
                ccPreprocessorOption = itemConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
                ccInheritMacros = itemConfiguration.getCCCompilerConfiguration().getInheritPreprocessor();
                ccPreprocessorUndefinedOption = itemConfiguration.getCCCompilerConfiguration().getUndefinedPreprocessorConfiguration();
                ccInheritUndefinedMacros = itemConfiguration.getCCCompilerConfiguration().getInheritUndefinedPreprocessor();                
                if (itemConfiguration.getCCCompilerConfiguration().getCommandLineConfiguration().getDirty()){
                    itemConfiguration.getCCCompilerConfiguration().getCommandLineConfiguration().setDirty(false);
                    ccFiles = true;
                }
                if (!ccFiles && itemConfiguration.getCCCompilerConfiguration().isCppStandardChanged()) {
                    itemConfiguration.getCCCompilerConfiguration().getCppStandard().setDirty(false);
                    ccFiles = true;
                }
                if (!ccFiles && itemConfiguration.getCCCompilerConfiguration().getSixtyfourBits().getDirty()) {
                    itemConfiguration.getCCCompilerConfiguration().getSixtyfourBits().setDirty(false);
                    ccFiles = true;
                }                
            }
            if (itemConfiguration.getExcluded().getDirty()) {
                itemConfiguration.getExcluded().setDirty(false);
                ArrayList<NativeFileItem> list = new ArrayList<>();
                list.add(item);
                if (itemConfiguration.getExcluded().getValue()) {
                    fireFilesRemoved(list);
                } else {
                    fireFilesAdded(list);
                }
            }
            items = new Item[]{item};
        } else {
            libsChanged = makeConfiguration.getRequiredProjectsConfiguration().getDirty()
                    || makeConfiguration.getLinkerConfiguration().getLibrariesConfiguration().getDirty();
            cIncludeDirectories = makeConfiguration.getCCompilerConfiguration().getIncludeDirectories();
            cInheritIncludes = makeConfiguration.getCCompilerConfiguration().getInheritIncludes();
            cIncludeFiles = makeConfiguration.getCCompilerConfiguration().getIncludeFiles();
            cInheritFiles = makeConfiguration.getCCompilerConfiguration().getInheritFiles();
            cPreprocessorOption = makeConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
            cPreprocessorUndefinedOption = makeConfiguration.getCCompilerConfiguration().getUndefinedPreprocessorConfiguration();
            if (makeConfiguration.getCCompilerConfiguration().getCommandLineConfiguration().getDirty()){
                makeConfiguration.getCCompilerConfiguration().getCommandLineConfiguration().setDirty(false);
                cFiles = true;
            }
            if (!cFiles && makeConfiguration.getCCompilerConfiguration().isCStandardChanged()) {
                makeConfiguration.getCCompilerConfiguration().getCStandard().setDirty(false);
                cFiles = true;
            }
            if (!cFiles && makeConfiguration.getCCompilerConfiguration().getSixtyfourBits().getDirty()) {
                makeConfiguration.getCCompilerConfiguration().getSixtyfourBits().setDirty(false);
                cFiles = true;
            }
            cInheritMacros = makeConfiguration.getCCompilerConfiguration().getInheritPreprocessor();
            cInheritUndefinedMacros = makeConfiguration.getCCompilerConfiguration().getInheritUndefinedPreprocessor();
            ccIncludeDirectories = makeConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
            ccInheritIncludes = makeConfiguration.getCCCompilerConfiguration().getInheritIncludes();
            ccIncludeFiles = makeConfiguration.getCCCompilerConfiguration().getIncludeFiles();
            ccInheritFiles = makeConfiguration.getCCCompilerConfiguration().getInheritFiles();
            ccPreprocessorOption = makeConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
            ccInheritMacros = makeConfiguration.getCCCompilerConfiguration().getInheritPreprocessor();
            ccPreprocessorUndefinedOption = makeConfiguration.getCCCompilerConfiguration().getUndefinedPreprocessorConfiguration();
            ccInheritUndefinedMacros = makeConfiguration.getCCCompilerConfiguration().getInheritUndefinedPreprocessor(); 
            if (makeConfiguration.getCCCompilerConfiguration().getCommandLineConfiguration().getDirty()){
                makeConfiguration.getCCCompilerConfiguration().getCommandLineConfiguration().setDirty(false);
                ccFiles = true;
            }
            if (makeConfiguration.getCodeAssistanceConfiguration().getIncludeInCA().getDirty()) {
                makeConfiguration.getCodeAssistanceConfiguration().getIncludeInCA().setDirty(false);
                cFiles = true;
                ccFiles = true;
            }
            if (!ccFiles && makeConfiguration.getCCCompilerConfiguration().isCppStandardChanged()) {
                makeConfiguration.getCCCompilerConfiguration().getCppStandard().setDirty(false);
                ccFiles = true;
            }
            if (!ccFiles && makeConfiguration.getCCCompilerConfiguration().getSixtyfourBits().getDirty()) {
                makeConfiguration.getCCCompilerConfiguration().getSixtyfourBits().setDirty(false);
                ccFiles = true;
            }
            items = descriptor.getProjectItems();
            projectChanged = true;
        }

        if (cIncludeDirectories != null
                && (cIncludeDirectories.getDirty() || cIncludeFiles.getDirty() ||cPreprocessorOption.getDirty()
                || cInheritIncludes.getDirty() || cInheritFiles.getDirty()|| cInheritMacros.getDirty() 
                || cPreprocessorUndefinedOption.getDirty() || cInheritUndefinedMacros.getDirty())) {
            cFiles = true;
            cIncludeDirectories.setDirty(false);
            cIncludeFiles.setDirty(false);
            cPreprocessorOption.setDirty(false);
            cInheritIncludes.setDirty(false);
            cInheritFiles.setDirty(false);
            cInheritMacros.setDirty(false);
            cPreprocessorUndefinedOption.setDirty(false);
            cInheritUndefinedMacros.setDirty(false);
        }
        if (ccIncludeDirectories != null
                && (ccIncludeDirectories.getDirty() || ccIncludeFiles.getDirty() || ccPreprocessorOption.getDirty()
                || ccInheritIncludes.getDirty() || ccInheritFiles.getDirty() || ccInheritMacros.getDirty() 
                || ccPreprocessorUndefinedOption.getDirty() || ccInheritUndefinedMacros.getDirty())) {
            ccFiles = true;
            ccIncludeDirectories.setDirty(false);
            ccIncludeFiles.setDirty(false);
            ccPreprocessorOption.setDirty(false);
            ccInheritIncludes.setDirty(false);
            ccInheritFiles.setDirty(false);
            ccInheritMacros.setDirty(false);
            ccPreprocessorUndefinedOption.setDirty(false);
            ccInheritUndefinedMacros.setDirty(false);
        }
        if (libsChanged) {
            makeConfiguration.getRequiredProjectsConfiguration().setDirty(false);
            makeConfiguration.getLinkerConfiguration().getLibrariesConfiguration().setDirty(false);
            cFiles = true;
            ccFiles = true;
        }
        if (cFiles || ccFiles) {
            firePropertiesChanged(items, cFiles, ccFiles, projectChanged);
        }
    }    

    private void firePropertiesChanged(Item[] items, boolean cFiles, boolean ccFiles, boolean projectChanged) {
        NativeProjectProvider.firePropertiesChanged(items, cFiles, ccFiles, projectChanged, getActiveConfiguration(), getNativeProjectChangeSupport());
    }
    
    public void checkForChangedItems(SnapShot delta) {
        if (getNativeProjectChangeSupport() != null) { // once not null, it never becomes null
            checkForChangedItems2(delta);
        }
        MakeLogicalViewModel viewModel = project.getLookup().lookup(MakeLogicalViewModel.class);
        if (viewModel != null) {
            viewModel.checkForChangedViewItemNodes(delta);
        }
    }

    private void checkForChangedItems2(final SnapShot delta) {
        if (SwingUtilities.isEventDispatchThread()) {
            RP.post(() -> {
                checkForChangedItemsWorker(delta);
            });
        } else {
            checkForChangedItemsWorker(delta);
        }
    }

    private void checkForChangedItemsWorker(SnapShot snapShot) {
        if (snapShot instanceof Delta) {
            Delta delta = (Delta) snapShot;
            if (delta.isEmpty()) {
                return;
            }
            List<NativeFileItem> deleted = delta.getDeleted();
            List<NativeFileItem> excluded = delta.getExcluded();
            if (!(deleted.isEmpty() && excluded.isEmpty())) {
                List<NativeFileItem> list = new ArrayList<NativeFileItem>(deleted);
                list.addAll(excluded);
                getNativeProjectChangeSupport().fireFilesRemoved(list);
            }
            List<NativeFileItem> added = delta.getAdded();
            List<NativeFileItem> included = delta.getIncluded();
            if (!(added.isEmpty() && included.isEmpty())) {
                List<NativeFileItem> list = new ArrayList<NativeFileItem>(added);
                list.addAll(included);
                getNativeProjectChangeSupport().fireFilesAdded(list);
            }
            List<NativeFileItem> changed = delta.getChanged();
            if (!changed.isEmpty()) {
                getNativeProjectChangeSupport().fireFilesPropertiesChanged(new ArrayList<NativeFileItem>(changed));
            }
        }
    }
    
    @Override
    public void copyFromProjectDescriptor(ConfigurationDescriptor copyProjectDescriptor) {
        MakeConfigurationDescriptor copyExtProjectDescriptor = (MakeConfigurationDescriptor) copyProjectDescriptor;
        setConfs(copyExtProjectDescriptor.getConfs());
        setProjectMakefileName(copyExtProjectDescriptor.getProjectMakefileName());
        setExternalFileItems(copyExtProjectDescriptor.getExternalFileItems());
        setLogicalFolders(copyExtProjectDescriptor.getLogicalFolders());
        setProjectItemsMap(((MakeConfigurationDescriptor) copyProjectDescriptor).getProjectItemsMap());
        setProjectItemsChangeListeners(((MakeConfigurationDescriptor) copyProjectDescriptor).getProjectItemsChangeListeners());
        setSourceRoots(((MakeConfigurationDescriptor) copyProjectDescriptor).getSourceRoots());
    }

    @Override
    public void assign(ConfigurationDescriptor clonedConfigurationDescriptor) {
        Configuration[] clonedConfs = clonedConfigurationDescriptor.getConfs().toArray();
        Configuration[] newConfs = new Configuration[clonedConfs.length];

        for (int i = 0; i < clonedConfs.length; i++) {
            final Configuration cloneOf = clonedConfs[i].getCloneOf();
            if (cloneOf != null) {
                cloneOf.assign(clonedConfs[i]);
                newConfs[i] = cloneOf;
            } else {
                newConfs[i] = clonedConfs[i];
            }
        }
        init(newConfs, clonedConfigurationDescriptor.getConfs().getActiveAsIndex());
        setProjectMakefileName(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getProjectMakefileName());
        setExternalFileItems(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getExternalFileItems());
        setLogicalFolders(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getLogicalFolders());
        setProjectItemsMap(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getProjectItemsMap());
        setProjectItemsChangeListeners(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getProjectItemsChangeListeners());
        setSourceRoots(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getSourceRoots());
        setTestRoots(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getTestRootsRaw());
        setFolderVisibilityQuery(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getFolderVisibilityQuery().getRegEx());
    }

    @Override
    public ConfigurationDescriptor cloneProjectDescriptor() {
        MakeConfigurationDescriptor clone = new MakeConfigurationDescriptor(
                getProjectDirFileObject(), getBaseDirFileObject());
        super.cloneProjectDescriptor(clone);
        clone.setProjectMakefileName(getProjectMakefileName());
        clone.setExternalFileItems(getExternalFileItems());
        clone.setLogicalFolders(getLogicalFolders());
        clone.setProjectItemsMap(getProjectItemsMap());
        clone.setProjectItemsChangeListeners(getProjectItemsChangeListeners());
        clone.setSourceRoots(getSourceRoots());
        clone.setTestRoots(getTestRootsRaw());
        clone.setFolderVisibilityQuery(getFolderVisibilityQuery().getRegEx());
        return clone;
    }

    /**
     * @deprecated Use org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor.isModified instead.
     */
    @Deprecated
    public boolean getModified() {
        return isModified();
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public final void setModified() {
        setModified(true);
    }

    public void setModified(boolean modified) {
        //System.out.println("setModified - " + modified);
        if (this.modified != modified) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Set configuration "+getBaseDir()+" modified="+modified, new Exception());
            }
            this.modified = modified;
            if (modified && getConfs() != null) {
                Configuration[] confs = getConfs().toArray();
                for (int i = 0; i < confs.length; i++) {
                    ((MakeConfiguration) confs[i]).setRequiredLanguagesDirty(true);
                }
            }
        }
    }

    public void refreshRequiredLanguages() {
        if (getConfs() != null) {
            Configuration[] confs = getConfs().toArray();
            for (int i = 0; i < confs.length; i++) {
                ((MakeConfiguration) confs[i]).reCountLanguages(this);
            }
        }
    }

    public void saveAndClose() {
        save();
        closed();
    }

    @Override
    public boolean save() {
        return save(NbBundle.getMessage(MakeProjectImpl.class, "ProjectNotSaved")); // FIXUP: move message into Bundle for this class after UI freeze
    }

    @Override
    public boolean save(final String extraMessage) {
        SaveRunnable saveRunnable = new SaveRunnable(extraMessage);
        LongOperation.getLongOperation().executeLongOperation2(saveRunnable, 
                getString("MakeConfigurationDescriptor.SaveConfigurationTitle"), // NOI18N
                getString("MakeConfigurationDescriptor.SaveConfigurationText")); // NOI18N
        return saveRunnable.ret;
    }
    
    /**
     * Check needed header extensions and store list in the NB/project properties.
     * @param needAdd list of needed extensions of header files.
     */
    public boolean addAdditionalHeaderExtensions(Collection<String> needAdd) {
        return ((MakeProjectImpl) getProject()).addAdditionalHeaderExtensions(needAdd);
    }

    public CndVisibilityQuery getFolderVisibilityQuery() {
        if (folderVisibilityQuery == null) {
            folderVisibilityQuery = new CndVisibilityQuery(DEFAULT_IGNORE_FOLDERS_PATTERN);
        }
        return folderVisibilityQuery;

    }

    public void setFolderVisibilityQuery(String regex) {
        if (folderVisibilityQuery == null) {
            folderVisibilityQuery = new CndVisibilityQuery(regex);
        } else {
            folderVisibilityQuery.setIgnoredPattern(regex);
        }
    }

    static AtomicBoolean getWriteLock(Project project) {
        AtomicBoolean lock = new AtomicBoolean(false);
        AtomicBoolean oldLock = projectWriteLocks.putIfAbsent(project.getProjectDirectory().getPath(), lock);
        return (oldLock == null) ? lock : oldLock;
    }

    private class SaveRunnable implements Runnable {

        private boolean ret = false;
        private final String extraMessage;

        public SaveRunnable(String extraMessage) {
            this.extraMessage = extraMessage;
        }

        @Override
        public void run() {
            Project project = getProject();
            if (project == null) {
                return;
            }
            AtomicBoolean writeLock = getWriteLock(project);
            synchronized (writeLock) {
                writeLock.set(true);
                FullRemoteExtension.configurationSaving(MakeConfigurationDescriptor.this);
                try {
                    ret = saveWorker(extraMessage);
                } finally {
                    FullRemoteExtension.configurationSaved(MakeConfigurationDescriptor.this, ret);
                    writeLock.set(false);
                }
            }
        }
    }

    private boolean saveWorker(String extraMessage) {

        // Prevent project files corruption.
        if (getState() != State.READY) {
            return false;
        }

        // First check all configurations aux objects if they have changed
        Configuration[] configurations = getConfs().toArray();
        for (int i = 0; i < configurations.length; i++) {
            Configuration conf = configurations[i];
            ConfigurationAuxObject[] auxObjects = conf.getAuxObjects();
            for (int j = 0; j < auxObjects.length; j++) {
                if (auxObjects[j].hasChanged()) {
                    setModified();
                }
                auxObjects[j].clearChanged();
            }
        }

        updateExtensionList();
        if (!isModified()) {
            //if (!MakeProjectUtils.isFullRemote(project)) {
            //    // Always check for missing or out-of-date makefiles. They may not have been generated or have been removed.
            //    new ConfigurationMakefileWriter(this).writeMissingMakefiles();
            //}
            try {
                new ConfigurationMakefileWriter(this).write();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Error writing ConfigurationMakefile", ex);
            }

            ConfigurationPrivateXMLWriter();
            saveProject();

            return true;
        }

        // Check metadata files are writable
        List<String> notOkFiles = new ArrayList<>();
        FileObject[] metadataFileObjects = new FileObject[] {
            getBaseDirFileObject().getFileObject(MakeConfiguration.NBPROJECT_FOLDER),
            getBaseDirFileObject().getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER)
        };
        boolean allOk = true;
        for (FileObject file : metadataFileObjects) {
            if (file == null || !file.isValid()) {
                continue;
            }
            if (!file.canWrite()) {
                allOk = false;
                notOkFiles.add(FileUtil.getFileDisplayName(file));
            }
        }
        if (!allOk) {
            String projectName = CndPathUtilities.getBaseName(getBaseDir());
            StringBuilder text = new StringBuilder();
            text.append(getString("CannotSaveTxt", projectName)); // NOI18N
            for (int i = 0; i < notOkFiles.size(); i++) {
                text.append("\n").append(notOkFiles.get(i)); // NOI18N
            }
            if (extraMessage != null) {
                text.append("\n\n").append(extraMessage); // NOI18N
            }
            if (CndUtils.isStandalone() || SwingUtilities.isEventDispatchThread()) {
                LOGGER.info(text.toString());
            } else {
                CndNotifier.getDefault().notifyError(text.toString());
            }
            return allOk;
        }

        // ALl OK
        FileObject fo = getProjectDirFileObject();
        if (fo != null) {
            LOGGER.log(Level.FINE, "Start of writting project descriptor MakeConfigurationDescriptor@{0} for project {1} @{2}", new Object[]{System.identityHashCode(this), fo.getName(), System.identityHashCode(this.project)}); // NOI18N
            try {
                new ConfigurationXMLWriter(fo, this).write();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Error writing configuration", ex);
            }

            try {
                new ConfigurationMakefileWriter(this).write();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Error writing ConfigurationMakefile", ex);
            }

            ConfigurationProjectXMLWriter();
            ConfigurationPrivateXMLWriter();
            saveProject();
            LOGGER.log(Level.FINE, "End of writting project descriptor MakeConfigurationDescriptor@{0} for project {1} @{2}", new Object[]{System.identityHashCode(this), fo.getName(), System.identityHashCode(this.project)}); // NOI18N
        }

        // Clear flag
        setModified(false);

        return allOk;
    }

    private void ConfigurationProjectXMLWriter() {
        // And save the project
        if (getProject() == null) {
            // See http://www.netbeans.org/issues/show_bug.cgi?id=167577
            // This method uses AntProjectHelper and Project but they are not created (correctly?) under junit tests so this will fail.
            // It means make dependen project and encoding is not correctly stored in project.xml when running tests.
            // Fix is to rewrite this method to not use Project and Ant Helper and use DocumentFactory.createInstance().parse instead to open the document.
            return;
        }
        MakeProjectHelper helper = ((MakeProject) getProject()).getHelper();
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();

        // Remove old project dependency node
        NodeList nodeList = data.getElementsByTagName(MakeProjectTypeImpl.MAKE_DEP_PROJECTS);
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                data.removeChild(node);
            }
        }
        // Create new project dependency node
        Element element = doc.createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.MAKE_DEP_PROJECTS);
        Set<String> subprojectLocations = getSubprojectLocations();
        for (String loc : subprojectLocations) {
            Node n1;
            n1 = doc.createElement(MakeProjectTypeImpl.MAKE_DEP_PROJECT);
            n1.appendChild(doc.createTextNode(loc));
            element.appendChild(n1);
        }
        data.appendChild(element);

        // Remove old source root node
        nodeList = data.getElementsByTagName(MakeProjectTypeImpl.SOURCE_ROOT_LIST_ELEMENT);
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                data.removeChild(node);
            }
        }
        // Create new source root node
        element = doc.createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.SOURCE_ROOT_LIST_ELEMENT);
        List<String> sourceRootist = getSourceRoots();
        for (String loc : sourceRootist) {
            Node n1;
            n1 = doc.createElement(MakeProjectTypeImpl.SOURCE_ROOT_ELEMENT);
            n1.appendChild(doc.createTextNode(loc));
            element.appendChild(n1);
        }
        data.appendChild(element);

        // Remove old configuration node
        nodeList = data.getElementsByTagName(MakeProjectTypeImpl.CONFIGURATION_LIST_ELEMENT);
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                data.removeChild(node);
            }
        }
        // Create new configuration node
        element = doc.createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.CONFIGURATION_LIST_ELEMENT);
        for (Configuration conf : getConfs().getConfigurations()) {
            Element element2 = doc.createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.CONFIGURATION_ELEMENT);
            Node n1;
            n1 = doc.createElement(MakeProjectTypeImpl.CONFIGURATION_NAME_ELEMENT);
            n1.appendChild(doc.createTextNode(conf.getName()));
            element2.appendChild(n1);
            
            n1 = doc.createElement(MakeProjectTypeImpl.CONFIGURATION_TYPE_ELEMENT);
            n1.appendChild(doc.createTextNode("" + ((MakeConfiguration) conf).getConfigurationType().getValue()));
            element2.appendChild(n1);
            
            if (((MakeConfiguration) conf).isCustomConfiguration()) {
                n1 = doc.createElement(MakeProjectTypeImpl.CUSTOMIZERID_ELEMENT);
                n1.appendChild(doc.createTextNode("" + ((MakeConfiguration) conf).getCustomizerId()));
                element2.appendChild(n1);
            }
            
            element.appendChild(element2);
        }
        data.appendChild(element);

        // Create source encoding node
        nodeList = data.getElementsByTagName(MakeProjectTypeImpl.SOURCE_ENCODING_TAG);
        if (nodeList != null && nodeList.getLength() > 0) {
            // Node already there
            Node node = nodeList.item(0);
            node.setTextContent(((MakeProject) getProject()).getSourceEncoding());
        } else {
            // Create node
            Element nativeProjectType = doc.createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.SOURCE_ENCODING_TAG); // NOI18N
            nativeProjectType.appendChild(doc.createTextNode(((MakeProject) getProject()).getSourceEncoding()));
            data.appendChild(nativeProjectType);
        }

        // Remove old formatting node
        nodeList = data.getElementsByTagName(MakeProjectTypeImpl.FORMATTING_STYLE_ELEMENT);
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                data.removeChild(node);
            }
        }
        
        // Create new formatting node
        element = doc.createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.FORMATTING_STYLE_ELEMENT);
        Node n = doc.createElement(MakeProjectTypeImpl.FORMATTING_STYLE_PROJECT_ELEMENT);
        MakeProject.FormattingStyle isProject = ((MakeProject) getProject()).isProjectFormattingStyle();
        switch (isProject){
            case Global:
                n.appendChild(doc.createTextNode("false")); //NOI18N
                break;
            case Project:
                n.appendChild(doc.createTextNode("true")); //NOI18N
                break;
            case ClangFormat:
                n.appendChild(doc.createTextNode("clang-format")); //NOI18N
                break;
        }
        element.appendChild(n);
        if (isProject == MakeProject.FormattingStyle.Project) {
            n = doc.createElement(MakeProjectTypeImpl.C_FORMATTING_STYLE_ELEMENT);
            n.appendChild(doc.createTextNode(((MakeProject) getProject()).getProjectFormattingStyle(MIMENames.C_MIME_TYPE).toExternal()));
            element.appendChild(n);

            n = doc.createElement(MakeProjectTypeImpl.CPP_FORMATTING_STYLE_ELEMENT);
            n.appendChild(doc.createTextNode(((MakeProject) getProject()).getProjectFormattingStyle(MIMENames.CPLUSPLUS_MIME_TYPE).toExternal()));
            element.appendChild(n);

            n = doc.createElement(MakeProjectTypeImpl.HEADER_FORMATTING_STYLE_ELEMENT);
            n.appendChild(doc.createTextNode(((MakeProject) getProject()).getProjectFormattingStyle(MIMENames.HEADER_MIME_TYPE).toExternal()));
            element.appendChild(n);
        } else if (isProject == MakeProject.FormattingStyle.ClangFormat) {
            n = doc.createElement(MakeProjectTypeImpl.CLANG_FORMAT_STYLE_ELEMENT);
            n.appendChild(doc.createTextNode(((MakeProject) getProject()).getProjectFormattingStyle(null).toExternal()));
            element.appendChild(n);
        }
        data.appendChild(element);
        
        helper.putPrimaryConfigurationData(data, true);
    }

    private void ConfigurationPrivateXMLWriter() {
        if (getProject() == null) {
            return;
        }
        MakeProjectHelper helper = ((MakeProject) getProject()).getHelper();
        Element data = helper.getPrimaryConfigurationData(false);
        Document doc = data.getOwnerDocument();

        // Create active configuration type node
        NodeList nodeList = data.getElementsByTagName(MakeProjectTypeImpl.ACTIVE_CONFIGURATION_TYPE_ELEMENT);
        MakeConfiguration active = (MakeConfiguration) getConfs().getActive();
        if (active != null) {
            if (nodeList != null && nodeList.getLength() > 0) {
                // Node already there
                Node node = nodeList.item(0);
                node.setTextContent("" + active.getConfigurationType().getValue());
            } else {
                // Create node
                Element elem = doc.createElementNS(MakeProjectTypeImpl.PRIVATE_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.ACTIVE_CONFIGURATION_TYPE_ELEMENT); // NOI18N
                elem.appendChild(doc.createTextNode("" + active.getConfigurationType().getValue()));
                data.appendChild(elem);
            }
        }

        // Create active configuration type node
        nodeList = data.getElementsByTagName(MakeProjectTypeImpl.ACTIVE_CONFIGURATION_INDEX_ELEMENT);
        if (nodeList != null && nodeList.getLength() > 0) {
            // Node already there
            Node node = nodeList.item(0);
            node.setTextContent("" + getConfs().getActiveAsIndex());
        } else {
            // Create node
            Element elem = doc.createElementNS(MakeProjectTypeImpl.PRIVATE_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.ACTIVE_CONFIGURATION_INDEX_ELEMENT); // NOI18N
            elem.appendChild(doc.createTextNode("" + getConfs().getActiveAsIndex()));
            data.appendChild(elem);
        }

        if (active != null && active.isCustomConfiguration()) {
            // Create custumizerid type node
            nodeList = data.getElementsByTagName(MakeProjectTypeImpl.ACTIVE_CONFIGURATION_CUSTOMIZERID);
            if (nodeList != null && nodeList.getLength() > 0) {
                // Node already there
                Node node = nodeList.item(0);
                node.setTextContent(active.getCustomizerId());
            } else {
                // Create node
                Element elem = doc.createElementNS(MakeProjectTypeImpl.PRIVATE_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.ACTIVE_CONFIGURATION_CUSTOMIZERID); // NOI18N
                elem.appendChild(doc.createTextNode(active.getCustomizerId()));
                data.appendChild(elem);
            }
        }


        helper.putPrimaryConfigurationData(data, false);
    }

    private void saveProject() {
        if (getProject() == null) {
            return;
        }
        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException ex) {
            Set<Entry<Thread, StackTraceElement[]>> entrySet = Thread.getAllStackTraces().entrySet();
            ex.printStackTrace(System.err);
            StringBuilder buf = new StringBuilder();
            buf.append("----- Start thread dump on catching IOException-----\n"); // NOI18N
            for (Map.Entry<Thread, StackTraceElement[]> entry : entrySet) {
                buf.append(entry.getKey().getName()).append('\n'); // NOI18N
                for (StackTraceElement element : entry.getValue()) {
                    buf.append("\tat ").append(element.toString()).append('\n'); // NOI18N
                }
                buf.append('\n'); // NOI18N
            }
            buf.append("-----End thread dump on catching IOException-----\n"); // NOI18N
            LOGGER.log(Level.INFO, buf.toString());
        }
    }

    private void updateExtensionList() {
        Set<String> h = MakeProjectImpl.createExtensionSet();
        Set<String> c = MakeProjectImpl.createExtensionSet();
        Set<String> cpp = MakeProjectImpl.createExtensionSet();
        for (Item item : getProjectItems()) {
            String itemName = item.getName();
            String ext = FileUtil.getExtension(itemName);
            if (ext.length() > 0) {
                if (!h.contains(ext) && !c.contains(ext) && !cpp.contains(ext)) {
                    if (MIMEExtensions.isRegistered(MIMENames.HEADER_MIME_TYPE, ext)) {
                        h.add(ext);
                    } else if (MIMEExtensions.isRegistered(MIMENames.C_MIME_TYPE, ext)) {
                        c.add(ext);
                    } else if (MIMEExtensions.isRegistered(MIMENames.CPLUSPLUS_MIME_TYPE, ext)) {
                        cpp.add(ext);
                    }
                }
            }
        }
        MakeProjectImpl makeProject = (MakeProjectImpl) getProject();
        if (makeProject != null) {
            makeProject.updateExtensions(c, cpp, h);
        }
    }

    /**
     * Returns project locations (rel or abs) or all subprojects in all configurations.
     */
    public Set<String> getSubprojectLocations() {
        Set<String> subProjects = new HashSet<>();

        Configuration[] confs = getConfs().toArray();
        for (int i = 0; i < confs.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration) confs[i];

            if (((MakeConfiguration) confs[i]).isLinkerConfiguration()) {
                LibrariesConfiguration librariesConfiguration = makeConfiguration.getLinkerConfiguration().getLibrariesConfiguration();
                for (LibraryItem item : librariesConfiguration.getValue()) {
                    if (item instanceof LibraryItem.ProjectItem) {
                        LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) item;
                        subProjects.add(projectItem.getMakeArtifact().getProjectLocation());
                    }
                }
            }

            for (LibraryItem.ProjectItem item : makeConfiguration.getRequiredProjectsConfiguration().getValue()) {
                subProjects.add(item.getMakeArtifact().getProjectLocation());
            }
        }

        return subProjects;
    }

    public void addSourceRootRaw(String path) {
        synchronized (sourceRoots) {
            sourceRoots.add(path);
        }
    }

    public void addTestRootRaw(String path) {
        synchronized (testRoots) {
            testRoots.add(path);
        }
    }

    private void addTestRoot(String path) {
        String absPath = CndPathUtilities.toAbsolutePath(getBaseDirFileObject(), path);
        String relPath = CndPathUtilities.normalizeSlashes(CndPathUtilities.toRelativePath(getBaseDir(), path));
        boolean addPath = true;

        //if (CndPathUtilities.isPathAbsolute(relPath) || relPath.startsWith("..") || relPath.startsWith(".")) { // NOI18N
        synchronized (testRoots) {
            if (addPath) {
                String usePath;
                if (ProjectSupport.getPathMode(project) == MakeProjectOptions.PathMode.REL_OR_ABS) {
                    usePath = CndPathUtilities.normalizeSlashes(CndPathUtilities.toAbsoluteOrRelativePath(getBaseDir(), path));
                } else if (ProjectSupport.getPathMode(project) == MakeProjectOptions.PathMode.REL) {
                    usePath = relPath;
                } else {
                    usePath = absPath;
                }

                testRoots.add(usePath);
                setModified();
            }
        }
    }

    /*
     * Add a source new root.
     * Don't add if root inside project
     * Don't add if root is subdir of existing root
     */
    public void addSourceRoot(String path) {
        String absPath = CndPathUtilities.toAbsolutePath(getBaseDirFileObject(), path);
        String canonicalPath;
        try {
            canonicalPath = FileSystemProvider.getCanonicalPath(baseDirFS, absPath);
        } catch (IOException ioe) {
            canonicalPath = null;
        }
        String relPath = CndPathUtilities.normalizeSlashes(CndPathUtilities.toRelativePath(getBaseDir(), path));
        boolean addPath = true;
        ArrayList<String> toBeRemoved = new ArrayList<>();

        synchronized (sourceRoots) {
            if (canonicalPath != null) {
                int canonicalPathLength = canonicalPath.length();
                for (String sourceRoot : sourceRoots) {
                    String absSourceRoot = CndPathUtilities.toAbsolutePath(getBaseDirFileObject(), sourceRoot);
                    String canonicalSourceRoot;
                    try {
                        canonicalSourceRoot = FileSystemProvider.getCanonicalPath(baseDirFS, absSourceRoot);
                    } catch (IOException ioe) {
                        canonicalSourceRoot = null;
                    }
                    if (canonicalSourceRoot != null) {
                        int canonicalSourceRootLength = canonicalSourceRoot.length();
                        if (canonicalSourceRoot.equals(canonicalPath)) {
                            // Identical - don't add
                            addPath = false;
                            break;
                        }
                        if (canonicalSourceRoot.startsWith(canonicalPath) && canonicalSourceRoot.charAt(canonicalPathLength) == File.separatorChar) {
                            // Existing root sub dir of new path - remove existing path
                            toBeRemoved.add(sourceRoot);
                            continue;
                        }
                        if (canonicalPath.startsWith(canonicalSourceRoot) && canonicalPath.charAt(canonicalSourceRootLength) == File.separatorChar) {
                            // Sub dir of existing root - don't add
                            addPath = false;
                            break;
                        }
                    }
                }
            }
            if (toBeRemoved.size() > 0) {
                toBeRemoved.forEach((toRemove) -> {
                    sourceRoots.remove(toRemove);
                });
            }
            if (addPath) {
                String usePath;
                if (ProjectSupport.getPathMode(project) == MakeProjectOptions.PathMode.REL_OR_ABS) {
                    usePath = CndPathUtilities.normalizeSlashes(CndPathUtilities.toAbsoluteOrRelativePath(getBaseDir(), path));
                } else if (ProjectSupport.getPathMode(project) == MakeProjectOptions.PathMode.REL) {
                    usePath = relPath;
                } else {
                    usePath = absPath;
                }

                sourceRoots.add(usePath);
                setModified();
            }
        }
        MakeSources makeSources = getProject().getLookup().lookup(MakeSources.class);
        if (makeSources != null) {
            makeSources.sourceRootsChanged();
        }
    }

    private List<String> getTestRootsRaw() {
        return testRoots;
    }

    public void setSourceRoots(List<String> list) {
        synchronized (sourceRoots) {
            sourceRoots.clear();
            sourceRoots.addAll(list);
        }
    }

    public void setTestRoots(List<String> list) {
        synchronized (testRoots) {
            testRoots.clear();
            testRoots.addAll(list);
        }
    }

//    public void setSourceRootsList(List<String> list) {
//        synchronized (sourceRoots) {
//            sourceRoots.clear();
//            for (String l : list) {
//                addSourceRoot(l);
//            }
//        }
//        MakeSources makeSources = getProject().getLookup().lookup(MakeSources.class);
//        if (makeSources != null) {
//            makeSources.sourceRootsChanged();
//        }
//    }
    private boolean inList(List<String> list, String s) {
        for (String l : list) {
            if (l.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public void checkForChangedSourceRoots(List<String> oldList, List<String> newList) {
        synchronized (sourceRoots) {
            if (oldList.size() == newList.size()) {
                if (oldList.containsAll(newList)) {
                    return;
                }
            }
            sourceRoots.clear();
            newList.forEach((l) -> {
                addSourceRoot(l);
            });

            MakeConfiguration active = (MakeConfiguration) getConfs().getActive(); // FIXUP: need better check
            if (!active.isMakefileConfiguration()) {
                MakeSources makeSources = getProject().getLookup().lookup(MakeSources.class);
                if (makeSources != null) {
                    makeSources.sourceRootsChanged();
                }
                return;
            }

            List<String> toBeAdded = new ArrayList<>();
            for (String s : sourceRoots) {
                if (!inList(oldList, s)) {
                toBeAdded.add(s);
                }
            }
            List<String> toBeRemoved = new ArrayList<>();
            for (String s : oldList) {
                if (!inList(sourceRoots, s)) {
                toBeRemoved.add(s);
                }
            }

            // Add new source root folders
            if (toBeAdded.size() > 0) {
                for (String root : toBeAdded) {
                    FileObject fo = RemoteFileUtil.getFileObject(baseDirFO, root);
                    addFilesFromRoot(getLogicalFolders(), fo, null, null, true, Folder.Kind.SOURCE_DISK_FOLDER, null);
                }
                setModified();
            }

            // Remove old source root folders
            if (toBeRemoved.size() > 0) {
                for (String rootToBeRemoved : toBeRemoved) {
                    List<Folder> rootFolders = getLogicalFolders().getAllFolders(modified); // FIXUP: should probably alays be 'true'
                    for (Folder root : rootFolders) {
                        if (root.isDiskFolder() && root.getRoot() != null && root.getRoot().equals(rootToBeRemoved)) {
                            getLogicalFolders().removeFolderAction(root);
                        }
                    }
                }
                setModified();
            }

            // Notify source root notifiers
            if (toBeAdded.size() > 0 || toBeRemoved.size() > 0) {
                MakeSources makeSources = getProject().getLookup().lookup(MakeSources.class);
                if (makeSources != null) {
                    makeSources.sourceRootsChanged();
                }
            }
        }
    }

    public void checkForChangedTestRoots(List<String> oldList, List<String> newList) {
        synchronized (testRoots) {
            testRoots.clear();
            newList.forEach((l) -> {
                addTestRoot(l);
            });

            List<String> toBeAdded = new ArrayList<>();
            for (String s : testRoots) {
                if (!inList(oldList, s)) {
                    toBeAdded.add(s);
                }
            }
            List<String> toBeRemoved = new ArrayList<>();
            for (String s : oldList) {
                if (!inList(testRoots, s)) {
                    toBeRemoved.add(s);
                }
            }

            // Notify source root notifiers
            // FIXUP: hack to get the tree updated! Need to only refresh actual nodes.
            if (toBeAdded.size() > 0 || toBeRemoved.size() > 0) {
                List<String> sourceRootsSave = new ArrayList<>();
                sourceRootsSave.addAll(sourceRoots);
                checkForChangedSourceRoots(sourceRootsSave, new ArrayList<String>());
                checkForChangedSourceRoots(new ArrayList<String>(), sourceRootsSave);
                setModified();
            }
        }
    }

    public void checkConfigurations(Configuration oldActive, Configuration newActive) {
        getConfs().fireChangedActiveConfiguration(oldActive, newActive);
    }

    /*
     * return copy
     */
    public List<String> getSourceRoots() {
        List<String> copy;
        synchronized (sourceRoots) {
            copy = new ArrayList<>(sourceRoots);
        }
        return copy;
    }

    /*
     * return copy
     */
    public List<String> getTestRoots() {
        List<String> copy;
        synchronized (testRoots) {
            copy = new ArrayList<>(testRoots);
        }
        return copy;
    }

    /*
     * return copy and convert to absolute
     */
    public List<String> getAbsoluteSourceRoots() {
        List<String> copy = new ArrayList<>();
        synchronized (sourceRoots) {
            sourceRoots.forEach((sr) -> {
                copy.add(CndPathUtilities.toAbsolutePath(baseDirFO, sr));
            });
        }
        return copy;
    }

    /*
     * return copy and convert to absolute
     */
    public List<String> getAbsoluteTestRoots() {
        List<String> copy = new ArrayList<>();
        synchronized (testRoots) {
            testRoots.forEach((s) -> {
                copy.add(CndPathUtilities.toAbsolutePath(baseDirFO, s));
            });
        }
        return copy;
    }

    private NativeProjectChangeSupport getNativeProjectChangeSupport() {
        // the cons
        if (nativeProjectChangeSupport == null) {
            FileObject fo = projectDirFO;
            try {
                Project aProject = getProject();
                nativeProjectChangeSupport = aProject.getLookup().lookup(NativeProjectChangeSupport.class);
                if (nativeProjectChangeSupport == null) {
                    NativeProject nativeProject = aProject.getLookup().lookup(NativeProject.class);
                    if (nativeProject instanceof NativeProjectChangeSupport) {
                        nativeProjectChangeSupport = (NativeProjectChangeSupport) nativeProject;
                    }
                }
            } catch (Exception e) {
                // This may be ok. The project could have been removed ....
                System.err.println("getNativeProject " + e);
            }

        }
        return nativeProjectChangeSupport;
    }

    public static class ProjectItemChangeEvent extends ChangeEvent {

        public static final int ITEM_ADDED = 0;
        public static final int ITEM_REMOVED = 1;
        private final Item item;
        private final int action;

        public ProjectItemChangeEvent(Object src, Item item, int action) {
            super(src);
            this.item = item;
            this.action = action;
        }

        public Item getItem() {
            return item;
        }

        public int getAction() {
            return action;
        }
    }

    public void addFilesFromRoot(Folder folder, FileObject dir, ProgressHandle handle, final Interrupter interrupter,
                boolean attachListeners, Folder.Kind folderKind, @NullAllowed FileObjectFilter fileFilter) {
        CndUtils.assertTrueInConsole(folder != null, "null folder"); //NOI18N
        CndUtils.assertTrueInConsole(dir != null, "null directory"); //NOI18N
        if (folder == null || dir == null || !dir.isValid()) {
            return;
        }
        ArrayList<NativeFileItem> filesAdded = new ArrayList<>();
        Folder srcRoot = folder.findFolderByAbsolutePath(dir.getPath());
        String rootPath = null;
        if (folderKind == Folder.Kind.SOURCE_DISK_FOLDER) {
            rootPath = ProjectSupport.toProperPath(baseDirFO, dir, project);
            rootPath = CndPathUtilities.normalizeSlashes(rootPath);
        }
        if (srcRoot == null) {
            String name = dir.getNameExt();
            if (folderKind == Folder.Kind.SOURCE_DISK_FOLDER) {
                name = MakeProjectUtils.getDiskFolderId(project, folder);
            }
            srcRoot = new Folder(folder.getConfigurationDescriptor(), folder, name, dir.getNameExt(), true, folderKind);
            if (folderKind == Folder.Kind.SOURCE_DISK_FOLDER) {
                srcRoot.setRoot(rootPath);
            }
            srcRoot = folder.addFolder(srcRoot, true);
        }
        if (srcRoot.getKind() != folderKind) {
            LOGGER.log(Level.INFO, "Folder {0} has unexpected kind {1}. Expected kind is {2}.", new Object[]{srcRoot.getDisplayName(), srcRoot.getKind(), folderKind}); //NOI18N
        }
        addFilesImpl(srcRoot, dir, handle, interrupter, filesAdded, true, true, fileFilter, true/*all found are included by default*/);
        if (getNativeProjectChangeSupport() != null) { // once not null, it never becomes null
            getNativeProjectChangeSupport().fireFilesAdded(filesAdded);
        }
        if (attachListeners) {
            final Folder aSrcRoot = srcRoot;
            RP_LISTENER.post(() -> {
                aSrcRoot.attachListeners(interrupter);
            });
        }

        addSourceRoot(dir.getPath());
    }

    public Folder addFilesFromRefreshedDir(Folder folder, FileObject dir, boolean attachListeners, boolean setModified, @NullAllowed FileObjectFilter fileFilter, boolean useOldSchemeBehavior) {
        return addFilesFromDirImpl(folder, dir, null, null, attachListeners, setModified, fileFilter, useOldSchemeBehavior);
    }

    public Folder addFilesFromDir(Folder folder, FileObject dir, boolean attachListeners, boolean setModified, @NullAllowed FileObjectFilter fileFilter) {
        return addFilesFromDirImpl(folder, dir, null, null, attachListeners, setModified, fileFilter, false);
    }
    
    private Folder addFilesFromDirImpl(Folder folder, FileObject dir, ProgressHandle handle, Interrupter interrupter,
            boolean attachListeners, boolean setModified, @NullAllowed FileObjectFilter fileFilter, boolean useOldSchemeBehavior) {
        ArrayList<NativeFileItem> filesAdded = new ArrayList<>();
        Folder subFolder = folder.findFolderByName(dir.getNameExt());
        if (subFolder == null) {
            subFolder = new Folder(folder.getConfigurationDescriptor(), folder, dir.getNameExt(), dir.getNameExt(), true, null);
        }
        subFolder = folder.addFolder(subFolder, setModified);
        addFilesImpl(subFolder, dir, handle, interrupter, filesAdded, true, setModified, fileFilter, useOldSchemeBehavior);
        if (getNativeProjectChangeSupport() != null) { // once not null, it never becomes null
            getNativeProjectChangeSupport().fireFilesAdded(filesAdded);
        }
        if (attachListeners) {
            subFolder.attachListeners(interrupter);
        }
        return subFolder;
    }

    private void addFilesImpl(final Folder aFolder, final FileObject aDir, final ProgressHandle handle, Interrupter interrupter,
            final ArrayList<NativeFileItem> filesAdded, final boolean notify, final boolean setModified,
            @NullAllowed final FileObjectFilter fileFilter, final boolean useOldSchemeBehavior) {
        List<String> absTestRootsList = getAbsoluteTestRoots();
        List<AntiLoop> down = new ArrayList<>();
        String canPath;
        try {
            canPath = RemoteFileUtil.getCanonicalPath(aDir);
        } catch (IOException ex) {
            return;
        }
        AntiLoop antiLoop = new AntiLoop(aFolder, aDir, null);
        antiLoop.push(canPath);
        down.add(antiLoop);
        while (!down.isEmpty()) {
            List<AntiLoop> next = new ArrayList<>();
            for (AntiLoop loop : down) {
                FileObject dir = loop.getFile();
                Folder folder = loop.getFolder();
                if (handle != null) {
                    handle.progress("("+filesAdded.size()+") "+dir.getPath()); //NOI18N
                }
                if (interrupter != null && interrupter.cancelled()) {
                    return;
                }
                PerformanceLogger.PerformaceAction lsPerformanceEvent = PerformanceLogger.getLogger().start(Folder.LS_FOLDER_PERFORMANCE_EVENT, dir);
                FileObject[] files = null;
                try {
                    lsPerformanceEvent.setTimeOut(Folder.FS_TIME_OUT);
                    files = dir.getChildren();
                    if (files == null) {
                        continue;
                    }
                } finally {
                    lsPerformanceEvent.log(files == null ? 0 : files.length);
                }

                final boolean hideBinaryFiles = !MakeOptions.getInstance().getViewBinaryFiles();
                for (FileObject file : files) {
                    if (interrupter != null && interrupter.cancelled()) {
                        return;
                    }
                    if (!VisibilityQuery.getDefault().isVisible(file)) {
                        continue;
                    }
                    if (fileFilter != null && !fileFilter.accept(file)) {
                        continue;
                    }
                    if (hideBinaryFiles && CndFileVisibilityQuery.getDefault().isIgnored(file.getNameExt())) {
                        continue;
                    }
                    if (file.isData() && folder.isDiskFolder() && !CndFileVisibilityQuery.getDefault().isVisible(file)) {
                        // be consistent in checks to prevent adding item here followed
                        // by remove in Folder.refreshDiskFolder due to !CndFileVisibilityQuery.getDefault().isIgnored(file)
                        continue;
                    }
                    if (file.isFolder() && getFolderVisibilityQuery().isIgnored(file)) {
                        continue;
                    }
                    if (handle != null) {
                        handle.progress("("+filesAdded.size()+") "+file.getPath()); //NOI18N
                    }
                    if (file.isFolder()) {
                        try {
                            canPath = RemoteFileUtil.getCanonicalPath(file);
                            if (loop.contains(canPath)) {
                                // It seems we have recursive link
                                LOGGER.log(Level.INFO, "Ignore recursive link {0} in folder {1}", new Object[]{canPath, folder.getPath()});
                                continue;
                            }
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, ex.getMessage(), ex);
                            continue;
                        }
                        Folder dirfolder = folder.findFolderByName(file.getNameExt());
                        if (dirfolder == null) {
                            // child folder inherits kind of parent folder
                            if (inList(absTestRootsList, RemoteFileUtil.getAbsolutePath(file)) || folder.isTestLogicalFolder()) {
                                dirfolder = folder.addNewFolder(file.getNameExt(), file.getNameExt(), true, Folder.Kind.TEST_LOGICAL_FOLDER);
                            } else {
                                dirfolder = folder.addNewFolder(file.getNameExt(), file.getNameExt(), true, (Folder.Kind) null);
                            }
                        }
                        dirfolder.markRemoved(false);
                        antiLoop = new AntiLoop(dirfolder, file, loop);
                        antiLoop.push(canPath);
                        next.add(antiLoop);
                    } else {
                        PerformanceLogger.PerformaceAction performanceEvent = PerformanceLogger.getLogger().start(Folder.CREATE_ITEM_PERFORMANCE_EVENT, file);
                        Item item = null;
                        try {
                            performanceEvent.setTimeOut(Folder.FS_TIME_OUT);
                            String path = ProjectSupport.toProperPath(baseDirFO, file, project);
                            item = ItemFactory.getDefault().createInBaseDir(baseDirFO, path);
                            if (folder.addItemFromRefreshDir(item, notify, setModified, useOldSchemeBehavior) == item) {
                                filesAdded.add(item);
                            }
                        } finally {
                            performanceEvent.log(item);
                        }
                    }
                }
            }
            down = next;
        }
    }

    public boolean okToChange() {
        int previousVersion = getVersion();
        if (previousVersion == -1) {
            return true;
        }
        int currentVersion = CommonConfigurationXMLCodec.CURRENT_VERSION;
        if (previousVersion < currentVersion) {
            String txt = getString("UPGRADE_TXT"); //NOI18N
            String autoMassage = getString("UPGRADE_TXT_AUTO");
            if (CndUtils.isStandalone()) {
                System.err.print(txt);
                System.err.println(autoMassage); //NOI18N
            } else {
                String dialogTitle = getString("UPGRADE_DIALOG_TITLE");
                ConfirmSupport.ConfirmVersion confirm = ConfirmSupport.getConfirmVersionFactory().createAndWait(dialogTitle, txt, autoMassage);
                if (confirm == null) {
                    return false;
                }
            }
            setVersion(currentVersion);
        }
        return true;
    }
    
    public boolean hasProjectCustomizer() {
        MakeConfiguration activeConfiguration = getActiveConfiguration();
        if (activeConfiguration != null) {
            return activeConfiguration.isCustomConfiguration();
        }
        return false;
    }
    
    public MakeProjectCustomizer getProjectCustomizer() {
        MakeProjectCustomizer makeprojectCustomizer = null;
        if (hasProjectCustomizer()) {
            makeprojectCustomizer = getActiveConfiguration().getProjectCustomizer();
        }
        return makeprojectCustomizer;
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeConfigurationDescriptor.class, s);
    }

    private static String getString(String s, String a1) {
        return NbBundle.getMessage(MakeConfigurationDescriptor.class, s, a1);
    }
    
    static class AntiLoop {
        private final Folder currentFolder;
        private final FileObject currentFile;
        private final List<String> antiLoop = new ArrayList<>();
        AntiLoop(Folder folder, FileObject file, AntiLoop prev){
            if (prev != null) {
                antiLoop.addAll(prev.antiLoop);
            }
            this.currentFolder = folder;
            this.currentFile = file;
        }
        boolean contains(String canonicalPath) {
            return antiLoop.contains(canonicalPath);
        }
        void push(String canonicalPath) {
            antiLoop.add(canonicalPath);
        }
        Folder getFolder() {
            return currentFolder;
        }
        FileObject getFile() {
            return currentFile;
        }
    }
}
