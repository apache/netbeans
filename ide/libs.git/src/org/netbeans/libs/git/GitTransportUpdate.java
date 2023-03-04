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

import java.util.Map;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.URIish;

/**
 * Represents a result of transport and update of a git reference between a local and remote
 * repository.
 * Instance of this class is usually returned by inter-repository commands (as fetch or push).
 * 
 * @author Ondra Vrabec
 */
public final class GitTransportUpdate {

    private final String localName;
    private final String remoteName;
    private final String oldObjectId;
    private final String newObjectId;
    private final GitRefUpdateResult result;
    private final String uri;
    private final Type type;

    /**
     * Git object kind.
     */
    public enum Type {
        /**
         * a branch
         */
        BRANCH,
        /**
         * a tag
         */
        TAG,
        /**
         * a general reference
         */
        REFERENCE
    }

    GitTransportUpdate (URIish uri, TrackingRefUpdate update) {
        this.localName = stripRefs(update.getLocalName());
        this.remoteName = stripRefs(update.getRemoteName());
        this.oldObjectId = update.getOldObjectId() == null || ObjectId.zeroId().equals(update.getOldObjectId()) ? null : update.getOldObjectId().getName();
        this.newObjectId = update.getNewObjectId() == null || ObjectId.zeroId().equals(update.getNewObjectId()) ? null : update.getNewObjectId().getName();
        this.result = GitRefUpdateResult.valueOf((update.getResult() == null 
                ? RefUpdate.Result.NOT_ATTEMPTED 
                : update.getResult()).name());
        this.uri = uri.toString();
        this.type = getType(update.getLocalName());
    }

    /**
     *
     * @param uri uri of the repo.
     * @param update
     * @param remoteBranches key value list of remote branches.
     * @param remoteTags key value list of remote tags. Key - name of the tag, value - id (hash) of the tag.
     */
    GitTransportUpdate(URIish uri, RemoteRefUpdate update, Map<String, GitBranch> remoteBranches, Map<String, String> remoteTags) {
        this.localName = stripRefs(update.getSrcRef());
        this.remoteName = stripRefs(update.getRemoteName());
        this.type = getType(update.getRemoteName());
        if (type == type.TAG) {
            //get object id for deleted tag.
            this.oldObjectId = remoteTags.get(remoteName);
        } else {
            this.oldObjectId = getOldRevisionId(remoteBranches.get(remoteName));
        }

        this.newObjectId = update.getNewObjectId() == null || ObjectId.zeroId().equals(update.getNewObjectId()) ? null : update.getNewObjectId().getName();
        this.result = GitRefUpdateResult.valueOf(update.getStatus().name());
        this.uri = uri.toString();
    }
    
    /**
     * @return URI of a remote repository
     */
    public String getRemoteUri () {
        return uri;
    }

    /**
     * Returns the name of the reference in a local repository without the prefix.
     * Instead of <code>refs/heads/master</code> <code>master</code> is returned.
     * @return name of the reference in a local repository without the reference prefix.
     */
    public String getLocalName () {
        return localName;
    }

    /**
     * Returns the name of the reference in a remote repository without the prefix.
     * Instead of <code>refs/heads/master</code> <code>master</code> is returned.
     * @return name of the reference in a remote repository without the reference prefix.
     */
    public String getRemoteName () {
        return remoteName;
    }

    /**
     * @return object id the reference pointed to before it was updated.
     */
    public String getOldObjectId () {
        return oldObjectId;
    }

    /**
     * @return object id the reference points to now, after the update.
     */
    public String getNewObjectId () {
        return newObjectId;
    }

    /**
     * Returns information about the result of the local/remote reference update.
     * @return result of the reference update
     */
    public GitRefUpdateResult getResult () {
        return result;
    }

    /**
     * @return kind of a git object this update refers to.
     */
    public Type getType () {
        return type;
    }

    private static String stripRefs (String refName) {
        if (refName == null) {
            
        } else if (refName.startsWith(Constants.R_HEADS)) {
            refName = refName.substring(Constants.R_HEADS.length());
        } else if (refName.startsWith(Constants.R_TAGS)) {
            refName = refName.substring(Constants.R_TAGS.length());
        } else if (refName.startsWith(Constants.R_REMOTES)) {
            refName = refName.substring(Constants.R_REMOTES.length());
        } else if (refName.startsWith(Constants.R_REFS)) {
            refName = refName.substring(Constants.R_REFS.length());
        } else {
            throw new IllegalArgumentException("Unknown refName: " + refName);
        }
        return refName;
    }

    private Type getType (String refName) {
        Type retval;
        if (refName.startsWith(Constants.R_TAGS)) {
            retval = Type.TAG;
        } else if (refName.startsWith(Constants.R_REMOTES)) {
            retval = Type.BRANCH;
        } else if (refName.startsWith(Constants.R_HEADS)) {
            retval = Type.BRANCH;
        } else if (refName.startsWith(Constants.R_REFS)) {
            retval = Type.REFERENCE;
        } else {
            throw new IllegalArgumentException("Unknown type for: " + refName);
        }
        return retval;
    }

    private String getOldRevisionId (GitBranch branch) {
        return branch == null ? null : branch.getId();
    }
}
