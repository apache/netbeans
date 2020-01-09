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
package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.Assert.assertFalse;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor.DefaultProgressMonitor;
import org.netbeans.libs.git.progress.StatusListener;

/**
 *
 * @author ondra
 */
public class StatusTest extends AbstractGitTestCase {

    private File workDir;
    private Repository repository;
    private static final StatusListener NULL_STATUS_LISTENER = new StatusListener() {
        @Override
        public void notifyStatus(GitStatus status) {
        }
    };

    public StatusTest(String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testMiscStatus () throws Exception {
        write(new File(workDir, ".gitignore"), "ignored");
        File untracked = new File(workDir, "untracked");
        write(untracked, "untracked");
        File ignored = new File(workDir, "ignored");
        write(ignored, "ignored");
        File added_uptodate = new File(workDir, "added-uptodate");
        write(added_uptodate, "added-uptodate");
        File added_modified = new File(workDir, "added-modified");
        write(added_modified, "added_modified");
        File added_deleted = new File(workDir, "added-deleted");
        write(added_deleted, "added_deleted");

        File uptodate_uptodate = new File(workDir, "uptodate-uptodate");
        write(uptodate_uptodate, "uptodate_uptodate");
        File uptodate_modified = new File(workDir, "uptodate-modified");
        write(uptodate_modified, "uptodate_modified");
        File uptodate_deleted = new File(workDir, "uptodate-deleted");
        write(uptodate_deleted, "uptodate_deleted");

        File modified_uptodate = new File(workDir, "modified-uptodate");
        write(modified_uptodate, "modified_uptodate");
        File modified_modified = new File(workDir, "modified-modified");
        write(modified_modified, "modified_modified");
        File modified_reset = new File(workDir, "modified-reset");
        write(modified_reset, "modified_reset");
        File modified_deleted = new File(workDir, "modified-deleted");
        write(modified_deleted, "modified_deleted");

        // we cannot
        File deleted_uptodate = new File(workDir, "deleted-uptodate");
        write(deleted_uptodate, "deleted_uptodate");
        File deleted_untracked = new File(workDir, "deleted-untracked");
        write(deleted_untracked, "deleted_untracked");
        File deleted_modified = new File(workDir, "deleted-modified");
        write(deleted_modified, "deleted_modified");

        add(uptodate_uptodate, uptodate_modified, uptodate_deleted, modified_uptodate, modified_modified, modified_reset, modified_deleted, deleted_uptodate, deleted_untracked, deleted_modified);
        commit(workDir);
        add(added_uptodate, added_modified, added_deleted);
        write(modified_deleted, "modification modified_deleted");
        write(modified_modified, "modification modified_modified");
        write(modified_reset, "modification modified_reset");
        write(modified_uptodate, "modification modified_uptodate");
        add(modified_deleted, modified_modified, modified_reset, modified_uptodate);
        deleted_uptodate.delete();
        deleted_untracked.delete();
        deleted_modified.delete();
        remove(true, deleted_uptodate, deleted_untracked, deleted_modified);
        write(added_modified, "modification2 added_modified");
        write(uptodate_modified, "modification2 uptodate_modified");
        write(modified_modified, "modification2 modified_modified");
        write(modified_reset, "modified_reset");
        added_deleted.delete();
        modified_deleted.delete();
        uptodate_deleted.delete();
        write(deleted_untracked, "deleted_untracked");
        write(deleted_modified, "deleted_modified\nchange");

        TestStatusListener listener = new TestStatusListener();
        GitClient client = getClient(workDir);
        client.addNotificationListener(listener);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertFalse(statuses.isEmpty());
        assertStatus(statuses, workDir, untracked, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false, listener);
        assertStatus(statuses, workDir, added_uptodate, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false, listener);
        assertStatus(statuses, workDir, added_modified, true, Status.STATUS_ADDED, Status.STATUS_MODIFIED, Status.STATUS_ADDED, false, listener);
        assertStatus(statuses, workDir, added_deleted, true, Status.STATUS_ADDED, Status.STATUS_REMOVED, Status.STATUS_NORMAL, false, listener);
        assertStatus(statuses, workDir, uptodate_uptodate, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, listener);
        assertStatus(statuses, workDir, uptodate_modified, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, listener);
        assertStatus(statuses, workDir, uptodate_deleted, true, Status.STATUS_NORMAL, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false, listener);
        assertStatus(statuses, workDir, modified_uptodate, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false, listener);
        assertStatus(statuses, workDir, modified_modified, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, listener);
        assertStatus(statuses, workDir, modified_reset, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, false, listener);
        assertStatus(statuses, workDir, modified_deleted, true, Status.STATUS_MODIFIED, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false, listener);
        assertStatus(statuses, workDir, deleted_uptodate, true, Status.STATUS_REMOVED, Status.STATUS_NORMAL, Status.STATUS_REMOVED, false, listener);
        assertStatus(statuses, workDir, deleted_untracked, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_NORMAL, false, listener);
        assertStatus(statuses, workDir, deleted_modified, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_MODIFIED, false, listener);
        // what about isIgnored() here?
        assertStatus(statuses, workDir, ignored, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false, listener);
    }
    
    // diff WT against a commit other than HEAD
    public void testMiscStatusCommit () throws Exception {
        write(new File(workDir, ".gitignore"), "ignored");
        File untracked = new File(workDir, "untracked");
        write(untracked, "untracked");
        File ignored = new File(workDir, "ignored");
        write(ignored, "ignored");
        File added_uptodate = new File(workDir, "added-uptodate");
        write(added_uptodate, "added-uptodate");
        File added_modified = new File(workDir, "added-modified");
        write(added_modified, "added_modified");
        File added_deleted = new File(workDir, "added-deleted");
        write(added_deleted, "added_deleted");

        File uptodate_uptodate = new File(workDir, "uptodate-uptodate");
        write(uptodate_uptodate, "uptodate_uptodate");
        File uptodate_modified = new File(workDir, "uptodate-modified");
        write(uptodate_modified, "uptodate_modified");
        File uptodate_deleted = new File(workDir, "uptodate-deleted");
        write(uptodate_deleted, "uptodate_deleted");

        File modified_uptodate = new File(workDir, "modified-uptodate");
        write(modified_uptodate, "modified_uptodate");
        File modified_modified = new File(workDir, "modified-modified");
        write(modified_modified, "modified_modified");
        File modified_reset = new File(workDir, "modified-reset");
        write(modified_reset, "modified_reset");
        File modified_deleted = new File(workDir, "modified-deleted");
        write(modified_deleted, "modified_deleted");

        // we cannot
        File deleted_uptodate = new File(workDir, "deleted-uptodate");
        write(deleted_uptodate, "deleted_uptodate");
        File deleted_untracked = new File(workDir, "deleted-untracked");
        write(deleted_untracked, "deleted_untracked");
        File deleted_modified = new File(workDir, "deleted-modified");
        write(deleted_modified, "deleted_modified");

        add(uptodate_uptodate, uptodate_modified, uptodate_deleted, modified_uptodate, modified_modified, modified_reset, modified_deleted, deleted_uptodate, deleted_untracked, deleted_modified);
        commit(workDir);
        add(added_uptodate, added_modified, added_deleted);
        write(modified_deleted, "modification modified_deleted");
        write(modified_modified, "modification modified_modified");
        write(modified_reset, "modification modified_reset");
        write(modified_uptodate, "modification modified_uptodate");
        add(modified_deleted, modified_modified, modified_reset, modified_uptodate);
        deleted_uptodate.delete();
        deleted_untracked.delete();
        deleted_modified.delete();
        remove(true, deleted_uptodate, deleted_untracked, deleted_modified);
        write(added_modified, "modification2 added_modified");
        write(uptodate_modified, "modification2 uptodate_modified");
        write(modified_modified, "modification2 modified_modified");
        write(modified_reset, "modified_reset");
        added_deleted.delete();
        modified_deleted.delete();
        uptodate_deleted.delete();
        write(deleted_untracked, "deleted_untracked");
        write(deleted_modified, "deleted_modified\nchange");
        
        GitClient client = getClient(workDir);
        String revId = client.getBranches(false, NULL_PROGRESS_MONITOR).get(Constants.MASTER).getId();
        
        File someFile = new File(workDir, "fileforothercommit");
        write(someFile, "fileforothercommit");
        add(someFile);
        commit(someFile);

        TestStatusListener listener = new TestStatusListener();
        client.addNotificationListener(listener);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, Constants.HEAD, NULL_PROGRESS_MONITOR);
        assertFalse(statuses.isEmpty());
        assertStatus(statuses, workDir, untracked, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false, listener);
        assertStatus(statuses, workDir, added_uptodate, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false, listener);
        assertStatus(statuses, workDir, added_modified, true, Status.STATUS_ADDED, Status.STATUS_MODIFIED, Status.STATUS_ADDED, false, listener);
        assertStatus(statuses, workDir, added_deleted, true, Status.STATUS_ADDED, Status.STATUS_REMOVED, Status.STATUS_NORMAL, false, listener);
        assertStatus(statuses, workDir, uptodate_uptodate, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, listener);
        assertStatus(statuses, workDir, uptodate_modified, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, listener);
        assertStatus(statuses, workDir, uptodate_deleted, true, Status.STATUS_NORMAL, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false, listener);
        assertStatus(statuses, workDir, modified_uptodate, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false, listener);
        assertStatus(statuses, workDir, modified_modified, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, listener);
        assertStatus(statuses, workDir, modified_reset, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, false, listener);
        assertStatus(statuses, workDir, modified_deleted, true, Status.STATUS_MODIFIED, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false, listener);
        assertStatus(statuses, workDir, deleted_uptodate, true, Status.STATUS_REMOVED, Status.STATUS_NORMAL, Status.STATUS_REMOVED, false, listener);
        assertStatus(statuses, workDir, deleted_untracked, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_NORMAL, false, listener);
        assertStatus(statuses, workDir, deleted_modified, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_MODIFIED, false, listener);
        assertStatus(statuses, workDir, someFile, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, listener);
        // what about isIgnored() here?
        assertStatus(statuses, workDir, ignored, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false, listener);
        
        listener = new TestStatusListener();
        client.addNotificationListener(listener);
        statuses = client.getStatus(new File[] { workDir }, revId, NULL_PROGRESS_MONITOR);
        assertFalse(statuses.isEmpty());
        assertStatus(statuses, workDir, untracked, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false, listener);
        assertStatus(statuses, workDir, added_uptodate, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false, listener);
        assertStatus(statuses, workDir, added_modified, true, Status.STATUS_ADDED, Status.STATUS_MODIFIED, Status.STATUS_ADDED, false, listener);
        assertStatus(statuses, workDir, added_deleted, true, Status.STATUS_ADDED, Status.STATUS_REMOVED, Status.STATUS_NORMAL, false, listener);
        assertStatus(statuses, workDir, uptodate_uptodate, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, listener);
        assertStatus(statuses, workDir, uptodate_modified, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, listener);
        assertStatus(statuses, workDir, uptodate_deleted, true, Status.STATUS_NORMAL, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false, listener);
        assertStatus(statuses, workDir, modified_uptodate, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false, listener);
        assertStatus(statuses, workDir, modified_modified, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, listener);
        assertStatus(statuses, workDir, modified_reset, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, false, listener);
        assertStatus(statuses, workDir, modified_deleted, true, Status.STATUS_MODIFIED, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false, listener);
        assertStatus(statuses, workDir, deleted_uptodate, true, Status.STATUS_REMOVED, Status.STATUS_NORMAL, Status.STATUS_REMOVED, false, listener);
        assertStatus(statuses, workDir, deleted_untracked, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_NORMAL, false, listener);
        assertStatus(statuses, workDir, deleted_modified, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_MODIFIED, false, listener);
        // what about isIgnored() here?
        assertStatus(statuses, workDir, ignored, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false, listener);
        // file somefile was not known in that revision
        assertStatus(statuses, workDir, someFile, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false, listener);
    }

