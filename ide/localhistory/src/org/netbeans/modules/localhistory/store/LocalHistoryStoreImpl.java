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
package org.netbeans.modules.localhistory.store;

import java.io.*;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.versioning.ui.history.HistorySettings;
import org.netbeans.modules.localhistory.utils.FileUtils;
import org.netbeans.modules.localhistory.utils.Utils;
import org.netbeans.modules.turbo.CustomProviders;
import org.netbeans.modules.turbo.Turbo;
import org.netbeans.modules.turbo.TurboProvider;
import org.netbeans.modules.turbo.TurboProvider.MemoryCache;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
class LocalHistoryStoreImpl implements LocalHistoryStore {

    private static final int DELETED = 0;
    private static final int TOUCHED = 1;

    private static final String DATA_FILE     = "data";                  // NOI18N
    private static final String HISTORY_FILE  = "history";               // NOI18N
    private static final String LABELS_FILE   = "labels";                // NOI18N
    private static final String STORAGE_FILE  = "storage";               // NOI18N

    private static final String STORAGE_VERSION = "1.0";                 // NOI18N

    private File storage;
    private Turbo turbo;
    private DataFilesTurboProvider cacheProvider;
    private final ListenersSupport listenersSupport;

    private static List<HistoryEntry> emptyHistory = new ArrayList<HistoryEntry>(0);
    private static Map<Long, String> emptyLabels = new HashMap<Long, String>();
    private static StoreEntry[] emptyStoreEntryArray = new StoreEntry[0];

    private Set<File> lockedFolders = Collections.synchronizedSet(new HashSet<File>(5));

    private static long LOCK_TIMEOUT = 30;
    private final RequestProcessor rp = new RequestProcessor("LocalHistoryStore", 50); // NOI18N
    private final Map<VCSFileProxy, Semaphore> proccessedFiles = new HashMap<VCSFileProxy, Semaphore>();
    static final Logger LOG = Logger.getLogger(LocalHistoryStoreImpl.class.getName());    
    
    private static FilenameFilter fileEntriesFilter =
            new FilenameFilter() {
        @Override
                public boolean accept(File dir, String fileName) {
                    return !( fileName.endsWith(DATA_FILE)    ||
                              fileName.endsWith(HISTORY_FILE) ||
                              fileName.endsWith(LABELS_FILE)  ||
                              fileName.endsWith(STORAGE_FILE));
                }
            };

    LocalHistoryStoreImpl() {
        initStorage();

        listenersSupport = new ListenersSupport(this);

        cacheProvider = new DataFilesTurboProvider();
        turbo = Turbo.createCustom(
                new CustomProviders() {
                    private final Set providers = Collections.singleton(cacheProvider);
                    public Iterator providers() {
                        return providers.iterator();
                    }
                },
                20, -1);
    }

    @Override
    public void fileCreate(VCSFileProxy file, long ts) {
        Semaphore s = lock(file, "fileCreate"); // NOI18N
        try {
            fileCreateImpl(file, ts, null, FileUtils.getPath(file));
        } catch (IOException ioe) {
            LocalHistory.LOG.log(Level.WARNING, null, ioe);
        } finally {
            if(s != null) s.release();
        }
    }

    private void fileCreateImpl(VCSFileProxy file, long ts, String from, String to) throws IOException {
       if(lastModified(file) > 0) {
            return;
        }
        String tsString = Long.toString(ts);
        File storeFile = null;
        if(file.isFile()) {
            try {
                storeFile = getStoreFile(file, tsString, true);
                FileUtils.copy(file, StoreEntry.createStoreFileOutputStream(storeFile));
            } finally {
                // release lock
                lockedFolders.remove(storeFile.getParentFile());
            }
            LocalHistory.logCreate(file, storeFile, ts, from, to);
        }
        touch(file, new StoreDataFile(FileUtils.getPath(file), TOUCHED, ts, file.isFile()));
        VCSFileProxy parent = file.getParentFile();
        if(parent != null) {
            // XXX consider also touching the parent - yes (collisions, ...)
            writeHistoryForFile(parent, new HistoryEntry[] {new HistoryEntry(ts, from, to, TOUCHED)}, true);
        }
        fireChanged(file, ts);
    }

    @Override
    public void fileChange(final VCSFileProxy file) {
        final Semaphore s = lock(file, "fileChange"); // NOI18N
        rp.post(new Runnable() {
            @Override
            public void run() {
                final long ts = file.lastModified(); // - (1000 * 60 * 60 * 24 * 14);
                try {
                    long lastModified = lastModified(file);
                    if(lastModified == ts) {
                        LocalHistory.LOG.log(Level.FINE, "skipping fileChange for file {0} because timestap already exists.", new Object[]{FileUtils.getPath(file), ts}); // NOI18N
                        return;
                    }
                    if(file.isFile()) {
                        storeChangedSync(file, ts);
                    } else {
                        try {
                            touch(file, new StoreDataFile(FileUtils.getPath(file), TOUCHED, ts, false));
                        } catch (IOException ioe) {
                            LocalHistory.LOG.log(Level.WARNING, null, ioe);
                        }
                    }
                } finally {
                    if(s != null) s.release();
                    synchronized(proccessedFiles) {
                        proccessedFiles.remove(file);
                    }
                }
                fireChanged(file, ts);
            }
        });
        
    }

