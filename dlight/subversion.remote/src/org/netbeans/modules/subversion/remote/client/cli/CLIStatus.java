/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
