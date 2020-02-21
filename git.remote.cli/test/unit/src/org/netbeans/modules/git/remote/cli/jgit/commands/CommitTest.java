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
import java.util.List;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo.GitFileInfo;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo.GitFileInfo.Status;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.GitUser;
import org.netbeans.modules.git.remote.cli.SearchCriteria;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitConfig;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class CommitTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;
    protected static final boolean KIT = CommitCommand.KIT;

    public CommitTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testCommitOnlySomeOfAllFiles","testAmendCommit","testCherryPickCommit").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testCommitNoRoots () throws Exception {
        VCSFileProxy toCommit = VCSFileProxy.createFileProxy(workDir, "testnotadd.txt");
        write(toCommit, "blablabla");
        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { toCommit }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        client.add(new VCSFileProxy[] { toCommit }, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new VCSFileProxy[] { toCommit }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        GitRevisionInfo info = client.commit(new VCSFileProxy[0], "initial commit", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new VCSFileProxy[] { toCommit }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        final SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setLimit(1);
        GitRevisionInfo[] log = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        GitRevisionInfo com = log[0];
        
        
if(KIT) assertEquals("initial commit", info.getFullMessage());
else    assertEquals("initial commit\n", info.getFullMessage());
if(KIT) assertEquals("initial commit", com.getFullMessage());
else    assertEquals("initial commit\n", com.getFullMessage());
        assertEquals(com.getRevision(), info.getRevision());
        Map<VCSFileProxy, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertTrue(modifiedFiles.get(toCommit).getStatus().equals(Status.ADDED));
    }

    public void testSingleFileCommit () throws Exception {
        repository.getConfig().setString("user", null, "name", "John");
        repository.getConfig().setString("user", null, "email", "");
        repository.getConfig().save();

        VCSFileProxy toCommit = VCSFileProxy.createFileProxy(workDir, "testnotadd.txt");
        write(toCommit, "blablabla");
        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { toCommit }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        client.add(new VCSFileProxy[] { toCommit }, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new VCSFileProxy[] { toCommit }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        long t1 = System.currentTimeMillis();
        Thread.sleep(1000);
        GitRevisionInfo info = client.commit(new VCSFileProxy[] { toCommit }, "initial commit", null, null,  NULL_PROGRESS_MONITOR);
        Thread.sleep(1000);
        long t2 = System.currentTimeMillis();
        statuses = client.getStatus(new VCSFileProxy[] { toCommit }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, toCommit, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertTrue(t1 <= info.getCommitTime() && t2 >= info.getCommitTime());

        final SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setLimit(1);
        GitRevisionInfo[] log = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        GitRevisionInfo com = log[0];
if(KIT) assertEquals("initial commit", info.getFullMessage());
else    assertEquals("initial commit\n", info.getFullMessage());
if(KIT) assertEquals("initial commit", com.getFullMessage());
else    assertEquals("initial commit\n", com.getFullMessage());
        assertEquals( "", info.getAuthor().getEmailAddress());
        assertEquals( "", com.getAuthor().getEmailAddress());
        assertEquals(com.getRevision(), info.getRevision());
        Map<VCSFileProxy, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertTrue(modifiedFiles.get(toCommit).getStatus().equals(Status.ADDED));
    }

    public void testMultipleFileCommit () throws Exception {
        repository.getConfig().setString("user", null, "name", "John");
        repository.getConfig().setString("user", null, "email", "");
        repository.getConfig().save();

        VCSFileProxy dir = VCSFileProxy.createFileProxy(workDir, "testdir");
        VCSFileProxy newOne = VCSFileProxy.createFileProxy(dir, "test.txt");
        VCSFileProxy another = VCSFileProxy.createFileProxy(dir, "test2.txt");
        VCSFileProxySupport.mkdirs(dir);
        write(newOne, "this is test!");
        write(another, "this is another test!");

        GitClient client = getClient(workDir);
        client.add(new VCSFileProxy[] { newOne, another }, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { newOne, another }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        GitRevisionInfo info = client.commit(new VCSFileProxy[] { newOne, another }, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new VCSFileProxy[] { newOne, another }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        Map<VCSFileProxy, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertTrue(modifiedFiles.get(newOne).getStatus().equals(Status.ADDED));
        assertTrue(modifiedFiles.get(another).getStatus().equals(Status.ADDED));

        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setLimit(1);
        GitRevisionInfo[] log = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        GitRevisionInfo com = log[0];
if(KIT) assertEquals("initial commit", com.getFullMessage());
else    assertEquals("initial commit\n", com.getFullMessage());
        assertEquals( "", com.getAuthor().getEmailAddress());

        write(newOne, "!modification!");
        write(another, "another modification!");

        client.add(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(new VCSFileProxy[] { newOne, another }, "second commit", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        modifiedFiles = info.getModifiedFiles();
        assertTrue(modifiedFiles.get(newOne).getStatus().equals(Status.MODIFIED));
        assertTrue(modifiedFiles.get(another).getStatus().equals(Status.MODIFIED));

        searchCriteria = new SearchCriteria();
        searchCriteria.setLimit(1);
        log = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        com = log[0];
if(KIT) assertEquals("second commit", com.getFullMessage());
else    assertEquals("second commit\n", com.getFullMessage());
        assertEquals( "", com.getAuthor().getEmailAddress());
    }

    public void testCommitOnlySomeOfAllFiles () throws Exception {
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "file1");
        write(file1, "file1 content");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2");
        write(file2, "file2 content");
        VCSFileProxy[] files = new VCSFileProxy[] { file1, file2 };
        GitClient client = getClient(workDir);
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(new VCSFileProxy[] { file1 }, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.ADDED));
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        // file1 should be up to date
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        // but file2 should still be staged for commit
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        info = client.commit(new VCSFileProxy[] { file2 }, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file2).getStatus().equals(Status.ADDED));

        write(file1, "file1 content changed");
        write(file2, "file2 content changed");
        client.add(new VCSFileProxy[] { file1 }, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(new VCSFileProxy[] { file1 }, "change in content", null, null, NULL_PROGRESS_MONITOR);
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
        client.add(new VCSFileProxy[] { file1, file2 }, NULL_PROGRESS_MONITOR);
        write(file2, "file2 content changed again and again");
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(new VCSFileProxy[] { file1 }, "another change in content", null, null, NULL_PROGRESS_MONITOR);
        modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.MODIFIED));
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        // file1 should be up to date
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        // but file2 should still be staged for commit
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);

        write(file1, "file1 content changed again and again");
        client.add(new VCSFileProxy[] { file1 }, NULL_PROGRESS_MONITOR);
        client.remove(new VCSFileProxy[] { file2 }, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_MODIFIED, false);
        info = client.commit(new VCSFileProxy[] { file1 }, "another change in content", null, null,  NULL_PROGRESS_MONITOR);
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
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        write(file, "file1 content");
        VCSFileProxy[] files = new VCSFileProxy[] { file };
        GitClient client = getClient(workDir);
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file).getStatus().equals(Status.ADDED));

        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        VCSFileProxySupport.delete(file);
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
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxy subfolder1 = VCSFileProxy.createFileProxy(folder, "subfolder");
        VCSFileProxy subfolder11 = VCSFileProxy.createFileProxy(subfolder1, "subfolder1");
        VCSFileProxy subfolder12 = VCSFileProxy.createFileProxy(subfolder1, "subfolder2");
        VCSFileProxySupport.mkdirs(subfolder11);
        VCSFileProxySupport.mkdirs(subfolder12);
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(subfolder11, "file1");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(subfolder12, "file2");
        write(file1, "file1 content");
        write(file2, "file2 content");
        VCSFileProxy[] files = new VCSFileProxy[] { folder };
        GitClient client = getClient(workDir);
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(2, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.ADDED));
        assertTrue(modifiedFiles.get(file2).getStatus().equals(Status.ADDED));

        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setLimit(1);
        GitRevisionInfo[] log = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        GitRevisionInfo com = log[0];
