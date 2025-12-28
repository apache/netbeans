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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.dircache.Checkout;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.errors.CheckoutConflictException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.WindowCacheConfig;
import org.netbeans.junit.Filter;
import org.netbeans.libs.git.ApiUtils;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;

/**
 *
 * @author ondra
 */
public class CheckoutTest extends AbstractGitTestCase {

    private File workDir;
    private Repository repository;
    private static final String BRANCH = "nova";

    public CheckoutTest (String testName) throws IOException {
        super(testName);
        if (Boolean.getBoolean("skip.git.integration.tests")) {
            Filter filter = new Filter();
            filter.setExcludes(new Filter.IncludeExclude[] {
                new Filter.IncludeExclude("testLargeFile", ""),
            });
            setFilter(filter);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testJGitCheckout () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        Git git = new Git(repository);
        org.eclipse.jgit.api.AddCommand cmd = git.add();
        cmd.addFilepattern("file1");
        cmd.call();

        org.eclipse.jgit.api.CommitCommand commitCmd = git.commit();
        commitCmd.setAuthor("author", "author@something");
        commitCmd.setMessage("commit message");
        commitCmd.call();

        String commitId = git.log().call().iterator().next().getId().getName();
        DirCache cache = repository.lockDirCache();
        try {
            DirCacheCheckout checkout = new DirCacheCheckout(repository, null, cache, new RevWalk(repository).parseCommit(repository.resolve(commitId)).getTree());
            checkout.checkout();
        } finally {
            cache.unlock();
        }
    }

    public void testCheckoutFilesFromIndex () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "file 1 content");
        File file2 = new File(workDir, "file2");
        write(file2, "file 2 content");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        String content1 = "change in file 1";
        write(file1, content1);
        write(file2, "change in file 2");
        add(files);

