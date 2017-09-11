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
        return cherryPickedCommits.toArray(new GitRevisionInfo[cherryPickedCommits.size()]);
    }
    
}
