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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectFileProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item.ItemFactory;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.netbeans.modules.dlight.libs.common.PerformanceLogger;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;

public class Folder implements FileChangeListener, ChangeListener {

    public enum Kind {

        ROOT,
        SOURCE_LOGICAL_FOLDER,
        SOURCE_DISK_FOLDER,
        IMPORTANT_FILES_FOLDER,
        TEST_LOGICAL_FOLDER,
        TEST
    };
    public static final String DEFAULT_FOLDER_NAME = "f"; // NOI18N
    public static final String DEFAULT_FOLDER_DISPLAY_NAME = getString("NewFolderName");
    public static final String DEFAULT_TEST_FOLDER_DISPLAY_NAME = getString("NewTestFolderName");
    public static final int FS_TIME_OUT = 30;
    public static final String LS_FOLDER_PERFORMANCE_EVENT = "LS_FOLDER_PERFORMANCE_EVENT"; //NOI18N
    public static final String CREATE_ITEM_PERFORMANCE_EVENT = "CREATE_ITEM_PERFORMANCE_EVENT"; //NOI18N
    public static final String GET_ITEM_FILE_OBJECT_PERFORMANCE_EVENT = "GET_ITEM_FILE_OBJECT_PERFORMANCE_EVENT"; //NOI18N
    private final MakeConfigurationDescriptor configurationDescriptor;
    private volatile boolean listenerAttached;
    private final String name;
    private String displayName;
    private final Folder parent;
    private final ArrayList<Object> items; // Folder or Item
    private final ReentrantReadWriteLock itemsLock = new ReentrantReadWriteLock();
    private HashMap<String, HashMap<Configuration, DeletedConfiguration>> deletedItems;
    private final Set<ChangeListener> changeListenerList = new WeakSet<>(1);
    private final boolean projectFiles;
    private String id = null;
    private String root;
    private volatile boolean removed;
    final static Logger log = Logger.getLogger("makeproject.folder"); // NOI18N
    private static final boolean checkedLogging = checkLogging();
    private final Kind kind;

