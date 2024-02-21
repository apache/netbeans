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

package org.netbeans.modules.java.j2seplatform.libraries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 * Finds the locations of sources for various libraries.
 * @author Tomas Zezula
 */
@ServiceProvider(service=SourceForBinaryQueryImplementation.class, position=150)
public class J2SELibrarySourceForBinaryQuery implements SourceForBinaryQueryImplementation2, PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger (J2SELibrarySourceForBinaryQuery.class.getName ());
    private final Map<URL,SourceForBinaryQueryImplementation2.Result> cache = new ConcurrentHashMap<>();
    private final Map<URL,URL> normalizedURLCache = new ConcurrentHashMap<>();
    private final AtomicBoolean lmListens = new AtomicBoolean();
    //@GuardedBy("this")
    private Iterable<? extends LibraryManager> lmCache;
    //@GuardedBy("this")
    private int lmCacheEvntCnt;

    /** Default constructor for lookup. */
    public J2SELibrarySourceForBinaryQuery() {}

    @Override
    public SourceForBinaryQueryImplementation2.Result findSourceRoots2 (URL binaryRoot) {
        SourceForBinaryQueryImplementation2.Result res = cache.get(binaryRoot);
        if (res != null) {
            return res;
        }
        try {
            boolean isNormalizedURL = isNormalizedURL (binaryRoot);
            for (LibraryManager mgr : getLibraryManagers()) {
                for (Library lib : mgr.getLibraries()) {
                    if (lib.getType().equals(J2SELibraryTypeProvider.LIBRARY_TYPE)) {
                        for (URL entry : lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH)) {
                            URL normalizedEntry = entry;
                            if (isNormalizedURL) {
                                try {
                                    normalizedEntry = getNormalizedURL (normalizedEntry);
                                } catch (MalformedURLException ex) {
                                    LOG.log (Level.INFO, "Invalid URL: " + normalizedEntry, ex);
                                    return null;
                                }
                            }
                            if (binaryRoot.equals(normalizedEntry)) {
                                res = new Result(entry, mgr, lib);
                                cache.put(binaryRoot, res);
                                return res;
                            }
                        }
                    }
                }
            }
        } catch (MalformedURLException | URISyntaxException ex) {
            LOG.log (Level.INFO, "Invalid URL: " + binaryRoot, ex);
            //cache.put (binaryRoot, null);
        }
        return null;
    }
    
    
    @Override
    public SourceForBinaryQuery.Result findSourceRoots (final URL binaryRoot) {
        return this.findSourceRoots2(binaryRoot);
    }
    
    public void preInit() {
        for (final LibraryManager lm : getLibraryManagers()) {
            for (final Library lib : lm.getLibraries()) {
                if (J2SELibraryTypeProvider.LIBRARY_TYPE.equals(lib.getType())) {
                    for (final URL url : lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH)) {
                        try {
                            getNormalizedURL (url);
                        } catch (MalformedURLException ex) {
                            LOG.log (Level.INFO, "Invalid URL: " + url, ex);
                        }
                    }
                }
            }
        }
    }
    
    private URL getNormalizedURL (URL url) throws MalformedURLException {
        //URL is already nornalized, return it
        if (isNormalizedURL(url)) {
            return url;
        }
        //Todo: Should listen on the LibrariesManager and cleanup cache        
        // in this case the search can use the cache onle and can be faster 
        // from O(n) to O(ln(n))
        URL normalizedURL = normalizedURLCache.get(url);
        if (normalizedURL == null) {
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                normalizedURL = fo.toURL();
                this.normalizedURLCache.put (url, normalizedURL);
            }
        }
        return normalizedURL;
    }
    
    /**
     * Returns true if the given URL is file based, it is already
     * resolved either into file URL or jar URL with file path.
     * @param URL url
     * @return true if  the URL is normal
     */
    private static boolean isNormalizedURL (URL url) throws MalformedURLException {
        if ("jar".equals(url.getProtocol())) { //NOI18N
            String path = url.getPath();
            int index = path.indexOf("!/"); //NOI18N
            if (index < 0)
                throw new MalformedURLException ();
            String jarPath = path.substring (0, index);
            if (
                jarPath.indexOf ("file://") > -1 &&         //NOI18N
                jarPath.indexOf("file:////") == -1          //NOI18N
            ) {
                /* Replace because JDK application classloader wrongly recognizes UNC paths. */
                jarPath = jarPath.replaceFirst ("file://", "file:////");  //NOI18N
            }
            url = new   URL(jarPath);
        }
        return "file".equals(url.getProtocol());    //NOI18N
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (LibraryManager.PROP_OPEN_LIBRARY_MANAGERS.equals(evt.getPropertyName())) {
            synchronized (this) {
                lmCache = null;
                lmCacheEvntCnt++;
            }
        }
    }

    private static class Result implements SourceForBinaryQueryImplementation2.Result, PropertyChangeListener {

        private final LibraryManager manager;        
        private final URI entry;
        private final ChangeSupport cs = new ChangeSupport(this);
        private FileObject[] cache;
        private volatile Library lib;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public Result (
                @NonNull final URL queryFor,
                @NonNull final LibraryManager manager,
                @NonNull final Library lib) throws URISyntaxException {
            this.entry = queryFor.toURI();
            this.manager = manager;
            this.lib = lib;
            manager.addPropertyChangeListener(WeakListeners.propertyChange(this, manager));
            lib.addPropertyChangeListener(WeakListeners.propertyChange(this, lib));
        }
        
        @Override
        public synchronized FileObject[] getRoots () {
            if (this.cache == null) {
                // entry is not resolved so directly volume content can be searched for it:
                final Library _lib = this.lib;
                if (getResolvedURIContent(_lib, manager, J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH).contains(entry)) {
                    List<FileObject> result = new ArrayList<FileObject>();
                    for (URL u : _lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_SRC)) {
                        FileObject sourceRoot = URLMapper.findFileObject(u);
                        if (sourceRoot!=null) {
                            result.add (sourceRoot);
                        }
                    }
                    this.cache = result.toArray(new FileObject[0]);
                }
                else {
                    this.cache = new FileObject[0];
                }
            }
            return this.cache;
        }
        
        @Override
        public synchronized void addChangeListener (ChangeListener l) {
            assert l != null : "Listener cannot be null"; // NOI18N
            cs.addChangeListener(l);
        }
        
        @Override
        public synchronized void removeChangeListener (ChangeListener l) {
            assert l != null : "Listener cannot be null"; // NOI18N
            cs.removeChangeListener(l);
        }
        
        @Override
        public void propertyChange (PropertyChangeEvent event) {
            final String propName = event.getPropertyName();
            if (Library.PROP_CONTENT.equals(propName)) {
                synchronized (this) {                    
                    this.cache = null;
                }
                cs.fireChange();
            } else if (LibraryManager.PROP_LIBRARIES.equals(propName) && manager.equals(event.getSource())) {
                final Library currentLib = lib;
                final Library newLib = manager.getLibrary(currentLib.getName());
                final boolean change;
                if (newLib == null) {
                    change = true;
                } else if (newLib == currentLib) {
                    change = false;
                } else {
                    final List<? extends URI> newBin = newLib.getURIContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
                    if (newBin == null || !newBin.contains(entry)) {
                        change = true;
                    } else {
                        final List<? extends URI> newSrc = newLib.getURIContent(J2SELibraryTypeProvider.VOLUME_TYPE_SRC);
                        change = newSrc == null || !newSrc.equals(currentLib.getURIContent(J2SELibraryTypeProvider.VOLUME_TYPE_SRC));
                    }
                }
                if (change) {
                    LOG.log(
                        Level.FINE,
                        "Library {0} redefined.",   //NOI18N
                        currentLib.getName());
                    boolean fire = false;
                    synchronized (this) {
                        if (newLib != null) {
                            lib = newLib;
                            fire = true;
                        }
                        this.cache = null;
                    }
                    if (fire) {
                        cs.fireChange();
                    }
                }
            }
        }

        @Override
        public boolean preferSources() {
            return false;
        }

        private static List<? extends URI> getResolvedURIContent(
            @NonNull final Library lib,
            @NonNull final LibraryManager manager,
            @NonNull final String type) {
            final List<? extends URI> content = lib.getURIContent(type);
            final URL location = manager.getLocation();
            if (location == null) {
                return content;
            } else {
                final List<URI> res = new ArrayList<>(content.size());
                for (URI toResolve : content) {
                    final URI resolved = LibrariesSupport.resolveLibraryEntryURI(location, toResolve);
                    if (resolved != null) {
                        res.add(resolved);
                    } else {
                        LOG.log(
                            Level.WARNING,
                            "Cannot resolve: {0} in: {1}",  //NOI18N
                            new Object[]{
                                toResolve,
                                location
                            });
                    }
                }
                return res;
            }
        }
    }

    @NonNull
    private Iterable<? extends LibraryManager> getLibraryManagers() {
        Iterable<? extends LibraryManager> res;
        final int current;
        synchronized (this) {
            res = lmCache;
            current = lmCacheEvntCnt;
        }
        if (res == null) {
            if (lmListens.compareAndSet(false,true)) {
                LibraryManager.addOpenManagersPropertyChangeListener(this);
            }
            res = LibraryManager.getOpenManagers();
            synchronized (this) {
                if (current == lmCacheEvntCnt) {
                    lmCache = res;
                } else if (lmCache != null) {
                    res = lmCache;
                }
            }
        }
        return res;
    }

    @CheckForNull
    public static J2SELibrarySourceForBinaryQuery getInstance() {
        J2SELibrarySourceForBinaryQuery result = Lookup.getDefault().lookup(J2SELibrarySourceForBinaryQuery.class);
        if (result == null) {
            for (SourceForBinaryQueryImplementation impl : Lookup.getDefault().lookupAll(SourceForBinaryQueryImplementation.class)) {
                if (J2SELibrarySourceForBinaryQuery.class == impl.getClass()) {
                    result = (J2SELibrarySourceForBinaryQuery)impl;
                    break;
                }
            }
        }
        return result;
    }
}
