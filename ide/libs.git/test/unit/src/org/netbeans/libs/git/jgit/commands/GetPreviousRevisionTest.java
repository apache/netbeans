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
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class GetPreviousRevisionTest extends AbstractGitTestCase {
    private File workDir;

    public GetPreviousRevisionTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
    }
    
    public void testGetPreviousRevision () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        File[] files = new File[] { f };
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
        File f = new File(workDir, "f");
        File f2 = new File(workDir, "f2");
        write(f, "init");
        write(f2, "init");
        File[] files = new File[] { f, f2 };
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
