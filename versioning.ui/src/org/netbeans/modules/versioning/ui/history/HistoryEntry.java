/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
