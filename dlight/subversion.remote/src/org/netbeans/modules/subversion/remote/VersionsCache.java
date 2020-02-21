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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnClientFactory;
import org.netbeans.modules.subversion.remote.ui.diff.Setup;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.historystore.Storage;
import org.netbeans.modules.versioning.historystore.StorageManager;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * File revisions cache. It can access pristine files.
 *
 * 
 */
public class VersionsCache {

    private static final Map<FileSystem,VersionsCache> instances = new HashMap<>();
    private final FileSystem fileSystem;

    /** Creates a new instance of VersionsCache */
    private VersionsCache(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public static synchronized VersionsCache getInstance(FileSystem fileSystem) {
        VersionsCache res = instances.get(fileSystem);
        if (res == null) {
            res = new VersionsCache(fileSystem);
            instances.put(fileSystem, res);
        }
        return res;
    }

    /**
     * Loads a content of the given file in the given revision. May communicate over network.
     * @param repoUrl target repository URL
     * @param url target file full url
     * @param revision target revision, will be used also as the peg revision
     * @param fileName basename of the file which will be used as a temporary file's name
     * @return a temporary file with given file's content
     * @throws java.io.IOException
     */
    public File getFileRevision(SVNUrl repoUrl, SVNUrl url, String revision, String fileName) throws IOException {
        return getFileRevision(repoUrl, url, revision, revision, fileName);
    }
    /**
     * Loads a content of the given file in the given revision. May communicate over network.
     * @param repoUrl target repository URL
     * @param url target file full url
     * @param revision target revision
     * @param pegRevision peg revision
     * @param fileName basename of the file which will be used as a temporary file's name
     * @return a temporary file with given file's content
     * @throws java.io.IOException
     */
    public File getFileRevision(SVNUrl repoUrl, SVNUrl url, String revision, String pegRevision, String fileName) throws IOException {
        boolean canUseRevisionsCache = true;
        try {
            canUseRevisionsCache = SVNRevision.getRevision(revision).getKind() == SVNRevision.Kind.number
                    && SVNRevision.getRevision(pegRevision).getKind() == SVNRevision.Kind.number;
        } catch (ParseException ex) {
        }
        try {
            Context context = new Context(VCSFileProxy.createFileProxy(fileSystem.getRoot()));
            if (!canUseRevisionsCache || "false".equals(System.getProperty("versioning.subversion.historycache.enable", "true"))) { //NOI18N
                SvnClient client = Subversion.getInstance().getClient(context, repoUrl);
                InputStream in = getInputStream(client, url, revision, pegRevision);
                return createContent(fileName, in);
            } else {
                String rootUrl = repoUrl.toString();
                String resourceUrl = url.toString() + "@" + pegRevision; //NOI18N
                Storage cachedVersions = StorageManager.getInstance().getStorage(rootUrl);
                File cachedFile = cachedVersions.getContent(resourceUrl, fileName, revision);
                if (cachedFile.length() == 0) { // not yet cached
                    SvnClient client = Subversion.getInstance().getClient(context, repoUrl);
                    InputStream in = getInputStream(client, url, revision, pegRevision);
                    cachedFile = createContent(fileName, in);
                    if (cachedFile.length() != 0) {
                        cachedVersions.setContent(resourceUrl, revision, cachedFile);
                    }
                }
                return cachedFile;
            }
        } catch (SVNClientException ex) {
            throw new IOException("Can not load: " + url + " in revision: " + revision, ex); //NOI18N
        }
    }

    /**
     * Loads the file in specified revision.
     * For peg revision <code>revision</code> will be used for existing files, for repository files it will be the HEAD revision.
     * <p>It's may connect over network I/O do not
     * call from the GUI thread.</p>
     *
     * @return null if the file does not exit in given revision
     */
    public VCSFileProxy getFileRevision(VCSFileProxy base, String revision) throws IOException {
        return getFileRevision(base, revision, Setup.REVISION_BASE);
    }

    /**
     * Loads the file in specified revision.
     *
     * <p>It's may connect over network I/O do not
     * call from the GUI thread.
     *
     * @return null if the file does not exit in given revision
     */
    public VCSFileProxy getFileRevision(VCSFileProxy base, String revision, String pegRevision) throws IOException {
        try {
            SvnClientFactory.checkClientAvailable(new Context(base));
        } catch (SVNClientException e) {
            return null;
        }
        if (Setup.REVISION_BASE.equals(revision)) {
            return getBaseRevisionFile(base);
        } else if (Setup.REVISION_PRISTINE.equals(revision)) {
            // should not be used, will not work with 1.7
            String name = base.getName();
            VCSFileProxy svnDir = getMetadataDir(base.getParentFile());
            if (svnDir != null) {
                VCSFileProxy text_base = VCSFileProxy.createFileProxy(svnDir, "text-base"); // NOI18N
                VCSFileProxy pristine = VCSFileProxy.createFileProxy(text_base, name + ".svn-base"); // NOI18N
                if (pristine.isFile()) {
                    return pristine;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else if (Setup.REVISION_CURRENT.equals(revision)) {
            return base;
        } else {
            SVNRevision svnrevision = SvnUtils.toSvnRevision(revision);
            try {
                SvnClient client = Subversion.getInstance().getClient(base);
                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                InputStream in = null;
                try {
                    boolean gotContent = false;
                    if ((cache.getStatus(base).getStatus() & FileInformation.STATUS_VERSIONED) != 0)  {
                        try {
                            in = client.getContent(base, svnrevision);
                            gotContent = true;
                        } catch (SVNClientException e) {
                            if(svnrevision.getKind() != SVNRevision.Kind.number 
                                    || !(SvnClientExceptionHandler.isFileNotFoundInRevision(e.getMessage()) || SvnClientExceptionHandler.isPathNotFound(e.getMessage()))) {
                                throw e;
                            }
                        }
                    }
                    if (!gotContent) {
                        if (Setup.REVISION_BASE.equals(pegRevision)) {
                            try {
                                // we can't contact server with BASE being peg revision
                                ISVNStatus st = SvnUtils.getSingleStatus(client, base);
                                if (st != null && st.getRevision() != null) {
                                    pegRevision = st.getRevision().toString();
                                } else {
                                    pegRevision = SVNRevision.HEAD.toString();
                                }
                            } catch (SVNClientException e) {}
                        }
                        SVNUrl url = SvnUtils.getRepositoryUrl(base);
                        if (url != null) {
                            in = getInputStream(client, url, revision, pegRevision);
                        } else {
                            in = new ByteArrayInputStream(org.openide.util.NbBundle.getMessage(VersionsCache.class, "MSG_UnknownURL").getBytes("UTF-8")); // NOI18N
                        }                
                    }
                } catch (SVNClientException e) {
                    if(SvnClientExceptionHandler.isFileNotFoundInRevision(e.getMessage()) ||
                            SvnClientExceptionHandler.isPathNotFound(e.getMessage())) {
                        in = new ByteArrayInputStream(new byte[] {});
                    } else {
                        throw e;
                    }
                }
                return VCSFileProxy.createFileProxy(createContent(base.getName(), in));
            } catch (SVNClientException ex) {
                throw new IOException("Can not load: " + base.getPath() + " in revision: " + revision, ex); //NOI18N
            }
        }
    }

    /**
     * Returns content of the given file at its base revision.
     * In other words, returns content of the given file without local
     * modifications.
     * The returned {@code File} may be deleted after use.
     * The returned {@code File} is <em>not</em> scheduled for automatic
     * deletion upon JVM shutdown.
     *
     * @param  referenceFile  reference file
     * @return  file holding content of the unmodified version of the given file
     * @exception  java.io.IOException  if some file handling operation failed
     */
    VCSFileProxy getBaseRevisionFile(VCSFileProxy referenceFile) throws IOException {
        try {
            boolean newMetadataFormat = false;
            VCSFileProxy svnDir = getMetadataDir(referenceFile.getParentFile());
            if (svnDir == null) {
                // try to check 1.7 metadata
                VCSFileProxy topmost = Subversion.getInstance().getTopmostManagedAncestor(referenceFile);
                VCSFileProxy newMetadataFolder;
                if (topmost != null && (newMetadataFolder = VCSFileProxy.createFileProxy(topmost, SvnUtils.SVN_ADMIN_DIR)).exists()) {
                    svnDir = newMetadataFolder;
                    newMetadataFormat = VCSFileProxy.createFileProxy(svnDir, "pristine").exists(); //NOI18N
                    if (!newMetadataFormat) {
                        Logger.getLogger(VersionsCache.class.getName()).log(Level.FINE,
                                "No 1.7 metadata in {0}", svnDir); //NOI18N
                    }
                }
            }
            if (svnDir == null) {
                return null;
            }
            if (newMetadataFormat) {
                return getContentBase(referenceFile, new File(Utils.getTempFolder(), referenceFile.getName() + ".netbeans-base")); //NOI18N
            } else {
                VCSFileProxy svnBase = VCSFileProxy.createFileProxy(svnDir, "text-base/" + referenceFile.getName() + ".svn-base"); //NOI18N
                if (!svnBase.exists()) {
                    if (VCSFileProxy.createFileProxy(svnDir, "pristine").exists()) { //NOI18N - svn1.7, file is directly in the root of a checkout
                        return getContentBase(referenceFile, new File(Utils.getTempFolder(), referenceFile.getName() + ".netbeans-base")); //NOI18N
                    }
                    return null;
                }
                VCSFileProxy expanded = VCSFileProxy.createFileProxy(svnDir, "text-base/" + referenceFile.getName() + ".netbeans-base"); //NOI18N
                if (VCSFileProxySupport.canRead(expanded) && svnBase.isFile() && VCSFileProxySupport.length(expanded) == VCSFileProxySupport.length(svnBase)
                        && (expanded.lastModified() >= svnBase.lastModified())) {
                    return expanded;
                }
                expanded = getContentBase(referenceFile, expanded);
                VCSFileProxySupport.setLastModified(expanded, svnBase);
                return expanded;
            }
        } catch (SVNClientException e) {
            throw new IOException(e);
        }
    }

    private VCSFileProxy getContentBase (VCSFileProxy referenceFile, VCSFileProxy output) throws SVNClientException, IOException {
        SvnClient client = Subversion.getInstance().getClient(false, new Context(referenceFile)); // local call, does not need to be instantiated with the file instance
        InputStream in;
        try {
            in = client.getContent(referenceFile, SVNRevision.BASE);
        } catch (SVNClientException ex) {
            if(SvnClientExceptionHandler.isUnversionedResource(ex.getMessage())
                    || SvnClientExceptionHandler.hasNoBaseRevision(ex.getMessage())) {
                // Subversion 1.7 error messages
                in = new ByteArrayInputStream(new byte[] {});
            } else {
                throw ex;
            }
        }
        VCSFileProxy res = output.normalizeFile();
        VCSFileProxySupport.copyStreamToFile(new BufferedInputStream(in), res);
        return res;
    }

    private VCSFileProxy getContentBase (VCSFileProxy referenceFile, File output) throws SVNClientException, IOException {
        SvnClient client = Subversion.getInstance().getClient(false, new Context(referenceFile)); // local call, does not need to be instantiated with the file instance
        InputStream in;
        try {
            in = client.getContent(referenceFile, SVNRevision.BASE);
        } catch (SVNClientException ex) {
            if(SvnClientExceptionHandler.isUnversionedResource(ex.getMessage())
                    || SvnClientExceptionHandler.hasNoBaseRevision(ex.getMessage())) {
                // Subversion 1.7 error messages
                in = new ByteArrayInputStream(new byte[] {});
            } else {
                throw ex;
            }
        }
        VCSFileProxy res = VCSFileProxy.createFileProxy(FileUtil.normalizeFile(output));
        VCSFileProxySupport.copyStreamToFile(new BufferedInputStream(in), res);
        return res;
    }

    /**
     * Tries to acquire a content of the given file url in the given revision
     * @param client
     * @param url
     * @param revision
     * @param pegRevision 
     * @return content of the file in the given revision
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException
     */
    private InputStream getInputStream (SvnClient client, SVNUrl url, String revision, String pegRevision) throws SVNClientException {
        InputStream in = null;
        try {
            in = client.getContent(url, SvnUtils.toSvnRevision(revision), SvnUtils.toSvnRevision(pegRevision));
        } catch (SVNClientException e) {
            if (SvnClientExceptionHandler.isFileNotFoundInRevision(e.getMessage()) ||
                    SvnClientExceptionHandler.isPathNotFound(e.getMessage())) {
                in = new ByteArrayInputStream(new byte[]{});
            } else {
                throw e;
            }
        }
        return in;
    }

    /**
     * Creates a temporary file and prints the content of <code>in</code> to it.
     * @param fileName temporary file name, will be prefixed by <code>"nb-svn"</code>
     * @param in content to be printed
     * @return created temporary file
     * @throws java.io.IOException
     */
    private File createContent (String fileName, InputStream in) throws IOException {
        // keep original extension so MIME can be guessed by the extension
        File tmp = new File(Utils.getTempFolder(), fileName);  // NOI18N
        tmp = FileUtil.normalizeFile(tmp);
        tmp.deleteOnExit();  // hard to track actual lifetime
        FileUtils.copyStreamToFile(new BufferedInputStream(in), tmp);
        return tmp;
    }

    private VCSFileProxy getMetadataDir(VCSFileProxy dir) {
        VCSFileProxy svnDir = VCSFileProxy.createFileProxy(dir, SvnUtils.SVN_ADMIN_DIR);  // NOI18N
        if (!svnDir.isDirectory()) {
            return null;
        }
        return svnDir;
    }
}
