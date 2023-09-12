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

package org.netbeans.modules.editor.settings.storage;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.netbeans.modules.editor.settings.storage.spi.StorageDescription;
import org.netbeans.modules.editor.settings.storage.spi.StorageFilter;
import org.netbeans.modules.editor.settings.storage.spi.StorageReader;
import org.netbeans.modules.editor.settings.storage.spi.StorageWriter;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Vita Stejskal
 */
public final class StorageImpl <K extends Object, V extends Object> {
    
    // -J-Dorg.netbeans.modules.editor.settings.storage.StorageImpl.level=FINE
    private static final Logger LOG = Logger.getLogger(StorageImpl.class.getName());

    public StorageImpl(StorageDescription<K, V> sd, Callable<Void> callback) {
        this.storageDescription = sd;
        this.dataChangedCallback = callback;
        this.baseFolder = FileUtil.getConfigFile("Editors"); //NOI18N
        try {
            this.tracker = new FilesystemTracker(FileUtil.getConfigRoot().getFileSystem());
        } catch (FileStateInvalidException ex) {
            // something is terribly wrong, because we can't access SystemFileSystem
            throw new IllegalStateException(ex);
        }
        Filters.registerCallback(this);
    }

    public Map<K, V> load(MimePath mimePath, String profile, boolean defaults) throws IOException {
        assert mimePath != null : "The parameter mimePath must not be null"; //NOI18N
        if (storageDescription.isUsingProfiles()) {
            assert profile != null : "The parameter profile must not be null"; //NOI18N
        } else {
            assert profile == null : "The '" + storageDescription.getId() + "' settings type does not use profiles."; //NOI18N
        }

        synchronized (lock) {
            Map<K, V> data;
            Map<CacheKey, Map<K, V>> profilesData = profilesCache.get(mimePath);
            CacheKey cacheKey = cacheKey(profile, defaults);
            
            if (profilesData == null) {
                data = null;
                profilesData = new HashMap<CacheKey, Map<K, V>>();
                profilesCache.put(mimePath, profilesData);
            } else {
                data = profilesData.get(cacheKey);
            }

            if (data == null) {
                data = _load(mimePath, profile, defaults);
                filterAfterLoad(data, mimePath, profile, defaults);
                data = Collections.unmodifiableMap(data);
                profilesData.put(cacheKey, data);
            }
            
            return data;
        }
    }
    
    public void save(MimePath mimePath, String profile, boolean defaults, Map<K, V> data) throws IOException {
        assert mimePath != null : "The parameter mimePath must not be null"; //NOI18N
        if (storageDescription.isUsingProfiles()) {
            assert profile != null : "The parameter profile must not be null"; //NOI18N
        } else {
            assert profile == null : "The '" + storageDescription.getId() + "' settings type does not use profiles."; //NOI18N
        }

        synchronized (lock) {
            CacheKey cacheKey = null;
            Map<CacheKey, Map<K, V>> profilesData = profilesCache.get(mimePath);
            if (profilesData == null) {
                profilesData = new HashMap<CacheKey, Map<K, V>>();
                profilesCache.put(mimePath, profilesData);
            } else {
                cacheKey = cacheKey(profile, defaults);
                Map<K, V> cacheData = profilesData.get(cacheKey);
                if (cacheData != null && !Utils.quickDiff(cacheData, data)) {
                    // no differences, no need to save or update the cache
                    return;
                }
            }

            Map<K, V> dataForSave = new HashMap<K, V>(data);
            filterBeforeSave(dataForSave, mimePath, profile, defaults);
            _save(mimePath, profile, defaults, dataForSave);
            profilesCache.clear();
        }
    }

    public void delete(MimePath mimePath, String profile, boolean defaults) throws IOException {
        assert mimePath != null : "The parameter mimePath must not be null"; //NOI18N
        if (storageDescription.isUsingProfiles()) {
            assert profile != null : "The parameter profile must not be null"; //NOI18N
        } else {
            assert profile == null : "The '" + storageDescription.getId() + "' settings type does not use profiles."; //NOI18N
        }
        
        synchronized (lock) {
            Map<CacheKey, Map<K, V>> profilesData = profilesCache.get(mimePath);
            if (profilesData != null) {
                profilesData.remove(cacheKey(profile, defaults));
            }
            _delete(mimePath, profile, defaults);
        }
    }

