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
package org.netbeans.modules.versioning.core.api;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import org.netbeans.modules.versioning.core.*;
import org.netbeans.modules.versioning.core.filesystems.VCSFileProxyOperations;
import org.netbeans.modules.versioning.core.spi.VersioningSystem;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.openide.filesystems.FileUtil;

/**
 * Collection of utility methods for Versioning systems implementors. 
 * 
 * @author Maros Sandor
 * @author Tomas Stupka
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
        return VersioningConfig.getDefault().getPreferences();
    }
        
    /**
     * Queries the Versioning infrastructure for file ownership.
     * 
     * @param file a file to examine
     * @return VersioningSystem a system that owns (manages) the file or null if the file is not versioned
     */
    public static VersioningSystem getOwner(VCSFileProxy file) {
        VCSSystemProvider.VersioningSystem owner = VersioningManager.getInstance().getOwner(file);
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
    public static boolean isFlat(VCSFileProxy file) {
        return APIAccessor.IMPL.isFlat(file);
    }
    
    /**
     * Helper method to signal that a versioning system started to manage some previously unversioned files 
     * (those files were imported into repository).
     */
    public static void versionedRootsChanged() {
        VersioningManager.getInstance().versionedRootsChanged();
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
     * @return true if the given folder is excluded from version control, false otherwise
     */
    public static boolean isExcluded (VCSFileProxy folder) {
        // forbid scanning for UNC paths \\ or \\computerName
        if (folder.getPath().startsWith("\\\\")) { //NOI18N
            return folder.getParentFile() == null || folder.getParentFile().getPath().equals("\\\\"); //NOI18N
        }
        for (String unversionedFolderPath : Utils.getUnversionedFolders()) {
            if (Utils.isAncestorOrEqual(unversionedFolderPath, folder)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Creates Process builder.
     * All VCS clients should use this process builder instead of java.lang.ProcessBuilder
     * 
     * @param file
     * @return process builder for local or remote environment 
     */
    public static org.netbeans.api.extexecution.ProcessBuilder createProcessBuilder(VCSFileProxy file) {
        return file.createProcessBuilder();
    }
    
    /**
     * Refreshes all necessary filesystems. Not all instances of <code>FileObject</code> are refreshed
     * but just those that represent passed <code>files</code> and their children recursively.
     * 
     * @param files 
     */
    public static void refreshFor(VCSFileProxy[] files) {
        Set<File> fileList = new HashSet<File>();
        Map<VCSFileProxyOperations, Set<VCSFileProxy>> proxyMap = new HashMap<VCSFileProxyOperations, Set<VCSFileProxy>>();
        for(VCSFileProxy file : files) {
            File javaFile = file.toFile();
            if (javaFile != null) {
                fileList.add(javaFile);
            } else {
                VCSFileProxyOperations fileProxyOperations = file.getFileProxyOperations();
                if (fileProxyOperations != null) {
                    Set<VCSFileProxy> set = proxyMap.get(fileProxyOperations);
                    if (set == null) {
                        set = new HashSet<VCSFileProxy>();
                        proxyMap.put(fileProxyOperations, set);
                    }
                    set.add(file);
                }
            }
        }
        if (fileList.size() > 0) {
            FileUtil.refreshFor(fileList.toArray(new File[fileList.size()])); 
        }
        for (Map.Entry<VCSFileProxyOperations, Set<VCSFileProxy>> e : proxyMap.entrySet()) {
            VCSFileProxyOperations fileProxyOperations = e.getKey();
            fileProxyOperations.refreshFor(e.getValue().toArray(new VCSFileProxy[fileList.size()]));
        }
    }
}