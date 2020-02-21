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
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitConfig;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class SetUpstreamBranchTest extends AbstractGitTestCase {
    private VCSFileProxy workDir;
    private JGitRepository repository;
    private static final String BRANCH = "mybranch";

    public SetUpstreamBranchTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testLocalTracking","testRemoteTrackingNoRemoteSet","testRemoteTracking").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }
    
    public void testLocalTracking () throws GitException, IOException {
        GitClient client = getClient(workDir);
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxySupport.createNew(f);
        add(f);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        // prepare twp branches
        GitBranch b = client.createBranch(BRANCH, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertNull(b.getTrackedBranch());
        
        // set tracking
        b = client.setUpstreamBranch(BRANCH, GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        assertEquals(GitConstants.MASTER, b.getTrackedBranch().getName());
    }
    
    public void testRemoteTrackingNoRemoteSet () throws GitException, IOException {
        GitClient client = getClient(workDir);
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxySupport.createNew(f);
        add(f);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        // push to remote
        JGitConfig cfg = repository.getConfig();
        cfg.load();
        String remoteUri = cfg.getString(JGitConfig.CONFIG_KEY_REMOTE, "origin", JGitConfig.CONFIG_KEY_URL);
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
        
        cfg.load();
        assertEquals(".", cfg.getString(JGitConfig.CONFIG_BRANCH_SECTION, "master", JGitConfig.CONFIG_KEY_REMOTE));
        assertEquals("refs/remotes/origin/master", cfg.getString(JGitConfig.CONFIG_BRANCH_SECTION, "master", JGitConfig.CONFIG_KEY_MERGE));
    }
    
    public void testRemoteTracking () throws GitException, IOException {
        GitClient client = getClient(workDir);
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxySupport.createNew(f);
        add(f);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        // push to remote
        JGitConfig cfg = repository.getConfig();
        cfg.load();
        String remoteUri = cfg.getString(JGitConfig.CONFIG_KEY_REMOTE, "origin", JGitConfig.CONFIG_KEY_URL);
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
        
        // set tracking
        GitBranch b = client.setUpstreamBranch("master", "origin/master", NULL_PROGRESS_MONITOR);
        assertEquals("origin/master", b.getTrackedBranch().getName());
        
        cfg.load();
        assertEquals("origin", cfg.getString(JGitConfig.CONFIG_BRANCH_SECTION, "master", JGitConfig.CONFIG_KEY_REMOTE));
        assertEquals("refs/heads/master", cfg.getString(JGitConfig.CONFIG_BRANCH_SECTION, "master", JGitConfig.CONFIG_KEY_MERGE));
    }
    
}
