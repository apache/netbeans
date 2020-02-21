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

package org.netbeans.modules.subversion.remote.ui.history;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.VersionsCache;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Stream source for diffing remote SVN managed files .
 *
 * 
 */
public class DiffStreamSource extends StreamSource implements Cancellable {

    private final VCSFileProxy      baseFile;
    private final String    revision, pegRevision;
    private final String    title;
    private String          mimeType;
    private final SVNUrl    url;
    private final SVNUrl    repoUrl;
    private SvnClient       client;
    private final FileSystem fileSystem;

    /**
     * Null is a valid value if base file does not exist in this revision.
     */
    private VCSFileProxy            remoteFile;
    private boolean isDirectory;
    private final String baseFileName;

    /**
     * Creates a new StreamSource implementation for Diff engine.
     *
     * @param baseFile
     * @param revision file revision, may be null if the revision does not exist (ie for new files)
     * @param title title to use in diff panel
     */
    public DiffStreamSource(FileSystem fs, VCSFileProxy baseFile, SVNUrl repoUrl, SVNUrl fileUrl, String revision, String title) {
        this.fileSystem = fs;
        this.baseFile = baseFile;
        this.baseFileName = baseFile.getName();
        this.revision = this.pegRevision = revision;
        this.title = title;
        this.url = fileUrl;
        this.repoUrl = repoUrl;
    }

    /**
     * Creates a new StreamSource implementation for Diff engine.
     *
     * @param baseFile
     * @param revision file revision, may be null if the revision does not exist (ie for new files)
     * @param pegRevision file peg revision
     * @param title title to use in diff panel
     */
    public DiffStreamSource(FileSystem fs, VCSFileProxy baseFile, String baseFileName, SVNUrl repoUrl, SVNUrl fileUrl, String revision, String pegRevision, String title) {
        this.fileSystem = fs;
        this.baseFile = baseFile;
        this.baseFileName = baseFileName;
        this.revision = revision;
        this.pegRevision = pegRevision;
        this.title = title;
        this.url = fileUrl;
        this.repoUrl = repoUrl;
    }

    @Override
    public String getName() {
        return baseFileName;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public synchronized String getMIMEType() {
        try {
            init();
        } catch (IOException e) {
            Subversion.LOG.log(Level.INFO, "DiffStreamSource.getMIMEType() returns null", e);
            return null; // HACK null cases the file handled as binary
        }
        return mimeType;
    }

    @Override
    public synchronized Reader createReader() throws IOException {
        init();
        if (revision == null || remoteFile == null) return null;
        if (!mimeType.startsWith("text/")) { //NOI18N
            return null;
        } else {
            return org.netbeans.modules.versioning.util.Utils.createReader(remoteFile.toFileObject());
        }
    }

    @Override
    public Writer createWriter(Difference[] conflicts) throws IOException {
        throw new IOException("Operation not supported"); // NOI18N
    }

    @Override
    public final boolean isEditable() {
        return false;
    }

    private boolean isPrimary() {
        FileObject fo = baseFile == null ? null : baseFile.toFileObject();
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                return fo.equals(dao.getPrimaryFile());
            } catch (DataObjectNotFoundException e) {
                // no dataobject, never mind
            }
        }
        return true;
    }

    @Override
    public synchronized Lookup getLookup() {
        try {
            init();
        } catch (IOException e) {
            return Lookups.fixed();
        }
        if (remoteFile == null || !isPrimary()) {
            return Lookups.fixed();
        }
        FileObject remoteFo = remoteFile.toFileObject();
        if (remoteFo == null) {
            return Lookups.fixed();
        }

        return Lookups.fixed(remoteFo);
    }

    /**
     * Loads data over network.
     */
    public synchronized void init() throws IOException {
        if (remoteFile != null || revision == null) {
            return;
        }
        if (isDirectory || baseFile != null && baseFile.isDirectory()) {
            mimeType = "content/unknown"; //NOI18N
            return;
        } else if (baseFile == null) {
            mimeType = "content/unknown"; //NOI18N
        } else {            
            mimeType = SvnUtils.getMimeType(baseFile);
        }
        try {
            File rf = VersionsCache.getInstance(fileSystem).getFileRevision(repoUrl, url, revision, pegRevision, baseFileName);
            remoteFile = VCSFileProxy.createFileProxy(rf);
            if (baseFile != null) {
                VCSFileProxySupport.associateEncoding(baseFile, remoteFile);
            }
        } catch (IOException e) {
            if ((e.getCause() != null && SvnClientExceptionHandler.isTargetDirectory(e.getCause().getMessage()) || SvnClientExceptionHandler.isTargetDirectory(e.getMessage()))) {
                // target is a directory, but locally deleted
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.log(Level.FINE, "", e);
                }
                mimeType = "content/unknown"; // NOI18N
                isDirectory = true;
                return;
            }
            throw e;
        }
        if ((baseFile == null || !baseFile.exists()) && remoteFile != null && remoteFile.exists()) {
            mimeType = SvnUtils.getMimeType(remoteFile);
        }
    }

    @Override
    public boolean cancel() {
        if(client != null) {
            client.cancel();
        }
        return true;
    }

}
