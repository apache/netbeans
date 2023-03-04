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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitMergeResult;
import org.netbeans.libs.git.GitMergeResult.MergeStatus;
import org.netbeans.libs.git.GitPullResult;
import org.netbeans.libs.git.GitRefUpdateResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.libs.git.GitTransportUpdate.Type;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class PullTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;
    private static final String BRANCH_NAME = "new_branch";
    private File otherWT;
    private File f, f2;
    private GitRevisionInfo masterInfo;
    private GitBranch branch;

    public PullTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
        
        otherWT = new File(workDir.getParentFile(), "repo2");
        GitClient client = getClient(otherWT);
        client.init(NULL_PROGRESS_MONITOR);
        f = new File(otherWT, "f");
        write(f, "init");
        f2 = new File(otherWT, "f2");
        write(f2, "init");
        client.add(new File[] { f, f2 }, NULL_PROGRESS_MONITOR);
        masterInfo = client.commit(new File[] { f, f2 }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        branch = client.createBranch(BRANCH_NAME, Constants.MASTER, NULL_PROGRESS_MONITOR);
        RemoteConfig cfg = new RemoteConfig(repository.getConfig(), "origin");
        cfg.addURI(new URIish(otherWT.toURI().toURL().toString()));
        cfg.update(repository.getConfig());
        repository.getConfig().save();
    }

    public void testPullNotExistingBranch () throws Exception {
        GitClient client = getClient(workDir);
        try {
            GitPullResult result = client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/idontexistbranch", NULL_PROGRESS_MONITOR);
            fail("Must fail");
        } catch (GitException.MissingObjectException ex) {
            // OK
        }
    }

    public void testPullNoLocalHead () throws Exception {
        GitClient client = getClient(workDir);
        File f = new File(workDir, "local");
        write(f, "aaa");
        add(f);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
        assertTrue(f.exists());
        GitPullResult result = client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(3, branches.size());
        assertTrue(branches.get("origin/master").isRemote());
        assertTrue(branches.get("origin/" + BRANCH_NAME).isRemote());
        assertEquals(branch.getId(), branches.get("origin/" + BRANCH_NAME).getId());
        Map<String, GitTransportUpdate> updates = result.getFetchResult();
        assertEquals(2, updates.size());
        assertUpdate(updates.get("origin/master"), "origin/master", "master", masterInfo.getRevision(), null, new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.NEW);
        assertUpdate(updates.get("origin/" + BRANCH_NAME), "origin/" + BRANCH_NAME, BRANCH_NAME, branch.getId(), null, new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.NEW);
        
        assertEquals(MergeStatus.FAST_FORWARD, result.getMergeResult().getMergeStatus());
        
        // we should be on master branch
        assertTrue(branches.get("master").isActive());
        // the old file should be deleted
        assertFalse(f.exists());
        // and replaced with a new file
        assertTrue(new File(workDir, this.f.getName()).exists());
    }

    public void testPullIntoDetached () throws Exception {
        GitClient client = getClient(workDir);
        client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        File f = new File(workDir, this.f.getName());
        write(f, "blabla");
        add(f);
        commit(f);
        client.checkoutRevision("origin/master", true, NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertTrue(branches.get(GitBranch.NO_BRANCH).isActive());
        
        String commitId = makeRemoteChange("master");
        
        GitPullResult result = client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(4, branches.size());
        assertTrue(branches.get("origin/master").isRemote());
        assertEquals(commitId, branches.get("origin/master").getId());
        assertTrue(branches.get(GitBranch.NO_BRANCH).isActive());
        assertEquals(commitId, branches.get(GitBranch.NO_BRANCH).getId());
        Map<String, GitTransportUpdate> updates = result.getFetchResult();
        assertEquals(1, updates.size());
        assertUpdate(updates.get("origin/master"), "origin/master", "master", commitId, masterInfo.getRevision(), new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.FAST_FORWARD);
        
        assertEquals(MergeStatus.FAST_FORWARD, result.getMergeResult().getMergeStatus());
        assertEquals(commitId, result.getMergeResult().getNewHead());
    }

    public void testPullChangesInSameBranch () throws Exception {
        GitClient client = getClient(workDir);
        client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        
        String commitId = makeRemoteChange("master");
        
        GitPullResult result = client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertTrue(branches.get("master").isActive());
        assertEquals(commitId, branches.get("origin/master").getId());
        assertEquals(commitId, branches.get("master").getId());
        Map<String, GitTransportUpdate> updates = result.getFetchResult();
        assertEquals(1, updates.size());
        assertUpdate(updates.get("origin/master"), "origin/master", "master", commitId, masterInfo.getRevision(), new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.FAST_FORWARD);
        assertEquals(MergeStatus.FAST_FORWARD, result.getMergeResult().getMergeStatus());
        assertEquals(commitId, result.getMergeResult().getNewHead());
    }

    public void testPullChangesInSameBranchPlusMerge () throws Exception {
        GitClient client = getClient(workDir);
        client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        File f = new File(workDir, this.f.getName());
        File f2 = new File(workDir, "f2");
        write(f2, "hi, i am new");
        add(f2);
        String localCommitId = client.commit(new File[] { f2 }, "local change", null, null, NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        
        String commitId = makeRemoteChange("master");
        
        GitPullResult result = client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertTrue(branches.get("master").isActive());
        assertEquals(commitId, branches.get("origin/master").getId());
        assertFalse(commitId.equals(branches.get("master").getId()));
        Map<String, GitTransportUpdate> updates = result.getFetchResult();
        assertEquals(1, updates.size());
        assertUpdate(updates.get("origin/master"), "origin/master", "master", commitId, masterInfo.getRevision(), new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.FAST_FORWARD);
        assertEquals(MergeStatus.MERGED, result.getMergeResult().getMergeStatus());
        assertEquals(new HashSet<String>(Arrays.asList(commitId, localCommitId)), new HashSet<String>(Arrays.asList(result.getMergeResult().getMergedCommits())));
        assertTrue(f.exists());
        assertTrue(f2.exists());
    }

    public void testPullChangesMergeConflict () throws Exception {
        GitClient client = getClient(workDir);
        client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        File f = new File(workDir, this.f.getName());
        write(f, "hi, i am new");
        add(f);
        client.commit(new File[] { f }, "local change", null, null, NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        
        String commitId = makeRemoteChange("master");
        
        GitPullResult result = client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertTrue(branches.get("master").isActive());
        assertEquals(commitId, branches.get("origin/master").getId());
        assertFalse(commitId.equals(branches.get("master").getId()));
        Map<String, GitTransportUpdate> updates = result.getFetchResult();
        assertEquals(1, updates.size());
        assertUpdate(updates.get("origin/master"), "origin/master", "master", commitId, masterInfo.getRevision(), new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.FAST_FORWARD);
        assertEquals(MergeStatus.CONFLICTING, result.getMergeResult().getMergeStatus());
        assertEquals(new HashSet<File>(Arrays.asList(f)), new HashSet<File>(result.getMergeResult().getConflicts()));
        assertEquals("<<<<<<< HEAD\nhi, i am new\n=======\nremote change\n>>>>>>> branch 'master' of " + new URIish(otherWT.toURI().toString()).toString(), read(f)); // this should be fixed in JGit
    }

    public void testPullChangesInOtherBranchPlusMerge () throws Exception {
        GitClient client = getClient(workDir);
        client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        File f = new File(workDir, this.f.getName());
        File f2 = new File(workDir, "f2");
        write(f2, "hi, i am new");
        add(f2);
        String localCommitId = client.commit(new File[] { f2 }, "local change", null, null, NULL_PROGRESS_MONITOR).getRevision();
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        
        String commitId = makeRemoteChange(BRANCH_NAME);
        
        GitPullResult result = client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/" + BRANCH_NAME, NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertTrue(branches.get("master").isActive());
        assertEquals(commitId, branches.get("origin/" + BRANCH_NAME).getId());
        assertFalse(commitId.equals(branches.get("master").getId()));
        assertFalse(localCommitId.equals(branches.get("master").getId()));
        Map<String, GitTransportUpdate> updates = result.getFetchResult();
        assertEquals(1, updates.size());
        assertUpdate(updates.get("origin/" + BRANCH_NAME), "origin/" + BRANCH_NAME, BRANCH_NAME, commitId, branch.getId(), new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.FAST_FORWARD);
        assertEquals(MergeStatus.MERGED, result.getMergeResult().getMergeStatus());
        assertEquals(new HashSet<String>(Arrays.asList(commitId, localCommitId)), new HashSet<String>(Arrays.asList(result.getMergeResult().getMergedCommits())));
        assertTrue(f.exists());
        assertTrue(f2.exists());
    }

    public void testPullFailOnLocalChanges () throws Exception {
        GitClient client = getClient(workDir);
        client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        File f = new File(workDir, this.f.getName());
        write(f, "local change");
        add(f);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        
        makeRemoteChange("master");
        try {
            GitPullResult result = client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
            fail("Should fail");
        } catch (GitException.CheckoutConflictException ex) {
            // OK
        }
        client.reset("master", GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        File f2 = new File(workDir, "f2");
        write(f2, "hi, i am new");
        add(f2);
        GitPullResult result = client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.FAST_FORWARD, result.getMergeResult().getMergeStatus());
        assertStatus(client.getStatus(new File[] { f2 }, NULL_PROGRESS_MONITOR), workDir, f2, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
    }

    public void testPullCommitMessages () throws Exception {
        GitClient client = getClient(workDir);
        client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        File f = new File(workDir, "localFile");
        
        makeLocalChange(f, "1");
        makeRemoteChange("master");
        GitPullResult result = client.pull(otherWT.toURI().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        assertEquals(GitMergeResult.MergeStatus.MERGED, result.getMergeResult().getMergeStatus());
        assertEquals("Merge branch 'master' of " + new URIish(otherWT.toURI().toString()).toString(), client.log(result.getMergeResult().getNewHead(), NULL_PROGRESS_MONITOR).getFullMessage());
        
        makeLocalChange(f, "2");
        makeRemoteChange("master", "2");
        result = client.pull("origin", Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), "origin/master", NULL_PROGRESS_MONITOR);
        assertEquals(GitMergeResult.MergeStatus.MERGED, result.getMergeResult().getMergeStatus());
        assertEquals("Merge branch 'master' of " + new URIish(otherWT.toURI().toString()).toString(), client.log(result.getMergeResult().getNewHead(), NULL_PROGRESS_MONITOR).getFullMessage());
    }

    private void setupRemoteSpec (String remote, String fetchSpec) throws URISyntaxException, IOException {
        RemoteConfig cfg = new RemoteConfig(repository.getConfig(), remote);
        cfg.addFetchRefSpec(new RefSpec(fetchSpec));
        cfg.update(repository.getConfig());
        repository.getConfig().save();
    }

    private void assertUpdate(GitTransportUpdate update, String localName, String remoteName, String newObjectId, String oldObjectId, String remoteUri, Type type, GitRefUpdateResult result) {
        assertEquals(localName, update.getLocalName());
        assertEquals(remoteName, update.getRemoteName());
        assertEquals(newObjectId, update.getNewObjectId());
        assertEquals(oldObjectId, update.getOldObjectId());
        assertEquals(remoteUri, update.getRemoteUri());
        assertEquals(type, update.getType());
        assertEquals(result, update.getResult());
    }

    private String makeRemoteChange (String branch) throws Exception {
        return makeRemoteChange(branch, "remote change");
    }
    
    private String makeRemoteChange (String branch, String content) throws Exception {
        GitClient client = getClient(otherWT);
        client.checkoutRevision(branch, true, NULL_PROGRESS_MONITOR);
        write(f, content);
        File[] roots = new File[] { f };
        client.add(roots, NULL_PROGRESS_MONITOR);
        return client.commit(roots, "remote change", null, null, NULL_PROGRESS_MONITOR).getRevision();
    }

    private String makeLocalChange (File f, String content) throws Exception {
        GitClient client = getClient(workDir);
        write(f, content);
        File[] roots = new File[] { f };
        client.add(roots, NULL_PROGRESS_MONITOR);
        return client.commit(roots, "local change: " + content, null, null, NULL_PROGRESS_MONITOR).getRevision();
    }
}
