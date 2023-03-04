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
package org.netbeans.modules.subversion;

import org.netbeans.modules.versioning.spi.VCSVisibilityQuery;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.SVNClientException;

import java.io.File;
import java.util.*;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import org.netbeans.modules.subversion.ui.shelve.ShelveChangesAction;
import org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry;
import org.netbeans.modules.versioning.spi.*;

/**
 * @author Maros Sandor
 * 
 * Warning:
 * $FOLDER_NAME:getenv:$VARIABLE:null|notnull - private contract with Versioning to determine 
 * the metadata folder name based on 
 * System.getenv($VARIABLE) == null or System.getenv($VARIABLE) != null respectively.
 * see also arch.xml in versioning.
 */
@VersioningSystem.Registration(
    displayName="#CTL_Subversion_DisplayName", 
    menuLabel="#CTL_Subversion_MainMenu", 
    metadataFolderNames={".svn:getenv:SVN_ASP_DOT_NET_HACK:null", "_svn:getenv:SVN_ASP_DOT_NET_HACK:notnull"}, 
    actionsCategory="Subversion"
)
public class SubversionVCS extends VersioningSystem implements VersioningListener, PreferenceChangeListener, PropertyChangeListener {
    
    private SubversionVisibilityQuery visibilityQuery;

    public SubversionVCS() {
        putProperty(PROP_DISPLAY_NAME, getDisplayName());
        putProperty(PROP_MENU_LABEL, NbBundle.getMessage(SubversionVCS.class, "CTL_Subversion_MainMenu"));
    
        SvnModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
        Subversion.getInstance().attachListeners(this);
        Subversion.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                ShelveChangesActionsRegistry.getInstance().registerAction(SubversionVCS.this, ShelveChangesAction.getProvider());
            }
        });
    }

    public static String getDisplayName() {
        return NbBundle.getMessage(SubversionVCS.class, "CTL_Subversion_DisplayName");
    }
    
    /**
     * Tests whether the file is managed by this versioning system. If it is, the method should return the topmost 
     * parent of the file that is still versioned.
     *  
     * @param file a file
     * @return File the file itself or one of its parents or null if the supplied file is NOT managed by this versioning system
     */
    @Override
    public File getTopmostManagedAncestor(File file) {
        return Subversion.getInstance().getTopmostManagedAncestor(file);
    }

    @Override
    public VCSAnnotator getVCSAnnotator() {
        return Subversion.getInstance().getVCSAnnotator();
    }

    @Override
    public VCSInterceptor getVCSInterceptor() {
        return Subversion.getInstance().getVCSInterceptor();
    }

    @Override
    public void getOriginalFile(File workingCopy, File originalFile) {
        Subversion.getInstance().getOriginalFile(workingCopy, originalFile);
    }

    @Override
    public VCSHistoryProvider getVCSHistoryProvider() {
        return Subversion.getInstance().getHistoryProvider();
    }
    
    @Override
    public CollocationQueryImplementation getCollocationQueryImplementation() {
        return collocationQueryImplementation;
    }

    @Override
    public VCSVisibilityQuery getVisibilityQuery() {
        if(visibilityQuery == null) {
            visibilityQuery = new SubversionVisibilityQuery();
        }
        return visibilityQuery;
    }

    private final CollocationQueryImplementation collocationQueryImplementation = new CollocationQueryImplementation() {
        @Override
        public boolean areCollocated(File a, File b) {
            File fra = getTopmostManagedAncestor(a);
            File frb = getTopmostManagedAncestor(b);
            if (fra == null || !fra.equals(frb)) return false;
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
        public File findRoot(File file) {
            // TODO: we should probably return the closest common ancestor
            return getTopmostManagedAncestor(file);
        }
    };
    
    @Override
    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            File file = (File) event.getParams()[0];
            fireStatusChanged(file);
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(SvnModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            fireStatusChanged((Set<File>) null);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Subversion.PROP_ANNOTATIONS_CHANGED)) {
            fireAnnotationsChanged((Set<File>) evt.getNewValue());
        } else if (evt.getPropertyName().equals(Subversion.PROP_BASE_FILE_CHANGED)) {
            fireStatusChanged((Set<File>) evt.getNewValue());
        } else if (evt.getPropertyName().equals(Subversion.PROP_VERSIONED_FILES_CHANGED)) {
            Subversion.LOG.fine("cleaning unversioned parents cache");
            fireVersionedFilesChanged();
        }
    }
}
