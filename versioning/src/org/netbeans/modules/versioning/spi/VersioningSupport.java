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
package org.netbeans.modules.versioning.spi;

import java.io.File;
import java.util.prefs.Preferences;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.DelegatingVCS;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.netbeans.modules.versioning.core.util.Utils;

/**
 * Collection of utility methods for Versioning systems implementors. 
 * 
 * @author Maros Sandor
 */
public final class VersioningSupport {
    
    /**
     * Boolean property defining visibility of textual versioning annotations (aka Status Labels).
     * 
     * @see #getPreferences()
     */
    public static final String PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE = "textAnnotationsVisible";
    
    private VersioningSupport() {
    }
    
    /**
     * Common settings and preferences for versioning modules are set in this preferences node.  
     * 
     * @return Preferences node for Versioning modules
     * @see #PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE
     */
    public static Preferences getPreferences() {
        return org.netbeans.modules.versioning.core.api.VersioningSupport.getPreferences();
    }
        
    /**
     * Queries the Versioning infrastructure for file ownership.
     * 
     * @param file a file to examine
     * @return VersioningSystem a system that owns (manages) the file or null if the file is not versioned
     */
    public static VersioningSystem getOwner(File file) {
        VCSSystemProvider.VersioningSystem owner = Utils.getOwner(VCSFileProxy.createFileProxy(file));
        if(owner != null) {
            Object delegate = owner.getDelegate();
            if(delegate instanceof DelegatingVCS) {
                return ((DelegatingVCS) delegate).getDelegate();
            } else if(delegate instanceof VersioningSystem) {
                return (VersioningSystem) delegate;
            }
        }
        return null;
    }

    /**
     * Tests whether the given file represents a flat folder (eg a java package), that is a folder 
     * that contains only its direct children.
     * 
     * @param file a File to test
     * @return true if the File represents a flat folder (eg a java package), false otherwise
     */
    public static boolean isFlat(File file) {
        return Utils.isFlat(file);
    }

    /**
     * Creates a File that is marked is flat (eg a java package), that is a folder 
     * that contains only its direct children.
     * 
     * @param path a file path
     * @return File a flat file representing given abstract path
     */
    public static File getFlat(String path) {
        return Utils.getFlat(path);
    }
    
    /**
     * Helper method to signal that a versioning system started to manage some previously unversioned files 
     * (those files were imported into repository).
     * 
     * @see VersioningSystem#fireVersionedFilesChanged() 
     */
    public static void versionedRootsChanged() {
        Utils.versionedRootsChanged();
    }

    /**
     * Tests whether the given folder is excluded (unversioned) from version control.
     * <ul>
     * <li>Folders set in <code>versioning.unversionedFolders</code> system property are excluded. 
     * Misconfigured automount daemons may try to look for versioning metadata causing hangs and full load.</li>
     * <li>Netbeans userdir is excluded by default. To include the userdir set <code>versioning.netbeans.user.versioned</code> system property to <code>true</code></li>
     * </ul>
     * <p>Versioning systems <strong>must NOT</strong> scan a folder if this method returns true and should consider it as unversioned.</p>
     *
     * @param folder a folder to query
     * @link http://www.netbeans.org/bugzilla/show_bug.cgi?id=105161
     * @link http://www.netbeans.org/bugzilla/show_bug.cgi?id=195284
     * @since 1.25
     * @return true if the given folder is excluded from version control, false otherwise
     */
    public static boolean isExcluded (File folder) {
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(folder);
        return proxy != null ? org.netbeans.modules.versioning.core.api.VersioningSupport.isExcluded(proxy) : null;
    }
}