    private void storeChangedSync(VCSFileProxy file, long ts) {
        File storeFile = getStoreFile(file, Long.toString(ts), true);
        try {
            try {
                FileUtils.copy(file, StoreEntry.createStoreFileOutputStream(storeFile));
                LocalHistory.LOG.log(Level.FINE, "copied file {0} into storage file {1}", new Object[]{FileUtils.getPath(file), storeFile}); // NOI18N

                LocalHistory.logChange(file, storeFile, ts);
                touch(file, new StoreDataFile(FileUtils.getPath(file), TOUCHED, ts, true));
            } finally {
                // release lock
                lockedFolders.remove(storeFile.getParentFile());
            }
        } catch (FileNotFoundException ioe) {                                
            LocalHistory.LOG.log(Level.INFO, "exception while copying file " + file + " to " + storeFile, ioe); // NOI18N                                    
        } catch (IOException ioe) {
            LocalHistory.LOG.log(Level.WARNING, null, ioe);
        } finally {
            LocalHistory.LOG.log(Level.FINE, "finnished copy file {0} into storage file {1}", new Object[]{FileUtils.getPath(file), storeFile}); // NOI18N
        }
    }
    
    @Override
    public void fileDelete(VCSFileProxy file, long ts) {
        Semaphore s = lock(file, "fileDelete"); // NOI18N
        try {
            fileDeleteImpl(file, null, FileUtils.getPath(file), ts);
        } catch (IOException ioe) {
            LocalHistory.LOG.log(Level.WARNING, null, ioe);
        } finally {
            if(s != null) s.release();
        }
        fireChanged(file, ts);
    }

    private void fileDeleteImpl(VCSFileProxy file, String from, String to, long ts) throws IOException {
        StoreDataFile data = readStoreData(file);
        // XXX what if already deleted?

        if(data == null) {
            LocalHistory.log("deleting without data for file : " + file); // NOI18N
            return;
        }
        // copy from previous entry
        long lastModified = data.getLastModified();
        boolean isFile = data.isFile();

        if(!LocalHistory.LOG.isLoggable(Level.FINE)) {
            File storeFile = getDataFile(file);
            LocalHistory.logDelete(file, storeFile, ts);
        }

        touch(file, new StoreDataFile(FileUtils.getPath(file), DELETED, lastModified, isFile));
        VCSFileProxy parent = file.getParentFile();
        if(parent != null) {
            // XXX consider also touching the parent
            writeHistoryForFile(parent, new HistoryEntry[] {new HistoryEntry(ts, from, to, DELETED)}, true);
        }
    }

    @Override
    public void fileCreateFromMove(VCSFileProxy from, VCSFileProxy to, long ts) {
        Semaphore s = lock(from, "fileCreateFromMove"); // NOI18N
        try {
            if(lastModified(to) > 0) {
                return;
            }
            fileCreateImpl(to, ts, FileUtils.getPath(from), FileUtils.getPath(to));
        } catch (IOException ioe) {
            LocalHistory.LOG.log(Level.WARNING, null, ioe);
        } finally {
            if(s != null) s.release();
        }
        fireChanged(to, ts);
    }

    @Override
    public void fileDeleteFromMove(VCSFileProxy from, VCSFileProxy to, long ts) {
        Semaphore s = lock(from, "fileDeleteFromMove"); // NOI18N
        try {
            fileDeleteImpl(from, FileUtils.getPath(from), FileUtils.getPath(to), ts);
        } catch (IOException ioe) {
            LocalHistory.LOG.log(Level.WARNING, null, ioe);
        } finally {
            if(s != null) s.release();
        }
        fireChanged(from, ts);
    }

    static File getStorageRootFile() {
        return new File(new File(Places.getUserDirectory(), "var"), "filehistory"); // NOI18N
    }

    private long lastModified(VCSFileProxy file) {
        StoreDataFile data = readStoreData(file);
        return data != null && data.getStatus() != DELETED ? data.getLastModified() : -1;
    }

    @Override
    public StoreEntry[] getStoreEntries(VCSFileProxy file) {
        Semaphore s = lock(file, "getStoreEntries"); // NOI18N
        try {
            // XXX file.isFile() won't work for deleted files
            return getStoreEntriesImpl(file);
        } finally {
            if(s != null) s.release();
        }
    }

    private StoreEntry[] getStoreEntriesImpl(VCSFileProxy file) {
        File storeFolder = getStoreFolder(file);
        File[] storeFiles = storeFolder.listFiles(fileEntriesFilter);
        if(storeFiles != null && storeFiles.length > 0) {
            List<StoreEntry> ret = new ArrayList<StoreEntry>(storeFiles.length);
            if(storeFiles.length > 0) {
                Map<Long, String> labels = getLabels(getLabelsFile(file));
                for (int i = 0; i < storeFiles.length; i++) {
                    long ts = Long.parseLong(storeFiles[i].getName());
                    String label = labels.get(ts);
                    ret.add(StoreEntry.createStoreEntry(file, storeFiles[i], ts, label));
                }
                return ret.toArray(new StoreEntry[storeFiles.length]);
            }
            return emptyStoreEntryArray;
        } else {
            return emptyStoreEntryArray;
        }
    }

