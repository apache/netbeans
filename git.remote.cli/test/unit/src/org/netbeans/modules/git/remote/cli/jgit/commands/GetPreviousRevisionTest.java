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
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class GetPreviousRevisionTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;

    public GetPreviousRevisionTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testGetPreviousRevisionSkipUntouched").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }
    
    public void testGetPreviousRevision () throws Exception {
        VCSFileProxy f =VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        write(f, "modification");
        add(files);
        GitRevisionInfo commit1 = client.commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        
        write(f, "modification2");
        add(files);
        GitRevisionInfo commit2 = client.commit(files, "modification2", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo revision = client.getPreviousRevision(f, commit2.getRevision(), NULL_PROGRESS_MONITOR);
        assertRevisions(commit1, revision);
    }
    
    public void testGetPreviousRevisionSkipUntouched () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "f2");
        write(f, "init");
        write(f2, "init");
        VCSFileProxy[] files = new VCSFileProxy[] { f, f2 };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo init = client.commit(files, "init", null, null, NULL_PROGRESS_MONITOR);

        write(f, "modification 1");
        add(files);
        GitRevisionInfo commit1 = client.commit(files, "modification 1", null, null, NULL_PROGRESS_MONITOR);
        
        // commit 2 does not touch "f"
        write(f2, "modification 2");
        add(files);
        GitRevisionInfo commit2 = client.commit(files, "modification 2", null, null, NULL_PROGRESS_MONITOR);
        
        write(f, "modification 3");
        add(files);
        GitRevisionInfo commit3 = client.commit(files, "modification 3", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo revision = client.getPreviousRevision(f, commit3.getRevision(), NULL_PROGRESS_MONITOR);
        assertRevisions(commit1, revision);
        
        revision = client.getPreviousRevision(f2, commit2.getRevision(), NULL_PROGRESS_MONITOR);
        assertRevisions(init, revision);
        
        revision = client.getCommonAncestor(new String[] { commit2.getParents()[0] }, NULL_PROGRESS_MONITOR);
        assertRevisions(commit1, revision);
        revision = client.getPreviousRevision(f2, commit3.getRevision(), NULL_PROGRESS_MONITOR);
        assertRevisions(commit2, revision);
    }

    private void assertRevisions (GitRevisionInfo expected, GitRevisionInfo info) throws GitException {
        assertEquals(expected.getRevision(), info.getRevision());
        assertEquals(expected.getAuthor().toString(), info.getAuthor().toString());
        assertEquals(expected.getCommitTime(), info.getCommitTime());
        assertEquals(expected.getCommitter().toString(), info.getCommitter().toString());
        assertEquals(expected.getFullMessage(), info.getFullMessage());
        assertEquals(expected.getShortMessage(), info.getShortMessage());
        assertEquals(expected.getModifiedFiles().size(), info.getModifiedFiles().size());
    }

}
