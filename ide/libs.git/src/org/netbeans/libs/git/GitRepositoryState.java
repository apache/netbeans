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

import org.eclipse.jgit.lib.RepositoryState;
import org.netbeans.libs.git.jgit.Utils;

/**
 * Represents th state a repository is currently in. The state implies what git commands
 * may be called on the repository and what should be the next steps to get the repository
 * into a normal state.
 * @author Ondra Vrabec
 */
public enum GitRepositoryState {
        /** Has no work tree and cannot be used for normal editing. */
	BARE {
            @Override
            public boolean canCheckout () { return false; }
            @Override
            public boolean canResetHead () { return false; }
            @Override
            public boolean canCommit () { return false; }
            @Override
            public String toString () { return Utils.getBundle(GitRepositoryState.class).getString("LBL_RepositoryInfo_Bare"); } //NOI18N
	},

	/**
	 * A safe state for working normally
	 * */
	SAFE {
            @Override
            public boolean canCheckout () { return true; }
            @Override
            public boolean canResetHead () { return true; }
            @Override
            public boolean canCommit () { return true; }
            @Override
            public String toString () { return Utils.getBundle(GitRepositoryState.class).getString("LBL_RepositoryInfo_Safe"); } //NOI18N
	},

	/**
     * An unfinished merge. Must resolve or reset before continuing normally
	 */
	MERGING {
            @Override
            public boolean canCheckout () { return false; }
            @Override
            public boolean canResetHead () { return true; }
            @Override
            public boolean canCommit () { return false; }
            @Override
            public String toString () { return Utils.getBundle(GitRepositoryState.class).getString("LBL_RepositoryInfo_Merging"); } //NOI18N
	},

	/**
	 * A merge where all conflicts have been resolved. The index does not
	 * contain any unmerged paths.
	 */
	MERGING_RESOLVED {
            @Override
            public boolean canCheckout () { return true; }
            @Override
            public boolean canResetHead () { return true; }
            @Override
            public boolean canCommit () { return true; }
            @Override
            public String toString () { return Utils.getBundle(GitRepositoryState.class).getString("LBL_RepositoryInfo_Merged"); } //NOI18N
	},

	/**
     * An unfinished cherry-pick. Must resolve or reset before continuing normally
     * @since 1.27
	 */
	CHERRY_PICKING {
            @Override
            public boolean canCheckout () { return false; }
            @Override
            public boolean canResetHead () { return true; }
            @Override
            public boolean canCommit () { return false; }
            @Override
            public String toString () { return Utils.getBundle(GitRepositoryState.class).getString("LBL_RepositoryInfo_CherryPicking"); } //NOI18N
	},

	/**
	 * A cherry-picked commit where all conflicts have been resolved. The index does not
	 * contain any unmerged paths and the repository requires a commit.
     * @since 1.27
	 */
	CHERRY_PICKING_RESOLVED {
            @Override
            public boolean canCheckout () { return true; }
            @Override
            public boolean canResetHead () { return true; }
            @Override
            public boolean canCommit () { return true; }
            @Override
            public String toString () { return Utils.getBundle(GitRepositoryState.class).getString("LBL_RepositoryInfo_CherryPickingResolved"); } //NOI18N
	},

	/**
	 * An unfinished rebase or am. Must resolve, skip or abort before normal work can take place
	 */
	REBASING {
            @Override
            public boolean canCheckout () { return false; }
            @Override
            public boolean canResetHead () { return false; }
            @Override
            public boolean canCommit () { return true; }
            @Override
            public String toString () { return Utils.getBundle(GitRepositoryState.class).getString("LBL_RepositoryInfo_Rebasing"); } //NOI18N
	},

	/**
	 * An unfinished apply. Must resolve, skip or abort before normal work can take place
	 */
	APPLY {
            @Override
            public boolean canCheckout () { return false; }
            @Override
            public boolean canResetHead () { return false; }
            @Override
            public boolean canCommit () { return true; }
            @Override
            public String toString () { return Utils.getBundle(GitRepositoryState.class).getString("LBL_RepositoryInfo_Apply"); } //NOI18N
	},

	/**
	 * Bisecting being done. Normal work may continue but is discouraged
	 */
	BISECTING {
            /* Changing head is a normal operation when bisecting */
            @Override
            public boolean canCheckout () { return true; }

            /* Do not reset, checkout instead */
            @Override
            public boolean canResetHead () { return false; }

            /* Commit during bisect is useful */
            @Override
            public boolean canCommit () { return true; }

            @Override
            public String toString () { return Utils.getBundle(GitRepositoryState.class).getString("LBL_RepositoryInfo_Bisecting"); } //NOI18N
	};

	/**
	 * @return true if changing HEAD is sane.
	 */
	public abstract boolean canCheckout ();

	/**
	 * @return true if we can commit
	 */
	public abstract boolean canCommit ();

	/**
	 * @return true if reset to another HEAD is considered SAFE
	 */
	public abstract boolean canResetHead ();

        static GitRepositoryState getStateFor (RepositoryState state) {
            switch (state) {
                case APPLY:
                    return GitRepositoryState.APPLY;
                case BARE:
                    return GitRepositoryState.BARE;
                case BISECTING:
                    return GitRepositoryState.BISECTING;
                case MERGING:
                case REVERTING:
                    return GitRepositoryState.MERGING;
                case CHERRY_PICKING:
                    return GitRepositoryState.CHERRY_PICKING;
                case CHERRY_PICKING_RESOLVED:
                    return GitRepositoryState.CHERRY_PICKING_RESOLVED;
                case MERGING_RESOLVED:
                case REVERTING_RESOLVED:
                    return GitRepositoryState.MERGING_RESOLVED;
                case REBASING:
                case REBASING_INTERACTIVE:
                case REBASING_MERGE:
                case REBASING_REBASING:
                    return GitRepositoryState.REBASING;
                case SAFE:
                    return GitRepositoryState.SAFE;
                default:
                    throw new IllegalStateException(state.getDescription());
            }
        }
}
