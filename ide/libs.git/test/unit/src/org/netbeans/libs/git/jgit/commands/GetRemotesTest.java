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
import java.util.Map;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class GetRemotesTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public GetRemotesTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testGetRemotes () throws Exception {
        GitClient client = getClient(workDir);
        StoredConfig cfg = repository.getConfig();
        RemoteConfig remoteConfig = new RemoteConfig(cfg, "origin");
        
        Map<String, GitRemoteConfig> remotes = client.getRemotes(NULL_PROGRESS_MONITOR);
        assertEquals(0, remotes.size());
        remoteConfig.update(cfg);
        cfg.save();
        
        remotes = client.getRemotes(NULL_PROGRESS_MONITOR);
        assertEquals(0, remotes.size());
        
        remoteConfig.addURI(new URIish("file:///home/repository"));
        remoteConfig.addURI(new URIish("file:///home/repository2"));
        remoteConfig.addPushURI(new URIish("file:///home/repository"));
        remoteConfig.addPushURI(new URIish("file:///home/repository3"));
        remoteConfig.addFetchRefSpec(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
        remoteConfig.addFetchRefSpec(new RefSpec("+refs/heads/master:refs/remotes/origin/my-master"));
        remoteConfig.addPushRefSpec(new RefSpec("refs/remotes/origin/*:refs/heads/*"));
        remoteConfig.update(cfg);
        cfg.save();
        
        remotes = client.getRemotes(NULL_PROGRESS_MONITOR);
        assertEquals(1, remotes.size());
        assertEquals("origin", remotes.get("origin").getRemoteName());
        assertEquals(Arrays.asList(new String[] { "file:///home/repository", "file:///home/repository2" }), remotes.get("origin").getUris());
        assertEquals(Arrays.asList(new String[] { "file:///home/repository", "file:///home/repository3" }), remotes.get("origin").getPushUris());
        assertEquals(Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*", "+refs/heads/master:refs/remotes/origin/my-master" }), remotes.get("origin").getFetchRefSpecs());
        assertEquals(Arrays.asList(new String[] { "refs/remotes/origin/*:refs/heads/*" }), remotes.get("origin").getPushRefSpecs());
        
        GitRemoteConfig remote = client.getRemote("origin", NULL_PROGRESS_MONITOR);
        assertEquals("origin", remote.getRemoteName());
        assertEquals(Arrays.asList(new String[] { "file:///home/repository", "file:///home/repository2" }), remote.getUris());
        assertEquals(Arrays.asList(new String[] { "file:///home/repository", "file:///home/repository3" }), remote.getPushUris());
        assertEquals(Arrays.asList(new String[] { "+refs/heads/*:refs/remotes/origin/*", "+refs/heads/master:refs/remotes/origin/my-master" }), remote.getFetchRefSpecs());
        assertEquals(Arrays.asList(new String[] { "refs/remotes/origin/*:refs/heads/*" }), remote.getPushRefSpecs());
    }
}
