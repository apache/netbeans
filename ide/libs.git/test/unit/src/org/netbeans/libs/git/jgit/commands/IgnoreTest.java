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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class IgnoreTest extends AbstractGitTestCase {
    private File workDir;

    public IgnoreTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
    }

    public void testIgnoreFileInRoot () throws Exception {
        File f = new File(workDir, "file");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }

    public void testIgnoreFolderInRoot () throws Exception {
        File f = new File(workDir, "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFileInSubfolder () throws Exception {
        File f = new File(new File(new File(workDir, "subFolder"), "anotherSubfolder"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/subFolder/anotherSubfolder/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFolderInSubfolder () throws Exception {
        File f = new File(new File(new File(workDir, "subFolder"), "anotherSubfolder"), "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/subFolder/anotherSubfolder/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }

    public void testIgnoreFileInRootAppend () throws Exception {
        File f = new File(workDir, "file");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#this is ignore file\n\n\nfff\nfff2");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#this is ignore file\n\n\nfff\nfff2\n/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }

    public void testIgnoreFolderInRootAppend () throws Exception {
        File f = new File(workDir, "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#this is ignore file\n\n\nfff\nfff2");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#this is ignore file\n\n\nfff\nfff2\n/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFileInSubfolderAppend () throws Exception {
        File f = new File(new File(new File(workDir, "subFolder"), "anotherSubfolder"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#this is ignore file\nfff\nfff2");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#this is ignore file\nfff\nfff2\n/subFolder/anotherSubfolder/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFolderInSubfolderAppend () throws Exception {
        File f = new File(new File(new File(workDir, "subFolder"), "anotherSubfolder"), "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#this is ignore file\nfff\nfff2");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#this is ignore file\nfff\nfff2\n/subFolder/anotherSubfolder/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreIgnoredEqualPath () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/file\n#end ignoreFile");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/file\n#end ignoreFile", read(gitIgnore));
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreFolderIgnoredEqualPath () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/folder/");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/folder");
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/folder", read(gitIgnore));
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreIgnoredPartialEqualPath () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\nfile");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\nfile", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "sf1/sf2/file");
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("sf1/sf2/file", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "sf1/*/file");
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("sf1/*/file", read(gitIgnore));
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreFolderIgnoredPartialEqualPath () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        new File(f, "file").createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\nfolder");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\nfolder", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "#ignoreFile\nfolder/");
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\nfolder/", read(gitIgnore));
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreRemoveNegation () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/file\n!/sf1/sf2/file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "");
        File ignore2 = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(ignore2, "!sf2/file");
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/sf1/sf2/file", read(gitIgnore));
        assertEquals("", read(ignore2));
        assertEquals(Arrays.asList(ignore2, gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFolderRemoveNegation () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/folder/\n!/sf1/sf2/folder/");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/folder\n!/sf1/sf2/folder/");
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/folder", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreNoNegationRemoval () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/file\n!file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n!file\n/sf1/sf2/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }

    public void testIgnoreFolderNoNegationRemoval () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/folder\n!folder");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/folder\n!folder\n/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/folder\n!/sf1/sf2/folder");
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/folder\n!/sf1/sf2/folder\n/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFileInSubfolder_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(f.getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#dummy ignore file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#dummy ignore file", read(gitIgnore));
        gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        assertEquals("/sf1/sf2/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFolderInSubfolder_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(f.getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#dummy ignore file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#dummy ignore file", read(gitIgnore));
        gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        assertEquals("/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }

    public void testIgnoreIgnoredEqualPath_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(f.getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "/file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/file", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(0, ignores.length);
    }

    public void testIgnoreFolderIgnoredEqualPath_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "/sf2/folder/");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/sf2/folder/", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreIgnoredPartialEqualPath_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\nf*");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\nf*", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreFolderIgnoredPartialEqualPath_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\nfold*");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\nfold*", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreRemoveNegation_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf2/file\n!/sf2/file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf2/file", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "sf2/file\n!sf2/file");
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("sf2/file", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "#ignoreFile\nsf2/f*\n!/sf2/file");
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\nsf2/f*", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFolderRemoveNegation_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf2/folder/\n!/sf2/folder/");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf2/folder/", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "#ignoreFile\nsf2/f*\n!/sf2/folder/");
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\nsf2/f*", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreNoNegationRemoval_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "\n/sf2/file\n!file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("\n!file\n/sf2/file", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFolderNoNegationRemoval_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "\n/sf2/folder/\n!/sf2/folder");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("\n!/sf2/folder\n/sf2/folder/", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFileWithStarChar () throws Exception {
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
        
        write(gitIgnore, "/fi[*]le");
        GitStatus st = getClient(workDir).getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals(Status.STATUS_IGNORED, st.getStatusIndexWC());
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/fi[*]le", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "/fi\\*le");
        // jgit seems to incorrectly handle escaped wildcards
        st = getClient(workDir).getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals(Status.STATUS_IGNORED, st.getStatusIndexWC());
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/fi\\*le", read(gitIgnore));
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreFileWithQuestionMark () throws Exception {
        if (isWindows()) {
            // win do not allow '?' in filename
            return;
        }
        File f = new File(workDir, "fi?le");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/fi[?]le", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "/fi[?]le");
        GitStatus st = getClient(workDir).getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals(Status.STATUS_IGNORED, st.getStatusIndexWC());
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/fi[?]le", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "/fi\\?le");
        // jgit seems to incorrectly handle escaped wildcards
        st = getClient(workDir).getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals(Status.STATUS_IGNORED, st.getStatusIndexWC());
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/fi\\?le", read(gitIgnore));
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreFileWithBracket () throws Exception {
        File f = new File(workDir, "fi[le");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/fi[[]le", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "/fi[[]le");
        GitStatus st = getClient(workDir).getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals(Status.STATUS_IGNORED, st.getStatusIndexWC());
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/fi[[]le", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "/fi\\[le");
        // jgit seems to incorrectly handle escaped wildcards
        st = getClient(workDir).getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f);
        assertEquals(Status.STATUS_IGNORED, st.getStatusIndexWC());
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals("/fi\\[le", read(gitIgnore));
        assertEquals(0, ignores.length);
    }
    
    public void testDoNotIgnoreExcludedFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File excludeFile = new File(workDir, ".git/info/exclude");
        excludeFile.getParentFile().mkdirs();
        write(excludeFile, "/sf1/sf2/file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(excludeFile.exists());
        assertEquals("/sf1/sf2/file", read(excludeFile));
        assertEquals(0, ignores.length);
        
        write(excludeFile, "file");
        ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(excludeFile.exists());
        assertEquals("file", read(excludeFile));
        assertEquals(0, ignores.length);
    }
    
    public void test195841 () throws Exception {
        File f = new File(new File(new File(workDir, "suite"), "build"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File excludeFile = new File(workDir, ".gitignore");
        write(excludeFile, "build/");
        File[] ignores = getClient(workDir).ignore(new File[] { f.getParentFile() }, NULL_PROGRESS_MONITOR);
        assertTrue(excludeFile.exists());
        assertEquals(0, ignores.length);
    }
    
    public void test199443_GlobalIgnoreFile () throws Exception {
        File f = new File(new File(workDir, "nbproject"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File ignoreFile = new File(workDir.getParentFile(), "globalignore");
        write(ignoreFile, ".DS_Store\n.svn\nnbproject\nnbproject/private\n");
        Repository repo = getRepository(getLocalGitRepository());
        StoredConfig cfg = repo.getConfig();
        cfg.setString(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_EXCLUDESFILE, ignoreFile.getAbsolutePath());
        cfg.save();
        GitClient client = getClient(workDir);
        assertEquals(Status.STATUS_IGNORED, client.getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f).getStatusIndexWC());
        
        // now since the file is already ignored, no ignore file should be modified
        assertEquals(0, client.ignore(new File[] { f }, NULL_PROGRESS_MONITOR).length);
                
        // on the other hand, if .git/info/exclude reverts the effect of global excludes file, ignored file should be modified
        File dotGitIgnoreFile = new File(new File(repo.getDirectory(), "info"), "exclude");
        dotGitIgnoreFile.getParentFile().mkdirs();
        write(dotGitIgnoreFile, "!/nbproject/");
        assertEquals(Status.STATUS_ADDED, client.getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f).getStatusIndexWC());
        assertEquals(dotGitIgnoreFile, client.ignore(new File[] { f }, NULL_PROGRESS_MONITOR)[0]);
        assertEquals(Status.STATUS_IGNORED, client.getStatus(new File[] { f }, NULL_PROGRESS_MONITOR).get(f).getStatusIndexWC());
    }
    
    public void test199443_GlobalIgnoreFileOverwrite () throws Exception {
        File f = new File(new File(workDir, "nbproject"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File ignoreFile = new File(workDir.getParentFile(), "globalignore");
        Repository repo = getRepository(getLocalGitRepository());
        StoredConfig cfg = repo.getConfig();
        cfg.setString(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_EXCLUDESFILE, ignoreFile.getAbsolutePath());
        cfg.save();
        
        write(ignoreFile, "!nbproject");
        GitClient client = getClient(workDir);
        
        assertEquals(new File(workDir, Constants.GITIGNORE_FILENAME), client.ignore(new File[] { f }, NULL_PROGRESS_MONITOR)[0]);
        assertEquals("/nbproject/file", read(new File(workDir, Constants.GITIGNORE_FILENAME)));
    }
    
    public void test242551_DoubleStarPattern () throws Exception {
        File f = new File(new File(workDir, "AAA/BBB/CCC"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        
        GitClient client = getClient(workDir);
        
        Map<File, GitStatus> statuses = client.getStatus(new File[0], NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
        
        File ignoreFile = new File(workDir, ".gitignore");
        write(ignoreFile, "AAA/**/file");
        
        statuses = client.getStatus(new File[0], NULL_PROGRESS_MONITOR);
        assertEquals(Status.STATUS_IGNORED, statuses.get(f).getStatusIndexWC());
    }

    public void testGitIgnoreEndsWithNewLine () throws Exception {
        File f = new File(workDir, "file");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertTrue("The .gitignore file should ends with an empty new line.",containsCRorLF(gitIgnore));
    }

    public void testUpdateRetainsFileSpecificLineSeparators1() throws Exception {
        checkLineSeparatorRetention("\r\n");
    }

    public void testUpdateRetainsFileSpecificLineSeparators2() throws Exception {
        checkLineSeparatorRetention("\n");
    }

    private void checkLineSeparatorRetention(String lineSeparator) throws Exception {
        Path gitignore = workDir.toPath().resolve(Constants.DOT_GIT_IGNORE);
        Path toIgnore = workDir.toPath().resolve("file");
        String firstLine = "#comment" + lineSeparator;
        String secondLine = "/file" + lineSeparator;
        Files.writeString(gitignore, firstLine);
        Files.writeString(toIgnore, "ignore");
        getClient(workDir).ignore(new File[] { toIgnore.toFile() }, NULL_PROGRESS_MONITOR);

        String postUpdate = Files.readString(gitignore);
        assertEquals(firstLine, postUpdate.substring(0, firstLine.length()));
        assertEquals(secondLine, postUpdate.substring(firstLine.length(), firstLine.length() + secondLine.length()));
    }
}
