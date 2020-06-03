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

package org.netbeans.modules.subversion.remote.ui.diff;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.subversion.remote.VersionsCache;
import org.netbeans.modules.subversion.remote.client.PropertiesClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Stream source for diffing CVS managed files.
 *
 * 
 */
public class DiffStreamSource extends StreamSource {

    private final VCSFileProxy      baseFile;
    private final String    propertyName;
    private final String    revision;
    private final String    title;
    private String          mimeType;
    private boolean         initialized;

    /**
     * Null is a valid value if base file does not exist in this revision. 
     */ 
    private VCSFileProxy            remoteFile;
    private MultiDiffPanel.Property propertyValue;
    private Boolean canWriteBaseFile;

    /**
     * Creates a new StreamSource implementation for Diff engine.
     * 
     * @param baseFile
     * @param revision file revision, may be null if the revision does not exist (ie for new files)
     * @param title title to use in diff panel
     */ 
    public DiffStreamSource(VCSFileProxy baseFile, String propertyName, String revision, String title) {
        this.baseFile = VCSFileProxySupport.isMac(baseFile) ? baseFile.normalizeFile() : baseFile;
        this.propertyName = propertyName;
        this.revision = revision;
        this.title = title;
    }

    @Override
    public String getName() {
        if (baseFile != null) {
            return baseFile.getName();
        } else {
            return NbBundle.getMessage(DiffStreamSource.class, "LBL_Diff_Anonymous"); // NOI18N
        }
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
            return null; // XXX use error manager HACK null  potentionally kills DiffViewImpl, NPE while constructing EditorKit
        }
        return mimeType;
    }

    @Override
    public synchronized Reader createReader() throws IOException {
        init();
        if (propertyName != null) {
            if (propertyValue != null) {
                return propertyValue.toReader();
            } else {
                return null;
            }
        } else {
            if (revision == null || remoteFile == null) {
                return null;
            }
            if (!mimeType.startsWith("text/")) { //NOI18N
                return null;
            } else {
                return org.netbeans.modules.versioning.util.Utils.createReader(remoteFile.toFileObject());
            }
        }
    }

    @Override
    public Writer createWriter(Difference[] conflicts) throws IOException {
        throw new IOException("Operation not supported"); // NOI18N
    }

    @Override
    public boolean isEditable() {
        return propertyName == null && Setup.REVISION_CURRENT.equals(revision) && isPrimary() && isBaseFileWritable();
    }

    private boolean isBaseFileWritable () {
        if (canWriteBaseFile == null) {
            FileObject fo = baseFile.toFileObject();
            canWriteBaseFile = fo != null && fo.canWrite();
        }
        return canWriteBaseFile;
    }

    private boolean isPrimary() {
        FileObject fo = baseFile.toFileObject();
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
        if (propertyName != null || remoteFile == null || !isPrimary()) {
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
    synchronized void init() throws IOException {
        if (initialized) {
            return;
        }
        initialized = true;
        if (propertyValue != null || remoteFile != null || revision == null) {
            return;
        }
        if (propertyName != null) {
            initProperty();
            return;
        }
        if (baseFile.isDirectory()) {
            mimeType = "content/unknown"; // NOI18N
            return;
        }
        mimeType = SvnUtils.getMimeType(baseFile);
        try {
            if (isEditable()) {
                // we cannot move editable documents because that would break Document sharing
                remoteFile = VersionsCache.getInstance(VCSFileProxySupport.getFileSystem(baseFile)).getFileRevision(baseFile, revision);
            } else {
                VCSFileProxy tempFolder = VCSFileProxySupport.getTempFolder(baseFile, true);
                // To correctly get content of the base file, we need to checkout all files that belong to the same
                // DataObject. One example is Form files: data loader removes //GEN:BEGIN comments from the java file but ONLY
                // if it also finds associate .form file in the same directory
                Set<VCSFileProxy> allFiles = VCSFileProxySupport.getAllDataObjectFiles(baseFile);
                for (VCSFileProxy file : allFiles) {
                    boolean isBase = file.equals(baseFile);
                    try {
                        VCSFileProxy rf = VersionsCache.getInstance(VCSFileProxySupport.getFileSystem(file)).getFileRevision(file, revision);
                        if(rf == null) {
                            remoteFile = null;
                            return;
                        }
                        VCSFileProxy newRemoteFile = VCSFileProxy.createFileProxy(tempFolder, file.getName());
                        VCSFileProxySupport.deleteOnExit(newRemoteFile);
                        org.netbeans.modules.versioning.util.Utils.copyStreamsCloseAll(VCSFileProxySupport.getOutputStream(newRemoteFile), rf.getInputStream(false));
                        if (isBase) {
                            remoteFile = newRemoteFile;
                            VCSFileProxySupport.associateEncoding(file, newRemoteFile);                            
                        }
                    } catch (Exception e) {
                        if (SvnClientExceptionHandler.isTargetDirectory(e.getMessage())
                                || e.getCause() != null && SvnClientExceptionHandler.isTargetDirectory(e.getCause().getMessage())) {
                            mimeType = "content/unknown"; // NOI18N
                            return;
                        } else if (isBase) throw e;
                        // we cannot check out peer file so the dataobject will not be constructed properly
                    }
                }
            }
            if (!baseFile.exists() && remoteFile != null && remoteFile.exists()) {
                mimeType = SvnUtils.getMimeType(remoteFile);
            }
        } catch (Exception e) {
            throw new IOException("Can not load remote file for " + baseFile, e); //NOI18N
        }
        FileObject fo = baseFile.toFileObject();
        canWriteBaseFile = fo != null && fo.canWrite();
    }

    private void initProperty() throws IOException {
        PropertiesClient client = new PropertiesClient(baseFile);
        if (Setup.REVISION_BASE.equals(revision)) {
            byte [] value = client.getBaseProperties(true).get(propertyName);
            propertyValue = value != null ? new MultiDiffPanel.Property(value) : null;
        } else if (Setup.REVISION_CURRENT.equals(revision)) {
            byte [] value = client.getProperties().get(propertyName);
            propertyValue = value != null ? new MultiDiffPanel.Property(value) : null;
        }
        mimeType = propertyValue != null ? propertyValue.getMIME() : "content/unknown"; // NOI18N
    }
}
