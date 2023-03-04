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
package org.netbeans.libs.git.jgit;

import java.io.File;
import java.util.Collections;
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
import org.netbeans.libs.git.GitBlameResult;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitCherryPickResult;
import org.netbeans.libs.git.GitConflictDescriptor;
import org.netbeans.libs.git.GitConflictDescriptor.Type;
import org.netbeans.libs.git.GitMergeResult;
import org.netbeans.libs.git.GitPullResult;
import org.netbeans.libs.git.GitPushResult;
import org.netbeans.libs.git.GitRebaseResult;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitRevertResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitRevisionInfo.GitFileInfo;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.GitSubmoduleStatus;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.libs.git.GitUser;

/**
 *
 * @author ondra
 */
public abstract class GitClassFactory {
    
    public abstract GitBlameResult createBlameResult (BlameResult result, Repository repository);
    
    public abstract GitBranch createBranch (String name, boolean remote, boolean active, ObjectId id);

    public abstract GitCherryPickResult createCherryPickResult (
            GitCherryPickResult.CherryPickStatus status, List<File> conflicts,
            List<File> failures, GitRevisionInfo head, List<GitRevisionInfo> cherryPickedCommits);

    public abstract GitConflictDescriptor createConflictDescriptor (Type type);

    public abstract GitFileInfo createFileInfo (File file, String oldPath, GitFileInfo.Status status, File originalFile, String originalPath);
    
    public abstract GitMergeResult createMergeResult (MergeResult mergeResult, File workTree);

    public abstract GitPullResult createPullResult (Map<String, GitTransportUpdate> fetchUpdates, GitMergeResult mergeResult);

    public abstract GitPushResult createPushResult (Map<String, GitTransportUpdate> remoteRepositoryUpdates, Map<String, GitTransportUpdate> localRepositoryUpdates);
    
    public abstract GitRebaseResult createRebaseResult (RebaseResult rebaseResult, List<File> rebaseConflicts, List<File> failures,
            String newHead);

    public abstract GitRemoteConfig createRemoteConfig (RemoteConfig remoteConfig);

    public abstract GitRevertResult createRevertResult (GitRevertResult.Status status, GitRevisionInfo createRevisionInfo, List<File> conflicts, List<File> failures);
    
    public final GitRevisionInfo createRevisionInfo (RevCommit commit, Repository repository) {
        return createRevisionInfo(commit, Collections.<String, GitBranch>emptyMap(), repository);
    }
    
    public abstract GitRevisionInfo createRevisionInfo (RevCommit commit, Map<String, GitBranch> affectedBranches, Repository repository);

    public abstract GitStatus createStatus (boolean tracked, String path, String workTreePath, File file, 
                Status statusHeadIndex, Status statusIndexWC, Status statusHeadWC, 
                GitConflictDescriptor conflictDescriptor, boolean folder, DiffEntry diffEntry,
                long indexEntryTimestamp);
    
    public abstract GitSubmoduleStatus createSubmoduleStatus (SubmoduleStatus status, File folder);

    public abstract GitTag createTag (RevTag revTag);

    public abstract GitTag createTag (String tagName, RevObject revObject);

    public abstract GitTag createTag (String tagName, GitRevisionInfo revCommit);

    public abstract GitTransportUpdate createTransportUpdate (URIish urI, TrackingRefUpdate update);

    public abstract GitTransportUpdate createTransportUpdate(URIish urI, RemoteRefUpdate update, Map<String, GitBranch> remoteBranches, Map<String, String> remoteTags);

    public abstract GitUser createUser (PersonIdent personIdent);

    public abstract void setBranchTracking (GitBranch branch, GitBranch trackedBranch);

}
