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

import java.util.Set;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.net.URI;
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
 * Extends framework <code>VersioningSystem</code> to Mercurial module functionality.
 * 
 * 
 */
@VersioningSystem.Registration(
    displayName="#CTL_Mercurial_DisplayName", //NOI18N
    menuLabel="#CTL_Mercurial_MainMenu", //NOI18N
    actionsCategory="MercurialRemote", //NOI18N
    metadataFolderNames=".hg") //NOI18N
public class MercurialVCS extends VersioningSystem implements PropertyChangeListener, PreferenceChangeListener, RemoteFileSystemConnectionListener {
    
    public MercurialVCS() {
        //putProperty(PROP_DISPLAY_NAME, getDisplayName()); // NOI18N
        //putProperty(PROP_MENU_LABEL, org.openide.util.NbBundle.getMessage(MercurialVCS.class, "CTL_Mercurial_MainMenu")); // NOI18N
        // moved to HgModuleConfig
        //HgModuleConfig.getDefault(root).getPreferences().addPreferenceChangeListener(this);
        RemoteFileSystemConnectionManager.getInstance().addRemoteFileSystemConnectionListener(this);
        Mercurial.getInstance().register(this);        
    }

    public static String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(MercurialVCS.class, "CTL_Mercurial_DisplayName");
    }

    @Override
    public CollocationQueryImplementation2 getCollocationQueryImplementation() {
        return collocationQueryImplementation;
    }

    @Override
    public VCSHistoryProvider getVCSHistoryProvider() {
        return Mercurial.getInstance().getMercurialHistoryProvider();
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
            
    /**
     * Tests whether the file is managed by this versioning system. If it is, 
     * the method should return the topmost 
     * ancestor of the file that is still versioned.
     *  
     * @param file a file
     * @return File the file itself or one of its ancestors or null if the 
     *  supplied file is NOT managed by this versioning system
     */
    @Override
    public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
        return Mercurial.getInstance().getTopmostManagedAncestor(file);
    }

    /**
     * Coloring label, modifying icons, providing action on file
     */
    @Override
    public VCSAnnotator getVCSAnnotator() {
        return Mercurial.getInstance().getMercurialAnnotator();
    }
    
    /**
     * Handle file system events such as delete, create, remove etc.
     */
    @Override
    public VCSInterceptor getVCSInterceptor() {
        return Mercurial.getInstance().getMercurialInterceptor();
    }

    @Override
    public void getOriginalFile(VCSFileProxy workingCopy, VCSFileProxy originalFile) {
        Mercurial.getInstance().getOriginalFile(workingCopy, originalFile);
    }

    @SuppressWarnings("unchecked") // Property Change event.getNewValue returning Object
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(FileStatusCache.PROP_FILE_STATUS_CHANGED)) {
            FileStatusCache.ChangedEvent changedEvent = (FileStatusCache.ChangedEvent) event.getNewValue();
            fireStatusChanged(changedEvent.getFile());
        } else if (event.getPropertyName().equals(Mercurial.PROP_HEAD_CHANGED)) {
            Set<VCSFileProxy> files = (Set<VCSFileProxy>) event.getNewValue();
            fireStatusChanged(files == null || files.isEmpty() ? null : files);
        } else if (event.getPropertyName().equals(Mercurial.PROP_ANNOTATIONS_CHANGED)) {
            fireAnnotationsChanged((Set<VCSFileProxy>) event.getNewValue());
        } else if (event.getPropertyName().equals(Mercurial.PROP_VERSIONED_FILES_CHANGED)) {
            Mercurial.LOG.fine("cleaning unversioned parents cache");   //NOI18N
            Mercurial.getInstance().clearAncestorCaches();
            fireVersionedFilesChanged();
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(HgModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            fireStatusChanged((Set<VCSFileProxy>) null);
        }
    }

    @Override
    public void connected(FileSystem fs) {
        Mercurial.getInstance().clearAncestorCaches();
        postVersionedRootsChanged();
    }

    @Override
    public void disconnected(FileSystem fs) {
        Mercurial.getInstance().clearAncestorCaches();
        postVersionedRootsChanged();
    }
    
    private void postVersionedRootsChanged() {
        Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                VersioningSupport.versionedRootsChanged();
            }
        });
    }
}
