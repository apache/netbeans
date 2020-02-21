/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
