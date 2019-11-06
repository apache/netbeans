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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitClient.DiffMode;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class ExportDiffTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public ExportDiffTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }
    
    public void testSkipIgnores () throws Exception {
        File file = new File(workDir, "file");
        File patchFile = new File(workDir.getParentFile(), "diff.patch");
        File[] files = new File[] { file };
        
        file.createNewFile();
        getClient(workDir).ignore(files, NULL_PROGRESS_MONITOR);
        add(new File(workDir, Constants.GITIGNORE_FILENAME));
        commit(new File(workDir, Constants.GITIGNORE_FILENAME));
        
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        assertTrue(patchFile.exists());
        assertEquals("", read(patchFile));
    }
    
    public void testDiffSelectedPaths () throws Exception {
        File file1 = new File(workDir, "file1");
        File file2 = new File(workDir, "file2");
        File patchFile = new File(workDir.getParentFile(), "diff.patch");
        File[] files = new File[] { file1, file2 };
        
        file1.createNewFile();
        file2.createNewFile();
        
        // export diff for both f1 and f2
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        assertTrue(patchFile.exists());
        assertTrue(read(patchFile).contains("file1"));
        assertTrue(read(patchFile).contains("file2"));
        
        // export diff only for f1
        files = new File[] { file1 };
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        assertTrue(patchFile.exists());
        assertTrue(read(patchFile).contains("file1"));
        assertFalse(read(patchFile).contains("file2"));
    }

    public void testDiffChanges () throws Exception {
        makeInitialCommit();
        File file = new File(workDir, "file");
        File patchFile = new File(workDir.getParentFile(), "diff.patch");
        GitClient client = getClient(workDir);
        File[] files = new File[] { file };
        // no changes
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        assertTrue(patchFile.exists());
        assertEquals("", read(patchFile));
        
        // ******* add *******
        write(file, "hello\n");
        // index vs wt
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        assertFile(patchFile, getGoldenFile("diffChanges-index-wt-add.patch"));
        // head vs wt
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
        assertFile(patchFile, getGoldenFile("diffChanges-head-wt-add.patch"));
        // head vs index
        add(file);
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_INDEX);
        assertFile(patchFile, getGoldenFile("diffChanges-head-index-add.patch"));
        
        commit(file);
        
        // ******* modify *******
        write(file, "modification\n");
        // index vs wt
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        assertFile(patchFile, getGoldenFile("diffChanges-index-wt-modify.patch"));
        // head vs wt
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
        assertFile(patchFile, getGoldenFile("diffChanges-head-wt-modify.patch"));
        add(file);
        write(file, "modification2\n");
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
        assertFile(patchFile, getGoldenFile("diffChanges-head-wt-modify2.patch"));
        // head vs index
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_INDEX);
        assertFile(patchFile, getGoldenFile("diffChanges-head-index-modify.patch"));
        add(file);
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_INDEX);
        assertFile(patchFile, getGoldenFile("diffChanges-head-index-modify2.patch"));
        
        commit(file);
        // ******* delete *******
        // index vs wt
        file.delete();
        exportDiff(files, patchFile, GitClient.DiffMode.INDEX_VS_WORKINGTREE);
        assertFile(patchFile, getGoldenFile("diffChanges-index-wt-delete.patch"));
        // head vs index
        getClient(workDir).reset(files, "HEAD", true, NULL_PROGRESS_MONITOR);
        remove(true, files);
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_INDEX);
        assertFile(patchFile, getGoldenFile("diffChanges-index-wt-delete.patch"));
        // head vs wt
        getClient(workDir).reset(files, "HEAD", true, NULL_PROGRESS_MONITOR);
        file.delete();
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
        assertFile(patchFile, getGoldenFile("diffChanges-head-wt-delete.patch"));
    }
    
    public void testDiffRename () throws Exception {
        File file = new File(workDir, "file");
        File renamed = new File(workDir, "renamed");
        File patchFile = new File(workDir.getParentFile(), "diff.patch");
        File[] files = new File[] { file };
        
        write(file, "hey, i will be renamed\n");
        add(file);
        commit(file);
        
        getClient(workDir).rename(file, renamed, false, NULL_PROGRESS_MONITOR);
        exportDiff(files, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
        assertFile(patchFile, getGoldenFile("diffRename.patch"));
        exportDiff(new File[] { file, renamed }, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
        assertFile(patchFile, getGoldenFile("diffRename2.patch"));
        write(renamed, "hey, i will be renamed\nand now i am\n");
//        add(renamed);
        exportDiff(new File[] { file, renamed }, patchFile, GitClient.DiffMode.HEAD_VS_WORKINGTREE);
        assertFile(patchFile, getGoldenFile("diffRename3.patch"));
    }
    
    // issue in JGit prevents us from calling DiffFormater.format directly
    // change the source code when it's fixed
    public void testDiffRenameDetectionProblem () throws Exception {
        File file = new File(workDir, "file");
        File renamed = new File(workDir, "renamed");
        write(file, "hey, i will be renamed\n");
        add(file);
        commit(file);

        file.renameTo(renamed);
        write(renamed, "hey, i will be renamed\nand now i am\n");
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
        try (OutputStream out = new BufferedOutputStream(baos);
            DiffFormatter formatter = new DiffFormatter(out);) {
            formatter.setRepository(repository);
            formatter.setDetectRenames(true);
            AbstractTreeIterator firstTree = new DirCacheIterator(repository.readDirCache());
            AbstractTreeIterator secondTree = new FileTreeIterator(repository);
            formatter.format(firstTree, secondTree);
            formatter.flush();
        }
        assertFalse(
            "Fixed in JGit, modify and simplify the sources in ExportDiff command",
            baos.toString().contains("similarity index ")
        );
    }
    
    public void testDiffTwoCommits () throws Exception {
        File file = new File(workDir, "file");
        File file2 = new File(workDir, "folder/file2");
        file2.getParentFile().mkdirs();
        File patchFile = new File(workDir.getParentFile(), "diff.patch");
        File[] files = new File[] { file, file2 };
        
        write(file, "FILE 1\n");
        write(file2, "FILE 2\n");
        add();
        commit();
        
        write(file, "FILE 1 CHANGE\n");
        write(file2, "FILE 2 CHANGE\n");
        add();
        commit();
        
        exportDiff(files, patchFile, "master~1", "master");
        assertFile(patchFile, getGoldenFile("diffTwoCommits.patch"));
    }

    private void exportDiff (File[] files, File patchFile, DiffMode diffMode) throws Exception {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(patchFile))) {
            getClient(workDir).exportDiff(files, diffMode, out, NULL_PROGRESS_MONITOR);
        }
    }

    private void exportDiff (File[] files, File patchFile, String base, String to) throws Exception {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(patchFile))) {
            getClient(workDir).exportDiff(files, base, to, out, NULL_PROGRESS_MONITOR);
        }
    }

    private void makeInitialCommit () throws Exception {
        File f = new File(workDir, "dummy");
        f.createNewFile();
        add(f);
        commit(f);
    }
}
