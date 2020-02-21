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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.remotefs.versioning.api.RemoteFileSystemConnectionListener;
import org.netbeans.modules.remotefs.versioning.api.RemoteFileSystemConnectionManager;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.spi.VCSVisibilityQuery;
import org.netbeans.modules.versioning.core.spi.VersioningSystem;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.spi.queries.CollocationQueryImplementation2;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
@VersioningSystem.Registration(displayName="#CTL_Subversion_DisplayName",  //NOI18N
    menuLabel="#CTL_Subversion_MainMenu",  //NOI18N
    metadataFolderNames={".svn:getenv:SVN_ASP_DOT_NET_HACK:null", "_svn:getenv:SVN_ASP_DOT_NET_HACK:notnull"},  //NOI18N
    actionsCategory="RemoteSubversion") //NOI18N
public class SubversionVCS extends VersioningSystem implements PropertyChangeListener, VersioningListener, PreferenceChangeListener, RemoteFileSystemConnectionListener {

    /**
     * Fired when textual annotations and badges have changed. The NEW value is
     * Set<File> of files that changed or NULL if all annotations changed.
     */
    static final String PROP_ANNOTATIONS_CHANGED = "annotationsChanged"; // NOI18N

    private VCSVisibilityQuery visibilityQuery;

    public SubversionVCS() {
        RemoteFileSystemConnectionManager.getInstance().addRemoteFileSystemConnectionListener(this);
        for(FileSystem fileSystem : VCSFileProxySupport.getConnectedFileSystems()) {
            SvnModuleConfig.getDefault(fileSystem).getPreferences().addPreferenceChangeListener(this);
        }
        Subversion.getInstance().attachListeners(this);
    }

    public static String getDisplayName() {
        return NbBundle.getMessage(SubversionVCS.class, "CTL_Subversion_DisplayName");
    }

    /**
     * Tests whether the file is managed by this versioning system. If it is,
     * the method should return the topmost ancestor of the file that is still
     * versioned.
     *
     * @param file a file
     * @return File the file itself or one of its ancestors or null if the
     * supplied file is NOT managed by this versioning system
     */
    @Override
    public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
        return Subversion.getInstance().getTopmostManagedAncestor(file);
    }

    /**
     * Coloring label, modifying icons, providing action on file
     */
    @Override
    public VCSAnnotator getVCSAnnotator() {
        return Subversion.getInstance().getVCSAnnotator();
    }

    /**
     * Handle file system events such as delete, create, remove etc.
     */
    @Override
    public VCSInterceptor getVCSInterceptor() {
        return Subversion.getInstance().getInterceptor();
    }

    @Override
    public void getOriginalFile(VCSFileProxy workingCopy, VCSFileProxy originalFile) {
        Subversion.getInstance().getOriginalFile(workingCopy, originalFile);
    }

    @Override
    public VCSHistoryProvider getVCSHistoryProvider() {
        return Subversion.getInstance().getHistoryProvider();
    }
    
    @Override
    public CollocationQueryImplementation2 getCollocationQueryImplementation() {
        return collocationQueryImplementation;
    }

    @Override
    public VCSVisibilityQuery getVisibilityQuery() {
        if (visibilityQuery == null) {
            visibilityQuery = new SubversionVisibilityQuery();
        }
        return visibilityQuery;
    }
    
    private final CollocationQueryImplementation2 collocationQueryImplementation = new CollocationQueryImplementation2() {

        @Override
        public boolean areCollocated(URI file1, URI file2) {
            VCSFileProxy a = VCSFileProxySupport.fromURI(file1);
            VCSFileProxy b = VCSFileProxySupport.fromURI(file2);
            if (a == null || b == null) {
                return false;
            }
            VCSFileProxy fra = getTopmostManagedAncestor(a);
            VCSFileProxy frb = getTopmostManagedAncestor(b);
            if (fra == null || !fra.equals(frb)) {
                return false;
            }
            try {
                SVNUrl ra = SvnUtils.getRepositoryRootUrl(a);
                if(ra == null) {
                    // this might happen. there is either no svn client available or
                    // no repository url stored in the metadata (svn < 1.3).
                    // one way or another, can't do anything reasonable at this point
                    Subversion.LOG.log(Level.WARNING, "areCollocated returning false due to missing repository url for {0} {1}", new Object[] {a, b});
                    return false;
                }
                SVNUrl rb = SvnUtils.getRepositoryRootUrl(b);
                SVNUrl rr = SvnUtils.getRepositoryRootUrl(fra);
                return ra.equals(rb) && ra.equals(rr);
            } catch (SVNClientException e) {
                if (!WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                    Subversion.LOG.log(Level.INFO, null, e);
                }
                Subversion.LOG.log(Level.WARNING, "areCollocated returning false due to catched exception " + a + " " + b);
                // root not found
                return false;
            }
        }

        @Override
        public URI findRoot(URI file) {
            // TODO: we should probably return the closest common ancestor
            VCSFileProxy fromURI = VCSFileProxySupport.fromURI(file);
            if (fromURI != null) {
                VCSFileProxy topmostManagedAncestor = getTopmostManagedAncestor(fromURI);
                if (topmostManagedAncestor != null) {
                    return VCSFileProxySupport.toURI(topmostManagedAncestor);
                }
            }
            return null;
        }
    };

    @Override
    public void versioningEvent(VersioningEvent event) {
        if (FileStatusCache.EVENT_FILE_STATUS_CHANGED.equals(event.getId())) {
            VCSFileProxy file = (VCSFileProxy) event.getParams()[0];
            if (file != null) {
                fireStatusChanged(file);
            }
        }
    }
    
    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(SvnModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            fireStatusChanged((Set<VCSFileProxy>) null);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Subversion.PROP_ANNOTATIONS_CHANGED)) {
            fireAnnotationsChanged((Set<VCSFileProxy>) evt.getNewValue());
        } else if (evt.getPropertyName().equals(Subversion.PROP_BASE_FILE_CHANGED)) {
            fireStatusChanged((Set<VCSFileProxy>) evt.getNewValue());
        } else if (evt.getPropertyName().equals(Subversion.PROP_VERSIONED_FILES_CHANGED)) {
            Subversion.LOG.fine("cleaning unversioned parents cache");
            fireVersionedFilesChanged();
        }
    }

    @Override
    public void connected(FileSystem fs) {
        Subversion.getInstance().versionedFilesChanged();
        postVersionedRootsChanged();
    }

    @Override
    public void disconnected(FileSystem fs) {
        Subversion.getInstance().versionedFilesChanged();
        postVersionedRootsChanged();
    }
    
    private void postVersionedRootsChanged() {
        Subversion.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                VersioningSupport.versionedRootsChanged();
            }
        });
    }
}
