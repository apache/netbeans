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

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitClient.RebaseOperationType;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitRebaseResult;
import org.netbeans.modules.git.remote.cli.GitRebaseResult.RebaseStatus;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.SearchCriteria;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class RebaseTest extends AbstractGitTestCase {
    private static final String BRANCH_NAME = "new_branch";
    private JGitRepository repository;
    private VCSFileProxy workDir;

    public RebaseTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testRebaseSimple").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }
    
    public void testRebaseNoChange () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        String head = client.resolve(GitConstants.HEAD, NULL_PROGRESS_MONITOR).getRevision();
        GitBranch branch = client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        
        // rebase branch to master
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.UP_TO_DATE, result.getRebaseStatus());
        assertEquals(head, result.getCurrentHead());
        
        // do a commit and 
        write(f, "change");
        add(f);
        GitRevisionInfo commit = client.commit(new VCSFileProxy[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        // rebase branch to master, they are now different but still no rebase is needed
        result = client.rebase(RebaseOperationType.BEGIN, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.UP_TO_DATE, result.getRebaseStatus());
        assertEquals(commit.getRevision(), result.getCurrentHead());
    }
    
    public void testRebaseSimple () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "file2");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        write(f2, GitConstants.MASTER);
        add(f2);
        GitRevisionInfo master = client.commit(new VCSFileProxy[] { f2 }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.createBranch(BRANCH_NAME, "master^1", NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        assertFalse(f2.exists());
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo info = client.commit(new VCSFileProxy[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        write(f, "another change");
        add(f);
        GitRevisionInfo info2 = client.commit(new VCSFileProxy[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        // sleep to change time
        Thread.sleep(1100);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.OK, result.getRebaseStatus());
        assertEquals("another change", read(f));
        assertEquals(GitConstants.MASTER, read(f2));
        
        SearchCriteria crit = new SearchCriteria();
        crit.setRevisionFrom(GitConstants.MASTER);
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
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "file2");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, GitConstants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new VCSFileProxy[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        write(f2, BRANCH_NAME);
        add(f2);
        GitRevisionInfo branchInfo = client.commit(new VCSFileProxy[] { f, f2 }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.STOPPED, result.getRebaseStatus());
        String content = read(f);
if (false){
        String  golden;
if (false) {
        golden =
        "<<<<<<< Upstream, based on master\n" +
        "master\n" +
        "=======\n" +
        "new_branch\n" +
        ">>>>>>> " + branchInfo.getRevision().substring(0, 7) + " " + branchInfo.getShortMessage();
} else {
        golden =
        "<<<<<<< HEAD\n" +
        "master\n" +
        "=======\n" +
        "new_branch\n" +
        ">>>>>>> change on branch";
}
        assertEquals(golden, content);
}        
        assertEquals(Arrays.asList(f), result.getConflicts());
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { f, f2 }, NULL_PROGRESS_MONITOR);
        assertTrue(statuses.get(f).isConflict());
        assertStatus(statuses, workDir, f2, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertEquals(branchInfo.getRevision(), result.getCurrentCommit());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
    }
    
    public void testRebaseAbort () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, GitConstants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new VCSFileProxy[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo branchInfo = client.commit(new VCSFileProxy[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.STOPPED, result.getRebaseStatus());
        assertEquals(Arrays.asList(f), result.getConflicts());
        assertEquals(branchInfo.getRevision(), result.getCurrentCommit());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        
        result = client.rebase(RebaseOperationType.ABORT, null, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.ABORTED, result.getRebaseStatus());
        assertTrue(result.getConflicts().isEmpty());
        // resets to original state
        assertEquals(branchInfo.getRevision(), result.getCurrentHead());
        assertEquals(branchInfo.getRevision(), client.resolve(BRANCH_NAME, NULL_PROGRESS_MONITOR).getRevision());
        assertEquals(masterInfo.getRevision(), client.resolve(GitConstants.MASTER, NULL_PROGRESS_MONITOR).getRevision());
    }
    
    public void testRebaseSkip () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, GitConstants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new VCSFileProxy[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo branchInfo = client.commit(new VCSFileProxy[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.STOPPED, result.getRebaseStatus());
        assertEquals(Arrays.asList(f), result.getConflicts());
        assertEquals(branchInfo.getRevision(), result.getCurrentCommit());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        
        result = client.rebase(RebaseOperationType.SKIP, null, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.OK, result.getRebaseStatus());
        assertTrue(result.getConflicts().isEmpty());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        assertEquals(masterInfo.getRevision(), client.resolve(BRANCH_NAME, NULL_PROGRESS_MONITOR).getRevision());
        assertEquals(masterInfo.getRevision(), client.resolve(GitConstants.MASTER, NULL_PROGRESS_MONITOR).getRevision());
    }
    
    public void testResolveConflictsNoCommit () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, GitConstants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new VCSFileProxy[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo branchInfo = client.commit(new VCSFileProxy[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.STOPPED, result.getRebaseStatus());
        String content = read(f);
if (false){
        String  golden;
if (false) {
        golden =
        "<<<<<<< Upstream, based on master\n" +
        "master\n" +
        "=======\n" +
        "new_branch\n" +
        ">>>>>>> " + branchInfo.getRevision().substring(0, 7) + " " + branchInfo.getShortMessage();
} else {
        golden =
        "<<<<<<< HEAD\n" +
        "master\n" +
        "=======\n" +
        "new_branch\n" +
        ">>>>>>> change on branch";
}
        assertEquals(golden, content);
}
        assertEquals(Arrays.asList(f), result.getConflicts());
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(statuses.get(f).isConflict());
        assertEquals(branchInfo.getRevision(), result.getCurrentCommit());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        write(f, GitConstants.MASTER);
        add(f);
        
        result = client.rebase(RebaseOperationType.CONTINUE, null, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.NOTHING_TO_COMMIT, result.getRebaseStatus());
        assertTrue(result.getConflicts().isEmpty());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        assertEquals(branchInfo.getRevision(), client.resolve(BRANCH_NAME, NULL_PROGRESS_MONITOR).getRevision());
        assertEquals(masterInfo.getRevision(), client.resolve(GitConstants.MASTER, NULL_PROGRESS_MONITOR).getRevision());
        
        // still have to finish rebase
        result = client.rebase(RebaseOperationType.SKIP, null, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.OK, result.getRebaseStatus());
        assertTrue(result.getConflicts().isEmpty());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        assertEquals(masterInfo.getRevision(), client.resolve(BRANCH_NAME, NULL_PROGRESS_MONITOR).getRevision());
        assertEquals(masterInfo.getRevision(), client.resolve(GitConstants.MASTER, NULL_PROGRESS_MONITOR).getRevision());
    }
    
    public void testResolveConflicts () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, GitConstants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new VCSFileProxy[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo branchInfo = client.commit(new VCSFileProxy[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.STOPPED, result.getRebaseStatus());
        assertEquals(Arrays.asList(f), result.getConflicts());
        assertEquals(branchInfo.getRevision(), result.getCurrentCommit());
        assertEquals(masterInfo.getRevision(), result.getCurrentHead());
        
        write(f, "resolving conflict");
        add(f);
        
        result = client.rebase(RebaseOperationType.CONTINUE, null, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.OK, result.getRebaseStatus());
        assertEquals(client.resolve(GitConstants.HEAD, NULL_PROGRESS_MONITOR).getRevision(), result.getCurrentHead());
        assertEquals(client.resolve(GitConstants.HEAD, NULL_PROGRESS_MONITOR).getRevision(), client.resolve(BRANCH_NAME, NULL_PROGRESS_MONITOR).getRevision());
        assertEquals(masterInfo.getRevision(), client.resolve(GitConstants.MASTER, NULL_PROGRESS_MONITOR).getRevision());
    }
    
    public void testRebaseFailCheckoutLocalChanges () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, GitConstants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new VCSFileProxy[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo branchInfo = client.commit(new VCSFileProxy[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        write(f, "local change");
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.FAILED, result.getRebaseStatus());
if(false)assertEquals(Arrays.asList(f), result.getFailures());
else     ;// TODO: uncommited changes
        assertEquals(branchInfo.getRevision(), client.resolve(BRANCH_NAME, NULL_PROGRESS_MONITOR).getRevision());
        assertEquals(masterInfo.getRevision(), client.resolve(GitConstants.MASTER, NULL_PROGRESS_MONITOR).getRevision());
        // resets HEAD
        assertEquals(branchInfo.getRevision(), client.resolve(GitConstants.HEAD, NULL_PROGRESS_MONITOR).getRevision());
    }
    
    public void testRebaseFailMergeLocalChanges () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "file2");
        write(f, "init");
        write(f2, "init");
        add(f, f2);
        commit(f, f2);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, GitConstants.MASTER);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new VCSFileProxy[] { f, f2 }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo branchInfo = client.commit(new VCSFileProxy[] { f, f2 }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        write(f2, "local change");
        add(f2);
        
        GitRebaseResult result = client.rebase(RebaseOperationType.BEGIN, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(RebaseStatus.FAILED, result.getRebaseStatus());
if(false)assertEquals(Arrays.asList(f2), result.getFailures());        
else     ;// TODO: detect uncommited changes
        assertEquals(branchInfo.getRevision(), client.resolve(BRANCH_NAME, NULL_PROGRESS_MONITOR).getRevision());
        assertEquals(masterInfo.getRevision(), client.resolve(GitConstants.MASTER, NULL_PROGRESS_MONITOR).getRevision());
        // resets HEAD
        assertEquals(branchInfo.getRevision(), client.resolve(GitConstants.HEAD, NULL_PROGRESS_MONITOR).getRevision());
    }
}
