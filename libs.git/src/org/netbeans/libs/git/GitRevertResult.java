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
