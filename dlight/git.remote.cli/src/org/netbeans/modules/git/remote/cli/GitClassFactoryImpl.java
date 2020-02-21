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
package org.netbeans.modules.git.remote.cli;

import java.util.List;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitConflictDescriptor.Type;
import org.netbeans.modules.git.remote.cli.GitRevertResult.Status;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo.GitFileInfo;
import org.netbeans.modules.git.remote.cli.GitStatus.GitDiffEntry;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
final class GitClassFactoryImpl extends GitClassFactory {
    private static GitClassFactoryImpl instance;

    static synchronized GitClassFactory getInstance () {
        if (instance == null) {
            instance = new GitClassFactoryImpl();
        }
        return instance;
    }

    @Override
    public GitBlameResult createBlameResult (VCSFileProxy file, Map<String, GitBlameResult.GitBlameContent> result, JGitRepository repository) {
        return new GitBlameResult(file, result, repository);
    }

    @Override
    public GitBranch createBranch (String name, boolean remote, boolean active, String id) {
        return new GitBranch(name, remote, active, id);
    }

    @Override
    public GitConflictDescriptor createConflictDescriptor (Type type) {
        return new GitConflictDescriptor(type);
    }

    @Override
    public GitFileInfo createFileInfo (VCSFileProxy file, String relativePath, GitFileInfo.Status status, VCSFileProxy originalFile, String originalPath) {
        return new GitFileInfo(file, relativePath, status, originalFile, originalPath);
    }

    @Override
    public GitMergeResult createMergeResult (GitMergeResult.MergeResultContainer mergeResult, VCSFileProxy workTree) {
        return new GitMergeResult(mergeResult, workTree);
    }

    @Override
    public GitPullResult createPullResult (Map<String, GitTransportUpdate> fetchUpdates, GitMergeResult mergeResult) {
        return new GitPullResult(fetchUpdates, mergeResult);
    }

    @Override
    public GitPushResult createPushResult (Map<String, GitTransportUpdate> remoteRepositoryUpdates, Map<String, GitTransportUpdate> localRepositoryUpdates) {
        return new GitPushResult(remoteRepositoryUpdates, localRepositoryUpdates);
    }

    @Override
    public GitRebaseResult createRebaseResult (GitRebaseResult.RebaseResultContainer rebaseResult) {
        return new GitRebaseResult(rebaseResult);
    }

//    @Override
//    public GitRemoteConfig createRemoteConfig (RemoteConfig remoteConfig) {
//        return GitRemoteConfig.fromRemoteConfig(remoteConfig);
//    }

    @Override
    public GitRevertResult createRevertResult (Status status, GitRevisionInfo commit, List<VCSFileProxy> conflicts, List<VCSFileProxy> failures) {
        return new GitRevertResult(status, commit, conflicts, failures);
    }

//    @Override
//    public GitRevisionInfo createRevisionInfo (RevCommit commit, Map<String, GitBranch> affectedBranches, JGitRepository repository) {
//        return new GitRevisionInfo(commit, affectedBranches, repository);
//    }

    @Override
    public GitRevisionInfo createRevisionInfo(GitRevisionInfo.GitRevCommit status, Map<String, GitBranch> affectedBranches, JGitRepository repository) {
        return new GitRevisionInfo(status, affectedBranches, repository);
    }

    @Override
    public GitStatus createStatus (boolean tracked, String path, String workTreePath, VCSFileProxy file, 
                GitStatus.Status statusHeadIndex, GitStatus.Status statusIndexWC, GitStatus.Status statusHeadWC, 
                GitConflictDescriptor conflictDescriptor, boolean folder, GitDiffEntry diffEntry,
                long indexEntryTimestamp) {
        GitStatus status = new GitStatus(workTreePath, file, path, tracked);
        status.setDiffEntry(diffEntry);
        status.setConflictDescriptor(conflictDescriptor);
        status.setFolder(folder);
        status.setStatusHeadIndex(statusHeadIndex);
        status.setStatusHeadWC(statusHeadWC);
        status.setStatusIndexWC(statusIndexWC);
        status.setIndexEntryModificationDate(indexEntryTimestamp);
        return status;
    }

    @Override
    public GitTag createTag (GitTag.TagContainer revTag) {
        return new GitTag(revTag);
    }

//    @Override
//    public GitTag createTag (String tagName, RevObject revObject) {
//        return new GitTag(tagName, revObject);
//    }

//    @Override
//    public GitTag createTag (String tagName, GitRevisionInfo revCommit) {
//        return new GitTag(tagName, revCommit);
//    }

//    @Override
//    public GitTransportUpdate createTransportUpdate (URIish urI, TrackingRefUpdate update) {
//        return new GitTransportUpdate(urI, update);
//    }

//    @Override
//    public GitTransportUpdate createTransportUpdate (URIish urI, RemoteRefUpdate update, Map<String, GitBranch> remoteBranches) {
//        return new GitTransportUpdate(urI, update, remoteBranches);
//    }
    
    @Override
    public GitTransportUpdate createTransportUpdate (GitTransportUpdate.GitTransportUpdateContainer container) {
        return new GitTransportUpdate(container);
    }

    @Override
    public GitUser createUser (String name, String mail) {
        return new GitUser(name, mail);
    }
    
    @Override
    public void setBranchTracking (GitBranch branch, GitBranch trackedBranch) {
        branch.setTrackedBranch(trackedBranch);
    }

    @Override
    public GitSubmoduleStatus createSubmoduleStatus (GitSubmoduleStatus.StatusType status, VCSFileProxy folder) {
        return new GitSubmoduleStatus(status, folder);
    }

    @Override
    public GitCherryPickResult createCherryPickResult (GitCherryPickResult.CherryPickStatus status,
            List<VCSFileProxy> conflicts, List<VCSFileProxy> failures, GitRevisionInfo head,
            List<GitRevisionInfo> cherryPickedCommits) {
        return new GitCherryPickResult(status, conflicts, failures, head, cherryPickedCommits);
    }

}
