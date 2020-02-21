package org.netbeans.modules.git.remote.cli.jgit.commands;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

import java.io.IOException;
import java.util.Arrays;
import org.netbeans.modules.git.remote.cli.GitCherryPickResult;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitRepositoryState;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitStatus.Status;
import org.netbeans.modules.git.remote.cli.SearchCriteria;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class CherryPickTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;
    private static final String BRANCH = "b";
    
    public CherryPickTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected boolean isFailed() {
        return Arrays.asList("testCherryPickCommitsConflictResolve","testCherryPickCommit",
                "testCherryPickCommits","testCherryPickFailure","testCherryPickCommitsConflictQuit",
                "testCherryPickCommitsConflictAbort","testCherryPickCommitConflictResolve").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }
    
    public void testCherryPickCommit () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        write(f, "change on branch");
        add(f);
        GitRevisionInfo c = client.commit(new VCSFileProxy[] { f }, "on branch", null, null, NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initCommit = client.log("HEAD", NULL_PROGRESS_MONITOR);
        
        Thread.sleep(1100);
        
        GitCherryPickResult res = client.cherryPick(GitClient.CherryPickOperation.BEGIN, new String[] { BRANCH }, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.OK, res.getCherryPickStatus());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getParents()[0]);
        assertEquals(c.getCommitTime(), res.getCurrentHead().getCommitTime());
        assertEquals(1, res.getCherryPickedCommits().length);
        assertEquals(c.getRevision(), res.getCherryPickedCommits()[0].getRevision());
        assertFalse(VCSFileProxy.createFileProxy(workDir, ".git/sequencer").exists());
    }

    public void testCherryPickCommits () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        VCSFileProxy[] roots = new VCSFileProxy[] { f };
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        write(f, "change 1 on branch");
        add(f);
        GitRevisionInfo c1 = client.commit(roots, "on branch 1", null, null, NULL_PROGRESS_MONITOR);
        write(f, "change 2 on branch");
        add(f);
        GitRevisionInfo c2 = client.commit(roots, "on branch 2", null, null, NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
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
        assertFalse(VCSFileProxy.createFileProxy(workDir, ".git/sequencer").exists());
    }
    
    public void testCherryPickFailure () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        VCSFileProxy[] roots = new VCSFileProxy[] { f };
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        write(f, "change on branch");
        add(f);
        GitRevisionInfo c = client.commit(roots, "on branch", null, null, NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        // make modification so cherry-pick cannot start
        write(f, "change in master");
        
        GitCherryPickResult res = client.cherryPick(GitClient.CherryPickOperation.BEGIN, new String[] { BRANCH }, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.FAILED, res.getCherryPickStatus());
        assertEquals(0, res.getCurrentHead().getParents().length);
    }
    
    public void testCherryPickCommitsConflictAbort () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        VCSFileProxy[] roots = new VCSFileProxy[] { f };
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
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
        
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
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
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        VCSFileProxy[] roots = new VCSFileProxy[] { f };
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
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
        
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initCommit = client.log("HEAD", NULL_PROGRESS_MONITOR);
        GitCherryPickResult res = client.cherryPick(GitClient.CherryPickOperation.BEGIN,
                new String[] { c1.getRevision(), c3.getRevision() }, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.CONFLICTING, res.getCherryPickStatus());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getParents()[0]);
        
        GitRevisionInfo commit = client.log(c3.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals("pick " + c3.getRevision() + " " + commit.getShortMessage(),
                read(VCSFileProxy.createFileProxy(repository.getLocation(), "sequencer/todo")));
        
        res = client.cherryPick(GitClient.CherryPickOperation.QUIT, null, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.CONFLICTING, res.getCherryPickStatus());
        assertEquals(1, res.getConflicts().size());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getParents()[0]);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true,
                Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, true);
        assertEquals(GitRepositoryState.CHERRY_PICKING, repository.getRepositoryState());
        
        res = client.cherryPick(GitClient.CherryPickOperation.ABORT, null, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.ABORTED, res.getCherryPickStatus());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getParents()[0]);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true,
                Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }
    
    public void testCherryPickCommitConflictResolve () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        VCSFileProxy[] roots = new VCSFileProxy[] { f };
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH, true, NULL_PROGRESS_MONITOR);
        
        write(f, "change 1 on branch");
        add(f);
        GitRevisionInfo c1 = client.commit(roots, "on branch 1", null, null, NULL_PROGRESS_MONITOR);
        write(f, "change 2 on branch");
        add(f);
        GitRevisionInfo c2 = client.commit(roots, "on branch 2", null, null, NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initCommit = client.log("HEAD", NULL_PROGRESS_MONITOR);
        GitCherryPickResult res = client.cherryPick(GitClient.CherryPickOperation.BEGIN,
                new String[] { c2.getRevision() }, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.CONFLICTING, res.getCherryPickStatus());
        assertEquals(1, res.getConflicts().size());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getRevision());
        assertFalse(VCSFileProxy.createFileProxy(repository.getLocation(), "sequencer").exists());
        
        write(f, "init\nchange 2 on branch");
        add(f);
        
        // try continue, should interrupt and ask for commit
        res = client.cherryPick(GitClient.CherryPickOperation.CONTINUE, null, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.UNCOMMITTED, res.getCherryPickStatus());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getRevision());
        assertFalse(VCSFileProxy.createFileProxy(repository.getLocation(), "sequencer").exists());
        
        GitRevisionInfo commit = client.commit(new VCSFileProxy[0], c2.getFullMessage(), null, null, NULL_PROGRESS_MONITOR);
        assertEquals(c2.getCommitTime(), commit.getCommitTime());
        assertEquals(GitRepositoryState.SAFE, repository.getRepositoryState());
        assertFalse(VCSFileProxy.createFileProxy(repository.getLocation(), "sequencer").exists());
        
        res = client.cherryPick(GitClient.CherryPickOperation.CONTINUE, null, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.OK, res.getCherryPickStatus());
    }
    
    public void testCherryPickCommitsConflictResolve () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        VCSFileProxy[] roots = new VCSFileProxy[] { f };
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
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
        
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initCommit = client.log("HEAD", NULL_PROGRESS_MONITOR);
        GitCherryPickResult res = client.cherryPick(GitClient.CherryPickOperation.BEGIN,
                new String[] { c2.getRevision(), c3.getRevision(), c4.getRevision() }, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.CONFLICTING, res.getCherryPickStatus());
        assertEquals(1, res.getConflicts().size());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getRevision());
        
        write(f, "init\nchange 2 on branch");
        add(f);
        
        GitRevisionInfo commit = client.commit(new VCSFileProxy[0], c2.getFullMessage(), null, null, NULL_PROGRESS_MONITOR);
        assertEquals(c2.getCommitTime(), commit.getCommitTime());
        assertEquals(GitRepositoryState.SAFE, repository.getRepositoryState());
        assertTrue(VCSFileProxy.createFileProxy(repository.getLocation(), "sequencer").exists());
        
        res = client.cherryPick(GitClient.CherryPickOperation.CONTINUE, null, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.CONFLICTING, res.getCherryPickStatus());
        assertEquals(1, res.getConflicts().size());
        assertEquals(initCommit.getRevision(), res.getCurrentHead().getParents()[0]);
        
        write(f, "change 3 on branch");
        add(f);
        
        client.commit(new VCSFileProxy[0], c3.getFullMessage(), null, null, NULL_PROGRESS_MONITOR);
        res = client.cherryPick(GitClient.CherryPickOperation.CONTINUE, null, NULL_PROGRESS_MONITOR);
        assertEquals(GitCherryPickResult.CherryPickStatus.OK, res.getCherryPickStatus());
        assertEquals(c4.getCommitTime(), res.getCurrentHead().getCommitTime());
        assertFalse(VCSFileProxy.createFileProxy(repository.getLocation(), "sequencer").exists());
        assertEquals(1, res.getCherryPickedCommits().length);
        assertEquals(c4.getRevision(), res.getCherryPickedCommits()[0].getRevision());
    }
}
