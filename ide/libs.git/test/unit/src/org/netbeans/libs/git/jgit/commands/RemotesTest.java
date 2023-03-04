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
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class RemotesTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;
    
    public RemotesTest (String name) throws IOException {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }
    
    public void testRemoveRemote () throws Exception {
        File otherWT = new File(workDir.getParentFile(), "repo2");
        GitClient client = getClient(otherWT);
        client.init(NULL_PROGRESS_MONITOR);
        File f = new File(otherWT, "f");
        write(f, "init");
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        RemoteConfig cfg = new RemoteConfig(repository.getConfig(), "origin");
        cfg.addURI(new URIish(otherWT.toURI().toURL().toString()));
        cfg.addFetchRefSpec(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
        cfg.update(repository.getConfig());
        repository.getConfig().save();
        
        client = getClient(workDir);
        client.fetch("origin", NULL_PROGRESS_MONITOR);
        client.createBranch("master", "origin/master", NULL_PROGRESS_MONITOR);
        client.createBranch("nova", "origin/master", NULL_PROGRESS_MONITOR);
        
        StoredConfig config = repository.getConfig();
        assertEquals("+refs/heads/*:refs/remotes/origin/*", config.getString("remote", "origin", "fetch"));
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
        StoredConfig config = repository.getConfig();
        assertEquals(0, config.getSubsections("remote").size());
        
        GitClient client = getClient(workDir);
        GitRemoteConfig remoteConfig = new GitRemoteConfig("origin",
                Arrays.asList(new File(workDir.getParentFile(), "repo2").toURI().toString()),
                Arrays.asList(new File(workDir.getParentFile(), "repo2").toURI().toString()),
                Arrays.asList("+refs/heads/*:refs/remotes/origin/*"),
                Arrays.asList("refs/remotes/origin/*:+refs/heads/*"));
        client.setRemote(remoteConfig, NULL_PROGRESS_MONITOR);
        
        config.load();
        RemoteConfig cfg = new RemoteConfig(config, "origin");
        assertEquals(Arrays.asList(new URIish(new File(workDir.getParentFile(), "repo2").toURI().toString())), cfg.getURIs());
        assertEquals(Arrays.asList(new URIish(new File(workDir.getParentFile(), "repo2").toURI().toString())), cfg.getPushURIs());
        assertEquals(Arrays.asList(new RefSpec("+refs/heads/*:refs/remotes/origin/*")), cfg.getFetchRefSpecs());
        assertEquals(Arrays.asList(new RefSpec("refs/remotes/origin/*:+refs/heads/*")), cfg.getPushRefSpecs());
    }
    
    public void testUpdateRemote () throws Exception {
        StoredConfig config = repository.getConfig();
        RemoteConfig cfg = new RemoteConfig(config, "origin");
        cfg.addURI(new URIish("blablabla"));
        cfg.setFetchRefSpecs(Arrays.asList(new RefSpec("refs/heads/master:refs/remotes/origin/master")));
        cfg.update(config);
        config.save();
        config.load();
        assertEquals(1, config.getSubsections("remote").size());        
        
        GitClient client = getClient(workDir);
        GitRemoteConfig remoteConfig = new GitRemoteConfig("origin",
                Arrays.asList(new File(workDir.getParentFile(), "repo2").toURI().toString()),
                Arrays.asList(new File(workDir.getParentFile(), "repo2").toURI().toString()),
                Arrays.asList("+refs/heads/*:refs/remotes/origin/*"),
                Arrays.asList("refs/remotes/origin/*:+refs/heads/*"));
        client.setRemote(remoteConfig, NULL_PROGRESS_MONITOR);
        
        config.load();
        cfg = new RemoteConfig(config, "origin");
        assertEquals(Arrays.asList(new URIish(new File(workDir.getParentFile(), "repo2").toURI().toString())), cfg.getURIs());
        assertEquals(Arrays.asList(new URIish(new File(workDir.getParentFile(), "repo2").toURI().toString())), cfg.getPushURIs());
        assertEquals(Arrays.asList(new RefSpec("+refs/heads/*:refs/remotes/origin/*")), cfg.getFetchRefSpecs());
        assertEquals(Arrays.asList(new RefSpec("refs/remotes/origin/*:+refs/heads/*")), cfg.getPushRefSpecs());
    }
    
    public void testUpdateRemoteRollback () throws Exception {
        StoredConfig config = repository.getConfig();
        RemoteConfig cfg = new RemoteConfig(config, "origin");
        cfg.addURI(new URIish("blablabla"));
        cfg.setFetchRefSpecs(Arrays.asList(new RefSpec("refs/heads/master:refs/remotes/origin/master")));
        cfg.update(config);
        config.save();
        config.load();
        assertEquals(1, config.getSubsections("remote").size());        
        
        GitClient client = getClient(workDir);
        GitRemoteConfig remoteConfig = new GitRemoteConfig("origin",
                Arrays.asList(new File(workDir.getParentFile(), "repo2").toURI().toString()),
                Arrays.asList(new File(workDir.getParentFile(), "repo2").toURI().toString()),
                Arrays.asList("+refs/heads/*:refs/remotes/origin/master"),
                Arrays.asList("refs/remotes/origin/*:+refs/heads/*"));
        // an error while setting the remote must result in the rollback of the modification
        try {
            client.setRemote(remoteConfig, NULL_PROGRESS_MONITOR);
        } catch (GitException ex) {
            
        }
        cfg = new RemoteConfig(config, "origin");
        assertEquals(Arrays.asList(new URIish("blablabla")), cfg.getURIs());
        assertEquals(Arrays.asList(new RefSpec("refs/heads/master:refs/remotes/origin/master")), cfg.getFetchRefSpecs());
    }
}
