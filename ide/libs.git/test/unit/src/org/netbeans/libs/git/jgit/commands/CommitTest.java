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
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEditor;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitRevisionInfo.GitFileInfo;
import org.netbeans.libs.git.GitRevisionInfo.GitFileInfo.Status;
import org.netbeans.libs.git.GitUser;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.jgit.Utils;

/**
 *
 * @author ondra
 */
public class CommitTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public CommitTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testCommitNoRoots () throws Exception {
        File toCommit = new File(workDir, "testnotadd.txt");
        write(toCommit, "blablabla");
        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { toCommit }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        client.add(new File[] { toCommit }, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { toCommit }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        GitRevisionInfo info = client.commit(new File[0], "initial commit", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { toCommit }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Git git = new Git(repository);
        LogCommand log = git.log();
        RevCommit com = log.call().iterator().next();
        assertEquals("initial commit", info.getFullMessage());
        assertEquals("initial commit", com.getFullMessage());
        assertEquals(ObjectId.toString(com.getId()), info.getRevision());
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertTrue(modifiedFiles.get(toCommit).getStatus().equals(Status.ADDED));
    }

    public void testSingleFileCommit () throws Exception {
        repository.getConfig().setString("user", null, "name", "John");
        repository.getConfig().setString("user", null, "email", "john@git.com");
        repository.getConfig().save();

        File toCommit = new File(workDir, "testnotadd.txt");
        write(toCommit, "blablabla");
        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { toCommit }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        client.add(new File[] { toCommit }, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { toCommit }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        long t1 = System.currentTimeMillis();
        Thread.sleep(1000);
        GitRevisionInfo info = client.commit(new File[] { toCommit }, "initial commit", null, null,  NULL_PROGRESS_MONITOR);
        Thread.sleep(1000);
        long t2 = System.currentTimeMillis();
        statuses = client.getStatus(new File[] { toCommit }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertTrue(t1 <= info.getCommitTime() && t2 >= info.getCommitTime());

        Git git = new Git(repository);
        LogCommand log = git.log();
        RevCommit com = log.call().iterator().next();
        assertEquals("initial commit", info.getFullMessage());
        assertEquals("initial commit", com.getFullMessage());
        assertEquals( "john@git.com", info.getAuthor().getEmailAddress());
        assertEquals( "john@git.com", com.getAuthorIdent().getEmailAddress());
        assertEquals(ObjectId.toString(com.getId()), info.getRevision());
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertTrue(modifiedFiles.get(toCommit).getStatus().equals(Status.ADDED));
    }

    public void testMultipleFileCommit () throws Exception {
        repository.getConfig().setString("user", null, "name", "John");
        repository.getConfig().setString("user", null, "email", "john@git.com");
        repository.getConfig().save();

        File dir = new File(workDir, "testdir");
        File newOne = new File(dir, "test.txt");
        File another = new File(dir, "test2.txt");
        dir.mkdirs();
        write(newOne, "this is test!");
        write(another, "this is another test!");

        GitClient client = getClient(workDir);
        client.add(new File[] { newOne, another }, NULL_PROGRESS_MONITOR);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { newOne, another }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        GitRevisionInfo info = client.commit(new File[] { newOne, another }, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { newOne, another }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertTrue(modifiedFiles.get(newOne).getStatus().equals(Status.ADDED));
        assertTrue(modifiedFiles.get(another).getStatus().equals(Status.ADDED));

        Git git = new Git(repository);
        LogCommand log = git.log();
        RevCommit com = log.call().iterator().next();
        assertEquals("initial commit", com.getFullMessage());
        assertEquals( "john@git.com", com.getAuthorIdent().getEmailAddress());

        write(newOne, "!modification!");
        write(another, "another modification!");

        client.add(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(new File[] { newOne, another }, "second commit", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        modifiedFiles = info.getModifiedFiles();
        assertTrue(modifiedFiles.get(newOne).getStatus().equals(Status.MODIFIED));
        assertTrue(modifiedFiles.get(another).getStatus().equals(Status.MODIFIED));

        log = git.log();
        com = log.call().iterator().next();
        assertEquals("second commit", com.getFullMessage());
        assertEquals( "john@git.com", com.getAuthorIdent().getEmailAddress());
    }

    public void testCommitOnlySomeOfAllFiles () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "file1 content");
        File file2 = new File(workDir, "file2");
        write(file2, "file2 content");
        File[] files = new File[] { file1, file2 };
        GitClient client = getClient(workDir);
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(new File[] { file1 }, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.ADDED));
        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        // file1 should be up to date
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        // but file2 should still be staged for commit
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        info = client.commit(new File[] { file2 }, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file2).getStatus().equals(Status.ADDED));

        write(file1, "file1 content changed");
        write(file2, "file2 content changed");
        client.add(new File[] { file1 }, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(new File[] { file1 }, "change in content", null, null, NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.MODIFIED));
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        // file1 should be up to date
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        // but file2 was not staged
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);

        write(file1, "file1 content changed again");
        write(file2, "file2 content changed again");
        client.add(new File[] { file1, file2 }, NULL_PROGRESS_MONITOR);
        write(file2, "file2 content changed again and again");
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(new File[] { file1 }, "another change in content", null, null, NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.MODIFIED));
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        // file1 should be up to date
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        // but file2 should still be staged for commit
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);

