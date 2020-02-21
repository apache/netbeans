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

package org.netbeans.modules.git.remote;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.remotefs.versioning.api.RemoteFileSystemConnectionListener;
import org.netbeans.modules.remotefs.versioning.api.RemoteFileSystemConnectionManager;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.spi.VersioningSystem;
import org.netbeans.spi.queries.CollocationQueryImplementation2;
import org.openide.filesystems.FileSystem;

/**
 *
 */
@VersioningSystem.Registration(
    displayName="#CTL_Git_DisplayName", 
    menuLabel="#CTL_Git_MainMenu", 
    metadataFolderNames={".git"}, 
    actionsCategory="GitRemote"
)
public class GitVCS extends VersioningSystem implements PropertyChangeListener, PreferenceChangeListener, RemoteFileSystemConnectionListener {

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.git.remote.GitVCS"); //NOI18N

    public GitVCS() {
        //putProperty(PROP_DISPLAY_NAME, getDisplayName()); 
        //putProperty(PROP_MENU_LABEL, org.openide.util.NbBundle.getMessage(GitVCS.class, "CTL_Git_MainMenu")); // NOI18N
        RemoteFileSystemConnectionManager.getInstance().addRemoteFileSystemConnectionListener(this);
        GitModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
        Git.getInstance().registerGitVCS(this);
    }

    public static String getDisplayName() throws MissingResourceException {
        return org.openide.util.NbBundle.getMessage(GitVCS.class, "CTL_Git_DisplayName");
    }

    @Override
    public VCSAnnotator getVCSAnnotator () {
        return Git.getInstance().getVCSAnnotator();
    }

    @Override
    public VCSInterceptor getVCSInterceptor () {
        return Git.getInstance().getVCSInterceptor();
    }

    @Override
    public void getOriginalFile (VCSFileProxy workingCopy, VCSFileProxy originalFile) {
        Git.getInstance().getOriginalFile(workingCopy, originalFile);
    }

    @Override
    public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
        return Git.getInstance().getTopmostManagedAncestor(file);
    }
    
    @Override
    public CollocationQueryImplementation2 getCollocationQueryImplementation() {
        return collocationQueryImplementation;
    }

    @Override
    public VCSHistoryProvider getVCSHistoryProvider() {
        return Git.getInstance().getHistoryProvider();
    }

    private final CollocationQueryImplementation2 collocationQueryImplementation = new CollocationQueryImplementation2() {
        
        @Override
        public boolean areCollocated(URI a, URI b) {
            VCSFileProxy fra = VCSFileProxySupport.fromURI(a);
            VCSFileProxy frb = VCSFileProxySupport.fromURI(b);
            if (fra == null || frb == null) {
                return false;
            }
            fra = getTopmostManagedAncestor(fra);
            frb = getTopmostManagedAncestor(frb);
            if (fra == null || !fra.equals(frb)) {
                return false;
            }
            return true;
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
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(FileStatusCache.PROP_FILE_STATUS_CHANGED)) {
            FileStatusCache.ChangedEvent changedEvent = (FileStatusCache.ChangedEvent) event.getNewValue();
            fireStatusChanged(changedEvent.getFile());
        } else if (event.getPropertyName().equals(Git.PROP_ANNOTATIONS_CHANGED)) {
            fireAnnotationsChanged((Set<VCSFileProxy>) event.getNewValue());
        } else if (event.getPropertyName().equals(Git.PROP_VERSIONED_FILES_CHANGED)) {
            LOG.fine("cleaning unversioned parents cache"); //NOI18N
            Git.getInstance().clearAncestorCaches();
            fireVersionedFilesChanged();
        }
    }

    void refreshStatus (Set<VCSFileProxy> files) {
        fireStatusChanged(files == null || files.isEmpty() ? null : files);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(GitModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            fireStatusChanged((Set<VCSFileProxy>) null);
        }
    }

    @Override
    public void connected(FileSystem fs) {
        Git.getInstance().clearAncestorCaches();
        postVersionedRootsChanged();
    }

    @Override
    public void disconnected(FileSystem fs) {
        Git.getInstance().clearAncestorCaches();
        postVersionedRootsChanged();
    }
    
    private void postVersionedRootsChanged() {
        Git.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                VersioningSupport.versionedRootsChanged();
            }
        });
    }    
}