    public void refresh() {
        synchronized (lock) {
            profilesCache.clear();
        }
        
        // notify about possible changes in the cached data
        if (dataChangedCallback != null) {
            try {
                dataChangedCallback.call();
            } catch (Exception e) {
                // ignore, the callback is not supposed to throw anything
            }
        }
    }
    
    public static interface Operations<K extends Object, V extends Object> {
        public Map<K, V> load(MimePath mimePath, String profile, boolean defaults) throws IOException;
        public boolean save(MimePath mimePath, String profile, boolean defaults, Map<K, V> data, Map<K, V> defaultData) throws IOException;
        public void delete(MimePath mimePath, String profile, boolean defaults) throws IOException;
    } // End of Operations interface
    
    // ------------------------------------------
    // private implementation
    // ------------------------------------------
    
    private final StorageDescription<K, V> storageDescription;
    private final Callable<Void> dataChangedCallback;
    private final FileObject baseFolder;
    private final FilesystemTracker tracker;
    
    private final Object lock = new String("StorageImpl.lock"); //NOI18N
    private final Map<MimePath, Map<CacheKey, Map<K, V>>> profilesCache = new WeakHashMap<MimePath, Map<CacheKey, Map<K, V>>>();

    private static volatile boolean ignoreFilesystemEvents = false;
    
    /* test */ static void ignoreFilesystemEvents(boolean ignore) {
        ignoreFilesystemEvents = ignore;
    }

    private List<Object []> scan(MimePath mimePath, String profile, boolean scanModules, boolean scanUsers) {
        Map<String, List<Object []>> files = new HashMap<String, List<Object []>>();

        SettingsType.getLocator(storageDescription).scan(baseFolder, mimePath.getPath(), profile, true, scanModules, scanUsers, mimePath.size() > 1, files);
        assert files.size() <= 1 : "Too many results in the scan"; //NOI18N
        
        return files.get(profile);
    }
    
