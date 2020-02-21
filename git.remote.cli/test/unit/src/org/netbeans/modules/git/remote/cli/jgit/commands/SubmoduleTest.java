/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.ApiUtils;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitSubmoduleStatus;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitConfig;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.jgit.URIish;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class SubmoduleTest extends AbstractGitTestCase {
    private VCSFileProxy workDir;
    private JGitRepository repository;
    
    private JGitRepository repositorySM1;
    private JGitRepository repositorySM2;
    private VCSFileProxy moduleRepo;
    private VCSFileProxy submoduleRepo1;
    private VCSFileProxy submoduleRepo2;
    private VCSFileProxy submoduleFolder1;
    private VCSFileProxy submoduleFolder2;
    private VCSFileProxy f1;
    private VCSFileProxy f2;
    private VCSFileProxy f;

    public SubmoduleTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testStatusEmpty","testStatusUninitialized","testInitialize","testUpdate","testStatusCommit").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
        
        moduleRepo = VCSFileProxy.createFileProxy(workDir.getParentFile(), "module");
        VCSFileProxySupport.mkdirs(moduleRepo);
        GitClient client = getClient(moduleRepo);
        client.initBare(NULL_PROGRESS_MONITOR);
        
        submoduleRepo1 = VCSFileProxy.createFileProxy(workDir.getParentFile(), "submodule1");
        VCSFileProxySupport.mkdirs(submoduleRepo1);
        client = getClient(submoduleRepo1);
        client.initBare(NULL_PROGRESS_MONITOR);
        
        submoduleRepo2 = VCSFileProxy.createFileProxy(workDir.getParentFile(), "submodule2");
        VCSFileProxySupport.mkdirs(submoduleRepo2);
        client = getClient(submoduleRepo2);
        client.initBare(NULL_PROGRESS_MONITOR);
        
        client = getClient(workDir);
        f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        client.add(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        JGitConfig config = repository.getConfig();
        config.load();
        String uri = new URIish(moduleRepo.toURI().toURL().toString()).toString();
        config.setString("remote", "origin", JGitConfig.CONFIG_KEY_URL, uri);
        config.save();
        client.push(uri, Arrays.asList("refs/heads/master:refs/heads/master"),
                Arrays.asList("+refs/heads/master:refs/remotes/origin/master"), NULL_PROGRESS_MONITOR);
        
        VCSFileProxy submodules = VCSFileProxy.createFileProxy(workDir, "submodules");
        
        submoduleFolder1 = VCSFileProxy.createFileProxy(submodules, "submodule1");
        VCSFileProxySupport.mkdirs(submoduleFolder1);
        getClient(submoduleFolder1).init(NULL_PROGRESS_MONITOR);
        f1 = VCSFileProxy.createFileProxy(submoduleFolder1, "file");
        write(f1, "init");
        getClient(submoduleFolder1).add(new VCSFileProxy[] { f1 }, NULL_PROGRESS_MONITOR);
        getClient(submoduleFolder1).commit(new VCSFileProxy[] { f1 }, "init SM1 commit", null, null, NULL_PROGRESS_MONITOR);
        repositorySM1 = new JGitRepository(submoduleFolder1);
        
        config = repositorySM1.getConfig();
        config.load();
        uri = submoduleRepo1.toURI().toURL().toString();
        config.setString("remote", "origin", JGitConfig.CONFIG_KEY_URL, uri);
        config.save();
        getClient(submoduleFolder1).push(uri, Arrays.asList("refs/heads/master:refs/heads/master"),
                Arrays.asList("+refs/heads/master:refs/remotes/origin/master"), NULL_PROGRESS_MONITOR);
        
        submoduleFolder2 = VCSFileProxy.createFileProxy(submodules, "submodule2");
        VCSFileProxySupport.mkdirs(submoduleFolder2);
        getClient(submoduleFolder2).init(NULL_PROGRESS_MONITOR);
        f2 = VCSFileProxy.createFileProxy(submoduleFolder2, "file");
        write(f2, "init");
        getClient(submoduleFolder2).add(new VCSFileProxy[] { f2 }, NULL_PROGRESS_MONITOR);
        getClient(submoduleFolder2).commit(new VCSFileProxy[] { f2 }, "init SM1 commit", null, null, NULL_PROGRESS_MONITOR);
        repositorySM2 = new JGitRepository(submoduleFolder2);
        
        config = repositorySM2.getConfig();
        config.load();
        config.setString("remote", "origin", JGitConfig.CONFIG_KEY_URL, submoduleRepo2.toURI().toURL().toString());
        config.save();
        getClient(submoduleFolder2).push("origin", Arrays.asList("refs/heads/master:refs/heads/master"),
                Arrays.asList("+refs/heads/master:refs/remotes/origin/master"), NULL_PROGRESS_MONITOR);
    }
    
    public void testStatusEmpty () throws Exception {
        GitClient client = getClient(workDir);
        assertEquals(0, client.getSubmoduleStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR).size());
    }
    
    public void testStatusUninitialized () throws Exception {
        prepareUninitializedWorkdir();
        
        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitSubmoduleStatus> status = client.getSubmoduleStatus(new VCSFileProxy[] { f } , NULL_PROGRESS_MONITOR);
        assertEquals(0, status.size());
        
        status = client.getSubmoduleStatus(new VCSFileProxy[] { submoduleFolder1 } , NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder1), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder1);
        
        status = client.getSubmoduleStatus(new VCSFileProxy[] { submoduleFolder2 } , NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder2), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder2);
        
        status = client.getSubmoduleStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder1), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder1);
        assertStatus(status.get(submoduleFolder2), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder2);
    }
    
    public void testInitialize () throws Exception {
        prepareUninitializedWorkdir();
        
        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitSubmoduleStatus> status = client.getSubmoduleStatus(new VCSFileProxy[] { f } , NULL_PROGRESS_MONITOR);
        assertEquals(0, status.size());
        
        status = client.getSubmoduleStatus(new VCSFileProxy[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder1), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder1);
        
        status = client.initializeSubmodules(new VCSFileProxy[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder1), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder1);
    }
    
    public void testUpdate () throws Exception {
        prepareUninitializedWorkdir();
        
        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitSubmoduleStatus> status = client.getSubmoduleStatus(new VCSFileProxy[] { f } , NULL_PROGRESS_MONITOR);
        assertEquals(0, status.size());
        
        status = client.initializeSubmodules(new VCSFileProxy[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder1), GitSubmoduleStatus.StatusType.UNINITIALIZED,
                submoduleFolder1);
        status = client.updateSubmodules(new VCSFileProxy[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        assertStatus(status.get(submoduleFolder1), GitSubmoduleStatus.StatusType.INITIALIZED,
                submoduleFolder1);
    }
    
    public void testStatusCommit () throws Exception {
        prepareUninitializedWorkdir();
        
        GitClient client = getClient(workDir);
        client.initializeSubmodules(new VCSFileProxy[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        client.updateSubmodules(new VCSFileProxy[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        
        GitClient subClient = getClient(submoduleFolder1);
        subClient.checkoutRevision("master", true, NULL_PROGRESS_MONITOR);
        GitRevisionInfo previous = subClient.log("HEAD", NULL_PROGRESS_MONITOR);
        write(f1, "change");
        subClient.add(new VCSFileProxy[] { f1 }, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitSubmoduleStatus> status = client.getSubmoduleStatus(new VCSFileProxy[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        assertEquals(GitSubmoduleStatus.StatusType.INITIALIZED, status.get(submoduleFolder1).getStatus());
        assertEquals(previous.getRevision(), status.get(submoduleFolder1).getReferencedCommitId());
        
        subClient.commit(new VCSFileProxy[] { f1 }, "change commit", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo current = subClient.log("HEAD", NULL_PROGRESS_MONITOR);
        status = client.getSubmoduleStatus(new VCSFileProxy[] { submoduleFolder1 }, NULL_PROGRESS_MONITOR);
        assertEquals(GitSubmoduleStatus.StatusType.REV_CHECKED_OUT, status.get(submoduleFolder1).getStatus());
        assertEquals(previous.getRevision(), status.get(submoduleFolder1).getReferencedCommitId());
    }

    private void prepareUninitializedWorkdir () throws Exception {
        VCSFileProxy gmFile = VCSFileProxy.createFileProxy(workDir, ".gitmodules");
        VCSFileProxySupport.createNew(gmFile);
        write(gmFile, "[submodule \"submodules/submodule1\"]\n" +
"        path = submodules/submodule1\n" +
"        url = " + submoduleRepo1.toURI().toURL().toString() + "\n" +
"[submodule \"submodules/submodule2\"]\n" +
"        path = submodules/submodule2\n" +
"        url = " + submoduleRepo2.toURI().toURL().toString() + "\n");
        
        GitClient client = getClient(workDir);
        client.add(new VCSFileProxy[] { gmFile, submoduleFolder1, submoduleFolder2 }, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[0], "adding modules", null, null, NULL_PROGRESS_MONITOR);
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
            VCSFileProxy submoduleRoot) {
        assertEquals(statusKind, status.getStatus());
        assertEquals(submoduleRoot, status.getSubmoduleFolder());
    }
}
