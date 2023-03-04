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
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.ApiUtils;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitRepository;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitSubmoduleStatus;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.jgit.Utils;

/**
 *
 * @author Ondrej Vrabec
 */
public class SubmoduleTest extends AbstractGitTestCase {
    
    private Repository repository;
    private Repository repositorySM1;
    private Repository repositorySM2;
    private File workDir;
    private File moduleRepo;
    private File submoduleRepo1;
    private File submoduleRepo2;
    private File submoduleFolder1;
    private File submoduleFolder2;
    private File f1;
    private File f2;
    private File f;

    public SubmoduleTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
        
        moduleRepo = new File(workDir.getParentFile(), "module");
        GitClient client = getClient(moduleRepo);
        client.init(NULL_PROGRESS_MONITOR);
        submoduleRepo1 = new File(workDir.getParentFile(), "submodule1");
        client = getClient(submoduleRepo1);
        client.init(NULL_PROGRESS_MONITOR);
        submoduleRepo2 = new File(workDir.getParentFile(), "submodule2");
        client = getClient(submoduleRepo2);
        client.init(NULL_PROGRESS_MONITOR);
        
        client = getClient(workDir);
        f = new File(workDir, "f");
        write(f, "init");
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        RemoteConfig cfg = new RemoteConfig(repository.getConfig(), "origin");
        cfg.addURI(new URIish(moduleRepo.toURI().toURL().toString()));
        cfg.update(repository.getConfig());
        repository.getConfig().save();
        client.push("origin", Arrays.asList("refs/heads/master:refs/heads/master"),
                Arrays.asList("+refs/heads/master:refs/remotes/origin/master"), NULL_PROGRESS_MONITOR);
        
        File submodules = new File(workDir, "submodules");
        