    public Folder(MakeConfigurationDescriptor configurationDescriptor, Folder parent, String name, String displayName, boolean projectFiles, Kind kind) {
        this.configurationDescriptor = configurationDescriptor;
        this.parent = parent;
        this.name = name;
        this.displayName = displayName;
        this.projectFiles = projectFiles;
        if (kind == null) {
            if (parent.isDiskFolder()) {
                kind = Kind.SOURCE_DISK_FOLDER;
            } else if (parent.isTestLogicalFolder()) {
                kind = Kind.TEST_LOGICAL_FOLDER;
            } else {
                kind = Kind.SOURCE_LOGICAL_FOLDER;
            }
        }
        this.kind = kind;
        if (this.kind != Kind.SOURCE_DISK_FOLDER) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "------------Non Physical Folder {0}", getPath()); // NOI18N
            }
        }
        this.items = new ArrayList<>();
    }

    /**
     * For internal purpose. Method reduce folder items size
     */
    public void pack() {
        itemsLock.writeLock().lock();
        try {
            items.trimToSize();
        } finally {
            itemsLock.writeLock().unlock();
        }
    }

    public Kind getKind() {
        return kind;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getRoot() {
        return root;
    }

    public void refreshDiskFolderAfterRestoringOldScheme(Interrupter interrupter) {
        if (!UNCHANGED_PROJECT_MODE) {
            refreshDiskFolder(new LinkedList<String>(), true, interrupter);
        }
    }

    public void refreshDiskFolder(Interrupter interrupter) {
        if (!UNCHANGED_PROJECT_MODE) {
            refreshDiskFolder(new LinkedList<String>(), false, interrupter);
        }
    }

    public void forceDiskFolderRefreshAndWait() {
        FileObject folderFile = getThisFolder();
        if (folderFile != null) {
            folderFile.refresh(true);
        }               
    }
    
    private void refreshDiskFolder(LinkedList<String> antiLoop, boolean useOldSchemeBehavior, Interrupter interrupter) {
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "----------refreshDiskFolder {0}", getPath()); // NOI18N
        }
        if (interrupter != null && interrupter.cancelled()) {
            return;
        }
        String rootPath = getRootPath();
        FileObject folderFile = getThisFolder();

        // Folders to be removed
        if (folderFile == null || !folderFile.isValid()
                || !folderFile.isFolder()
                || getConfigurationDescriptor().getFolderVisibilityQuery().isIgnored(folderFile)
                || !VisibilityQuery.getDefault().isVisible(folderFile)) {
            // Remove it plus all subfolders and items from project
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "------------removing folder {0} in {1}", new Object[]{getPath(), getParent().getPath()}); // NOI18N
            }
            getParent().removeFolderImpl(this, true, false);
            return;
        }
        // Items to be removed
        for (Item item : getItemsAsArray()) {
            if (interrupter != null && interrupter.cancelled()) {
                return;
            }
            FileObject fo = item.getFileObject();
            if (fo == null) {
                log.log(Level.INFO, "Null file object for {0}", item.getAbsolutePath()); //NOI18N
                continue;
            }
            if (!fo.isValid()
                    || !fo.isData()
                    || !CndFileVisibilityQuery.getDefault().isVisible(fo)
                    || !VisibilityQuery.getDefault().isVisible(fo)) {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "------------removing item {0} in {1} [{2}]", new Object[]{item.getPath(), getPath(), fo}); // NOI18N
                }
                removeItemImpl(item, true, false);
            }
        }
        try {
            String canonicalPath = RemoteFileUtil.getCanonicalPath(folderFile);
            if (antiLoop.contains(canonicalPath)) {
                // It seems we have recursive link
                log.log(Level.INFO, "Ignore recursive link {0} in folder {1}", new Object[]{canonicalPath, folderFile.getPath()});
                return;
            }
            antiLoop.addLast(canonicalPath);
        } catch (IOException ex) {
            log.log(Level.INFO, ex.getMessage(), ex);
            return;
        }
        try {
            // files/folders to be added
            FileObject files[] = folderFile.getChildren();
            if (files != null) {
                List<FileObject> fileList = new ArrayList<>();
                ArrayList<CharSequence> otherFileList = new ArrayList<>();
                for (int i = 0; i < files.length; i++) {
                    if (interrupter != null && interrupter.cancelled()) {
                        return;
                    }
                    if (!VisibilityQuery.getDefault().isVisible(files[i])) {
                        continue;
                    }
                    if (files[i].isFolder()) {
                        if (getConfigurationDescriptor().getFolderVisibilityQuery().isIgnored(files[i])) {
                            continue;
                        }
                    } else {
                        if (!CndFileVisibilityQuery.getDefault().isVisible(files[i])) {
                            otherFileList.add(FilePathCache.getManager().getString(CharSequences.create(files[i].getPath())));
                            continue;
                        }
                    }
                    fileList.add(files[i]);
                }
                if (interrupter != null && interrupter.cancelled()) {
                    return;
                }
                if (otherFileList.size() > 0) {
                    otherFileList.trimToSize();
                }
                MakeProjectFileProvider.updateSearchBase(configurationDescriptor.getProject(), this, otherFileList);
                for (FileObject file : fileList) {
                    if (interrupter != null && interrupter.cancelled()) {
                        return;
                    }
                    if (file.isFolder()) {
                        try {
                            String canonicalPath = RemoteFileUtil.getCanonicalPath(file);
                            if (antiLoop.contains(canonicalPath)) {
                                // It seems we have recursive link
                                log.log(Level.INFO, "Ignore recursive link {0} in folder {1}", new Object[]{canonicalPath, folderFile.getPath()});
                                continue;
                            }
                        } catch (IOException ex) {
                            log.log(Level.INFO, ex.getMessage(), ex);
                            continue;
                        }
                        Folder existingFolder = findFolderByName(file.getNameExt());
                        if (existingFolder == null) {
                            if (log.isLoggable(Level.FINE)) {
                                log.log(Level.FINE, "------------adding folder {0} in {1}", new Object[]{file.getPath(), getPath()}); // NOI18N
                            }
                            getConfigurationDescriptor().addFilesFromRefreshedDir(this, file, true, true, null, useOldSchemeBehavior);
                        } else {
                            existingFolder.markRemoved(false);
                        }
                    } else {
                        String path = rootPath + '/' + file.getNameExt();
                        if (path.startsWith("./")) { // NOI18N
                            path = path.substring(2);
                        }
                        if (findItemByPath(path) == null) {
                            if (log.isLoggable(Level.FINE)) {
                                log.log(Level.FINE, "------------adding {2} item {0} in {1}", new Object[]{file.getPath(), getPath(), useOldSchemeBehavior ? "included" : "excluded"}); // NOI18N
                            }
                            PerformanceLogger.PerformaceAction performanceEvent = PerformanceLogger.getLogger().start(CREATE_ITEM_PERFORMANCE_EVENT, file);
                            Item item = null;
                            try {
                                performanceEvent.setTimeOut(FS_TIME_OUT);
                                item = ItemFactory.getDefault().createInFileSystem(configurationDescriptor.getBaseDirFileSystem(), path);
                                addItemFromRefreshDir(item, true, true, useOldSchemeBehavior);
                            } finally {
                                performanceEvent.log(item);
                            }
                        }
                    }
                }
                if (interrupter == null || !interrupter.cancelled()) {
                    // Repeast for all sub folders
                    List<Folder> subFolders = getFolders();
                    for (Folder f : subFolders) {
                        if (interrupter != null && interrupter.cancelled()) {
                            return;
                        }
                        f.refreshDiskFolder(antiLoop, useOldSchemeBehavior, interrupter);
                    }
                }
            }
        } finally {
            antiLoop.removeLast();
        }
    }

    public String getDiskName() {
        String diskName = getAbsolutePath();
        if (diskName != null) {
            return CndPathUtilities.getBaseName(diskName);
        }
        return null;
    }
    private static final boolean UNCHANGED_PROJECT_MODE = Boolean.getBoolean("cnd.unchanged.project"); // NOI18N

    public void attachListeners(final Interrupter interrupter) {
        if (interrupter != null && interrupter.cancelled()) {
            return;
        }
        if (configurationDescriptor == null) {
            CndUtils.assertTrueInConsole(false, "null configurationDescriptor for " + this.name);
            return;
        }
        String rootPath = getRootPath();
        if (listenerAttached) {
            CndUtils.assertTrueInConsole(false, "listeners already attached to " + rootPath);
            return;
        }
        FileSystem fileSystem = configurationDescriptor.getBaseDirFileSystem();
        String absRootPath = CndPathUtilities.toAbsolutePath(configurationDescriptor.getBaseDirFileObject(), rootPath);

        if (CndFileUtils.isLocalFileSystem(fileSystem)) {
            // TODO: Remove this check: it was keeped just because of code freeze
            File folderFile = new File(absRootPath);
            if (!folderFile.exists() || !folderFile.isDirectory()) {
                return;
            }
        }

        if (isDiskFolder() && getRoot() != null) {
            VisibilityQuery.getDefault().addChangeListener(this);
            CndFileVisibilityQuery.getDefault().addChangeListener(this);
            getConfigurationDescriptor().getFolderVisibilityQuery().addChangeListener(this);
            if (log.isLoggable(Level.FINER)) {
                log.log(Level.FINER, "-----------attachFilterListener {0}:{1} ({2})", new Object[] {getPath(), absRootPath, System.identityHashCode(this)}); // NOI18N
            }
            try {
                if (!UNCHANGED_PROJECT_MODE) {
                    Callable<Boolean> stopper = null;
                    if (interrupter != null) {
                        stopper = interrupter::cancelled;
                    }
                    FileFilter filter = (File folderFile) -> !getConfigurationDescriptor().getFolderVisibilityQuery().isIgnored(folderFile) && VisibilityQuery.getDefault().isVisible(folderFile);
                    FileSystemProvider.addRecursiveListener(this, fileSystem, absRootPath, filter, stopper);
                }
                listenerAttached = true;
                if (log.isLoggable(Level.FINER)) {
                    log.log(Level.FINER, "-----------attachFileChangeListener {0}:{1} ({2})", new Object[] {getPath(), absRootPath, System.identityHashCode(this)}); // NOI18N
                }
            } catch (IllegalArgumentException iae) {
                // Can happen if trying to attach twice...
                if (log.isLoggable(Level.FINER)) {
                    log.log(Level.FINER, "-----------attachFileChangeListener duplicate error {0}:{1} ({2})", new Object[] {getPath(), absRootPath, System.identityHashCode(this)}); // NOI18N
                }
            }
            return;
        }
        if (interrupter != null && interrupter.cancelled()) {
            return;
        }
        // Repeast for all sub folders
        List<Folder> subFolders = getFolders();
        for (Folder f : subFolders) {
            if (interrupter != null && interrupter.cancelled()) {
                return;
            }
            f.attachListeners(interrupter);
        }
    }

    public void detachListener() {
        if (!listenerAttached) {
            if (isDiskFolder() && getRoot() != null && log.isLoggable(Level.FINER)) {
                log.log(Level.FINER, "----------- skip detaching FileChangeListener {0}: ({1})", new Object[]{getPath(), System.identityHashCode(this)}); // NOI18N
            }
            return;
        }
        if (configurationDescriptor == null) {
            CndUtils.assertTrueInConsole(false, "null configurationDescriptor for " + this.name);
            return;
        }

        String rootPath = getRootPath();
        FileSystem fileSystem = configurationDescriptor.getBaseDirFileSystem();
        String absRootPath = CndPathUtilities.toAbsolutePath(configurationDescriptor.getBaseDirFileObject(), rootPath);

        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINER, "-----------detachFileChangeListener {0}:{1} ({2})", new Object[]{getPath(), absRootPath, System.identityHashCode(this)}); // NOI18N
        }

        try {
            if (!UNCHANGED_PROJECT_MODE) {
                FileSystemProvider.removeRecursiveListener(this, fileSystem, absRootPath);
            }
        } catch(IllegalArgumentException iae) {
            // Can happen if trying to detach twice or folder was GCed...
            log.log(Level.INFO, "-----------detachFileChangeListener not-attached error {0}:{1} ({2})", new Object[] {getPath(), absRootPath, System.identityHashCode(this)}); // NOI18N
        } finally {
            listenerAttached = false;
        }
        if (isDiskFolder() && getRoot() != null) {
            VisibilityQuery.getDefault().removeChangeListener(this);
            CndFileVisibilityQuery.getDefault().removeChangeListener(this);
            getConfigurationDescriptor().getFolderVisibilityQuery().removeChangeListener(this);
            if (log.isLoggable(Level.FINER)) {
                log.log(Level.FINER, "-----------detachFilterListener {0}:{1} ({2})", new Object[]{getPath(), absRootPath, System.identityHashCode(this)}); // NOI18N
            }
        }

    }

    final void onClose() {
        synchronized (changeListenerList) {
            changeListenerList.clear();
        }
        itemsLock.writeLock().lock();
        try {
            items.clear();
        } finally {
            itemsLock.writeLock().unlock();
        }
        deletedItems = null;
    }


    public Folder getParent() {
        return parent;
    }

    public Project getProject() {
        return getConfigurationDescriptor().getProject();
    }

    public String getName() {
        return name;
    }

    private String getSortName() {
        return displayName;
    }

    public String getPath() {
        StringBuilder builder2 = new StringBuilder(32);
        reversePath(this, builder2, false);
        return builder2.toString();
    }

    public String getRootPath() {
        StringBuilder builder2 = new StringBuilder(32);
        reversePath(this, builder2, true);
        String path = builder2.toString();
        return path;
    }

    private void reversePath(Folder folder, StringBuilder builder, boolean fromRoot) {
        Folder aParent = folder.getParent();
        if (aParent != null && aParent.getParent() != null) {
            reversePath(aParent, builder, fromRoot);
            builder.append('/'); // NOI18N
        }
        if (fromRoot && folder.isDiskFolder() && folder.getRoot() != null) {
            builder.append(folder.getRoot());
        } else {
            builder.append(folder.getName());
        }
    }

    public String getDisplayName() {
        // This is dirty fix for #201152. Do not see other way to do this,
        // as Folders instances are always updated and it is impossible
        // to provide them with the right name.
        if (isDiskFolder() && getRoot() != null) {
            String diskName = getDiskName();
            if (diskName != null) {
                return diskName;
            }
        }
        return displayName;
    }

    public final boolean isRemoved() {
        return removed;
    }

    public final void markRemoved(boolean broken) {
        this.removed = broken;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        configurationDescriptor.setModified();
        getParent().reInsertElement(this);
    }

    public MakeConfigurationDescriptor getConfigurationDescriptor() {
        return configurationDescriptor;
    }

    public boolean isProjectFiles() {
        return projectFiles;
    }

    public boolean isDiskFolder() {
        return getKind() == Kind.SOURCE_DISK_FOLDER;
    }

    public boolean isTestLogicalFolder() {
        return getKind() == Kind.TEST_LOGICAL_FOLDER;
    }

    public boolean isTestRootFolder() {
        return isTestLogicalFolder() && getName().equals(MakeConfigurationDescriptor.TEST_FILES_FOLDER);
    }

    public boolean isTest() {
        return getKind() == Kind.TEST;
    }

    public List<Object> getElements() {
        itemsLock.readLock().lock();
        try {
            return new ArrayList<>(items);
        } finally {
            itemsLock.readLock().unlock();
        }
    }

    private void reInsertElement(Object element) {
        itemsLock.writeLock().lock();
        try {
            int index = items.indexOf(element);
            if (index < 0) {
                return;
            }
            items.remove(element);
        } finally {
            itemsLock.writeLock().unlock();
        }
        if (element instanceof Folder) {
            Folder inserted = insertFolderElement((Folder) element);
            inserted.markRemoved(false);
        } else if (element instanceof Item) {
            insertItemElement((Item) element);
        } else {
            assert false;
        }
        fireChangeEvent();
    }

    private Folder insertFolderElement(Folder element) {
        itemsLock.writeLock().lock();
        try {
            element.markRemoved(false);
            if (!element.isProjectFiles()) {
                // Insert last
                items.add(element);
                return element;
            }
            String name1 = element.getSortName();
            int indexAt = items.size() - 1;
            while (indexAt >= 0) {
                Object o = items.get(indexAt);
                if (!(o instanceof Folder)) {
                    indexAt--;
                    continue;
                }
                Folder f = (Folder) o;
                if (!f.isProjectFiles()) {
                    indexAt--;
                    continue;
                }
                String name2 = f.getSortName();
                int compareRes = name1.compareToIgnoreCase(name2);
                if (compareRes == 0) {
                    if (isSameFolder(f, element)) {
                        // already in list
                        f.markRemoved(false);
                        return f;
                    }
                }
                if (compareRes < 0) {
                    indexAt--;
                    continue;
                }
                break;
            }
            items.add(indexAt + 1, element);
            return element;
        } finally {
            itemsLock.writeLock().unlock();
        }
    }

    private boolean isSameFolder(Folder f, Folder element) {
        assert f != null;
        assert element != null;
        return f.getKind() == element.getKind() && f.getRootPath().equals(element.getRootPath());
    }
   
    public static Item insertItemElementInList(ArrayList<Object> list, Item element) {
        String name1 = (element).getSortName();
        int indexAt = list.size() - 1;
        while (indexAt >= 0) {
            Object o = list.get(indexAt);
            if (!(o instanceof Item)) {
                //indexAt--;
                break;
            }
            String name2 = ((Item) o).getSortName();
            int compareRes = name1.compareTo(name2);
            if (compareRes < 0) {
                indexAt--;
                continue;
            }
            break;
        }
        list.add(indexAt + 1, element);
        return element;
    }

    private Item insertItemElement(Item element) {
        itemsLock.writeLock().lock();
        try {
            return insertItemElementInList(items, element);
        } finally {
            itemsLock.writeLock().unlock();
        }
    }

    private Object addElement(Object element, boolean setModified) { // FIXUP: shopuld be private
        // Always keep the vector sorted
        if (element instanceof Item) {
            element = insertItemElement((Item) element);
        } else if (element instanceof Folder) {
            element = insertFolderElement((Folder) element);
        } else {
            assert false;
        }
        fireChangeEvent(this, setModified);
        return element;
    }

    /**
     * add item and make sure it is included in all configurations
     *
     * @param item
     * @return
     */
    public Item addItemAction(Item item) {
        Item added = addItemActionImpl(item, true, false);
        if (added != null) {
            for (ItemConfiguration conf : added.getItemConfigurations()) {
                if (conf != null) {
                    conf.getExcluded().setValue(false);
                }
            }
            MakeLogicalViewModel viewModel = getProject().getLookup().lookup(MakeLogicalViewModel.class);
            if (viewModel != null) {
                viewModel.checkForChangedViewItemNodes(this, item);
            }
        }
        return added;
    }

    private Item addItemActionImpl(Item item, boolean setModified, boolean excludedByDefault) {
        final Item addedItem = addItemImpl(item, true, setModified, excludedByDefault);
        if (addedItem != item) {
            return addedItem; // Nothing new was added
        }
        ArrayList<NativeFileItem> list = new ArrayList<>(1);
        list.add(item);
        configurationDescriptor.fireFilesAdded(list);
        return item;
    }

    public Item addItem(Item item) {
        return addItemImpl(item, true, true, false);
    }

    public Item addItemFromRefreshDir(Item item, boolean notify, boolean setModified, boolean useOldSchemeBehavior) {
        // in new scheme we restored "non-excluded" items => all new are treated as excluded by default
        // in old scheme we didn't store "non-excluded" items, we stored "excluded" instead
        // so to support projects with old scheme we need to consider all new files as "non-excluded"
        return addItemImpl(item, notify, setModified, !useOldSchemeBehavior);
    }

    private synchronized Item addItemImpl(Item item, boolean notify, boolean setModified, boolean excludedByDefault) {
        if (item == null) {
            return null;
        }
        // Check if already in project. Refresh if it's there.
        Item existingItem;
        if (isProjectFiles() && (existingItem = configurationDescriptor.findProjectItemByPath(item.getPath())) != null) {
            //System.err.println("Folder - addItem - item ignored, already added: " + item); // NOI18N  // FIXUP: correct?
            fireChangeEvent(existingItem, setModified);
            return existingItem; // Nothing added
        }
        // Add it to the folder
        item.setFolder(this);
        item = (Item) addElement(item, setModified);

        // Add item to the dataObject's lookup
        if (isProjectFiles() && notify) {
            item.onAddedToFolder(this);
        }

        // Add it to project Items
        if (isProjectFiles()) {
            configurationDescriptor.addProjectItem(item);
            if (setModified) {
                final Project project = configurationDescriptor.getProject();
                if (project != null) {
                    CharSequence itemPath = FilePathCache.getManager().getString(CharSequences.create(item.getAbsolutePath()));
                    MakeProjectFileProvider.addToSearchBase(project, this, itemPath);
                }
                configurationDescriptor.setModified();
            }
            // Add configuration to all configurations
            if (configurationDescriptor.getConfs() == null) {
                return item;
            }
            HashMap<Configuration, DeletedConfiguration> map = null;
            if (deletedItems != null) {
                map = deletedItems.get(item.getPath());
            }
            Configuration[] configurations = configurationDescriptor.getConfs().toArray();
            for (int i = 0; i < configurations.length; i++) {
                // this is hack to initialize folder configuration
                FolderConfiguration folderConfiguration = getFolderConfiguration(configurations[i]);
                DeletedConfiguration old = null;
                if (map != null) {
                    old = map.get(configurations[i]);
                }
                ItemConfiguration ic = new ItemConfiguration(configurations[i], item);
                ic.getExcluded().setValue(excludedByDefault);
                if (old != null && old.ic != null && old.aux != null) {
                    ic.setTool(old.ic.getTool());
                    ic.assignValues(old.aux);
                }
                configurations[i].addAuxObject(ic);
            }
            if (map != null && deletedItems != null) {
                deletedItems.remove(item.getPath());
            }
        }
        
        return item;
    }

    public Folder addFolder(Folder folder, boolean setModified) {
        Folder aFolder = this;
        while (aFolder != null) {
            if (aFolder.equals(folder)) {
                log.log(Level.INFO, "Folder {0} already was added.", folder.getDisplayName()); // NOI18N
                return folder;
            }
            aFolder = aFolder.getParent();
        }
        folder = (Folder) addElement(folder, setModified);
        if (isProjectFiles()) {
            // Add configuration to all configurations
            if (configurationDescriptor.getConfs() == null) {
                return folder;
            }
            Configuration[] configurations = configurationDescriptor.getConfs().toArray();
            for (int i = 0; i < configurations.length; i++) {
                folder.getFolderConfiguration(configurations[i]);
            }
        }
        return folder;
    }

    /**
     * Returns an unique id (String) used to retrive this object from the pool
     * of aux objects
     */
    public String getId() {
        if (id == null) {
            id = "f-" + getPath(); // NOI18N
        }
        return id;
    }

    public FolderConfiguration getFolderConfiguration(Configuration configuration) {
        FolderConfiguration folderConfiguration = null;
        if (isProjectFiles() || isTest() || isTestLogicalFolder()) {
            folderConfiguration = (FolderConfiguration) configuration.getAuxObject(getId());
            if (folderConfiguration == null) {
                CCompilerConfiguration parentCCompilerConfiguration;
                CCCompilerConfiguration parentCCCompilerConfiguration;
                FolderConfiguration parentFolderConfiguration = null;
                if (getParent() != null) {
                    parentFolderConfiguration = getParent().getFolderConfiguration(configuration);
                }
                if (parentFolderConfiguration != null) {
                    parentCCompilerConfiguration = parentFolderConfiguration.getCCompilerConfiguration();
                    parentCCCompilerConfiguration = parentFolderConfiguration.getCCCompilerConfiguration();
                } else {
                    parentCCompilerConfiguration = ((MakeConfiguration) configuration).getCCompilerConfiguration();
                    parentCCCompilerConfiguration = ((MakeConfiguration) configuration).getCCCompilerConfiguration();
                }
                folderConfiguration = new FolderConfiguration(configuration, parentCCompilerConfiguration, parentCCCompilerConfiguration, this);
                configuration.addAuxObject(folderConfiguration);
            }
        }
        return folderConfiguration;
    }

    public FolderConfiguration[] getFolderConfigurations() {
        FolderConfiguration[] folderConfigurations;
        if (configurationDescriptor == null) {
            return new FolderConfiguration[0];
        }
        Configuration[] configurations = configurationDescriptor.getConfs().toArray();
        folderConfigurations = new FolderConfiguration[configurations.length];
        for (int i = 0; i < configurations.length; i++) {
            folderConfigurations[i] = getFolderConfiguration(configurations[i]);
        }
        return folderConfigurations;
    }

    public String suggestedNewTestFolderName() {
        return suggestedName(DEFAULT_TEST_FOLDER_DISPLAY_NAME);
    }

    public String suggestedNewFolderName() {
        return suggestedName(DEFAULT_FOLDER_DISPLAY_NAME);
    }

    public String suggestedName(String template) {
        String aNname;
        String aDisplayName;
        for (int i = 1;; i++) {
            aNname = DEFAULT_FOLDER_NAME + i;
            aDisplayName = template + " " + i; // NOI18N
            if (findFolderByName(aNname) == null) {
                break;
            }
        }
        return aDisplayName;
    }

    public Folder addNewFolder(boolean projectFiles) {
        return addNewFolder(projectFiles, getKind());
    }

    public Folder addNewFolder(boolean projectFiles, Kind kind) {
        String aNname;
        String aDisplayName;
        for (int i = 1;; i++) {
            aNname = DEFAULT_FOLDER_NAME + i;
            aDisplayName = DEFAULT_FOLDER_DISPLAY_NAME + " " + i; // NOI18N
            if (findFolderByName(aNname) == null) {
                break;
            }
        }
        return addNewFolder(aNname, aDisplayName, projectFiles, kind); // NOI18N
    }

    public Folder addNewFolder(String name, String displayName, boolean projectFiles, String kindText) {
        Kind k = null;
        if (kindText != null) {
            if (kindText.equals("IMPORTANT_FILES_FOLDER")) { // NOI18N
                k = Kind.IMPORTANT_FILES_FOLDER;
            } else if (kindText.equals("SOURCE_DISK_FOLDER")) { // NOI18N
                k = Kind.SOURCE_DISK_FOLDER;
            } else if (kindText.equals("SOURCE_LOGICAL_FOLDER")) { // NOI18N
                k = Kind.SOURCE_LOGICAL_FOLDER;
            } else if (kindText.equals("TEST")) { // NOI18N
                k = Kind.TEST;
            } else if (kindText.equals("TEST_LOGICAL_FOLDER")) { // NOI18N
                k = Kind.TEST_LOGICAL_FOLDER;
            }
        }
        return addNewFolder(name, displayName, projectFiles, k);
    }

    public Folder addNewFolder(String name, String displayName, boolean projectFiles, Kind kind) {
        Folder newFolder = new Folder(getConfigurationDescriptor(), this, name, displayName, projectFiles, kind);
        addFolder(newFolder, true);
        return newFolder;
    }

    public boolean removeItemAction(Item item) {
        ArrayList<NativeFileItem> list = new ArrayList<>(1);
        list.add(item);
        boolean ret = removeItemImpl(item, true, true);
        if (isProjectFiles()) {
            configurationDescriptor.fireFilesRemoved(list);
        }
        return ret;
    }

    private boolean removePhysicalItem(Item item, boolean setModified) {
        ArrayList<NativeFileItem> list = new ArrayList<>(1);
        list.add(item);
        boolean ret = removeItemImpl(item, setModified, false);
        if (isProjectFiles()) {
            configurationDescriptor.fireFilesRemoved(list);
        }
        return ret;
    }

    public void renameItemAction(String oldPath, Item newItem) {
        configurationDescriptor.fireFileRenamed(oldPath, newItem);
    }

    public boolean removeItem(Item item) {
        // shouldn't it be the same as removeItemAction?
        return removeItemImpl(item, true, true);
    }

    private boolean removeItemImpl(Item item, boolean setModified, boolean requestForCompleteRemove) {
        if (!requestForCompleteRemove && item.hasImportantAttributes()) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "------------removeItemImpl does NOT REMOVED attributed {0} in {1}", new Object[]{item, getPath()}); // NOI18N
            }
            return false;
        }
        boolean ret = false;
        if (item == null) {
            return false;
        }
        // Remove it from folder
        itemsLock.writeLock().lock();
        try {
            ret = items.remove(item);
        } finally {
            itemsLock.writeLock().unlock();
        }
        if (!ret) {
            fireChangeEvent(this, false);
            return ret;
        }

        // Remove item from the dataObject's lookup
        if (isProjectFiles()) {
            item.onClose();
        }

