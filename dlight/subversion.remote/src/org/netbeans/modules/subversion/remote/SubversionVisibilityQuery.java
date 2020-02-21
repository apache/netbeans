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
package org.netbeans.modules.subversion.remote;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.VCSVisibilityQuery;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;

/**
 * Hides folders that have 'Localy removed' status.
 * 
 * 
 */
public class SubversionVisibilityQuery extends VCSVisibilityQuery implements VersioningListener {

    private FileStatusCache       cache;
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.subversion.remote.SubversionVisibilityQuery"); //NOI18N

    public SubversionVisibilityQuery() {
    }

    @Override
    public boolean isVisible(VCSFileProxy file) {
        long t = System.currentTimeMillis();
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "isVisible {0}", new Object[] { file });
        }
        boolean ret = true;
        try {
    
        if(file == null) return true;
        if (file.isFile()) {
            return true;
        }
        if(!(VersioningSupport.getOwner(file) instanceof SubversionVCS)) {
            return true;
        }
        try {
            // get cached status so you won't block or trigger synchronous shareability calls
            FileInformation info = getCache().getCachedStatus(file);
            return info == null || info.getStatus() != FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY || !containsMetadata(file);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            return true;
        }        
        } finally {
            if(Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, "isVisible returns {0} in {1} millis", new Object[] { ret, System.currentTimeMillis() - t });
    }
        }
    }

    private synchronized FileStatusCache getCache() {
        if (cache == null) {
            cache = Subversion.getInstance().getStatusCache();
            cache.addVersioningListener(this);
        }
        return cache;
    }
    
    @Override
    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            VCSFileProxy file = (VCSFileProxy) event.getParams()[0];
            if (file != null && file.isDirectory() && containsMetadata(file)) {
                FileInformation old = (FileInformation) event.getParams()[1];
                FileInformation cur = (FileInformation) event.getParams()[2];
                if (old != null && old.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY || cur.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                    fireVisibilityChanged(file);
                }
            }
        }
    }

    static boolean isHiddenFolder(FileInformation info, VCSFileProxy file) {
        return file.isDirectory() && info != null && info.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY;
    }

    /**
     * It makes sense to implement versioning query only for folders with metadata inside.
     * Otherwise they are either new or come from 1.7+ working copies where svn folders
     * are removed from disk immediately.
     */
    private boolean containsMetadata (VCSFileProxy folder) {
        final VCSFileProxy[] listFiles = folder.listFiles();
        if (listFiles != null) {
            for(VCSFileProxy child : listFiles) {
                if (SvnUtils.isAdministrative(child.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}          
