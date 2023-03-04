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

package org.netbeans.modules.subversion.client.parser;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Date;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNConflictDescriptor;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Ed Hillmann
 */
public class ParserSvnStatus implements ISVNStatus {

    private File file = null;
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
    private File conflictNew = null;
    private File conflictOld = null;
    private File conflictWorking = null;
    private Date lockCreationDate = null;
    private String lockComment = null;
    private String lockOwner = null;
    private boolean treeConflict;
    private final SVNConflictDescriptor conflictDescriptor;

    /** Creates a new instance of LocalSvnStatusImpl */
    public ParserSvnStatus(File file, String url, long revision, String kind,
            String textStatus, String propStatus,
            String lastCommitAuthor, long lastChangedRevision, Date lastChangedDate,
            boolean isCopied, String urlCopiedFrom,
            File conflictNew, File conflictOld, File conflictWorking,
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

        this.textStatus = SVNStatusKind.fromString(textStatus);
        this.propStatus = SVNStatusKind.fromString(propStatus);
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

    public boolean isCopied() {
        return isCopied;
    }

    public SVNUrl getUrlCopiedFrom() {
        return urlCopiedFrom;
    }

    public SVNUrl getUrl() {
        return url;
    }

    public SVNStatusKind getTextStatus() {
        return textStatus;
    }

    public SVNRevision.Number getRevision() {
        return revision;
    }

    public SVNStatusKind getRepositoryTextStatus() {
        return null; 
    }

    public SVNStatusKind getRepositoryPropStatus() {
        return null; 
    }

    public File getConflictNew() {
        return conflictNew;
    }

    public File getConflictOld() {
        return conflictOld;
    }

    public File getConflictWorking() {
        return conflictWorking;
    }

    public File getFile() {
        return file;
    }

    public Date getLastChangedDate() {
        return lastChangedDate;
    }

    public SVNRevision.Number getLastChangedRevision() {
        return lastChangedRevision;
    }

    public String getLastCommitAuthor() {
        return lastCommitAuthor;
    }

    public String getLockComment() {
        return lockComment;
    }

    public Date getLockCreationDate() {
        return lockCreationDate;
    }

    public String getLockOwner() {
        return lockOwner;
    }

    public SVNNodeKind getNodeKind() {
        return kind;
    }

    public String getPath() {
        return file.getPath();
    }

    public SVNStatusKind getPropStatus() {
        return propStatus;
    }

    public String getUrlString() {
        return url.toString();
    }

    public boolean isWcLocked() {
        // TODO implement me
        throw new UnsupportedOperationException("not implemented yet");             // NOI18N
    }

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

    public boolean isFileExternal() {
        throw new UnsupportedOperationException("Not supported yet.");
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

