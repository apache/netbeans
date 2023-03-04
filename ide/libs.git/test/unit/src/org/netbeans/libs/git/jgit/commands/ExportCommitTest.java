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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitMergeResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class ExportCommitTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public ExportCommitTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }
    
    public void testExportCommit () throws Exception {
        File patchFile = new File(workDir.getParentFile(), "diff.patch");
        File file = new File(workDir, "file");
        File[] files = new File[] { file };
        write(file, "init\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        write(file, "modification\n");
        add(files);
        GitRevisionInfo commit = client.commit(files, "my commit message", null, null, NULL_PROGRESS_MONITOR);
        exportDiff(commit.getRevision(), patchFile);
        assertPatchFile(commit, getGoldenFile("exportCommit.patch"), patchFile);
    }
    
    public void testExportCommitMultiLine () throws Exception {
        File patchFile = new File(workDir.getParentFile(), "diff.patch");
        File file = new File(workDir, "file");
        File file2 = new File(workDir, "file2");
        File[] files = new File[] { file, file2 };
        write(file, "init\n");
        write(file2, "init\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        write(file, "modification 1\n");
        write(file2, "modification 2\n");
        add(files);
        GitRevisionInfo commit = client.commit(files, "first\nsecond\nthird", null, null, NULL_PROGRESS_MONITOR);
        
        exportDiff(commit.getRevision(), patchFile);
        assertPatchFile(commit, getGoldenFile("exportCommitMultiLine.patch"), patchFile);
    }
    
    public void testExportMergeFail () throws Exception {
        File patchFile = new File(workDir.getParentFile(), "diff.patch");
        File file = new File(workDir, "file");
        File[] files = new File[] { file };
        write(file, "a\nb\nc\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        client.createBranch("branch", "master", NULL_PROGRESS_MONITOR);
        client.checkoutRevision("branch", true, NULL_PROGRESS_MONITOR);
        write(file, "modification on branch\nb\nc\n");
        add(files);
        GitRevisionInfo branchCommit = client.commit(files, "branch modified", null, null, NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision("master", true, NULL_PROGRESS_MONITOR);
        write(file, "a\nb\nmodification on master\n");
        add(files);
        GitRevisionInfo commit = client.commit(files, "master modified", null, null, NULL_PROGRESS_MONITOR);
        
        assertEquals(GitMergeResult.MergeStatus.MERGED, client.merge("branch", NULL_PROGRESS_MONITOR).getMergeStatus());
        try {
            exportDiff("master", patchFile);
            fail();
        } catch (GitException ex) {
            assertEquals("Unable to export a merge commit", ex.getMessage());
        }
    }

    public void testExportCommitRename () throws Exception {
        File patchFile = new File(workDir.getParentFile(), "diff.patch");
        File file = new File(workDir, "file");
        File renamed = new File(workDir, "renamed");
        File[] files = new File[] { file, renamed };
        write(file, "first\nsecond\nthrirrd\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        client.rename(file, renamed, false, NULL_PROGRESS_MONITOR);
        write(renamed, "first\nsecond\nthird\n");
        add(renamed);
        GitRevisionInfo commit = client.commit(files, "file renamed", null, null, NULL_PROGRESS_MONITOR);
        exportDiff(commit.getRevision(), patchFile);
        assertPatchFile(commit, getGoldenFile("exportCommitRename.patch"), patchFile);
    }
    
    public void testExportInitialCommit () throws Exception {
        File patchFile = new File(workDir.getParentFile(), "diff.patch");
        File file = new File(workDir, "file");
        File[] files = new File[] { file };
        write(file, "init\n");
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo commit = client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        exportDiff("master", patchFile);
        assertPatchFile(commit, getGoldenFile("exportInitialCommit.patch"), patchFile);
    }

    private void exportDiff (String commit, File patchFile) throws Exception {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(patchFile));
        getClient(workDir).exportCommit(commit, out, NULL_PROGRESS_MONITOR);
        out.close();
    }

    private void assertPatchFile (GitRevisionInfo commit, File goldenFile, File patchFile) throws Exception {
        String expectedContent = read(goldenFile);
        expectedContent = MessageFormat.format(expectedContent, new Object[] { commit.getRevision(), 
            commit.getAuthor(),
            DateFormat.getDateTimeInstance().format(new Date(commit.getCommitTime())), 
            commit.getFullMessage() });
        assertEquals(expectedContent, read(patchFile));
    }
}
