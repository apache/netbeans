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

package org.netbeans.modules.mercurial.remote;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.historystore.Storage;
import org.netbeans.modules.versioning.historystore.StorageManager;

/**
 * File revisions cache. It can access pristine files.
 *
 * XXX and what exactly is cached here?!
 * 
 * 
 */
public class VersionsCache {

    private static VersionsCache instance;

    /** Creates a new instance of VersionsCache */
    private VersionsCache() {
    }

    public static synchronized VersionsCache getInstance() {
        if (instance == null) {
            instance = new VersionsCache();
        }
        return instance;
    }

    /**
     * Loads the file in specified revision.
     *
     * @return null if the file does not exist in given revision
     */
    public VCSFileProxy getFileRevision(VCSFileProxy base, HgRevision revision) throws IOException {
        return getFileRevision(base, revision, true);
    }
    
    public VCSFileProxy getFileRevision(VCSFileProxy base, HgRevision revision, boolean tryHard) throws IOException {
        String revisionNumber = revision.getRevisionNumber();
        if("-1".equals(revisionNumber)) { //NOI18N
            return null; // NOI18N
        }
        
        VCSFileProxy repository = Mercurial.getInstance().getRepositoryRoot(base);
        if (HgRevision.CURRENT.equals(revision)) {
            return base;
        } else {
            try {
                VCSFileProxy tempFile = VCSFileProxy.createFileProxy(VCSFileProxySupport.getTempFolder(repository, true), "nb-hg-" + base.getName()); //NOI18N
                if (HgRevision.BASE.equals(revision)) {
                    HgCommand.doCat(repository, base, tempFile, null);
                } else {
                    if ("false".equals(System.getProperty("versioning.mercurial.historycache.enable", "true"))) { //NOI18N
                        HgCommand.doCat(repository, base, tempFile, revisionNumber, null);
                    } else {
                        String changesetId = revision.getChangesetId();
                        Storage cachedVersions = StorageManager.getInstance().getStorage(repository.getPath());
                        String relativePath = HgUtils.getRelativePath(base);
                        File cachedFile = cachedVersions.getContent(relativePath, base.getName(), changesetId);
                        if (cachedFile.length() == 0) { // not yet cached
                            HgCommand.doCat(repository, base, tempFile, revisionNumber, null, tryHard);
                            if (VCSFileProxySupport.length(tempFile) != 0) {
                                cachedVersions.setContent(relativePath, changesetId, tempFile.getInputStream(false));
                            }
                        } else {
                            VCSFileProxySupport.copyStreamToFile(new BufferedInputStream(new FileInputStream(cachedFile)), tempFile);
                        }
                    }
                }
                if (VCSFileProxySupport.length(tempFile) == 0) {
                    VCSFileProxySupport.delete(tempFile);
                    return null;
                }
                return tempFile;
            } catch (HgException e) {
                throw new IOException(e);
            }
        }
    }
}
