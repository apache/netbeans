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
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitMergeResult;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class ExportCommitTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;

    public ExportCommitTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testExportCommit","testExportMergeFail","testExportCommitMultiLine",
                "testExportCommitRename","testExportInitialCommit").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }
    
    public void testExportCommit () throws Exception {
        VCSFileProxy patchFile = VCSFileProxy.createFileProxy(workDir.getParentFile(), "diff.patch");
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy[] files = new VCSFileProxy[] { file };
        write(file, "init\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        write(file, "modification\n");
        add(files);
        GitRevisionInfo commit = client.commit(files, "my commit message", null, null, NULL_PROGRESS_MONITOR);
        exportDiff(commit.getRevision(), patchFile);
        String content =
"From {0} Mon Sep 17 00:00:00 2001\n" +
"From: {1}\n" +
"Date: {2}\n" +
"\n" +
"my commit message\n" +
"\n" +
"diff --git a/file b/file\n" +
"index b1b7161..ee73c61 100644\n" +
"--- a/file\n" +
"+++ b/file\n" +
"@@ -1 +1 @@\n" +
"-init\n" +
"+modification\n" +
"";
        assertPatchFile(commit,  content, patchFile);
    }
    
    public void testExportCommitMultiLine () throws Exception {
        VCSFileProxy patchFile = VCSFileProxy.createFileProxy(workDir.getParentFile(), "diff.patch");
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2");
        VCSFileProxy[] files = new VCSFileProxy[] { file, file2 };
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
        String content =
"From {0} Mon Sep 17 00:00:00 2001\n" +
"From: {1}\n" +
"Date: {2}\n" +
"\n" +
"first\n" +
"second\n" +
"third\n" +
"\n" +
"diff --git a/file b/file\n" +
"index b1b7161..d03681f 100644\n" +
"--- a/file\n" +
"+++ b/file\n" +
"@@ -1 +1 @@\n" +
"-init\n" +
"+modification 1\n" +
"diff --git a/file2 b/file2\n" +
"index b1b7161..4e5e8f2 100644\n" +
"--- a/file2\n" +
"+++ b/file2\n" +
"@@ -1 +1 @@\n" +
"-init\n" +
"+modification 2\n" +
"";
        assertPatchFile(commit, content, patchFile);
    }
    
    public void testExportMergeFail () throws Exception {
        VCSFileProxy patchFile = VCSFileProxy.createFileProxy(workDir.getParentFile(), "diff.patch");
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy[] files = new VCSFileProxy[] { file };
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
        VCSFileProxy patchFile = VCSFileProxy.createFileProxy(workDir.getParentFile(), "diff.patch");
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy renamed = VCSFileProxy.createFileProxy(workDir, "renamed");
        VCSFileProxy[] files = new VCSFileProxy[] { file, renamed };
        write(file, "first\nsecond\nthrirrd\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        client.rename(file, renamed, false, NULL_PROGRESS_MONITOR);
        write(renamed, "first\nsecond\nthird\n");
        add(renamed);
        GitRevisionInfo commit = client.commit(files, "file renamed", null, null, NULL_PROGRESS_MONITOR);
        exportDiff(commit.getRevision(), patchFile);
        String content =
"From {0} Mon Sep 17 00:00:00 2001\n" +
"From: {1}\n" +
"Date: {2}\n" +
"\n" +
"file renamed\n" +
"\n" +
"diff --git a/file b/renamed\n" +
"similarity index 61%\n" +
"rename from file\n" +
"rename to renamed\n" +
"index d26a10c..ff6e6b1 100644\n" +
"--- a/file\n" +
"+++ b/renamed\n" +
"@@ -1,3 +1,3 @@\n" +
" first\n" +
" second\n" +
"-thrirrd\n" +
"+third\n" +
"";
        assertPatchFile(commit, content, patchFile);
    }
    
    public void testExportInitialCommit () throws Exception {
        VCSFileProxy patchFile = VCSFileProxy.createFileProxy(workDir.getParentFile(), "diff.patch");
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy[] files = new VCSFileProxy[] { file };
        write(file, "init\n");
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo commit = client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        exportDiff("master", patchFile);
        String content = 
"From {0} Mon Sep 17 00:00:00 2001\n" +
"From: {1}\n" +
"Date: {2}\n" +
"\n" +
"initial commit\n" +
"\n" +
"diff --git a/file b/file\n" +
"new file mode 100644\n" +
"index 0000000..b1b7161\n" +
"--- /dev/null\n" +
"+++ b/file\n" +
"@@ -0,0 +1 @@\n" +
"+init\n" +
"";
        assertPatchFile(commit, content, patchFile);
    }

    private void exportDiff (String commit, VCSFileProxy patchFile) throws Exception {
        OutputStream out = VCSFileProxySupport.getOutputStream(patchFile);
        getClient(workDir).exportCommit(commit, out, NULL_PROGRESS_MONITOR);
        out.close();
    }

    private void assertPatchFile (GitRevisionInfo commit, String expectedContent, VCSFileProxy patchFile) throws Exception {
        expectedContent = MessageFormat.format(expectedContent, new Object[] { commit.getRevision(), 
            commit.getAuthor(),
            DateFormat.getDateTimeInstance().format(new Date(commit.getCommitTime())), 
            commit.getFullMessage() });
        assertEquals(expectedContent, read(patchFile));
    }
}