        write(file1, "file1 content changed again and again");
        client.add(new File[] { file1 }, NULL_PROGRESS_MONITOR);
        client.remove(new File[] { file2 }, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(new File[] { file1 }, "another change in content", null, null,  NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.MODIFIED));
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        // file1 should be up to date
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        // but file2 should still be staged for commit
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_MODIFIED, false);
    }

    public void testCommitRemoval () throws Exception {
        File file = new File(workDir, "file");
        write(file, "file1 content");
        File[] files = new File[] { file };
        GitClient client = getClient(workDir);
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file).getStatus().equals(Status.ADDED));

        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        file.delete();
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_REMOVED, false);

        // commit should remove file from the repository
        client.remove(files, false, NULL_PROGRESS_MONITOR);
        info = client.commit(files, "deleting file", null, null, NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file).getStatus().equals(Status.REMOVED));
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertNull(statuses.get(file));
    }

    public void testSingleTreeCommit () throws Exception {
        File folder = new File(workDir, "folder");
        File subfolder1 = new File(folder, "subfolder");
        File subfolder11 = new File(subfolder1, "subfolder1");
        File subfolder12 = new File(subfolder1, "subfolder2");
        subfolder11.mkdirs();
        subfolder12.mkdirs();
        File file1 = new File(subfolder11, "file1");
        File file2 = new File(subfolder12, "file2");
        write(file1, "file1 content");
        write(file2, "file2 content");
        File[] files = new File[] { folder };
        GitClient client = getClient(workDir);
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(2, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.ADDED));
        assertTrue(modifiedFiles.get(file2).getStatus().equals(Status.ADDED));

        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Git git = new Git(repository);
        LogCommand log = git.log();
        RevCommit com = log.call().iterator().next();
        assertEquals("initial commit", com.getFullMessage());
    }

    public void testMultipleTreesCommit () throws Exception {
        File folder1 = new File(workDir, "folder1");
        File subfolder11 = new File(folder1, "subfolder1");
        File subfolder12 = new File(folder1, "subfolder2");
        subfolder11.mkdirs();
        subfolder12.mkdirs();
        File file11 = new File(subfolder11, "file1");
        File file12 = new File(subfolder12, "file2");
        write(file11, "file1 content");
        write(file12, "file2 content");
        File folder2 = new File(workDir, "folder2");
        File subfolder21 = new File(folder2, "subfolder1");
        File subfolder22 = new File(folder2, "subfolder2");
        subfolder21.mkdirs();
        subfolder22.mkdirs();
        File file21 = new File(subfolder21, "file1");
        File file22 = new File(subfolder22, "file2");
        write(file21, "file1 content");
        write(file22, "file2 content");
        File[] files = new File[] { folder1, folder2 };
        GitClient client = getClient(workDir);
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(4, modifiedFiles.size());

        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Git git = new Git(repository);
        LogCommand log = git.log();
        RevCommit com = log.call().iterator().next();
        assertEquals("initial commit", com.getFullMessage());

        write(file11, "!modification!");
        write(file12, "another modification!");
        write(file21, "!modification!");
        write(file22, "another modification!");

        client.add(files, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        client.commit(files, "second commit", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        log = git.log();
        com = log.call().iterator().next();
        assertEquals("second commit", com.getFullMessage());
    }

    public void testCommitOnlySomeOfAllFilesFromMultipleTrees () throws Exception {
        File folder1 = new File(workDir, "folder1");
        File subfolder11 = new File(folder1, "subfolder1");
        File subfolder12 = new File(folder1, "subfolder2");
        subfolder11.mkdirs();
        subfolder12.mkdirs();
        File file11 = new File(subfolder11, "file1");
        File file12 = new File(subfolder12, "file2");
        write(file11, "file1 content");
        write(file12, "file2 content");
        File folder2 = new File(workDir, "folder2");
        File subfolder21 = new File(folder2, "subfolder1");
        File subfolder22 = new File(folder2, "subfolder2");
        subfolder21.mkdirs();
        subfolder22.mkdirs();
        File file21 = new File(subfolder21, "file1");
        File file22 = new File(subfolder22, "file2");
        write(file21, "file1 content");
        write(file22, "file2 content");
        File[] trees = new File[] { folder1, folder2 };
        File[] filesToCommit = new File[] { folder1, subfolder21 };
        File[] filesSingleFolder = new File[] { subfolder21 };
        GitClient client = getClient(workDir);
        client.add(trees, NULL_PROGRESS_MONITOR);

        // COMMIT SOME
        GitRevisionInfo info = client.commit(filesSingleFolder, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        Map<File, GitStatus> statuses = client.getStatus(trees, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        Git git = new Git(repository);
        LogCommand log = git.log();
        RevCommit com = log.call().iterator().next();
        assertEquals("initial commit", com.getFullMessage());

        // COMMIT ALL
        info = client.commit(trees, "commit all", null, null, NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(3, modifiedFiles.size());
        statuses = client.getStatus(trees, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        write(file11, "!modification!");
        write(file12, "another modification!");
        write(file21, "!modification!");
        write(file22, "another modification!");

        client.add(trees, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(trees, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(filesToCommit, "second commit", null, null, NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(3, modifiedFiles.size());
        statuses = client.getStatus(trees, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);

        log = git.log();
        com = log.call().iterator().next();
        assertEquals("second commit", com.getFullMessage());

        // COMMIT ALL
        client.commit(trees, "commit all", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(trees, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
    }

    public void testCommitRemovalTree () throws Exception {
        File folder = new File(workDir, "folder");
        File subfolder1 = new File(folder, "subfolder");
        File subfolder11 = new File(subfolder1, "subfolder1");
        File subfolder12 = new File(subfolder1, "subfolder2");
        subfolder11.mkdirs();
        subfolder12.mkdirs();
        File file1 = new File(subfolder11, "file1");
        File file2 = new File(subfolder12, "file2");
        write(file1, "file1 content");
        write(file2, "file2 content");
        File[] files = new File[] { folder };
        GitClient client = getClient(workDir);
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<File, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(2, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.ADDED));
        assertTrue(modifiedFiles.get(file2).getStatus().equals(Status.ADDED));

        Map<File, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        client.remove(files, false, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);

        // commit should remove file from the repository
        info = client.commit(files, "deleting files", null, null, NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(2, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.REMOVED));
        assertTrue(modifiedFiles.get(file2).getStatus().equals(Status.REMOVED));
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertNull(statuses.get(file1));
        assertNull(statuses.get(file2));
    }

    public void testNeverCommitConflicts () throws Exception {
        File f = new File(workDir, "conflict");
        DirCache cache = repository.lockDirCache();
        try {
            DirCacheBuilder builder = cache.builder();
            DirCacheEntry e = new DirCacheEntry(f.getName(), 1);
            e.setLength(0);
            e.setLastModified(Instant.ofEpochMilli(0));
            e.setFileMode(FileMode.REGULAR_FILE);
            builder.add(e);
            builder.finish();
            builder.commit();
        } finally {
            cache.unlock();
        }
        File mergeFile = new File(new File(workDir, Constants.DOT_GIT), "MERGE_HEAD");
        mergeFile.createNewFile();
        try {
            getClient(workDir).commit(new File[] { f }, "nothing", null, null, NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException ex) {
            assertEquals("Index contains files in conflict, please resolve them before commit", ex.getMessage());
        }
        cache = repository.lockDirCache();
        try {
            DirCacheEditor edit = cache.editor();
            edit.add(new DirCacheEditor.DeletePath(f.getName()));
            edit.finish();
            edit.commit();
        } finally {
            cache.unlock();
        }
        try {
            getClient(workDir).commit(new File[] { f }, "nothing", null, null, NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException ex) {
            assertEquals("Cannot do a partial commit during a merge.", ex.getMessage());
        }

        //TODO try to commit the whole WT, for that we need to create the real conflict, not just this fake ones
    }
    
    public void testLineEndingsWindows () throws Exception {
        if (!isWindows()) {
            return;
        }
        // lets turn autocrlf on
        StoredConfig cfg = repository.getConfig();
        cfg.setString(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOCRLF, "true");
        cfg.save();
        
        File f = new File(workDir, "f");
        write(f, "a\r\nb\r\n");
        File[] roots = new File[] { f };
        
        GitClient client = getClient(workDir);
        client.add(roots, NULL_PROGRESS_MONITOR);
        DirCacheEntry e1 = repository.readDirCache().getEntry("f");
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR),
                workDir, f, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        List<String> res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(Arrays.asList("A  f"), res);
        GitRevisionInfo info = client.commit(roots, "aaa", null, null, NULL_PROGRESS_MONITOR);
        
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR),
                workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(0, res.size());
        
        RevCommit commit = Utils.findCommit(repository, info.getRevision());
        TreeWalk walk = new TreeWalk(repository);
        walk.reset();
        walk.addTree(commit.getTree());
        walk.setFilter(PathFilter.create("f"));
        walk.setRecursive(true);
        walk.next();
        assertEquals("f", walk.getPathString());
        ObjectLoader loader = repository.getObjectDatabase().open(walk.getObjectId(0));
        assertEquals(4, loader.getSize());
        assertEquals("a\nb\n", new String(loader.getBytes()));
        assertEquals(e1.getObjectId(), walk.getObjectId(0));
        
        
        File f2 = new File(workDir, "f2");
        write(f2, "a\r\nb\r\n");
        roots = new File[] { f2 };
        
        client.add(roots, NULL_PROGRESS_MONITOR);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR),
                workDir, f2, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(Arrays.asList("A  f2"), res);
        info = client.commit(roots, "bbb", null, null, NULL_PROGRESS_MONITOR);
        
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR),
                workDir, f2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(0, res.size());
        
        commit = Utils.findCommit(repository, info.getRevision());
        walk = new TreeWalk(repository);
        walk.reset();
        walk.addTree(commit.getTree());
        walk.setFilter(PathFilter.create("f"));
        walk.setRecursive(true);
        while(walk.next()) {
            loader = repository.getObjectDatabase().open(walk.getObjectId(0));
            assertEquals(4, loader.getSize());
            assertEquals("a\nb\n", new String(loader.getBytes()));
            assertEquals(e1.getObjectId(), walk.getObjectId(0));
        }
    }
    
    public void testAmendCommit () throws Exception {
        repository.getConfig().setString("user", null, "name", "John");
        repository.getConfig().setString("user", null, "email", "john@git.com");
        repository.getConfig().save();

        File dir = new File(workDir, "testdir");
        File newOne = new File(dir, "test.txt");
        File another = new File(dir, "test2.txt");
        dir.mkdirs();
        write(newOne, "content1");
        write(another, "content2");

        GitClient client = getClient(workDir);
        client.add(new File[] { newOne, another }, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(new File[] { newOne, another }, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { newOne, another }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        
        write(newOne, "modification1");
        write(another, "modification2");

        client.add(new File[] { newOne, another }, NULL_PROGRESS_MONITOR);
        GitRevisionInfo lastCommit = client.commit(new File[] { newOne }, "second commit", null, null, false, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        Map<File, GitFileInfo> modifiedFiles = lastCommit.getModifiedFiles();
        assertTrue(modifiedFiles.get(newOne).getStatus().equals(Status.MODIFIED));
        assertNull(modifiedFiles.get(another));

        assertEquals(1, lastCommit.getParents().length);
        assertEquals(info.getRevision(), lastCommit.getParents()[0]);
        assertEquals(lastCommit.getRevision(), client.getBranches(false, NULL_PROGRESS_MONITOR).get("master").getId());
        
        Thread.sleep(1100);
        
        long time = lastCommit.getCommitTime();
        RevWalk walk = new RevWalk(repository);
        RevCommit originalCommit = walk.parseCommit(repository.resolve(lastCommit.getRevision()));
        lastCommit = client.commit(new File[] { newOne, another }, "second commit, modified message",
                new GitUser("user2", "user2.email"), new GitUser("committer2", "committer2.email"), true, NULL_PROGRESS_MONITOR);
        RevCommit amendedCommit = walk.parseCommit(repository.resolve(lastCommit.getRevision()));
        assertEquals("Commit time should not change after amend", time, lastCommit.getCommitTime());
        assertEquals(originalCommit.getAuthorIdent().getWhen(), amendedCommit.getAuthorIdent().getWhen());
        // commit time should not equal.
        assertFalse(originalCommit.getCommitterIdent().getWhen().equals(amendedCommit.getCommitterIdent().getWhen()));
        statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        modifiedFiles = lastCommit.getModifiedFiles();
        assertTrue(modifiedFiles.get(newOne).getStatus().equals(Status.MODIFIED));
        assertTrue(modifiedFiles.get(another).getStatus().equals(Status.MODIFIED));

        assertEquals(1, lastCommit.getParents().length);
        assertEquals(info.getRevision(), lastCommit.getParents()[0]);
        assertEquals(lastCommit.getRevision(), client.getBranches(false, NULL_PROGRESS_MONITOR).get("master").getId());
    }
    
    public void testCherryPickCommit () throws Exception {
        repository.getConfig().setString("user", null, "name", "John");
        repository.getConfig().setString("user", null, "email", "john@git.com");
        repository.getConfig().save();
        
        File f = new File(workDir, "f");
        write(f, "init");
        File[] files = new File[] { f };
        
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        write(f, "change");
        add(f);
        GitRevisionInfo info = client.commit(files, "change to CherryPick", null, null, NULL_PROGRESS_MONITOR);
        
        Thread.sleep(1100);
        
        client.reset("HEAD~1", GitClient.ResetType.MIXED, NULL_PROGRESS_MONITOR);
        repository.writeCherryPickHead(repository.resolve(info.getRevision()));
        
        // now we are cherry-picking
        // amend is not allowed
        try {
            client.commit(new File[0], info.getFullMessage(), null, null, true, NULL_PROGRESS_MONITOR);
            fail("Amend not allowed");
        } catch (GitException ex) {
            assertEquals(Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_CannotAmend"), ex.getMessage());
        }
        
        // doing commit should preserve authorship of the original commit (info)
        GitRevisionInfo commit = client.commit(new File[0], info.getFullMessage(), null, null, NULL_PROGRESS_MONITOR);
        assertEquals(info.getAuthor(), commit.getAuthor());
        assertEquals(info.getCommitTime(), commit.getCommitTime());
        assertEquals(Utils.findCommit(repository, info.getRevision()).getAuthorIdent().getWhen(),
                Utils.findCommit(repository, commit.getRevision()).getAuthorIdent().getWhen());
    }
}
