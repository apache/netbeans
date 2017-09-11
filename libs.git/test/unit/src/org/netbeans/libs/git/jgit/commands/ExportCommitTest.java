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