        write(file1, "another change in file 1");
        String content2 = "another change in file 2";
        write(file2, content2);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        client.checkout(new File[] { file1 }, null, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file1));
        assertEquals(content2, read(file2));
        write(file1, "another change in file 1");
        client.checkout(new File[] { file1 }, null, false, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file1));
        assertEquals(content2, read(file2));

        file1.delete();
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        client.checkout(new File[] { file1 }, null, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file1));
        assertEquals(content2, read(file2));
        file1.delete();
        client.checkout(new File[] { file1 }, null, false, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file1));
        assertEquals(content2, read(file2));
    }

    // @TODO randomly failing
    public void /*test*/CheckoutFilesFromIndex_NotRecursive () throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File file1 = new File(folder, "file1");
        write(file1, "file 1 content");
        File subFolder = new File(folder, "subfolder");
        subFolder.mkdirs();
        File file2 = new File(subFolder, "file2");
        write(file2, "file 2 content");
        File[] files = new File[] { file1, file2 };
        add(files);
        commit(files);

        String content1 = "change 1";
        write(file1, content1);
        write(file2, content1);
        add(files);

        String content2 = "change 2";
        write(file1, content2);
        write(file2, content2);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);

        // direct file descendants
        client.checkout(new File[] { folder }, null, false, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file1));
        assertEquals(content2, read(file2));

        write(file1, content2);
        // recursive
        client.checkout(new File[] { folder }, null, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file1));
        assertEquals(content1, read(file2));
    }

    public void testCheckoutFilesFromIndexFolderToFile () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "file 1 content");
        File file2 = new File(file1, "file2");
        File[] files = new File[] { file1 };
        add(files);
        commit(files);

        file1.delete();
        file1.mkdirs();
        write(file2, "blabla");

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_REMOVED, false);
        client.checkout(new File[] { file1 }, null, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assert(file1.isFile());
        assertEquals("file 1 content", read(file1));
    }

    public void testCheckoutFilesFromIndexFileToFolder () throws Exception {
        File folder = new File(workDir, "folder");
        File subFolder = new File(folder, "folder");
        File file1 = new File(subFolder, "file2");
        subFolder.mkdirs();
        write(file1, "file 1 content");
        File[] files = new File[] { folder };
        add(files);
        commit(files);

        file1.delete();
        subFolder.delete();
        folder.delete();
        write(folder, "blabla");

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_REMOVED, false);
        client.checkout(new File[] { folder }, null, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assert(file1.isFile());
        assertEquals("file 1 content", read(file1));
    }

    public void testCheckoutPathsFromRevision () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "file 1 content");
        File[] files = new File[] { file1 };
        add(files);
        commit(files);

        String content1 = "change in file 1";
        write(file1, content1);
        add(files);
        commit(files);

        write(file1, "another change in file 1");

        Iterator<RevCommit> logs = new Git(repository).log().call().iterator();
        String currentRevision = logs.next().getId().getName();
        String previousRevision = logs.next().getId().getName();

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        client.checkout(new File[] { file1 }, currentRevision, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertEquals(content1, read(file1));
        assertEquals(currentRevision, new Git(repository).log().call().iterator().next().getId().getName());
        write(file1, "another change in file 1");
        client.checkout(new File[] { file1 }, currentRevision, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertEquals(content1, read(file1));
        assertEquals(currentRevision, new Git(repository).log().call().iterator().next().getId().getName());

        write(file1, "another change in file 1");
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        client.checkout(new File[] { file1 }, previousRevision, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals("file 1 content", read(file1));
        assertEquals(currentRevision, new Git(repository).log().call().iterator().next().getId().getName());
        write(file1, "another change in file 1");
        client.checkout(new File[] { file1 }, previousRevision, false, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals("file 1 content", read(file1));
        assertEquals(currentRevision, new Git(repository).log().call().iterator().next().getId().getName());
    }
    
    
    public void testCheckoutPathsFromRevision_NotRecursive () throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File file1 = new File(folder, "file1");
        write(file1, "file 1 content");
        File subFolder = new File(folder, "subfolder");
        subFolder.mkdirs();
        File file2 = new File(subFolder, "file2");
        write(file2, "file 2 content");
        File[] files = new File[] { file1, file2 };
        add(files);
        GitClient client = getClient(workDir);
        commit(files);

        String content1 = "change 1";
        write(file1, content1);
        write(file2, content1);
        add(files);

        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);

        // direct file descendants
        client.checkout(new File[] { folder }, "HEAD", false, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertEquals(content1, read(file2));

        write(file1, content1);
        add(files);
        // recursive
        client.checkout(new File[] { folder }, "HEAD", true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
    }

    public void testLargeFile () throws Exception {
        unpack("large.dat.zip");
        File large = new File(workDir, "large.dat");
        assertTrue(large.exists());
        assertEquals(2158310, large.length());
        add();
        DirCache cache = repository.readDirCache();
        DirCacheEntry e = cache.getEntry("large.dat");
        WindowCacheConfig cfg = new WindowCacheConfig();
        cfg.setStreamFileThreshold((int) large.length() - 1);
        cfg.install();
        new Checkout(repository)
                .setRecursiveDeletion(false)
                .checkout(e, null, repository.newObjectReader(), null);
    }
    
    public void testCheckoutBranch () throws Exception {
        File file = new File(workDir, "file");
        write(file, "initial");
        File[] files = new File[] { file };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.commit(files, "initial", null, null, NULL_PROGRESS_MONITOR);
        client.createBranch(BRANCH, info.getRevision(), NULL_PROGRESS_MONITOR);
        
        write(file, Constants.MASTER);
        add(file);
        GitRevisionInfo masterInfo = client.commit(files, Constants.MASTER, null, null, NULL_PROGRESS_MONITOR);
        
        // test checkout
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        String logFileContent[] = read(new File(workDir, ".git/logs/HEAD")).split("\\n");
        assertEquals("checkout: moving from master to nova", logFileContent[logFileContent.length - 1].substring(logFileContent[logFileContent.length - 1].indexOf("checkout: ")));
        assertTrue(m.notifiedFiles.contains(file));
        assertEquals("initial", read(file));
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        Map<String, GitBranch> branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(BRANCH).isActive());
        
        write(file, BRANCH);
        add();
        GitRevisionInfo novaInfo = client.commit(files, BRANCH, null, null, NULL_PROGRESS_MONITOR);
        m = new Monitor();
        client.addNotificationListener(m);
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        assertTrue(m.notifiedFiles.contains(file));
        assertEquals(Constants.MASTER, read(file));
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(Constants.MASTER).isActive());
        
        m = new Monitor();
        client.addNotificationListener(m);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        assertTrue(m.notifiedFiles.contains(file));
        assertEquals(BRANCH, read(file));
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(BRANCH).isActive());
    }
    
    public void testCheckoutRevision () throws Exception {
        File file = new File(workDir, "file");
        write(file, "initial");
        File[] files = new File[] { file };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.commit(files, "initial", null, null, NULL_PROGRESS_MONITOR);
        
        write(file, Constants.MASTER);
        add(file);
        GitRevisionInfo masterInfo = client.commit(files, Constants.MASTER, null, null, NULL_PROGRESS_MONITOR);
        
        // test checkout
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.checkoutRevision(info.getRevision(), true, NULL_PROGRESS_MONITOR);
        String logFileContent[] = read(new File(workDir, ".git/logs/HEAD")).split("\\n");
        assertEquals("checkout: moving from master to " + info.getRevision(), logFileContent[logFileContent.length - 1].substring(logFileContent[logFileContent.length - 1].indexOf("checkout: ")));
        assertTrue(m.notifiedFiles.contains(file));
        assertEquals("initial", read(file));
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        Map<String, GitBranch> branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(GitBranch.NO_BRANCH).isActive());
        
        write(file, BRANCH);
        add();
        GitRevisionInfo novaInfo = client.commit(files, BRANCH, null, null, NULL_PROGRESS_MONITOR);
        m = new Monitor();
        client.addNotificationListener(m);
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        assertTrue(m.notifiedFiles.contains(file));
        assertEquals(Constants.MASTER, read(file));
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(Constants.MASTER).isActive());
    }
    
    public void testCheckoutRevisionKeepLocalChanges () throws Exception {
        File file = new File(workDir, "file");
        write(file, "initial");
        File[] files = new File[] { file };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.commit(files, "initial", null, null, NULL_PROGRESS_MONITOR);
        client.createBranch(BRANCH, info.getRevision(), NULL_PROGRESS_MONITOR);
        
        write(file, Constants.MASTER);
        
        // test checkout
        // the file remains modified in WT
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        assertEquals(Constants.MASTER, read(file));
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        Map<String, GitBranch> branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(BRANCH).isActive());
        
        add(file);
        // the file remains modified in index
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        assertEquals(Constants.MASTER, read(file));
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(Constants.MASTER).isActive());
    }
    
    public void testCheckoutRevisionAddRemoveFile () throws Exception {
        File file = new File(workDir, "file");
        write(file, "initial");
        File[] files = new File[] { file };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.commit(files, "initial", null, null, NULL_PROGRESS_MONITOR);
        client.createBranch(BRANCH, info.getRevision(), NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        remove(false, file);
        commit(files);
        
        // test checkout
        // the file is added to WT
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        assertTrue(file.exists());
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        
        // the file is removed from WT
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        assertFalse(file.exists());
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertNull(statuses.get(file));
    }
    
    public void testCheckoutRevisionMergeLocalChanges () throws Exception {
        File file = new File(workDir, "file");
        write(file, "initial");
        File[] files = new File[] { file };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.commit(files, "initial", null, null, NULL_PROGRESS_MONITOR);
        client.createBranch(BRANCH, info.getRevision(), NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        write(file, BRANCH);
        add(file);
        client.commit(files, BRANCH, null, null, NULL_PROGRESS_MONITOR);
        write(file, "branch change");
        try {
            client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
            fail("Should fail, there are conflicts");
        } catch (GitException.CheckoutConflictException ex) {
            assertEquals(1, ex.getConflicts().length);
            assertEquals(file.getName(), ex.getConflicts()[0]);
            Map<String, GitBranch> branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
            assertTrue(branches.get(BRANCH).isActive());
        }

        client.reset(BRANCH, GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        write(file, "branch change");
        
        CheckoutRevisionCommand cmd = new CheckoutRevisionCommand(repository, ApiUtils.getClassFactory(), Constants.MASTER, false, NULL_PROGRESS_MONITOR, new FileListener() {
            @Override
            public void notifyFile (File file, String relativePathToRoot) { }
        });
        cmd.execute();
        Map<File, GitStatus> status = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertTrue(status.get(file).isConflict());
        assertEquals("<<<<<<< OURS\nbranch change\n=======\ninitial\n>>>>>>> THEIRS", read(file));
    }

    public void testCheckoutNoHeadYet () throws Exception {
        final File otherWT = new File(workDir.getParentFile(), "repo2");
        GitClient client = getClient(otherWT);
        client.init(NULL_PROGRESS_MONITOR);
        File f = new File(otherWT, "f");
        write(f, "init");
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        client = getClient(workDir);
        client.fetch(otherWT.getAbsolutePath(), Arrays.asList(new String[] { "refs/heads/*:refs/remotes/origin/*" }), NULL_PROGRESS_MONITOR);
        client.checkoutRevision("origin/master", true, NULL_PROGRESS_MONITOR);
    }

    // must not checkout from nested repositories
    public void testCheckoutIndexNested () throws Exception {
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
        write(f2, "change");
        
        client.checkout(new File[] { workDir }, null, true, NULL_PROGRESS_MONITOR);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, nested, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        statuses = clientNested.getStatus(new File[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        
        clientNested.add(new File[] { f2 }, NULL_PROGRESS_MONITOR);
        client.checkout(new File[] { workDir }, "HEAD", true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, nested, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        statuses = clientNested.getStatus(new File[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        
        client.checkoutRevision("master", true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, nested, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        statuses = clientNested.getStatus(new File[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
    }

    public void testCheckoutWithAddedNestedRoot () throws Exception {
        File f = new File(workDir, "f");
        write(f, "file");
        
        GitClient client = getClient(workDir);
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        client.createBranch(BRANCH, "master", NULL_PROGRESS_MONITOR);
        
        File nested = new File(workDir, "nested");
        nested.mkdirs();
        File f2 = new File(nested, "f");
        write(f2, "file");
        GitClient clientNested = getClient(nested);
        clientNested.init(NULL_PROGRESS_MONITOR);
        clientNested.add(new File[] { f2 }, NULL_PROGRESS_MONITOR);
        clientNested.commit(new File[] { f2 }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        // add the root as gitlink
        client.add(new File[] { nested }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { nested }, "nested repo added", null, null, NULL_PROGRESS_MONITOR);
        Utils.deleteRecursively(nested);
        nested.mkdirs();
        Map<File, GitStatus> statuses = client.getStatus(new File[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, nested, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        assertFalse(nested.isDirectory());
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        // ours
        assertFalse(nested.isDirectory());
        client.checkoutRevision("master", true, NULL_PROGRESS_MONITOR);
        assertTrue(nested.isDirectory());
        DirCacheEntry e = repository.readDirCache().getEntry("nested");
        assertEquals(FileMode.GITLINK, e.getFileMode());
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, nested, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        //checkout index - aka revert
        assertTrue(nested.delete());
        client.remove(new File[] { nested }, true, NULL_PROGRESS_MONITOR);
        client.checkout(new File[] { nested }, "master", true, NULL_PROGRESS_MONITOR);
        assertTrue(nested.isDirectory());
        e = repository.readDirCache().getEntry("nested");
        assertEquals(FileMode.GITLINK, e.getFileMode());
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, nested, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
    }

    private void checkoutJGitTestNestedAddedRoot (Repository repository, String revision) throws Exception {
        try {
            ObjectId headTree = Utils.findCommit(repository, Constants.HEAD).getTree();
            DirCache cache = repository.lockDirCache();
            DirCacheCheckout dco = null;
            RevCommit commit;
            try {
                commit = Utils.findCommit(repository, revision);
                dco = new DirCacheCheckout(repository, headTree, cache, commit.getTree());
                dco.setFailOnConflict(true);
                dco.checkout();
            } catch (CheckoutConflictException ex) {
                List<String> conflicts = dco.getConflicts();
                throw new GitException.CheckoutConflictException(conflicts.toArray(new String[0]));
            } finally {
                cache.unlock();
            }
        } catch (IOException ex) {
            throw new GitException(ex);
        }
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
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        
        client.checkout(roots, "HEAD", true, NULL_PROGRESS_MONITOR);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertEquals(e1.getObjectId(), repository.readDirCache().getEntry("f").getObjectId());
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(0, res.size());

        write(f, "a\r\nb\r\nc\r\n");
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(Arrays.asList(" M f"), res);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        
        client.checkout(roots, null, true, NULL_PROGRESS_MONITOR);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertEquals(e1.getObjectId(), repository.readDirCache().getEntry("f").getObjectId());
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(0, res.size());
    }
    
    public void testCheckoutAfterUnresolvedMerge () throws Exception {
        File file = new File(workDir, "file");
        write(file, "initial");
        File[] files = new File[] { file };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.commit(files, "initial", null, null, NULL_PROGRESS_MONITOR);
        client.createBranch(BRANCH, info.getRevision(), NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        write(file, BRANCH);
        add(file);
        client.commit(files, BRANCH, null, null, NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        write(file, "master change");
        add(file);
        client.commit(files, "master commit", null, null, NULL_PROGRESS_MONITOR);
        
        client.merge(BRANCH, NULL_PROGRESS_MONITOR);
        
        try {
            client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
            fail("Should fail, there are conflicts");
        } catch (GitException.CheckoutConflictException ex) {
            // ok
        }
        
        try {
            client.checkoutRevision(BRANCH, false, NULL_PROGRESS_MONITOR);
            fail("Should fail, there are conflicts");
        } catch (GitException ex) {
            assertEquals(ex.getMessage(), MessageFormat.format(Utils.getBundle(GitCommand.class).getString("MSG_Error_CannotCheckoutHasConflicts"), workDir));
        }
    }

    private void unpack (String filename) throws IOException {
        File zipLarge = new File(getDataDir(), filename);
        ZipInputStream is = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipLarge)));
        ZipEntry entry;
        while ((entry = is.getNextEntry()) != null) {
            File unpacked = new File(workDir, entry.getName());
            FileChannel channel = new FileOutputStream(unpacked).getChannel();
            byte[] bytes = new byte[2048];
            try {
                int len;
                long size = entry.getSize();
                while (size > 0 && (len = is.read(bytes, 0, 2048)) > 0) {
                    ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, len);
                    int j = channel.write(buffer);
                    size -= len;
                }
            } finally {
                channel.close();
            }
        }
        ZipEntry e = is.getNextEntry();
    }
}
