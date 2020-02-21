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
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitRemoteConfig;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitConfig;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class RemotesTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;
    
    public RemotesTest (String name) throws IOException {
        super(name);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testRemoveRemote").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }
    
    public void testRemoveRemote () throws Exception {
        VCSFileProxy otherWT = VCSFileProxy.createFileProxy(workDir.getParentFile(), "repo2");
        VCSFileProxySupport.mkdirs(otherWT);
        GitClient client = getClient(otherWT);
        client.init(NULL_PROGRESS_MONITOR);
        VCSFileProxy f = VCSFileProxy.createFileProxy(otherWT, "f");
        write(f, "init");
        client.add(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        client.setRemote(new GitRemoteConfig("origin", 
                Arrays.asList(otherWT.getPath()),
                Arrays.asList(otherWT.getPath()),
                Arrays.asList("+refs/heads/*:refs/remotes/origin/*"), Collections.<String>emptyList()), NULL_PROGRESS_MONITOR);
        
        client = getClient(workDir);
        client.fetch("origin", NULL_PROGRESS_MONITOR);
        client.createBranch("master", "origin/master", NULL_PROGRESS_MONITOR);
        client.createBranch("nova", "origin/master", NULL_PROGRESS_MONITOR);
        
        JGitConfig config = repository.getConfig();
        config.load();
        assertEquals("+refs/heads/*:refs/remotes/origin/*", config.getString("remote", "origin", JGitConfig.CONFIG_KEY_FETCH));
        assertEquals("origin", config.getString("branch", "master", "remote"));
        assertEquals("refs/heads/master", config.getString("branch", "master", "merge"));
        assertEquals("origin", config.getString("branch", "nova", "remote"));
        assertEquals("refs/heads/master", config.getString("branch", "nova", "merge"));
        
        // now try to remove the remote
        client.removeRemote("origin", NULL_PROGRESS_MONITOR);
        config = repository.getConfig();
        config.load();
        // is everything deleted?
        assertEquals(0, config.getSubsections("remote").size());
        assertNull(config.getString("branch", "master", "remote"));
        assertNull(config.getString("branch", "master", "merge"));
        assertNull(config.getString("branch", "nova", "remote"));
        assertNull(config.getString("branch", "nova", "merge"));
    }
    
    public void testAddRemote () throws Exception {
        JGitConfig config = repository.getConfig();
if(false)assertEquals(0, config.getSubsections("remote").size());
        
        GitClient client = getClient(workDir);
        GitRemoteConfig remoteConfig = new GitRemoteConfig("origin",
                Arrays.asList(VCSFileProxy.createFileProxy(workDir.getParentFile(), "repo2").toURI().toString()),
                Arrays.asList(VCSFileProxy.createFileProxy(workDir.getParentFile(), "repo2").toURI().toString()),
                Arrays.asList("+refs/heads/*:refs/remotes/origin/*"),
                Arrays.asList("refs/remotes/origin/*:+refs/heads/*"));
        client.setRemote(remoteConfig, NULL_PROGRESS_MONITOR);
        
        config.load();
        GitRemoteConfig cfg = new GitRemoteConfig(config, "origin");
        assertEquals(Arrays.asList(VCSFileProxy.createFileProxy(workDir.getParentFile(), "repo2").toURI().toString()), cfg.getUris());
        assertEquals(Arrays.asList(VCSFileProxy.createFileProxy(workDir.getParentFile(), "repo2").toURI().toString()), cfg.getPushUris());
        assertEquals(Arrays.asList("+refs/heads/*:refs/remotes/origin/*"), cfg.getFetchRefSpecs());
        assertEquals(Arrays.asList("refs/remotes/origin/*:+refs/heads/*"), cfg.getPushRefSpecs());
    }
    
    public void testUpdateRemote () throws Exception {
        JGitConfig config = repository.getConfig();
        config.load();
        GitClient client = getClient(workDir);
        GitRemoteConfig remoteConfig = new GitRemoteConfig("origin",
                Arrays.asList(VCSFileProxy.createFileProxy(workDir.getParentFile(), "repo2").toURI().toString()),
                Arrays.asList(VCSFileProxy.createFileProxy(workDir.getParentFile(), "repo2").toURI().toString()),
                Arrays.asList("+refs/heads/*:refs/remotes/origin/*"),
                Arrays.asList("refs/remotes/origin/*:+refs/heads/*"));
        client.setRemote(remoteConfig, NULL_PROGRESS_MONITOR);
        config.load();
        remoteConfig = new GitRemoteConfig(config, "origin");
        assertEquals(Arrays.asList(VCSFileProxy.createFileProxy(workDir.getParentFile(), "repo2").toURI().toString()), remoteConfig.getUris());
        assertEquals(Arrays.asList(VCSFileProxy.createFileProxy(workDir.getParentFile(), "repo2").toURI().toString()), remoteConfig.getPushUris());
        assertEquals(Arrays.asList("+refs/heads/*:refs/remotes/origin/*"), remoteConfig.getFetchRefSpecs());
        assertEquals(Arrays.asList("refs/remotes/origin/*:+refs/heads/*"), remoteConfig.getPushRefSpecs());
    }
    
}
