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
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitMergeResult;
import org.netbeans.modules.git.remote.cli.GitMergeResult.MergeStatus;
import org.netbeans.modules.git.remote.cli.GitRepository;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.SearchCriteria;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitConfig;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class MergeTest extends AbstractGitTestCase {
    private JGitRepository repo;
    private VCSFileProxy workDir;
    private static final String BRANCH_NAME = "new_branch";

    public MergeTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testConflicts","testResolveConflicts","testMergeFFOnly",
                "testMergeFailOnLocalChanges","testMergeCommitFails250370").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repo = getLocalGitRepository();
    }

    public void testMergeNoChange () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        GitBranch branch = client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        
        GitMergeResult result = client.merge(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.ALREADY_UP_TO_DATE, result.getMergeStatus());
        result = client.merge(branch.getId(), NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.ALREADY_UP_TO_DATE, result.getMergeStatus());
    }
    
    public void testMergeFastForward () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo info = client.commit(new VCSFileProxy[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        
        assertEquals("init", read(f));
        
        GitMergeResult result = client.merge(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.FAST_FORWARD, result.getMergeStatus());
        assertEquals(BRANCH_NAME, read(f));
        
        SearchCriteria crit = new SearchCriteria();
        crit.setRevisionTo(GitConstants.MASTER);
        GitRevisionInfo[] logs = client.log(crit, NULL_PROGRESS_MONITOR);
        assertEquals(2, logs.length);
        assertEquals(logs[0].getRevision(), info.getRevision());
        
        // continue working on branch
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        remove(false, f);
        info = client.commit(new VCSFileProxy[] { f }, "delete on branch", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        
        assertEquals(BRANCH_NAME, read(f));
        
        result = client.merge(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.FAST_FORWARD, result.getMergeStatus());
        assertFalse(f.exists());
        
        crit = new SearchCriteria();
        crit.setRevisionTo(GitConstants.MASTER);
        logs = client.log(crit, NULL_PROGRESS_MONITOR);
        assertEquals(3, logs.length);
        assertEquals(logs[0].getRevision(), info.getRevision());
    }
    
    public void testMergeRevision () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo info = client.commit(new VCSFileProxy[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        write(f, "another change");
        add(f);
        GitRevisionInfo info2 = client.commit(new VCSFileProxy[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        
        assertEquals("init", read(f));
        
        GitMergeResult result = client.merge(info.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.FAST_FORWARD, result.getMergeStatus());
        assertEquals(BRANCH_NAME, read(f));
        
        SearchCriteria crit = new SearchCriteria();
        crit.setRevisionTo(GitConstants.MASTER);
        GitRevisionInfo[] logs = client.log(crit, NULL_PROGRESS_MONITOR);
        assertEquals(2, logs.length);
        assertEquals(logs[0].getRevision(), info.getRevision());
        
        // merge the rest
        result = client.merge(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.FAST_FORWARD, result.getMergeStatus());
        assertEquals("another change", read(f));
        
        crit = new SearchCriteria();
        crit.setRevisionTo(GitConstants.MASTER);
        logs = client.log(crit, NULL_PROGRESS_MONITOR);
        assertEquals(3, logs.length);
        assertEquals(logs[0].getRevision(), info2.getRevision());
    }
    
    public void testConflicts () throws Exception {
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
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        
        assertEquals(GitConstants.MASTER, read(f));
        
        GitMergeResult result = client.merge(branchInfo.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.CONFLICTING, result.getMergeStatus());
        assertEquals("<<<<<<< HEAD\nmaster\n=======\nnew_branch\n>>>>>>> " + branchInfo.getRevision(), read(f));
        assertNull(result.getNewHead());
        assertEquals(Arrays.asList(f), result.getConflicts());
        
        SearchCriteria crit = new SearchCriteria();
        crit.setRevisionTo(GitConstants.MASTER);
        GitRevisionInfo[] logs = client.log(crit, NULL_PROGRESS_MONITOR);
        assertEquals(2, logs.length);
        assertEquals(logs[0].getRevision(), masterInfo.getRevision());
        
        // try merge with branch as revision
        client.reset(GitConstants.MASTER, GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        result = client.merge(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.CONFLICTING, result.getMergeStatus());
        assertEquals("<<<<<<< HEAD\nmaster\n=======\nnew_branch\n>>>>>>> " + BRANCH_NAME, read(f));
        assertNull(result.getNewHead());
        assertEquals(Arrays.asList(f), result.getConflicts());
        //assertEquals("Merge new_branch\n\nConflicts:\n\tfile\n", repo.readMergeCommitMsg());
        
        crit = new SearchCriteria();
        crit.setRevisionTo(GitConstants.MASTER);
        logs = client.log(crit, NULL_PROGRESS_MONITOR);
        assertEquals(2, logs.length);
        assertEquals(logs[0].getRevision(), masterInfo.getRevision());
        
        // test obstructing paths
        client.reset(GitConstants.MASTER, GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        write(f, "local change");
        result = client.merge(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.FAILED, result.getMergeStatus());
        assertEquals("local change", read(f));
        assertNull(result.getNewHead());
        assertEquals(Arrays.asList(f), result.getFailures());
        //assertNull(repo.readMergeCommitMsg());
    }
    
    public void testResolveConflicts () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        String[] contents = { "aaaaa\nbbbbb\nccccc", "xxxxx\nbbbbb\nccccc", "aaaaa\nbbbbb\nyyyyy", "xxxxx\nbbbbb\nyyyyy" };
        write(f, contents[0]);
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, contents[1]);
        add(f);
        GitRevisionInfo masterInfo = client.commit(new VCSFileProxy[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        Thread.sleep(1100);
        write(f, contents[2]);
        add(f);
        GitRevisionInfo branchInfo = client.commit(new VCSFileProxy[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        
        assertEquals(contents[1], read(f));
        
        GitMergeResult result = client.merge(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.MERGED, result.getMergeStatus());
        assertEquals(contents[3], read(f));
        assertEquals(0, result.getConflicts().size());
        assertEquals(Arrays.asList(new String[] { masterInfo.getRevision(), branchInfo.getRevision() }), Arrays.asList(result.getMergedCommits()));
        
        SearchCriteria crit = new SearchCriteria();
        crit.setRevisionTo(GitConstants.MASTER);
        GitRevisionInfo[] logs = client.log(crit, NULL_PROGRESS_MONITOR);
        assertEquals(logs[0].getRevision(), result.getNewHead());
        assertEquals(logs[1].getRevision(), branchInfo.getRevision());
        assertEquals(logs[2].getRevision(), masterInfo.getRevision());
        String logFileContent[] = read(VCSFileProxy.createFileProxy(workDir, ".git/logs/HEAD")).split("\\n");
        assertEquals("merge new_branch: Merge made by recursive.", 
                logFileContent[logFileContent.length - 1].substring(logFileContent[logFileContent.length - 1].indexOf("merge new_branch")));
        
        client.reset("master~1", GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        result = client.merge(branchInfo.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.MERGED, result.getMergeStatus());
        assertEquals(contents[3], read(f));
        assertEquals(0, result.getConflicts().size());
        assertEquals(Arrays.asList(new String[] { masterInfo.getRevision(), branchInfo.getRevision() }), Arrays.asList(result.getMergedCommits()));
        
        crit = new SearchCriteria();
        crit.setRevisionTo(GitConstants.MASTER);
        logs = client.log(crit, NULL_PROGRESS_MONITOR);
        assertEquals(logs[0].getRevision(), result.getNewHead());
        assertEquals(logs[1].getRevision(), branchInfo.getRevision());
        assertEquals(logs[2].getRevision(), masterInfo.getRevision());
        logFileContent = read(VCSFileProxy.createFileProxy(workDir, ".git/logs/HEAD")).split("\\n");
        assertEquals("merge " + branchInfo.getRevision() + ": Merge made by recursive.", logFileContent[logFileContent.length - 1].substring(logFileContent[logFileContent.length - 1].indexOf("merge ")));
    }
    
    public void testMergeFailOnLocalChanges () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        write(f, "init");
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "file2");
        write(f2, "init");
        VCSFileProxy[] files = { f, f2 };
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        write(f2, BRANCH_NAME);
        add(f2);
        GitRevisionInfo branchInfo = client.commit(files, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        
        assertEquals("init", read(f));
        assertEquals("init", read(f2));
        
        write(f, GitConstants.MASTER);
        write(f2, GitConstants.MASTER);
        
        try {
            client.merge(branchInfo.getRevision(), NULL_PROGRESS_MONITOR);
            fail("Should fail");
        } catch (GitException.CheckoutConflictException ex) {
            // OK
            assertEquals(Arrays.asList(new String[] { f.getName(), f2.getName() }), Arrays.asList(ex.getConflicts()));
        }
    }
    
//    public void testMergeBranchNoHeadYet_196837 () throws Exception {
//        StoredConfig cfg = getRemoteRepository().getConfig();
//        cfg.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_BARE, false);
//        cfg.save();
//        VCSFileProxy otherRepo = getRemoteRepository().getWorkTree();
//        VCSFileProxy original = VCSFileProxy.createFileProxy(otherRepo, "f");
//        GitClient clientOtherRepo = getClient(otherRepo);
//        write(original, "initial content");
//        clientOtherRepo.add(new VCSFileProxy[] { original }, NULL_PROGRESS_MONITOR);
//        clientOtherRepo.commit(new VCSFileProxy[] { original }, "initial commit", null, null, NULL_PROGRESS_MONITOR);
//        
//        GitClient client = getClient(workDir);
//        Map<String, GitTransportUpdate> updates = client.fetch(otherRepo.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/master:refs/remotes/origin/master" }), NULL_PROGRESS_MONITOR);
//        GitMergeResult result = client.merge("origin/master", NULL_PROGRESS_MONITOR);
//        assertEquals(MergeStatus.FAST_FORWARD, result.getMergeStatus());
//        assertEquals(Arrays.asList(new String[] { ObjectId.zeroId().getName(), updates.get("origin/master").getNewObjectId() }), Arrays.asList(result.getMergedCommits()));
//    }
    
    public void testMergeCrissCross_232904 () throws Exception {
        VCSFileProxy f1 = VCSFileProxy.createFileProxy(workDir, "f1");
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "f2");
        VCSFileProxy f3 = VCSFileProxy.createFileProxy(workDir, "f3");
        write(f1, "initial content");
        GitClient client = getClient(workDir);
        VCSFileProxy[] files = new VCSFileProxy[] { f1, f2, f3 };
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        
        client.createBranch(BRANCH_NAME, "master", NULL_PROGRESS_MONITOR);
        
        // change on master
        write(f1, GitConstants.MASTER);
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(files, "master commit", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo masterCommit = client.log("master", NULL_PROGRESS_MONITOR);
        
        // change on branch
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f2, BRANCH_NAME);
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(files, "branch commit", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo branchCommit = client.log(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        
        // merge last master commit (not merge) into branch
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        client.merge(masterCommit.getRevision(), NULL_PROGRESS_MONITOR);
        
        // merge last branch commit (not merge) into master
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        client.merge(branchCommit.getRevision(), NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        client.merge(BRANCH_NAME, NULL_PROGRESS_MONITOR);
    }
    
    public void testMergeNoFastForward () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        write(f, "init");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, BRANCH_NAME);
        add(f);
        GitRevisionInfo info = client.commit(new VCSFileProxy[] { f }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        
        assertEquals("init", read(f));
        
        GitMergeResult result = client.merge(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.FAST_FORWARD, result.getMergeStatus());
        assertEquals(BRANCH_NAME, read(f));
        
        SearchCriteria crit = new SearchCriteria();
        crit.setRevisionTo(GitConstants.MASTER);
        GitRevisionInfo[] logs = client.log(crit, NULL_PROGRESS_MONITOR);
        assertEquals(2, logs.length);
        assertEquals(logs[0].getRevision(), info.getRevision());
        
        // continue working on branch
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        remove(false, f);
        client.commit(new VCSFileProxy[] { f }, "delete on branch", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        
        assertEquals(BRANCH_NAME, read(f));
        
        result = client.merge(BRANCH_NAME, GitRepository.FastForwardOption.NO_FAST_FORWARD, NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.MERGED, result.getMergeStatus());
        assertFalse(f.exists());
        
        crit = new SearchCriteria();
        crit.setRevisionTo(GitConstants.MASTER);
        logs = client.log(crit, NULL_PROGRESS_MONITOR);
        assertEquals(4, logs.length);
        assertEquals(2, logs[0].getParents().length);
    }
    
    public void testMergeFFOnly () throws Exception {
        VCSFileProxy f1 = VCSFileProxy.createFileProxy(workDir, "file1");
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "file2");
        write(f1, "init");
        write(f2, "init");
        add(f1, f2);
        commit(f1, f2);
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f1, BRANCH_NAME);
        add(f1);
        client.commit(new VCSFileProxy[] { f1 }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        write(f2, "another change");
        add(f2);
        client.commit(new VCSFileProxy[] { f2 }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        
        GitMergeResult result = client.merge(BRANCH_NAME, GitRepository.FastForwardOption.FAST_FORWARD_ONLY, NULL_PROGRESS_MONITOR);
        // no merge commits allowed => FAIL
        assertEquals(MergeStatus.ABORTED, result.getMergeStatus());
        
        // test also config files
        assertEquals(GitRepository.FastForwardOption.FAST_FORWARD, GitRepository.getInstance(workDir).getDefaultFastForwardOption());
        
        JGitConfig cfg = repo.getConfig();
        cfg.setString(JGitConfig.CONFIG_KEY_MERGE, null, JGitConfig.CONFIG_KEY_FF, "only");
        cfg.save();
        assertEquals(GitRepository.FastForwardOption.FAST_FORWARD_ONLY, GitRepository.getInstance(workDir).getDefaultFastForwardOption());
        result = client.merge(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        // no merge commits allowed => FAIL
        assertEquals(MergeStatus.ABORTED, result.getMergeStatus());
        
        result = client.merge(BRANCH_NAME, GitRepository.FastForwardOption.FAST_FORWARD, NULL_PROGRESS_MONITOR);
        // merge commits allowed => OK
        assertEquals(MergeStatus.MERGED, result.getMergeStatus());
    }
    
    public void testMergeCommitFails250370 () throws Exception {
        VCSFileProxy f1 = VCSFileProxy.createFileProxy(workDir, "file1");
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "file2");
        write(f1, "init");
        write(f2, "init");
        add(f1, f2);
        commit(f1, f2);
        
        VCSFileProxySupport.mkdirs(VCSFileProxy.createFileProxy(workDir, ".git/rebase-apply"));
        
        GitClient client = getClient(workDir);
        client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f1, BRANCH_NAME);
        add(f1);
        client.commit(new VCSFileProxy[] { f1 }, "change on branch", null, null, NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        write(f2, "master");
        add(f2);
        client.commit(new VCSFileProxy[] { f2 }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        
        try {
            client.merge(BRANCH_NAME, NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException ex) {
            // merge should return a meaningful message with a description
            assertEquals(Utils.getBundle(MergeCommand.class).getString("MSG_MergeCommand.commitErr.wrongRepoState"), ex.getLocalizedMessage());
        }
    }
    
}
