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
import org.eclipse.jgit.lib.ReflogReader;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitRefUpdateResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class UpdateRefTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public UpdateRefTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testNotAttempted () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        write(f, "modi");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.log("HEAD", NULL_PROGRESS_MONITOR);
        
        client.reset("HEAD~1", GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        
        GitRefUpdateResult res = client.updateReference("HEAD", info.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(GitRefUpdateResult.NOT_ATTEMPTED, res);
    }

    public void testMoveMergeCommit () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        write(f, "modif");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.log("HEAD", NULL_PROGRESS_MONITOR);
        
        client.reset("HEAD~1", GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        
        GitRefUpdateResult res = client.updateReference("master", info.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(GitRefUpdateResult.FAST_FORWARD, res);
        ReflogReader reflogReader = repository.getReflogReader("master");
        assertEquals("merge " + info.getRevision() + ": Fast-forward", reflogReader.getLastEntry().getComment());
    }

    public void testMoveMergeRef () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        write(f, "modif");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch("BRANCH", "HEAD", NULL_PROGRESS_MONITOR);
        
        client.reset("HEAD~1", GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        
        GitRefUpdateResult res = client.updateReference("master", "BRANCH", NULL_PROGRESS_MONITOR);
        assertEquals(GitRefUpdateResult.FAST_FORWARD, res);
        ReflogReader reflogReader = repository.getReflogReader("master");
        assertEquals("merge BRANCH: Fast-forward", reflogReader.getLastEntry().getComment());
    }

    // must fail if would end in a non FF update
    public void testMoveMergeRejected () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        write(f, "modif");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch("BRANCH", "HEAD", NULL_PROGRESS_MONITOR);
        
        client.reset("HEAD~1", GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        
        write(f, "modif2");
        add(f);
        commit(f);
        GitRefUpdateResult res = client.updateReference("master", "BRANCH", NULL_PROGRESS_MONITOR);
        assertEquals(GitRefUpdateResult.REJECTED, res);
    }
}
