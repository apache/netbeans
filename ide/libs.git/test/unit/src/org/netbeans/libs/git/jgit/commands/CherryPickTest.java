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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.revwalk.RevCommit;
import org.netbeans.libs.git.ApiUtils;
import org.netbeans.libs.git.GitCherryPickResult;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.jgit.Utils;

/**
 *
 * @author ondra
 */
public class CherryPickTest extends AbstractGitTestCase {

    private File workDir;
    private Repository repository;
    private static final String BRANCH = "b";
    
    public CherryPickTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }
    
    public void testCherryPickCommit () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, Constants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        write(f, "change on branch");
        add(f);
        GitRevisionInfo c = client.commit(new File[] { f }, "on branch", null, null, NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initCommit = client.log("HEAD", NULL_PROGRESS_MONITOR);
        
        Thread.sleep(1100);
        
        GitCherryPickResult res = client.cherryPick(GitClient.CherryPickOperation.BEGIN, new String[] { BRANCH }, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.OK, res.getCherryPickStatus());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getParents()[0]);
        assertEquals(c.getCommitTime(), res.getCurrentHead().getCommitTime());
        assertEquals(1, res.getCherryPickedCommits().length);
        assertEquals(c.getRevision(), res.getCherryPickedCommits()[0].getRevision());
        assertFalse(new File(workDir, ".git/sequencer").exists());
    }

    public void testCherryPickCommits () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        File[] roots = new File[] { f };
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, Constants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        write(f, "change 1 on branch");
        add(f);
        GitRevisionInfo c1 = client.commit(roots, "on branch 1", null, null, NULL_PROGRESS_MONITOR);
        write(f, "change 2 on branch");
        add(f);
        GitRevisionInfo c2 = client.commit(roots, "on branch 2", null, null, NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initCommit = client.log("HEAD", NULL_PROGRESS_MONITOR);
        
        Thread.sleep(1100);
        
        GitCherryPickResult res = client.cherryPick(GitClient.CherryPickOperation.BEGIN,
                new String[] { c1.getRevision(), c2.getRevision() }, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.OK, res.getCherryPickStatus());
        SearchCriteria sc = new SearchCriteria();
        sc.setRevisionTo("HEAD");
        GitRevisionInfo[] logs = client.log(sc, NULL_PROGRESS_MONITOR);
        assertEquals(3, logs.length);
        assertEquals(c2.getFullMessage(), logs[0].getFullMessage());
        assertEquals(c2.getCommitTime(), logs[0].getCommitTime());
        assertEquals(c1.getFullMessage(), logs[1].getFullMessage());
        assertEquals(c1.getCommitTime(), logs[1].getCommitTime());
        assertEquals(initCommit.getRevision(), logs[2].getRevision());

        assertEquals(2, res.getCherryPickedCommits().length);
        assertEquals(c1.getRevision(), res.getCherryPickedCommits()[0].getRevision());
        assertEquals(c2.getRevision(), res.getCherryPickedCommits()[1].getRevision());
        assertFalse(new File(workDir, ".git/sequencer").exists());
    }
    
    public void testCherryPickFailure () throws Exception {
        File f1 = new File(workDir, "f1");
        File f2 = new File(workDir, "f2");
        write(f1, "init");
        write(f2, "init");
        add(f1, f2);
        commit(f1, f2);
        
        File[] roots = new File[] { f1, f2 };
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, Constants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        write(f1, "change on branch");
        write(f2, "change on branch");
        add(f1, f2);
        GitRevisionInfo c = client.commit(roots, "on branch", null, null, NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        // make modification so cherry-pick cannot start
        write(f1, "change in master");
        write(f2, "change in master");
        
        CherryPickCommand cmd = new CherryPickCommand(getRepository(client), ApiUtils.getClassFactory(),
                new String[] { BRANCH }, GitClient.CherryPickOperation.BEGIN, NULL_PROGRESS_MONITOR, null);
        Field f = CherryPickCommand.class.getDeclaredField("workAroundStrategyIssue");
        f.setAccessible(true);
        f.set(cmd, false);
        cmd.execute();
        GitCherryPickResult res = cmd.getResult();
        // when starts failing, remove the WA in CherryPickCommand for recursive strategy merger.
        assertEquals(1, res.getFailures().size());
        
        res = client.cherryPick(GitClient.CherryPickOperation.BEGIN, new String[] { BRANCH }, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.FAILED, res.getCherryPickStatus());
        assertEquals(2, res.getFailures().size());
        assertEquals(0, res.getCurrentHead().getParents().length);
    }
    
    public void testCherryPickCommitsConflictAbort () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        File[] roots = new File[] { f };
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, Constants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        write(f, "change 1 on branch");
        add(f);
        GitRevisionInfo c1 = client.commit(roots, "on branch 1", null, null, NULL_PROGRESS_MONITOR);
        write(f, "change 2 on branch");
        add(f);
        GitRevisionInfo c2 = client.commit(roots, "on branch 2", null, null, NULL_PROGRESS_MONITOR);
        write(f, "change 3 on branch");
        add(f);
        GitRevisionInfo c3 = client.commit(roots, "on branch 3", null, null, NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initCommit = client.log("HEAD", NULL_PROGRESS_MONITOR);
        GitCherryPickResult res = client.cherryPick(GitClient.CherryPickOperation.BEGIN,
                new String[] { c1.getRevision(), c3.getRevision() }, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.CONFLICTING, res.getCherryPickStatus());
        assertEquals(1, res.getConflicts().size());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getParents()[0]);
        
        res = client.cherryPick(GitClient.CherryPickOperation.ABORT, null, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.ABORTED, res.getCherryPickStatus());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getRevision());
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true,
                Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }
    
    public void testCherryPickCommitsConflictQuit () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        File[] roots = new File[] { f };
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, Constants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        write(f, "change 1 on branch");
        add(f);
        GitRevisionInfo c1 = client.commit(roots, "on branch 1\nblablabla", null, null, NULL_PROGRESS_MONITOR);
        write(f, "change 2 on branch");
        add(f);
        GitRevisionInfo c2 = client.commit(roots, "on branch 2", null, null, NULL_PROGRESS_MONITOR);
        write(f, "change 3 on branch");
        add(f);
        GitRevisionInfo c3 = client.commit(roots, "on branch 3\nBLABLABLA", null, null, NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initCommit = client.log("HEAD", NULL_PROGRESS_MONITOR);
        GitCherryPickResult res = client.cherryPick(GitClient.CherryPickOperation.BEGIN,
                new String[] { c1.getRevision(), c3.getRevision() }, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.CONFLICTING, res.getCherryPickStatus());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getParents()[0]);
        
        ObjectReader or = repository.newObjectReader();
        RevCommit commit = Utils.findCommit(repository, c3.getRevision());
        assertEquals("pick " + or.abbreviate(commit).name() + " " + commit.getShortMessage(),
                read(new File(repository.getDirectory(), "sequencer/todo")));
        
        res = client.cherryPick(GitClient.CherryPickOperation.QUIT, null, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.CONFLICTING, res.getCherryPickStatus());
        assertEquals(1, res.getConflicts().size());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getParents()[0]);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true,
                Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, true);
        assertEquals(RepositoryState.CHERRY_PICKING, repository.getRepositoryState());
        
        res = client.cherryPick(GitClient.CherryPickOperation.ABORT, null, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.ABORTED, res.getCherryPickStatus());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getParents()[0]);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true,
                Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }
    
    public void testCherryPickCommitConflictResolve () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        File[] roots = new File[] { f };
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, Constants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        write(f, "change 1 on branch");
        add(f);
        GitRevisionInfo c1 = client.commit(roots, "on branch 1", null, null, NULL_PROGRESS_MONITOR);
        write(f, "change 2 on branch");
        add(f);
        GitRevisionInfo c2 = client.commit(roots, "on branch 2", null, null, NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initCommit = client.log("HEAD", NULL_PROGRESS_MONITOR);
        GitCherryPickResult res = client.cherryPick(GitClient.CherryPickOperation.BEGIN,
                new String[] { c2.getRevision() }, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.CONFLICTING, res.getCherryPickStatus());
        assertEquals(1, res.getConflicts().size());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getRevision());
        assertFalse(new File(repository.getDirectory(), "sequencer").exists());
        
        write(f, "init\nchange 2 on branch");
        add(f);
        
        // try continue, should interrupt and ask for commit
        res = client.cherryPick(GitClient.CherryPickOperation.CONTINUE, null, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.UNCOMMITTED, res.getCherryPickStatus());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getRevision());
        assertFalse(new File(repository.getDirectory(), "sequencer").exists());
        
        GitRevisionInfo commit = client.commit(new File[0], c2.getFullMessage(), null, null, NULL_PROGRESS_MONITOR);
        assertEquals(c2.getCommitTime(), commit.getCommitTime());
        assertEquals(RepositoryState.SAFE, repository.getRepositoryState());
        assertFalse(new File(repository.getDirectory(), "sequencer").exists());
        
        res = client.cherryPick(GitClient.CherryPickOperation.CONTINUE, null, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.OK, res.getCherryPickStatus());
    }
    
    public void testCherryPickCommitsConflictResolve () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        File[] roots = new File[] { f };
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, Constants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        write(f, "change 1 on branch");
        add(f);
        GitRevisionInfo c1 = client.commit(roots, "on branch 1", null, null, NULL_PROGRESS_MONITOR);
        write(f, "change 2 on branch");
        add(f);
        GitRevisionInfo c2 = client.commit(roots, "on branch 2", null, null, NULL_PROGRESS_MONITOR);
        write(f, "change 3 on branch");
        add(f);
        GitRevisionInfo c3 = client.commit(roots, "on branch 3", null, null, NULL_PROGRESS_MONITOR);
        write(f, "change 4 on branch");
        add(f);
        GitRevisionInfo c4 = client.commit(roots, "on branch 4", null, null, NULL_PROGRESS_MONITOR);
        
        Thread.sleep(1100);
        
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initCommit = client.log("HEAD", NULL_PROGRESS_MONITOR);
        GitCherryPickResult res = client.cherryPick(GitClient.CherryPickOperation.BEGIN,
                new String[] { c2.getRevision(), c3.getRevision(), c4.getRevision() }, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.CONFLICTING, res.getCherryPickStatus());
        assertEquals(1, res.getConflicts().size());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getRevision());
        
        write(f, "init\nchange 2 on branch");
        add(f);
        
        GitRevisionInfo commit = client.commit(new File[0], c2.getFullMessage(), null, null, NULL_PROGRESS_MONITOR);
        assertEquals(c2.getCommitTime(), commit.getCommitTime());
        assertEquals(RepositoryState.SAFE, repository.getRepositoryState());
        assertTrue(new File(repository.getDirectory(), "sequencer").exists());
        
        res = client.cherryPick(GitClient.CherryPickOperation.CONTINUE, null, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.CONFLICTING, res.getCherryPickStatus());
        assertEquals(1, res.getConflicts().size());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getParents()[0]);
        
        write(f, "change 3 on branch");
        add(f);
        
        client.commit(new File[0], c3.getFullMessage(), null, null, NULL_PROGRESS_MONITOR);
        res = client.cherryPick(GitClient.CherryPickOperation.CONTINUE, null, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.OK, res.getCherryPickStatus());
        assertEquals(c4.getCommitTime(), res.getCurrentHead().getCommitTime());
        assertFalse(new File(repository.getDirectory(), "sequencer").exists());
        assertEquals(1, res.getCherryPickedCommits().length);
        assertEquals(c4.getRevision(), res.getCherryPickedCommits()[0].getRevision());
    }
}