if(KIT) assertEquals("initial commit", com.getFullMessage());
else    assertEquals("initial commit\n", com.getFullMessage());
    }

    public void testMultipleTreesCommit () throws Exception {
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(workDir, "folder1");
        VCSFileProxy subfolder11 = VCSFileProxy.createFileProxy(folder1, "subfolder1");
        VCSFileProxy subfolder12 = VCSFileProxy.createFileProxy(folder1, "subfolder2");
        VCSFileProxySupport.mkdirs(subfolder11);
        VCSFileProxySupport.mkdirs(subfolder12);
        VCSFileProxy file11 = VCSFileProxy.createFileProxy(subfolder11, "file1");
        VCSFileProxy file12 = VCSFileProxy.createFileProxy(subfolder12, "file2");
        write(file11, "file1 content");
        write(file12, "file2 content");
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(workDir, "folder2");
        VCSFileProxy subfolder21 = VCSFileProxy.createFileProxy(folder2, "subfolder1");
        VCSFileProxy subfolder22 = VCSFileProxy.createFileProxy(folder2, "subfolder2");
        VCSFileProxySupport.mkdirs(subfolder21);
        VCSFileProxySupport.mkdirs(subfolder22);
        VCSFileProxy file21 = VCSFileProxy.createFileProxy(subfolder21, "file1");
        VCSFileProxy file22 = VCSFileProxy.createFileProxy(subfolder22, "file2");
        write(file21, "file1 content");
        write(file22, "file2 content");
        VCSFileProxy[] files = new VCSFileProxy[] { folder1, folder2 };
        GitClient client = getClient(workDir);
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(4, modifiedFiles.size());

        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setLimit(1);
        GitRevisionInfo[] log = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        GitRevisionInfo com = log[0];
if(KIT) assertEquals("initial commit", com.getFullMessage());
else    assertEquals("initial commit\n", com.getFullMessage());

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

        searchCriteria = new SearchCriteria();
        searchCriteria.setLimit(1);
        log = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        com = log[0];
if(KIT) assertEquals("second commit", com.getFullMessage());
else    assertEquals("second commit\n", com.getFullMessage());
    }

    public void testCommitOnlySomeOfAllFilesFromMultipleTrees () throws Exception {
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(workDir, "folder1");
        VCSFileProxy subfolder11 = VCSFileProxy.createFileProxy(folder1, "subfolder1");
        VCSFileProxy subfolder12 = VCSFileProxy.createFileProxy(folder1, "subfolder2");
        VCSFileProxySupport.mkdirs(subfolder11);
        VCSFileProxySupport.mkdirs(subfolder12);
        VCSFileProxy file11 = VCSFileProxy.createFileProxy(subfolder11, "file1");
        VCSFileProxy file12 = VCSFileProxy.createFileProxy(subfolder12, "file2");
        write(file11, "file1 content");
        write(file12, "file2 content");
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(workDir, "folder2");
        VCSFileProxy subfolder21 = VCSFileProxy.createFileProxy(folder2, "subfolder1");
        VCSFileProxy subfolder22 = VCSFileProxy.createFileProxy(folder2, "subfolder2");
        VCSFileProxySupport.mkdirs(subfolder21);
        VCSFileProxySupport.mkdirs(subfolder22);
        VCSFileProxy file21 = VCSFileProxy.createFileProxy(subfolder21, "file1");
        VCSFileProxy file22 = VCSFileProxy.createFileProxy(subfolder22, "file2");
        write(file21, "file1 content");
        write(file22, "file2 content");
        VCSFileProxy[] trees = new VCSFileProxy[] { folder1, folder2 };
        VCSFileProxy[] filesToCommit = new VCSFileProxy[] { folder1, subfolder21 };
        VCSFileProxy[] filesSingleFolder = new VCSFileProxy[] { subfolder21 };
        GitClient client = getClient(workDir);
        client.add(trees, NULL_PROGRESS_MONITOR);

        // COMMIT SOME
        GitRevisionInfo info = client.commit(filesSingleFolder, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(1, modifiedFiles.size());
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(trees, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setLimit(1);
        GitRevisionInfo[] log = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        GitRevisionInfo com = log[0];
if(KIT) assertEquals("initial commit", com.getFullMessage());
else    assertEquals("initial commit\n", com.getFullMessage());
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

        searchCriteria = new SearchCriteria();
        searchCriteria.setLimit(1);
        log = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        com = log[0];
if(KIT) assertEquals("second commit", com.getFullMessage());
else    assertEquals("second commit\n", com.getFullMessage());

        // COMMIT ALL
        client.commit(trees, "commit all", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(trees, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file12, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file22, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
    }

    public void testCommitRemovalTree () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxy subfolder1 = VCSFileProxy.createFileProxy(folder, "subfolder");
        VCSFileProxy subfolder11 = VCSFileProxy.createFileProxy(subfolder1, "subfolder1");
        VCSFileProxy subfolder12 = VCSFileProxy.createFileProxy(subfolder1, "subfolder2");
        VCSFileProxySupport.mkdirs(subfolder11);
        VCSFileProxySupport.mkdirs(subfolder12);
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(subfolder11, "file1");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(subfolder12, "file2");
        write(file1, "file1 content");
        write(file2, "file2 content");
        VCSFileProxy[] files = new VCSFileProxy[] { folder };
        GitClient client = getClient(workDir);
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitFileInfo> modifiedFiles = info.getModifiedFiles();
        assertEquals(2, modifiedFiles.size());
        assertTrue(modifiedFiles.get(file1).getStatus().equals(Status.ADDED));
        assertTrue(modifiedFiles.get(file2).getStatus().equals(Status.ADDED));

        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
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
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "conflict");
        VCSFileProxySupport.createNew(f);
        add(f);
        commit(f);
        VCSFileProxy mergeFile = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(workDir, GitConstants.DOT_GIT), "MERGE_HEAD");
        VCSFileProxySupport.createNew(mergeFile);
        try {
            getClient(workDir).commit(new VCSFileProxy[] { f }, "nothing", null, null, NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException ex) {
            //assertEquals("Index contains files in conflict, please resolve them before commit", ex.getMessage());
        }
        write(f, "test");
        try {
            getClient(workDir).commit(new VCSFileProxy[] { f }, "nothing", null, null, NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException ex) {
            //assertEquals("Cannot do a partial commit during a merge.", ex.getMessage());
        }

        //TODO try to commit the whole WT, for that we need to create the real conflict, not just this fake ones
    }
    
    public void testLineEndingsWindows () throws Exception {
        if (!isWindows()) {
            return;
        }
        // lets turn autocrlf on
        JGitConfig cfg = repository.getConfig();
        cfg.setString(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_AUTOCRLF, "true");
        cfg.save();
        
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "a\r\nb\r\n");
        VCSFileProxy[] roots = new VCSFileProxy[] { f };
        
        GitClient client = getClient(workDir);
        client.add(roots, NULL_PROGRESS_MONITOR);
        //DirCacheEntry e1 = repository.getRepository().readDirCache().getEntry("f");
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR),
                workDir, f, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        List<String> res = runExternally(workDir, Arrays.asList("status", "-s"));
        assertEquals(Arrays.asList("A  f"), res);
        GitRevisionInfo info = client.commit(roots, "aaa", null, null, NULL_PROGRESS_MONITOR);
        
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR),
                workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        res = runExternally(workDir, Arrays.asList("status", "-s"));
        assertEquals(0, res.size());
        
//        RevCommit commit = Utils.findCommit(repository, info.getRevision());
//        TreeWalk walk = new TreeWalk(repository.getRepository());
//        walk.reset();
//        walk.addTree(commit.getTree());
//        walk.setFilter(PathFilter.create("f"));
//        walk.setRecursive(true);
//        walk.next();
//        assertEquals("f", walk.getPathString());
//        ObjectLoader loader = repository.getRepository().getObjectDatabase().open(walk.getObjectId(0));
//        assertEquals(4, loader.getSize());
//        assertEquals("a\nb\n", new String(loader.getBytes()));
//        assertEquals(e1.getObjectId(), walk.getObjectId(0));
        
        
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "f2");
        write(f2, "a\r\nb\r\n");
        roots = new VCSFileProxy[] { f2 };
        
        client.add(roots, NULL_PROGRESS_MONITOR);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR),
                workDir, f2, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        res = runExternally(workDir, Arrays.asList("status", "-s"));
        assertEquals(Arrays.asList("A  f2"), res);
        info = client.commit(roots, "bbb", null, null, NULL_PROGRESS_MONITOR);
        
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR),
                workDir, f2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        res = runExternally(workDir, Arrays.asList("status", "-s"));
        assertEquals(0, res.size());
        
