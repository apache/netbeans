/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
