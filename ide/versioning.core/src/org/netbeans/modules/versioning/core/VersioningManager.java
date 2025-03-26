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
package org.netbeans.modules.versioning.core;

import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.spi.VCSVisibilityQuery;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import java.lang.reflect.Method;
import java.util.Map.Entry;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.util.Lookup;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import java.io.File;
import java.util.*;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor.VCSAnnotationEvent;
import org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor.VCSAnnotationListener;
import org.netbeans.modules.versioning.core.spi.*;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.netbeans.spi.queries.CollocationQueryImplementation2;
import org.openide.modules.Places;
import org.openide.util.*;
import org.openide.util.Lookup.Result;

/**
 * Top level versioning manager that mediates communitation between IDE and registered versioning systems.
 * 
 * @author Maros Sandor
 */
public class VersioningManager implements PropertyChangeListener, ChangeListener, PreferenceChangeListener {
    
    /**
     * @see org.netbeans.modules.versioning.core.util.Utils#EVENT_VERSIONED_ROOTS
     */
    public static final String EVENT_VERSIONED_ROOTS = org.netbeans.modules.versioning.core.util.Utils.EVENT_VERSIONED_ROOTS;

    /**
     * @see org.netbeans.modules.versioning.core.util.Utils#EVENT_STATUS_CHANGED
     */
    public static final String EVENT_STATUS_CHANGED = org.netbeans.modules.versioning.core.util.Utils.EVENT_STATUS_CHANGED;

    /**
     * @see org.netbeans.modules.versioning.core.util.Utils#EVENT_ANNOTATIONS_CHANGED
     */
    public static final String EVENT_ANNOTATIONS_CHANGED = org.netbeans.modules.versioning.core.util.Utils.EVENT_ANNOTATIONS_CHANGED;

    public static final String ATTRIBUTE_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";

    /**
     * Priority defining the order of versioning systems used when determining the owner of a file. I.e. what versioning system should handle the file.
     * @see #getProperty(String)
     * @see #putProperty(String, Object)
     */
    static final String PROP_PRIORITY = "Integer VCS.Priority"; //NOI18N
    
    private static VersioningManager instance;
    private static boolean initialized = false;
    private static boolean initializing = false;
    private static final Object INIT_LOCK = new Object();
    private static volatile Set<VCSAnnotationListener> statusListeners = Collections.emptySet();

    public static synchronized VersioningManager getInstance() {
        if (instance == null) {
            instance = new VersioningManager();
            instance.init();
        }
        return instance;
    }

    public static boolean isInitialized() {
        if(initialized && OpenProjects.getDefault().openProjects().isDone()) {
            return true;
        }
        synchronized(INIT_LOCK) {
            if(!initializing) {
                initializing = true;
                new RequestProcessor("Initialize VCS").post(new Runnable() {        // NOI18N
                    @Override
                    public void run() {                    
                        getInstance(); // init manager                                                    
                    }                    
                });
            }
        }
        return false;
    }

    public static void deliverStatusEvent(VCSAnnotationEvent ev) {
        for (VCSAnnotationListener l : statusListeners) {
            l.annotationChanged(ev);
        }
    }

    // ======================================================================================================

    /**
     * Holds all registered versioning systems.
     */
    private final List<VCSSystemProvider.VersioningSystem> versioningSystems = new ArrayList<VCSSystemProvider.VersioningSystem>(5);

    /**
     * What folder is versioned by what versioning system. 
     * TODO: use SoftHashMap if there is one available in APIs
     */
    private final Map<VCSFileProxy, VCSSystemProvider.VersioningSystem> folderOwners = new HashMap<VCSFileProxy, VCSSystemProvider.VersioningSystem>(200);
    
    /**
     * What file is versioned by what versioning system - keep it small
     * We will hold only recently questioned files for cases when a file is subsequently 
     * queried too often in a short time.
     * 
     */
    private final Map<VCSFileProxy, VCSSystemProvider.VersioningSystem> fileOwners = new LinkedHashMap<VCSFileProxy, VCSSystemProvider.VersioningSystem>(50) {
        private int MAX_SIZE = 50;
        @Override
        protected boolean removeEldestEntry(Entry<VCSFileProxy, VCSSystemProvider.VersioningSystem> eldest) {
            return size() > MAX_SIZE;
        }        
    };

