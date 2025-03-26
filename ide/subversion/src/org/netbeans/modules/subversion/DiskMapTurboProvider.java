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

package org.netbeans.modules.subversion;

import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.turbo.CacheIndex;
import org.netbeans.modules.subversion.util.*;
import org.netbeans.modules.turbo.TurboProvider;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.Places;
import org.openide.util.NbBundle;

/**
 * Storage of file attributes with shortcut to retrieve all stored values.
 *
 * @author Maros Sandor
 */
class DiskMapTurboProvider implements TurboProvider {

    static final String ATTR_STATUS_MAP = "subversion.STATUS_MAP";  // NOI18N

    private static final int STATUS_VALUABLE = FileInformation.STATUS_MANAGED
            & ~FileInformation.STATUS_VERSIONED_UPTODATE & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    private static final String CACHE_DIRECTORY = "svncache"; // NOI18N
    private static final int DIRECTORY = Integer.highestOneBit(Integer.MAX_VALUE);
    private static final Logger LOG = Logger.getLogger(DiskMapTurboProvider.class.getName());

    private File                            cacheStore;

    private final CacheIndex index = createCacheIndex();
    private final CacheIndex conflictedIndex = createCacheIndex();
    private final CacheIndex ignoresIndex = createCacheIndex();
    private final boolean logModifiedFiles = Boolean.getBoolean("versioning.subversion.turbo.logModifiedFiles"); //NOI18N

    DiskMapTurboProvider() {
        initCacheStore();
    }