    private Map<K, V> _load(MimePath mimePath, String profile, boolean defaults) throws IOException {
        if (storageDescription instanceof Operations) {
            @SuppressWarnings("unchecked")
            Operations<K, V> operations = (Operations<K, V>) storageDescription;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Forwarding loading of '" + storageDescription.getId() + "' to: " + operations); //NOI18N
            }
            return operations.load(mimePath, profile, defaults);
        } else {
            // Perform the operation
            List<Object []> profileInfos = scan(mimePath, profile, true, !defaults);
            Map<K, V> map = new HashMap<K, V>();

            if (profileInfos != null) {
                for(Object [] info : profileInfos) {
                    assert info.length == 5;
                    FileObject profileHome = (FileObject) info[0];
                    FileObject settingFile = (FileObject) info[1];
                    boolean modulesFile = ((Boolean) info[2]).booleanValue();
                    FileObject linkTarget = (FileObject) info[3];
                    boolean legacyFile = ((Boolean) info[4]).booleanValue();
                    
                    if (linkTarget != null) {
                        // link to another mimetype
                        MimePath linkedMimePath = MimePath.parse(linkTarget.getPath().substring(baseFolder.getPath().length() + 1));
                        assert linkedMimePath != mimePath : "linkedMimePath should not be the same as the original one"; //NOI18N
                        
                        if (linkedMimePath.size() == 1) {
                            Map<K, V> linkedMap = load(linkedMimePath, profile, defaults);
                            map.putAll(linkedMap);
                            LOG.fine("Adding linked '" + storageDescription.getId() + "' from: '" + linkedMimePath.getPath() + "'"); //NOI18N
                        } else {
                            if (LOG.isLoggable(Level.WARNING)) {
                                LOG.warning("Linking to other than top level mime types is prohibited. " //NOI18N
                                    + "Ignoring editor settings link from '" + mimePath.getPath() //NOI18N
                                    + "' to '" + linkedMimePath.getPath() + "'"); //NOI18N
                            }
                        }
                    } else {
                        // real settings file
                        StorageReader<? extends K, ? extends V> reader = storageDescription.createReader(settingFile, mimePath.getPath());

                        // Load data from the settingFile
                        Utils.load(settingFile, reader, !legacyFile);
                        Map<? extends K, ? extends V> added = reader.getAdded();
                        Set<? extends K> removed = reader.getRemoved();

                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Loading '" + storageDescription.getId() + "' from: '" + settingFile.getPath() + "'"); //NOI18N
                        }

                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.finest("--- Removing '" + storageDescription.getId() + "': " + removed); //NOI18N
                        }

                        // First remove all entries marked as removed
                        map.keySet().removeAll(removed);

                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.finest("--- Adding '" + storageDescription.getId() + "': " + added); //NOI18N
                        }

                        // Then add all new entries
                        for (Map.Entry<? extends K, ? extends V> entry : added.entrySet()) {
                            K key = entry.getKey();
                            V value = entry.getValue();
                            V origValue = map.put(key, value);
                            if (LOG.isLoggable(Level.FINEST) && origValue != null && !origValue.equals(value)) {
                                LOG.finest("--- Replacing old entry for '" + key + "', orig value = '" + origValue + "', new value = '" + value + "'"); //NOI18N
                            }
                        }

                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.finest("-------------------------------------"); //NOI18N
                        }
                    }
                }
            }

            return map;
        }
    }
    
    private boolean _save(MimePath mimePath, String profile, boolean defaults, Map<K, V> data) throws IOException {
        Map<K, V> defaultData = load(mimePath, profile, true);
        
        if (storageDescription instanceof Operations) {
            @SuppressWarnings("unchecked")
            Operations<K, V> operations = (Operations<K, V>) storageDescription;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Forwarding saving of '" + storageDescription.getId() + "' to: " + operations); //NOI18N
            }
            return operations.save(mimePath, profile, defaults, data, defaultData);
        } else {
            final Map<K, V> added = new HashMap<K, V>();
            final Map<K, V> removed = new HashMap<K, V>();
            Utils.diff(defaultData, data, added, removed);

            // Perform the operation
            final String mimePathString = mimePath.getPath();
            final String settingFileName = SettingsType.getLocator(storageDescription).getWritableFileName(
                mimePathString, profile, null, defaults);
            
            tracker.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    if (added.size() > 0 || removed.size() > 0) {
                        FileObject f = FileUtil.createData(baseFolder, settingFileName);
                        StorageWriter<K, V> writer = storageDescription.createWriter(f, mimePathString);
                        writer.setAdded(added);
                        writer.setRemoved(removed.keySet());
                        Utils.save(f, writer);
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Saving '" + storageDescription.getId() + "' to: '" + f.getPath() + "'"); //NOI18N
                        }
                    } else {
                        FileObject f = baseFolder.getFileObject(settingFileName);
                        if (f != null) {
                            f.delete();
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Saving '" + storageDescription.getId() + 
                                    "', no changes from defaults therefore deleting: '" + f.getPath() + "'"); //NOI18N
                            }
                        }
                    }
                }
            });
    
            return false;
        }
    }

    private void _delete(MimePath mimePath, String profile, boolean defaults) throws IOException {
        if (storageDescription instanceof Operations) {
            @SuppressWarnings("unchecked")
            Operations<K, V> operations = (Operations<K, V>) storageDescription;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Forwarding deletion of '" + storageDescription.getId() + "' to: " + operations); //NOI18N
            }
            operations.delete(mimePath, profile, defaults);
        } else {
            // Perform the operation
            final List<Object []> profileInfos = scan(mimePath, profile, defaults, !defaults);
            if (profileInfos != null) {
                tracker.runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        for(Object [] info : profileInfos) {
                            assert info.length == 5;
                            FileObject profileHome = (FileObject) info[0];
                            FileObject settingFile = (FileObject) info[1];
                            boolean modulesFile = ((Boolean) info[2]).booleanValue();
                            
                            // will delete either a real settings file or a link file (.shadow)
                            settingFile.delete();
                            
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Deleting '" + storageDescription.getId() + "' file: '" + settingFile.getPath() + "'"); //NOI18N
                            }
                        }
                    }
                });
            }
        }
    }

    private void filterAfterLoad(Map<K, V> data, MimePath mimePath, String profile, boolean defaults) throws IOException {
        List<StorageFilter> filters = Filters.getFilters(storageDescription.getId());
        for(int i = 0; i < filters.size(); i++) {
            @SuppressWarnings("unchecked") StorageFilter<K, V> filter = filters.get(i);
            filter.afterLoad(data, mimePath, profile, defaults);
        }
    }
    
    private void filterBeforeSave(Map<K, V> data, MimePath mimePath, String profile, boolean defaults) throws IOException {
        List<StorageFilter> filters = Filters.getFilters(storageDescription.getId());
        for(int i = filters.size() - 1; i >= 0; i--) {
            @SuppressWarnings("unchecked") StorageFilter<K, V> filter = filters.get(i);
            filter.beforeSave(data, mimePath, profile, defaults);
        }
    }
    
    private static CacheKey cacheKey(String profile, boolean defaults) {
        return new CacheKey(profile, defaults);
    }
    
    private static final class CacheKey {
        private final String profile;
        private final boolean defaults;
        
        public CacheKey(String profile, boolean defaults) {
            this.profile = profile;
            this.defaults = defaults;
        }

        public @Override boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CacheKey other = (CacheKey) obj;
            if ((this.profile == null && other.profile != null) ||
                (this.profile != null && other.profile == null) ||
                (this.profile != null && !this.profile.equals(other.profile))
            ) {
                return false;
            }
            if (this.defaults != other.defaults) {
                return false;
            }
            return true;
        }

        public @Override int hashCode() {
            int hash = this.profile != null ? this.profile.hashCode() : 7;
            hash = 37 * hash + Boolean.valueOf(this.defaults).hashCode();
            return hash;
        }
        
    } // End of CacheKey class
    
    private static final class Filters implements Callable<Void> {
        
        public static List<StorageFilter> getFilters(String storageDescriptionId) {
            synchronized (filters) {
                if (allFilters == null) {
                    allFilters = Lookup.getDefault().lookupResult(StorageFilter.class);
                    allFilters.addLookupListener(WeakListeners.create(LookupListener.class, allFiltersTracker, allFilters));
                    rebuild();
                }
                
                Filters filtersForId = filters.get(storageDescriptionId);
                return filtersForId == null ? Collections.emptyList() : filtersForId.filtersForId;
            }
        }

        public static void registerCallback(StorageImpl storageImpl) {
            callbacks.put(storageImpl.storageDescription.getId(), new WeakReference<StorageImpl>(storageImpl));
        }
        
        public Void call() {
            resetCaches(Collections.singleton(storageDescriptionId));
            return null;
        }
        
        // ------------------------------------------
        // private implementation
        // ------------------------------------------

        private static final Map<String, Filters> filters = new HashMap<String, Filters>();
        private static Lookup.Result<StorageFilter> allFilters = null;
        private static final LookupListener allFiltersTracker = new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                Set<String> changedIds;
                
                synchronized (filters) {
                    if (!rebuilding) {
                        rebuilding = true;
                        try {
                            changedIds = rebuild();
                        } finally {
                            rebuilding = false;
                        }
                    } else {
                        // ignore the event, see #159714
                        return;
                    }
                }
                
                resetCaches(changedIds);
            }
        };
        private static final Map<String, Reference<StorageImpl>> callbacks = new HashMap<String, Reference<StorageImpl>>();
        private static boolean rebuilding = false;
        
        private final String storageDescriptionId;
        private final List<StorageFilter> filtersForId = new ArrayList<>();
        
        private static Set<String> rebuild() {
            filters.clear();

            Collection<? extends StorageFilter> all = allFilters.allInstances();
            for(StorageFilter f : all) {
                String id = SpiPackageAccessor.get().storageFilterGetStorageDescriptionId(f);
                Filters filterForId = filters.get(id);
                if (filterForId == null) {
                    filterForId = new Filters(id);
                    filters.put(id, filterForId);
                }

                SpiPackageAccessor.get().storageFilterInitialize(f, filterForId);
                filterForId.filtersForId.add(f);
            }
            
            Set<String> changedIds = new HashSet<String>(filters.keySet());
            return changedIds;
        }
        
        private static void resetCaches(Set<String> storageDescriptionIds) {
            for(String id : storageDescriptionIds) {
                Reference<StorageImpl> ref = callbacks.get(id);
                StorageImpl storageImpl = ref == null ? null : ref.get();
                if (storageImpl != null) {
                    storageImpl.refresh();
                }
            }
        }
        
        private Filters(String storageDescriptionId) {
            this.storageDescriptionId = storageDescriptionId;
        }

    } // End of Filters class

    private final class FilesystemTracker implements FileChangeListener, Runnable {

        // -------------------------------------------------------------------
        // FileChangeListener implementation
        // -------------------------------------------------------------------

        public void fileAttributeChanged(FileAttributeEvent fe) {
            // just ignore these
        }

        public void fileChanged(FileEvent fe) {
            if (!filterEvents(fe)) {
                boolean processed = processFile(fe.getFile());
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("fileChanged (" + (processed ? "processed" : "ignored" ) + "): " //NOI18N
                        + fe.getFile().getPath());
                }
            }
        }

        public void fileDataCreated(FileEvent fe) {
            if (!filterEvents(fe)) {
                boolean processed = processFile(fe.getFile());
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("fileDataCreated (" + (processed ? "processed" : "ignored" ) + "): " //NOI18N
                        + fe.getFile().getPath());
                }
            }
        }

        public void fileDeleted(FileEvent fe) {
            if (!filterEvents(fe)) {
                boolean processed = processFile(fe.getFile());
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("fileDeleted (" + (processed ? "processed" : "ignored" ) + "): " //NOI18N
                        + fe.getFile().getPath());
                }
            }
        }

        public void fileFolderCreated(FileEvent fe) {
            if (!filterEvents(fe)) {
                boolean processed = processKids(fe.getFile());
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("fileFolderCreated (" + (processed ? "processed" : "ignored" ) + "): " //NOI18N
                        + fe.getFile().getPath());
                }
            }
        }

        public void fileRenamed(FileRenameEvent fe) {
            if (!filterEvents(fe)) {
                boolean processed = processKids(fe.getFile().getParent());
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("fileRenamed (" + (processed ? "processed" : "ignored" ) + "): " //NOI18N
                        + fe.getFile().getPath());
                }
            }
        }

        // -------------------------------------------------------------------
        // Runnable implementation
        // -------------------------------------------------------------------

        // runs asynchronously on the default RequestProcessor's thread
        public void run() {
            StorageImpl.this.refresh();
        }

        // -------------------------------------------------------------------
        // Public implementation
        // -------------------------------------------------------------------

        public FilesystemTracker(FileSystem fileSystem) {
            this.controlledFilesPattern = Pattern.compile("^Editors/(.*)" + storageDescription.getId() + "(.*)"); //NOI18N
            this.fileSystem = fileSystem;
            this.fileSystem.addFileChangeListener(FileUtil.weakFileChangeListener(this, this.fileSystem));
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(this + " sensitive to " //NOI18N
                    + controlledFilesPattern.pattern() + " paths and " //NOI18N
                    + storageDescription.getMimeType() + "setting files"); //NOI18N
            }
        }

        // runs under StorageImpl.this.lock
        public void runAtomicAction(FileSystem.AtomicAction task) throws IOException {
            assert atomicAction == null;
            atomicAction = task;
            try {
                fileSystem.runAtomicAction(task);
            } finally {
                atomicAction = null;
            }
        }

        // -------------------------------------------------------------------
        // Private implementation
        // -------------------------------------------------------------------

        private final FileSystem fileSystem;
        private final RequestProcessor.Task refreshCacheTask= new RequestProcessor("Editor-Setting-Files-Tracker-" + storageDescription.getId()).create(this); //NOI18N
        private final List<Reference<FileEvent>> recentEvents = new LinkedList<Reference<FileEvent>>();
        private final Pattern controlledFilesPattern;

        private volatile FileSystem.AtomicAction atomicAction;

        private boolean filterEvents(FileEvent event) {
            // filter out anything that does not match required file path pattern
            if (!controlledFilesPattern.matcher(event.getFile().getPath()).matches()) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer(event.getFile().getPath() + " does not match: " + controlledFilesPattern.pattern()); //NOI18N
                }
                return true;
            }

            // filter out our own events
            final FileSystem.AtomicAction aa = atomicAction;
            if (aa != null && event.firedFrom(aa)) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Filesystem event for " + event.getFile().getPath() + " caused by saving settings"); //NOI18N
                }
                return true;
            }
            
            // filter out duplicate events, maybe this does not have any effect
            synchronized (recentEvents) {
                for(Iterator<Reference<FileEvent>> i = recentEvents.iterator(); i.hasNext(); ) {
                    Reference<FileEvent> ref = i.next();
                    FileEvent e = ref.get();
                    if (e == null) {
                        i.remove();
                    } else {
                        if (e == event) {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Filtering out duplicate filesystem event (1): original=[" + printEvent(e) + "]" //NOI18N
                                    + ", duplicate=[" + printEvent(event) + "]"); //NOI18N
                            }
                            return true;
                        }
                        
                        if (e.getTime() == event.getTime() && e.getFile().getPath().equals(event.getFile().getPath())) {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Filtering out duplicate filesystem event (2): original=[" + printEvent(e) + "]" //NOI18N
                                        + ", duplicate=[" + printEvent(event) + "]"); //NOI18N
                            }
                            return true;
                        }
                    }
                }

                if (recentEvents.size() > 100) {
                    recentEvents.remove(recentEvents.size() - 1);
                }
                recentEvents.add(0, new WeakReference<FileEvent>(event));
                return false;
            }
        }

        // runs asynchronously when events are fired from the filesystem
        private boolean processFile(FileObject f) {
            if (!ignoreFilesystemEvents
                && f.isData()
                && (f.getMIMEType().equals(storageDescription.getMimeType())
                    || f.getNameExt().equals(storageDescription.getLegacyFileName()))
            ) {
                refreshCacheTask.schedule(71);
                return true;
            } else {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Not a settings file: " + f.getPath() + ", mimeType=" + f.getMIMEType()); //NOI18N
                }
                return false;
            }
        }

        private boolean processKids(FileObject f) {
            assert f.isFolder() == true : "Expecting folder, but got: " + f; //NOI18N
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(f.getPath() + " has " + f.getChildren().length + " children"); //NOI18N
            }

            for(FileObject ff : f.getChildren()) {
                if (ff.isData()) {
                    if (processFile(ff)) {
                        return true;
                    }
                } else {
                    if (controlledFilesPattern.matcher(ff.getPath()).matches()) {
                        if (processKids(ff)) {
                            return true;
                        }
                    } else if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine(ff.getPath() + " does not match: " + controlledFilesPattern.pattern()); //NOI18N
                    }
                }
            }
            return false;
        }

        private String printEvent(FileEvent event) {
            return event.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(event)) //NOI18N
                    + ", ts=" + event.getTime() //NOI18N
                    + ", path=" + event.getFile().getPath(); //NOI18N
        }
    } // End of FilesystemTracker class
    
    /**
     * Caching provider. Storage cache cannot be held in static variable, as they initialize
     * with per-execution data.
     */
    public static interface StorageCache {
        public <K,V> EditorSettingsStorage<K,V>    createStorage(StorageDescription<K,V> desc);
    }
    
    /**
     * Caching for individual EditorSettingStorages. Originally the cache was implemented in
     * a static variable, but we need to cache the storage for each execution/user separately.
     */
    @ServiceProvider(service = StorageImpl.StorageCache.class)
    public static final class StorageCacheImpl implements StorageImpl.StorageCache {
        private final Map<StorageDescription<?, ?>, EditorSettingsStorage> cache = new HashMap<>();

        @Override
        public <K, V> EditorSettingsStorage<K, V> createStorage(StorageDescription<K, V> sd) {
            synchronized (cache) {
                EditorSettingsStorage<K, V> ess = null;
                if (sd != null) {
                    ess = cache.get(sd);
                    if (ess == null) {
                        ess = ApiAccessor.get().createSettingsStorage(sd);
                        cache.put(sd, ess);
                    }
                }            

                return ess;
            }
        }
    
    }
}

