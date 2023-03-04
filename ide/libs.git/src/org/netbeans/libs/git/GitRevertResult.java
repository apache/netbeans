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
import java.util.List;

/**
 * Provides information about the result of reverting a commit.
 * 
 * @author Ondra Vrabec
 */
public final class GitRevertResult {
    
    private Status status;
    private GitRevisionInfo revertCommit;

    private final List<File> conflicts;
    private final List<File> failures;

    /**
     * Result status of a commit revert.
     */
    public static enum Status {
        REVERTED {
            @Override
            public String toString() {
                return "Reverted";
            }
        },
        REVERTED_IN_INDEX {
            @Override
            public String toString() {
                return "Reverted In Index";
            }
        },
        CONFLICTING {
            @Override
            public String toString() {
                return "Conflicting";
            }
        },
        FAILED {
            @Override
            public String toString() {
                return "Failed";
            }
        },
        NO_CHANGE {
            @Override
            public String toString() {
                return "No Change";
            }
        }
    }
    
    GitRevertResult (Status status, GitRevisionInfo commit, List<File> conflicts, List<File> failures) {
        this.status = status;
        this.revertCommit = commit;
        this.conflicts = conflicts == null ? Collections.<File>emptyList() : conflicts;
        this.failures = failures == null ? Collections.<File>emptyList() : failures;
    }

    /**
     * @return result of the revert.
     */
    public Status getStatus () {
        return status;
    }

    /**
     * @return current HEAD commit after the successful revert or <code>null</code> if it failed.
     */
    public GitRevisionInfo getNewHead () {
        return revertCommit;
    }

    /**
     * If the revert started but was unable to finish because of unresolved conflicts then the method
     * returns a collection of such files in conflict.
     * To complete the commit revert you need to resolve the conflicts and commit the changes.
     * @return files in conflict
     */
    public Collection<File> getConflicts () {
        return conflicts;
    }

    /**
     * When the commit revert fails because of local modifications then this 
     * method returns a collections of files causing the failure.
     * @return files that cause the revert to fail.
     */
    public Collection<File> getFailures () {
        return failures;
    }
}
