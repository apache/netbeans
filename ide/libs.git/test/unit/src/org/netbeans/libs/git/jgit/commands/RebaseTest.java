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
import java.util.Map;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitClient.RebaseOperationType;
import org.netbeans.libs.git.GitRebaseResult;
import org.netbeans.libs.git.GitRebaseResult.RebaseStatus;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class RebaseTest extends AbstractGitTestCase {
    private File workDir;
    private static final String BRANCH_NAME = "new_branch";
    private Repository repo;

    public RebaseTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repo = getRepository(getLocalGitRepository());
    }
    
    public void testRebaseOperationMapping () {
        for (RebaseOperationType operation : RebaseOperationType.values()) {
            assertNotNull(RebaseCommand.getOperation(operation));
        }
    }

    public void testRebaseNoChange () throws Exception {
        File f = new File(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        String head = getRepository(client).resolve(Constants.HEAD).getName();
        GitBranch branch = client.createBranch(BRANCH_NAME, Constants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        
        // rebase branch to master
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.UP_TO_DATE, result.getRebaseStatus());
        assertEquals(head, result.getCurrentHead());
        
        // do a commit and 
        write(f, "change");
        add(f);
        GitRevisionInfo commit = client.commit(new File[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        // rebase branch to master, they are now different but still no rebase is needed
        result = client.rebase(RebaseOperationType.BEGIN, Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.UP_TO_DATE, result.getRebaseStatus());
        assertEquals(commit.getRevision(), result.getCurrentHead());
    }
    
    public void testRebaseSimple () throws Exception {
        File f = new File(workDir, "file");
        File f2 = new File(workDir, "file2");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        write(f2, Constants.MASTER);
        add(f2);
        GitRevisionInfo master = client.commit(new File[] { f2 }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.createBranch(BRANCH_NAME, "master^1", NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        assertFalse(f2.exists());
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo info = client.commit(new File[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        write(f, "another change");
        add(f);
        GitRevisionInfo info2 = client.commit(new File[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        // sleep to change time
        Thread.sleep(1100);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.OK, result.getRebaseStatus());
        assertEquals("another change", read(f));
        assertEquals(Constants.MASTER, read(f2));
        
        SearchCriteria crit = new SearchCriteria();
        crit.setRevisionFrom(Constants.MASTER);
        crit.setRevisionTo(result.getCurrentHead());
        GitRevisionInfo[] logs = client.log(crit, NULL_PROGRESS_MONITOR);
        assertEquals(3, logs.length);
        assertFalse(info2.getRevision().equals(logs[0].getRevision()));
        assertEquals(info2.getCommitTime(), logs[0].getCommitTime());
        assertFalse(info.getRevision().equals(logs[1].getRevision()));
        assertEquals(info.getCommitTime(), logs[1].getCommitTime());
        assertEquals(master.getRevision(), logs[2].getRevision());
    }
    
    public void testConflicts () throws Exception {
        File f = new File(workDir, "file");
        File f2 = new File(workDir, "file2");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, Constants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, Constants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new File[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        write(f2, BRANCH_NAME);
        add(f2);
        GitRevisionInfo branchInfo = client.commit(new File[] { f, f2 }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.STOPPED, result.getRebaseStatus());
        assertEquals("<<<<<<< Upstream, based on master\nmaster\n=======\nnew_branch\n>>>>>>> " 
                + branchInfo.getRevision().substring(0, 7) + " " + branchInfo.getShortMessage(), read(f));
        assertEquals(Arrays.asList(f), result.getConflicts());
        Map<File, GitStatus> statuses = client.getStatus(new File[] { f, f2 }, NULL_PROGRESS_MONITOR);
        assertTrue(statuses.get(f).isConflict());
        assertStatus(statuses, workDir, f2, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertEquals(branchInfo.getRevision(), result.getCurrentCommit());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
    }
    
    public void testRebaseAbort () throws Exception {
        File f = new File(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, Constants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, Constants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new File[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo branchInfo = client.commit(new File[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.STOPPED, result.getRebaseStatus());
        assertEquals(Arrays.asList(f), result.getConflicts());
        assertEquals(branchInfo.getRevision(), result.getCurrentCommit());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        
        result = client.rebase(RebaseOperationType.ABORT, null, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.ABORTED, result.getRebaseStatus());
        assertTrue(result.getConflicts().isEmpty());
        // resets to original state
        assertEquals(branchInfo.getRevision(), result.getCurrentHead());
        assertEquals(branchInfo.getRevision(), getRepository(client).resolve(BRANCH_NAME).name());
        assertEquals(masterInfo.getRevision(), getRepository(client).resolve(Constants.MASTER).name());
    }
    
    public void testRebaseSkip () throws Exception {
        File f = new File(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, Constants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, Constants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new File[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo branchInfo = client.commit(new File[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.STOPPED, result.getRebaseStatus());
        assertEquals(Arrays.asList(f), result.getConflicts());
        assertEquals(branchInfo.getRevision(), result.getCurrentCommit());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        
        result = client.rebase(RebaseOperationType.SKIP, null, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.OK, result.getRebaseStatus());
        assertTrue(result.getConflicts().isEmpty());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        assertEquals(masterInfo.getRevision(), getRepository(client).resolve(BRANCH_NAME).name());
        assertEquals(masterInfo.getRevision(), getRepository(client).resolve(Constants.MASTER).name());
    }
    
    public void testResolveConflictsNoCommit () throws Exception {
        File f = new File(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, Constants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, Constants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new File[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo branchInfo = client.commit(new File[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.STOPPED, result.getRebaseStatus());
        assertEquals("<<<<<<< Upstream, based on master\nmaster\n=======\nnew_branch\n>>>>>>> " 
                + branchInfo.getRevision().substring(0, 7) + " " + branchInfo.getShortMessage(), read(f));
        assertEquals(Arrays.asList(f), result.getConflicts());
        Map<File, GitStatus> statuses = client.getStatus(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(statuses.get(f).isConflict());
        assertEquals(branchInfo.getRevision(), result.getCurrentCommit());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        write(f, Constants.MASTER);
        add(f);
        
        result = client.rebase(RebaseOperationType.CONTINUE, null, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.NOTHING_TO_COMMIT, result.getRebaseStatus());
        assertTrue(result.getConflicts().isEmpty());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        assertEquals(branchInfo.getRevision(), getRepository(client).resolve(BRANCH_NAME).name());
        assertEquals(masterInfo.getRevision(), getRepository(client).resolve(Constants.MASTER).name());
        
        // still have to finish rebase
        result = client.rebase(RebaseOperationType.SKIP, null, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.OK, result.getRebaseStatus());
        assertTrue(result.getConflicts().isEmpty());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        assertEquals(masterInfo.getRevision(), getRepository(client).resolve(BRANCH_NAME).name());
        assertEquals(masterInfo.getRevision(), getRepository(client).resolve(Constants.MASTER).name());
    }
    
    public void testResolveConflicts () throws Exception {
        File f = new File(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, Constants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, Constants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new File[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo branchInfo = client.commit(new File[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.STOPPED, result.getRebaseStatus());
        assertEquals(Arrays.asList(f), result.getConflicts());
        assertEquals(branchInfo.getRevision(), result.getCurrentCommit());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        
        write(f, "resolving conflict");
        add(f);
        
        result = client.rebase(RebaseOperationType.CONTINUE, null, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.OK, result.getRebaseStatus());
        assertEquals(getRepository(client).resolve(Constants.HEAD).name(), result.getCurrentHead());
        assertEquals(getRepository(client).resolve(Constants.HEAD).name(), getRepository(client).resolve(BRANCH_NAME).name());
        assertEquals(masterInfo.getRevision(), getRepository(client).resolve(Constants.MASTER).name());
    }
    
    public void testRebaseFailCheckoutLocalChanges () throws Exception {
        File f = new File(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, Constants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, Constants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new File[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo branchInfo = client.commit(new File[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        write(f, "local change");
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.FAILED, result.getRebaseStatus());
        assertEquals(Arrays.asList(f), result.getFailures());
        assertEquals(branchInfo.getRevision(), getRepository(client).resolve(BRANCH_NAME).name());
        assertEquals(masterInfo.getRevision(), getRepository(client).resolve(Constants.MASTER).name());
        // resets HEAD
        assertEquals(branchInfo.getRevision(), getRepository(client).resolve(Constants.HEAD).name());
    }
    
    public void testRebaseFailMergeLocalChanges () throws Exception {
        File f = new File(workDir, "file");
        File f2 = new File(workDir, "file2");
        write(f, "init");
        write(f2, "init");
        add(f, f2);
        commit(f, f2);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, Constants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, Constants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new File[] { f, f2 }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo branchInfo = client.commit(new File[] { f, f2 }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        write(f2, "local change");
        add(f2);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.FAILED, result.getRebaseStatus());
        assertEquals(Arrays.asList(f2), result.getFailures());
        assertEquals(branchInfo.getRevision(), getRepository(client).resolve(BRANCH_NAME).name());
        assertEquals(masterInfo.getRevision(), getRepository(client).resolve(Constants.MASTER).name());
        // resets HEAD
        assertEquals(branchInfo.getRevision(), getRepository(client).resolve(Constants.HEAD).name());
    }
}
