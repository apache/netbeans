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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.mercurial;

import java.io.File;
import java.util.Set;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VersioningSystem;

/**
 * Extends framework <code>VersioningSystem</code> to Mercurial module functionality.
 * 
 * @author Maros Sandor
 */
@VersioningSystem.Registration(
    displayName="#CTL_Mercurial_DisplayName",
    menuLabel="#CTL_Mercurial_MainMenu",
    actionsCategory="Mercurial",
    metadataFolderNames=".hg")
public class MercurialVCS extends VersioningSystem implements PropertyChangeListener, PreferenceChangeListener {

    public MercurialVCS() {
        putProperty(PROP_DISPLAY_NAME, getDisplayName()); // NOI18N
        putProperty(PROP_MENU_LABEL, org.openide.util.NbBundle.getMessage(MercurialVCS.class, "CTL_Mercurial_MainMenu")); // NOI18N
        
        HgModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
        Mercurial.getInstance().register(this);        
    }

    public static String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(MercurialVCS.class, "CTL_Mercurial_DisplayName");
    }

    @Override
    public CollocationQueryImplementation getCollocationQueryImplementation() {
        return collocationQueryImplementation;
    }

    @Override
    public VCSHistoryProvider getVCSHistoryProvider() {
        return Mercurial.getInstance().getMercurialHistoryProvider();
    }

    
    private final CollocationQueryImplementation collocationQueryImplementation = new CollocationQueryImplementation() {
        @Override
        public boolean areCollocated(File a, File b) {
            File fra = getTopmostManagedAncestor(a);
            File frb = getTopmostManagedAncestor(b);

            if (fra == null || !fra.equals(frb)) return false;

            return true;
        }

        @Override
        public File findRoot(File file) {
            // TODO: we should probably return the closest common ancestor
            return getTopmostManagedAncestor(file);
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
    public File getTopmostManagedAncestor(File file) {
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
    public void getOriginalFile(File workingCopy, File originalFile) {
        Mercurial.getInstance().getOriginalFile(workingCopy, originalFile);
    }

    @SuppressWarnings("unchecked") // Property Change event.getNewValue returning Object
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(FileStatusCache.PROP_FILE_STATUS_CHANGED)) {
            FileStatusCache.ChangedEvent changedEvent = (FileStatusCache.ChangedEvent) event.getNewValue();
            fireStatusChanged(changedEvent.getFile());
        } else if (event.getPropertyName().equals(Mercurial.PROP_HEAD_CHANGED)) {
            Set<File> files = (Set<File>) event.getNewValue();
            fireStatusChanged(files == null || files.isEmpty() ? null : files);
        } else if (event.getPropertyName().equals(Mercurial.PROP_ANNOTATIONS_CHANGED)) {
            fireAnnotationsChanged((Set<File>) event.getNewValue());
        } else if (event.getPropertyName().equals(Mercurial.PROP_VERSIONED_FILES_CHANGED)) {
            Mercurial.LOG.fine("cleaning unversioned parents cache");   //NOI18N
            Mercurial.getInstance().clearAncestorCaches();
            fireVersionedFilesChanged();
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(HgModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            fireStatusChanged((Set<File>) null);
        }
    }
}
