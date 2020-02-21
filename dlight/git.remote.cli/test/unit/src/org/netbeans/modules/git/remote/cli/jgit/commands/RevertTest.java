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
import java.util.Arrays;
import org.netbeans.modules.git.remote.cli.GitRevertResult;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class RevertTest extends AbstractGitTestCase {
    private VCSFileProxy workDir;
    private JGitRepository repository;

    public RevertTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testRevertConflict","testRevertNoCommit").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }
    
    public void testRevertLastCommitOneFile () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        write(f, "init");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "change");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        assertEquals("change", read(f));
        getClient(workDir).revert(commit.getRevision(), null, true, NULL_PROGRESS_MONITOR);
        assertEquals("init", read(f));
if(false)assertEquals("Revert \"modification\"\n\nThis reverts commit " + commit.getRevision() + ".", getClient(workDir).log("master", NULL_PROGRESS_MONITOR).getFullMessage());
else     assertEquals("Revert \"modification\"\n\nThis reverts commit " + commit.getRevision() + ".\n", getClient(workDir).log("master", NULL_PROGRESS_MONITOR).getFullMessage());
    }
    
    public void testRevertLastCommitTwoFiles () throws Exception {
        VCSFileProxy f1 = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "f2");
        VCSFileProxy[] files = new VCSFileProxy[] { f1, f2 };
        write(f1, "init1");
        write(f2, "init2");
        add(files);
        commit(files);
        
        // modify and commit
        write(f1, "change1");
        write(f2, "change2");
        add(files);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        assertEquals("change1", read(f1));
        assertEquals("change2", read(f2));
        getClient(workDir).revert(commit.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals("init1", read(f1));
        assertEquals("init2", read(f2));
    }
    
    public void testRevertCommitBeforeLast () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        write(f, "a\nb\nc");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "z\nb\nc");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        // second commit
        write(f, "z\nb\ny");
        add(f);
        commit(f);
        assertEquals("z\nb\ny", read(f));
        getClient(workDir).revert(commit.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals("a\nb\ny", read(f));
    }
    
    public void testRevertLastTwoCommits () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        write(f, "a\nb\nc");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "z\nb\nc");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        // second commit
        write(f, "z\nb\ny");
        add(f);
        GitRevisionInfo commit2 = getClient(workDir).commit(files, "modification 2", null, null, NULL_PROGRESS_MONITOR);
        assertEquals("z\nb\ny", read(f));
        getClient(workDir).revert(commit2.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals("z\nb\nc", read(f));
        getClient(workDir).revert(commit.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals("a\nb\nc", read(f));
    }
    
    public void testRevertFailure () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        write(f, "init");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "change");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        write(f, "local change");
        GitRevertResult result = getClient(workDir).revert(commit.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals("local change", read(f));
        assertEquals(Arrays.asList(f), result.getFailures());
        assertEquals(GitRevertResult.Status.FAILED, result.getStatus());
        //assertNull(repository.readMergeCommitMsg());
    }
    
    public void testRevertConflict () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        write(f, "init");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "change");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        write(f, "local change");
        add(f);
        commit(f);
        GitRevertResult result = getClient(workDir).revert(commit.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals("<<<<<<< OURS\nlocal change\n=======\ninit\n>>>>>>> THEIRS", read(f));
        assertEquals(Arrays.asList(f), result.getConflicts());
        assertEquals(GitRevertResult.Status.CONFLICTING, result.getStatus());
        //assertEquals("Revert \"modification\"\n\nThis reverts commit " + commit.getRevision() + ".\n\nConflicts:\n\tf\n", repository.readMergeCommitMsg());
    }
    
    public void testRevertNotIncluded () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        write(f, "init");
        add(f);
        commit(f);
        
        getClient(workDir).createBranch("branch", "master", NULL_PROGRESS_MONITOR);
        
        // modify and commit
        write(f, "change");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        getClient(workDir).checkoutRevision("branch", true, NULL_PROGRESS_MONITOR);
        GitRevertResult result = getClient(workDir).revert(commit.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals(GitRevertResult.Status.NO_CHANGE, result.getStatus());
    }

    public void testRevertNoCommit () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        write(f, "init");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "change");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        assertEquals("change", read(f));
        GitRevertResult result = getClient(workDir).revert(commit.getRevision(), null, false, NULL_PROGRESS_MONITOR);
        assertEquals("init", read(f));
        assertEqualsID(commit.getRevision(), getClient(workDir).getBranches(false, NULL_PROGRESS_MONITOR).get("master").getId());
        assertEquals(Arrays.asList(files), Arrays.asList(getClient(workDir).listModifiedIndexEntries(files, NULL_PROGRESS_MONITOR)));
        assertEquals(GitRevertResult.Status.REVERTED_IN_INDEX, result.getStatus());
    }

    public void testRevertMessage () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        write(f, "init");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "change");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        assertEquals("change", read(f));
        getClient(workDir).revert(commit.getRevision(), "blablabla message", true, NULL_PROGRESS_MONITOR);
        assertEquals("init", read(f));
if(false)assertEquals("blablabla message", getClient(workDir).log("master", NULL_PROGRESS_MONITOR).getFullMessage());
else    ;//unsupported comment
    }
}
