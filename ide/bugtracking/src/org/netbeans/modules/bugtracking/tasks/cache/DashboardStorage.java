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
package org.netbeans.modules.bugtracking.tasks.cache;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.tasks.cache.StorageUtils.FileLocks;
import org.netbeans.modules.bugtracking.tasks.cache.StorageUtils.FileLocks.FileLock;
import org.netbeans.modules.bugtracking.commons.TextUtils;
import org.openide.modules.Places;

/**
 *
 * @author jpeska
 */
public class DashboardStorage {

    private static DashboardStorage instance;
    private static final String STORAGE_FILE = "storage";              // NOI18N
    private static final String CLOSED_CAT_FILE = "closedCategories";              // NOI18N
    private static final String CLOSED_REPO_FILE = "closedRepositories";              // NOI18N
    private static final String STORAGE_VERSION_1_0 = "1.0";            // NOI18N
    private static final String STORAGE_VERSION = STORAGE_VERSION_1_0;  // NOI18N
    private static final String CATEGORY_SUFIX = ".c";
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.tasks.ui.cache.CategoryStorage"); // NOI18N
    private File storageFolder;

    private DashboardStorage() {
    }

    public static DashboardStorage getInstance() {
        if (instance == null) {
            instance = new DashboardStorage();
            instance.initStorage();
        }
        return instance;
    }

    private void initStorage() {
        storageFolder = getStorageRootFile();
        getStorageFolder(storageFolder);
        writeStorage();
    }

    public void storeClosedCategories(List<String> closedCategoryNames) {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the category storage in awt"; // NOI18N
        File closedCategoryFile = new File(getStorageFolder(storageFolder), CLOSED_CAT_FILE);
        storeClosedEntries(closedCategoryFile, closedCategoryNames);
    }

    public void storeClosedRepositories(List<String> closedCategoryIds) {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the category storage in awt"; // NOI18N
        File closedRepositoryFile = new File(getStorageFolder(storageFolder), CLOSED_REPO_FILE);
        storeClosedEntries(closedRepositoryFile, closedCategoryIds);
    }

