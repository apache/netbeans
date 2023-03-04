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
package org.netbeans.modules.versioning.ui.history;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.core.util.Utils;

/**
 *
 * @author tomas
 */
class HistoryEntry {
    private final VCSHistoryProvider.HistoryEntry entry;
    private final boolean local;
    private Map<VCSFileProxy, HistoryEntry> parents;

    HistoryEntry(VCSHistoryProvider.HistoryEntry entry, boolean local) {
        this.entry = entry;
        this.local = local;
    }

    public Object[] getLookupObjects() {
        Object[] delegates = Utils.getDelegateEntry(entry);
        if(delegates == null) {
            return new Object[] { entry };
        } else {
            Object[] ret = new Object[delegates.length + 1];
            System.arraycopy(delegates, 0, ret, 0, delegates.length);
            ret[delegates.length] = entry;
            return ret;
        }
    }
    
    public String getUsernameShort() {
        return entry.getUsernameShort();
    }

    public String getUsername() {
        return entry.getUsername();
    }

    public String getRevisionShort() {
        return entry.getRevisionShort();
    }

    public void getRevisionFile(VCSFileProxy originalFile, VCSFileProxy revisionFile) {
        entry.getRevisionFile(originalFile, revisionFile);
        org.netbeans.modules.versioning.util.Utils.associateEncoding(originalFile.toFileObject(), revisionFile.toFileObject());
    }

    public String getRevision() {
        return entry.getRevision();
    }

    public String getMessage() {
        return entry.getMessage();
    }

    public VCSFileProxy[] getFiles() {
        return entry.getFiles();
    }

    public Date getDateTime() {
        return entry.getDateTime();
    }

    public Action[] getActions() {
        return entry.getActions();
    }

    public void setMessage(String message) throws IOException {
        entry.setMessage(message);
    }

    public boolean canEdit() {
        return entry.canEdit();
    }
    
    public synchronized HistoryEntry getParent(VCSFileProxy file) {
        HistoryEntry parent = null;
        if(parents == null) {
            parents = new HashMap<VCSFileProxy, HistoryEntry>();
        } else {
            parent = parents.get(file);
        }
        if(parent == null) {
            VCSHistoryProvider.HistoryEntry vcsParent = entry.getParentEntry(file);
            parent = vcsParent != null ? new HistoryEntry(vcsParent, local) : null;
            parents.put(file, parent);
        }
        return parent;
    }
    
    public boolean isLocalHistory() {
        return local;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("files=[");
        VCSFileProxy[] files = getFiles();
        for (int i = 0; i < files.length; i++) {
            sb.append(files[i]);
            if(i < files.length -1) sb.append(",");
        }
        sb.append("],");
        sb.append("timestamp=");
        sb.append(getDateTime().getTime());
        sb.append(",");
        sb.append("revision=");
        sb.append(getRevision());
        sb.append(",");
        sb.append("username=");
        sb.append(getUsername());
        sb.append(",");
        sb.append("message=");
        sb.append(getMessage());
        sb.append(",");
        sb.append("canEdit=");
        sb.append(canEdit());
        sb.append("]");
        return sb.toString();
    }

}
