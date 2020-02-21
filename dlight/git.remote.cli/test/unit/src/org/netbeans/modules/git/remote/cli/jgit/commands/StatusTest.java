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
package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.GitStatus.Status;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitConfig;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor.DefaultProgressMonitor;
import org.netbeans.modules.git.remote.cli.progress.StatusListener;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class StatusTest extends AbstractGitTestCase {
    private VCSFileProxy workDir;
    private JGitRepository repository;
    protected static final boolean KIT = StatusCommand.KIT;

    public StatusTest(String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testIgnoredFilesAreNotTracked","testSkipIgnoredFolders","testConflictScan","testIgnoredSymlinkFolder").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testMiscStatus () throws Exception {
        write(VCSFileProxy.createFileProxy(workDir, GitConstants.GITIGNORE_FILENAME), "ignored");
        VCSFileProxy untracked = VCSFileProxy.createFileProxy(workDir, "untracked");
        write(untracked, "untracked");
        VCSFileProxy ignored = VCSFileProxy.createFileProxy(workDir, "ignored");
        write(ignored, "ignored");
        VCSFileProxy added_uptodate = VCSFileProxy.createFileProxy(workDir, "added-uptodate");
        write(added_uptodate, "added-uptodate");
        VCSFileProxy added_modified = VCSFileProxy.createFileProxy(workDir, "added-modified");
        write(added_modified, "added_modified");
        VCSFileProxy added_deleted = VCSFileProxy.createFileProxy(workDir, "added-deleted");
        write(added_deleted, "added_deleted");

        VCSFileProxy uptodate_uptodate = VCSFileProxy.createFileProxy(workDir, "uptodate-uptodate");
        write(uptodate_uptodate, "uptodate_uptodate");
        VCSFileProxy uptodate_modified = VCSFileProxy.createFileProxy(workDir, "uptodate-modified");
        write(uptodate_modified, "uptodate_modified");
        VCSFileProxy uptodate_deleted = VCSFileProxy.createFileProxy(workDir, "uptodate-deleted");
        write(uptodate_deleted, "uptodate_deleted");

        VCSFileProxy modified_uptodate = VCSFileProxy.createFileProxy(workDir, "modified-uptodate");
        write(modified_uptodate, "modified_uptodate");
        VCSFileProxy modified_modified = VCSFileProxy.createFileProxy(workDir, "modified-modified");
        write(modified_modified, "modified_modified");
        VCSFileProxy modified_reset = VCSFileProxy.createFileProxy(workDir, "modified-reset");
        write(modified_reset, "modified_reset");
        VCSFileProxy modified_deleted = VCSFileProxy.createFileProxy(workDir, "modified-deleted");
        write(modified_deleted, "modified_deleted");

        // we cannot
        VCSFileProxy deleted_uptodate = VCSFileProxy.createFileProxy(workDir, "deleted-uptodate");
        write(deleted_uptodate, "deleted_uptodate");
        VCSFileProxy deleted_untracked = VCSFileProxy.createFileProxy(workDir, "deleted-untracked");
        write(deleted_untracked, "deleted_untracked");
        VCSFileProxy deleted_modified = VCSFileProxy.createFileProxy(workDir, "deleted-modified");
        write(deleted_modified, "deleted_modified");

        add(uptodate_uptodate, uptodate_modified, uptodate_deleted, modified_uptodate, 
                modified_modified, modified_reset, modified_deleted, deleted_uptodate, deleted_untracked, deleted_modified);
        commit(workDir);
        add(added_uptodate, added_modified, added_deleted);
        write(modified_deleted, "modification modified_deleted");
        write(modified_modified, "modification modified_modified");
        write(modified_reset, "modification modified_reset");
        write(modified_uptodate, "modification modified_uptodate");
        add(modified_deleted, modified_modified, modified_reset, modified_uptodate);
        VCSFileProxySupport.delete(deleted_uptodate);
        VCSFileProxySupport.delete(deleted_untracked);
        VCSFileProxySupport.delete(deleted_modified);
        remove(true, deleted_uptodate, deleted_untracked, deleted_modified);
        write(added_modified, "modification2 added_modified");
        write(uptodate_modified, "modification2 uptodate_modified");
        write(modified_modified, "modification2 modified_modified");
        write(modified_reset, "modified_reset");
        VCSFileProxySupport.delete(added_deleted);
        VCSFileProxySupport.delete(modified_deleted);
        VCSFileProxySupport.delete(uptodate_deleted);
        write(deleted_untracked, "deleted_untracked");
        write(deleted_modified, "deleted_modified\nchange");

        TestStatusListener listener = new TestStatusListener();
        GitClient client = getClient(workDir);
        client.addNotificationListener(listener);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertFalse(statuses.isEmpty());

        //                                                       Head.vs.Index          Index.vs.WC            Head.vs.WC
        //?? untracked
        assertStatus(statuses, workDir, untracked,         false,Status.STATUS_NORMAL,  Status.STATUS_ADDED,   Status.STATUS_ADDED,   false, listener);               
        //A  added-uptodate
        assertStatus(statuses, workDir, added_uptodate,    true, Status.STATUS_ADDED,   Status.STATUS_NORMAL,  Status.STATUS_ADDED,   false, listener);           
        //AM added-modified
        assertStatus(statuses, workDir, added_modified,    true, Status.STATUS_ADDED,   Status.STATUS_MODIFIED,Status.STATUS_ADDED,   false, listener);         
        //AD added-deleted
        assertStatus(statuses, workDir, added_deleted,     true, Status.STATUS_ADDED,   Status.STATUS_REMOVED, Status.STATUS_NORMAL,  false, listener);
        //
        assertStatus(statuses, workDir, uptodate_uptodate, true, Status.STATUS_NORMAL,  Status.STATUS_NORMAL,  Status.STATUS_NORMAL,  false, listener);
        // M uptodate-modified
        assertStatus(statuses, workDir, uptodate_modified, true, Status.STATUS_NORMAL,  Status.STATUS_MODIFIED,Status.STATUS_MODIFIED,false, listener);
        // D uptodate-deleted
        assertStatus(statuses, workDir, uptodate_deleted,  true, Status.STATUS_NORMAL,  Status.STATUS_REMOVED, Status.STATUS_REMOVED, false, listener);
        //M  modified-uptodate
        assertStatus(statuses, workDir, modified_uptodate, true, Status.STATUS_MODIFIED,Status.STATUS_NORMAL,  Status.STATUS_MODIFIED,false, listener);
        //MM modified-modified
        assertStatus(statuses, workDir, modified_modified, true, Status.STATUS_MODIFIED,Status.STATUS_MODIFIED,Status.STATUS_MODIFIED,false, listener);
        //MM modified-reset
        assertStatus(statuses, workDir, modified_reset,    true, Status.STATUS_MODIFIED,Status.STATUS_MODIFIED,Status.STATUS_NORMAL,  false, listener);
        //MD modified-deleted
        assertStatus(statuses, workDir, modified_deleted,  true, Status.STATUS_MODIFIED,Status.STATUS_REMOVED, Status.STATUS_REMOVED, false, listener);
        //D  deleted-uptodate
        assertStatus(statuses, workDir, deleted_uptodate,  true, Status.STATUS_REMOVED, Status.STATUS_NORMAL,  Status.STATUS_REMOVED, false, listener);
        //D  deleted-untracked
        //?? deleted-untracked
if(KIT) assertStatus(statuses, workDir, deleted_untracked, true, Status.STATUS_REMOVED, Status.STATUS_ADDED,   Status.STATUS_NORMAL,  false, listener);
else    assertStatus(statuses, workDir, deleted_untracked, true, Status.STATUS_REMOVED, Status.STATUS_ADDED,   Status.STATUS_MODIFIED,  false, listener);
        //D  deleted-modified
        //?? deleted-modified
        assertStatus(statuses, workDir, deleted_modified,  true, Status.STATUS_REMOVED, Status.STATUS_ADDED,   Status.STATUS_MODIFIED,false, listener);
        // what about isIgnored() here?
        //!! .gitignore
        assertStatus(statuses, workDir, ignored,           false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED,   false, listener);
    }
    
    // diff WT against a commit other than HEAD
    public void testMiscStatusCommit () throws Exception {
        write(VCSFileProxy.createFileProxy(workDir, GitConstants.GITIGNORE_FILENAME), "ignored");
        VCSFileProxy untracked = VCSFileProxy.createFileProxy(workDir, "untracked");
        write(untracked, "untracked");
        VCSFileProxy ignored = VCSFileProxy.createFileProxy(workDir, "ignored");
        write(ignored, "ignored");
        VCSFileProxy added_uptodate = VCSFileProxy.createFileProxy(workDir, "added-uptodate");
        write(added_uptodate, "added-uptodate");
        VCSFileProxy added_modified = VCSFileProxy.createFileProxy(workDir, "added-modified");
        write(added_modified, "added_modified");
        VCSFileProxy added_deleted = VCSFileProxy.createFileProxy(workDir, "added-deleted");
        write(added_deleted, "added_deleted");

        VCSFileProxy uptodate_uptodate = VCSFileProxy.createFileProxy(workDir, "uptodate-uptodate");
        write(uptodate_uptodate, "uptodate_uptodate");
        VCSFileProxy uptodate_modified = VCSFileProxy.createFileProxy(workDir, "uptodate-modified");
        write(uptodate_modified, "uptodate_modified");
        VCSFileProxy uptodate_deleted = VCSFileProxy.createFileProxy(workDir, "uptodate-deleted");
        write(uptodate_deleted, "uptodate_deleted");

        VCSFileProxy modified_uptodate = VCSFileProxy.createFileProxy(workDir, "modified-uptodate");
        write(modified_uptodate, "modified_uptodate");
        VCSFileProxy modified_modified = VCSFileProxy.createFileProxy(workDir, "modified-modified");
        write(modified_modified, "modified_modified");
        VCSFileProxy modified_reset = VCSFileProxy.createFileProxy(workDir, "modified-reset");
        write(modified_reset, "modified_reset");
        VCSFileProxy modified_deleted = VCSFileProxy.createFileProxy(workDir, "modified-deleted");
        write(modified_deleted, "modified_deleted");

        // we cannot
        VCSFileProxy deleted_uptodate = VCSFileProxy.createFileProxy(workDir, "deleted-uptodate");
        write(deleted_uptodate, "deleted_uptodate");
        VCSFileProxy deleted_untracked = VCSFileProxy.createFileProxy(workDir, "deleted-untracked");
        write(deleted_untracked, "deleted_untracked");
        VCSFileProxy deleted_modified = VCSFileProxy.createFileProxy(workDir, "deleted-modified");
        write(deleted_modified, "deleted_modified");

        add(uptodate_uptodate, uptodate_modified, uptodate_deleted, modified_uptodate, modified_modified, modified_reset, modified_deleted, deleted_uptodate, deleted_untracked, deleted_modified);
        commit(workDir);
        add(added_uptodate, added_modified, added_deleted);
        write(modified_deleted, "modification modified_deleted");
        write(modified_modified, "modification modified_modified");
        write(modified_reset, "modification modified_reset");
        write(modified_uptodate, "modification modified_uptodate");
        add(modified_deleted, modified_modified, modified_reset, modified_uptodate);
        VCSFileProxySupport.delete(deleted_uptodate);
        VCSFileProxySupport.delete(deleted_untracked);
        VCSFileProxySupport.delete(deleted_modified);
        remove(true, deleted_uptodate, deleted_untracked, deleted_modified);
        write(added_modified, "modification2 added_modified");
        write(uptodate_modified, "modification2 uptodate_modified");
        write(modified_modified, "modification2 modified_modified");
        write(modified_reset, "modified_reset");
        VCSFileProxySupport.delete(added_deleted);
        VCSFileProxySupport.delete(modified_deleted);
        VCSFileProxySupport.delete(uptodate_deleted);
        write(deleted_untracked, "deleted_untracked");
        write(deleted_modified, "deleted_modified\nchange");
        
        GitClient client = getClient(workDir);
        String revId = client.getBranches(false, NULL_PROGRESS_MONITOR).get(GitConstants.MASTER).getId();
        
        VCSFileProxy someFile = VCSFileProxy.createFileProxy(workDir, "fileforothercommit");
        write(someFile, "fileforothercommit");
        add(someFile);
        commit(someFile);

        TestStatusListener listener = new TestStatusListener();
        client.addNotificationListener(listener);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { workDir }, GitConstants.HEAD, NULL_PROGRESS_MONITOR);
        assertFalse(statuses.isEmpty());
        //?? untracked
        assertStatus(statuses, workDir, untracked, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false, listener);
        //A  added-uptodate
        assertStatus(statuses, workDir, added_uptodate, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false, listener);
        //AM added-modified
        assertStatus(statuses, workDir, added_modified, true, Status.STATUS_ADDED, Status.STATUS_MODIFIED, Status.STATUS_ADDED, false, listener);
        //AD added-deleted
        assertStatus(statuses, workDir, added_deleted, true, Status.STATUS_ADDED, Status.STATUS_REMOVED, Status.STATUS_NORMAL, false, listener);
        assertStatus(statuses, workDir, uptodate_uptodate, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, listener);
        // M uptodate-modified
        assertStatus(statuses, workDir, uptodate_modified, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, listener);
        // D uptodate-deleted
        assertStatus(statuses, workDir, uptodate_deleted, true, Status.STATUS_NORMAL, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false, listener);
        //M  modified-uptodate
        assertStatus(statuses, workDir, modified_uptodate, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false, listener);
        //MM modified-modified
        assertStatus(statuses, workDir, modified_modified, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, listener);
        //MM modified-reset
        assertStatus(statuses, workDir, modified_reset, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, false, listener);
        //MD modified-deleted
        assertStatus(statuses, workDir, modified_deleted, true, Status.STATUS_MODIFIED, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false, listener);
        //D  deleted-uptodate
        assertStatus(statuses, workDir, deleted_uptodate, true, Status.STATUS_REMOVED, Status.STATUS_NORMAL, Status.STATUS_REMOVED, false, listener);
        //D  deleted-untracked
        //?? deleted-untracked
if(KIT) assertStatus(statuses, workDir, deleted_untracked, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_NORMAL, false, listener);
else    assertStatus(statuses, workDir, deleted_untracked, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_MODIFIED, false, listener);
        //D  deleted-modified
        //?? deleted-modified
        assertStatus(statuses, workDir, deleted_modified, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_MODIFIED, false, listener);
        assertStatus(statuses, workDir, someFile, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, listener);
        // what about isIgnored() here?
        //!! ignored
        assertStatus(statuses, workDir, ignored, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false, listener);
        
        listener = new TestStatusListener();
        client.addNotificationListener(listener);
        statuses = client.getStatus(new VCSFileProxy[] { workDir }, revId, NULL_PROGRESS_MONITOR);
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
if(KIT) assertStatus(statuses, workDir, deleted_untracked, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_NORMAL, false, listener);
else    assertStatus(statuses, workDir, deleted_untracked, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_MODIFIED, false, listener);
        assertStatus(statuses, workDir, deleted_modified, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_MODIFIED, false, listener);
        // what about isIgnored() here?
        assertStatus(statuses, workDir, ignored, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false, listener);
        // file somefile was not known in that revision
        assertStatus(statuses, workDir, someFile, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false, listener);
    }

    public void testStatusSingleFile () throws Exception {
        VCSFileProxy untracked = VCSFileProxy.createFileProxy(workDir, "untracked");
        write(untracked, "untracked");
        VCSFileProxy added_modified = VCSFileProxy.createFileProxy(workDir, "added-modified");
        write(added_modified, "added_modified");
        VCSFileProxy uptodate_modified = VCSFileProxy.createFileProxy(workDir, "uptodate-modified");
        write(uptodate_modified, "uptodate_modified");
        VCSFileProxy modified_modified = VCSFileProxy.createFileProxy(workDir, "modified-modified");
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
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { untracked }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, untracked, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false, monitor);
        monitor = new TestStatusListener();
        client.addNotificationListener(monitor);
        statuses = client.getStatus(new VCSFileProxy[] { added_modified }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, added_modified, true, Status.STATUS_ADDED, Status.STATUS_MODIFIED, Status.STATUS_ADDED, false, monitor);
        monitor = new TestStatusListener();
        client.addNotificationListener(monitor);
        statuses = client.getStatus(new VCSFileProxy[] { uptodate_modified }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, uptodate_modified, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, monitor);
        monitor = new TestStatusListener();
        client.addNotificationListener(monitor);
        statuses = client.getStatus(new VCSFileProxy[] { modified_modified }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, modified_modified, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false, monitor);
    }

    public void testStatusTree () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder1");
        VCSFileProxySupport.mkdirs(folder);
        write(VCSFileProxy.createFileProxy(folder, "untracked1"), "untracked");
        write(VCSFileProxy.createFileProxy(folder, "untracked2"), "untracked");
        folder = VCSFileProxy.createFileProxy(workDir, "folder2");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy f1 = VCSFileProxy.createFileProxy(folder, "f1");
        write(f1, "f1");
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(folder, "f2");
        write(f2, "f2");
        VCSFileProxy folder21 = VCSFileProxy.createFileProxy(folder, "folder21");
        VCSFileProxySupport.mkdirs(folder21);
        VCSFileProxy f3 = VCSFileProxy.createFileProxy(folder21, "f3");
        write(f3, "f3");
        VCSFileProxy f4 = VCSFileProxy.createFileProxy(folder21, "f4");
        write(f4, "f4");
        VCSFileProxy folder22 = VCSFileProxy.createFileProxy(folder, "folder22");
        VCSFileProxySupport.mkdirs(folder22);
        VCSFileProxy f5 = VCSFileProxy.createFileProxy(folder22, "f5");
        write(f5, "f5");
        VCSFileProxy f6 = VCSFileProxy.createFileProxy(folder22, "f6");
        write(f6, "f6");

        add(f1, f2, f3, f4, f5, f6);
        commit(f1, f2, f3, f4, f5, f6);

        GitClient client = getClient(workDir);
        TestStatusListener monitor = new TestStatusListener();
        client.addNotificationListener(monitor);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { folder }, NULL_PROGRESS_MONITOR);
        assertEquals(6, statuses.size());
        assertStatus(statuses, workDir, f1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f3, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f4, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f5, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
        assertStatus(statuses, workDir, f6, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false, monitor);
    }
    
    public void testStatusDifferentTree () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir.getParentFile(), "folder1");
        VCSFileProxySupport.mkdirs(folder);
        try {
            StatusListener monitor = new TestStatusListener();
            getClient(workDir).getStatus(new VCSFileProxy[] { folder }, NULL_PROGRESS_MONITOR);
            fail("Different tree");
        } catch (IllegalArgumentException ex) {
            // OK
        }
    }

    public void testSkipIgnoredFolders () throws Exception {
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "file1");
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(folder, "file2");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxySupport.createNew(file1);
        VCSFileProxySupport.createNew(file2);
        VCSFileProxy subFolder = VCSFileProxy.createFileProxy(folder, "subfolder");
        VCSFileProxy file3 = VCSFileProxy.createFileProxy(folder, "file3");
        VCSFileProxy file4 = VCSFileProxy.createFileProxy(subFolder, "file4");
        VCSFileProxySupport.mkdirs(subFolder);
        VCSFileProxySupport.createNew(file3);
        VCSFileProxySupport.createNew(file4);
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(workDir, "folder2");
        VCSFileProxySupport.mkdirs(folder2);
        VCSFileProxy file5 = VCSFileProxy.createFileProxy(folder2, "file5");
        VCSFileProxySupport.createNew(file5);
        VCSFileProxy subFolder2 = VCSFileProxy.createFileProxy(folder2, "subfolder");
        VCSFileProxy file6 = VCSFileProxy.createFileProxy(subFolder2, "file6");
        VCSFileProxySupport.mkdirs(subFolder2);
        VCSFileProxySupport.createNew(file6);

        write(VCSFileProxy.createFileProxy(workDir, GitConstants.GITIGNORE_FILENAME), "folder\nfile1");
        write(VCSFileProxy.createFileProxy(folder2, ".gitignore"), "subfolder");

        Map<VCSFileProxy, GitStatus> statuses = getClient(workDir).getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, folder, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
        assertTrue(statuses.get(folder).isFolder());
        assertNull(statuses.get(file2));
        assertNull(statuses.get(file3));
        assertNull(statuses.get(file4));
        assertStatus(statuses, workDir, file5, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, subFolder2, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
if(KIT) assertTrue(statuses.get(subFolder2).isFolder());
else    assertNull(statuses.get(subFolder2));
        assertNull(statuses.get(file6));

        statuses = getClient(workDir).getStatus(new VCSFileProxy[] { folder }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, folder, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
        assertTrue(statuses.get(folder).isFolder());
        assertNull(statuses.get(file2));
        assertNull(statuses.get(file3));
        assertNull(statuses.get(file4));
        
        statuses = getClient(workDir).getStatus(new VCSFileProxy[] { file2 }, NULL_PROGRESS_MONITOR);
if(KIT) assertStatus(statuses, workDir, file2, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
else    assertStatus(statuses, workDir, file2, false, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertNull(statuses.get(folder));
        assertNull(statuses.get(file3));
        assertNull(statuses.get(file4));

        statuses = getClient(workDir).getStatus(new VCSFileProxy[] { folder, file2 }, NULL_PROGRESS_MONITOR);
if(KIT) assertStatus(statuses, workDir, file2, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
else    assertStatus(statuses, workDir, file2, false, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, folder, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
        assertTrue(statuses.get(folder).isFolder());
        assertNull(statuses.get(file3));
        assertNull(statuses.get(file4));

        statuses = getClient(workDir).getStatus(new VCSFileProxy[] { folder, file2, file3 }, NULL_PROGRESS_MONITOR);
if(KIT) assertStatus(statuses, workDir, file2, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
else    assertStatus(statuses, workDir, file2, false, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
if(KIT) assertStatus(statuses, workDir, file3, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
else    assertStatus(statuses, workDir, file3, false, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, folder, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
        assertTrue(statuses.get(folder).isFolder());
        assertNull(statuses.get(file4));

        // FIXME: childs of ignored folder should be ignored, but we don't have any information if these files exist
        statuses = getClient(workDir).getStatus(new VCSFileProxy[] { folder, file2, file3, file4 }, NULL_PROGRESS_MONITOR);
if(KIT) assertStatus(statuses, workDir, file2, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
else    assertStatus(statuses, workDir, file2, false, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
if(KIT) assertStatus(statuses, workDir, file3, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
else    assertStatus(statuses, workDir, file3, false, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
if(KIT) assertStatus(statuses, workDir, file4, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
else    assertStatus(statuses, workDir, file4, false, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, folder, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_IGNORED, false);
        assertTrue(statuses.get(folder).isFolder());
    }

    public void testIgnoredFilesAreNotTracked () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "ignoredFile");
        VCSFileProxySupport.createNew(file);
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "ignoredFolder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(workDir, "ignoredFolder2");
        VCSFileProxySupport.mkdirs(folder2);
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(folder2, "addedFile");
        VCSFileProxySupport.createNew(file2);
        VCSFileProxy ignoreFile = VCSFileProxy.createFileProxy(workDir, GitConstants.GITIGNORE_FILENAME);
        write(ignoreFile, "ignored*");

        add(file2);

        Map<VCSFileProxy, GitStatus> statuses = getClient(workDir).getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertFalse(statuses.get(file).isTracked());
        assertFalse(statuses.get(folder).isTracked());
        assertFalse(statuses.get(folder2).isTracked());
    }

    public void testCancel () throws Exception {
        final VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(file);
        final VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2");
        VCSFileProxySupport.createNew(file2);

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
                    client.getStatus(new VCSFileProxy[] { file, file2 }, m);
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
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxySupport.createNew(f);
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "f2");
        VCSFileProxySupport.createNew(f2);
        VCSFileProxy[] roots = new VCSFileProxy[] { f, f2 };
        add(roots);
        commit(roots);
        Map<VCSFileProxy, GitStatus> conflicts = client.getConflicts(roots, NULL_PROGRESS_MONITOR);
        assertEquals(0, conflicts.size());

        JGitRepository remoteGitRepository = getRemoteGitRepository();
        VCSFileProxy remoteLocation = remoteGitRepository.getLocation();
        VCSFileProxy rf = VCSFileProxy.createFileProxy(remoteLocation, "f");
        //write(rf, "remote f");
        VCSFileProxySupport.createNew(rf);
        VCSFileProxy rf2 = VCSFileProxy.createFileProxy(remoteLocation, "f2");
        //write(rf2, "remote f2");
        VCSFileProxySupport.createNew(rf2);
        VCSFileProxy[] rroots = new VCSFileProxy[] { rf, rf2 };
        GitClient remote = getClient(remoteLocation);
        remote.add(rroots, NULL_PROGRESS_MONITOR);
        remote.commit(rroots, "init", null, null, NULL_PROGRESS_MONITOR);
        client.fetch("origin", NULL_PROGRESS_MONITOR);
        //client.pull(remoteLocation.getPath(), Arrays.asList(new String[] { "refs/heads/master:refs/remotes/origin/master" }), "master", NULL_PROGRESS_MONITOR);

        conflicts = client.getConflicts(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals(1, conflicts.size());
        conflicts = client.getConflicts(new VCSFileProxy[] { f2 }, NULL_PROGRESS_MONITOR);
        assertEquals(1, conflicts.size());
        conflicts = client.getConflicts(roots, NULL_PROGRESS_MONITOR);
        assertEquals(2, conflicts.size());
    }
    
    public void testIgnoredInExlude () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "hi, i am ignored");
        VCSFileProxy exclude = VCSFileProxy.createFileProxy(workDir, ".git/info/exclude");
        VCSFileProxySupport.mkdirs(exclude.getParentFile());
        write(exclude, "f");
        GitStatus st = getClient(workDir).getStatus(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals(Status.STATUS_IGNORED, st.getStatusIndexWC());
    }
    
    public void testIgnoreExecutable () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "hi, i am executable");
        VCSFileProxySupport.setExecutable(f, true);
        VCSFileProxy[] roots = { f };
        add(roots);
        commit(roots);
        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        
        VCSFileProxySupport.setExecutable(f, false);
        statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        
        JGitConfig config = repository.getConfig();
        config.setBoolean(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_FILEMODE, false);
        config.save();
        statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        
        config.setBoolean(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_FILEMODE, true);
        config.save();
        add(roots);
        statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        
        config.setBoolean(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_FILEMODE, false);
        config.save();
        add(roots);
        statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
if(KIT) assertStatus(statuses, workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
else    assertStatus(statuses, workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
    }

    // must not return status for nested repositories
    public void testStatusNested () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "file");
        
        GitClient client = getClient(workDir);
        client.add(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        VCSFileProxy nested = VCSFileProxy.createFileProxy(workDir, "nested");
        VCSFileProxySupport.mkdirs(nested);
        VCSFileProxySupport.mkdirs(VCSFileProxy.createFileProxy(workDir, "emptyFolder"));
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size()); // commandline is silent about empty folders
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        GitClient clientNested = getClient(nested);
        clientNested.init(NULL_PROGRESS_MONITOR);
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(nested, "f");
        write(f2, "file");
        clientNested.add(new VCSFileProxy[] { f2 }, NULL_PROGRESS_MONITOR);
        clientNested.commit(new VCSFileProxy[] { f2 }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size()); // on the other hand, nested repository parent should be listed as is on commandline
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, nested, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
        
        statuses = clientNested.getStatus(new VCSFileProxy[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }
    
    public void testSymlinkedFolder () throws Exception {
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(workDir, "boo");
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "old_file");
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(workDir, "some_dir");
        VCSFileProxy file2_1 = VCSFileProxy.createFileProxy(folder2, "some_file");
        
        VCSFileProxySupport.mkdirs(folder1);
        VCSFileProxySupport.mkdirs(folder2);
        VCSFileProxySupport.createNew(file1);
        VCSFileProxySupport.createNew(file2_1);
        add(workDir);
        commit(workDir);
        
        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2_1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
     
        // create a symlink, not added to index
        String relPath = "../some_dir";
        VCSFileProxy link = VCSFileProxy.createFileProxy(folder1, folder2.getName());
        VCSFileProxySupport.createSymbolicLink(link, relPath);
        assertTrue(VCSFileProxySupport.isSymlink(link));
        statuses = client.getStatus(new VCSFileProxy[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
        
        // symlink is added to index, not yet committed
        client.add(new VCSFileProxy[] { link }, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new VCSFileProxy[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false);
        
        // symlink is committed
        client.commit(new VCSFileProxy[] { link }, "commit symlink", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new VCSFileProxy[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        
        // symlink is deleted on disk
        VCSFileProxySupport.delete(link);
        assertFalse(VCSFileProxySupport.isSymlink(link));
        statuses = client.getStatus(new VCSFileProxy[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, true, Status.STATUS_NORMAL, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false);
        
        // symlink is also deleted from index
        client.remove(new VCSFileProxy[] { link }, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new VCSFileProxy[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, true, Status.STATUS_REMOVED, Status.STATUS_NORMAL, Status.STATUS_REMOVED, false);
    }
    
    public void testIgnoredSymlinkFile () throws Exception {
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(workDir, "boo");
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "old_file");
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(workDir, "some_dir");
        VCSFileProxy file2_1 = VCSFileProxy.createFileProxy(folder2, "some_file");
        
        VCSFileProxySupport.mkdirs(folder1);
        VCSFileProxySupport.mkdirs(folder2);
        VCSFileProxySupport.createNew(file1);
        VCSFileProxySupport.createNew(file2_1);
        add(workDir);
        commit(workDir);
        
        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2_1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
     
        // create a symlink, not added to index
        String relPath = "../some_dir/some_file";
        VCSFileProxy link = VCSFileProxy.createFileProxy(folder1, file2_1.getName());
        VCSFileProxySupport.createSymbolicLink(link, relPath);
        assertTrue(VCSFileProxySupport.isSymlink(link));
        statuses = client.getStatus(new VCSFileProxy[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
        
        client.ignore(new VCSFileProxy[] { link }, NULL_PROGRESS_MONITOR);
        assertEquals("/boo/some_file", read(VCSFileProxy.createFileProxy(workDir, GitConstants.GITIGNORE_FILENAME)));
        statuses = client.getStatus(new VCSFileProxy[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
    }
    
    public void testIgnoredSymlinkFolder () throws Exception {
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(workDir, "boo");
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "old_file");
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(workDir, "some_dir");
        VCSFileProxy file2_1 = VCSFileProxy.createFileProxy(folder2, "some_file");
        
        VCSFileProxySupport.mkdirs(folder1);
        VCSFileProxySupport.mkdirs(folder2);
        VCSFileProxySupport.createNew(file1);
        VCSFileProxySupport.createNew(file2_1);
        add(workDir);
        commit(workDir);
        
        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2_1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
     
        // create a symlink, not added to index
        String relPath = "../some_dir";
        VCSFileProxy link = VCSFileProxy.createFileProxy(folder1, folder2.getName());
        VCSFileProxySupport.createSymbolicLink(link, relPath);
        assertTrue(VCSFileProxySupport.isSymlink(link));
        statuses = client.getStatus(new VCSFileProxy[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
        
        client.ignore(new VCSFileProxy[] { link }, NULL_PROGRESS_MONITOR);
        assertEquals("/boo/some_dir/", read(VCSFileProxy.createFileProxy(workDir, GitConstants.GITIGNORE_FILENAME)));
        statuses = client.getStatus(new VCSFileProxy[] { link }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, link, false, Status.STATUS_NORMAL, Status.STATUS_IGNORED, Status.STATUS_ADDED, false);
    }
    
    public void testLastIndexModificationDate () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        
        GitClient client = getClient(workDir);
        write(f, "init");
        
        // not yet added to the index => ts: -1
        GitStatus status = client.getStatus(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals(-1, status.getIndexEntryModificationDate());
        
        add(f);
        // added => current timestamp
        status = client.getStatus(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR).get(f);
        long ts = f.lastModified();
if(KIT) assertEquals((ts / 1000) * 1000, (status.getIndexEntryModificationDate() / 1000) * 1000);
else    assertEquals(-1, status.getIndexEntryModificationDate()); 

        commit(f);
        // still the same => current timestamp
        status = client.getStatus(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR).get(f);
if(KIT) assertEquals((ts / 1000) * 1000, (status.getIndexEntryModificationDate() / 1000) * 1000);
else    assertEquals(-1, status.getIndexEntryModificationDate()); 
        
        Thread.sleep(1000);
        write(f, "modification");
        // modified => both should differ
        status = client.getStatus(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR).get(f);
if(KIT) assertEquals((ts / 1000) * 1000, (status.getIndexEntryModificationDate() / 1000) * 1000);
else    assertEquals(-1, status.getIndexEntryModificationDate()); 
        
        ts = f.lastModified();
if(KIT) assertNotSame((ts / 1000) * 1000, (status.getIndexEntryModificationDate() / 1000) * 1000);
else    assertEquals(-1, status.getIndexEntryModificationDate()); 
        
        add(f);
        // updated -> both are the same
        status = client.getStatus(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR).get(f);
if(KIT) assertEquals((ts / 1000) * 1000, (status.getIndexEntryModificationDate() / 1000) * 1000);
else    assertEquals(-1, status.getIndexEntryModificationDate()); 
        
        client.remove(new VCSFileProxy[] { f }, true, NULL_PROGRESS_MONITOR);
        // removed => ts: -1
        status = client.getStatus(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR).get(f);
if(KIT) assertEquals(-1, status.getIndexEntryModificationDate());
else    assertEquals(-1, status.getIndexEntryModificationDate()); 
    }
    
}
