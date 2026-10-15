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

package org.netbeans.libs.git;

import org.eclipse.jgit.lib.ObjectId;

/**
 * Represents a local or remote branch in the local Git repository.
 * 
 * @author Ondra Vrabec
 */
public final class GitBranch {

    /**
     * Symbolic name for a detached HEAD.
     */
    public static final String NO_BRANCH = "(no branch)"; //NOI18N
    
    /**
     * A null branch instance. Usually used for just initialized repositories that contain no HEAD yet.
     */
    public static final GitBranch NO_BRANCH_INSTANCE = new GitBranch(NO_BRANCH, false, true, ObjectId.zeroId());

    private final String name;
    private final boolean remote;
    private final boolean active;
    private final ObjectId id;
    private GitBranch trackedBranch;

    GitBranch (String name, boolean remote, boolean active, ObjectId id) {
        this.name = name;
        this.remote = remote;
        this.active = active;
        this.id = id;
    }

    /**
     * @return name of the branch, prefixed with remote's name in case of remote branches.
     */
    public String getName () {
        return name;
    }

    /**
     * @return <code>true</code> when the branch represents a branch from a remote, <code>false</code> otherwise
     */
    public boolean isRemote () {
        return remote;
    }

    /**
     * @return <code>true</code> when the branch is checked out, <code>false</code> otherwise
     */
    public boolean isActive () {
        return active;
    }

    /**
     * @return commit id of the HEAD commit in the branch.
     */
    public String getId () {
        return ObjectId.toString(id);
    }
    
    /**
     * @return tracked branch, <code>null</code> when no tracking is set.
     */
    public GitBranch getTrackedBranch () {
        return trackedBranch;
    }
    
    // ************* package-private ************** //
    
    void setTrackedBranch (GitBranch trackedBranch) {
        this.trackedBranch = trackedBranch;
    }

    @Override
    public String toString() {
        return "GitBranch{"
                + "name=" + name
                + ", id=" + getId()
                + ", remote=" + remote
                + ", active=" + active
                + ", trackedBranch=" + trackedBranch
                + '}';
    }

}
