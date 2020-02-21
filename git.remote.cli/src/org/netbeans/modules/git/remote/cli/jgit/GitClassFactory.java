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
package org.netbeans.modules.git.remote.cli.jgit;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitBlameResult;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitCherryPickResult;
import org.netbeans.modules.git.remote.cli.GitConflictDescriptor;
import org.netbeans.modules.git.remote.cli.GitConflictDescriptor.Type;
import org.netbeans.modules.git.remote.cli.GitMergeResult;
import org.netbeans.modules.git.remote.cli.GitPullResult;
import org.netbeans.modules.git.remote.cli.GitPushResult;
import org.netbeans.modules.git.remote.cli.GitRebaseResult;
import org.netbeans.modules.git.remote.cli.GitRevertResult;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo.GitFileInfo;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.GitStatus.GitDiffEntry;
import org.netbeans.modules.git.remote.cli.GitStatus.Status;
import org.netbeans.modules.git.remote.cli.GitSubmoduleStatus;
import org.netbeans.modules.git.remote.cli.GitTag;
import org.netbeans.modules.git.remote.cli.GitTransportUpdate;
import org.netbeans.modules.git.remote.cli.GitUser;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public abstract class GitClassFactory {
    
    public abstract GitBlameResult createBlameResult (VCSFileProxy file, Map<String, GitBlameResult.GitBlameContent> result, JGitRepository repository);
    
    public abstract GitBranch createBranch (String name, boolean remote, boolean active, String id);

    public abstract GitCherryPickResult createCherryPickResult (
            GitCherryPickResult.CherryPickStatus status, List<VCSFileProxy> conflicts,
            List<VCSFileProxy> failures, GitRevisionInfo head, List<GitRevisionInfo> cherryPickedCommits);

    public abstract GitConflictDescriptor createConflictDescriptor (Type type);

    public abstract GitFileInfo createFileInfo (VCSFileProxy file, String oldPath, GitFileInfo.Status status, VCSFileProxy originalFile, String originalPath);
    
    public abstract GitMergeResult createMergeResult (GitMergeResult.MergeResultContainer mergeResult, VCSFileProxy workTree);

    public abstract GitPullResult createPullResult (Map<String, GitTransportUpdate> fetchUpdates, GitMergeResult mergeResult);

    public abstract GitPushResult createPushResult (Map<String, GitTransportUpdate> remoteRepositoryUpdates, Map<String, GitTransportUpdate> localRepositoryUpdates);
    
    public abstract GitRebaseResult createRebaseResult (GitRebaseResult.RebaseResultContainer rebaseResult);

    //public abstract GitRemoteConfig createRemoteConfig (RemoteConfig remoteConfig);

    public abstract GitRevertResult createRevertResult (GitRevertResult.Status status, GitRevisionInfo createRevisionInfo, List<VCSFileProxy> conflicts, List<VCSFileProxy> failures);
        
    //public final GitRevisionInfo createRevisionInfo (RevCommit commit, JGitRepository repository) {
    //    return createRevisionInfo(commit, Collections.<String, GitBranch>emptyMap(), repository);
    //}
    
    //public abstract GitRevisionInfo createRevisionInfo (RevCommit commit, Map<String, GitBranch> affectedBranches, JGitRepository repository);
    
    public final GitRevisionInfo createRevisionInfo(GitRevisionInfo.GitRevCommit status, JGitRepository repository) {
        return createRevisionInfo(status, Collections.<String, GitBranch>emptyMap(), repository);
    }

    public abstract GitRevisionInfo createRevisionInfo(GitRevisionInfo.GitRevCommit status, Map<String, GitBranch> affectedBranches, JGitRepository repository);

    public abstract GitStatus createStatus (boolean tracked, String path, String workTreePath, VCSFileProxy file, 
                Status statusHeadIndex, Status statusIndexWC, Status statusHeadWC, 
                GitConflictDescriptor conflictDescriptor, boolean folder, GitDiffEntry diffEntry,
                long indexEntryTimestamp);
    
    public abstract GitSubmoduleStatus createSubmoduleStatus (GitSubmoduleStatus.StatusType status, VCSFileProxy folder);

    public abstract GitTag createTag (GitTag.TagContainer revTag);

    //public abstract GitTag createTag (String tagName, RevObject revObject);

    //public abstract GitTag createTag (String tagName, GitRevisionInfo revCommit);

    //public abstract GitTransportUpdate createTransportUpdate (URIish urI, TrackingRefUpdate update);

    //public abstract GitTransportUpdate createTransportUpdate (URIish urI, RemoteRefUpdate update, Map<String, GitBranch> remoteBranches);

    public abstract GitTransportUpdate createTransportUpdate (GitTransportUpdate.GitTransportUpdateContainer container);

    public abstract GitUser createUser (String name, String mail);

    public abstract void setBranchTracking (GitBranch branch, GitBranch trackedBranch);

}