    File[] getIndexValues(File file, int includeStatus) {
        if (includeStatus == FileInformation.STATUS_VERSIONED_CONFLICT) {
            return conflictedIndex.get(file);
        } else if (includeStatus == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
            return ignoresIndex.get(file);
        } else if ((includeStatus & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0) {
            File[] files = index.get(file);
            File[] ignores = ignoresIndex.get(file);
            return mergeArrays(files, ignores);
        } else {
            return index.get(file);
        }
    }

    File[] getAllIndexValues() {
        File[] files = index.getAllValues();
        File[] ignores = ignoresIndex.getAllValues();
        return mergeArrays(files, ignores);
    }

    private File[] mergeArrays (File[] arr1, File[] arr2) {
        if (arr1.length == 0) {
            return arr2;
        } else if (arr2.length == 0) {
            return arr1;
        } else {
            Set<File> merged = new HashSet<>(Arrays.asList(arr2));
            merged.addAll(Arrays.asList(arr1));
            return merged.toArray(new File[0]);
        }
    }

    public void computeIndex() {
        long ts = System.currentTimeMillis();
        long entriesCount = 0;
        long failedReadCount = 0;
        try {
            if (!cacheStore.isDirectory()) {
                cacheStore.mkdirs();
            }
            
            File [] files;
            synchronized(this) {
                files = cacheStore.listFiles();
            }

            if(files == null) {
                return;
            }
            
            int modifiedFiles = 0;
            int locallyNewFiles = 0;
            Map<String, Integer> locallyNewFolders = new HashMap<String, Integer>();
            Map<String, Integer> modifiedFolders = new HashMap<String, Integer>();

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                synchronized(this) {
                    if (file.getName().endsWith(".bin") == false) { // NOI18N
                        // on windows list returns already deleted .new files
                        continue;
                    }
                    boolean readFailed = false;
                    int itemIndex = -1;
                    DataInputStream dis = null;
                    try {
                        int retry = 0;
                        while (true) {
                            try {
                                dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                                break;
                            } catch (IOException ioex) {
                                retry++;
                                if (retry > 7) {
                                    throw ioex;
                                }
                                Thread.sleep(retry * 30);
                            }
                        }

                        itemIndex = 0;
                        for (;;) {
                            ++itemIndex;
                            int pathLen;
                            try {
                                pathLen = dis.readInt();
                            } catch (EOFException e) {
                                // reached EOF, no entry for this key
                                break;
                            }
                            dis.readInt();
                            String path = readChars(dis, pathLen);
                            Map value = readValue(dis, path);
                            for (Iterator j = value.keySet().iterator(); j.hasNext();) {
                                entriesCount++;
                                File f = (File) j.next();
                                FileInformation info = (FileInformation) value.get(f);
                                if((info.getStatus() & FileInformation.STATUS_VERSIONED_CONFLICT) != 0) {
                                    conflictedIndex.add(f);
                                }
                                if ((info.getStatus() & STATUS_VALUABLE) != 0) {
                                    index.add(f);
                                    modifiedFiles++;
                                    addModifiedFile(modifiedFolders, f);
                                    if ((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0) {
                                        locallyNewFiles++;
                                        addLocallyNewFile(locallyNewFolders, info.isDirectory() ? f.getAbsolutePath() : f.getParent());
                                    }
                                }
                            }
                        }
                    } catch (EOFException e) {
                        logCorruptedCacheFile(file, itemIndex, e);
                        readFailed = true;
                    } catch (Exception e) {
                        Subversion.LOG.log(Level.SEVERE, null, e);
                    } finally {
                        if (dis != null) try { dis.close(); } catch (IOException e) {}
                    }
                    if (readFailed) {
                        // cache file is corrupted, delete it (will be recreated on-demand later)
                        file.delete();
                        failedReadCount++;
                    }
                }
            }
            if (locallyNewFiles > 1000) {
                logTooManyNewFiles(locallyNewFolders, locallyNewFiles);
            } else if (modifiedFiles > 5000) {
                logTooManyModifications(modifiedFolders, modifiedFiles);
            }
        } finally {
            Subversion.LOG.log(Level.INFO, "Finished indexing svn cache with {0} entries. Elapsed time: {1} ms.", new Object[]{entriesCount, System.currentTimeMillis() - ts});
            if(failedReadCount > 0) {
                Subversion.LOG.log(Level.INFO, " read failed {0} times.", failedReadCount);
            }
        }
    }

    @Override
    public boolean recognizesAttribute(String name) {
        return ATTR_STATUS_MAP.equals(name);
    }

    @Override
    public boolean recognizesEntity(Object key) {
        return key instanceof File;
    }

    @Override
    public synchronized Object readEntry(Object key, String name, MemoryCache memoryCache) {
        assert key instanceof File;
        assert name != null;

        boolean readFailed = false;
        File dir = (File) key;
        File store = getStore(dir);
        if (!store.isFile()) {
            return null;
        }

        String dirPath = dir.getAbsolutePath();
        int dirPathLen = dirPath.length();
        DataInputStream dis = null;
        int itemIndex = -1;
        try {

            int retry = 0;
            while (true) {
                try {
                    dis = new DataInputStream(new BufferedInputStream(new FileInputStream(store)));
                    break;
                } catch (IOException ioex) {
                    retry++;
                    if (retry > 7) {
                        throw ioex;
                    }
                    Thread.sleep(retry * 30);
                }
            }

            itemIndex = 0;
            for (;;) {
                ++itemIndex;
                int pathLen;
                try {
                    pathLen = dis.readInt();
                } catch (EOFException e) {
                    // reached EOF, no entry for this key
                    break;
                }
                int mapLen = dis.readInt();
                if (pathLen != dirPathLen) {
                    skip(dis, pathLen * 2 + mapLen);
                } else {
                    String path = readChars(dis, pathLen);
                    if (dirPath.equals(path)) {
                        return readValue(dis, path);
                    } else {
                        skip(dis, mapLen);
                    }
                }
            }
        } catch (EOFException e) {
            logCorruptedCacheFile(store, itemIndex, e);
            readFailed = true;
        } catch (Exception e) {
            Subversion.LOG.log(Level.INFO, e.getMessage(), e);
            readFailed = true;
        } finally {
            if (dis != null) try { dis.close(); } catch (IOException e) {}
        }
        if (readFailed) store.delete(); // cache file is corrupted, delete it (will be recreated on-demand later)
        return null;
    }

    @Override
    public synchronized boolean writeEntry(Object key, String name, Object value) {
        assert key instanceof File;
        assert name != null;

        if (value != null) {
            if (!(value instanceof Map)) return false;
            if (!isValuable(value)) value = null;
        }

        File dir = (File) key;
        String dirPath = dir.getAbsolutePath();
        int dirPathLen = dirPath.length();
        File store = getStore(dir);

        if (value == null && !store.exists()) return true;

        File storeNew = new File(store.getParentFile(), store.getName() + ".new"); // NOI18N
        if (!cacheStore.isDirectory()) {
            cacheStore.mkdirs();
        }

        DataOutputStream oos = null;
        DataInputStream dis = null;
        boolean readFailed = false;
        int itemIndex = -1;
        try {
            oos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(storeNew)));
            if (value != null) {
                writeEntry(oos, dirPath, value);
            }
            if (store.exists()) {
                int retry = 0;
                while (true) {
                    try {
                        dis = new DataInputStream(new BufferedInputStream(new FileInputStream(store)));
                        break;
                    } catch (IOException ioex) {
                        retry++;
                        if (retry > 7) {
                            throw ioex;
                        }
                        Thread.sleep(retry * 30);
                    }
                }

                itemIndex = 0;
                for (;;) {
                    ++itemIndex;
                    int pathLen;
                    try {
                        pathLen = dis.readInt();
                    } catch (EOFException e) {
                        break;
                    }
                    int mapLen = dis.readInt();
                    if (pathLen == dirPathLen) {
                        String path = readChars(dis, pathLen);
                        if (dirPath.equals(path)) {
                            skip(dis, mapLen);
                        } else {
                            oos.writeInt(pathLen);
                            oos.writeInt(mapLen);
                            oos.writeChars(path);
                            copyStreams(oos, dis, mapLen);
                        }
                    } else {
                        oos.writeInt(pathLen);
                        oos.writeInt(mapLen);
                        copyStreams(oos, dis, mapLen + pathLen * 2);
                    }
                }
            }
        } catch (EOFException e) {
            logCorruptedCacheFile(store, itemIndex, e);
            readFailed = true;
        } catch (FileNotFoundException ex) {
            Subversion.LOG.log(Level.INFO, "File could not be created, check if you are running only a single instance of netbeans for this userdir", ex); //NOI18N
            return true;
        } catch (Exception e) {
            Subversion.LOG.log(Level.INFO, "Copy: " + store.getAbsolutePath() + " to: " + storeNew.getAbsolutePath(), e);  // NOI18N
            return true;
        } finally {
            if (oos != null) try { oos.close(); } catch (IOException e) {}
            if (dis != null) try { dis.close(); } catch (IOException e) {}
        }
        adjustIndex(dir, value);