    @Override
    public StoreEntry[] getFolderState(VCSFileProxy root, VCSFileProxy[] files, long ts) {
        // XXX get rid of this! used by nobody anyway
//        Semaphore s = lock(root, "getFolderState"); // NOI18N
//        try {
//            return getFolderStateIntern(root, files, ts);
//        } finally {
//            if(s != null) s.release();
//        }
        return new StoreEntry[0];
    }

//    private StoreEntry[] getFolderStateIntern(VCSFileProxy root, VCSFileProxy[] files, long ts) {
//
//        // check if the root wasn't deleted to that time
//        VCSFileProxy parentFile = root.getParentFile();
//        if(parentFile != null) {
//            List<HistoryEntry> parentHistory = readHistoryForFile(parentFile);
//            if(wasDeleted(root, parentHistory, ts)) {
//                return emptyStoreEntryArray;
//            }
//        }
//
//        List<HistoryEntry> history = readHistoryForFile(root);
//
//        // StoreEntries we will return
//        List<StoreEntry> ret = new ArrayList<StoreEntry>();
//
//        Map<File, HistoryEntry> beforeRevert = new HashMap<File, HistoryEntry>();
//        Map<File, HistoryEntry> afterRevert = new HashMap<File, HistoryEntry>();
//
//        for(HistoryEntry he : history) {
//            File file = new File(he.getTo());
//            if(he.getTimestamp() < ts) {
//                // this is the LAST thing which happened
//                // to a file before the given time
//                beforeRevert.put(file, he);
//            } else {
//                // this is the FIRST thing which happened
//                // to a file before the given time
//                if(!afterRevert.containsKey(file)) {
//                    afterRevert.put(file, he);
//                }
//            }
//        }
//
//        for(VCSFileProxy file : files) {
//            HistoryEntry before = beforeRevert.get(file);
//            HistoryEntry after = afterRevert.get(file);
//
//            // lets see what remains when we are throught all existing files
//            beforeRevert.remove(file);
//            afterRevert.remove(file);
//
//            if(before != null && before.getStatus() == DELETED) {
//                // the file was deleted to the given time -> delete it!
//                ret.add(StoreEntry.createDeletedStoreEntry(file, ts));
//                continue;
//            }
//
//            StoreDataFile data = readStoreData(file);
//            if(data == null) {
//                // XXX ???
//                continue;
//            }
//            if(data.isFile()) {
//                StoreEntry se = getStoreEntryIntern(file, ts);
//                if(se != null) {
//                    ret.add(se);
//                } else {
//                    if(after != null && after.getStatus() == TOUCHED) {
//                        ret.add(StoreEntry.createDeletedStoreEntry(file, ts));
//                    } else {
//                        // XXX is this possible?
//                    }
//                    // the file still exists and there is no entry -> uptodate?
//                }
//            } else {
//                if(after != null && after.getStatus() == TOUCHED) {
//                    ret.add(StoreEntry.createDeletedStoreEntry(file, ts));
//                } else {
//                    // XXX is this possible?
//                }
//                // the folder still exists and it wasn't deleted, so do nothing
//            }
//        }
//
//
//        for(Entry<File, HistoryEntry> entry : beforeRevert.entrySet()) {
//
//            File file = entry.getKey();
//
//            // lets see what remains
//            afterRevert.remove(file);
//
//            // the file doesn't exist now, but
//            // there was something done to it before the given time
//            if(entry.getValue().getStatus() == DELETED) {
//                // this is exactly what we have - forget it!
//                continue;
//            }
//
//            StoreDataFile data = readStoreData(file);
//            if(data != null) {
//                if(data.isFile()) {
//                    StoreEntry se = getStoreEntryIntern(file, ts);
//                    if(se != null) {
//                        ret.add(se);
//                    } else {
//                        // XXX what now? this should be covered
//                    }
//                } else {
//                    // it must have existed
//                    File storeFile = getStoreFolder(root); // XXX why returning the root
//                    StoreEntry folderEntry = StoreEntry.createStoreEntry(new File(data.getAbsolutePath()), storeFile, data.getLastModified(), "");
//                    ret.add(folderEntry);
//                }
//            } else {
//                // XXX how to cover this?
//            }
//        }
//
//        // XXX do we even need this
////        for(Entry<File, HistoryEntry> entry : afterRevert.entrySet()) {
////
////        }
//        return ret.toArray(new StoreEntry[ret.size()]);
//
//    }

    private boolean wasDeleted(VCSFileProxy file, List<HistoryEntry> history , long ts) {
        String path = FileUtils.getPath(file);
        boolean deleted = false;

        for(int i = 0; i < history.size(); i++) {
            HistoryEntry he = history.get(i);
            if(he.getTo().equals(path)) {
                if(he.getStatus() == DELETED) {
                    deleted = true;
                } else {
                    deleted = false;
                }
            }
            if(he.ts >= ts) {
                break;
            }
        }
        return deleted;
    }

    @Override
    public StoreEntry getStoreEntry(VCSFileProxy file, long ts) {
        Semaphore s = lock(file, "getStoreEntry"); // NOI18N
        try {
            return getStoreEntryIntern(file, ts);
        } finally {
            if(s != null) s.release();
        }
    }
    
    private StoreEntry getStoreEntryIntern(VCSFileProxy file, long ts) {
        return getStoreEntryImpl(file, ts, readStoreData(file));
    }

    private StoreEntry getStoreEntryImpl(VCSFileProxy file, long ts, StoreDataFile data) {
        // XXX what if file deleted?
        StoreEntry entry = null;

        if(data == null) {
            // not in storage?
            return null;
        }
        if(data.isFile()) {
            StoreEntry[] entries = getStoreEntriesImpl(file);
            for(StoreEntry se : entries) {
                if(se.getTimestamp() <= ts) {
                    if( entry == null || se.getTimestamp() > entry.getTimestamp() ) {
                        entry = se;
                    }
                }
            }
        } else {
            // XXX dont implement this for folders as long there is no need
        }

        return entry;
    }

