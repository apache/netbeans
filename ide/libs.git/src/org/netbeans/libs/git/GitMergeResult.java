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
