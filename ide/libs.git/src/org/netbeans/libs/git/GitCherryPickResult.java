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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Returned by a git cherry-pick command, represents its result.
 *
 * @author Ondra Vrabec
 * @since 1.27
 */
public final class GitCherryPickResult {

    private final CherryPickStatus status;
    private final List<File> conflicts;
    private final List<File> failures;
    private final GitRevisionInfo currentHead;
    private final List<GitRevisionInfo> cherryPickedCommits;

    /**
     * The status rebase resulted in.
     */
    public enum CherryPickStatus {
        /**
         * Command successfully finished. No action is required.
         */
        OK,
        /**
         * Command was aborted and reset to the original state. No action is
         * required.
         */
        ABORTED,
        /**
         * Failed because a dirty working tree prevents from starting the command.
         * Local modifications preventing from applying commit changes must be 
         * reverted.
         */
        FAILED,
        /**
         * The cherry-picking stopped in a state where it requires a manual commit.
         * E.g. after resolving conflicts client is required to commit the changes
         * before continuing with rebase.
         */
        UNCOMMITTED,
        /**
         * Conflicts when merging the cherry-picked commits.
         * Conflicts must be resolved and the command must be continued/aborted.
         */
        CONFLICTING;
    }

    GitCherryPickResult (CherryPickStatus status, List<File> conflicts, List<File> failures,
            GitRevisionInfo currentHead, List<GitRevisionInfo> cherryPickedCommits) {
        this.status = status;
        this.currentHead = currentHead;
        this.conflicts = conflicts;
        this.failures = failures;
        this.cherryPickedCommits = cherryPickedCommits;
    }

    /**
     * @return result of the cherry-pick command.
     */
    public CherryPickStatus getCherryPickStatus () {
        return status;
    }

    /**
     * @return current HEAD commit after the cherry-pick command.
     */
    public GitRevisionInfo getCurrentHead () {
        return currentHead;
    }

    /**
     * If the cherry-pick started but was unable to finish because of unresolved
     * conflicts then the method returns a collection of such files in conflict.
     * To complete the command you need to resolve the conflicts and continue the
     * unfinished command.
     *
     * @return files in conflict
     */
    public Collection<File> getConflicts () {
        return Collections.unmodifiableList(conflicts);
    }

    /**
     * When the command fails because of local modifications then this method
     * returns a collections of files causing the failure.
     *
     * @return files that cause the cherry-pick to fail.
     */
    public Collection<File> getFailures () {
        return Collections.unmodifiableList(failures);
    }

    /**
     * Returns commits cherry-picked to the current branch by the last run of the 
     * cherry-pick command.
     * 
     * @return array of commits cherry-picked to head.
     */
    public GitRevisionInfo[] getCherryPickedCommits () {
        return cherryPickedCommits.toArray(new GitRevisionInfo[0]);
    }
    
}
