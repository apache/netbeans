/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.libs.git;

import java.io.File;
import org.eclipse.jgit.submodule.SubmoduleStatus;
import org.eclipse.jgit.submodule.SubmoduleStatusType;

/**
 * Describes current status of a repository's submodule
 *
 * @author Ondrej Vrabec
 * @since 1.16
 */
public final class GitSubmoduleStatus {

    private final SubmoduleStatus delegate;
    private final StatusType statusType;
    private final File folder;

    /**
     * Submodule's status
     */
    public enum StatusType {

        /**
         * Submodule's configuration is missing
         */
        MISSING,
        /**
         * Submodule's Git repository is not initialized
         */
        UNINITIALIZED,
        /**
         * Submodule's Git repository is initialized
         */
        INITIALIZED,
        /**
         * Submodule checked out commit is different than the commit referenced
         * in the index tree
         */
        REV_CHECKED_OUT;
    }

    GitSubmoduleStatus (SubmoduleStatus delegate, File folder) {
        this.delegate = delegate;
        this.folder = folder;
        this.statusType = parseStatus(delegate.getType());
    }

    /**
     * Returns status of the submodule
     *
     * @return submodule's status
     */
    public StatusType getStatus () {
        return statusType;
    }

    /**
     * Returns the submodule's root folder.
     *
     * @return submodule's root folder.
     */
    public File getSubmoduleFolder () {
        return folder;
    }

    /**
     * Returns the commit id of the currently checked-out commit.
     *
     * @return submodule's commit id.
     */
    public String getHeadId () {
        return delegate.getHeadId().getName();
    }

    /**
     * Returns the commit id of the submodule entry, in other words the
     * referenced commit.
     *
     * @return submodule's referenced commit id.
     */
    public String getReferencedCommitId () {
        return delegate.getIndexId().getName();
    }

    static StatusType parseStatus (SubmoduleStatusType status) {
        return StatusType.valueOf(status.name());
    }
}
