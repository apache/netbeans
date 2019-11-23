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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.jgit.Utils;

/**
 *
 * @author ondra
 */
public class BranchTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;
    private static final String BRANCH_NAME = "new_branch";
    private static final String BRANCH_NAME_2 = "new_branch2";
    private static final String BRANCH_NAME_3 = "new_branch3";

    public BranchTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testListBranches () throws Exception {
        GitClient client = getClient(workDir);
        Map<String, GitBranch> branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
        
        File f = new File(workDir, "file");
        write(f, "hello");
        File[] files = new File[] { f };
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(files, "init", null, null, NULL_PROGRESS_MONITOR);
        write(f, "hello again");
        client.commit(files, "change", null, null, NULL_PROGRESS_MONITOR);

        Iterator<RevCommit> it = new Git(repository).log().call().iterator();
        RevCommit info = it.next();
        String commitId = info.getId().getName();

        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(1, branches.size());
        assertEquals("master", branches.get("master").getName());
        assertEquals(commitId, branches.get("master").getId());
        assertFalse(branches.get("master").isRemote());
        assertTrue(branches.get("master").isActive());

        write(new File(workDir, ".git/refs/heads/nova"), commitId);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertEquals("master", branches.get("master").getName());
        assertFalse(branches.get("master").isRemote());
        assertTrue(branches.get("master").isActive());
        assertEquals(commitId, branches.get("master").getId());
        assertEquals("nova", branches.get("nova").getName());
        assertFalse(branches.get("nova").isRemote());
        assertFalse(branches.get("nova").isActive());
        assertEquals(commitId, branches.get("nova").getId());

        Thread.sleep(1100);
        write(new File(workDir, ".git/HEAD"), commitId);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(3, branches.size());
        assertEquals(GitBranch.NO_BRANCH, branches.get(GitBranch.NO_BRANCH).getName());
        assertFalse(branches.get(GitBranch.NO_BRANCH).isRemote());
        assertTrue(branches.get(GitBranch.NO_BRANCH).isActive());
        assertEquals(commitId, branches.get(GitBranch.NO_BRANCH).getId());
        assertEquals("master", branches.get("master").getName());
        assertFalse(branches.get("master").isRemote());
        assertFalse(branches.get("master").isActive());
        assertEquals("nova", branches.get("nova").getName());
        assertFalse(branches.get("nova").isRemote());
        assertFalse(branches.get("nova").isActive());
    }
    
    public void testCreateBranch () throws Exception {
        File f = new File(workDir, "file");
        write(f, "hello");
        File[] files = new File[] { f };
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
        assertTrue(branches.containsKey("master"));
        assertTrue(branches.containsKey(BRANCH_NAME));
        assertEquals(BRANCH_NAME, branch.getName());
        assertEquals(commitId, branch.getId());
        assertFalse(branch.isActive());
        assertFalse(branch.isRemote());
        branch = branches.get(BRANCH_NAME);
        assertEquals(BRANCH_NAME, branch.getName());
        assertEquals(commitId, branch.getId());
        assertFalse(branch.isActive());
        assertFalse(branch.isRemote());
        assertTrue(branches.get("master").isActive());
        assertEquals(commitId, read(new File(workDir, ".git/refs/heads/" + BRANCH_NAME)));

        client.createBranch(BRANCH_NAME_2, Constants.HEAD, NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(3, branches.size());
        assertTrue(branches.containsKey("master"));
        assertTrue(branches.containsKey(BRANCH_NAME));
        assertTrue(branches.containsKey(BRANCH_NAME_2));
        assertTrue(branches.get("master").isActive());
        assertEquals(lastCommitId, read(new File(workDir, ".git/refs/heads/" + BRANCH_NAME_2)));
        client.createBranch(BRANCH_NAME_3, "refs/heads/master", NULL_PROGRESS_MONITOR);
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(4, branches.size());
        assertTrue(branches.containsKey("master"));
        assertTrue(branches.containsKey(BRANCH_NAME));
        assertTrue(branches.containsKey(BRANCH_NAME_2));
        assertTrue(branches.containsKey(BRANCH_NAME_3));
        assertTrue(branches.get("master").isActive());
        assertEquals(lastCommitId, read(new File(workDir, ".git/refs/heads/" + BRANCH_NAME_3)));

        try {
            client.createBranch(BRANCH_NAME, commitId, NULL_PROGRESS_MONITOR);
            fail("Branch should not have been created, it already existed");
        } catch (GitException ex) {
            // OK
            assertEquals("Ref " + BRANCH_NAME + " already exists", ex.getCause().getMessage());
        }
        branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(4, branches.size());
        assertTrue(branches.get("master").isActive());
        assertEquals(commitId, read(new File(workDir, ".git/refs/heads/" + BRANCH_NAME)));
    }

    public void testCreateBranchWithRebase () throws Exception {
        final File otherWT = new File(workDir.getParentFile(), "repo2");
        GitClient client = getClient(otherWT);
        client.init(NULL_PROGRESS_MONITOR);
        File f = new File(otherWT, "f");
        write(f, "init");
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);

        client = getClient(workDir);
        client.setRemote(new GitRemoteConfig("origin",
                Arrays.asList(new String[] { otherWT.getAbsolutePath() }),
                Arrays.asList(new String[] { otherWT.getAbsolutePath() }),
                Arrays.asList(new String[] { "refs/heads/*:refs/remotes/origin/*" }),
                Arrays.asList(new String[] { "refs/remotes/origin/*:refs/heads/*" })), NULL_PROGRESS_MONITOR);
        client.fetch("origin", NULL_PROGRESS_MONITOR);

        StoredConfig config = repository.getConfig();
        config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOSETUPREBASE, ConfigConstants.CONFIG_KEY_NEVER);
        config.save();

        GitBranch b = client.createBranch(BRANCH_NAME, "origin/master", NULL_PROGRESS_MONITOR);
        assertFalse(repository.getConfig().getBoolean(ConfigConstants.CONFIG_BRANCH_SECTION, BRANCH_NAME, ConfigConstants.CONFIG_KEY_REBASE, false));
        client.deleteBranch(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);

        config = repository.getConfig();
        config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOSETUPREBASE, ConfigConstants.CONFIG_KEY_REMOTE);
        config.save();

        b = client.createBranch(BRANCH_NAME, "origin/master", NULL_PROGRESS_MONITOR);
        assertTrue(repository.getConfig().getBoolean(ConfigConstants.CONFIG_BRANCH_SECTION, BRANCH_NAME, ConfigConstants.CONFIG_KEY_REBASE, false));
    }
    
    public void testFileProtocolFails () throws Exception {
        try {
            Transport.open(repository, new URIish(workDir.toURI().toURL()));
            fail("Workaround not needed, fix ListRemoteBranchesCommand - Transport.open(String) to Transport.open(URL)");
        } catch (NotSupportedException ex) {
            
        }
    }
    
    public void testListRemoteBranches () throws Exception {
        File otherWT = new File(workDir.getParentFile(), "repo2");
        GitClient client = getClient(otherWT);
        client.init(NULL_PROGRESS_MONITOR);
        File f = new File(otherWT, "f");
        write(f, "init");
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        GitBranch branch = client.createBranch(BRANCH_NAME, "master", NULL_PROGRESS_MONITOR);
        write(f, "change on master");
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        GitRevisionInfo master = client.commit(new File[] { f }, "change on master", null, null, NULL_PROGRESS_MONITOR);
        
        Map<String, GitBranch> remoteBranches = getClient(workDir).listRemoteBranches(otherWT.getAbsolutePath(), NULL_PROGRESS_MONITOR);
        assertEquals(2, remoteBranches.size());
        assertEquals(branch.getId(), remoteBranches.get(BRANCH_NAME).getId());
        assertEquals(master.getRevision(), remoteBranches.get("master").getId());
    }
    
    public void testDeleteUntrackedLocalBranch () throws Exception {
        File f = new File(workDir, "f");
        File[] files = { f };
        write(f, "init");
        add(files);
        commit(files);
        GitClient client = getClient(workDir);
        GitBranch b = client.createBranch(BRANCH_NAME, "master", NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertNotNull(branches.get(BRANCH_NAME));
        assertEquals(0, repository.getConfig().getSubsections(ConfigConstants.CONFIG_BRANCH_SECTION).size());
        
        // delete branch
        client.deleteBranch(BRANCH_NAME, false, NULL_PROGRESS_MONITOR);
        branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(1, branches.size());
        assertNull(branches.get(BRANCH_NAME));
    }
    
    public void testDeleteTrackedBranch () throws Exception {
        final File otherWT = new File(workDir.getParentFile(), "repo2");
        GitClient client = getClient(otherWT);
        client.init(NULL_PROGRESS_MONITOR);
        File f = new File(otherWT, "f");
        write(f, "init");
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        client = getClient(workDir);
        client.setRemote(new GitRemoteConfig("origin",
                Arrays.asList(new String[] { otherWT.getAbsolutePath() }),
                Arrays.asList(new String[] { otherWT.getAbsolutePath() }),
                Arrays.asList(new String[] { "refs/heads/*:refs/remotes/origin/*" }),
                Arrays.asList(new String[] { "refs/remotes/origin/*:refs/heads/*" })), NULL_PROGRESS_MONITOR);
        client.fetch("origin", NULL_PROGRESS_MONITOR);
        client.checkoutRevision("origin/master", true, NULL_PROGRESS_MONITOR);
        GitBranch b = client.createBranch(BRANCH_NAME, "origin/master", NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(2, branches.size());
        assertNotNull(branches.get(BRANCH_NAME));
        assertEquals(1, repository.getConfig().getSubsections(ConfigConstants.CONFIG_BRANCH_SECTION).size());
        
        //delete tracked branch and test
        client.deleteBranch(BRANCH_NAME, false, NULL_PROGRESS_MONITOR);
        branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(1, branches.size());
        assertNull(branches.get(BRANCH_NAME));
        assertEquals(0, repository.getConfig().getSubsections(ConfigConstants.CONFIG_BRANCH_SECTION).size());        
    }
    
    public void testDeleteUnmergedBranch () throws Exception {
        File f = new File(workDir, "f");
        File[] files = { f };
        write(f, "init");
        add(files);
        commit(files);
        GitClient client = getClient(workDir);
        GitBranch b = client.createBranch(BRANCH_NAME, "master", NULL_PROGRESS_MONITOR);
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f, "change on branch");
        add(files);
        commit(files);
        //checkout other revision
        client.checkoutRevision("master", true, NULL_PROGRESS_MONITOR);
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
        File f = new File(workDir, "f");
        File[] files = { f };
        write(f, "init");
        add(files);
        commit(files);
        GitClient client = getClient(workDir);
        try {
            client.deleteBranch("master", true, NULL_PROGRESS_MONITOR);
            fail("Can not delete active branch");
        } catch (GitException ex) {
            assertTrue(ex.getMessage().contains("Branch master is checked out and cannot be deleted"));
        }
    }
    
    public void testDeleteRemoteBranch () throws Exception {
        final File otherWT = new File(workDir.getParentFile(), "repo2");
        GitClient client = getClient(otherWT);
        client.init(NULL_PROGRESS_MONITOR);
        File f = new File(otherWT, "f");
        write(f, "init");
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        client = getClient(workDir);
        client.fetch(otherWT.getAbsolutePath(), Arrays.asList(new String[] { "refs/heads/*:refs/remotes/origin/*" }), NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertEquals(1, branches.size());
        assertNotNull(branches.get("origin/master"));
        
        // delete remote branch
        client.deleteBranch("origin/master", false, NULL_PROGRESS_MONITOR);
        branches = client.getBranches(false, NULL_PROGRESS_MONITOR);
        assertEquals(0, branches.size());
    }
    
    public void testBranchTracking () throws Exception {
        final File otherWT = new File(workDir.getParentFile(), "repo2");
        GitClient client = getClient(otherWT);
        client.init(NULL_PROGRESS_MONITOR);
        File f = new File(otherWT, "f");
        write(f, "init");
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        client = getClient(workDir);
        client.setRemote(new GitRemoteConfig("origin", 
                Arrays.asList(otherWT.getAbsolutePath()),
                Arrays.asList(otherWT.getAbsolutePath()),
                Arrays.asList("+refs/heads/*:refs/remotes/origin/*"), Collections.<String>emptyList()), NULL_PROGRESS_MONITOR);
        client.fetch("origin", NULL_PROGRESS_MONITOR);
        GitBranch b = client.createBranch(Constants.MASTER, "origin/master", NULL_PROGRESS_MONITOR);
        assertEquals("origin/master", b.getTrackedBranch().getName());
        assertTrue(b.getTrackedBranch().isRemote());
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        
        b = client.createBranch("nova1", Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertNull(b.getTrackedBranch());
        
        StoredConfig cfg = repository.getConfig();
        cfg.setString(ConfigConstants.CONFIG_BRANCH_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOSETUPMERGE, "always");
        cfg.save();
        b = client.createBranch("nova2", Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals("master", b.getTrackedBranch().getName());
        assertFalse(b.getTrackedBranch().isRemote());
        
        // list branches
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        b = branches.get(Constants.MASTER);
        assertEquals("origin/master", b.getTrackedBranch().getName());
        assertTrue(b.getTrackedBranch().isRemote());
        b = branches.get("origin/master");
        assertNull(b.getTrackedBranch());
    }
    
    public void testListBranches_Issue213538 () throws Exception {
        GitClient client = getClient(workDir);
        File f = new File(workDir, "f");
        write(f, "init");
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        // cannot end with a RuntimeException
        File configFile = new File(Utils.getMetadataFolder(workDir), Constants.CONFIG);
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
        File emptyRepo = new File(workDir, "empty");
        GitClient client = getClient(emptyRepo);
        client.init(NULL_PROGRESS_MONITOR);
        Repository repo = getRepository(client);
        FileBasedConfig cfg = new FileBasedConfig(repo.getFS().resolve(repo.getDirectory(), Constants.CONFIG),
				repo.getFS());
        cfg.load();
        assertFalse(cfg.getSections().contains(ConfigConstants.CONFIG_BRANCH_SECTION));
        client.createBranch(Constants.MASTER, Constants.R_REMOTES + "origin/whateverbranch", NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertTrue(branches.isEmpty());
        cfg = new FileBasedConfig(repo.getFS().resolve(repo.getDirectory(), Constants.CONFIG),
				repo.getFS());
        cfg.load();
        assertTrue(cfg.getSections().contains(ConfigConstants.CONFIG_BRANCH_SECTION));
        assertEquals("origin", cfg.getString(ConfigConstants.CONFIG_BRANCH_SECTION,
                Constants.MASTER, ConfigConstants.CONFIG_KEY_REMOTE));
        assertEquals(Constants.R_HEADS + "whateverbranch", cfg.getString(ConfigConstants.CONFIG_BRANCH_SECTION,
                Constants.MASTER, ConfigConstants.CONFIG_KEY_MERGE));
    }
}
