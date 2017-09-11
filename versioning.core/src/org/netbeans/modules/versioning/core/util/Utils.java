/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.core.util;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Date;
import javax.swing.Action;
import org.netbeans.modules.versioning.core.*;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.HistoryEntry;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.MessageEditProvider;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.ParentProvider;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.RevisionProvider;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.spi.queries.VisibilityQueryImplementation;

/**
 * Provides access to some versioning.core functionality needed by versioning.spi and versioning.ui.
 * 
 * WARNING: VCS internal use only, might be subject of future change and shouldn't be accessed by vcs clients.
 * 
 * @author Tomas Stupka
 */
public final class Utils {
    
/**
     * Indicates to the Versioning manager that the layout of versioned files may have changed. Previously unversioned 
     * files became versioned, versioned files became unversioned or the versioning system for some files changed.
     * The manager will flush any caches that may be holding such information.  
     * A versioning system usually needs to fire this after an Import action. 
     */
    public static final String EVENT_VERSIONED_ROOTS = "null VCS.VersionedFilesChanged";

    /**
     * The NEW value is a Set of Files whose versioning status changed. This event is used to re-annotate files, re-fetch
     * original content of files and generally refresh all components that are connected to these files.
     */
    public static final String EVENT_STATUS_CHANGED = "Set<File> VCS.StatusChanged";

    /**
     * Used to signal the Versioning manager that some annotations changed. Note that this event is NOT required in case
     * the status of the file changes in which case annotations are updated automatically. Use this event to force annotations
     * refresh in special cases, for example when the format of annotations changes.
     * Use null as new value to force refresh of all annotations.
     */
    public static final String EVENT_ANNOTATIONS_CHANGED = "Set<File> VCS.AnnotationsChanged";

    private Utils() { }
    
    /**
     * Return the LoacalHistory VersioningSystem in case it is available for the given file
     * 
     * @param file a file for which the the LoacalHistory VersioningSystem has to be retrieved
     * @return the LoacalHistory VersioningSystem
     */
    public static VersioningSystem getLocalHistory(VCSFileProxy file) {
        return VersioningManager.getInstance().getLocalHistory(file);
    }
    
    /**
     * Stop managing the given path by the given versioning system
     * 
     * @param versioningSystem the versioning system to stop manage for the given path
     * @param absolutePath the path to stop managed by the given versioning system
     * @see #connectRepository(org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem, java.lang.String) 
     */
    public static void disconnectRepository(VersioningSystem versioningSystem, String absolutePath) {
        VersioningConfig.getDefault().disconnectRepository(versioningSystem, absolutePath);
    }

    /**
     * Start again to manage the given path by the given versioning system
     * 
     * @param versioningSystem the versioning system to stop manage for the given path
     * @param absolutePath the path to stop managed by the given versioning system
     * @see #disconnectRepository(org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem, java.lang.String) 
     */
    public static void connectRepository(VersioningSystem versioningSystem, String absolutePath) {
        VersioningConfig.getDefault().connectRepository(versioningSystem, absolutePath);
    }

    /**
     * Returns all paths marked as not to managed by the given system
     * 
     * @param versioningSystem the versioning system 
     * @return path not managed by the given versioning system
     */
    public static String[] getDisconnectedRoots(VersioningSystem versioningSystem) {
        return VersioningConfig.getDefault().getDisconnectedRoots(versioningSystem);
    }
    
    /**
     * Empties the file owner cache
     */
    public static void flushNullOwners() {
        VersioningManager.getInstance().flushNullOwners();
    }
    
    /**
     * Notifies about visibility changes according to {@link VisibilityQueryImplementation}
     */
    public static void fireVisibilityChanged() {
        VcsVisibilityQueryImplementation.visibilityChanged();
    }
    
    /**
     * Notifies about visibility changes according to {@link VisibilityQueryImplementation}
     * 
     * @param File[] the files with a changed visibility
     */
    public static void fireVisibilityChanged(File... files) {
        VCSFileProxy[] proxies = new VCSFileProxy[files.length];
        for (int i = 0; i < files.length; i++) {
            proxies[i] = VCSFileProxy.createFileProxy(files[i]);
        }
        VcsVisibilityQueryImplementation.visibilityChanged(proxies);
    }
    
    /**
     * Notifies that a versioning system started to manage some previously unversioned files 
     * (e.g. those files were imported into repository).
     */
    public static void versionedRootsChanged() {
        VersioningManager.getInstance().versionedRootsChanged();
    }
    
    /**
     * Queries the Versioning infrastructure for file ownership.
     * 
     * @param proxy
     * @return VersioningSystem a system that owns (manages) the file or null if the file is not versioned
     */
    public static VCSSystemProvider.VersioningSystem getOwner(VCSFileProxy proxy) {
        return VersioningManager.getInstance().getOwner(proxy);        
    }
    
    /**
     * Tests whether the given file represents a flat folder (eg a java package), that is a folder 
     * that contains only its direct children.
     * 
     * @param file a File to test
     * @return true if the File represents a flat folder (eg a java package), false otherwise
     */
    public static boolean isFlat(File file) {
        return file instanceof FlatFolder; 
    }

    /**
     * Creates a File that is marked is flat (eg a java package), that is a folder 
     * that contains only its direct children.
     * 
     * @param path a file path
     * @return File a flat file representing given abstract path
     */    
    public static File getFlat(String path) {
        return new FlatFolder(path);
    }

    /**
     * Add PropertyChangeListener to be notified about changes in the versioning infrastructure.
     * @param l 
     */
    public static void addPropertyChangeListener(PropertyChangeListener l) {
        VersioningManager.getInstance().addPropertyChangeListener(l);
    }

    /**
     * Remove PropertyChangeListener 
     * @param l 
     */
    public static void removePropertyChangeListener(PropertyChangeListener l) {
        VersioningManager.getInstance().removePropertyChangeListener(l);
    }
    
    public static Object[] getDelegateEntry(VCSHistoryProvider.HistoryEntry entry) {
        return SPIAccessor.IMPL.getLookupObjects(entry);
    }
    
    public static HistoryEntry createHistoryEntry(VCSFileProxy[] proxies, Date dateTime, String message, String username, String usernameShort, String revision, String revisionShort, Action[] actions, RevisionProvider rp, MessageEditProvider mep, ParentProvider pp, Object[] lookupObjects) {
        HistoryEntry entry = new HistoryEntry(proxies, dateTime, message, username, usernameShort, revision, revisionShort, actions, rp, mep, pp);
        SPIAccessor.IMPL.setLookupObjects(entry, lookupObjects);
        return entry;
    }

    /**
     * Some folders are special and versioning should not look for metadata in
     * them. Folders like /net with automount enabled may take a long time to
     * answer I/O on their children, so
     * <code>VCSFileProxy.exists("/net/.git")</code> will freeze until it
     * timeouts. You should call this method before asking any I/O on children
     * of this folder you are unsure to actually exist. This does not mean
     * however that whole subtree should be excluded from version control, only
     * that you should not look for the metadata directly in this folder.
     * Returns <code>true</code> if the given folder is among such folders.
     * 
     * @param folder a folder to query
     * @return <code>true</code> if the given folder should be skipped when
     * searching for metadata.
     * @since 1.19
     */
    public static boolean isForbiddenFolder (VCSFileProxy folder) {
        return org.netbeans.modules.versioning.core.Utils.isForbiddenFolder(folder);
    }
    
}
