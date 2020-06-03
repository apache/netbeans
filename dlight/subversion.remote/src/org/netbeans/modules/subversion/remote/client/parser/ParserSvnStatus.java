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

package org.netbeans.modules.subversion.remote.client.parser;

import java.net.MalformedURLException;
import java.util.Date;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import org.netbeans.modules.subversion.remote.api.SVNConflictDescriptor;
import org.netbeans.modules.subversion.remote.api.SVNNodeKind;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public class ParserSvnStatus implements ISVNStatus {

    private VCSFileProxy file = null;
    private SVNUrl url = null;
    private SVNRevision.Number revision = null;
    private SVNNodeKind kind = null;
    private SVNStatusKind textStatus = null;
    private SVNStatusKind propStatus = null;
    private String lastCommitAuthor = null;
    private SVNRevision.Number lastChangedRevision = null;
    private Date lastChangedDate = null;
    private boolean isCopied = false;
    private SVNUrl urlCopiedFrom = null;
    private VCSFileProxy conflictNew = null;
    private VCSFileProxy conflictOld = null;
    private VCSFileProxy conflictWorking = null;
    private Date lockCreationDate = null;
    private String lockComment = null;
    private String lockOwner = null;
    private final boolean treeConflict;
    private final SVNConflictDescriptor conflictDescriptor;

    /** Creates a new instance of LocalSvnStatusImpl */
    public ParserSvnStatus(VCSFileProxy file, String url, long revision, String kind,
            SVNStatusKind textStatus, SVNStatusKind propStatus,
            String lastCommitAuthor, long lastChangedRevision, Date lastChangedDate,
            boolean isCopied, String urlCopiedFrom,
            VCSFileProxy conflictNew, VCSFileProxy conflictOld, VCSFileProxy conflictWorking,
            Date lockCreationDate, String lockComment, String lockOwner,
            boolean treeConflict, SVNConflictDescriptor conflictDescriptor) {

        this.file = file;

        if (url != null) {
            try {
                this.url = new SVNUrl(url);
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        this.revision = new SVNRevision.Number(revision);
        this.kind = SVNNodeKind.fromString(kind);

        this.textStatus = textStatus;
        this.propStatus = propStatus;
        this.lastCommitAuthor = lastCommitAuthor;

        this.lastChangedRevision = new SVNRevision.Number(lastChangedRevision);
        this.lastChangedDate = lastChangedDate;

        this.isCopied = isCopied;
        if (urlCopiedFrom != null) {
            try {
                this.urlCopiedFrom = new SVNUrl(urlCopiedFrom);
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        this.conflictNew = conflictNew;
        this.conflictOld = conflictOld;
        this.conflictWorking = conflictWorking;
        this.lockCreationDate = lockCreationDate;
        this.lockComment  = lockComment;
        this.lockOwner = lockOwner;
        this.treeConflict = treeConflict;
        this.conflictDescriptor = conflictDescriptor;
    }

    @Override
    public boolean isCopied() {
        return isCopied;
    }

    public SVNUrl getUrlCopiedFrom() {
        return urlCopiedFrom;
    }

    @Override
    public SVNUrl getUrl() {
        return url;
    }

    @Override
    public SVNStatusKind getTextStatus() {
        return textStatus;
    }

    @Override
    public SVNRevision.Number getRevision() {
        return revision;
    }

    @Override
    public SVNStatusKind getRepositoryTextStatus() {
        return null; 
    }

    @Override
    public SVNStatusKind getRepositoryPropStatus() {
        return null; 
    }

    @Override
    public VCSFileProxy getConflictNew() {
        return conflictNew;
    }

    @Override
    public VCSFileProxy getConflictOld() {
        return conflictOld;
    }

    @Override
    public VCSFileProxy getConflictWorking() {
        return conflictWorking;
    }

    @Override
    public VCSFileProxy getFile() {
        return file;
    }

    @Override
    public Date getLastChangedDate() {
        return lastChangedDate;
    }

    @Override
    public SVNRevision.Number getLastChangedRevision() {
        return lastChangedRevision;
    }

    @Override
    public String getLastCommitAuthor() {
        return lastCommitAuthor;
    }

    @Override
    public String getLockComment() {
        return lockComment;
    }

    @Override
    public Date getLockCreationDate() {
        return lockCreationDate;
    }

    @Override
    public String getLockOwner() {
        return lockOwner;
    }

    @Override
    public SVNNodeKind getNodeKind() {
        return kind;
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public SVNStatusKind getPropStatus() {
        return propStatus;
    }

    @Override
    public String getUrlString() {
        return url.toString();
    }

    @Override
    public boolean isWcLocked() {
        // TODO implement me
        throw new UnsupportedOperationException("not implemented yet");             // NOI18N
    }

    @Override
    public boolean isSwitched() {
        // TODO implement me
        throw new UnsupportedOperationException("not implemented yet");             // NOI18N
    }

    @Override
    public boolean hasTreeConflict() {
        return treeConflict;
    }

    @Override
    public SVNConflictDescriptor getConflictDescriptor() {
        return conflictDescriptor;
    }

    @Override
    public boolean isFileExternal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMovedFromAbspath () {
        return null;
    }

    @Override
    public String getMovedToAbspath () {
        return null;
    }

}

