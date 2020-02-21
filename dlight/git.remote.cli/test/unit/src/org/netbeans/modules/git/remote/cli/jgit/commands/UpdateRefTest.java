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
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitRefUpdateResult;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class UpdateRefTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;

    public UpdateRefTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testNotAttempted","testMoveMergeCommit","testMoveMergeRef","testMoveMergeRejected").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testNotAttempted () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
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
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
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
        GitRevisionInfo resolve = client.resolve(GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        //ReflogReader reflogReader = repository.getReflogReader("master");
        //assertEquals("merge " + info.getRevision() + ": Fast-forward", reflogReader.getLastEntry().getComment());
    }

    public void testMoveMergeRef () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
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
        GitRevisionInfo resolve = client.resolve(GitConstants.MASTER, NULL_PROGRESS_MONITOR);
        //ReflogReader reflogReader = repository.getReflogReader("master");
        //assertEquals("merge BRANCH: Fast-forward", reflogReader.getLastEntry().getComment());
    }

    // must fail if would end in a non FF update
    public void testMoveMergeRejected () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
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
