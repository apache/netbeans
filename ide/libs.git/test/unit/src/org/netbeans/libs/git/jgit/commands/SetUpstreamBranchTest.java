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
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class SetUpstreamBranchTest extends AbstractGitTestCase {
    private File workDir;
    private static final String BRANCH = "mybranch";
    private Repository repository;

    public SetUpstreamBranchTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }
    
    public void testLocalTracking () throws GitException {
        GitClient client = getClient(workDir);
        File f = new File(workDir, "f");
        add(f);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        // prepare twp branches
        GitBranch b = client.createBranch(BRANCH, Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertNull(b.getTrackedBranch());
        
        // set tracking
        b = client.setUpstreamBranch(BRANCH, Constants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(Constants.MASTER, b.getTrackedBranch().getName());
    }
    
    public void testRemoteTrackingNoRemoteSet () throws GitException {
        GitClient client = getClient(workDir);
        File f = new File(workDir, "f");
        add(f);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        // push to remote
        String remoteUri = getRemoteRepository().getWorkTree().toURI().toString();
        client.push(remoteUri,
                Arrays.asList("refs/heads/master:refs/heads/master"),
                Arrays.asList("+refs/heads/*:refs/remotes/origin/*"),
                NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertTrue(branches.containsKey("origin/master"));
        assertNull(branches.get("master").getTrackedBranch());
        
        // set tracking
        GitBranch b = client.setUpstreamBranch("master", "origin/master", NULL_PROGRESS_MONITOR);
        assertEquals("origin/master", b.getTrackedBranch().getName());
        
        Config cfg = repository.getConfig();
        assertEquals(".", cfg.getString(ConfigConstants.CONFIG_BRANCH_SECTION, "master", ConfigConstants.CONFIG_KEY_REMOTE));
        assertEquals("refs/remotes/origin/master", cfg.getString(ConfigConstants.CONFIG_BRANCH_SECTION, "master", ConfigConstants.CONFIG_KEY_MERGE));
    }
    
    public void testRemoteTracking () throws Exception {
        GitClient client = getClient(workDir);
        File f = new File(workDir, "f");
        add(f);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        // push to remote
        String remoteUri = getRemoteRepository().getWorkTree().toURI().toString();
        client.setRemote(new GitRemoteConfig("origin",
                Arrays.asList(remoteUri),
                Collections.<String>emptyList(),
                Arrays.asList("+refs/heads/*:refs/remotes/origin/*"),
                Collections.<String>emptyList()),
                NULL_PROGRESS_MONITOR);
        client.push("origin",
                Arrays.asList("refs/heads/master:refs/heads/master"),
                Arrays.asList("+refs/heads/master:refs/remotes/origin/master"),
                NULL_PROGRESS_MONITOR);
        Map<String, GitBranch> branches = client.getBranches(true, NULL_PROGRESS_MONITOR);
        assertTrue(branches.containsKey("origin/master"));
        assertNull(branches.get("master").getTrackedBranch());
        
        StoredConfig config = repository.getConfig();
        config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOSETUPREBASE, ConfigConstants.CONFIG_KEY_NEVER);
        config.save();
        
        // set tracking
        GitBranch b = client.setUpstreamBranch("master", "origin/master", NULL_PROGRESS_MONITOR);
        assertEquals("origin/master", b.getTrackedBranch().getName());
        
        config = repository.getConfig();
        assertEquals("origin", config.getString(ConfigConstants.CONFIG_BRANCH_SECTION, "master", ConfigConstants.CONFIG_KEY_REMOTE));
        assertEquals("refs/heads/master", config.getString(ConfigConstants.CONFIG_BRANCH_SECTION, "master", ConfigConstants.CONFIG_KEY_MERGE));
        assertFalse(config.getBoolean(ConfigConstants.CONFIG_BRANCH_SECTION, "master", ConfigConstants.CONFIG_KEY_REBASE, false));
        
        // change autosetuprebase
        config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOSETUPREBASE, ConfigConstants.CONFIG_KEY_REMOTE);
        config.save();
        // set tracking
        b = client.setUpstreamBranch("master", "origin/master", NULL_PROGRESS_MONITOR);
        assertEquals("origin/master", b.getTrackedBranch().getName());
        config = repository.getConfig();
        assertEquals("origin", config.getString(ConfigConstants.CONFIG_BRANCH_SECTION, "master", ConfigConstants.CONFIG_KEY_REMOTE));
        assertEquals("refs/heads/master", config.getString(ConfigConstants.CONFIG_BRANCH_SECTION, "master", ConfigConstants.CONFIG_KEY_MERGE));
        assertTrue(config.getBoolean(ConfigConstants.CONFIG_BRANCH_SECTION, "master", ConfigConstants.CONFIG_KEY_REBASE, false));
    }
    
}
