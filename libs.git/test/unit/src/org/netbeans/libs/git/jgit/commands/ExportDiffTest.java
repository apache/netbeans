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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitClient.DiffMode;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

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
        File patchFile = new File(workDir.getParentFile(), "diff.patch");
        write(file, "hey, i will be renamed\n");
        add(file);
        commit(file);
        
        file.renameTo(renamed);
        write(renamed, "hey, i will be renamed\nand now i am\n");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(patchFile));
        DiffFormatter formatter = new DiffFormatter(out);
        formatter.setRepository(repository);
        ObjectReader or = null;
        try {
            formatter.setDetectRenames(true);
            AbstractTreeIterator firstTree = new DirCacheIterator(repository.readDirCache());;
            AbstractTreeIterator secondTree = new FileTreeIterator(repository);
            formatter.format(firstTree, secondTree);
            formatter.flush();
            fail("Fixed in JGit, modify and simplify the sources in ExportDiff command");
        } catch (IOException ex) {
            assertEquals("Missing blob 7b34a309b8dbae2686c9e597efef28a612e48aff", ex.getMessage());
        } finally {
            if (or != null) {
                or.release();
            }
            formatter.release();
        }
        
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
        OutputStream out = new BufferedOutputStream(new FileOutputStream(patchFile));
        getClient(workDir).exportDiff(files, diffMode, out, NULL_PROGRESS_MONITOR);
        out.close();
    }

    private void exportDiff (File[] files, File patchFile, String base, String to) throws Exception {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(patchFile));
        getClient(workDir).exportDiff(files, base, to, out, NULL_PROGRESS_MONITOR);
        out.close();
    }

    private void makeInitialCommit () throws Exception {
        File f = new File(workDir, "dummy");
        f.createNewFile();
        add(f);
        commit(f);
    }
}