//	item.setFolder(null);
        if (isProjectFiles()) {
            // Remove it from project Items
            configurationDescriptor.removeProjectItem(item);
            if (setModified) {
                final Project project = configurationDescriptor.getProject();
                if (project != null) {
                    CharSequence itemPath = FilePathCache.getManager().getString(CharSequences.create(item.getAbsolutePath()));
                    MakeProjectFileProvider.removeFromSearchBase(project, this, itemPath);
                }
                configurationDescriptor.setModified();
            }

            // Remove it form all configurations
            if (deletedItems == null) {
                deletedItems = new HashMap<>();
            }
            HashMap<Configuration, DeletedConfiguration> map = new HashMap<>();
            deletedItems.put(item.getPath(), map);
            Configuration[] configurations = configurationDescriptor.getConfs().toArray();
            for (int i = 0; i < configurations.length; i++) {
                DeletedConfiguration del = new DeletedConfiguration();
                del.ic = item.getItemConfiguration(configurations[i]);
                del.aux = configurations[i].removeAuxObject(item.getId()/*ItemConfiguration.getId(item.getPath())*/);
                map.put(configurations[i], del);
            }
        }
        item.setFolder(null);
        fireChangeEvent(this, setModified);
        return ret;
    }

    public boolean removeFolderAction(Folder folder) {
        // this is request from user to remove folder completely
        boolean ret = removeFolderImpl(folder, true, true);
        configurationDescriptor.fireFilesRemoved(folder.getAllItemsAsList());
        return ret;
    }

    private boolean removeFolderImpl(Folder folder, boolean setModified, boolean requestForCompleteRemove) {
        boolean ret = false;
        if (folder != null) {
            folder.getAllFolders(false).forEach((f) -> {
                MakeProjectFileProvider.updateSearchBase(configurationDescriptor.getProject(), f, null);
            });
            MakeProjectFileProvider.updateSearchBase(configurationDescriptor.getProject(), folder, null);
            if (folder.isDiskFolder()) {
                folder.detachListener();
            }
            folder.removeAll(requestForCompleteRemove);
            folder.markRemoved(true);
            if (!requestForCompleteRemove && folder.hasAttributedItems()) {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "------------removeFolderImpl does NOT REMOVED attributed {0} in {1}", new Object[]{folder, getPath()}); // NOI18N
                }
                fireChangeEvent(this, false);
                return false;
            }
            itemsLock.writeLock().lock();
            try {
                ret = items.remove(folder);
            } finally {
                itemsLock.writeLock().unlock();
            }
            if (isProjectFiles()) {
                // Remove it form all configurations
                Configuration[] configurations = configurationDescriptor.getConfs().toArray();
                for (int i = 0; i < configurations.length; i++) {
                    configurations[i].removeAuxObject(folder.getId());
                }
            } else if (isTestLogicalFolder() && folder.isProjectFiles()) {
                // Test folder in logical tests folder
                // should be removed from configuration
                // Is it right that logical tests folder is not project folder?
                Configuration[] configurations = configurationDescriptor.getConfs().toArray();
                for (int i = 0; i < configurations.length; i++) {
                    configurations[i].removeAuxObject(folder.getId());
                }
            }
        }
        if (ret) {
            fireChangeEvent(this, setModified);
        }
        return ret;
    }

    /**
     * Remove all items and folders recursively
     */
    private void removeAll(boolean requestForCompleteRemove) {
        Item[] itemsToRemove = getItemsAsArray();
        Folder[] foldersToRemove = getFoldersAsArray();
        boolean setModified = requestForCompleteRemove;
        for (int i = 0; i < itemsToRemove.length; i++) {
            removeItemImpl(itemsToRemove[i], setModified, requestForCompleteRemove);
        }
        for (int i = 0; i < foldersToRemove.length; i++) {
            removeFolderImpl(foldersToRemove[i], setModified, requestForCompleteRemove);
        }
    }

    public void reset() {
        itemsLock.writeLock().lock();
        try {
            items.clear();
        } finally {
            itemsLock.writeLock().unlock();
        }
        fireChangeEvent();
    }

    public Item findItemByPath(String path) {
        if (path == null) {
            return null;
        }
        Item[] anItems = getItemsAsArray();
        for (int i = 0; i < anItems.length; i++) {
            if (path.equals(anItems[i].getPath())) {
                return anItems[i];
            }
        }
        return null;
    }

    public Item findItemByAbsolutePath(String path) {
        if (path == null) {
            return null;
        }
        Item[] anItems = getItemsAsArray();
        for (int i = 0; i < anItems.length; i++) {
            if (path.equals(anItems[i].getAbsolutePath())) {
                return anItems[i];
            }
        }
        return null;
    }

    public Item findItemByName(String name) {
        if (name == null) {
            return null;
        }
        Item[] anItems = getItemsAsArray();
        for (int i = 0; i < anItems.length; i++) {
            if (name.equals(anItems[i].getName())) {
                return anItems[i];
            }
        }
        return null;
    }

    public Folder findFolderByName(String name) {
        if (name == null) {
            return null;
        }
        Folder[] folders = getFoldersAsArray();
        for (int i = 0; i < folders.length; i++) {
            if (name.equals(folders[i].getName())) {
                return folders[i];
            }
        }
        return null;
    }

    public Folder findFolderByDisplayName(String name) {
        if (name == null) {
            return null;
        }
        Folder[] folders = getFoldersAsArray();
        for (int i = 0; i < folders.length; i++) {
            if (name.equals(folders[i].getDisplayName())) {
                return folders[i];
            }
        }
        return null;
    }

    public Folder findFolderByAbsolutePath(String path) {
        if (path == null) {
            return null;
        }
        for (Folder folder : getFolders()) {
            String absPath = folder.getAbsolutePath();

            if (absPath != null && path.equals(absPath)) {
                return folder;
            }
        }
        return null;
    }

    public Folder findFolderByRelativePath(String path) {
        if (path == null) {
            return null;
        }
        for (Folder folder : getFolders()) {
            String relPath = folder.getRoot();

            if (relPath != null && path.equals(relPath)) {
                return folder;
            }
        }
        return null;
    }

    public String getAbsolutePath() {
        String aRoot = getRoot();
        if (aRoot != null) {
            String absRootPath = CndPathUtilities.toAbsolutePath(configurationDescriptor.getBaseDirFileObject(), getRoot());
            absRootPath = RemoteFileUtil.normalizeAbsolutePath(absRootPath, getProject());
            FileObject folderFile = RemoteFileUtil.getFileObject(absRootPath, getProject());
            if (folderFile != null) {
                return folderFile.getPath();
            }
        }
        return null;
    }

    /*
     * FIXUP: not sure this method is working as intended.....
     */
    public Folder findFolderByPath(String path) {
        int i = path.indexOf('/');
        if (i >= 0) {
            String aName = path.substring(0, i);
            Folder folder = findFolderByName(aName);
            if (folder == null) {
                return null;
            }
            return folder.findFolderByPath(path.substring(i + 1));
        } else {
            return findFolderByName(path);
        }
    }

    public Item[] getItemsAsArray() {
        ArrayList<Item> found = new ArrayList<>();
        Iterator<?> iter = new ArrayList<>(getElements()).iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof Item) {
                found.add((Item) o);
            }
        }
        return found.toArray(new Item[found.size()]);
    }

    public boolean hasAttributedItems() {
        if (!this.isDiskFolder()) {
            return true;
        }
        if (this.getRoot() != null) {
            return true;
        }
        Iterator<?> iter = new ArrayList<>(getElements()).iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof Item) {
                if (((Item) o).hasImportantAttributes()) {
                    return true;
                }
            }
            if (o instanceof Folder) {
                if (((Folder) o).hasAttributedItems()) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<NativeFileItem> getAllItemsAsList() {
        ArrayList<NativeFileItem> found = new ArrayList<>();
        Iterator<?> iter = new ArrayList<>(getElements()).iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof Item) {
                found.add((Item) o);
            }
            if (o instanceof Folder) {
                List<NativeFileItem> anItems = ((Folder) o).getAllItemsAsList();
                found.addAll(anItems);
            }
        }
        return found;
    }

    public Item[] getAllItemsAsArray() {
        List<NativeFileItem> list = getAllItemsAsList();
        return list.toArray(new Item[list.size()]);
    }

    /*
     * Returns a set of all files in this logical folder and subfolders as FileObjetc's
     */
    public Set<FileObject> getAllItemsAsFileObjectSet(boolean projectFilesOnly, FileObjectNameMatcher matcher) {
        LinkedHashSet<FileObject> files = new LinkedHashSet<>();
        getAllItemsAsFileObjectSet(files, projectFilesOnly, matcher);
        return files;
    }

    private void getAllItemsAsFileObjectSet(Set<FileObject> files, boolean projectFilesOnly, FileObjectNameMatcher matcher) {
        if (!projectFilesOnly || isProjectFiles()) {
            if (matcher.isTerminated()) {
                return;
            }
            Iterator<?> iter = new ArrayList<>(getElements()).iterator();
            while (iter.hasNext()) {
                if (matcher.isTerminated()) {
                    return;
                }
                Object item = iter.next();
                if (item instanceof Item) {
                    FileObject fo = ((Item) item).getFileObject();
                    if (fo != null && (matcher == null || matcher.pathMatches(fo))) {
                        files.add(fo);
                    }
                } else if (item instanceof Folder) {
                    ((Folder) item).getAllItemsAsFileObjectSet(files, projectFilesOnly, matcher);
                }
            }
            if (!projectFilesOnly) {
                Set<CharSequence> searchBase = MakeProjectFileProvider.getSearchBase(getProject(), this);
                for(CharSequence path : searchBase) {
                    FSPath fsPath = new FSPath(configurationDescriptor.getBaseDirFileSystem(), path.toString());
                    FileObject fo = fsPath.getFileObject();
                    if (fo != null && (matcher == null || matcher.pathMatches(fo))) {
                        files.add(fo);
                    }
                }
            }
        }
    }

    /*
     * Returns a set of all logical folder in this folder as an array
     */
    public Folder[] getFoldersAsArray() {
        List<Folder> folders = getFolders();
        return folders.toArray(new Folder[folders.size()]);
    }

    /*
     * Returns a set of all logical folder in this folder
     */
    public List<Folder> getFolders() {
        List<Folder> folders = new ArrayList<>();
        Iterator<?> iter = new ArrayList<>(getElements()).iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item instanceof Folder) {
                folders.add((Folder) item);
            }
        }
        return folders;
    }

    /*
     * Returns a set of all logical folder and subfolders in this folder
     */
    public List<Folder> getAllFolders(boolean projectFilesOnly) {
        List<Folder> folders = new ArrayList<>();
        getAllFolders(folders, projectFilesOnly);
        return folders;
    }

    private void getAllFolders(List<Folder> folders, boolean projectFilesOnly) {
        if (!projectFilesOnly || isProjectFiles()) {
            Iterator<?> iter = new ArrayList<>(getElements()).iterator();
            while (iter.hasNext()) {
                Object item = iter.next();
                if (item instanceof Folder) {
                    Folder folder = (Folder) item;
                    if (!projectFilesOnly || folder.isProjectFiles()) {
                        folders.add(folder);
                        folder.getAllFolders(folders, projectFilesOnly);
                    }
                }
            }
        }
    }

    public List<Folder> getAllTests() {
        List<Folder> list = new ArrayList<>();
        getTests(list);
        return list;
    }

    /*
     * recursive!
     */
    private void getTests(List<Folder> list) {
        Iterator<?> iter = new ArrayList<>(getElements()).iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof Folder) {
                if (((Folder) o).isTest()) {
                    list.add((Folder) o);
                }
                ((Folder) o).getTests(list);
            }
        }
    }

    public void addChangeListener(ChangeListener cl) {
        synchronized (changeListenerList) {
            changeListenerList.add(cl);
        }
    }

    public void removeChangeListener(ChangeListener cl) {
        synchronized (changeListenerList) {
            changeListenerList.remove(cl);
        }
    }

    public void refresh(Object source) {
        fireChangeEvent(source, true);
    }

    public void fireChangeEvent() {
        fireChangeEvent(this, true);
    }

    private void fireChangeEvent(Object source, boolean setModified) {
        Iterator<ChangeListener> it;

        synchronized (changeListenerList) {
            it = new HashSet<>(changeListenerList).iterator();
        }
        ChangeEvent ev = new ChangeEvent(source);
        while (it.hasNext()) {
            (it.next()).stateChanged(ev);
        }
        if (setModified) {
            configurationDescriptor.setModified();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINE, "------------stateChanged {0}", getPath()); // NOI18N
        }
        // Happens when filter has changed
        if (isDiskFolder()) {
            refreshDiskFolder(null);
            fireChangeEvent();
        }
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    @Override
    public void fileChanged(FileEvent fe) {
    }

    private FileObject getThisFolder() {
        String rootPath = getRootPath();
        return RemoteFileUtil.getFileObject(configurationDescriptor.getBaseDirFileObject(), rootPath);
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        FileObject fileObject = fe.getFile();
        FileObject thisFolder = getThisFolder();
        FileObject aParent = fileObject.getParent();
        if (aParent.equals(thisFolder)) {
            if (!CndFileVisibilityQuery.getDefault().isVisible(fileObject)
                    || !VisibilityQuery.getDefault().isVisible(fileObject)) {
                fireChangeEvent(this, false);
                return;
            }
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "------------fileDataCreated {0} in {1}", new Object[]{fileObject, getPath()}); // NOI18N
            }
            //if (true) return;
            if (fileObject == null || !fileObject.isValid() || fileObject.isFolder()) {
                return; // FIXUP: error
            }
            if (!FileFilterFactory.getAllSourceFileFilter().accept(fileObject)) {
                fireChangeEvent(this, false);
                return;
            }
            String itemPath = fileObject.getPath();
            itemPath = CndPathUtilities.toRelativePath(getConfigurationDescriptor().getBaseDir(), itemPath);
            itemPath = CndPathUtilities.normalizeSlashes(itemPath);
            Item item = ItemFactory.getDefault().createInFileSystem(configurationDescriptor.getBaseDirFileSystem(), itemPath);
            addItemActionImpl(item, true, true);
        } else {
            while (aParent != null && aParent.isValid() && !aParent.isRoot()) {
                if (aParent.equals(thisFolder)) {
                    getFolders().forEach((folder) -> {
                        folder.fileDataCreated(fe);
                    });
                    return;
                }
                aParent = aParent.getParent();
            }
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        FileObject fileObject = fe.getFile();
        assert fileObject.isFolder();
        FileObject thisFolder = getThisFolder();
        FileObject aParent = fileObject.getParent();
        if (aParent.equals(thisFolder)) {
            if (getConfigurationDescriptor().getFolderVisibilityQuery().isIgnored(fileObject)
                    || !VisibilityQuery.getDefault().isVisible(fileObject)) {
                fireChangeEvent(this, false);
                return;
            }
            if (fileObject.isValid()) {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "------------fileFolderCreated {0} in {1}", new Object[]{fileObject, getPath()}); // NOI18N
                }
                if (fileObject == null || !fileObject.isValid() || !fileObject.isFolder()) {
                    // It is possible that short-living temporary folder is created while building project
                    return;
                }
                /*Folder top =*/ getConfigurationDescriptor().addFilesFromDir(this, fileObject, true, true, null);
            }
        } else {
            while (aParent != null && aParent.isValid() && !aParent.isRoot()) {
                if (aParent.equals(thisFolder)) {
                    getFolders().forEach((folder) -> {
                        folder.fileFolderCreated(fe);
                    });
                    return;
                }
                aParent = aParent.getParent();
            }
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        FileObject fileObject = fe.getFile();
        FileObject thisFolder = getThisFolder();
        FileObject aParent = fileObject.getParent();
        if (aParent.equals(thisFolder)) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "------------fileDeleted {0} in {1}", new Object[]{fileObject, getPath()}); // NOI18N
            }
            //if (true) return;
            String path = getRootPath() + '/' + fileObject.getNameExt();
            if (path.startsWith("./")) { // NOI18N
                path = path.substring(2);
            }
            // Try item first
            Item item;
            if (CndPathUtilities.isPathAbsolute(path)) {
                item = findItemByAbsolutePath(path);
            } else {
                item = findItemByPath(path);
            }

            if (item != null) {
                removePhysicalItem(item, true);
                return;
            }
            // then folder
            Folder folder = findFolderByName(fileObject.getNameExt());
            if (folder != null) {
                removeFolderImpl(folder, true, false);
                return;
            }
            fireChangeEvent(this, false);
        } else {
            while (aParent != null && aParent.isValid() && !aParent.isRoot()) {
                if (aParent.equals(thisFolder)) {
                    getFolders().forEach((folder) -> {
                        folder.fileDeleted(fe);
                    });
                    return;
                }
                aParent = aParent.getParent();
            }
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        FileObject fileObject = fe.getFile();
        FileObject thisFolder = getThisFolder();
        FileObject aParent = fileObject.getParent();
        if (aParent.equals(thisFolder)) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "------------fileRenamed {0} in {1}", new Object[]{fileObject.getPath(), getPath()}); // NOI18N
            }
            // Try only folders. Items are taken care of in Item.propertyChange takes care of it....
            Folder folder = findFolderByName(fe.getName());
            if (folder != null && folder.isDiskFolder()) {
                // Add new Folder
                Folder top = getConfigurationDescriptor().addFilesFromDir(this, fileObject, true, false, null);
                // Copy all configurations
                copyConfigurations(folder, top);
                // Remove old folder
                removeFolderAction(folder);
            }
        } else {
            while (aParent != null && aParent.isValid() && !aParent.isRoot()) {
                if (aParent.equals(thisFolder)) {
                    getFolders().forEach((folder) -> {
                        folder.fileRenamed(fe);
                    });
                }
                aParent = aParent.getParent();
            }
        }
    }

    private void copyConfigurations(Folder src) {
        MakeConfigurationDescriptor makeConfigurationDescriptor = getConfigurationDescriptor();
        if (makeConfigurationDescriptor == null) {
            return;
        }

        for (Configuration conf : makeConfigurationDescriptor.getConfs().toArray()) {
            FolderConfiguration srcFolderConfiguration = src.getFolderConfiguration(conf);
            FolderConfiguration dstFolderConfiguration = getFolderConfiguration(conf);
            if (srcFolderConfiguration != null && dstFolderConfiguration != null) {
                dstFolderConfiguration.assignValues(srcFolderConfiguration);
            }
        }
    }

    private static void copyConfigurations(Folder oldFolder, Folder newFolder) {
        newFolder.copyConfigurations(oldFolder);
        // Copy item configurations
        Item oldItems[] = oldFolder.getItemsAsArray();
        for (Item oldItem : oldItems) {
            Item newItem = newFolder.findItemByName(oldItem.getName());
            if (newItem != null) {
                newItem.copyConfigurations(oldItem);
            }
        }
        // copy subfolder cnfigurations
        Folder srcFolders[] = oldFolder.getFoldersAsArray();
        for (Folder srcFolder : srcFolders) {
            Folder dstFolder = newFolder.findFolderByName(srcFolder.getName());
            if (dstFolder != null) {
                dstFolder.copyConfigurations(srcFolder);
            }
        }
    }

    private static boolean checkLogging() {
        if (checkedLogging) {
            return true;
        }
        String logProp = System.getProperty("makeproject.folder"); // NOI18N
        if (logProp != null) {
            if (logProp.equals("FINE")) { // NOI18N
                log.setLevel(Level.FINE);
            } else if (logProp.equals("FINER")) { // NOI18N
                log.setLevel(Level.FINER);
            } else if (logProp.equals("FINEST")) { // NOI18N
                log.setLevel(Level.FINEST);
            }
        }
        return true;
    }

    /**
     * Look up i18n strings here
     */
    private static String getString(String s) {
        return NbBundle.getMessage(Folder.class, s);
    }

    @Override
    public String toString() {
        return (removed ? "[removed]" : "") + name + "{[" + getPath() + "][" + getRootPath() + "]}"; // NOI18N
    }

    private static final class DeletedConfiguration {

        private ConfigurationAuxObject aux;
        private ItemConfiguration ic;
    }

    public static interface FileObjectNameMatcher {

        /**
         * @param fileObject File whose name or path should be matched.
         * @return True if file path matches required criteria, false otherwise.
         */
        boolean pathMatches(FileObject fileObject);

        boolean isTerminated();
    }
}