    public void testStatusSingleFile () throws Exception {
        File untracked = new File(workDir, "untracked");
        write(untracked, "untracked");
        File added_modified = new File(workDir, "added-modified");
        write(added_modified, "added_modified");
        File uptodate_modified = new File(workDir, "uptodate-modified");
        write(uptodate_modified, "uptodate_modified");
        File modified_modified = new File(workDir, "modified-modified");
        write(modified_modified, "modified_modified");

        add(uptodate_modified, modified_modified);
        commit(uptodate_modified, modified_modified);
        add(added_modified);
        write(modified_modified, "modification modified_modified");
        add(modified_modified);
        write(added_modified, "modification2 added_modified");
        write(uptodate_modified, "modification2 uptodate_modified");
        write(modified_modified, "modification2 modified_modified");

        GitClient client = getClient(workDir);
        TestStatusListener monitor = new TestStatusListener();
        client.addNotificationListener(monitor);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { untracked }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, untracked, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false, monitor);
        monitor = new TestStatusListener();
        client.addNotificationListener(monitor);
        statuses = client.getStatus(new File[] { added_modified }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, added_modified, true, Status.STATUS_ADDED, Status.STATUS_MODIFIED, Status.STATUS_ADDED, false, monitor);
        monitor = new TestStatusListener();
        client.addNotificationListener(monitor);
        statuses = client.getStatus(new File[] { uptodate_modified }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, uptodate_modified, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, monitor);
        monitor = new TestStatusListener();
        client.addNotificationListener(monitor);
        statuses = client.getStatus(new File[] { modified_modified }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, modified_modified, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, monitor);
    }

    public void testStatusTree () throws Exception {
        File folder = new File(workDir, "folder1");
        folder.mkdirs();
        write(new File(folder, "untracked1"), "untracked");
        write(new File(folder, "untracked2"), "untracked");
        folder = new File(workDir, "folder2");
        folder.mkdirs();
        File f1 = new File(folder, "f1");
        write(f1, "f1");
        File f2 = new File(folder, "f2");
        write(f2, "f2");
        File folder21 = new File(folder, "folder21");
        folder21.mkdirs();
        File f3 = new File(folder21, "f3");
        write(f3, "f3");
        File f4 = new File(folder21, "f4");
        write(f4, "f4");
        File folder22 = new File(folder, "folder22");
        folder22.mkdirs();
        File f5 = new File(folder22, "f5");
        write(f5, "f5");
        File f6 = new File(folder22, "f6");
        write(f6, "f6");

        add(f1, f2, f3, f4, f5, f6);
        commit(f1, f2, f3, f4, f5, f6);

        GitClient client = getClient(workDir);
        TestStatusListener monitor = new TestStatusListener();
        client.addNotificationListener(monitor);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { folder }, NULL_PROGRESS_MONITOR);
        assertEquals(6, statuses.size());
        assertStatus(statuses, workDir, f1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f3, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f4, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f5, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f6, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
    }
    
    public void testStatusDifferentTree () throws Exception {
        File folder = new File(workDir.getParent(), "folder1");
        folder.mkdirs();
        try {
            StatusListener monitor = new TestStatusListener();
            getClient(workDir).getStatus(new File[] { folder }, NULL_PROGRESS_MONITOR);
            fail("Different tree");
        } catch (IllegalArgumentException ex) {
            // OK
        }
    }

    public void testSkipIgnoredFolders () throws Exception {
        File file1 = new File(workDir, "file1");
        File folder = new File(workDir, "folder");
        File file2 = new File(folder, "file2");
        folder.mkdirs();
        file1.createNewFile();
        file2.createNewFile();
        File subFolder = new File(folder, "subfolder");
        File file3 = new File(folder, "file3");
        File file4 = new File(subFolder, "file4");
        subFolder.mkdirs();
        file3.createNewFile();
        file4.createNewFile();
        File folder2 = new File(workDir, "folder2");
        folder2.mkdirs();
        File file5 = new File(folder2, "file5");
        file5.createNewFile();
        File subFolder2 = new File(folder2, "subfolder");
        File file6 = new File(subFolder2, "file6");
        subFolder2.mkdirs();
        file6.createNewFile();

        write(new File(workDir, ".gitignore"), "folder\nfile1");
        write(new File(folder2, ".gitignore"), "subfolder");

        Map<File, GitStatus> statuses = getClient(workDir).getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, folder, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
        assertTrue(statuses.get(folder).isFolder());
        assertNull(statuses.get(file2));
        assertNull(statuses.get(file3));
        assertNull(statuses.get(file4));
        assertStatus(statuses, workDir, file5, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, subFolder2, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
        assertTrue(statuses.get(subFolder2).isFolder());
        assertNull(statuses.get(file6));

        statuses = getClient(workDir).getStatus(new File[] { folder }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, folder, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
        assertTrue(statuses.get(folder).isFolder());
        assertNull(statuses.get(file2));
        assertNull(statuses.get(file3));
        assertNull(statuses.get(file4));
        
        statuses = getClient(workDir).getStatus(new File[] { file2 }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file2, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
        assertNull(statuses.get(folder));
        assertNull(statuses.get(file3));
        assertNull(statuses.get(file4));

        statuses = getClient(workDir).getStatus(new File[] { folder, file2 }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file2, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, folder, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
        assertTrue(statuses.get(folder).isFolder());
        assertNull(statuses.get(file3));
        assertNull(statuses.get(file4));

        statuses = getClient(workDir).getStatus(new File[] { folder, file2, file3 }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file2, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file3, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, folder, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
        assertTrue(statuses.get(folder).isFolder());
        assertNull(statuses.get(file4));

        statuses = getClient(workDir).getStatus(new File[] { folder, file2, file3, file4 }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file2, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file3, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file4, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, folder, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
        assertTrue(statuses.get(folder).isFolder());
    }

    public void testIgnoredFilesAreNotTracked () throws Exception {
        File file = new File(workDir, "ignoredFile");
        file.createNewFile();
        File folder = new File(workDir, "ignoredFolder");
        folder.mkdirs();
        File folder2 = new File(workDir, "ignoredFolder2");
        folder2.mkdirs();
        File file2 = new File(folder2, "addedFile");
        file2.createNewFile();
        File ignoreFile = new File(workDir, ".gitignore");
        write(ignoreFile, "ignored*");

        add(file2);

        Map<File, GitStatus> statuses = getClient(workDir).getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertFalse(statuses.get(file).isTracked());
        assertFalse(statuses.get(folder).isTracked());
        assertFalse(statuses.get(folder2).isTracked());
    }

    public void testCancel () throws Exception {
        final File file = new File(workDir, "file");
        file.createNewFile();
        final File file2 = new File(workDir, "file2");
        file2.createNewFile();

        class Monitor extends DefaultProgressMonitor implements StatusListener {
            private boolean barrierAccessed;
            private int count;
            private boolean cont;
            @Override
            public void notifyStatus(GitStatus status) {
                barrierAccessed = true;
                ++count;
                while (!cont) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }
                }
            }
            private void waitAtBarrier() throws InterruptedException {
                for (int i = 0; i < 100; ++i) {
                    if (barrierAccessed) {
                        break;
                    }
                    Thread.sleep(100);
                }
                assertTrue(barrierAccessed);
            }
        }
        final Monitor m = new Monitor();
        final GitClient client = getClient(workDir);
        final Exception[] exs = new Exception[1];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.getStatus(new File[] { file, file2 }, m);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        m.cancel();
        m.cont = true;
        t1.join();
        assertTrue(m.isCanceled());
        assertEquals(1, m.count);
        assertEquals(null, exs[0]);
    }

    public void testConflictScan () throws Exception {
        GitClient client = getClient(workDir);
        File f = new File(workDir, "f");
        f.createNewFile();
        File f2 = new File(workDir, "f2");
        f2.createNewFile();
        File[] roots = new File[] { f, f2 };
        add(roots);
        commit(roots);
        Map<File, GitStatus> conflicts = client.getConflicts(roots, NULL_PROGRESS_MONITOR);
        assertEquals(0, conflicts.size());

        DirCache cache = repository.lockDirCache();
        try {
            DirCacheEntry e = cache.getEntry("f");
            DirCacheBuilder builder = cache.builder();
            DirCacheEntry toAdd = new DirCacheEntry("f", 1);
            toAdd.setFileMode(e.getFileMode());
            toAdd.setObjectId(e.getObjectId());
            builder.add(toAdd);
            toAdd = new DirCacheEntry("f", 2);
            toAdd.setFileMode(e.getFileMode());
            toAdd.setObjectId(e.getObjectId());
            builder.add(toAdd);

            e = cache.getEntry("f2");
            toAdd = new DirCacheEntry("f2", 1);
            toAdd.setFileMode(e.getFileMode());
            toAdd.setObjectId(e.getObjectId());
            builder.add(toAdd);
            toAdd = new DirCacheEntry("f2", 2);
            toAdd.setFileMode(e.getFileMode());
            toAdd.setObjectId(e.getObjectId());
            builder.add(toAdd);
            builder.finish();
            builder.commit();
        } finally {
            cache.unlock();
        }
        conflicts = client.getConflicts(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals(1, conflicts.size());
        conflicts = client.getConflicts(new File[] { f2 }, NULL_PROGRESS_MONITOR);
        assertEquals(1, conflicts.size());
        conflicts = client.getConflicts(roots, NULL_PROGRESS_MONITOR);
        assertEquals(2, conflicts.size());
    }
    
    public void testIgnoredInExlude () throws Exception {
        File f = new File(workDir, "f");
        write(f, "hi, i am ignored");
        File exclude = new File(workDir, ".git/info/exclude");
        exclude.getParentFile().mkdirs();
        write(exclude, "f");
        GitStatus st = getClient(workDir).getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals(Status.STATUS_IGNORED, st.getStatusIndexWC());
    }
    
    public void testModifiedUnderIgnored () throws Exception {
        File f = new File(workDir, "folder/f");
        f.getParentFile().mkdirs();
        write(f, "hi, i am new");
        add(f);
        commit(f);
        GitClient client = getClient(workDir);
        Map<File, GitStatus> st = client.getStatus(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertStatus(st, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        
        client.ignore(new File[] { f.getParentFile() }, NULL_PROGRESS_MONITOR);
        st = client.getStatus(new File[] { }, NULL_PROGRESS_MONITOR);
        assertStatus(st, workDir, f.getParentFile(), false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
        // file f is not ignored, it's committed
        assertStatus(st, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        
        write(f, "hi, i am modified");
        st = client.getStatus(new File[] { }, NULL_PROGRESS_MONITOR);
        assertStatus(st, workDir, f.getParentFile(), false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
        // file f is not ignored, it's committed and modified
        assertStatus(st, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        
    }
    
    public void testIgnoreExecutable () throws Exception {
        if (isWindows()) {
            // no reason to test on win
            return;
        }
        File f = new File(workDir, "f");
        write(f, "hi, i am executable");
        f.setExecutable(true);
        File[] roots = { f };
        add(roots);
        commit(roots);
        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        
        f.setExecutable(false);
        statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        
        StoredConfig config = repository.getConfig();
        config.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_FILEMODE, false);
        config.save();
        statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        
        config.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_FILEMODE, true);
        config.save();
        add(roots);
        statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        
        config.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_FILEMODE, false);
        config.save();
        add(roots);
        statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }

    // must not return status for nested repositories
    public void testStatusNested () throws Exception {
        File f = new File(workDir, "f");
        write(f, "file");
        
        GitClient client = getClient(workDir);
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        File nested = new File(workDir, "nested");
        nested.mkdirs();
        new File(workDir, "emptyFolder").mkdirs();
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size()); // commandline is silent about empty folders
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        GitClient clientNested = getClient(nested);
        clientNested.init(NULL_PROGRESS_MONITOR);
        File f2 = new File(nested, "f");
        write(f2, "file");
        clientNested.add(new File[] { f2 }, NULL_PROGRESS_MONITOR);
        clientNested.commit(new File[] { f2 }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size()); // on the other hand, nested repository parent should be listed as is on commandline
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, nested, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
        
        statuses = clientNested.getStatus(new File[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }
    
    public void testSymlinkedFolder () throws Exception {
        File folder1 = new File(workDir, "boo");
        File file1 = new File(workDir, "old_file");
        File folder2 = new File(workDir, "some_dir");
        File file2_1 = new File(folder2, "some_file");
        
        folder1.mkdirs();
        folder2.mkdirs();
        file1.createNewFile();
        file2_1.createNewFile();
        add(workDir);
        commit(workDir);
        
        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(new File[0], NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2_1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
     
        // create a symlink, not added to index
        String relPath = "../some_dir";
        File link = new File(folder1, folder2.getName());
        Files.createSymbolicLink(Paths.get(link.getAbsolutePath()), Paths.get(relPath));
        assertTrue(Files.isSymbolicLink(Paths.get(link.getAbsolutePath())));
        statuses = client.getStatus(new File[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
        
        // symlink is added to index, not yet committed
        client.add(new File[] { link }, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false);
        
        // symlink is committed
        client.commit(new File[] { link }, "commit symlink", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        
        // symlink is deleted on disk
        Files.delete(Paths.get(link.getAbsolutePath()));
        assertFalse(Files.isSymbolicLink(Paths.get(link.getAbsolutePath())));
        statuses = client.getStatus(new File[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, true, Status.STATUS_NORMAL, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false);
        
        // symlink is also deleted from index
        client.remove(new File[] { link }, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, true, Status.STATUS_REMOVED, Status.STATUS_NORMAL, Status.STATUS_REMOVED, false);
    }
    
    public void testIgnoredSymlinkFile () throws Exception {
        File folder1 = new File(workDir, "boo");
        File file1 = new File(workDir, "old_file");
        File folder2 = new File(workDir, "some_dir");
        File file2_1 = new File(folder2, "some_file");
        
        folder1.mkdirs();
        folder2.mkdirs();
        file1.createNewFile();
        file2_1.createNewFile();
        add(workDir);
        commit(workDir);
        
        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(new File[0], NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2_1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
     
        // create a symlink, not added to index
        String relPath = "../some_dir/some_file";
        File link = new File(folder1, file2_1.getName());
        Files.createSymbolicLink(Paths.get(link.getAbsolutePath()), Paths.get(relPath));
        assertTrue(Files.isSymbolicLink(Paths.get(link.getAbsolutePath())));
        statuses = client.getStatus(new File[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
        
        client.ignore(new File[] { link }, NULL_PROGRESS_MONITOR);
        assertEquals("/boo/some_file", read(new File(workDir, ".gitignore")));
        statuses = client.getStatus(new File[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
    }
    
    public void testIgnoredSymlinkFolder () throws Exception {
        File folder1 = new File(workDir, "boo");
        File file1 = new File(workDir, "old_file");
        File folder2 = new File(workDir, "some_dir");
        File file2_1 = new File(folder2, "some_file");
        
        folder1.mkdirs();
        folder2.mkdirs();
        file1.createNewFile();
        file2_1.createNewFile();
        add(workDir);
        commit(workDir);
        
        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(new File[0], NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2_1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
     
        // create a symlink, not added to index
        String relPath = "../some_dir";
        File link = new File(folder1, folder2.getName());
        Files.createSymbolicLink(Paths.get(link.getAbsolutePath()), Paths.get(relPath));
        assertTrue(Files.isSymbolicLink(Paths.get(link.getAbsolutePath())));
        statuses = client.getStatus(new File[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
        
        client.ignore(new File[] { link }, NULL_PROGRESS_MONITOR);
        assertEquals("/boo/some_dir", read(new File(workDir, ".gitignore")));
        statuses = client.getStatus(new File[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
    }
    
    public void testLastIndexModificationDate () throws Exception {
        File f = new File(workDir, "f");
        
        GitClient client = getClient(workDir);
        write(f, "init");
        
        // not yet added to the index => ts: -1
        GitStatus status = client.getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals(-1, status.getIndexEntryModificationDate());
        
        add(f);
        // added => current timestamp
        status = client.getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f);
        long ts = f.lastModified();
        assertEquals((ts / 1000) * 1000, (status.getIndexEntryModificationDate() / 1000) * 1000);
        
        commit(f);
        // still the same => current timestamp
        status = client.getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals((ts / 1000) * 1000, (status.getIndexEntryModificationDate() / 1000) * 1000);
        
        Thread.sleep(1000);
        write(f, "modification");
        // modified => both should differ
        status = client.getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals((ts / 1000) * 1000, (status.getIndexEntryModificationDate() / 1000) * 1000);
        ts = f.lastModified();
        assertNotSame((ts / 1000) * 1000, (status.getIndexEntryModificationDate() / 1000) * 1000);
        
        add(f);
        // updated -> both are the same
        status = client.getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals((ts / 1000) * 1000, (status.getIndexEntryModificationDate() / 1000) * 1000);
        
        client.remove(new File[] { f }, true, NULL_PROGRESS_MONITOR);
        // removed => ts: -1
        status = client.getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals(-1, status.getIndexEntryModificationDate());
    }
    
    private void assertStatus(Map<File, GitStatus> statuses, File repository, File file, boolean tracked, Status headVsIndex, Status indexVsWorking, Status headVsWorking, boolean conflict, TestStatusListener monitor) {
        assertStatus(statuses, repository, file, tracked, headVsIndex, indexVsWorking, headVsWorking, conflict);
        assertStatus(monitor.notifiedStatuses, repository, file, tracked, headVsIndex, indexVsWorking, headVsWorking, conflict);
    }

    private static class TestStatusListener implements StatusListener {
        private final Map<File, GitStatus> notifiedStatuses;

        public TestStatusListener() {
            notifiedStatuses = new HashMap<File, GitStatus>();
        }

        @Override
        public void notifyStatus(GitStatus status) {
            notifiedStatuses.put(status.getFile(), status);
        }
    }
}
