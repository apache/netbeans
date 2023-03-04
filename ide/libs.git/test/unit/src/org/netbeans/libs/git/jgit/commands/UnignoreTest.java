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
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class UnignoreTest extends AbstractGitTestCase {
    private File workDir;

    public UnignoreTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
    }

    public void testUnignoreFileInRoot () throws Exception {
        File f = new File(workDir, "file");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertFalse(gitIgnore.exists());
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "/file");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "f\nf2\n/file\nf3\nf4");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("f\nf2\nf3\nf4", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testUnignoreFileWithStarChar () throws Exception {
        if (isWindows()) {
            // win do not allow '*' in filename
            return;
        }
        File f = new File(workDir, "fi*le");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/fi[*]le", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testUnignoreFileWithQuestionMark () throws Exception {
        if (isWindows()) {
            // win do not allow '?' in filename
            return;
        }
        File f = new File(workDir, "fi?le");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/fi[?]le", read(gitIgnore));
        
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testUnignoreFileWithBracket () throws Exception {
        File f = new File(workDir, "fi[le");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/fi[[]le", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("", read(gitIgnore));
    }

    public void testUnignoreFolderInRoot () throws Exception {
        File f = new File(workDir, "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertFalse(gitIgnore.exists());
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "/folder/");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "/folder");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/folder\n!/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testUnignoreFileInSubfolder () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertFalse(gitIgnore.exists());
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "/sf1/sf2/file");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "/sf1/sf2/file/");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/sf1/sf2/file/", read(gitIgnore));
        assertEquals(0, ignores.length);
    }
    
    public void testUnignoreFolderInSubfolder () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertFalse(gitIgnore.exists());
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "sf1/sf2/folder/");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "/sf1/sf2/folder");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/sf1/sf2/folder\n!/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }

    public void testUnignoreWithNegation () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "!/sf1/sf2/file");
        File[] ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("!/sf1/sf2/file", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "/sf1/sf2/file\n!/sf1/sf2/file");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("!/sf1/sf2/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "!/sf1/sf2/file\n/sf1/sf2/file");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("!/sf1/sf2/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "file");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("file\n!/sf1/sf2/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        File gitIgnoreNested = new File(f.getParentFile(), Constants.DOT_GIT_IGNORE);
        File gitIgnoreNested2 = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnoreNested, "/file");
        write(gitIgnoreNested2, "/sf2/file");
        write(gitIgnore, "/sf1/sf2/file");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("", read(gitIgnore));
        assertEquals("", read(gitIgnoreNested));
        assertEquals("", read(gitIgnoreNested2));
        assertEquals(Arrays.asList(gitIgnoreNested, gitIgnoreNested2, gitIgnore), Arrays.asList(ignores));
    }
    
    public void testUnignoreFolderWithNegation () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "/sf1/sf2/folder/\n!/sf1/sf2/folder/");
        File[] ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("!/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "/sf1/sf2/folder\n!/sf1/sf2/folder/");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/sf1/sf2/folder\n!/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "!/sf1/sf2/folder/\n/sf1/sf2/folder/");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("!/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testUnignoreExcludedFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File excludeFile = new File(workDir, ".git/info/exclude");
        excludeFile.getParentFile().mkdirs();
        write(excludeFile, "/sf1/sf2/file");
        File[] ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(excludeFile.exists());
        assertEquals("", read(excludeFile));
        assertEquals(Arrays.asList(excludeFile), Arrays.asList(ignores));
        
        write(excludeFile, "file");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(excludeFile.exists());
        assertEquals("file\n!/sf1/sf2/file", read(excludeFile));
        assertEquals(Arrays.asList(excludeFile), Arrays.asList(ignores));
        
        write(excludeFile, "");
        ignores = getClient(workDir).unignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(excludeFile.exists());
        assertEquals("", read(excludeFile));
        assertEquals(0, ignores.length);
    }

    public void test199443_GlobalIgnoreFile () throws Exception {
        File f = new File(new File(workDir, "nbproject"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File ignoreFile = new File(workDir.getParentFile(), "globalignore");
        write(ignoreFile, "nbproject");
        Repository repo = getRepository(getLocalGitRepository());
        StoredConfig cfg = repo.getConfig();
        cfg.setString(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_EXCLUDESFILE, ignoreFile.getAbsolutePath());
        cfg.save();
        GitClient client = getClient(workDir);
        assertEquals(Status.STATUS_IGNORED, client.getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f).getStatusIndexWC());
        
        assertEquals(new File(workDir, Constants.GITIGNORE_FILENAME), client.unignore(new File[] { f }, NULL_PROGRESS_MONITOR)[0]);
        
        write(new File(workDir, Constants.GITIGNORE_FILENAME), "/nbproject/file");
        assertEquals(new File(workDir, Constants.GITIGNORE_FILENAME), client.unignore(new File[] { f }, NULL_PROGRESS_MONITOR)[0]);
        assertEquals("!/nbproject/file", read(new File(workDir, Constants.GITIGNORE_FILENAME)));
    }
}
