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
package org.netbeans.libs.git.jgit.factory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.ApiUtils;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRepository;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class CreateClientTest extends AbstractGitTestCase {

    private File workDir;

    public CreateClientTest (String name) throws IOException {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
    }

    /**
     * Submodules have .git folder elsewhere, they use GIT_LINK mechanism to access it
     */
    public void testClientForSubmodule () throws Exception {
        File subRepo = new File(workDir, "subrepo");
        subRepo.mkdirs();
        File newFile = new File(subRepo, "file");
        newFile.createNewFile();
        File[] roots = new File[] { newFile };
        
        GitClient client = GitRepository.getInstance(subRepo).createClient();
        client.init(NULL_PROGRESS_MONITOR);
        client.add(roots, NULL_PROGRESS_MONITOR);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { newFile }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, subRepo, newFile, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        Repository repo = getRepository(client);
        StoredConfig config = repo.getConfig();
        config.setString(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_WORKTREE, subRepo.getAbsolutePath());
        config.save();

        File gitFolder = new File(subRepo, ".git");
        File newLocation = new File(workDir.getParentFile(), "newFolder");
        newLocation.mkdirs();
        gitFolder.renameTo(new File(newLocation, ".git"));
        gitFolder = new File(newLocation, ".git");
        File gitFile = new File(subRepo, ".git");
        write(gitFile, "gitdir: " + gitFolder.getAbsolutePath());
        
        ApiUtils.clearRepositoryPool();
        client = GitRepository.getInstance(subRepo).createClient();
        repo = getRepository(client);
        assertEquals(subRepo, repo.getWorkTree());
        assertEquals(gitFolder, repo.getDirectory());
        statuses = client.getStatus(new File[] { newFile }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, subRepo, newFile, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
    }
    
    public void testClientRelease () throws Exception {
        GitClient client1 = GitRepository.getInstance(workDir).createClient();
        Repository jgitRepo1 = getRepository(client1);
        assertRepoClients(jgitRepo1, 1);
        client1.release();
        assertRepoClients(jgitRepo1, 0);
        
        client1 = GitRepository.getInstance(workDir).createClient();
        assertEquals(jgitRepo1, getRepository(client1));
        assertRepoClients(jgitRepo1, 1);
        // some commands
        client1.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        client1.add(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        
        GitClient client2 = GitRepository.getInstance(workDir).createClient();
        assertEquals(jgitRepo1, getRepository(client2));
        assertRepoClients(jgitRepo1, 2);
        // some commands
        client2.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        client2.add(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        
        assertRepoClients(jgitRepo1, 2);        
        client1.release();
        assertRepoClients(jgitRepo1, 1);
        client2.release();
        assertRepoClients(jgitRepo1, 0);
    }

    private void assertRepoClients (Repository jgitRepo1, int expectedClients) throws Exception {
        Field f = Repository.class.getDeclaredField("useCnt");
        f.setAccessible(true);
        AtomicInteger cnt = (AtomicInteger) f.get(jgitRepo1);
        assertEquals(expectedClients, cnt.intValue());
    }
    
}
