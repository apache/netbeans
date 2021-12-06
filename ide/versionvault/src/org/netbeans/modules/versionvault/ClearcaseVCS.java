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
/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.netbeans.modules.versionvault;

import org.netbeans.modules.versionvault.*;
import java.io.File;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.util.Set;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VCSVisibilityQuery;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.openide.util.NbBundle;

/**
 * Extends framework <code>VersioningSystem</code> to Clearcase module functionality.
 * 
 * @author Maros Sandor
 */
/*@VersioningSystem.Registration(
    displayName="#OpenIDE-Module-Name", 
    menuLabel="#OpenIDE-Module-Name", 
    metadataFolderNames={".hcl"}, 
    actionsCategory="VersionVault"
)*/
@VersioningSystem.Registration(
    displayName="#OpenIDE-Module-Name", 
    menuLabel="#OpenIDE-Module-Name", 
    metadataFolderNames={".git"}, 
    actionsCategory="Git"
)
public class ClearcaseVCS extends VersioningSystem implements PropertyChangeListener, VersioningListener {

    /**
     * Fired when textual annotations and badges have changed. The NEW value is Set<File> of files that changed or NULL
     * if all annotaions changed.
     */
    static final String PROP_ANNOTATIONS_CHANGED = "annotationsChanged";
    
    private VCSVisibilityQuery visibilityQuery;

    public ClearcaseVCS() {
        putProperty(PROP_DISPLAY_NAME, NbBundle.getMessage(ClearcaseVCS.class, "VCS_Clearcase_Name"));
        putProperty(PROP_MENU_LABEL, NbBundle.getMessage(ClearcaseVCS.class, "VCS_Clearcase_Menu_Label"));
        Clearcase.getInstance().getFileStatusCache().addVersioningListener(this);
        Clearcase.getInstance().getAnnotator().addPropertyChangeListener(this);
    }

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
        return Clearcase.getInstance().getTopmostManagedParent(file);
    }
    
    /**
     * Coloring label, modifying icons, providing action on file
     */
    @Override
    public VCSAnnotator getVCSAnnotator() {
        return Clearcase.getInstance().getAnnotator();
    }
    
    /**
     * Handle file system events such as delete, create, remove etc.
     */
    @Override
    public VCSInterceptor getVCSInterceptor() {
        return Clearcase.getInstance().getInterceptor();
    }

    @Override
    public CollocationQueryImplementation getCollocationQueryImplementation() {
        return collocationQueryImplementation;
    }

    @Override
    public VCSVisibilityQuery getVisibilityQuery() {
        if(visibilityQuery == null) {
            visibilityQuery = new ClearcaseVisibilityQuery();
        }
        return visibilityQuery;
    }

    private final CollocationQueryImplementation collocationQueryImplementation = new CollocationQueryImplementation() {
    
        public boolean areCollocated(File a, File b) {
            File fra = getTopmostManagedAncestor(a);
            File frb = getTopmostManagedAncestor(b);
            if (fra == null || !fra.equals(frb)) return false;
            
            // TODO: should we check that they come from the same view?
            return true;
        }
    
        public File findRoot(File file) {
            // TODO: we should probably return the closest common ancestor
            return getTopmostManagedAncestor(file);
        }
    };
    
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(ClearcaseAnnotator.PROP_ANNOTATIONS_CHANGED)) {
            fireAnnotationsChanged((Set<File>) event.getNewValue());
        } 
    }

    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            File file = (File) event.getParams()[0];
            fireStatusChanged(file);
        }
    }
    
    @Override
    public void getOriginalFile(File workingCopy, File originalFile) {
        Clearcase.getInstance().getOriginalFile(workingCopy, originalFile);
    }
}
