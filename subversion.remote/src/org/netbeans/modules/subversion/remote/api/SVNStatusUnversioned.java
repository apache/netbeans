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
package org.netbeans.modules.subversion.remote.api;

import java.util.Date;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public class SVNStatusUnversioned implements ISVNStatus {
    private final VCSFileProxy file;
    private final boolean isIgnored;
    
    public SVNStatusUnversioned(VCSFileProxy file) {
        this.file= file;
        this.isIgnored = false;
    }

    public SVNStatusUnversioned(VCSFileProxy file, boolean isIgnored) {
        this.file= file;
        this.isIgnored = isIgnored;
    }

    @Override
    public SVNConflictDescriptor getConflictDescriptor() {
        return null;
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
    public VCSFileProxy getFile() {
        return file;
    }

    @Override
    public Date getLastChangedDate() {
        return null;
    }

    @Override
    public SVNRevision.Number getLastChangedRevision() {
        return null;
    }

    @Override
    public String getLastCommitAuthor() {
        return null;
    }

    @Override
    public String getLockComment() {
        return null;
    }

    @Override
    public Date getLockCreationDate() {
        return null;
    }

    @Override
    public String getLockOwner() {
        return null;
    }

    @Override
    public String getMovedFromAbspath() {
        return null;
    }

    @Override
    public String getMovedToAbspath() {
        return null;
    }

    @Override
    public SVNNodeKind getNodeKind() {
        return SVNNodeKind.UNKNOWN;
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public SVNStatusKind getPropStatus() {
        return SVNStatusKind.NONE;
    }

    @Override
    public SVNStatusKind getRepositoryPropStatus() {
        return SVNStatusKind.UNVERSIONED;
    }

    @Override
    public SVNStatusKind getRepositoryTextStatus() {
        return SVNStatusKind.UNVERSIONED;
    }

    @Override
    public SVNRevision.Number getRevision() {
        return null;
    }

    @Override
    public SVNStatusKind getTextStatus() {
        if (isIgnored) {
            return SVNStatusKind.IGNORED;
        } else {
            return SVNStatusKind.UNVERSIONED;
        }
    }

    @Override
    public SVNUrl getUrl() {
        return null;
    }

    @Override
    public String getUrlString() {
        return null;
    }

    @Override
    public boolean hasTreeConflict() {
        return false;
    }

    @Override
    public boolean isCopied() {
        return false;
    }

    @Override
    public boolean isFileExternal() {
        return false;
    }

    @Override
    public boolean isSwitched() {
        return false;
    }

    @Override
    public boolean isWcLocked() {
        return false;
    }
}