    @Override
    public void deleteEntry(VCSFileProxy file, long ts) {
        Semaphore s = lock(file, "deleteEntry"); // NOI18N
        try {
            File storeFile = getStoreFile(file, Long.toString(ts), false);
            if(storeFile.exists()) {
                storeFile.delete();
            }
            // XXX delete from parent history
            fireDeleted(file, ts);
        } finally {
            if(s != null) s.release();
        }
    }

    @Override
    public StoreEntry[] getDeletedFiles(VCSFileProxy root) {
        Semaphore s = lock(root, "getDeletedFiles"); // NOI18N
        try {
            return getDeletedFilesIntern(root);
        } finally {
            if(s != null) s.release();
        }
    }
    
    private StoreEntry[] getDeletedFilesIntern(VCSFileProxy root) {
        if(root.isFile()) {
            return null;
        }
        if(root.toFile() == null) {
            // XXX VCSFileProxy hack! there is no way to create a non local (io.File) file
        }
        
        Map<String, StoreEntry> deleted = new HashMap<String, StoreEntry>();

        // first of all find files which are tagged as deleted in the given folder
        // as this is what we know for sure
        List<HistoryEntry> historyEntries = readHistoryForFile(root);
        for(HistoryEntry he : historyEntries) {
            if(he.getStatus() == DELETED) {
                String filePath = he.getTo();
                if(!deleted.containsKey(filePath)) {
                    StoreDataFile data = readStoreData(FileUtils.createProxy(he.getTo()));
                    if(data != null && data.getStatus() == DELETED) {
                        File storeFile = data.isFile ?
                                            getStoreFile(FileUtils.createProxy(data.getAbsolutePath()), Long.toString(data.getLastModified()), false) :
                                            getStoreFolder(root);
                        deleted.put(filePath, StoreEntry.createStoreEntry(FileUtils.createProxy(data.getAbsolutePath()), storeFile, data.getLastModified(), ""));
                    }
                }
            }
        }

        // the problem is that some files might got deleted outside of netbeans,
        // so they aren't tagged as deleted, but we still may have their previous versions stored.
        // It woudln't be very userfriendly to ignore them just because they don't meet all the byrocratic expectations
        // WARNING! don't see this as a bruteforce substitution for the previous block as it is impossible to
        // recover deleted folders this way
        List<VCSFileProxy> lostFiles = getLostFiles();
        for(VCSFileProxy lostFile : lostFiles) {
            if(!deleted.containsKey(FileUtils.getPath(lostFile))) {
                // careful about lostFile being the root folder - it has no parent
                if(root.equals(lostFile.getParentFile())) {
                    StoreEntry[] storeEntries = getStoreEntriesImpl(lostFile);
                    if(storeEntries == null || storeEntries.length == 0) {
                        continue;
                    }
                    StoreEntry storeEntry = storeEntries[0];
                    for(int i = 1; i < storeEntries.length; i++) {
                        if(storeEntry.getTimestamp() < storeEntries[i].getTimestamp()) {
                            storeEntry = storeEntries[i];
                        }
                    }
                    deleted.put(FileUtils.getPath(lostFile), storeEntry);
                }
            }
        }

        return deleted.values().toArray(new StoreEntry[deleted.size()]);
    }

    private List<VCSFileProxy> getLostFiles() {
        List<VCSFileProxy> files = new ArrayList<VCSFileProxy>();
        File[] topLevelFiles = storage.listFiles();
        if(topLevelFiles == null || topLevelFiles.length == 0) {
            return files;
        }
        for(File topLevelFile : topLevelFiles) {
            File[] secondLevelFiles = topLevelFile.listFiles();
            if(secondLevelFiles == null || secondLevelFiles.length == 0) {
                continue;
            }
            for(File storeFile : secondLevelFiles) {
                StoreDataFile data = readStoreFile(new File(storeFile, DATA_FILE));
                if(data == null) {
                    continue;
                }
                VCSFileProxy file = FileUtils.createProxy(data.getAbsolutePath());
                if(!file.exists()) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    @Override
    public StoreEntry setLabel(VCSFileProxy file, long ts, String label) {
        Semaphore s = lock(file, "setLabel"); // NOI18N
        try {
            return setLabelIntern(file, ts, label);
        } finally {
            if(s != null) s.release();
        }
    }
    
    private StoreEntry setLabelIntern(VCSFileProxy file, long ts, String label) {
        File labelsFile = getLabelsFile(file);
        File parent = labelsFile.getParentFile();
        if(!parent.exists()) {
            parent.mkdirs();
        }

        File labelsNew = null;
        DataInputStream dis = null;
        DataOutputStream oos = null;
        boolean foundLabel = false;
        try {
            if(!labelsFile.exists()) {
                oos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(labelsFile)));
                oos.writeLong(ts);
                writeString(oos, label);
            } else {
                labelsNew = new File(labelsFile.getParentFile(), labelsFile.getName() + ".new");            // NOI18N
                oos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(labelsNew)));

                dis = getInputStream(labelsFile);
                long readTs = -1;
                try {
                    while(true) {
                        readTs = dis.readLong();
                        if(readTs == ts) {
                            foundLabel = true;
                            if(label != null) {
                                oos.writeLong(readTs);
                                writeString(oos, label);
                                int len = dis.readInt();
                                skip(dis, len * 2);
                                copyStreams(oos, dis);
                            } else {
                                int len = dis.readInt();
                                skip(dis, len * 2);
                            }
                        } else {
                            oos.writeLong(readTs);
                            String l = readString(dis);
                            writeString(oos, l);
                        }
                    }
                } catch (EOFException e) {
                    if(!foundLabel && label != null) {
                        oos.writeLong(ts);
                        writeString(oos, label);
                    }
                }
            }
            oos.flush();
        } catch (EOFException e) {
            // ignore
        } catch (Exception e) {
            LocalHistory.LOG.log(Level.INFO, null, e);
        } finally {
            if (dis != null) {
                try { dis.close(); } catch (IOException e) { }
            }
            if (oos != null) {
                try { oos.close(); } catch (IOException e) { }
            }
        }

