/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
