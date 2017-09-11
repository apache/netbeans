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
