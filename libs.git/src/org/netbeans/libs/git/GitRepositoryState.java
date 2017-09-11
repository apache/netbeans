/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
