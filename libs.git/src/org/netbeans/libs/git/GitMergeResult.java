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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.merge.ResolveMerger;

/**
 * Returned by a git merge command, represents its result.
 * 
 * @author Ondra Vrabec
 */
public final class GitMergeResult {

    private final MergeStatus mergeStatus;
    private final File workDir;
    private final List<File> conflicts;
    private final List<File> failures;
    private final String newHead;
    private final String base;
    private final String[] mergedCommits;

    /**
     * The status the merge resulted in.
     */
    public enum MergeStatus {

        FAST_FORWARD {
            @Override
            public String toString() {
                return "Fast-forward";
            }
        },
        /**
         * Fast forward merge cannot be executed, a commit is needed.
         * @since 1.26
         */
        ABORTED {
            @Override
            public String toString() {
                return "Aborted";
            }
        },
        ALREADY_UP_TO_DATE {
            @Override
            public String toString() {
                return "Already up-to-date";
            }
        },
        FAILED {
            @Override
            public String toString() {
                return "Failed";
            }
        },
        MERGED {
            @Override
            public String toString() {
                return "Merged";
            }
        },
        CONFLICTING {
            @Override
            public String toString() {
                return "Conflicting";
            }
        },
        NOT_SUPPORTED {
            @Override
            public String toString() {
                return "Not-yet-supported";
            }
        }
    }
    
    GitMergeResult (MergeResult result, File workDir) {
        this.mergeStatus = parseMergeStatus(result.getMergeStatus());
        this.workDir = workDir;
        this.newHead = result.getNewHead() == null ? null : result.getNewHead().getName();
        this.base = result.getBase() == null ? null : result.getBase().getName();
        this.mergedCommits = getMergedCommits(result);
        this.conflicts = getConflicts(result);
        this.failures = getFailures(result);
    }
    
    /**
     * @return result of the merge.
     */
    public MergeStatus getMergeStatus () {
        return mergeStatus;
    }
    
    /**
     * @return the common base which was used to produce a content-merge.
     *         May be <code>null</code> if the result was produced without
     *         computing a common base
     */
    public String getBase () {
        return base;
    }
    
    /**
     * @return ids of commits merged
     */
    public String[] getMergedCommits () {
        return mergedCommits;
    }
    
    /**
     * @return current HEAD commit after the successful merge or <code>null</code> if the merge failed.
     */
    public String getNewHead () {
        return newHead;
    }
    
    /**
     * If the merge started but was unable to finish because of unresolved conflicts then the method
     * returns a collection of such files in conflict.
     * To complete the merge you need to resolve the conflicts and commit the changes.
     * @return files in conflict
     */
    public Collection<File> getConflicts() {
        return conflicts;
    }
    
    /**
     * When the merge fails because of local modifications then this 
     * method returns a collections of files causing the failure.
     * @return files that cause the merge to fail.
     */
    public Collection<File> getFailures () {
        return failures;
    }

    static MergeStatus parseMergeStatus (MergeResult.MergeStatus mergeStatus) {
        if (mergeStatus == MergeResult.MergeStatus.FAST_FORWARD_SQUASHED) {
            mergeStatus = MergeResult.MergeStatus.FAST_FORWARD;
        } else if (mergeStatus == MergeResult.MergeStatus.MERGED_SQUASHED) {
            mergeStatus = MergeResult.MergeStatus.MERGED;
        } else if (mergeStatus == MergeResult.MergeStatus.MERGED_NOT_COMMITTED) {
            mergeStatus = MergeResult.MergeStatus.MERGED;
        } else if (mergeStatus == MergeResult.MergeStatus.MERGED_SQUASHED_NOT_COMMITTED) {
            mergeStatus = MergeResult.MergeStatus.MERGED;
        } else if (mergeStatus == MergeResult.MergeStatus.CHECKOUT_CONFLICT) {
            mergeStatus = MergeResult.MergeStatus.CONFLICTING;
        }
        return GitMergeResult.MergeStatus.valueOf(mergeStatus.name());
    }

    private String[] getMergedCommits (MergeResult result) {
        ObjectId[] mergedObjectIds = result.getMergedCommits();
        String[] commits = new String[mergedObjectIds.length];
        for (int i = 0; i < mergedObjectIds.length; ++i) {
            commits[i] = ObjectId.toString(mergedObjectIds[i]);
        }
        return commits;
    }

    private List<File> getConflicts(MergeResult result) {
        List<File> files = new LinkedList<File>();
        Map<String, int[][]> mergeConflicts = result.getConflicts();
        if (mergeConflicts != null) {
            for (Map.Entry<String, int[][]> conflict : mergeConflicts.entrySet()) {
                files.add(new File(workDir, conflict.getKey()));
            }
        }
        return Collections.unmodifiableList(files);
    }

    private List<File> getFailures (MergeResult result) {
        List<File> files = new LinkedList<File>();
        Map<String, ResolveMerger.MergeFailureReason> obstructions = result.getFailingPaths();
        if (obstructions != null) {
            for (Map.Entry<String, ResolveMerger.MergeFailureReason> failure : obstructions.entrySet()) {
                files.add(new File(workDir, failure.getKey()));
            }
        }
        return Collections.unmodifiableList(files);
    }
}
