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
     * Returns the value of {@link #PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE}.
     * @see #getPreferences()
     */
    public static boolean isTextAnnotationVisible() {
        return getPreferences().getBoolean(PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, true);
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
     * @see <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=105161">#105161</a>
     * @see <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=195284">#195284</a>
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
            FileUtil.refreshFor(fileList.toArray(new File[0])); 
        }
        for (Map.Entry<VCSFileProxyOperations, Set<VCSFileProxy>> e : proxyMap.entrySet()) {
            VCSFileProxyOperations fileProxyOperations = e.getKey();
            fileProxyOperations.refreshFor(e.getValue().toArray(new VCSFileProxy[fileList.size()]));
        }
    }
}