//        commit = Utils.findCommit(repository.getRepository(), info.getRevision());
//        walk = new TreeWalk(repository.getRepository());
//        walk.reset();
//        walk.addTree(commit.getTree());
//        walk.setFilter(PathFilter.create("f"));
//        walk.setRecursive(true);
//        while(walk.next()) {
//            loader = repository.getRepository().getObjectDatabase().open(walk.getObjectId(0));
//            assertEquals(4, loader.getSize());
//            assertEquals("a\nb\n", new String(loader.getBytes()));
//            assertEquals(e1.getObjectId(), walk.getObjectId(0));
//        }
    }
    
    public void testAmendCommit () throws Exception {
        repository.getConfig().setString("user", null, "name", "John");
        repository.getConfig().setString("user", null, "email", "");
        repository.getConfig().save();

        VCSFileProxy dir = VCSFileProxy.createFileProxy(workDir, "testdir");
        VCSFileProxy newOne = VCSFileProxy.createFileProxy(dir, "test.txt");
        VCSFileProxy another = VCSFileProxy.createFileProxy(dir, "test2.txt");
        VCSFileProxySupport.mkdirs(dir);
        write(newOne, "content1");
        write(another, "content2");

        GitClient client = getClient(workDir);
        client.add(new VCSFileProxy[] { newOne, another }, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(new VCSFileProxy[] { newOne, another }, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { newOne, another }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        
        write(newOne, "modification1");
        write(another, "modification2");

        client.add(new VCSFileProxy[] { newOne, another }, NULL_PROGRESS_MONITOR);
        GitRevisionInfo lastCommit = client.commit(new VCSFileProxy[] { newOne }, "second commit", null, null, false, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        Map<VCSFileProxy, GitFileInfo> modifiedFiles = lastCommit.getModifiedFiles();
        assertTrue(modifiedFiles.get(newOne).getStatus().equals(Status.MODIFIED));
        assertNull(modifiedFiles.get(another));

        assertEquals(1, lastCommit.getParents().length);
        assertEquals(info.getRevision(), lastCommit.getParents()[0]);
if (ListBranchCommand.KIT) 
        assertEquals(lastCommit.getRevision(), client.getBranches(false, NULL_PROGRESS_MONITOR).get(GitConstants.MASTER).getId());
else    assertEqualsID(lastCommit.getRevision(), client.getBranches(false, NULL_PROGRESS_MONITOR).get(GitConstants.MASTER).getId());
        
        Thread.sleep(1100);
        
        long time = lastCommit.getCommitTime();
        //RevWalk walk = new RevWalk(repository.getRepository());
        //RevCommit originalCommit = walk.parseCommit(repository.getRepository().resolve(lastCommit.getRevision()));
        lastCommit = client.commit(new VCSFileProxy[] { newOne, another }, "second commit, modified message",
                new GitUser("user2", "user2.email"), new GitUser("committer2", "committer2.email"), true, NULL_PROGRESS_MONITOR);
        //RevCommit amendedCommit = walk.parseCommit(repository.getRepository().resolve(lastCommit.getRevision()));
        assertEquals("Commit time should not change after amend", time, lastCommit.getCommitTime());
        //assertEquals(originalCommit.getAuthorIdent().getWhen(), amendedCommit.getAuthorIdent().getWhen());
        // commit time should not equal.
        //assertFalse(originalCommit.getCommitterIdent().getWhen().equals(amendedCommit.getCommitterIdent().getWhen()));
        statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, newOne, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, another, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        modifiedFiles = lastCommit.getModifiedFiles();
        assertTrue(modifiedFiles.get(newOne).getStatus().equals(Status.MODIFIED));
        assertTrue(modifiedFiles.get(another).getStatus().equals(Status.MODIFIED));

        assertEquals(1, lastCommit.getParents().length);
        assertEquals(info.getRevision(), lastCommit.getParents()[0]);
if (ListBranchCommand.KIT) 
        assertEquals(lastCommit.getRevision(), client.getBranches(false, NULL_PROGRESS_MONITOR).get(GitConstants.MASTER).getId());
else    assertEqualsID(lastCommit.getRevision(), client.getBranches(false, NULL_PROGRESS_MONITOR).get(GitConstants.MASTER).getId());
    }
    
    public void testCherryPickCommit () throws Exception {
        repository.getConfig().setString("user", null, "name", "John");
        repository.getConfig().setString("user", null, "email", "john");
        repository.getConfig().save();
        
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        write(f, "change");
        add(f);
        GitRevisionInfo info = client.commit(files, "change to CherryPick", null, null, NULL_PROGRESS_MONITOR);
        
        Thread.sleep(1100);
        
        client.reset("HEAD~1", GitClient.ResetType.MIXED, NULL_PROGRESS_MONITOR);
        //repository.getRepository().writeCherryPickHead(repository.getRepository().resolve(info.getRevision()));
        
        // now we are cherry-picking
        // amend is not allowed
        try {
            client.commit(new VCSFileProxy[0], info.getFullMessage(), null, null, true, NULL_PROGRESS_MONITOR);
            fail("Amend not allowed");
        } catch (GitException ex) {
            assertEquals(Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_CannotAmend"), ex.getMessage());
        }
        
        // doing commit should preserve authorship of the original commit (info)
        GitRevisionInfo commit = client.commit(new VCSFileProxy[0], info.getFullMessage(), null, null, NULL_PROGRESS_MONITOR);
        assertEquals(info.getAuthor(), commit.getAuthor());
        assertEquals(info.getCommitTime(), commit.getCommitTime());
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setRevisionFrom(info.getRevision());
        GitRevisionInfo[] log = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        searchCriteria = new SearchCriteria();
        searchCriteria.setRevisionFrom(commit.getRevision());
        GitRevisionInfo[] log2 = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        assertEquals(log[0].getAuthorTime(), log2[0].getAuthorTime());
    }
}
