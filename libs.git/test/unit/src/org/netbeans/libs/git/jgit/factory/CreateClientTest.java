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
    
    public void testCorruptedConfigNoEndingLine () throws Exception {
        File gitFolder = new File(workDir, ".git");
        File newLocation = new File(workDir.getParentFile(), "newFolder");
        newLocation.mkdirs();
        gitFolder.renameTo(new File(newLocation, ".git"));
        gitFolder = new File(newLocation, ".git");
        String content = read(new File(gitFolder, "config"));
        write(new File(gitFolder, "config"), content + "[remote \"origin\"]\n	puttykeyfile = ");
        try {
            GitRepository.getInstance(newLocation).createClient();
            fail("Should fail");
        } catch (GitException ex) {
            assertEquals("java.io.IOException: Unknown repository format", ex.getMessage());
        }
        write(new File(gitFolder, "config"), read(new File(gitFolder, "config")) + "\n");
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
