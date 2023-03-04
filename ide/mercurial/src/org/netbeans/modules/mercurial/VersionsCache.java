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

package org.netbeans.modules.mercurial;

import java.io.*;
import org.netbeans.modules.versioning.historystore.Storage;
import org.netbeans.modules.versioning.historystore.StorageManager;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.util.*;
import org.netbeans.modules.versioning.util.Utils;

/**
 * File revisions cache. It can access pristine files.
 *
 * XXX and what exactly is cached here?!
 * 
 * @author Petr Kuzel
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
    public File getFileRevision(File base, HgRevision revision) throws IOException {
        return getFileRevision(base, revision, true);
    }
    
    public File getFileRevision(File base, HgRevision revision, boolean tryHard) throws IOException {
        String revisionNumber = revision.getRevisionNumber();
        if("-1".equals(revisionNumber)) return null; // NOI18N
        
        File repository = Mercurial.getInstance().getRepositoryRoot(base);
        if (HgRevision.CURRENT.equals(revision)) {
            return base;
        } else {
            try {
                File tempFile = new File(Utils.getTempFolder(), "nb-hg-" + base.getName()); //NOI18N
                tempFile.deleteOnExit();
                if (HgRevision.BASE.equals(revision)) {
                    HgCommand.doCat(repository, base, tempFile, null);
                } else {
                    if ("false".equals(System.getProperty("versioning.mercurial.historycache.enable", "true"))) { //NOI18N
                        HgCommand.doCat(repository, base, tempFile, revisionNumber, null);
                    } else {
                        String changesetId = revision.getChangesetId();
                        Storage cachedVersions = StorageManager.getInstance().getStorage(repository.getAbsolutePath());
                        String relativePath = HgUtils.getRelativePath(base);
                        File cachedFile = cachedVersions.getContent(relativePath, base.getName(), changesetId);
                        if (cachedFile.length() == 0) { // not yet cached
                            HgCommand.doCat(repository, base, tempFile, revisionNumber, null, tryHard);
                            if (tempFile.length() != 0) {
                                cachedVersions.setContent(relativePath, changesetId, tempFile);
                            }
                        } else {
                            tempFile = cachedFile;
                        }
                    }
                }
                if (tempFile.length() == 0) {
                    tempFile.delete();
                    return null;
                }
                return tempFile;
            } catch (HgException e) {
                throw new IOException(e);
            }
        }
    }

    private static boolean isLong (String revision) {
        boolean isLong = false;
        try {
            Long.parseLong(revision);
            isLong = true;
        } catch (NumberFormatException ex) { }
        return isLong;
    }
}
