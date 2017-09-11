/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
