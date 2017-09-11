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
