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

package org.netbeans.api.java.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.classpath.SPIAccessor;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.GlobalPathRegistryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Maintains a global registry of "interesting" classpaths of various kinds.
 * You may add and remove different kinds of {@link ClassPath}s to the registry
 * and listen to changes in them.
 * <p>
 * It is permitted to register the same classpath more than once; unregistration
 * keeps track of the number of registrations so that the operation is symmetric.
 * However {@link #getPaths} only ever returns one copy of the classpath, and
 * listeners are only notified the first time a given classpath is added to the
 * registry, or the last time it is removed.
 * ({@link ClassPath} equality is based on equality of the {@link ClassPathImplementation}
 * which by default is based on object identity, so there could be multiple paths
 * returned that at the time share the same list of roots. There may also be
 * several paths which contain some shared roots.)
 * </p>
 * <p>
 * The registry is not persisted between JVM sessions.
 * </p>
 * <div class="nonnormative">
 * <p>
 * Intended usage patterns:
 * </p>
 * <ol>
 * <li><p>When a project is opened using
 * <a href="@org-netbeans-modules-projectuiapi-base@/org/netbeans/spi/project/ui/ProjectOpenedHook.html">ProjectOpenedHook</a> it should add any paths
 * it defines, i.e. paths it might return from a
 * {@link org.netbeans.spi.java.classpath.ClassPathProvider}.
 * When closed it should remove them.</p></li>
 * <li><p>The <b>Fast&nbsp;Open</b> feature of the editor and other features which
 * require a global list of relevant sources should use {@link #getSourceRoots} or
 * the equivalent.</p></li>
 * <li><p>The <b>Javadoc&nbsp;Index&nbsp;Search</b> feature and <b>View&nbsp;&#8594;
 * Documentation&nbsp;Indices</b> submenu should operate on open Javadoc paths,
 * meaning that Javadoc corresponding to registered compile and boot classpaths
 * (according to <a href="@org-netbeans-api-java@/org/netbeans/api/java/queries/JavadocForBinaryQuery.html">JavadocForBinaryQuery</a>).</p></li>
 * <li><p>Stack trace hyperlinking can use the global list of source paths
 * to find sources, in case no more specific information about their origin is
 * available. The same would be true of debugging: if the debugger cannot find
 * Java-like sources using more precise means ({@link SourceForBinaryQuery}), it
 * can use {@link #findResource} as a fallback.</p></li>
 * </ol>
 * </div>
 * @author Jesse Glick
 * @since org.netbeans.api.java/1 1.4
 */
public final class GlobalPathRegistry {

    private static final Logger LOG = Logger.getLogger(GlobalPathRegistry.class.getName());

    //@GuardedBy("instances");
    private static final Map<GlobalPathRegistryImplementation,Reference<GlobalPathRegistry>> instances =
            new WeakHashMap<>();
    
    /**
     * Get the singleton instance of the registry.
     * <div class="nonnormative">
     * <p>
     * In environments with scoped global lookup don't cache
     * an instance of the {@link GlobalPathRegistry} but rather
     * call the {@link GlobalPathRegistry#getDefault} when the
     * instance is needed.
     * </p>
     * </div>
     * @return the default instance
     */
    @NonNull
    public static GlobalPathRegistry getDefault() {
        final GlobalPathRegistryImplementation spi = Lookup.getDefault().lookup(GlobalPathRegistryImplementation.class);
        if (spi == null) {
            throw new IllegalStateException("No GlobalPathRegistryImplementation found in the lookup"); //NOI18N
        }
        synchronized (instances) {
            final Reference<GlobalPathRegistry> apiRef = instances.get(spi);
            GlobalPathRegistry api;
            if (apiRef == null || (api = apiRef.get()) == null) {
                api = new GlobalPathRegistry(spi);
                SPIAccessor.getInstance().attachAPI(spi, api);
                instances.put(spi, new WeakReference<>(api));
            }
            return api;
        }
    }

    private final GlobalPathRegistryImplementation spi;
    private int resetCount;
    private final List<GlobalPathRegistryListener> listeners = new ArrayList<GlobalPathRegistryListener>();
    private Set<FileObject> sourceRoots = null;
    private Set<SourceForBinaryQuery.Result> results = new HashSet<SourceForBinaryQuery.Result>();
    
    
    private final ChangeListener resultListener = new SFBQListener ();
    
    private PropertyChangeListener classpathListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (GlobalPathRegistry.this) {
                //Reset cache
                GlobalPathRegistry.this.resetSourceRootsCache ();
            }
        }
    };
    
    private GlobalPathRegistry(@NonNull GlobalPathRegistryImplementation spi) {
        Parameters.notNull("spi", spi); //NOI18N
        this.spi = spi;
    }
    
    /** for use from unit test */
    void clear() {
        SPIAccessor.getInstance().clear(spi);
        listeners.clear();
        sourceRoots = null;
    }
    
    /**
     * Find all paths of a certain type.
     * @param id a classpath type, e.g. {@link ClassPath#SOURCE}
     * @return an immutable set of all registered {@link ClassPath}s of that type (may be empty but not null)
     */
    @NonNull
    public Set<ClassPath> getPaths(@NonNull final String id) {
        Parameters.notNull("id", id);   //NOI18N
        synchronized (this) {
            return SPIAccessor.getInstance().getPaths(spi, id);
        }
    }

    /**
     * Register some classpaths of a certain type.
     * @param id a classpath type, e.g. {@link ClassPath#SOURCE}
     * @param paths a list of classpaths to add to the registry
     */
    public void register(@NonNull final String id, @NonNull final ClassPath[] paths) {
        Parameters.notNull("id", id);       //NOI18N
        Parameters.notNull("paths", paths); //NOI18N
        // Do not log just when firing an event, since there may no listeners.
        LOG.log(Level.FINE, "registering paths {0} of type {1}", new Object[] {Arrays.asList(paths), id});
        Set<ClassPath> added;
        GlobalPathRegistryListener[] _listeners = null;
        synchronized (this) {
            added = SPIAccessor.getInstance().register(spi,id, paths);
            for (ClassPath path : added) {
                path.addPropertyChangeListener(classpathListener);
            }
            // Invalidate cache for getSourceRoots and findResource:
            resetSourceRootsCache ();
            if (LOG.isLoggable(Level.FINER)) {
                final Set<ClassPath> l = SPIAccessor.getInstance().getPaths(spi, id);
                LOG.log(Level.FINER, "now have {0} paths of type {1}", new Object[] {l.size(), id});
            }
            if (!listeners.isEmpty() && !added.isEmpty()) {
                _listeners = listeners.toArray(new GlobalPathRegistryListener[0]);
            }
        }
        if (_listeners != null) {
            for (GlobalPathRegistryListener listener : _listeners) {
                listener.pathsAdded(new GlobalPathRegistryEvent(this, id, Collections.unmodifiableSet(added)));
            }
        }
    }
    
    /**
     * Unregister some classpaths of a certain type.
     * @param id a classpath type, e.g. {@link ClassPath#SOURCE}
     * @param paths a list of classpaths to remove from the registry
     * @throws IllegalArgumentException if they had not been registered before
     */
    public void unregister(@NonNull final String id, @NonNull final ClassPath[] paths) throws IllegalArgumentException {
        Parameters.notNull("id", id);   //NOI18N
        Parameters.notNull("paths", paths); //NOI18N
        LOG.log(Level.FINE, "unregistering paths {0} of type {1}", new Object[] {Arrays.asList(paths), id});
        Set<ClassPath> removed;
        GlobalPathRegistryListener[] _listeners = null;
        synchronized (this) {
            removed = SPIAccessor.getInstance().unregister(spi,id, paths);
            for (ClassPath path : removed) {
                path.removePropertyChangeListener(classpathListener);
            }
            resetSourceRootsCache ();
            if (LOG.isLoggable(Level.FINER)) {
                final Set<ClassPath> l = SPIAccessor.getInstance().getPaths(spi, id);
                LOG.log(Level.FINER, "now have {0} paths of type {1}", new Object[] {l.size(), id});
            }
            if (!listeners.isEmpty() && !removed.isEmpty()) {
                _listeners = listeners.toArray(new GlobalPathRegistryListener[0]);
            }
        }
        if (_listeners != null) {
            for (GlobalPathRegistryListener listener : _listeners) {
                listener.pathsRemoved(new GlobalPathRegistryEvent(this, id, Collections.unmodifiableSet(removed)));
            }
        }
    }
    
    /**
     * Add a listener to the registry.
     * @param l a listener to add
     */
    public synchronized void addGlobalPathRegistryListener(GlobalPathRegistryListener l) {
        if (l == null) {
            throw new NullPointerException();
        }
        listeners.add(l);
    }
    
    /**
     * Remove a listener to the registry.
     * @param l a listener to remove
     */
    public synchronized void removeGlobalPathRegistryListener(GlobalPathRegistryListener l) {
        if (l == null) {
            throw new NullPointerException();
        }
        listeners.remove(l);
    }
    
    /**
     * Convenience method to find all relevant source roots.
     * This consists of:
     * <ol>
     * <li>Roots of all registered {@link ClassPath#SOURCE} paths.
     * <li>Sources (according to {@link SourceForBinaryQuery}) of all registered
     *     {@link ClassPath#COMPILE} paths.
     * <li>Sources of all registered {@link ClassPath#BOOT} paths.
     * </ol>
     * Order is not significant.
     * <p>
     * Currently there is no reliable way to listen for changes in the
     * value of this method: while you can listen to changes in the paths
     * mentioned, it is possible for {@link SourceForBinaryQuery} results to
     * change. In the future a change listener might be added for the value
     * of the source roots.
     * </p>
     * <p>
     * Note that this method takes no account of package includes/excludes.
     * </p>
     * @return an immutable set of <code>FileObject</code> source roots
     */
    public Set<FileObject> getSourceRoots() {        
        int currentResetCount;
        Set<ClassPath> sourcePaths, compileAndBootPaths;
        synchronized (this) {
            if (this.sourceRoots != null) {
                return this.sourceRoots;
            }            
            currentResetCount = this.resetCount;
            sourcePaths = getPaths(ClassPath.SOURCE);
            compileAndBootPaths = new LinkedHashSet<ClassPath>(getPaths(ClassPath.COMPILE));
            compileAndBootPaths.addAll(getPaths(ClassPath.BOOT));
        }
        
        Set<FileObject> newSourceRoots = new LinkedHashSet<FileObject>();
        for (ClassPath sp : sourcePaths) {
            newSourceRoots.addAll(Arrays.asList(sp.getRoots()));
        }
        
        final List<SourceForBinaryQuery.Result> newResults = new LinkedList<SourceForBinaryQuery.Result> ();
        final Set<String> seenEntryURL = new HashSet<>();
        final ChangeListener tmpResultListener = new SFBQListener ();
        for (ClassPath cp : compileAndBootPaths) {
            for (ClassPath.Entry entry : cp.entries()) {
                URL url = entry.getURL();
                String urlKey = url.toString();
                if (!seenEntryURL.add(urlKey)) {
                    //we have already processed this binary root, skip
                    continue;
                }
                SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(url);
                result.addChangeListener(tmpResultListener);
                newResults.add (result);
                FileObject[] someRoots = result.getRoots();
                newSourceRoots.addAll(Arrays.asList(someRoots));
            }
        }
        
        newSourceRoots = Collections.unmodifiableSet(newSourceRoots);        
        synchronized (this) {
            if (this.resetCount == currentResetCount) {
                this.sourceRoots = newSourceRoots;
                removeTmpSFBQListeners (newResults, tmpResultListener, true);
                this.results.addAll (newResults);
            }
            else {
                removeTmpSFBQListeners (newResults, tmpResultListener, false);
            }
            return newSourceRoots;
        }        
    }
    
    
    private void removeTmpSFBQListeners (List<? extends SourceForBinaryQuery.Result> results, ChangeListener listener, boolean addListener) {
        for (SourceForBinaryQuery.Result res : results) {
            if (addListener) {
                res.addChangeListener (this.resultListener);
            }
            res.removeChangeListener(listener);
        }
    }
    
    /**
     * Convenience method to find a particular source file by resource path.
     * This simply uses {@link #getSourceRoots} to find possible roots and
     * looks up the resource among them.
     * In case more than one source root contains the resource, one is chosen
     * arbitrarily.
     * As with {@link ClassPath#findResource}, include/exclude lists can affect the result.
     * @param resource a resource path, e.g. <code>somepkg/Foo.java</code>
     * @return some file found with that path, or null
     */
    public FileObject findResource(String resource) {
        for (ClassPath cp : getPaths(ClassPath.SOURCE)) {
            FileObject f = cp.findResource(resource);
            if (f != null) {
                return f;
            }
        }
        for (FileObject root : getSourceRoots()) {
            FileObject f = root.getFileObject(resource);
            if (f != null) {
                // Make sure it is not from one of the above, since they control incl/excl.
                for (ClassPath cp : getPaths(ClassPath.SOURCE)) {
                    if (cp.findOwnerRoot(f) != null) {
                        return null;
                    }
                }
                return f;
            }
        }
        return null;
    }
    
    
    private synchronized void resetSourceRootsCache () {
        this.sourceRoots = null;
        for (Iterator< ? extends SourceForBinaryQuery.Result>  it = results.iterator(); it.hasNext();) {
            SourceForBinaryQuery.Result result = it.next();
            it.remove();
            result.removeChangeListener(this.resultListener);
        }
        this.resetCount++;
    }
    
    private class SFBQListener implements ChangeListener {
        
        public void stateChanged (ChangeEvent event) {
            synchronized (GlobalPathRegistry.this) {
                //Reset cache
                GlobalPathRegistry.this.resetSourceRootsCache ();
            }
        }
    };
    
    /**
     * Testability
     * Used by unit GlobalPathRegistryTest
     * @return set of {@link SourceForBinaryQuery.Result} the {@link GlobalPathRegistry}
     * listens on.
     */
    Set<? extends SourceForBinaryQuery.Result> getResults () {
        return Collections.unmodifiableSet(this.results);
    }

}