        try {
            if(labelsNew != null ) {
                FileUtils.renameFile(labelsNew, labelsFile);
            }
        } catch (IOException ex) {
            LocalHistory.LOG.log(Level.SEVERE, null, ex);
        }
        
        return getStoreEntryIntern(file, ts);
    }

    @Override
    public void addVersioningListener(VersioningListener l) {
        listenersSupport.addListener(l);
    }

    @Override
    public void removeVersioningListener(VersioningListener l) {
        listenersSupport.removeListener(l);
    }

    @Override
    public void cleanUp(final long ttl) {
        // XXX run only once a day - use the top folder metadata for version and cleanup flag
        LocalHistory.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                LocalHistory.log("Cleanup Start");                       // NOI18N
                long t = System.currentTimeMillis();
                cleanUpImpl(ttl);
                LocalHistory.log("Cleanup End in " + (System.currentTimeMillis() - t) + " millis."); // NOI18N
            }
        });
    }

    private void cleanUpImpl(long ttl) {

        // XXX fire events

        int maxC = Integer.getInteger("netbeans.localhistory.maxCleanupCount", -1); // NOI18N
        long maxT = Long.getLong("netbeans.localhistory.maxCleanupTime", -1) * 1000; // NOI18N
            
        long now = System.currentTimeMillis();
        int count = 0;
        
        File[] files = storage.listFiles();
        if(files == null || files.length == 0) {
            return;
        }

        List<File> topLevelFiles = Arrays.asList(files);
        if(maxC > -1 || maxT > -1) {
            Collections.shuffle(topLevelFiles);
        }
        for(File topLevelFile : topLevelFiles) {

            if(topLevelFile.getName().equals(STORAGE_FILE)) {
                continue;
            }

            files = topLevelFile.listFiles();
            if(files == null || files.length == 0) {
                deleteRecursivelly(topLevelFile);
                continue;
            }
            
            List<File> secondLevelFiles = Arrays.asList(files);
            if(maxC > -1 || maxT > -1) {
                Collections.shuffle(secondLevelFiles);
            }
            boolean allEmpty = true;
            for(File secondLevelFile : secondLevelFiles) {
                count++;
                boolean empty = !lockedFolders.contains(secondLevelFile) && cleanUpFolder(secondLevelFile, ttl, now);
                if(empty) {
                    if(secondLevelFile.exists()) {
                        deleteRecursivelly(secondLevelFile);
                    }
                } else {
                    allEmpty = false;
                }
            }
            if(allEmpty) {
                deleteRecursivelly(topLevelFile);
            }
            
            if(maxC > 0 && count >= maxC) {
                break;
            }
            if(maxT > 0 && (System.currentTimeMillis() - now) >= maxT) {
                break;
            }
        }
    }

    private synchronized boolean cleanUpFolder(File folder, long ttl, long now) {
        File dataFile = new File(folder, DATA_FILE);

        if(!dataFile.exists()) {
            // it's a folder
            return cleanUpStoredFolder(folder, ttl, now);
        }

        StoreDataFile data = readStoreFile(dataFile);
        if(data == null || data.getAbsolutePath() == null) {
            // what's this?
            return true;
        }
        if(data.isFile()) {
           return cleanUpStoredFile(folder, ttl, now);
        } else {
           return cleanUpStoredFolder(folder, ttl, now);
        }
    }

    public void deleteRecursivelly(File file) {
        if (!lockedFolders.contains(file)) {
            FileUtils.deleteRecursively(file);
        }
    }

    private boolean cleanUpStoredFile(File store, long ttl, long now) {
        File dataFile = new File(store, DATA_FILE);

            if(!dataFile.exists()) {
            return true;
            }
            if(dataFile.lastModified() < now - ttl) {
                purgeDataFile(dataFile);
                return true;
            }

            File[] files = store.listFiles(fileEntriesFilter);
            boolean skipped = false;

            File labelsFile = new File(store, LABELS_FILE);
            Map<Long, String> labels = emptyLabels;
            if(labelsFile.exists()) {
                labels = getLabels(labelsFile);
            }
            if(files != null) {
                for(File f : files) {
                    // XXX check the timestamp when touched
                    long ts;
                    try {
                        ts = Long.parseLong(f.getName());
                    } catch (NumberFormatException ex) {
                        // heh! what's this? ignore...
                        continue;
                    }
                    if(ts < now - ttl) {
                        if(labels.size() > 0) {
                            if(HistorySettings.getInstance().getCleanUpLabeled()) {
                                // remove label and file
                                labels.remove(ts);
                                f.delete();                            
                            } else {
                                if(!labels.containsKey(ts)) {
                                    // remove only if no label
                                    f.delete();
                                }
                            }
                        } else {
                            // no labels => just remove
                            f.delete();
                        }
                    } else {
                        skipped = true;
                    }
                }
            }
            if(!skipped) {
                // all entries are gone -> remove also the metadata
                labelsFile.delete();
                writeStoreFile(dataFile, null);  // null stands for remove
            } else {
                if(labels.size() > 0) {
                    writeLabels(labelsFile, labels);
                } else {
                    labelsFile.delete();
                }
            }
        return !skipped;
    }

    private void writeLabels(File labelsFile, Map<Long, String> labels) {
        File parent = labelsFile.getParentFile();
        if(!parent.exists()) {
            parent.mkdirs();
        }
        DataInputStream dis = null;
        DataOutputStream oos = null;
        try {
            oos = getOutputStream(labelsFile, false);
            for(Entry<Long, String> label : labels.entrySet()) {
                oos.writeLong(label.getKey());
                writeString(oos, label.getValue());
            }
            oos.flush();
        } catch (Exception e) {
            LocalHistory.LOG.log(Level.INFO, null, e);
        } finally {
            if (dis != null) {
                try { dis.close(); } catch (IOException e) { }
            }
            if (oos != null) {
                try { oos.close(); } catch (IOException e) { }
            }
        }
    }

    private boolean cleanUpStoredFolder(File store, long ttl, long now) {
        File historyFile = new File(store, HISTORY_FILE);
        File dataFile = new File(store, DATA_FILE);

        boolean dataObsolete = !dataFile.exists() || dataFile.lastModified() < now - ttl;
        boolean historyObsolete = !historyFile.exists() || historyFile.lastModified() < now - ttl;

        if(!historyObsolete) {
            List<HistoryEntry> entries = readHistory(historyFile);
            List<HistoryEntry> newEntries = new ArrayList<HistoryEntry>();
            for(HistoryEntry entry : entries) {
                // XXX check the timestamp when touched - and you also should to write it with the historywhen
                if(entry.getTimestamp() > now - ttl) {
                    newEntries.add(entry);
                }
            }
            if(newEntries.size() > 0) {
                writeHistory(historyFile, newEntries.toArray(new HistoryEntry[0]), false);
            } else {
                historyFile.delete();
                historyObsolete = true;
            }
        }
        if(dataObsolete) {
            purgeDataFile(dataFile);
        }
        if(historyObsolete) {
            historyFile.delete();
        }

        return dataObsolete && historyObsolete;
    }

    private void purgeDataFile(File dataFile) {
        if(dataFile.exists()) {
            writeStoreFile(dataFile, null);
        }
    }

    private void fireChanged(VCSFileProxy file, long ts) {
        listenersSupport.fireVersioningEvent(EVENT_HISTORY_CHANGED, new Object[] {file, ts});
    }

    private void fireDeleted(VCSFileProxy file, long ts) {
        listenersSupport.fireVersioningEvent(EVENT_ENTRY_DELETED, new Object[] {file, ts});
    }

    private void touch(VCSFileProxy file, StoreDataFile data) throws IOException {
        writeStoreData(file, data);
    }

    private void initStorage() {
        storage = getStorageRootFile();
        if(!storage.exists()) {
            storage.mkdirs();
        }
        writeStorage();
    }

    private File getStoreFolder(VCSFileProxy file) {
        String filePath = FileUtils.getPath(file);
        File storeFolder = getStoreFolderName(filePath);
        int i = 0;
        while(storeFolder.exists()) {
            // check for collisions
            StoreDataFile data = readStoreFile(new File(storeFolder, DATA_FILE));
            if(data == null || data.getAbsolutePath().equals(filePath)) {
                break;
            }
            storeFolder = getStoreFolderName(filePath + "." + i++); // NOI18N
        }
        return storeFolder;
    }

    private File getStoreFolderName(String filePath) {
        int fileHash = filePath.hashCode();
        String storeFileName = getMD5(filePath);
        String storeIndex = storage.getAbsolutePath() + "/" + Integer.toString(fileHash % 173 + 172);   // NOI18N
        return new File(storeIndex + "/" + storeFileName);                                              // NOI18N
    }

    private String getMD5(String name) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");                          // NOI18N
        } catch (NoSuchAlgorithmException e) {
            // should not happen
            return null;
        }
        digest.update(name.getBytes());
        byte[] hash = digest.digest();
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(hash[i] & 0x000000FF);
            if(hex.length()==1) {
                hex = "0" + hex;                                                // NOI18N
            }
            ret.append(hex);
        }
        return ret.toString();
    }

    private File getStoreFile(VCSFileProxy file, String name, boolean forceCreate) {
        File storeFolder = getStoreFolder(file);
        if(forceCreate) {
            lockedFolders.add(storeFolder);
            if(!storeFolder.exists()) {
                storeFolder.mkdirs();
            }
        }
        return new File(storeFolder, name);
    }

    private File getHistoryFile(VCSFileProxy file) {
        File storeFolder = getStoreFolder(file);
        if(!storeFolder.exists()) {
            storeFolder.mkdirs();
        }
        return new File(storeFolder, HISTORY_FILE);
    }

    private File getDataFile(VCSFileProxy file) {
        File storeFolder = getStoreFolder(file);
        return new File(storeFolder, DATA_FILE);
    }

    private File getLabelsFile(VCSFileProxy file) {
        File storeFolder = getStoreFolder(file);
        return new File(storeFolder, LABELS_FILE);
    }

    private Map<Long, String> getLabels(File labelsFile) {

        if(!labelsFile.exists()) {
            return emptyLabels;
        }
        DataInputStream dis = null;
        Map<Long, String> ret = new HashMap<Long, String>();
        try {
            dis = getInputStream(labelsFile);
            while(true) {
                long ts = dis.readLong();
                String label = readString(dis);
                ret.put(ts, label);
            }
        } catch (EOFException e) {
            return ret;
        } catch (Exception e) {
            LocalHistory.LOG.log(Level.INFO, null, e);
        } finally {
            if (dis != null) {
                try { dis.close(); } catch (IOException e) { }
            }
        }
        return emptyLabels;
    }

    private void writeHistoryForFile(VCSFileProxy file, HistoryEntry[] entries, boolean append) {
        if(!LocalHistory.LOG.isLoggable(Level.FINE)) {
            if(getDataFile(file) == null) {
                LocalHistory.log("writing history for file without data : " + file);    // NOI18N
            }
        }
        File history = getHistoryFile(file);
        writeHistory(history, entries, append);
    }

    private void writeHistory(File history, HistoryEntry[] entries, boolean append) {
        DataOutputStream dos = null;
        try {
            dos = getOutputStream(history, append);
            for(HistoryEntry entry : entries) {
                dos.writeLong(entry.getTimestamp());
                writeString(dos, entry.getFrom());
                writeString(dos, entry.getTo());
                dos.writeInt(entry.getStatus());
            }
            dos.flush();
        } catch (Exception e) {
            LocalHistory.LOG.log(Level.INFO, null, e);
        } finally {
            if (dos != null) {
                try { dos.close(); } catch (IOException e) { }
            }
        }
    }

    private List<HistoryEntry> readHistoryForFile(VCSFileProxy file) {
        return readHistory(getHistoryFile(file));
    }

    private List<HistoryEntry> readHistory(File history) {
        if(!history.exists()) {
            return emptyHistory;
        }
        DataInputStream dis = null;
        List<HistoryEntry> entries = new ArrayList<HistoryEntry>();
        try {
            dis = getInputStream(history);
            while(true) {
                long ts = dis.readLong();
                String from = readString(dis);
                String to = readString(dis);
                int action = dis.readInt();
                entries.add(new HistoryEntry(ts, from, to, action));
            }
        } catch (EOFException e) {
            return entries;
        } catch (Exception e) {
            LocalHistory.LOG.log(Level.INFO, null, e);
        } finally {
            if (dis != null) {
                try { dis.close(); } catch (IOException e) { }
            }
        }
        return emptyHistory;
    }

    private StoreDataFile readStoreData(VCSFileProxy file) {
        return readStoreFile(getDataFile(file));
    }

    private StoreDataFile readStoreFile(File file) {
        return (StoreDataFile) turbo.readEntry(file, DataFilesTurboProvider.ATTR_DATA_FILES);
    }

    private void writeStorage() {
        DataOutputStream dos = null;
        try {
            dos = getOutputStream(new File(storage, STORAGE_FILE), false);
            writeString(dos, STORAGE_VERSION);
            dos.flush();
        } catch (Exception e) {
            LocalHistory.LOG.log(Level.INFO, null, e);
        } finally {
            if (dos != null) {
                try { dos.close(); } catch (IOException e) { }
            }
        }
    }

    private void writeStoreData(VCSFileProxy file, StoreDataFile data) {
        writeStoreFile(getDataFile(file), data);
    }

    private void writeStoreFile(File file, StoreDataFile data) {
        turbo.writeEntry(file, DataFilesTurboProvider.ATTR_DATA_FILES, data);
    }
    
    private static void writeString(DataOutputStream dos, String str) throws IOException {
        if(str != null) {
            dos.writeInt(str.length());
            dos.writeChars(str);
        } else {
            dos.writeInt(0);
        }
    }

    private static String readString(DataInputStream dis) throws IOException {
        int len = dis.readInt();
        if(len == 0) {
            return ""; // NOI18N
        }
        StringBuilder sb = new StringBuilder();
        while(len-- > 0) {
            char c = dis.readChar();
            sb.append(c);
        }
        return sb.toString();
    }

    private static void skip(InputStream is, long len) throws IOException {
        while (len > 0) {
            long n = is.skip(len);
            if (n < 0) throw new EOFException("Missing " + len + " bytes.");                // NOI18N
            len -= n;
        }
    }

    private static void copyStreams(OutputStream out, InputStream in) throws IOException {
        byte [] buffer = new byte[4096];
        for (;;) {
            int n = in.read(buffer);
            if (n < 0) break;
            out.write(buffer, 0, n);
        }
    }

    private static DataOutputStream getOutputStream(File file, boolean append) throws IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, append)));
            } catch (IOException ioex) {
                retry++;
                if (retry > 7) {
                    throw ioex;
                }
                Thread.sleep(retry * 30);
            }
        }
    }

    private static DataInputStream getInputStream(File file) throws IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            } catch (IOException ioex) {
                retry++;
                if (retry > 7) {
                    throw ioex;
                }
                Thread.sleep(retry * 30);
            }
        }
    }

    @Override
    public void waitForProcessedStoring(VCSFileProxy file, String caller) {
        Semaphore s;
        synchronized(proccessedFiles) {
            s = proccessedFiles.get(file);
        }
        if(s != null) {
            long l = System.currentTimeMillis();
            try {
                long t9Timeout = getT9LockReleaseTimeOut();
                long timeout = t9Timeout >= 0 ? t9Timeout : LOCK_TIMEOUT;
                boolean aquired = s.tryAcquire(timeout, TimeUnit.SECONDS);
                if(aquired) {
                    s.release();
                } else {
                    LOG.log(Level.WARNING, "{0} Releasing lock on file: {1}", new Object[] {caller, FileUtils.getPath(file)}); // NOI18N
                    synchronized(proccessedFiles) {
                        proccessedFiles.remove(file);
                    }
                }
            } catch (InterruptedException ex) {
                // nothing
            }
            LOG.log(Level.FINER, "{0} for file {1} was blocked {2} millis.", new Object[] {caller, FileUtils.getPath(file), System.currentTimeMillis() - l}); // NOI18N
        }
    }

    private Semaphore lock(VCSFileProxy file, String caller) {
        Semaphore s;
        synchronized(proccessedFiles) {
            s = proccessedFiles.get(file);
            if(s == null) {
                s = new Semaphore(1, true);
                proccessedFiles.put(file, s);
            }
        }
        try {
            
            long t9Timeout = getT9LockTimeOut();
            long timeout = t9Timeout >= 0 ? t9Timeout : LOCK_TIMEOUT;
            long taken = System.currentTimeMillis();
            boolean acquired = s.tryAcquire(timeout, TimeUnit.SECONDS);
            taken = System.currentTimeMillis() - taken;
            if(t9Timeout > 0) {
                assert acquired;
            }
            if(acquired) {
                LOG.log(Level.FINE, "{0} acquired lock for {1}", new Object[]{caller, FileUtils.getPath(file)}); // NOI18N
            } else {
                LOG.log(Level.WARNING, "{0} Releasing lock on file: {1}", new Object[] {caller, FileUtils.getPath(file)}); // NOI18N
            }
            if(taken > 3000) {
                LOG.log(Level.WARNING, "{0} acquiring lock for {1} took too long {2}", new Object[] {caller, FileUtils.getPath(file), taken}); // NOI18N
            }
        } catch (InterruptedException ex) {
            return null;
        }
        return s;
    }    
    
    private long getT9LockReleaseTimeOut() {
        String t9yLockTimeOut = System.getProperty("netbeans.t9y.localhistory.release-lock.timeout", "-1"); // NOI18N
        try {
            long l = Long.parseLong(t9yLockTimeOut);
            return l;
        } catch (NumberFormatException numberFormatException) {
            return -1;
        }
    } 
    
    private long getT9LockTimeOut() {
        String t9yLockTimeOut = System.getProperty("netbeans.t9y.localhistory.lock.timeout", "-1"); // NOI18N
        try {
            long l = Long.parseLong(t9yLockTimeOut);
            return l;
        } catch (NumberFormatException numberFormatException) {
            return -1;
        }
    } 

    private class HistoryEntry {
        private long ts;
        private String from;
        private String to;
        private int status;
        HistoryEntry(long ts, String from, String to, int action) {
            this.ts = ts;
            this.from = from;
            this.to = to;
            this.status = action;
        }
        long getTimestamp() {
            return ts;
        }
        String getFrom() {
            return from;
        }
        String getTo() {
            return to;
        }
        int getStatus() {
            return status;
        }
    }

    private static class StoreDataFile {
        private final int status;
        private final long lastModified;
        private final String absolutePath;
        private final boolean isFile;

        private StoreDataFile(String absolutePath, int action, long lastModified, boolean isFile) {
            this.status = action;
            this.lastModified = lastModified;
            this.absolutePath = absolutePath;
            this.isFile = isFile;
        }

        int getStatus() {
            return status;
        }

        long getLastModified() {
            return lastModified;
        }

        String getAbsolutePath() {
            return absolutePath;
        }

        boolean isFile() {
            return isFile;
        }

        static synchronized StoreDataFile read(File storeFile) {
            DataInputStream dis = null;
            try {
                dis = getInputStream(storeFile);
                boolean isFile = dis.readBoolean();
                int action = dis.readInt();
                long modified  = dis.readLong();
                String fileName = readString(dis);
                return new StoreDataFile(fileName, action, modified, isFile);
            } catch (Exception e) {
                LocalHistory.LOG.log(Level.INFO, null, e);
            } finally {
                if (dis != null) {
                    try { dis.close(); } catch (IOException e) { }
                }
            }
            return null;
        }

        static synchronized void write(File storeFile, StoreDataFile value) {
            DataOutputStream dos = null;
            try {
                dos = getOutputStream(storeFile, false);
                StoreDataFile data = (StoreDataFile) value;
                dos.writeBoolean(data.isFile);
                dos.writeInt(data.getStatus());
                dos.writeLong(data.getLastModified());
                dos.writeInt(data.getAbsolutePath().length());
                dos.writeChars(data.getAbsolutePath());
                dos.flush();
            } catch (Exception e) {
                LocalHistory.LOG.log(Level.INFO, null, e);
            } finally {
                if (dos != null) {
                    try { dos.close(); } catch (IOException e) { }
                }
            }
        }
    }

    private class DataFilesTurboProvider implements TurboProvider {

        static final String ATTR_DATA_FILES = "localhistory.ATTR_DATA_FILES";                 // NOI18N

        @Override
        public boolean recognizesAttribute(String name) {
            return ATTR_DATA_FILES.equals(name);
        }

        @Override
        public boolean recognizesEntity(Object key) {
            return key instanceof File;
        }

        @Override
        public synchronized Object readEntry(Object key, String name, MemoryCache memoryCache) {
            assert key instanceof File;
            assert name != null;

            File storeFile = (File) key;
            if(!storeFile.exists()) {
                return null;
            }
            return StoreDataFile.read(storeFile);
        }

        @Override
        public synchronized boolean writeEntry(Object key, String name, Object value) {
            assert key instanceof File;
            assert value == null || value instanceof StoreDataFile;
            assert name != null;

            File storeFile = (File) key;
            if(value == null) {
                if(storeFile.exists()) {
                    storeFile.delete();
                }
                return true;
            }

            File parent = storeFile.getParentFile();
            if(!parent.exists()) {
                parent.mkdirs();
            }
            StoreDataFile.write(storeFile, (StoreDataFile) value);
            return true;
        }
    }

}
