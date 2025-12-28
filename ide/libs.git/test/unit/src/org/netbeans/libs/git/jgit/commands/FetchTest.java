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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRefUpdateResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.libs.git.GitTransportUpdate.Type;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.jgit.DelegatingProgressMonitor;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class FetchTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;
    private static final String BRANCH_NAME = "new_branch";
    private File otherWT;
    private File f;
    private GitRevisionInfo masterInfo;
    private GitBranch branch;

    public FetchTest (String testName) throws IOException {
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
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        masterInfo = client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        branch = client.createBranch(BRANCH_NAME, Constants.MASTER, NULL_PROGRESS_MONITOR);
        RemoteConfig cfg = new RemoteConfig(repository.getConfig(), "origin");
        cfg.addURI(new URIish(otherWT.toURI().toURL().toString()));
        cfg.update(repository.getConfig());
        repository.getConfig().save();
    }

    public void testFetchAllBranches () throws Exception {
        setupRemoteSpec("origin", "+refs/heads/*:refs/remotes/origin/*");
        GitClient client = getClient(workDir);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
        Map<String, GitTransportUpdate> updates = client.fetch("origin", NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertTrue(branches.get("origin/master").isRemote());
        assertTrue(branches.get("origin/" + BRANCH_NAME).isRemote());
        assertEquals(branch.getId(), branches.get("origin/" + BRANCH_NAME).getId());
        assertEquals(2, updates.size());
        assertUpdate(updates.get("origin/master"), "origin/master", "master", masterInfo.getRevision(), null, new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.NEW);
        assertUpdate(updates.get("origin/" + BRANCH_NAME), "origin/" + BRANCH_NAME, BRANCH_NAME, branch.getId(), null, new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.NEW);
    }

    public void testFetchAllBranchesUrl () throws Exception {
        GitClient client = getClient(workDir);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
        Map<String, GitTransportUpdate> updates = client.fetch(otherWT.toURI().toURL().toString(), Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertTrue(branches.get("origin/master").isRemote());
        assertTrue(branches.get("origin/" + BRANCH_NAME).isRemote());
        assertEquals(branch.getId(), branches.get("origin/" + BRANCH_NAME).getId());
        assertEquals(2, updates.size());
        assertUpdate(updates.get("origin/master"), "origin/master", "master", masterInfo.getRevision(), null, new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.NEW);
        assertUpdate(updates.get("origin/" + BRANCH_NAME), "origin/" + BRANCH_NAME, BRANCH_NAME, branch.getId(), null, new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.NEW);
    }
    
    public void testFetchNamedBranches () throws Exception {
        setupRemoteSpec("origin", "+refs/heads/master:refs/remotes/origin/master");
        setupRemoteSpec("origin", "+refs/heads/" + BRANCH_NAME + ":refs/remotes/origin/" + BRANCH_NAME);
        GitClient client = getClient(workDir);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
        Map<String, GitTransportUpdate> updates = client.fetch("origin", NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertTrue(branches.get("origin/master").isRemote());
        assertTrue(branches.get("origin/" + BRANCH_NAME).isRemote());
        assertEquals(branch.getId(), branches.get("origin/" + BRANCH_NAME).getId());
        assertEquals(2, updates.size());
        assertUpdate(updates.get("origin/master"), "origin/master", "master", masterInfo.getRevision(), null, new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.NEW);
        assertUpdate(updates.get("origin/" + BRANCH_NAME), "origin/" + BRANCH_NAME, BRANCH_NAME, branch.getId(), null, new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.NEW);
    }
    
    public void testFetchMaster () throws Exception {
        setupRemoteSpec("origin", "+refs/heads/master:refs/remotes/origin/master");
        GitClient client = getClient(workDir);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
        Map<String, GitTransportUpdate> updates = client.fetch("origin", NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(1, branches.size());
        assertTrue(branches.get("origin/master").isRemote());
        assertEquals(masterInfo.getRevision(), branches.get("origin/master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("origin/master"), "origin/master", "master", masterInfo.getRevision(), null, new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.NEW);
    }
    
    public void testFetchNothingToFetch () throws Exception {
        GitClient client = getClient(workDir);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
        try {
            client.fetch("origin", NULL_PROGRESS_MONITOR);
            fail("Should fail, no refspec given");
        } catch (GitException ex) {
            assertEquals("Nothing to fetch.", ex.getMessage());
        }
    }
    
    public void testFetchMasterExplicitely () throws Exception {
        GitClient client = getClient(workDir);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
        setupRemoteSpec("origin", "+refs/heads/*:refs/remotes/origin/*");
        Map<String, GitTransportUpdate> updates = client.fetch("origin", Arrays.asList(new String[] { "+refs/heads/master:refs/remotes/origin/master" }), NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(1, branches.size());
        assertTrue(branches.get("origin/master").isRemote());
        assertEquals(masterInfo.getRevision(), branches.get("origin/master").getId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get("origin/master"), "origin/master", "master", masterInfo.getRevision(), null, new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.NEW);
    }
    
    public void testFetchAllExplicitely () throws Exception {
        GitClient client = getClient(workDir);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
        Map<String, GitTransportUpdate> updates = client.fetch("origin", Arrays.asList(new String[] { "+refs/heads/master:refs/remotes/origin/master", "+refs/heads/" + BRANCH_NAME + ":refs/remotes/origin/" + BRANCH_NAME }), NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertTrue(branches.get("origin/master").isRemote());
        assertTrue(branches.get("origin/" + BRANCH_NAME).isRemote());
        assertEquals(branch.getId(), branches.get("origin/" + BRANCH_NAME).getId());
        assertEquals(2, updates.size());
        assertUpdate(updates.get("origin/master"), "origin/master", "master", masterInfo.getRevision(), null, new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.NEW);
        assertUpdate(updates.get("origin/" + BRANCH_NAME), "origin/" + BRANCH_NAME, BRANCH_NAME, branch.getId(), null, new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.NEW);
    }

//    enable when the fixed in jgit - see the previous test
//    public void testFetchDeleteBranch () throws Exception {
//        GitClient client = getClient(workDir);
//        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
//        assertEquals(0, branches.size());
//        setupRemoteSpec("origin", "+refs/heads/" + BRANCH_NAME + ":refs/remotes/origin/" + BRANCH_NAME);
//        Map<String, GitTransportUpdate> updates = client.fetch("origin", Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), NULL_PROGRESS_MONITOR);
//        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
//        assertEquals(2, branches.size());
//        
//        // delete the remote branch
//        File branchFile = new File(otherWT, ".git/refs/heads/" + BRANCH_NAME);
//        assertEquals(2, getClient(otherWT).getBranches(false, NULL_PROGRESS_MONITOR).size());
//        Thread.sleep(100);
//        branchFile.delete();
//        assertEquals(1, getClient(otherWT).getBranches(false, NULL_PROGRESS_MONITOR).size());
//        
//        try {
//            client.fetch("origin", NULL_PROGRESS_MONITOR);
//            fail();
//        } catch (GitException ex) {
//            assertEquals("Remote does not have refs/heads/new_branch available for fetch.", ex.getMessage());
//        }
//        
//        updates = client.fetch("origin", Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*" }), NULL_PROGRESS_MONITOR);
//        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
//        assertEquals(1, branches.size());
//        assertEquals(1, updates.size());
//        assertUpdate(updates.get("origin/" + BRANCH_NAME), "origin/" + BRANCH_NAME, BRANCH_NAME, null, branch.getId(), new URIish(otherWT.toURI().toURL()).toString(), Type.BRANCH, GitRefUpdateResult.FORCED);
//    }
    
    public void testFetchTags () throws Exception {
        setupRemoteSpec("origin", "+refs/heads/master:refs/remotes/origin/master");
        GitClient client = getClient(workDir);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
        client.fetch("origin", NULL_PROGRESS_MONITOR);
        
        Repository repo = new RepositoryBuilder().setGitDir(new File(otherWT, ".git")).build();
        TagCommand cmd = new Git(repo).tag();
        cmd.setMessage("new tag message");
        cmd.setName("new.tag");
        cmd.setObjectId(new RevWalk(repo).parseCommit(repo.resolve(masterInfo.getRevision())));
        Ref ref = cmd.call();
        RevTag tag = new RevWalk(repo).parseTag(ref.getObjectId());
        
        Map<String, GitTransportUpdate> updates = client.fetch("origin", NULL_PROGRESS_MONITOR);
        Ref tagRef = repository.getRefDatabase().findRef(tag.getTagName());
        assertEquals(tag.getId(), tagRef.getTarget().getObjectId());
        assertEquals(1, updates.size());
        assertUpdate(updates.get(tag.getTagName()), tag.getTagName(), tag.getTagName(), tag.getId().getName(), null, new URIish(otherWT.toURI().toURL()).toString(), Type.TAG, GitRefUpdateResult.NEW);
    }

    public void testFetchProgress () throws Exception {
        setupRemoteSpec("origin", "+refs/heads/*:refs/remotes/origin/*");
        GitClient client = getClient(workDir);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
        final StringBuilder sb = new StringBuilder();
        ProgressMonitor pm = new ProgressMonitor.DefaultProgressMonitor() {

            @Override
            public void beginTask (String taskName, int totalWorkUnits) {
                sb.append("START: ").append(taskName);
                if (totalWorkUnits > 0) {
                    sb.append(" - ").append(totalWorkUnits).append("\n");
                }
            }

            @Override
            public void updateTaskState (int completed) {
                sb.append("UPDATE: ").append(completed).append("\n");
            }

            @Override
            public void endTask () {
                sb.append("ENDED\n");
            }
            
        };
        client.fetch("origin", pm);
        String messages = sb.toString();
        assertTrue(messages, messages.contains("START: remote: Finding sources - "));
        assertTrue(messages, messages.contains("START: remote: Getting sizes - "));
        assertTrue(messages, messages.contains("START: remote: Compressing objects - "));
        assertTrue(messages, messages.contains("START: Receiving objects - "));
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
}