        submoduleFolder1 = new File(submodules, "submodule1");
        submoduleFolder1.mkdirs();
        getClient(submoduleFolder1).init(NULL_PROGRESS_MONITOR);
        f1 = new File(submoduleFolder1, "file");
        write(f1, "init");
        getClient(submoduleFolder1).add(new File[] { f1 }, NULL_PROGRESS_MONITOR);
        getClient(submoduleFolder1).commit(new File[] { f1 }, "init SM1 commit", null, null, NULL_PROGRESS_MONITOR);
        repositorySM1 = getRepository(getClient(submoduleFolder1));
        cfg = new RemoteConfig(repositorySM1.getConfig(), "origin");
        cfg.addURI(new URIish(submoduleRepo1.toURI().toURL().toString()));
        cfg.update(repositorySM1.getConfig());
        repositorySM1.getConfig().save();
        getClient(submoduleFolder1).push("origin", Arrays.asList("refs/heads/master:refs/heads/master"),
                Arrays.asList("+refs/heads/master:refs/remotes/origin/master"), NULL_PROGRESS_MONITOR);
        
        
        submoduleFolder2 = new File(submodules, "submodule2");
        submoduleFolder2.mkdirs();
        getClient(submoduleFolder2).init(NULL_PROGRESS_MONITOR);
        f2 = new File(submoduleFolder2, "file");
        write(f2, "init");
        getClient(submoduleFolder2).add(new File[] { f2 }, NULL_PROGRESS_MONITOR);
        getClient(submoduleFolder2).commit(new File[] { f2 }, "init SM1 commit", null, null, NULL_PROGRESS_MONITOR);
        repositorySM2 = getRepository(getClient(submoduleFolder2));
        cfg = new RemoteConfig(repositorySM2.getConfig(), "origin");
        cfg.addURI(new URIish(submoduleRepo2.toURI().toURL().toString()));
        cfg.update(repositorySM2.getConfig());
        repositorySM2.getConfig().save();
        getClient(submoduleFolder2).push("origin", Arrays.asList("refs/heads/master:refs/heads/master"),
                Arrays.asList("+refs/heads/master:refs/remotes/origin/master"), NULL_PROGRESS_MONITOR);
    }
    
    public void testStatusEmpty () throws Exception {
        GitClient client = getClient(workDir);
        assertEquals(0, client.getSubmoduleStatus(new File[0], NULL_PROGRESS_MONITOR).size());
    }
    
    public void testStatusUninitialized () throws Exception {
        prepareUninitializedWorkdir();
        
        GitClient client = getClient(workDir);
        Map<File, GitSubmoduleStatus> status = client.getSubmoduleStatus(new File[] { f } , NULL_PROGRESS_MONITOR);
        assertEquals(0, status.size());
        
        status = client.getSubmoduleStatus(new File[] { submoduleFolder1 } , NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder1), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder1);
        
        status = client.getSubmoduleStatus(new File[] { submoduleFolder2 } , NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder2), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder2);
        
        status = client.getSubmoduleStatus(new File[0], NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder1), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder1);
        assertStatus(status.get(submoduleFolder2), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder2);
    }
    
    public void testInitialize () throws Exception {
        prepareUninitializedWorkdir();
        
        GitClient client = getClient(workDir);
        Map<File, GitSubmoduleStatus> status = client.getSubmoduleStatus(new File[] { f } , NULL_PROGRESS_MONITOR);
        assertEquals(0, status.size());
        
        status = client.getSubmoduleStatus(new File[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder1), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder1);
        
        status = client.initializeSubmodules(new File[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder1), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder1);
    }
    
    public void testUpdate () throws Exception {
        prepareUninitializedWorkdir();
        
        GitClient client = getClient(workDir);
        Map<File, GitSubmoduleStatus> status = client.getSubmoduleStatus(new File[] { f } , NULL_PROGRESS_MONITOR);
        assertEquals(0, status.size());
        
        status = client.initializeSubmodules(new File[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder1), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder1);
        status = client.updateSubmodules(new File[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder1), GitSubmoduleStatus.StatusType.INITIALIZED,
                submoduleFolder1);
    }
    
    public void testStatusCommit () throws Exception {
        prepareUninitializedWorkdir();
        
        GitClient client = getClient(workDir);
        client.initializeSubmodules(new File[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        client.updateSubmodules(new File[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        
        GitClient subClient = getClient(submoduleFolder1);
        subClient.checkoutRevision("master", true, NULL_PROGRESS_MONITOR);
        GitRevisionInfo previous = subClient.log("HEAD", NULL_PROGRESS_MONITOR);
        write(f1, "change");
        subClient.add(new File[] { f1 }, NULL_PROGRESS_MONITOR);
        Map<File, GitSubmoduleStatus> status = client.getSubmoduleStatus(new File[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        assertEquals(GitSubmoduleStatus.StatusType.INITIALIZED, status.get(submoduleFolder1).getStatus());
        assertEquals(previous.getRevision(), status.get(submoduleFolder1).getReferencedCommitId());
        
        subClient.commit(new File[] { f1 }, "change commit", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo current = subClient.log("HEAD", NULL_PROGRESS_MONITOR);
        status = client.getSubmoduleStatus(new File[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        assertEquals(GitSubmoduleStatus.StatusType.REV_CHECKED_OUT, status.get(submoduleFolder1).getStatus());
        assertEquals(previous.getRevision(), status.get(submoduleFolder1).getReferencedCommitId());
    }

    private void prepareUninitializedWorkdir () throws Exception {
        File gmFile = new File(workDir, ".gitmodules");
        gmFile.createNewFile();
        write(gmFile, "[submodule \"submodules/submodule1\"]\n" +
"        path = submodules/submodule1\n" +
"        url = " + submoduleRepo1.toURI().toURL().toString() + "\n" +
"[submodule \"submodules/submodule2\"]\n" +
"        path = submodules/submodule2\n" +
"        url = " + submoduleRepo2.toURI().toURL().toString() + "\n");
        
        GitClient client = getClient(workDir);
        client.add(new File[] { gmFile, submoduleFolder1, submoduleFolder2 }, NULL_PROGRESS_MONITOR);
        client.commit(new File[0], "adding modules", null, null, NULL_PROGRESS_MONITOR);
        client.push("origin", Arrays.asList("refs/heads/master:refs/heads/master"),
                Arrays.asList("+refs/heads/master:refs/remotes/origin/master"), NULL_PROGRESS_MONITOR);
        
        Utils.deleteRecursively(submoduleFolder1);
        Utils.deleteRecursively(submoduleFolder2);
        assertFalse(submoduleFolder1.exists());
        assertFalse(submoduleFolder2.exists());
        client.reset("master", GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        assertTrue(submoduleFolder1.exists());
        assertTrue(submoduleFolder2.exists());
        // must clear temporary git pool instances
        // submodule commands initialize submodules with .git as a link to parent/.git/... folders
        ApiUtils.clearRepositoryPool();
    }

    private void assertStatus (GitSubmoduleStatus status, GitSubmoduleStatus.StatusType statusKind,
            File submoduleRoot) {
        assertEquals(statusKind, status.getStatus());
        assertEquals(submoduleRoot, status.getSubmoduleFolder());
    }
}
