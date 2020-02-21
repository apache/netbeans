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

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitClient.DiffMode;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class ExportDiffTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;

    public ExportDiffTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testDiffSelectedPaths","testDiffChanges").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }
    
    public void testSkipIgnores () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy patchFile = VCSFileProxy.createFileProxy(workDir.getParentFile(), "diff.patch");
        VCSFileProxy[] files = new VCSFileProxy[] { file };
        
        VCSFileProxySupport.createNew(file);
        getClient(workDir).ignore(files, NULL_PROGRESS_MONITOR);
        add(VCSFileProxy.createFileProxy(workDir, GitConstants.GITIGNORE_FILENAME));
        commit(VCSFileProxy.createFileProxy(workDir, GitConstants.GITIGNORE_FILENAME));
        
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        assertTrue(patchFile.exists());
        assertEquals("", read(patchFile));
    }
    
    public void testDiffSelectedPaths () throws Exception {
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "file1");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2");
        VCSFileProxy patchFile = VCSFileProxy.createFileProxy(workDir.getParentFile(), "diff.patch");
        VCSFileProxy[] files = new VCSFileProxy[] { file1, file2 };
        
        VCSFileProxySupport.createNew(file1);
        VCSFileProxySupport.createNew(file2);
        
        // export diff for both f1 and f2
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        assertTrue(patchFile.exists());
        assertTrue(read(patchFile).contains("file1"));
        assertTrue(read(patchFile).contains("file2"));
        
        // export diff only for f1
        files = new VCSFileProxy[] { file1 };
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        assertTrue(patchFile.exists());
        assertTrue(read(patchFile).contains("file1"));
        assertFalse(read(patchFile).contains("file2"));
    }

    public void testDiffChanges () throws Exception {
        makeInitialCommit();
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy patchFile = VCSFileProxy.createFileProxy(workDir.getParentFile(), "diff.patch");
        GitClient client = getClient(workDir);
        VCSFileProxy[] files = new VCSFileProxy[] { file };
        // no changes
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        assertTrue(patchFile.exists());
        assertEquals("", read(patchFile));
        
        // ******* add *******
        write(file, "hello\n");
        // index vs wt
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        String content = 
"diff --git a/file b/file\n" +
"new file mode 100644\n" +
"index 0000000..ce01362\n" +
"--- /dev/null\n" +
"+++ b/file\n" +
"@@ -0,0 +1 @@\n" +
"+hello";
        //assertFile(patchFile, getGoldenFile("diffChanges-index-wt-add.patch"));
        assertEquals(read(patchFile), content);
        // head vs wt
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
        content = 
"diff --git a/file b/file\n" +
"new file mode 100644\n" +
"index 0000000..ce01362\n" +
"--- /dev/null\n" +
"+++ b/file\n" +
"@@ -0,0 +1 @@\n" +
"+hello";
        // assertFile(patchFile, getGoldenFile("diffChanges-head-wt-add.patch"));
        assertEquals(read(patchFile), content);
        // head vs index
        add(file);
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_INDEX);
        content = 
"diff --git a/file b/file\n" +
"new file mode 100644\n" +
"index 0000000..ce01362\n" +
"--- /dev/null\n" +
"+++ b/file\n" +
"@@ -0,0 +1 @@\n" +
"+hello";
//        assertFile(patchFile, getGoldenFile("diffChanges-head-index-add.patch"));
        assertEquals(read(patchFile), content);
        
        commit(file);
        
        // ******* modify *******
        write(file, "modification\n");
        // index vs wt
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        content = 
"diff --git a/file b/file\n" +
"index ce01362..ee73c61 100644\n" +
"--- a/file\n" +
"+++ b/file\n" +
"@@ -1 +1 @@\n" +
"-hello\n" +
"+modification";
//        assertFile(patchFile, getGoldenFile("diffChanges-index-wt-modify.patch"));
        assertEquals(read(patchFile), content);
        // head vs wt
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
        content = 
"diff --git a/file b/file\n" +
"index ce01362..ee73c61 100644\n" +
"--- a/file\n" +
"+++ b/file\n" +
"@@ -1 +1 @@\n" +
"-hello\n" +
"+modification";
//        assertFile(patchFile, getGoldenFile("diffChanges-head-wt-modify.patch"));
        assertEquals(read(patchFile), content);
        add(file);
        write(file, "modification2\n");
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
        content = 
"diff --git a/file b/file\n" +
"index ce01362..3ed3641 100644\n" +
"--- a/file\n" +
"+++ b/file\n" +
"@@ -1 +1 @@\n" +
"-hello\n" +
"+modification2";
//        assertFile(patchFile, getGoldenFile("diffChanges-head-wt-modify2.patch"));
        assertEquals(read(patchFile), content);
        // head vs index
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_INDEX);
        content = 
"diff --git a/file b/file\n" +
"index ce01362..ee73c61 100644\n" +
"--- a/file\n" +
"+++ b/file\n" +
"@@ -1 +1 @@\n" +
"-hello\n" +
"+modification";
//        assertFile(patchFile, getGoldenFile("diffChanges-head-index-modify.patch"));
        assertEquals(read(patchFile), content);
        add(file);
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_INDEX);
        content = 
