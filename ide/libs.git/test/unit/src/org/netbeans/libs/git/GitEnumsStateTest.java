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

package org.netbeans.libs.git;

import java.io.IOException;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.submodule.SubmoduleStatusType;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class GitEnumsStateTest extends AbstractGitTestCase {

    public GitEnumsStateTest (String testName) throws IOException {
        super(testName);
    }

    public void testUpdateResult () {
        for (RefUpdate.Result result : RefUpdate.Result.values()) {
            assertNotNull(GitRefUpdateResult.valueOf(result.name()));
        }
    }
    
    public void testMergeStatus () {
        for (MergeResult.MergeStatus status : MergeResult.MergeStatus.values()) {
            assertNotNull(GitMergeResult.parseMergeStatus(status));
        }
    }

    public void testRemoteUpdateStatus () {
        for (RemoteRefUpdate.Status status : RemoteRefUpdate.Status.values()) {
            assertNotNull(GitRefUpdateResult.valueOf(status.name()));
        }
    }

    public void testRepositoryState () {
        for (RepositoryState state : RepositoryState.values()) {
            assertNotNull(GitRepositoryState.getStateFor(state));
        }
    }
    
    public void testRebaseStatus () {
        for (RebaseResult.Status status : RebaseResult.Status.values()) {
            assertNotNull(GitRebaseResult.parseRebaseStatus(status));
        }
    }
    
    public void testSubmoduleStatus () {
        for (SubmoduleStatusType status : SubmoduleStatusType.values()) {
            assertNotNull(GitSubmoduleStatus.parseStatus(status));
        }
    }
    
    public void testCherryPickStatus () {
        for (CherryPickResult.CherryPickStatus status : CherryPickResult.CherryPickStatus.values()) {
            assertNotNull(GitCherryPickResult.CherryPickStatus.valueOf(status.name()));
        }
    }
}