    private void storeClosedEntries(File file, List<String> closedEntries) {
        LOG.log(Level.FINE, "start storing closed entries"); // NOI18N
        FileLock lock = null;
        DataOutputStream dos = null;
        try {
            lock = FileLocks.getLock(file);
            synchronized (lock) {
                if (closedEntries.isEmpty()) {
                    file.delete();
                }
                dos = getClosedOutputStream(file);
                for (String entry : closedEntries) {
                    writeString(dos, entry);
                }
                dos.flush();
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        } finally {
            LOG.log(Level.FINE, "finished storing closed entries"); // NOI18N
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                }
            }
            if (lock != null) {
                lock.release();
            }
        }
    }

    public List<String> readClosedCategories() {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the category storage in awt"; // NOI18N
        File closedCategoryFile = new File(getStorageFolder(storageFolder), CLOSED_CAT_FILE);
        return readClosedEntries(closedCategoryFile);
    }

    public List<String> readClosedRepositories() {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the repository storage in awt"; // NOI18N
        File closedRepositoryFile = new File(getStorageFolder(storageFolder), CLOSED_REPO_FILE);
        return readClosedEntries(closedRepositoryFile);
    }

    private List<String> readClosedEntries(File file) {
        LOG.log(Level.FINE, "start reading closed entries"); // NOI18N
        DataInputStream dis = null;
        FileLock lock = null;
        try {
            lock = FileLocks.getLock(file);
            synchronized (lock) {
                dis = getClosedInputStream(file);
                return readEntries(dis);
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
            return Collections.emptyList();
        } finally {
            LOG.log(Level.FINE, "finished reading closed entries"); // NOI18N
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                }
            }
            if (lock != null) {
                lock.release();
            }
        }
    }

    private List<String> readEntries(DataInputStream dis) throws IOException {
        if (dis == null) {
            return Collections.emptyList();
        }
        List<String> entries = new ArrayList<String>();
        while (true) {
            String entry;
            try {
                entry = readString(dis);
            } catch (EOFException e) {
                break;
            }
            entries.add(entry);
        }
        return entries;
    }

    public void storeCategory(String categoryName, List<TaskEntry> tasks) {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the category storage in awt"; // NOI18N
        LOG.log(Level.FINE, "start storing category {0}", categoryName); // NOI18N
        FileLock lock = null;
        DataOutputStream dos = null;
        try {
            File categoryFile = getCategoryFile(getStorageFolder(storageFolder), categoryName);
            lock = FileLocks.getLock(categoryFile);
            synchronized (lock) {
                dos = getCategoryOutputStream(categoryFile);
                for (TaskEntry taskEntry : tasks) {
                    writeString(dos, taskEntry.getIssueId());
                    writeString(dos, taskEntry.getRepositoryId());
                }
                dos.flush();
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        } finally {
            LOG.log(Level.FINE, "finished storing category {0}", categoryName); // NOI18N
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                }
            }
            if (lock != null) {
                lock.release();
            }
        }
    }

    public boolean renameCategory(String oldName, String newName) {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the category storage in awt"; // NOI18N
        LOG.log(Level.FINE, "start renaming category from {0} to {1}", new Object[] {oldName, newName}); // NOI18N
        FileLock lock = null;
        try {
            File categoryFile = getCategoryFile(getStorageFolder(storageFolder), oldName);
            lock = FileLocks.getLock(categoryFile);
            synchronized (lock) {
                return categoryFile.renameTo(getCategoryFile(storageFolder, newName));
            }
        } catch (SecurityException ex) {
            LOG.log(Level.WARNING, "Not able to delete category file", ex); // NOI18N
            return false;
        } finally {
            LOG.log(Level.FINE, "finished renaming category from {0} to {1}", new Object[] {oldName, newName}); // NOI18N
            if (lock != null) {
                lock.release();
            }
        }
    }

    public void deleteCategories(String... categoryNames) {
        for (String name : categoryNames) {
            deleteCategory(name);
        }
    }

    public boolean deleteCategory(String categoryName) {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the category storage in awt"; // NOI18N
        LOG.log(Level.FINE, "start deleting category {0}", categoryName); // NOI18N
        FileLock lock = null;
        try {
            File categoryFile = getCategoryFile(getStorageFolder(storageFolder), categoryName);
            lock = FileLocks.getLock(categoryFile);
            synchronized (lock) {
                return categoryFile.delete();
            }
        } catch (SecurityException ex) {
            LOG.log(Level.WARNING, "Not able to delete category file", ex); // NOI18N
            return false;
        } finally {
            LOG.log(Level.FINE, "finished deleting category {0}", categoryName); // NOI18N
            if (lock != null) {
                lock.release();
            }
        }
    }

    public List<CategoryEntry> readCategories() {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the category storage in awt"; // NOI18N
        List<CategoryEntry> categories = new ArrayList<CategoryEntry>();
        File[] files = getStorageFolder(storageFolder).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.equals(getStorageFolder(storageFolder)) && name.endsWith(CATEGORY_SUFIX);
            }
        });
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String categoryName = TextUtils.decodeURL(file.getName().replace(CATEGORY_SUFIX, ""));
            List<TaskEntry> taskEntries = readCategory(categoryName);
            if (taskEntries == null) {
                continue;
            }
            CategoryEntry categoryEntry = new CategoryEntry(categoryName, taskEntries);
            categories.add(categoryEntry);
        }

        return categories;
    }

    public List<TaskEntry> readCategory(String categoryName) {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the category storage in awt"; // NOI18N
        LOG.log(Level.FINE, "start reading category {0}", categoryName); // NOI18N

        DataInputStream dis = null;
        FileLock lock = null;
        try {
            File categoryFile = getCategoryFile(getStorageFolder(storageFolder), categoryName);
            if (!categoryFile.exists()) {
                return null;
            }
            lock = FileLocks.getLock(categoryFile);
            synchronized (lock) {
                dis = getCategoryInputStream(categoryFile);
                return readCategory(dis);
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
            return null;
        } finally {
            LOG.log(Level.FINE, "finished reading category {0}", categoryName); // NOI18N
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                }
            }
            if (lock != null) {
                lock.release();
            }
        }
    }

    private List<TaskEntry> readCategory(DataInputStream dis) throws IOException {
        if (dis == null) {
            return Collections.emptyList();
        }
        List<TaskEntry> ids = new ArrayList<TaskEntry>();
        while (true) {
            String idIssue;
            String idRepository;
            try {
                idIssue = readString(dis);
                idRepository = readString(dis);
            } catch (EOFException e) {
                break;
            }
            ids.add(new TaskEntry(idIssue, idRepository));
        }
        return ids;
    }

    private DataOutputStream getCategoryOutputStream(File categoryFile) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(categoryFile, false)));
        ZipEntry entry = new ZipEntry(categoryFile.getName());
        zos.putNextEntry(entry);
        return new DataOutputStream(zos);
    }

    private DataInputStream getCategoryInputStream(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
        zis.getNextEntry();
        return new DataInputStream(zis);
    }

    private DataOutputStream getClosedOutputStream(File closedFile) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(closedFile, false)));
        ZipEntry entry = new ZipEntry(closedFile.getName());
        zos.putNextEntry(entry);
        return new DataOutputStream(zos);
    }

    private DataInputStream getClosedInputStream(File closedFile) throws IOException {
        if (!closedFile.exists()) {
            return null;
        }
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(closedFile)));
        zis.getNextEntry();
        return new DataInputStream(zis);
    }

    private File getCategoryFile(File folder, String name) {
        return new File(folder, TextUtils.encodeURL(name) + CATEGORY_SUFIX);
    }

    private File getStorageRootFile() {
        File userDir = Places.getUserDirectory();
        return new File(new File(new File(userDir, "var"), "bugtracking"), "dashboard");               // NOI18N
    }

    private void writeStorage() {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(new File(getStorageFolder(storageFolder), STORAGE_FILE), false));
            writeString(dos, STORAGE_VERSION);
            dos.flush();
        } catch (IOException e) {
            LOG.log(Level.INFO, null, e);
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void writeString(DataOutputStream dos, String str) throws IOException {
        if (str != null) {
            dos.writeInt(str.length());
            dos.writeChars(str);
        } else {
            dos.writeInt(0);
        }
    }

    private static String readString(DataInputStream dis) throws IOException {
        int len = dis.readInt();
        if (len == 0) {
            return "";                                                          // NOI18N
        }
        StringBuilder sb = new StringBuilder();
        while (len-- > 0) {
            char c = dis.readChar();
            sb.append(c);
        }
        return sb.toString();
    }

    private File getStorageFolder(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }
}