"diff --git a/file b/file\n" +
"index ce01362..3ed3641 100644\n" +
"--- a/file\n" +
"+++ b/file\n" +
"@@ -1 +1 @@\n" +
"-hello\n" +
"+modification2";
//        assertFile(patchFile, getGoldenFile("diffChanges-head-index-modify2.patch"));
        assertEquals(read(patchFile), content);
        
        commit(file);
        // ******* delete *******
        // index vs wt
        VCSFileProxySupport.delete(file);
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        content = 
"diff --git a/file b/file\n" +
"deleted file mode 100644\n" +
"index 3ed3641..0000000\n" +
"--- a/file\n" +
"+++ /dev/null\n" +
"@@ -1 +0,0 @@\n" +
"-modification2";
//        assertFile(patchFile, getGoldenFile("diffChanges-index-wt-delete.patch"));
        assertEquals(read(patchFile), content);
        // head vs index
        getClient(workDir).reset(files, "HEAD", true, NULL_PROGRESS_MONITOR);
        remove(true, files);
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_INDEX);
        content = 
"diff --git a/file b/file\n" +
"deleted file mode 100644\n" +
"index 3ed3641..0000000\n" +
"--- a/file\n" +
"+++ /dev/null\n" +
"@@ -1 +0,0 @@\n" +
"-modification2";
//        assertFile(patchFile, getGoldenFile("diffChanges-index-wt-delete.patch"));
        assertEquals(read(patchFile), content);
        // head vs wt
        getClient(workDir).reset(files, "HEAD", true, NULL_PROGRESS_MONITOR);
        VCSFileProxySupport.delete(file);
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
        content = 
"diff --git a/file b/file\n" +
"deleted file mode 100644\n" +
"index 3ed3641..0000000\n" +
"--- a/file\n" +
"+++ /dev/null\n" +
"@@ -1 +0,0 @@\n" +
"-modification2";
//        assertFile(patchFile, getGoldenFile("diffChanges-head-wt-delete.patch"));
        assertEquals(read(patchFile), content);
    }
    
    public void testDiffRename () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy renamed = VCSFileProxy.createFileProxy(workDir, "renamed");
        VCSFileProxy patchFile = VCSFileProxy.createFileProxy(workDir.getParentFile(), "diff.patch");
        VCSFileProxy[] files = new VCSFileProxy[] { file };
        
        write(file, "hey, i will be renamed\n");
        add(file);
        commit(file);
        
        getClient(workDir).rename(file, renamed, false, NULL_PROGRESS_MONITOR);
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
//        assertFile(patchFile, getGoldenFile("diffRename.patch"));
        exportDiff(new VCSFileProxy[] { file, renamed }, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
//        assertFile(patchFile, getGoldenFile("diffRename2.patch"));
        write(renamed, "hey, i will be renamed\nand now i am\n");
//        add(renamed);
        exportDiff(new VCSFileProxy[] { file, renamed }, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
//        assertFile(patchFile, getGoldenFile("diffRename3.patch"));
    }
    
    // issue in JGit prevents us from calling DiffFormater.format directly
    // change the source code when it's fixed
    public void testDiffRenameDetectionProblem () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy renamed = VCSFileProxy.createFileProxy(workDir, "renamed");
        VCSFileProxy patchFile = VCSFileProxy.createFileProxy(workDir.getParentFile(), "diff.patch");
        write(file, "hey, i will be renamed\n");
        add(file);
        commit(file);
        
        VCSFileProxySupport.renameTo(file, renamed);
        write(renamed, "hey, i will be renamed\nand now i am\n");
        OutputStream out = VCSFileProxySupport.getOutputStream(patchFile);
//        DiffFormatter formatter = new DiffFormatter(out);
//        formatter.setRepository(repository);
//        ObjectReader or = null;
//        try {
//            formatter.setDetectRenames(true);
//            AbstractTreeIterator firstTree = new DirCacheIterator(repository.readDirCache());;
//            AbstractTreeIterator secondTree = new FileTreeIterator(repository);
//            formatter.format(firstTree, secondTree);
//            formatter.flush();
//            fail("Fixed in JGit, modify and simplify the sources in ExportDiff command");
//        } catch (IOException ex) {
//            assertEquals("Missing blob 7b34a309b8dbae2686c9e597efef28a612e48aff", ex.getMessage());
//        } finally {
//            if (or != null) {
//                or.release();
//            }
//            formatter.release();
//        }
        
    }
    
    public void testDiffTwoCommits () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "folder/file2");
        VCSFileProxySupport.mkdirs(file2.getParentFile());
        VCSFileProxy patchFile = VCSFileProxy.createFileProxy(workDir.getParentFile(), "diff.patch");
        VCSFileProxy[] files = new VCSFileProxy[] { file, file2 };
        
        write(file, "FILE 1\n");
        write(file2, "FILE 2\n");
        add();
        commit();
        
        write(file, "FILE 1 CHANGE\n");
        write(file2, "FILE 2 CHANGE\n");
        add();
        commit();
        
        exportDiff(files, patchFile, "master~1", "master");
//        assertFile(patchFile, getGoldenFile("diffTwoCommits.patch"));
    }

    private void exportDiff (VCSFileProxy[] files, VCSFileProxy patchFile, DiffMode diffMode) throws Exception {
        OutputStream out = VCSFileProxySupport.getOutputStream(patchFile);
        getClient(workDir).exportDiff(files, diffMode, out, NULL_PROGRESS_MONITOR);
        out.close();
    }

    private void exportDiff (VCSFileProxy[] files, VCSFileProxy patchFile, String base, String to) throws Exception {
        OutputStream out = VCSFileProxySupport.getOutputStream(patchFile);
        getClient(workDir).exportDiff(files, base, to, out, NULL_PROGRESS_MONITOR);
        out.close();
    }

    private void makeInitialCommit () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "dummy");
        VCSFileProxySupport.createNew(f);
        add(f);
        commit(f);
    }
}
