/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.versioning.historystore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbPreferences;

/**
 *
 * @author ondra
 */
public class Storage {

    private final File storageFolder;
    private boolean storageExists;
    private boolean storageAccessible;
    private static final String DATA_FILE = "data";                  // NOI18N
    static final String PREF_KEY_TTL = "HistoryStorage.ttl"; //NOI18N - how long to keep data in the cache, in days
    static final int INDEFINITE_TTL = Integer.MAX_VALUE;
    private static final String KIND_FILE_CONTENT = "fileContent"; //NOI18N
    private static final String KIND_REVISION_INFO = "revisionInfo"; //NOI18N
    private static final String REVISION_CONTENT_FN = "revisionContent"; //NOI18N
    private static final FilenameFilter FILE_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return !DATA_FILE.equals(name);
        }
    };

    Storage(File storageFolder) {
        this.storageFolder = storageFolder;
        ensureAccessible();
        if (!storageAccessible) {
            StorageManager.LOG.log(Level.WARNING, "Cannot write or read from storage at {0}", storageFolder.getAbsolutePath()); //NOI18N
        }
    }

    /**
     * Copies a file's cached content in the given revision and returns it in a temporary file as a returned value.
     * If no cached content is available, the returned file is not created.
     * @param repositoryPath repository path to the file
     * @param filename returned filename
     * @param revision revision
     * @return file's content in the given revision
     */
    public synchronized File getContent (String repositoryPath, String filename, String revision) {
        File content = new File(Utils.getTempFolder(), filename); //NOI18N
        if (storageAccessible) {
            content.deleteOnExit();
            Entry entry = getEntry(repositoryPath, revision, KIND_FILE_CONTENT, false);
            InputStream is = null;
            try {
                is = entry.getStoreFileInputStream();
                Utils.copyStreamsCloseAll(new FileOutputStream(content), is);
                is = null;
            } catch (FileNotFoundException ex) {
                //
            } catch (IOException ex) {
                StorageManager.LOG.log(Level.INFO, "getContent(): Cannot read file's content", ex); //NOI18N
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
        return content;
    }
    
    /**
     * Stores file's content in the given revision
     * @param repositoryPath repository path to the file
     * @param revision revision of the file's content
     * @param content input stream of the file in the given revision
     */
    public synchronized void setContent (String repositoryPath, String revision, InputStream content) {
        if (ensureAccessible()) {
            Entry entry = getEntry(repositoryPath, revision, KIND_FILE_CONTENT, true);
            try {
                OutputStream os = entry.getStoreFileOutputStream();
                Utils.copyStreamsCloseAll(os, content);
            } catch (IOException ex) {
                StorageManager.LOG.log(Level.INFO, "setContent(): Cannot save stream's content", ex); //NOI18N
            }
        } else {
            try {
                content.close();
            } catch (IOException ex) {
                StorageManager.LOG.log(Level.INFO, "setContent(): Cannot close stream's content", ex); //NOI18N
            }
        }
    }

    /**
     * Stores file's content in the given revision
     * @param repositoryPath repository path to the file
     * @param revision revision of the file's content
     * @param content content of the file in the given revision
     */
    public synchronized void setContent (String repositoryPath, String revision, File content) {
        if (ensureAccessible()) {
            Entry entry = getEntry(repositoryPath, revision, KIND_FILE_CONTENT, true);
            try {
                OutputStream os = entry.getStoreFileOutputStream();
                Utils.copyStreamsCloseAll(os, new FileInputStream(content));
            } catch (IOException ex) {
                StorageManager.LOG.log(Level.INFO, "setContent(): Cannot save file's content", ex); //NOI18N
            }
        }
    }

    /**
     * Returns stored information about a revision. The content stored can be anything 
     * passed in the last call of {@link #setRevisionInfo(java.lang.String, java.lang.String, java.io.ByteArrayInputStream) setRevisionInfo}
     * @param revision a unique identifier of the data persisted
     * @return content of the requested data or <code>null</code> if no such data found
     * @since 1.24
     */
    public synchronized byte[] getRevisionInfo (String revision) {
        byte[] content = null;
        if (storageAccessible) {
            Entry entry = getEntry(revision, REVISION_CONTENT_FN, KIND_FILE_CONTENT, false);
            InputStream is = null;
            try {
                is = entry.getStoreFileInputStream();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                Utils.copyStreamsCloseAll(os, is);
                is = null;
                content = os.toByteArray();
            } catch (FileNotFoundException ex) {
                //
            } catch (IOException ex) {
                StorageManager.LOG.log(Level.INFO, "getRevisionInfo(): Cannot read file's content", ex); //NOI18N
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
        return content;
    }
    
    /**
     * Persists whatever content you wish to save for a given revision.
     * @param revision unique revision id
     * @param content content to save
     * @since 1.24
     */
    public synchronized void setRevisionInfo (String revision, InputStream content) {
        if (ensureAccessible()) {
            Entry entry = getEntry(revision, REVISION_CONTENT_FN, KIND_FILE_CONTENT, true);
            try {
                OutputStream os = entry.getStoreFileOutputStream();
                Utils.copyStreamsCloseAll(os, content);
            } catch (IOException ex) {
                StorageManager.LOG.log(Level.INFO, "setRevisionInfo(): Cannot save revision content", ex); //NOI18N
            }
        } else {
            try {
                content.close();
            } catch (IOException ex) {
                StorageManager.LOG.log(Level.INFO, "setRevisionInfo(): Cannot close stream's content", ex); //NOI18N
            }
        }
    }

    private Entry getEntry(String key, String name, String expectedKind, boolean write) {
        return new Entry(getStoreFile(key, name, expectedKind, write));
    }

    private File getStoreFile (String key, String name, String expectedKind, boolean write) {
        File storeFolder = getStoreFolder(key, expectedKind);
        if (write) { 
            if(!storeFolder.exists()) {
                storeFolder.mkdirs();
            }
            StoreDataFile.write(new File(storeFolder, DATA_FILE), new StoreDataFile(key));
        }
        return new File(storeFolder, name);
    }

    private File getStoreFolder (String key, String expectedKind) {
        File storeFolder = getStoreFolderName(key);
        int i = 0;
        while (storeFolder.exists()) {
            // check for collisions 
            StoreDataFile data = StoreDataFile.read(new File(storeFolder, DATA_FILE), expectedKind);
            if (data == null || data.getKey ().equals(key)) {
                break;
            }
            storeFolder = getStoreFolderName(key + "." + i++); //NOI18N
        }
        return storeFolder;
    }

    private File getStoreFolderName(String filePath) {
        int fileHash = filePath.hashCode();
        String storeFileName = getMD5(filePath);
        File storeIndex = new File(storageFolder, Integer.toString(fileHash % 173 + 172)); //NOI18N
        return new File(storeIndex, storeFileName); //NOI18N
    }

    synchronized void cleanUp () {
        StorageManager.LOG.log(Level.FINE, "Cleaning storage: {0}", storageFolder); //NOI18N
        long ts = System.currentTimeMillis();
        File[] contentFolders = storageFolder.listFiles();
        if (contentFolders != null) {
            for (File contentFolder : contentFolders) { // 165
                File[] hashedFolders = contentFolder.listFiles();
                if (hashedFolders != null) {
                    boolean empty = true;
                    for (File hashedFolder : hashedFolders) { // 5dfad1d0b203a1b1d3fcea4a05ee5c88
                        if (cleanUp(hashedFolder)) {
                            Utils.deleteRecursively(hashedFolder);
                        } else {
                            empty = false;
                        }
                    }
                    if (empty) {
                        Utils.deleteRecursively(contentFolder);
                    }
                }
            }
        }
        contentFolders = storageFolder.listFiles();
        if (contentFolders == null || contentFolders.length == 0) {
            // history is empty, let's delete the storage folder
            Utils.deleteRecursively(storageFolder);
            storageExists = storageAccessible = false;
            StorageManager.LOG.log(Level.FINE, "Empty storage deleted: {0}", new Object[]{storageFolder}); //NOI18N
        }
        StorageManager.LOG.log(Level.FINE, "Storage cleaned: {0}, took {1} ms", new Object[] { storageFolder, System.currentTimeMillis() - ts }); //NOI18N
    }

    private boolean cleanUp (File contentFolder) {
        boolean empty = true;
        File[] contentFiles = contentFolder.listFiles(FILE_FILTER);
        long currentTS = System.currentTimeMillis();
        if (contentFiles != null) {
            for (File contentFile : contentFiles) {
                if (contentFile.lastModified() < currentTS - getTTL()) {
                    Utils.deleteRecursively(contentFile);
                } else {
                    empty = false;
                }
            }
        }
        return empty;
    }

    private boolean ensureAccessible () {
        if (!storageAccessible && !storageExists) {
            storageFolder.mkdirs();
            storageAccessible = storageFolder.canWrite() && storageFolder.canRead();
            storageExists = storageAccessible || storageFolder.exists();
        }
        return storageAccessible;
    }

    private long getTTL() {
        return 1000 * 3600 * 24 * (long) NbPreferences.forModule(Storage.class).getInt(PREF_KEY_TTL, INDEFINITE_TTL); //NOI18N keep data indefinitely as default
    }

    private static class StoreDataFile {

        private final String key;
        private static final int VERSION = 2;

        private StoreDataFile(String key) {
            this.key = key;
        }

        String getKey () {
            return key;
        }

        static synchronized StoreDataFile read (File storeFile, String expectedKind) {
            DataInputStream dis = null;
            try {
                if (storeFile.exists()) {
                    dis = getInputStream(storeFile);
                    long version = dis.readInt();
                    boolean found = false;
                    if (version == VERSION) {
                        String kind = dis.readUTF();
                        if (expectedKind.equals(kind)) {
                            String key = dis.readUTF();
                            return new StoreDataFile(key);
                        }
                    } else {
                        // old data file version, delete the whole obsolete folder
                        Utils.deleteRecursively(storeFile.getParentFile());
                    }
                }
            } catch (FileNotFoundException e) {
                StorageManager.LOG.log(Level.FINEST, null, e);
            } catch (Exception e) {
                StorageManager.LOG.log(Level.FINE, null, e);
            } finally {
                if (dis != null) {
                    try {
                        dis.close();
                    } catch (IOException e) {
                    }
                }
            }
            return null;
        }

        static synchronized void write(File storeFile, StoreDataFile value) {
            DataOutputStream dos = null;
            try {
                dos = getOutputStream(storeFile, false);
                StoreDataFile data = (StoreDataFile) value;
                dos.writeInt(VERSION);
                dos.writeUTF(data.getKey ());
                dos.flush();
            } catch (FileNotFoundException e) {
                StorageManager.LOG.log(Level.FINEST, null, e);
            } catch (Exception e) {
                StorageManager.LOG.log(Level.FINE, null, e);
            } finally {
                if (dos != null) {
                    try {
                        dos.close();
                    } catch (IOException e) {
                    }
                }
            }
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

    static String getMD5(String name) {
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
            if (hex.length() == 1) {
                hex = "0" + hex;                                                // NOI18N
            }
            ret.append(hex);
        }
        return ret.toString();
    }
}
