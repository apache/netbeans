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
package org.netbeans.modules.versioning.core.spi;

import org.netbeans.modules.versioning.core.VersioningManager;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.spi.queries.CollocationQueryImplementation2;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * Base class for a versioning system that integrates into IDE.
 *
 * A versioning system provides these services:
 * - annotations (coloring, actions)
 * - file system handler
 * - diff provider
 * 
 * Versioning system registration can be done via {@link VersioningSystem.Registration}. 
 *
 * @author Maros Sandor
 * @author Tomas Stupka
 */
public abstract class VersioningSystem {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * Protected constructor, does nothing.   
     */
    protected VersioningSystem() {
    }

    /**
     * Tests whether the file is managed by this versioning system. If it is, the method should return the topmost 
     * ancestor of the file that is still versioned.
     * For example (for CVS) if all your CVS checkouts are in a directory /home/johndoe/projects/cvscheckouts/... then for all files
     * that are under "cvscheckouts" directory and for the directory itselft this method should 
     * return "/home/johndoe/projects/cvscheckouts/" and for all other files return null.
     *  
     * @param file a file
     * @return File the file itself or one of its ancestors or null if the supplied file is NOT managed by this versioning system
     */
    public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
        return null;
    }
    
    /**
     * Retrieves a VCSAnnotator implementation if this versioning system provides one. 
     * 
     * @return a VCSAnnotator implementation or null
     */ 
    public VCSAnnotator getVCSAnnotator() {
        return null;
    }

    /**
     * Retrieves a VCSInterceptor implementation if this versioning system provides one. 
     * 
     * @return a VCSInterceptor implementation or null
     */ 
    public VCSInterceptor getVCSInterceptor() {
        return null;
    }

    /**
     * Retrieves a VCSHistoryProvider implementation if this versioning system provides one.
     * 
     * @return a VCSHistoryProvider implementation or null
     */
    public VCSHistoryProvider getVCSHistoryProvider() {
        return null;
    }
    
    /**
     * Get the original (unmodified) copy of a file. If the versioning system cannot provide it then this method should do nothing.
     * For version control systems that support keyword expansion, the original file must expand all keywords so the
     * diff sidebar support will not report any differences in keywords.
     * 
     * @param workingCopy a File in the working copy  
     * @param originalFile placeholder File for the original (unmodified) copy of the working file
     */ 
    public void getOriginalFile(VCSFileProxy workingCopy, VCSFileProxy originalFile) {
        // default implementation does nothing
    }

    /**
     * Retrieves a CollocationQueryImplementation if this versioning system provides one.
     * 
     * @return CollocationQueryImplementation a CollocationQueryImplementation instance or null if the system does not provide the service
     */
    public CollocationQueryImplementation2 getCollocationQueryImplementation() {
        return null;
    }    

    /**
     * Retrieves a VCSVisibilityQuery implementation if this versioning system provides one.
     *
     * @return VCSVisibilityQuery a VCSVisibilityQuery instance or null if the system does not provide the service
     */
    public VCSVisibilityQuery getVisibilityQuery() {
        return null;
    }

    /**
     * Adds a listener for change events.
     * 
     * @param listener a PropertyChangeListener 
     */ 
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        synchronized(support) {
            support.addPropertyChangeListener(listener);
        }
    }

    /**
     * Removes a listener for change events.
     * 
     * @param listener a PropertyChangeListener 
     */ 
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        synchronized(support) {
            support.removePropertyChangeListener(listener);
        }
    }

    /**
     * Helper method to signal that annotations of a set of files changed. Do NOT fire this event when changes in
     * annotations are caused by changes of status. Status change event will refresh annotations automatically.
     *  
     * @param files set of files whose annotations changed or null if the change affects all files 
     */ 
    protected final void fireAnnotationsChanged(Set<VCSFileProxy> files) {
        support.firePropertyChange(VersioningManager.EVENT_ANNOTATIONS_CHANGED, null, files);
    }
    
    /**
     * Helper method to signal that status of a set of files changed. Status change event will refresh annotations automatically.
     *  
     * @param files set of files whose status changed or null if all files changed status 
     */ 
    protected final void fireStatusChanged(Set<VCSFileProxy> files) {
        support.firePropertyChange(VersioningManager.EVENT_STATUS_CHANGED, null, files);
    }

    /**
     * Helper method to signal that the versioning system started to manage some previously unversioned files 
     * (those files were imported into repository).
     */ 
    protected final void fireVersionedFilesChanged() {
        support.firePropertyChange(VersioningManager.EVENT_VERSIONED_ROOTS, null, null);
    }
    
    /**
     * Helper method that calls fireStatusChanged(Collections.singleton(file)). 
     *  
     * @param file a file whose status changed
     * @see #fireStatusChanged(java.util.Set)  
     */ 
    protected final void fireStatusChanged(VCSFileProxy file) {
        fireStatusChanged(Collections.singleton(file));
    }

    /**
     * Backdoor for DelegatingVCS
     * 
     * @return 
     */
    void moveChangeListeners(VersioningSystem system) {
        synchronized(support) {
            PropertyChangeListener[] listeners = support.getPropertyChangeListeners();
            for (PropertyChangeListener l : listeners) {
                system.addPropertyChangeListener(l);
                this.removePropertyChangeListener(l);
            }
        }
    }    
    
    /**
     * <p>
     * Register a VersioningSystem in the IDE.<br> 
     * </p>
     * <p>
     * If possible, prefer the annotation prior to a {@link org.openide.util.lookup.ServiceProvider} 
     * registration because of a better overall VCS performance. All necessary
     * information will be available to the VCS infrastructure to create 
     * menu items and handle file visibility without having to activate the particular 
     * VCS System until an explicit user action or relevant file event occurs.
     * </p>
     * See also <a href="@org-netbeans-modules-versioning@/org/netbeans/modules/versioning/spi/VersioningSystem.html">org.netbeans.modules.versioning.spi.VersioningSystem</a>.
     * 
     * @author Tomas Stupka
     * @see <a href="@org-netbeans-modules-versioning@/org/netbeans/modules/versioning/spi/VersioningSystem.html">org.netbeans.modules.versioning.spi.VersioningSystem</a>
     * @see org.openide.util.lookup.ServiceProvider
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Registration {
        
        /**
         * <p>
         * Short name of the versioning system, it will be used as popup menu label, label in tooltips, etc. 
         * </p>
         * <p>
         * Examples: CVS, Subversion, Mercurial, Teamware, SourceSafe, VSS, Clearcase, Local History.
         * </p>
         * <p>
         * Corresponds with the property value <a href="@org-netbeans-modules-versioning@/org/netbeans/modules/versioning/spi/VersioningSystem.html#PROP_DISPLAY_NAME">org.netbeans.modules.versioning.spi.VersioningSystem#PROP_DISPLAY_NAME</a>
         * when used together with <a href="@org-netbeans-modules-versioning@/org/netbeans/modules/versioning/spi/VersioningSystem.html">org.netbeans.modules.versioning.spi.VersioningSystem</a>.
         * </p>
         * @see <a href="@org-netbeans-modules-versioning@/org/netbeans/modules/versioning/spi/VersioningSystem.html#PROP_DISPLAY_NAME">org.netbeans.modules.versioning.spi.VersioningSystem#PROP_DISPLAY_NAME</a>
         */
        public String displayName();
        
        /**
         * <p>
         * Short name of the versioning system, it will be used as menu label and it should define a mnemonic key.
         * </p>
         * <p>
         * Examples: &amp;CVS, &amp;Subversion, &amp;Mercurial, &amp;Teamware, &amp;SourceSafe, &amp;VSS, &amp;Clearcase, Local &amp;History.
         * </p>
         * <p>
         * Corresponds with the property value <a href="@org-netbeans-modules-versioning@/org/netbeans/modules/versioning/spi/VersioningSystem.html#PROP_MENU_LABEL">org.netbeans.modules.versioning.spi.VersioningSystem#PROP_MENU_LABEL</a>.
         * @see <a href="@org-netbeans-modules-versioning@/org/netbeans/modules/versioning/spi/VersioningSystem.html#PROP_MENU_LABEL">org.netbeans.modules.versioning.spi.VersioningSystem#PROP_MENU_LABEL</a>
         */
        public String menuLabel();
        
        /**
         * The VCS Systems metadata folder names. The provided values are used to determine:
         * 
         * <ul>
         *   <li> file visibility via VisibilityQuery - whatever folder with a name provided at this place will be hidden in the IDE </li>
         *   <li> file ownership - the VCS infrastructure tests first
         *        if there is a folder with the provided name next to the given file or under
         *        one of its ancestors and only then delegates the <code>getTopmostManagedAncestor</code> 
         *        call to the actual Versioning System. With other words - a particular VCS system is queried
         *        about a files ownership only if it lies next to or under a folder containing a VCS metadata folder.</li>
         * </ul>
         * Examples:
         * <ul>
         *  <li>{"CVS"}</li>
         *  <li>{".svn", "_svn"}</li>
         *  <li>{".hg"}</li>
         *  <li>{".git"}</li>
         * </ul>
         * @see #getTopmostManagedAncestor(VCSFileProxy)
         */
        public String[] metadataFolderNames();
        
        /**
         * <p>
         * Determines the path under which this VCS Systems actions are registered. 
         * The two following subpaths are then derived from it:
         * 
         * <ul>
         * <li>Actions/Global - contains all global (contextless) actions for the Versioning System - e.g. Subversion Checkout</li> 
         * <li>Actions/Unversioned - contains actions which should be available for an unversioned project - e.g. Subversion Import</li>
         * </ul>
         * returning "Subversion" would make the VCS infrastructure to look for actions under the {@link ActionReference} 
         * paths <code>"Versioning/Subversion/Actions/Unversioned"</code> and <code>"Versioning/Subversion/Actions/Global"</code>.
         * <p>
         * You can also make use of the {@link ActionRegistration#menuText} and {@link ActionRegistration#popupText} 
         * attributes, in case you need a different text in the Main Menu item and a projects popup menu item.
         * Also note that this has no direct relevance to {@link ActionID#category}.
         * </p>
         * <p>
         * Example: <br>
         * <p>
         * Register a <code>VersioningSystem</code> with the <code>actionCategory</code> "Subversion".
         * </p>
         * <pre><code>
         * &#64;VersioningSystem.Registration(actionsCategory="<b>Subversion</b>")
         * public class SubversionVCS extends VersioningSystem {
         *    ...
         * }
         * </code></pre>
         * 
         * <p>
         * Register the <code>ImportAction</code> under the <code>ActionReference</code> path 
         * <code>"Versioning/Subversion/Actions/Unversioned"</code> and set <br> 
         * <code>ActionRegistration.popupText</code> to <code>"Import into &amp;Subversion Repository..."</code> and<br> 
         * <code>ActionRegistration.menuText</code> to <code>"I&amp;mport into Repository..."</code>.
         * <br></p>
         * 
         * <pre><code>
         * &#64;ActionID(id = "org.netbeans.modules.subversion.ui.project.ImportAction", category = "Subversion")
         * &#64;ActionRegistration(displayName = "Import into Repository...", popupText="Import into &amp;Subversion Repository...", menuText="I&amp;mport into Repository...")
         * &#64;ActionReferences({&#64;ActionReference(path="<b>Versioning/Subversion/Actions/Unversioned</b>", position=1) })
         * public final class ImportAction implements ActionListener {
         *   ...
         * }
         * </code></pre>
         * <p>
         * The Main Menu for an unversioned project then will be:<br>
         * <code>Main Menu > Team > Subversion > Import into <u>R</u>epository...</code>
         * </p>
         * <p>
         * The Popup Menu for an unversioned project then will be:<br>
         * <code>Popup Menu > Versioning > Import into <u>S</u>ubversion Repository...</code>
         * </p>
         * 
         * @see org.openide.awt.ActionID
         * @see org.openide.awt.ActionRegistration
         * @see org.openide.awt.ActionReference
         */
        public String actionsCategory();
    }
}
