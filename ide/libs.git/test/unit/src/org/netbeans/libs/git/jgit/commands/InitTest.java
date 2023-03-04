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
import java.lang.reflect.Field;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitRepository;
import org.netbeans.libs.git.jgit.JGitRepository;

/**
 *
 * @author ondra
 */
public class InitTest extends AbstractGitTestCase {

    private File workDir;

    public InitTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
    }

    public void testInit () throws Exception {
        File repo2 = new File(workDir.getParentFile(), "other");
        GitClient client = GitRepository.getInstance(repo2).createClient();
        Field f = GitClient.class.getDeclaredField("gitRepository");
        f.setAccessible(true);
        JGitRepository jgitRepo = (JGitRepository) f.get(client);
        f = JGitRepository.class.getDeclaredField("repository");
        f.setAccessible(true);
        Repository repo = (Repository) f.get(jgitRepo);
        
        assertFalse(repo.getDirectory().exists());
        assertFalse(repo.getIndexFile().exists());
        assertNull(repo.getBranch());

        // test repository init
        client.init(NULL_PROGRESS_MONITOR);
        DirCache index = repo.readDirCache();
        assertEquals(0, index.getEntryCount());
        assertTrue(repo.getDirectory().exists());
        assertEquals("master", repo.getBranch());
        assertConfig(new FileBasedConfig(new File(repo.getDirectory(), "config"), repo.getFS()));

        // test failure when repository already exists
        try {
            client.init(NULL_PROGRESS_MONITOR);
            fail("Repository created twice");
        } catch (GitException ex) {
            assertTrue(ex.getMessage().contains("Git repository already exists"));
        }
    }

    private void assertConfig (FileBasedConfig config) throws Exception {
        config.load();
        // filemode
        assertEquals(isWindows() ? "false" : "true", config.getString(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_FILEMODE));
        // bare
        assertEquals("false", config.getString(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_BARE));
        // autocrlf
        assertEquals(null, config.getString(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOCRLF));
    }

}
