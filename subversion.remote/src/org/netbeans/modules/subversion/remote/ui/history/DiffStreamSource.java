/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