        if (readFailed) {
            store.delete(); // cache file is corrupted, delete it (will be recreated on-demand later)
            return true;
        }
        try {
            FileUtils.renameFile(storeNew, store);
        } catch (FileNotFoundException ex) {
            Subversion.LOG.log(Level.INFO, 
                    "File could not be renamed, check if you are running only a single instance of netbeans for this userdir", //NOI18N
                    ex);
        } catch (IOException ex) {
            Subversion.LOG.log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private void adjustIndex(File dir, Object value) {
        // the file must be a folder or must not exist
        // adding existing file is forbidden
        assert !dir.isFile();
        Map map = (Map) value;
        Set set = map != null ? map.keySet() : null;

        // all modified files
        Set<File> conflictedSet = new HashSet<File>();
        Set<File> newSet = new HashSet<File>();
        Set<File> ignoredSet = new HashSet<File>();
        if(set != null) {
            for (Iterator i = set.iterator(); i.hasNext();) {
                File file = (File) i.next();
                FileInformation info = (FileInformation) map.get(file);

                // conflict
                if((info.getStatus() & FileInformation.STATUS_VERSIONED_CONFLICT) != 0) {
                    conflictedSet.add(file);
                }

                if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                    ignoredSet.add(file);
                } else {
                    if ((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0) {
                        // this can hardly happen
                        assert false;
                        ignoredSet.add(file);
                    }
                    // all but uptodate
                    if((info.getStatus() & STATUS_VALUABLE) != 0) {
                        newSet.add(file);
                    }
                }
            }
        }
        index.add(dir, newSet);
        ignoresIndex.add(dir, ignoredSet);
        conflictedIndex.add(dir, conflictedSet);
    }

    /**
     * Logs the EOFException and the corrupted cache file
     * @param file file which caused the error
     * @param itemIndex a position in the file when the error showed
     * @param e
     */
    private void logCorruptedCacheFile(File file, int itemIndex, EOFException e) {
        try {
            File tmpFile = Files.createTempFile("svn_", ".bin").toFile();
            Subversion.LOG.log(Level.INFO, "Corrupted cache file " + file.getAbsolutePath() + " at position " + itemIndex, e);
            FileUtils.copyFile(file, tmpFile);
            byte[] contents = FileUtils.getFileContentsAsByteArray(tmpFile);
            Subversion.LOG.log(Level.INFO, "Corrupted cache file length: {0}", contents.length);
            String encodedContent = Base64.getEncoder().encodeToString(contents); // log the file contents
            Subversion.LOG.log(Level.INFO, "Corrupted cache file content:\n{0}\n", encodedContent);
            Exception ex = new Exception("Corrupted cache file \"" + file.getAbsolutePath() + "\", please report in subversion module issues and attach "
                    + tmpFile.getAbsolutePath() + " plus the IDE message log", e);
            Subversion.LOG.log(Level.INFO, null, ex);
        } catch (IOException ex) {
            Subversion.LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void skip(InputStream is, long len) throws IOException {
        while (len > 0) {
            long n = is.skip(len);
            if (n < 0) throw new EOFException("Missing " + len + " bytes.");  // NOI18N
            len -= n;
        }
    }

    private String readChars(DataInputStream dis, int len) throws IOException {
        if (len < 0 || len > 1024 * 1024 * 10) throw new EOFException("Len: " + len); // preventing from OOME
        StringBuilder sb = new StringBuilder(len);
        while (len-- > 0) {
            sb.append(dis.readChar());
        }
        return sb.toString();
    }

    private Map<File, FileInformation> readValue(DataInputStream dis, String dirPath) throws IOException {
        Map<File, FileInformation> map = new HashMap<File, FileInformation>();
        int len = dis.readInt();
        while (len-- > 0) {
            int nameLen = dis.readInt();
            String name = readChars(dis, nameLen);
            File file = new File(dirPath, name);
            int status = dis.readInt();
            FileInformation info = new FileInformation(status & (DIRECTORY - 1), status > (DIRECTORY - 1));
            map.put(file, info);
        }
        return map;
    }

    private void writeEntry(DataOutputStream dos, String dirPath, Object value) throws IOException {

        Map map = (Map) value;
        Set set = map.keySet();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(set.size() * 50);
        DataOutputStream temp = new DataOutputStream(baos);

        temp.writeInt(set.size());
        for (Iterator i = set.iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = (FileInformation) map.get(file);
            temp.writeInt(file.getName().length());
            temp.writeChars(file.getName());
            temp.writeInt(info.getStatus() + (info.isDirectory() ? DIRECTORY : 0));
        }
        temp.close();
        byte [] valueBytes = baos.toByteArray();

        dos.writeInt(dirPath.length());
        dos.writeInt(valueBytes.length);
        dos.writeChars(dirPath);
        dos.write(valueBytes);
    }

    private boolean isValuable(Object value) {
        Map map = (Map) value;
        for (Iterator i = map.values().iterator(); i.hasNext();) {
            FileInformation info = (FileInformation) i.next();
            if ((info.getStatus() & STATUS_VALUABLE) != 0) return true;
        }
        return false;
    }

    private File getStore(File dir) {
        String dirPath = dir.getAbsolutePath();
        int dirHash = dirPath.hashCode();
        return new File(cacheStore, Integer.toString(dirHash % 173 + 172) + ".bin"); // NOI18N
    }

    private void initCacheStore() {
        cacheStore = Places.getCacheSubdirectory(CACHE_DIRECTORY);
    }

    private static void copyStreams(OutputStream out, InputStream in, int len) throws IOException {
        byte [] buffer = new byte[4096];
        int totalLen = len;
        for (;;) {
            int n = (len >= 0 && len <= 4096) ? len : 4096;
            n = in.read(buffer, 0, n);
            if (n < 0) throw new EOFException("Missing " + len + " bytes from total " + totalLen + " bytes.");  // NOI18N
            out.write(buffer, 0, n);
            if ((len -= n) == 0) break;
        }
        out.flush();
    }

    private static CacheIndex createCacheIndex() {
        return new CacheIndex() {
            @Override
            protected boolean isManaged(File file) {
                return SvnUtils.isManaged(file);
}
        };
    }

    private void addLocallyNewFile (Map<String, Integer> locallyNewFolders, String path) {
        if (path == null) {
            return;
        }
        boolean toAdd = true;
        String toRemove = null;
        Integer val = locallyNewFolders.get(path);
        if (val != null) {
            locallyNewFolders.put(path, val + 1);
            return;
        }
        for (Map.Entry<String, Integer> e : locallyNewFolders.entrySet()) {
            if (path.startsWith(e.getKey() + File.separator)) {
                e.setValue(e.getValue() + 1);
                toAdd = false;
                break;
            } else if (e.getKey().startsWith(path + File.separator)) {
                toRemove = e.getKey();
                break;
            }
        }
        if (toRemove != null) {
            locallyNewFolders.put(path, locallyNewFolders.remove(toRemove));
        } else if (toAdd) {
            locallyNewFolders.put(path, 1);
        }
    }

    private void addModifiedFile (Map<String, Integer> modifiedFolders, File file) {
        if (logModifiedFiles) {
        File topmost = Subversion.getInstance().getTopmostManagedAncestor(file);
        if (topmost != null) {
            String path = topmost.getAbsolutePath();
            Integer val = modifiedFolders.get(path);
            if (val == null) {
                modifiedFolders.put(path, 1);
            } else {
                modifiedFolders.put(path, val + 1);
            }
        }
        }
    }

    @NbBundle.Messages({
        "# {0} - number of changes", "# {1} - the biggest unversioned folders",
        "MSG_FileStatusCache.cacheTooBig.newFiles.text=Subversion cache contains {0} locally new (uncommitted) files. "
            + "That many uncommitted files may cause performance problems when accessing the working copy. "
            + "You should consider committing or permanently ignoring these files. "
            + "Candidates for ignoring are: {1}",
        "# {0} - folder path", "# {1} - number of contained unversioned files",
        "MSG_FileStatusCache.cacheTooBig.ignoreCandidate={0}: {1} new files"
    })
    private void logTooManyNewFiles (Map<String, Integer> locallyNewFolders, int locallyNewFiles) {
        Map<Integer, List<String>> sortedFolders = sortFolders(locallyNewFolders);
        List<String> biggestFolders = new ArrayList<String>(3);
        outer: for (Map.Entry<Integer, List<String>> e : sortedFolders.entrySet()) {
            for (String folder : e.getValue()) {
                biggestFolders.add(Bundle.MSG_FileStatusCache_cacheTooBig_ignoreCandidate(folder, e.getKey()));
                if (biggestFolders.size() == 3) {
                    break outer;
                }
            }
        }
        LOG.log(Level.WARNING, Bundle.MSG_FileStatusCache_cacheTooBig_newFiles_text(locallyNewFiles, biggestFolders));
    }

    @NbBundle.Messages({
        "# {0} - number of changes", "# {1} - checkouts with the highest number of modifications",
        "MSG_FileStatusCache.cacheTooBig.text=Subversion cache contains {0} locally modified files. "
            + "That many uncommitted files may cause performance problems when accessing the working copy. "
            + "You should consider committing or reverting these changes. "
            + "Checkouts: {1}",
        "# {0} - number of changes",
        "MSG_FileStatusCache.cacheTooBig.text.unknownFiles=Subversion cache contains {0} locally modified files. "
            + "That many uncommitted files may cause performance problems when accessing the working copy. "
            + "Run the IDE with -J-Dversioning.subversion.turbo.logModifiedFiles=true to know the exact checkout causing the problem.",
        "# {0} - checkout folder", "# {1} - number of contained modified files",
        "MSG_FileStatusCache.cacheTooBig.checkoutWithModifications={0}: {1} modifications"
    })
    private void logTooManyModifications (Map<String, Integer> modifiedFolders, int modifiedFiles) {
        Map<Integer, List<String>> sortedFolders = sortFolders(modifiedFolders);
        List<String> biggestFolders = new ArrayList<String>(3);
        outer: for (Map.Entry<Integer, List<String>> e : sortedFolders.entrySet()) {
            for (String folder : e.getValue()) {
                biggestFolders.add(Bundle.MSG_FileStatusCache_cacheTooBig_checkoutWithModifications(folder, e.getKey()));
                if (biggestFolders.size() == 3) {
                    break outer;
                }
            }
        }
        if (logModifiedFiles) {
            LOG.log(Level.WARNING, Bundle.MSG_FileStatusCache_cacheTooBig_text(modifiedFiles, biggestFolders));
        } else {
            LOG.log(Level.WARNING, Bundle.MSG_FileStatusCache_cacheTooBig_text_unknownFiles(modifiedFiles));
        }
    }
    
    private static Map<Integer, List<String>> sortFolders (Map<String, Integer> unsortedFolders) {
        Map<Integer, List<String>> sortedFolders = new TreeMap<Integer, List<String>>(new Comparator<Integer>() {
            @Override
            public int compare (Integer o1, Integer o2) {
                return - o1.compareTo(o2);
            }
        });
        for (Map.Entry<String, Integer> e : unsortedFolders.entrySet()) {
            List<String> folders = sortedFolders.get(e.getValue());
            if (folders == null) {
                sortedFolders.put(e.getValue(), folders = new ArrayList<String>());
            }
            folders.add(e.getKey());
        }
        return sortedFolders;
    }

}
