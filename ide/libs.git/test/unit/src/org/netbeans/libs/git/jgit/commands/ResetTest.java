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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitClient.ResetType;
import org.netbeans.libs.git.GitConflictDescriptor;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class ResetTest extends AbstractGitTestCase {

    private File workDir;
    private Repository repository;

    public ResetTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testResetSoft () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        File file2 = new File(workDir, "file2");
        write(file2, "blablablabla in file2");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files,NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        write(file1, "change in content");
        add(file1);
        commit(files);

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        client.reset(revision, ResetType.SOFT, NULL_PROGRESS_MONITOR);

        assertEquals(revision, new Git(repository).log().call().iterator().next().getId().getName());
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }

    public void testResetMixed () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        File file2 = new File(workDir, "file2");
        write(file2, "blablablabla in file2");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files,NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        write(file1, "change in content");
        add(file1);
        commit(files);

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        client.reset(revision, ResetType.MIXED, NULL_PROGRESS_MONITOR);

        assertEquals(revision, new Git(repository).log().call().iterator().next().getId().getName());
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }

    public void testResetHard () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        File file2 = new File(workDir, "file2");
        write(file2, "blablablabla in file2");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files,NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        write(file1, "change in content");
        add(file1);
        commit(files);

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        long ts = file2.lastModified();
        Thread.sleep(1000);
        client.reset(revision, ResetType.HARD, NULL_PROGRESS_MONITOR);
        assertEquals(ts, file2.lastModified());

        assertEquals(revision, new Git(repository).log().call().iterator().next().getId().getName());
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }

    public void testResetHardTypeConflict () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        File file2 = new File(file1, "f");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files,NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        client.remove(files, false, NULL_PROGRESS_MONITOR);
        file1.mkdirs();
        assertTrue(file1.isDirectory());
        write(file2, "ssss");
        write(new File(file1, "untracked"), "ssss");
        add(file2);
        commit(files);

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        RevCommit commitCurrent = logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        client.reset(revision, ResetType.HARD, NULL_PROGRESS_MONITOR);

        assertEquals(revision, new Git(repository).log().call().iterator().next().getId().getName());
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertTrue(file1.isFile());

        String currentRevision = commitCurrent.getId().getName();
        client.reset(currentRevision, ResetType.HARD, NULL_PROGRESS_MONITOR);

        assertEquals(currentRevision, new Git(repository).log().call().iterator().next().getId().getName());
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertTrue(file1.isDirectory());
    }

    public void testResetHardOverwritesModification () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        File[] files = new File[] { file1 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files,NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        write(file1, "change in content");
        add(file1);
        commit(files);
        write(file1, "hello, i have local modifications");

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        client.reset(revision, ResetType.HARD, NULL_PROGRESS_MONITOR);

        assertEquals(revision, new Git(repository).log().call().iterator().next().getId().getName());
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertEquals("blablablabla", read(file1));
    }

    public void testResetHardRemoveFile () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        File file2 = new File(workDir, "file2");
        write(file2, "blablablabla");
        File[] files = new File[] { file1 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        add(file2);
        files = new File[] { file1, file2 };
        commit(files);

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        assertTrue(file2.exists());
        client.reset(revision, ResetType.HARD, NULL_PROGRESS_MONITOR);

        assertEquals(revision, new Git(repository).log().call().iterator().next().getId().getName());
        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertFalse(file2.exists());
    }

    public void testResetPaths () throws Exception {
        File file1 = new File(workDir, "file1"); // index entry will be modified
        write(file1, "blablablabla");
        File file2 = new File(workDir, "file2"); // index entry will be left alone
        write(file2, "blablablabla in file2");
        File file3 = new File(workDir, "file3"); // index entry will be added
        write(file3, "blablablabla in file3");
        File file4 = new File(workDir, "file4"); // index entry will be removed
        File[] files = new File[] { file1, file2, file3, file4 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);

        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(3, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file3, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        String content = "change in content";
        write(file1, content);
        write(file4, "blablablabla in file4");
        client.add(files, NULL_PROGRESS_MONITOR);
        commit(files);
        write(file2, "change in content in file 2");
        client.add(new File[] { file2 }, NULL_PROGRESS_MONITOR);
        client.remove(new File[] { file3 }, false,NULL_PROGRESS_MONITOR);

        LogCommand cmd = new Git(repository).log();
        Iterator<RevCommit> logs = cmd.call().iterator();
        logs.next();
        RevCommit commit = logs.next();
        String revision = commit.getId().getName();
        client.reset(new File[] { file1, file3, file4 }, revision, true, NULL_PROGRESS_MONITOR);

        // file1: modified HEAD-INDEX
        // file2: stays modified HEAD-INDEX
        // file3: removed in WT, normal HEAD-INDEX
        // file4: removed in index, normal in WT

        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(4, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file3, true, Status.STATUS_NORMAL, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, file4, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_NORMAL, false);
        assertEquals(content, read(file1));
    }
    
    public void testResetPaths_NonRecursive () throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File file1 = new File(folder, "file1"); // index entry will be modified
        write(file1, "blablablabla");
        File subfolder = new File(folder, "subfolder");
        subfolder.mkdirs();
        File file2 = new File(subfolder, "file2"); // index entry will be left alone
        write(file2, "blablablabla in file2");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        String content = "change in content";
        write(file1, content);
        write(file2, content);
        client.add(files, NULL_PROGRESS_MONITOR);
        
        // children
        client.reset(new File[] { folder }, "HEAD", false, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);

        write(file1, content);
        // recursive
        client.reset(new File[] { folder }, "HEAD", true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        
        write(file1, content);
        add(file1);
        // non recursive on file
        client.reset(new File[] { file1 }, "HEAD", false, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
    }

    public void testResetPathsChangeType () throws Exception {
        File file = new File(workDir, "f"); // index entry will be modified
        File file2 = new File(file, "file");
        write(file, "blablablabla");
        File[] files = new File[] { file, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        client.remove(files, false, NULL_PROGRESS_MONITOR);
        commit(files);
        file.mkdirs();
        write(file2, "aaaa");
        add(file2);
        commit(files);

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        String revisionCurrent = logs.next().getId().getName();
        logs.next();
        String revisionPrevious = logs.next().getId().getName();

        client.reset(files, revisionPrevious, true, NULL_PROGRESS_MONITOR);
        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file, true, Status.STATUS_ADDED, Status.STATUS_REMOVED, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_NORMAL, false);

        client.reset(files, revisionCurrent, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }

    // must not checkout from nested repositories
    public void testResetNested () throws Exception {
        File f = new File(workDir, "f");
        write(f, "file");
        
        GitClient client = getClient(workDir);
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        File nested = new File(workDir, "nested");
        nested.mkdirs();
        File f2 = new File(nested, "f");
        write(f2, "file");
        GitClient clientNested = getClient(nested);
        clientNested.init(NULL_PROGRESS_MONITOR);
        clientNested.add(new File[] { f2 }, NULL_PROGRESS_MONITOR);
        clientNested.commit(new File[] { f2 }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        write(f, "change");
        add(f);
        write(f2, "change");
        clientNested.add(new File[] { f2 }, NULL_PROGRESS_MONITOR);
        
        client.reset(new File[] { workDir, nested }, "HEAD", true, NULL_PROGRESS_MONITOR);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, nested, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
        statuses = clientNested.getStatus(new File[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        
        client.reset("master", ResetType.MIXED, NULL_PROGRESS_MONITOR);
        statuses = clientNested.getStatus(new File[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        
        client.reset("master", ResetType.HARD, NULL_PROGRESS_MONITOR);
        statuses = clientNested.getStatus(new File[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
    }
    
    public void testLineEndingsWindows () throws Exception {
        if (!isWindows()) {
            return;
        }
        // lets turn autocrlf on
        Thread.sleep(1100);
        StoredConfig cfg = repository.getConfig();
        cfg.setString(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOCRLF, "true");
        cfg.save();
        
        File f = new File(workDir, "f");
        write(f, "a\r\nb\r\n");
        File[] roots = new File[] { f };
        
        GitClient client = getClient(workDir);
        runExternally(workDir, Arrays.asList("git.cmd", "add", "f"));
        List<String> res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(Arrays.asList("A  f"), res);
        DirCacheEntry e1 = repository.readDirCache().getEntry("f");
        runExternally(workDir, Arrays.asList("git.cmd", "commit", "-m", "hello"));
        
        write(f, "a\r\nb\r\nc\r\n");
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(Arrays.asList(" M f"), res);
        runExternally(workDir, Arrays.asList("git.cmd", "add", "f"));
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(Arrays.asList("M  f"), res);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        
        client.reset(roots, "HEAD", true, NULL_PROGRESS_MONITOR);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        assertEquals(e1.getObjectId(), repository.readDirCache().getEntry("f").getObjectId());
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(Arrays.asList(" M f"), res);
        
        runExternally(workDir, Arrays.asList("git.cmd", "add", "f"));
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(Arrays.asList("M  f"), res);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        
        client.reset("HEAD", ResetType.HARD, NULL_PROGRESS_MONITOR);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertEquals(e1.getObjectId(), repository.readDirCache().getEntry("f").getObjectId());
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(0, res.size());
    }
    
    public void testResetConflict () throws Exception {
        File file = new File(workDir, "file");
        write(file, "init");
        File[] files = new File[] { file };
        add(files);
        commit(files);

        DirCache index = repository.lockDirCache();
        DirCacheBuilder builder = index.builder();
        DirCacheEntry e = index.getEntry(file.getName());
        DirCacheEntry e1 = new DirCacheEntry(file.getName(), 1);
        e1.setCreationTime(e.getCreationTime());
        e1.setFileMode(e.getFileMode());
        e1.setLastModified(e.getLastModifiedInstant());
        e1.setLength(e.getLength());
        e1.setObjectId(e.getObjectId());
        builder.add(e1);
        builder.finish();
        builder.commit();
        
        GitClient client = getClient(workDir);
        Map<File, GitStatus> status = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertTrue(status.get(file).isConflict());
        assertEquals(GitConflictDescriptor.Type.BOTH_DELETED, status.get(file).getConflictDescriptor().getType());
        
        client.reset(files, "HEAD", true, NULL_PROGRESS_MONITOR);
        status = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertFalse(status.get(file).isConflict());
    }
    
}
