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
import java.util.Collection;
import java.util.List;
import org.eclipse.jgit.api.RebaseResult;

/**
 * Returned by a git rebase command, represents its result.
 *
 * @author Ondra Vrabec
 * @since 1.8
 */
public final class GitRebaseResult {

    private final RebaseStatus rebaseStatus;
    private final List<File> conflicts;
    private final List<File> failures;
    private final String currentHead;
    private final String currentCommit;

    /**
     * The status rebase resulted in.
     */
    public enum RebaseStatus {
        /**
         * Rebase successfully finished. No action is required.
         */
        OK {
            @Override
            public boolean isSuccessful () {
                return true;
            }
        },
        /**
         * Rebase was aborted and reset to the original state. No action is
         * required.
         */
        ABORTED {
            @Override
            public boolean isSuccessful () {
                return false;
            }
        },
        /**
         * Stopped due to a conflict. Must be either aborted, resolved or the
         * commit must be skipped from the rebase.
         */
        STOPPED {
            @Override
            public boolean isSuccessful () {
                return false;
            }
        },
        /**
         * Failed and reset to the original state.
         * Local modifications preventing from applying commit changes must be 
         * reverted.
         */
        FAILED {
            @Override
            public boolean isSuccessful () {
                return false;
            }
        },
        /**
         * Conflicts when checking out the target HEAD.
         * Local modifications preventing the checkout must be reverted.
         */
        CONFLICTS {
            @Override
            public boolean isSuccessful () {
                return false;
            }
        },
        /**
         * Already up-to-date. No action is required.
         */
        UP_TO_DATE {
            @Override
            public boolean isSuccessful () {
                return true;
            }
        },
        /**
         * Fast-forward, HEAD points to the new commit. No action is required.
         */
        FAST_FORWARD {
            @Override
            public boolean isSuccessful () {
                return true;
            }
        },
        /**
         * Continue with nothing left to commit. Rebase commit does not bring
         * any modifications. A skip of the commit or complete rebase abort is
         * required.
         */
        NOTHING_TO_COMMIT {
            @Override
            public boolean isSuccessful () {
                return false;
            }
        };

        /**
         * @return whether the status indicates a successful result
         */
        public abstract boolean isSuccessful ();
    }

    GitRebaseResult (RebaseResult result, List<File> rebaseConflicts, List<File> failures, String currentHead) {
        this.rebaseStatus = parseRebaseStatus(result.getStatus());
        this.currentHead = currentHead;
        if (result.getCurrentCommit() == null) {
            this.currentCommit = null;
        } else {
            this.currentCommit = result.getCurrentCommit().getId().getName();
        }
        this.conflicts = rebaseConflicts;
        this.failures = failures;
    }

    /**
     * @return result of the rebase.
     */
    public RebaseStatus getRebaseStatus () {
        return rebaseStatus;
    }

    /**
     * @return current HEAD commit after the rebase.
     */
    public String getCurrentHead () {
        return currentHead;
    }

    /**
     * @return the last rebasing commit resulting in the
     * {@link RebaseStatus#STOPPED} state, or <code>null</code> if the result is
     * different.
     */
    public String getCurrentCommit () {
        return currentCommit;
    }

    /**
     * If the rebase started but was unable to finish because of unresolved
     * conflicts then the method returns a collection of such files in conflict.
     * To complete the rebase you need to resolve the conflicts and continue the
     * rebase.
     *
     * @return files in conflict
     */
    public Collection<File> getConflicts () {
        return conflicts;
    }

    /**
     * When the rebase fails because of local modifications (i.e. it finishes
     * with {@link RebaseStatus#CONFLICTS} or {@link RebaseStatus#FAILED}) then
     * this method returns a collections of files causing the failure.
     *
     * @return files that cause the rebase to fail.
     */
    public Collection<File> getFailures () {
        return failures;
    }

    static RebaseStatus parseRebaseStatus (RebaseResult.Status rebaseStatus) {
        switch (rebaseStatus) {
            case EDIT:
                return RebaseStatus.STOPPED;
            case UNCOMMITTED_CHANGES:
                return RebaseStatus.FAILED;
            case INTERACTIVE_PREPARED:
                return RebaseStatus.STOPPED;
            case STASH_APPLY_CONFLICTS:
                return RebaseStatus.CONFLICTS;
                
        }
        return GitRebaseResult.RebaseStatus.valueOf(rebaseStatus.name());
    }
}
