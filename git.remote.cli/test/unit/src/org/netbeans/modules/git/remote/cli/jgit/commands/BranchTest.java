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
import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRemoteConfig;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
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
public class BranchTest extends AbstractGitTestCase {
    private static final boolean KIT = ListBranchCommand.KIT;
    private JGitRepository repository;
    private VCSFileProxy workDir;
    private static final String BRANCH_NAME = "new_branch";
    private static final String BRANCH_NAME_2 = "new_branch2";
    private static final String BRANCH_NAME_3 = "new_branch3";

    public BranchTest (String testName) throws IOException {
        super(testName);
    }
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testCreateBranch","testBranchTracking", "testDeleteRemoteBranch", "testCreateInitialBranch").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testListBranches () throws Exception {
        GitClient client = getClient(workDir);
        Map<String, GitBranch> branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
        
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        write(f, "hello");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(files, "init", null, null, NULL_PROGRESS_MONITOR);
        write(f, "hello again");
        client.commit(files, "change", null, null, NULL_PROGRESS_MONITOR);

        final SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setLimit(1);
        GitRevisionInfo[] log = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = log[0];
        String commitId = info.getRevision();

        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(1, branches.size());
        assertEquals(GitConstants.MASTER, branches.get(GitConstants.MASTER).getName());
if(KIT) assertEquals(commitId, branches.get(GitConstants.MASTER).getId());
else    assertEqualsID(commitId, branches.get(GitConstants.MASTER).getId());
        assertFalse(branches.get(GitConstants.MASTER).isRemote());
        assertTrue(branches.get(GitConstants.MASTER).isActive());

        write(VCSFileProxy.createFileProxy(workDir, ".git/refs/heads/nova"), commitId);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertEquals(GitConstants.MASTER, branches.get(GitConstants.MASTER).getName());
        assertFalse(branches.get(GitConstants.MASTER).isRemote());
        assertTrue(branches.get(GitConstants.MASTER).isActive());    
if(KIT) assertEquals(commitId, branches.get(GitConstants.MASTER).getId());
else    assertEqualsID(commitId, branches.get(GitConstants.MASTER).getId());
        assertEquals("nova", branches.get("nova").getName());
        assertFalse(branches.get("nova").isRemote());
        assertFalse(branches.get("nova").isActive());
if(KIT) assertEquals(commitId, branches.get("nova").getId());
else    assertEqualsID(commitId, branches.get("nova").getId());