    /**
     * Holds registered local history system.
     */
    private VCSSystemProvider.VersioningSystem localHistory;

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.versioning");

    /**
     * What files or folders are managed by local history.
     * TODO: use SoftHashMap if there is one available in APIs
     */
    private final Map<VCSFileProxy, Boolean> localHistoryFiles = new LinkedHashMap<VCSFileProxy, Boolean>(200);

    /**
     * Holds methods intercepted by a specific vcs. See {@link #needsLocalHistory(methodName)}
     */
    private Map<String, Set<String>> interceptedMethods = new HashMap<String, Set<String>>();

    private final VersioningSystem NULL_OWNER = new VersioningSystem() {
        @Override public boolean isLocalHistory() {  throw new IllegalStateException(); }
        @Override public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) { throw new IllegalStateException(); }
        @Override public VCSInterceptor getVCSInterceptor() { throw new IllegalStateException(); }
        @Override public void getOriginalFile(VCSFileProxy workingCopy, VCSFileProxy originalFile) { throw new IllegalStateException(); }
        @Override public CollocationQueryImplementation2 getCollocationQueryImplementation() { throw new IllegalStateException(); }
        @Override public void addPropertyCL(PropertyChangeListener listener) { throw new IllegalStateException(); }
        @Override public void removePropertyCL(PropertyChangeListener listener) { throw new IllegalStateException(); }
        @Override public boolean isExcluded(VCSFileProxy file) { throw new IllegalStateException(); }
        @Override public VCSAnnotator getVCSAnnotator() { throw new IllegalStateException(); }
        @Override public VCSVisibilityQuery getVisibilityQuery() { throw new IllegalStateException(); }
        @Override public Object getDelegate() { throw new IllegalStateException(); }
        @Override public String getDisplayName() { throw new IllegalStateException(); }
        @Override public String getMenuLabel() { throw new IllegalStateException(); }
        @Override public boolean accept(VCSContext ctx) { throw new IllegalStateException(); }
        @Override public VCSHistoryProvider getVCSHistoryProvider() {throw new IllegalStateException(); }
        @Override public boolean isMetadataFile(VCSFileProxy file) { throw new IllegalStateException(); }
    };
    
    
    private final Result<VCSSystemProvider> providersLookupResult;
    
    private VersioningManager() {
        providersLookupResult = Lookup.getDefault().lookupResult(VCSSystemProvider.class);
    }
    
    private void init() {
        try {
            // initialize VCSContext which in turn initializes SPIAccessor
            // before any other thread touches SPIAccessor
            VCSContext ctx = VCSContext.EMPTY;
            synchronized (versioningSystems) {
                Collection<? extends VCSSystemProvider> providers = providersLookupResult.allInstances();
                for (VCSSystemProvider p : providers) {
                    p.addChangeListener(this);
                }
                // do not fire events under lock but asynchronously
                refreshVersioningSystems(true);
            }
            VersioningSupport.getPreferences().addPreferenceChangeListener(this);
        } finally {
            initialized = true;                                    
        }
    }

    /**
     * List of versioning systems changed.
     */
    private void refreshVersioningSystems (boolean fireAsync) {
        synchronized(versioningSystems) {
            // inline unloadVersioningSystems();
            for (VCSSystemProvider.VersioningSystem system : versioningSystems) {
                system.removePropertyCL(this);
            }
            versioningSystems.clear();
            Collection<? extends VCSSystemProvider> providers = providersLookupResult.allInstances();
            for (VCSSystemProvider p : providers) {
                Collection<VCSSystemProvider.VersioningSystem> systems = p.getVersioningSystems();
                localHistory = null;
                // inline unloadVersioningSystems();

                // inline loadVersioningSystems(systems);
                versioningSystems.addAll(systems);
                for (VCSSystemProvider.VersioningSystem system : versioningSystems) {
                    if (localHistory == null && system.isLocalHistory()) {
                        localHistory = system;
                    }
                    system.addPropertyCL(this);
                }
                // inline loadVersioningSystems(systems);
            }
        }

        if (fireAsync) {
            // this happens only once, default RP is fine
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run () {
                    versionedRootsChanged();
                }
            });
        } else {
            versionedRootsChanged();
        }
    }

    private void fireFileStatusChanged(Set<VCSFileProxy> files) {
        // pushing the change ... DiffSidebarManager may as well listen for changes
        propertyChangeSupport.firePropertyChange(EVENT_STATUS_CHANGED, null, files);
    }
    
    void flushFileOwnerCache() {
        synchronized(folderOwners) {
            folderOwners.clear();
        }
        synchronized(fileOwners) {
            fileOwners.clear();
        }
    }

    public void flushNullOwners() {
        synchronized(folderOwners) {
            flushNullOwners(folderOwners);
        }
        synchronized(fileOwners) {
            flushNullOwners(fileOwners);
        }
    }
    
    private void flushNullOwners(Map<VCSFileProxy, VersioningSystem> map) {
        Iterator<VCSFileProxy> it = map.keySet().iterator();
        while(it.hasNext()) {
            if(map.get(it.next()).equals(NULL_OWNER)) {
                it.remove();
            }
        }
    }

    VersioningSystem[] getVersioningSystems() {
        synchronized(versioningSystems) {
            return versioningSystems.toArray(new VersioningSystem[0]);
        }
    }

    /**
     * Determines versioning systems that manage files in given context.
     * 
     * @param ctx VCSContext to examine
     * @return VersioningSystem systems that manage this context or an empty array if the context is not versioned
     */
    VersioningSystem[] getOwners(VCSContext ctx) {
        Set<VCSFileProxy> files = ctx.getRootFiles();
        Set<VersioningSystem> owners = new HashSet<VersioningSystem>();
        for (VCSFileProxy file : files) {
            VersioningSystem vs = getOwner(file);
            if (vs != null) {
                owners.add(vs);
            }
        }
        return (VersioningSystem[]) owners.toArray(new VersioningSystem[0]);
    }

    /**
     * Determines the versioning system that manages given file.
     * Owner of a file:
     * - annotates its label in explorers, editor tab, etc.
     * - provides menu actions for it
     * - supplies "original" content of the file
     * 
     * Owner of a file may change over time (one common example is the Import command). In such case, the appropriate 
     * Versioning System is expected to fire the PROP_VERSIONED_ROOTS property change. 
     * 
     * @param file a file
     * @return VersioningSystem owner of the file or null if the file is not under version control
     */
    public VersioningSystem getOwner(VCSFileProxy file) {
        return getOwner(file, null);
    }
    
    /**
     * Determines the versioning system that manages given file.
     * Owner of a file:
     * - annotates its label in explorers, editor tab, etc.
     * - provides menu actions for it
     * - supplies "original" content of the file
     * 
     * Owner of a file may change over time (one common example is the Import command). In such case, the appropriate 
     * Versioning System is expected to fire the PROP_VERSIONED_ROOTS property change. 
     * 
     * @param file a file
     * @param isFile flag to avoid unnecessary disk access if information already available. Determines
     *        whether the given file is a file or directory.
     * @return VersioningSystem owner of the file or null if the file is not under version control
     */
    public VersioningSystem getOwner(VCSFileProxy file, Boolean isFile) {
        LOG.log(Level.FINE, "looking for owner of {0}", file);
                
        /**
         * minor speed optimization, file.isFile may last a while, so try to acquire
         * the owner from fileOwner or folderOwners directly before file.isFile call
         * otherwise the owner will be acquired after a file.isFile call
         */
        VersioningSystem owner = null;
        synchronized(fileOwners) {
            owner = fileOwners.get(file);
        }
        if(owner == null) {
            synchronized(folderOwners) {
                owner = folderOwners.get(file);
            }
        }
        if (owner != null) {
            if (owner == NULL_OWNER) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, " cached NULL_OWNER of {0}", new Object[] { file });
                }
                return null;
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, " cached owner {0} of {1}", new Object[] { owner.getClass().getName(), file });
            }
            return owner;
        }

        VCSFileProxy folder = file;
        
        if(isFile == null) {
            isFile = file.isFile();
        }
        
        if (isFile) {
            folder = file.getParentFile();
            if (folder == null) {
                LOG.log(Level.FINE, " null parent");
                return null;
            }
            synchronized(folderOwners) {
                owner = folderOwners.get(folder);
            }
        }

        if (owner == null && VersioningSupport.isExcluded(folder)) {
            // the owner is not known yet and the folder is excluded/unversioned
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, " caching NULL_OWNER of excluded {0}", new Object[] { file }); //NOI18N
            }
            if (isFile) {
                synchronized(fileOwners) {
                    fileOwners.put(folder, NULL_OWNER);
                }
            }
            synchronized(folderOwners) {
                folderOwners.put(folder, NULL_OWNER);
            }
            return null;
        } else if (owner != null) {
            synchronized(fileOwners) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, " caching owner {0} of {1}", new Object[] { owner != null ? owner.getClass().getName() : null, file }) ;
                }
                fileOwners.put(file, owner != null ? owner : NULL_OWNER);            
            }           
            if (owner == NULL_OWNER) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, " cached NULL_OWNER of {0}", new Object[] { folder });
                }
                return null;
            }            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, " cached owner {0} of {1}", new Object[] { owner.getClass().getName(), folder });
            }
            return owner;
        }        
        
        // no owner known yet - lets ask all registered VersioningSystem-s
        VCSFileProxy closestParent = null;

        VersioningSystem[] vs = getVersioningSystems();
        for (VersioningSystem system : vs) {
            if (system != localHistory) {    // currently, local history is never an owner of a file
                VCSFileProxy topmost = system.getTopmostManagedAncestor(folder);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, " {0} returns {1} ", new Object[] { system.getClass().getName(), topmost }) ;
                }
                if (topmost != null && (closestParent == null || Utils.isAncestorOrEqual(closestParent, topmost))) {
                    if (VersioningConfig.getDefault().isDisconnected(system, topmost)) {
                        // repository root is disconnected from this vcs
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, " skipping disconnected owner = {0} for {1}", new Object[] { 
                                system.getClass().getName(), topmost }) ;
                        }
                    } else {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, " owner = {0}", new Object[] { system.getClass().getName() }) ;
                        }
                        owner = system;
                        closestParent = topmost;
                    }
                }
            }
        }
                
        synchronized(folderOwners) {
            if (owner != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, " caching owner {0} of {1}", new Object[] { owner != null ? owner.getClass().getName() : null, folder }) ;
                }
                folderOwners.put(folder, owner);
            } else {
                // nobody owns the folder => all parents aren't owned
                while(folder != null) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, " caching unversioned folder {0}", new Object[] { folder }) ;
                    }
                    folderOwners.put(folder, NULL_OWNER);
                    folder = folder.getParentFile();
                }
            }
        }
        synchronized(fileOwners) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, " caching owner {0} of {1}", new Object[] { owner != null ? owner.getClass().getName() : null, file }) ;
            }
            fileOwners.put(file, owner != null ? owner : NULL_OWNER);            
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "owner = {0}", new Object[] { owner != null ? owner.getClass().getName() : null }) ;
        }
        return owner;
    }
    
    /**
     * Returns local history module that handles the given file.
     * 
     * @param file the file to examine
     * @return VersioningSystem local history versioning system or null if there is no local history for the file
     */
    public VersioningSystem getLocalHistory(VCSFileProxy file) {
        return getLocalHistory(file, null);
    }
    
    /**
     * Returns local history module that handles the given file.
     * 
     * @param file the file to examine
     * @param isFile flag to avoid unnecessary disk access if information already available. Determines
     *        whether the given file is a file or directory.
     * @return VersioningSystem local history versioning system or null if there is no local history for the file
     */
    public VersioningSystem getLocalHistory(VCSFileProxy file, Boolean isFile) {
                
        VersioningSystem lh = localHistory;
        if (lh == null) return null;

        File nbUserdir = Places.getUserDirectory();
        if (nbUserdir != null && !Utils.isVersionUserdir() 
                && Utils.isAncestorOrEqual(VCSFileProxy.createFileProxy(nbUserdir), file)) { 
            return null;
        }
        
        synchronized(localHistoryFiles) {
            Boolean isManagedByLocalHistory = localHistoryFiles.get(file);
            if (isManagedByLocalHistory != null && isManagedByLocalHistory) {
                return lh;
            }
        }
        VCSFileProxy folder = file;
        if(isFile == null) {
            isFile = file.isFile();
        }
        if (isFile) {
            folder = file.getParentFile();
            if (folder == null) return null;
        }

        synchronized(localHistoryFiles) {
            Boolean isManagedByLocalHistory = localHistoryFiles.get(folder);
            if (isManagedByLocalHistory != null) {
                return isManagedByLocalHistory ? lh : null;
            }
        }

        // ping and wake up LH, it does not have any metadata folders so the
        // default getTopmostManagedAncestor does not return true and does not
        // wake the instance. It needs to be waken by force.
        lh.getDelegate();
        boolean isManaged = lh.getTopmostManagedAncestor(folder) != null;
        if (isManaged) {
            putLocalHistoryFile(Boolean.TRUE, folder);
            return lh;
        } else {
            isManaged = lh.getTopmostManagedAncestor(file) != null;
            putLocalHistoryFile(isManaged, file);
            return isManaged ? lh : null;
        }        
    }

    private void putLocalHistoryFile(Boolean b, VCSFileProxy... files) {
        synchronized(localHistoryFiles) {
            if(localHistoryFiles.size() > 1500) {
                Iterator<VCSFileProxy> it = localHistoryFiles.keySet().iterator();
                for (int i = 0; i < 150; i++) {
                    it.next();
                    it.remove();
                }
            }
            for (VCSFileProxy file : files) {
                localHistoryFiles.put(file, b);
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refreshVersioningSystems(false);
    }
    
    /**
     * Versioning status or other parameter changed. 
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (EVENT_STATUS_CHANGED.equals(evt.getPropertyName())) {
            Set<VCSFileProxy> files = (Set<VCSFileProxy>) evt.getNewValue();
            VersioningAnnotationProvider.getDefault().refreshAnnotations(files);
            fireFileStatusChanged(files);
        } else if (EVENT_ANNOTATIONS_CHANGED.equals(evt.getPropertyName())) {
            Set<VCSFileProxy> files = (Set<VCSFileProxy>) evt.getNewValue();
            VersioningAnnotationProvider.getDefault().refreshAnnotations(files);
        } else if (EVENT_VERSIONED_ROOTS.equals(evt.getPropertyName())) {
            if(localHistory != null && evt.getSource() == localHistory.getDelegate()) {
                synchronized(localHistoryFiles) {
                    localHistoryFiles.clear();
                }
            } else {
                versionedRootsChanged(null);
            }
            propertyChangeSupport.firePropertyChange(EVENT_VERSIONED_ROOTS, null, null);
        }
    }

    public void versionedRootsChanged() {
        versionedRootsChanged(null);
        propertyChangeSupport.firePropertyChange(EVENT_VERSIONED_ROOTS, null, null);
    }
    
    private void versionedRootsChanged(VersioningSystem owner) {
        flushCachedContext();
        flushFileOwnerCache();
        fireFileStatusChanged(null);
        refreshAllAnnotations();
    }
    
    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        refreshAllAnnotations();
    }

    /**
     * Determines if the given methodName is implemented by local histories {@link VCSInterceptor}
     *
     * @param methodName
     * @return <code>true</code> if the given methodName is implemented by local histories {@link VCSInterceptor}
     * otherwise <code>false</code>
     */
    public boolean needsLocalHistory(String methodName) {
        boolean ret = false;
        try {
            synchronized(versioningSystems) {
                if(localHistory == null) {
                    return ret;
                }
                Set<String> s = interceptedMethods.get(localHistory.getClass().getName());
                if(s == null) {
                    s = new HashSet<String>();
                    Method[] m = localHistory.getVCSInterceptor().getClass().getDeclaredMethods();
                    for (Method method : m) {
                        if((method.getModifiers() & Modifier.PUBLIC) != 0) {
                            s.add(method.getName());
                        }
                    }
                    interceptedMethods.put(localHistory.getClass().getName(), s);
                }
                ret = s.contains(methodName);
                return ret;
            }
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "needsLocalHistory method [{0}] returns {1}", new Object[] {methodName, ret});
            }
        }
    }

    public static synchronized void statusListener(VCSAnnotationListener listener, boolean add) {
        WeakSet<VCSAnnotationListener> newSet = new WeakSet<VCSAnnotationListener>(statusListeners);
        if (add) {
            newSet.add(listener);
        } else {
            newSet.remove(listener);
        }
        statusListeners = newSet;
    }

    private static void refreshAllAnnotations() {
        VersioningAnnotationProvider.refreshAllAnnotations();
    }

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    boolean isLocalHistory(VersioningSystem system) {
        return system == localHistory;
    }

    private void flushCachedContext () {
        SPIAccessor.IMPL.flushCachedContext();
    }
}
