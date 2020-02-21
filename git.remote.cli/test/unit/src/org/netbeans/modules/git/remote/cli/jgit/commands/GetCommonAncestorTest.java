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
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitMergeResult;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class GetCommonAncestorTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;
    private static final String BRANCH_NAME = "new_branch";

    public GetCommonAncestorTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testGetBaseRevisionMerge","testGetBaseCrissCross_232904","testGetBaseCrissCrossConflict_244222").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testGetBaseRevisionMerge () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "a\nb\nc");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        write(f, "a\nb\nc\n");
        add(files);
        GitRevisionInfo revisionBase = client.commit(files, "base revision", null, null, NULL_PROGRESS_MONITOR);
        
        client.createBranch("b", "master", NULL_PROGRESS_MONITOR);
        client.checkoutRevision("b", true, NULL_PROGRESS_MONITOR);
        
        write(f, "modification on branch\nb\nc\n");
        add(files);
        
        client.checkoutRevision("master", true, NULL_PROGRESS_MONITOR);
        write(f, "a\nb\nmodification on master\n");
        add(files);
        client.commit(files, "modification on master", null, null, NULL_PROGRESS_MONITOR);
        
        GitRevisionInfo revisionMerge = client.log(client.merge("b", NULL_PROGRESS_MONITOR).getNewHead(), NULL_PROGRESS_MONITOR);
        
        GitRevisionInfo revision = client.getCommonAncestor(revisionMerge.getParents(), NULL_PROGRESS_MONITOR);
        assertRevisions(revisionBase, revision);
    }
    
    public void testGetBaseRevisionSimpleCommit () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        write(f, "modification");
        add(files);
        GitRevisionInfo commit = client.commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo revision = client.getCommonAncestor(new String[] { commit.getRevision() }, NULL_PROGRESS_MONITOR);
        assertRevisions(commit, revision);
    }
    
    public void testGetBaseRevisionCommitsInRow () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        write(f, "modification 1");
        add(files);
        GitRevisionInfo commit1 = client.commit(files, "modification 1", null, null, NULL_PROGRESS_MONITOR);
        
        write(f, "modification 2");
        add(files);
        GitRevisionInfo commit2 = client.commit(files, "modification 2", null, null, NULL_PROGRESS_MONITOR);
        
        write(f, "modification 3");
        add(files);
        GitRevisionInfo commit3 = client.commit(files, "modification 3", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo revision = client.getCommonAncestor(new String[] { commit1.getRevision(), commit2.getRevision(), commit3.getRevision() }, NULL_PROGRESS_MONITOR);
        assertRevisions(commit1, revision);
    }
    
    public void testGetBaseCrissCross_232904 () throws Exception {
        VCSFileProxy f1 = VCSFileProxy.createFileProxy(workDir, "f1");
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "f2");
        VCSFileProxy f3 = VCSFileProxy.createFileProxy(workDir, "f3");
        write(f1, "initial content");
        GitClient client = getClient(workDir);
        VCSFileProxy[] files = new VCSFileProxy[] { f1, f2, f3 };
        client.add(new VCSFileProxy[] {f1}, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initial = client.commit(new VCSFileProxy[] {f1}, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        
        client.createBranch(BRANCH_NAME, "master", NULL_PROGRESS_MONITOR);
        
        // change on master
        write(f1, GitConstants.MASTER);
        client.add(new VCSFileProxy[] {f1}, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] {f1}, "master commit", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo masterCommit = client.log("master", NULL_PROGRESS_MONITOR);
        
        // change on branch
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f2, BRANCH_NAME);
        client.add(new VCSFileProxy[] {f2}, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] {f2}, "branch commit", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo branchCommit = client.log(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        
        // merge last master commit (not merge) into branch
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        client.merge(masterCommit.getRevision(), NULL_PROGRESS_MONITOR);
        
        // merge last branch commit (not merge) into master
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        client.merge(branchCommit.getRevision(), NULL_PROGRESS_MONITOR);
        
        GitRevisionInfo ancestor = client.getCommonAncestor(new String[] { GitConstants.MASTER, BRANCH_NAME }, NULL_PROGRESS_MONITOR);
        assertEquals(initial.getRevision(), ancestor.getRevision());
    }
    
    public void testGetBaseCrissCrossConflict_244222 () throws Exception {
        VCSFileProxy f1 = VCSFileProxy.createFileProxy(workDir, "f1");
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "f2");
        VCSFileProxy f3 = VCSFileProxy.createFileProxy(workDir, "f3");
        write(f1, "initial content");
        GitClient client = getClient(workDir);
        VCSFileProxy[] files = new VCSFileProxy[] { f1, f2, f3 };
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initial = client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        
        client.createBranch(BRANCH_NAME, "master", NULL_PROGRESS_MONITOR);
        
        // change on master
        write(f1, GitConstants.MASTER);
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(files, "master commit", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo masterCommit = client.log("master", NULL_PROGRESS_MONITOR);
        
        // change on branch
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f1, BRANCH_NAME);
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(files, "branch commit", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo branchCommit = client.log(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        
        // merge last master commit (not merge) into branch
        GitMergeResult res = client.merge(masterCommit.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(GitMergeResult.MergeStatus.CONFLICTING, res.getMergeStatus());
        write(f1, BRANCH_NAME);
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[0], "Merge master into branch", null, null, NULL_PROGRESS_MONITOR);
        
        // merge last branch commit (not merge) into master
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        res = client.merge(branchCommit.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(GitMergeResult.MergeStatus.CONFLICTING, res.getMergeStatus());
        write(f1, GitConstants.MASTER);
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[0], "Merge branch into master", null, null, NULL_PROGRESS_MONITOR);
        
        GitRevisionInfo ancestor = client.getCommonAncestor(new String[] { GitConstants.MASTER, BRANCH_NAME }, NULL_PROGRESS_MONITOR);
        assertEquals(initial.getRevision(), ancestor.getRevision());
    }

    private void assertRevisions (GitRevisionInfo expected, GitRevisionInfo info) throws GitException {
        assertEquals(expected.getRevision(), info.getRevision());
        assertEquals(expected.getAuthor().toString(), info.getAuthor().toString());
        assertEquals(expected.getCommitTime(), info.getCommitTime());
        assertEquals(expected.getCommitter().toString(), info.getCommitter().toString());
        assertEquals(expected.getFullMessage(), info.getFullMessage());
        assertEquals(expected.getModifiedFiles().size(), info.getModifiedFiles().size());
        assertEquals(expected.getShortMessage(), info.getShortMessage());
    }

}
