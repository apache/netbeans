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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.remote.client.cli;

import java.util.Date;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import org.netbeans.modules.subversion.remote.api.SVNConflictDescriptor;
import org.netbeans.modules.subversion.remote.api.SVNNodeKind;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.cli.commands.StatusCommand.Status;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public class CLIStatus implements ISVNStatus {
    private final Status status;
    private final ISVNInfo info;
	
    CLIStatus(Status status, ISVNInfo info) {
        this.status = status;
        this.info = info;
    }

    CLIStatus(Status status) {
        this.status = status;
        this.info = null;
    }
    
    @Override
    public SVNUrl getUrl() {
        return info == null ? null : info.getUrl();
    }

    @Override
    public String getUrlString() {
        return info == null ? null : info.getUrlString();
    }

    @Override
    public SVNRevision.Number getLastChangedRevision() {
        return info == null ? null : info.getLastChangedRevision();
    }

    @Override
    public Date getLastChangedDate() {
        return info == null ? null : info.getLastChangedDate();
    }

    @Override
    public String getLastCommitAuthor() {
        return info == null ? null : info.getLastCommitAuthor();
    }

    @Override
    public SVNStatusKind getTextStatus() {
        return status.getWcStatus();
    }

    @Override
    public SVNStatusKind getRepositoryTextStatus() {
        return status.getRepoStatus();
    }

    @Override
    public SVNStatusKind getPropStatus() {
        return status.getWcPropsStatus();
    }

    @Override
    public SVNStatusKind getRepositoryPropStatus() {
        return status.getRepoPropsStatus();
    }

    @Override
    public SVNRevision.Number getRevision() {
        return info == null ? null : info.getRevision(); 
    }

    @Override
    public String getPath() {
        return info != null ? info.getFile().getPath() : status.getPath().getPath();
    }

    @Override
    public VCSFileProxy getFile() {
        return info == null ? status.getPath() : info.getFile();
    }

    @Override
    public SVNNodeKind getNodeKind() {
        return info == null ? null : info.getNodeKind();
    }

    @Override
    public boolean isCopied() {
        return status.isWcCopied();
    }

    @Override
    public boolean isWcLocked() {
        return status.isWcLocked();
    }

    @Override
    public boolean isSwitched() {
        return status.isWcSwitched();
    }

    public SVNUrl getUrlCopiedFrom() {
        return info.getCopyUrl();
    }

    @Override
    public VCSFileProxy getConflictNew() {
        return null;
    }

    @Override
    public VCSFileProxy getConflictOld() {
        return null;
    }

    @Override
    public VCSFileProxy getConflictWorking() {
        return null;
    }

    @Override
    public String getLockOwner() {
        return status.getLockOwner();
    }

    @Override
    public Date getLockCreationDate() {
        return status.getLockCreated();
    }

    @Override
    public String getLockComment() {
        return status.getLockComment();
    }

    @Override
    public boolean hasTreeConflict() {
        return status.hasTreeConflicts();
    }

    @Override
    public SVNConflictDescriptor getConflictDescriptor() {
        return status.getConflictDescriptor();
    }

    @Override
    public boolean isFileExternal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMovedFromAbspath () {
        throw new UnsupportedOperationException(); 
    }

    @Override
    public String getMovedToAbspath () {
        throw new UnsupportedOperationException();
    }
}
