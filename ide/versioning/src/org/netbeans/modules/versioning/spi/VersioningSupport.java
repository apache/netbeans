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