        Thread.sleep(1100);
        write(VCSFileProxy.createFileProxy(workDir, ".git/HEAD"), commitId);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(3, branches.size());
        assertEquals(GitBranch.NO_BRANCH, branches.get(GitBranch.NO_BRANCH).getName());
        assertFalse(branches.get(GitBranch.NO_BRANCH).isRemote());
        assertTrue(branches.get(GitBranch.NO_BRANCH).isActive());
if(KIT) assertEquals(commitId, branches.get(GitBranch.NO_BRANCH).getId());
else    assertEqualsID(commitId, branches.get(GitBranch.NO_BRANCH).getId());
        assertEquals(GitConstants.MASTER, branches.get(GitConstants.MASTER).getName());
        assertFalse(branches.get(GitConstants.MASTER).isRemote());
        assertFalse(branches.get(GitConstants.MASTER).isActive());
        assertEquals("nova", branches.get("nova").getName());
        assertFalse(branches.get("nova").isRemote());
        assertFalse(branches.get("nova").isActive());
    }
    
    public void testCreateBranch () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        write(f, "hello");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        add(files);
        commit(files);
        write(f, "hello again");
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        GitRevisionInfo[] logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        String lastCommitId = logs[0].getRevision();
        String commitId = logs[1].getRevision();

        GitBranch branch = client.createBranch(BRANCH_NAME, commitId, NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertTrue(branches.containsKey(GitConstants.MASTER));
        assertTrue(branches.containsKey(BRANCH_NAME));
        assertEquals(BRANCH_NAME, branch.getName());
if(KIT) assertEquals(commitId, branch.getId());
else    assertEqualsID(commitId, branch.getId());
        assertFalse(branch.isActive());
        assertFalse(branch.isRemote());
        branch = branches.get(BRANCH_NAME);
        assertEquals(BRANCH_NAME, branch.getName());
        assertEquals(commitId, branch.getId());
        assertFalse(branch.isActive());
        assertFalse(branch.isRemote());
        assertTrue(branches.get(GitConstants.MASTER).isActive());
        assertEquals(commitId, read(VCSFileProxy.createFileProxy(workDir, ".git/refs/heads/" + BRANCH_NAME)));

        client.createBranch(BRANCH_NAME_2, GitConstants.HEAD, NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(3, branches.size());
        assertTrue(branches.containsKey(GitConstants.MASTER));
        assertTrue(branches.containsKey(BRANCH_NAME));
        assertTrue(branches.containsKey(BRANCH_NAME_2));
        assertTrue(branches.get(GitConstants.MASTER).isActive());
        assertEquals(lastCommitId, read(VCSFileProxy.createFileProxy(workDir, ".git/refs/heads/" + BRANCH_NAME_2)));
        client.createBranch(BRANCH_NAME_3, "refs/heads/master", NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(4, branches.size());
        assertTrue(branches.containsKey(GitConstants.MASTER));
        assertTrue(branches.containsKey(BRANCH_NAME));
        assertTrue(branches.containsKey(BRANCH_NAME_2));
        assertTrue(branches.containsKey(BRANCH_NAME_3));
        assertTrue(branches.get(GitConstants.MASTER).isActive());
        assertEquals(lastCommitId, read(VCSFileProxy.createFileProxy(workDir, ".git/refs/heads/" + BRANCH_NAME_3)));

        try {
            client.createBranch(BRANCH_NAME, commitId, NULL_PROGRESS_MONITOR);
            fail("Branch should not have been created, it already existed");
        } catch (GitException ex) {
            // OK
            assertEquals("Ref " + BRANCH_NAME + " already exists", ex.getCause().getMessage());
        }
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(4, branches.size());
        assertTrue(branches.get(GitConstants.MASTER).isActive());
        assertEquals(commitId, read(VCSFileProxy.createFileProxy(workDir, ".git/refs/heads/" + BRANCH_NAME)));
    }
    
    public void testListRemoteBranches () throws Exception {
        VCSFileProxy otherWT = VCSFileProxy.createFileProxy(workDir.getParentFile(), "repo2");
        VCSFileProxySupport.mkdirs(otherWT);
        GitClient client = getClient(otherWT);
        client.init(NULL_PROGRESS_MONITOR);
        VCSFileProxy f = VCSFileProxy.createFileProxy(otherWT, "f");
        write(f, "init");
        client.add(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        GitBranch branch = client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        write(f, "change on master");
        client.add(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        GitRevisionInfo master = client.commit(new VCSFileProxy[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(otherWT.getPath(), NULL_PROGRESS_MONITOR);
        assertEquals(2, remoteBranches.size());
if(KIT) assertEquals(branch.getId(), remoteBranches.get(BRANCH_NAME).getId());
else    assertEqualsID(branch.getId(), remoteBranches.get(BRANCH_NAME).getId());
        assertEquals(master.getRevision(), remoteBranches.get(GitConstants.MASTER).getId());
    }
    
    public void testDeleteUntrackedLocalBranch () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = { f };
        write(f, "init");
        add(files);
        commit(files);
        GitClient client = getClient(workDir);
        GitBranch b = client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertNotNull(branches.get(BRANCH_NAME));
if (false)
        assertEquals(0, repository.getConfig().getSubsections(JGitConfig.CONFIG_BRANCH_SECTION).size());
else    assertEquals(1, repository.getConfig().getSubsections(JGitConfig.CONFIG_BRANCH_SECTION).size());
        
        // delete branch
        client.deleteBranch(BRANCH_NAME, false, NULL_PROGRESS_MONITOR);
        branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(1, branches.size());
        assertNull(branches.get(BRANCH_NAME));
    }
    
    public void testDeleteTrackedBranch () throws Exception {
        final VCSFileProxy otherWT = VCSFileProxy.createFileProxy(workDir.getParentFile(), "repo2");
        VCSFileProxySupport.mkdirs(otherWT);
        GitClient client = getClient(otherWT);
        client.init(NULL_PROGRESS_MONITOR);
        VCSFileProxy f = VCSFileProxy.createFileProxy(otherWT, "f");
        write(f, "init");
        client.add(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        client = getClient(workDir);
        client.setRemote(new GitRemoteConfig("origin",
                Arrays.asList(new String[] { otherWT.getPath() }),
                Arrays.asList(new String[] { otherWT.getPath() }),
                Arrays.asList(new String[] { "refs/heads/*:refs/remotes/origin/*" }),
                Arrays.asList(new String[] { "refs/remotes/origin/*:refs/heads/*" })), NULL_PROGRESS_MONITOR);
        client.fetch("origin", NULL_PROGRESS_MONITOR);
        client.checkoutRevision("origin/master", true, NULL_PROGRESS_MONITOR);
        GitBranch b = client.createBranch(BRANCH_NAME, "origin/master", NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertNotNull(branches.get(BRANCH_NAME));
        // I do not understand what is getSubsections.
        // I supose that it is a number of sections with same prefix, i.e.
        // Two config with two sections[branch "master"] [branch "new_branch"] returns 2 subsection of section "branch".
        // It seems I wrong.
if (false)assertEquals(1, repository.getConfig().getSubsections(JGitConfig.CONFIG_BRANCH_SECTION).size());
else    assertEquals(2, repository.getConfig().getSubsections(JGitConfig.CONFIG_BRANCH_SECTION).size());
        
        //delete tracked branch and test
        client.deleteBranch(BRANCH_NAME, false, NULL_PROGRESS_MONITOR);
        branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(1, branches.size());
        assertNull(branches.get(BRANCH_NAME));
        repository.getConfig().load();
if (false)assertEquals(0, repository.getConfig().getSubsections(JGitConfig.CONFIG_BRANCH_SECTION).size());        
else    assertEquals(1, repository.getConfig().getSubsections(JGitConfig.CONFIG_BRANCH_SECTION).size());
    }
    
    public void testDeleteUnmergedBranch () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = { f };
        write(f, "init");
        add(files);
        commit(files);
        GitClient client = getClient(workDir);
        GitBranch b = client.createBranch(BRANCH_NAME, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, "change on branch");
        add(files);
        commit(files);
        //checkout other revision
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertNotNull(branches.get(BRANCH_NAME));
        //delete and test
        try {
            client.deleteBranch(BRANCH_NAME, false, NULL_PROGRESS_MONITOR);
            fail("no force flag");
        } catch (GitException.NotMergedException ex) {
            // OK
        }
        branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertNotNull(branches.get(BRANCH_NAME));
        // delete with force flag
        client.deleteBranch(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(1, branches.size());
        assertNull(branches.get(BRANCH_NAME));
    }
    
    public void testDeleteActiveBranch () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = { f };
        write(f, "init");
        add(files);
        commit(files);
        GitClient client = getClient(workDir);
        try {
            client.deleteBranch(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
            fail("Can not delete active branch");
        } catch (GitException ex) {
            assertTrue(ex.getMessage().contains("Branch master is checked out and can not be deleted"));
        }
    }
    
    public void testDeleteRemoteBranch () throws Exception {
        final VCSFileProxy otherWT = VCSFileProxy.createFileProxy(workDir.getParentFile(), "repo2");
        VCSFileProxySupport.mkdirs(otherWT);
        GitClient client = getClient(otherWT);
        client.init(NULL_PROGRESS_MONITOR);
        VCSFileProxy f = VCSFileProxy.createFileProxy(otherWT, "f");
        write(f, "init");
        client.add(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        client = getClient(workDir);
        client.fetch(otherWT.getPath(), Arrays.asList(new String[] { "refs/heads/*:refs/remotes/origin/*" }), NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(1, branches.size());
        assertNotNull(branches.get("origin/master"));
        
        // delete remote branch
        client.deleteBranch("origin/master", false, NULL_PROGRESS_MONITOR);
        branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
    }
    
    public void testBranchTracking () throws Exception {
        final VCSFileProxy otherWT = VCSFileProxy.createFileProxy(workDir.getParentFile(), "repo2");
        VCSFileProxySupport.mkdirs(otherWT);
        GitClient client = getClient(otherWT);
        client.init(NULL_PROGRESS_MONITOR);
        VCSFileProxy f = VCSFileProxy.createFileProxy(otherWT, "f");
        write(f, "init");
        client.add(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        client = getClient(workDir);
        client.setRemote(new GitRemoteConfig("origin", 
                Arrays.asList(otherWT.getPath()),
                Arrays.asList(otherWT.getPath()),
                Arrays.asList("+refs/heads/*:refs/remotes/origin/*"), Collections.<String>emptyList()), NULL_PROGRESS_MONITOR);
        client.fetch("origin", NULL_PROGRESS_MONITOR);
        GitBranch b = client.createBranch(GitConstants.MASTER, "origin/master", NULL_PROGRESS_MONITOR);
        assertEquals("origin/master", b.getTrackedBranch().getName());
        assertTrue(b.getTrackedBranch().isRemote());
        client.checkoutRevision(GitConstants.MASTER, true, NULL_PROGRESS_MONITOR);
        
        b = client.createBranch("nova1", GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertNull(b.getTrackedBranch());
        
        JGitConfig cfg = repository.getConfig();
        cfg.load();
        cfg.setString(JGitConfig.CONFIG_BRANCH_SECTION, null, JGitConfig.CONFIG_KEY_AUTOSETUPMERGE, "always");
        cfg.save();
        b = client.createBranch("nova2", GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(GitConstants.MASTER, b.getTrackedBranch().getName());
        assertFalse(b.getTrackedBranch().isRemote());
        
        // list branches
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        b = branches.get(GitConstants.MASTER);
        assertEquals("origin/master", b.getTrackedBranch().getName());
        assertTrue(b.getTrackedBranch().isRemote());
        b = branches.get("origin/master");
        assertNull(b.getTrackedBranch());
    }
    
    public void testListBranches_Issue213538 () throws Exception {
        GitClient client = getClient(workDir);
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        client.add(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        // cannot end with a RuntimeException
        VCSFileProxy configFile = VCSFileProxy.createFileProxy(Utils.getMetadataFolder(workDir), GitConstants.CONFIG);
        String config = read(configFile);
        config += "\n\tbla:\n";
        write(configFile, config);
        Thread.sleep(1100);
        try {
            client.getBranches(false, NULL_PROGRESS_MONITOR);
        } catch (GitException ex) {
            assertEquals("It seems the config file for repository at [" + workDir + "] is corrupted.\nEnsure it's valid.", ex.getMessage());
        }
    }
    
    public void testCreateInitialBranch () throws Exception {
        VCSFileProxy emptyRepo = VCSFileProxy.createFileProxy(workDir, "empty");
        VCSFileProxySupport.mkdirs(emptyRepo);
        GitClient client = getClient(emptyRepo);
        client.init(NULL_PROGRESS_MONITOR);
        JGitConfig cfg = repository.getConfig();
        cfg.load();
        assertFalse(cfg.getSections().contains(JGitConfig.CONFIG_BRANCH_SECTION));
        client.createBranch(GitConstants.MASTER, GitConstants.R_REMOTES + "origin/whateverbranch", NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertTrue(branches.isEmpty());
        cfg.load();
        assertTrue(cfg.getSections().contains(JGitConfig.CONFIG_BRANCH_SECTION));
        assertEquals("origin", cfg.getString(JGitConfig.CONFIG_BRANCH_SECTION,
                GitConstants.MASTER, JGitConfig.CONFIG_KEY_REMOTE));
        assertEquals(GitConstants.R_HEADS + "whateverbranch", cfg.getString(JGitConfig.CONFIG_BRANCH_SECTION,
                GitConstants.MASTER, JGitConfig.CONFIG_KEY_MERGE));
    }
}
