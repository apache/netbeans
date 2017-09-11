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

import java.io.IOException;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.submodule.SubmoduleStatusType;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class GitEnumsStateTest extends AbstractGitTestCase {

    public GitEnumsStateTest (String testName) throws IOException {
        super(testName);
    }

    public void testUpdateResult () {
        for (RefUpdate.Result result : RefUpdate.Result.values()) {
            assertNotNull(GitRefUpdateResult.valueOf(result.name()));
        }
    }
    
    public void testMergeStatus () {
        for (MergeResult.MergeStatus status : MergeResult.MergeStatus.values()) {
            assertNotNull(GitMergeResult.parseMergeStatus(status));
        }
    }

    public void testRemoteUpdateStatus () {
        for (RemoteRefUpdate.Status status : RemoteRefUpdate.Status.values()) {
            assertNotNull(GitRefUpdateResult.valueOf(status.name()));
        }
    }

    public void testRepositoryState () {
        for (RepositoryState state : RepositoryState.values()) {
            assertNotNull(GitRepositoryState.getStateFor(state));
        }
    }
    
    public void testRebaseStatus () {
        for (RebaseResult.Status status : RebaseResult.Status.values()) {
            assertNotNull(GitRebaseResult.parseRebaseStatus(status));
        }
    }
    
    public void testSubmoduleStatus () {
        for (SubmoduleStatusType status : SubmoduleStatusType.values()) {
            assertNotNull(GitSubmoduleStatus.parseStatus(status));
        }
    }
    
    public void testCherryPickStatus () {
        for (CherryPickResult.CherryPickStatus status : CherryPickResult.CherryPickStatus.values()) {
            assertNotNull(GitCherryPickResult.CherryPickStatus.valueOf(status.name()));
        }
    }
}
