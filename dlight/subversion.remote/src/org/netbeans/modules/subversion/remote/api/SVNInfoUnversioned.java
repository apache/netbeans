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
public class SVNInfoUnversioned implements ISVNInfo {
    private final VCSFileProxy file;

    public SVNInfoUnversioned(VCSFileProxy file) {
        this.file =file;
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
    public SVNRevision.Number getCopyRev() {
        return null;
    }

    @Override
    public SVNUrl getCopyUrl() {
        return null;
    }

    @Override
    public int getDepth() {
        return -2;
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
    public Date getLastDatePropsUpdate() {
        return null;
    }

    @Override
    public Date getLastDateTextUpdate() {
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
    public SVNNodeKind getNodeKind() {
        return null;
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public SVNUrl getRepository() {
        return null;
    }

    @Override
    public SVNRevision.Number getRevision() {
        return null;
    }

    @Override
    public SVNScheduleKind getSchedule() {
        return null;
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
    public String getUuid() {
        return null;
    }

    @Override
    public boolean isCopied() {
        return false;
    }
    
}
