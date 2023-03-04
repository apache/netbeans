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

import java.io.File;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.submodule.SubmoduleStatus;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitConflictDescriptor.Type;
import org.netbeans.libs.git.GitRevertResult.Status;
import org.netbeans.libs.git.GitRevisionInfo.GitFileInfo;
import org.netbeans.libs.git.jgit.GitClassFactory;

/**
 *
 * @author ondra
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
    public GitBlameResult createBlameResult (BlameResult result, Repository repository) {
        return new GitBlameResult(result, repository);
    }

    @Override
    public GitBranch createBranch (String name, boolean remote, boolean active, ObjectId id) {
        return new GitBranch(name, remote, active, id);
    }

    @Override
    public GitConflictDescriptor createConflictDescriptor (Type type) {
        return new GitConflictDescriptor(type);
    }

    @Override
    public GitFileInfo createFileInfo (File file, String oldPath, GitFileInfo.Status status, File originalFile, String originalPath) {
        return new GitFileInfo(file, oldPath, status, originalFile, originalPath);
    }

    @Override
    public GitMergeResult createMergeResult (MergeResult mergeResult, File workTree) {
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
    public GitRebaseResult createRebaseResult (RebaseResult rebaseResult, List<File> rebaseConflicts, List<File> failures,
            String newHead) {
        return new GitRebaseResult(rebaseResult, rebaseConflicts, failures, newHead);
    }

    @Override
    public GitRemoteConfig createRemoteConfig (RemoteConfig remoteConfig) {
        return GitRemoteConfig.fromRemoteConfig(remoteConfig);
    }

    @Override
    public GitRevertResult createRevertResult (Status status, GitRevisionInfo commit, List<File> conflicts, List<File> failures) {
        return new GitRevertResult(status, commit, conflicts, failures);
    }

    @Override
    public GitRevisionInfo createRevisionInfo (RevCommit commit, Map<String, GitBranch> affectedBranches, Repository repository) {
        return new GitRevisionInfo(commit, affectedBranches, repository);
    }

    @Override
    public GitStatus createStatus (boolean tracked, String path, String workTreePath, File file, 
                GitStatus.Status statusHeadIndex, GitStatus.Status statusIndexWC, GitStatus.Status statusHeadWC, 
                GitConflictDescriptor conflictDescriptor, boolean folder, DiffEntry diffEntry,
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
    public GitTag createTag (RevTag revTag) {
        return new GitTag(revTag);
    }

    @Override
    public GitTag createTag (String tagName, RevObject revObject) {
        return new GitTag(tagName, revObject);
    }

    @Override
    public GitTag createTag (String tagName, GitRevisionInfo revCommit) {
        return new GitTag(tagName, revCommit);
    }

    @Override
    public GitTransportUpdate createTransportUpdate (URIish urI, TrackingRefUpdate update) {
        return new GitTransportUpdate(urI, update);
    }

    @Override
    public GitTransportUpdate createTransportUpdate(URIish urI, RemoteRefUpdate update, Map<String, GitBranch> remoteBranches, Map<String, String> remoteTags) {
        return new GitTransportUpdate(urI, update, remoteBranches, remoteTags);
    }

    @Override
    public GitUser createUser (PersonIdent personIdent) {
        if (personIdent == null) {
            personIdent = new PersonIdent("", ""); //NOI18N
        }
        return new GitUser(personIdent.getName(), personIdent.getEmailAddress());
    }
    
    @Override
    public void setBranchTracking (GitBranch branch, GitBranch trackedBranch) {
        branch.setTrackedBranch(trackedBranch);
    }

    @Override
    public GitSubmoduleStatus createSubmoduleStatus (SubmoduleStatus status, File folder) {
        return new GitSubmoduleStatus(status, folder);
    }

    @Override
    public GitCherryPickResult createCherryPickResult (GitCherryPickResult.CherryPickStatus status,
            List<File> conflicts, List<File> failures, GitRevisionInfo head,
            List<GitRevisionInfo> cherryPickedCommits) {
        return new GitCherryPickResult(status, conflicts, failures, head, cherryPickedCommits);
    }

}
