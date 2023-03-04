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

package org.netbeans.modules.java.source.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.api.annotations.common.NonNull;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.ClassIndexManagerEvent;
import org.netbeans.modules.java.source.usages.ClassIndexManagerListener;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public class CacheClassPath implements ClassPathImplementation, PropertyChangeListener, ClassIndexManagerListener {
    
    public static final boolean KEEP_JARS = Boolean.getBoolean("CacheClassPath.keepJars");     //NOI18N
    private static final Logger LOG = Logger.getLogger(CacheClassPath.class.getName());
    
    private final ClassPath cp;
    private final boolean translate;
    private final boolean scan;
    private final PropertyChangeSupport listeners;
    //@GuardedBy("this")
    private List<PathResourceImplementation> cache;
    //@GuardedBy("this")
    private Set<URL> expectedSourceRoots;
    //@GuardedBy("this")
    private long eventId;

    /** Creates a new instance of CacheClassPath */
    @SuppressWarnings("LeakingThisInConstructor")
    private CacheClassPath (ClassPath cp, boolean translate, boolean scan) {
        this.listeners = new PropertyChangeSupport (this);
        this.cp = cp;
        this.translate = translate;
        this.scan = scan;
        if (!scan) {
            this.cp.addPropertyChangeListener (WeakListeners.propertyChange(this,cp));
            final ClassIndexManager cim = ClassIndexManager.getDefault();
            cim.addClassIndexManagerListener(WeakListeners.create(ClassIndexManagerListener.class, this, cim));
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.cp);
        hash = 53 * hash + (this.translate ? 1 : 0);
        hash = 53 * hash + (this.scan ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CacheClassPath other = (CacheClassPath) obj;
        if (this.translate != other.translate) {
            return false;
        }
        if (this.scan != other.scan) {
            return false;
        }
        if (!Objects.equals(this.cp, other.cp)) {
            return false;
        }
        return true;
    }
    
    @Override
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        this.listeners.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        this.listeners.addPropertyChangeListener(listener);
    }
    
    @Override
    public void propertyChange (final PropertyChangeEvent event) {
        if (ClassPath.PROP_ENTRIES.equals(event.getPropertyName())) {
            synchronized (this) {
                this.cache = null;
                this.eventId++;
            }
            this.listeners.firePropertyChange(PROP_RESOURCES,null,null);
        }
    }

    @Override
    public void classIndexAdded(ClassIndexManagerEvent event) {
        final Set<? extends URL> added = event.getRoots();
        boolean fire = false;
        synchronized (this) {
            if (expectedSourceRoots != null) {
                for (URL ar : added) {
                    if (expectedSourceRoots.contains(ar)) {
                        this.cache = null;
                        this.eventId++;
                        fire = true;
                        break;
                    }
                }
            }
        }
        if (fire) {
            this.listeners.firePropertyChange(PROP_RESOURCES,null,null);
        }
    }

    @Override
    public void classIndexRemoved(@NonNull final ClassIndexManagerEvent event) {
        //Pass: Not needed, handled by ClassPathChanges
    }

    @Override
    public List<? extends PathResourceImplementation> getResources() {
        long currentEventId;
        synchronized (this) {
            if (this.cache!= null) {
                return this.cache;
            }
            currentEventId = this.eventId;
        }
        final List<ClassPath.Entry> entries = this.cp.entries();
        final Set<PathResourceImplementation> _cache = new LinkedHashSet<PathResourceImplementation> ();
        final PathRegistry preg = PathRegistry.getDefault();
        final Set<URL> unInitializedSourceRoots = new HashSet<URL>();
        for (ClassPath.Entry entry : entries) {
            URL url = entry.getURL();
            URL[] sourceUrls;
            if (translate) {
                sourceUrls = preg.sourceForBinaryQuery(url, this.cp, true);
            }
            else {
                sourceUrls = new URL[] {url};
            }
            if (sourceUrls != null) {
                for (URL sourceUrl : sourceUrls) {
                    if (scan || JavaIndex.hasSourceCache(sourceUrl, false)) {
                        try {
                            File cacheFolder = JavaIndex.getClassFolder(sourceUrl);
                            URL cacheUrl = FileUtil.urlForArchiveOrDir(cacheFolder);
                            _cache.add(ClassPathSupport.createResource(cacheUrl));
                        } catch (IOException ioe) {
                            if (LOG.isLoggable(Level.SEVERE))
                                LOG.log(Level.SEVERE, ioe.getMessage(), ioe);
                        }
                    } else {
                        unInitializedSourceRoots.add(sourceUrl);
                    }
                }
                if (KEEP_JARS && translate) {
                    _cache.add(ClassPathSupport.createResource(url));
                }
            } else {
                if (FileObjects.JAR.equals(url.getProtocol())) {
                    URL foo = FileUtil.getArchiveFile(url);
                    if (!FileObjects.FILE.equals(foo.getProtocol())) {
                        FileObject fo = URLMapper.findFileObject(foo);
                        if (fo != null) {
                            foo = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                            if (FileObjects.FILE.equals(foo.getProtocol())) {
                                url = FileUtil.getArchiveRoot(foo);
                            }
                        }
                    }
                }
                else if (!FileObjects.FILE.equals(url.getProtocol())) {
                    FileObject fo = URLMapper.findFileObject(url);
                    if (fo != null) {
                        URL foo = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                        if (foo != null && FileObjects.FILE.equals(foo.getProtocol())) {
                            url = foo;
                        }
                    }
                }
                _cache.add(new CachingPathResourceImpl(url,scan));
                _cache.add (ClassPathSupport.createResource(url));
            }
        }
        List<? extends PathResourceImplementation> res;
        synchronized (this) {
            if (currentEventId == this.eventId) {
                this.cache = new ArrayList<PathResourceImplementation>(_cache);
                expectedSourceRoots = unInitializedSourceRoots.isEmpty() ? null : Collections.unmodifiableSet(unInitializedSourceRoots);
                res = this.cache;
            }
            else {
                res = new ArrayList<PathResourceImplementation>(_cache);
            }
        }
        assert res != null;
        return res;
    }
    
    
    public static ClassPath forClassPath (final ClassPath cp, final boolean ru) {
        assert cp != null;
        return ClassPathFactory.createClassPath(new CacheClassPath(cp,true,ru));
    }
    
    public static ClassPath forBootPath (final ClassPath cp, final boolean ru) {
        assert cp != null;
        return ClassPathFactory.createClassPath(new CacheClassPath(cp,true, ru));
    }
    
    public static ClassPath forSourcePath (final ClassPath sourcePath, final boolean ru) {
        assert sourcePath != null;
        return ClassPathFactory.createClassPath(new CacheClassPath(sourcePath,false, ru));
    }

    private static final class CachingPathResourceImpl implements PathResourceImplementation {
        private static final URL[] EMPTY = new URL[0];
        
        private final URL   originalRoot;
        private final boolean scan;
        private       URL[] cacheRoot;

        public CachingPathResourceImpl(
                @NonNull final URL originalRoot,
                final boolean scan) {
            this.originalRoot = originalRoot;
            this.scan = scan;
        }

        @Override public synchronized URL[] getRoots() {
            URL[] result = cacheRoot;

            if (result == null) {
                result = EMPTY;
                try {
                    File sigs = JavaIndex.getClassFolder(originalRoot,false,false);
                    URL orl = FileUtil.urlForArchiveOrDir(sigs);
                    if (orl != null) {
                        result = new URL[] {orl};
                    }
                    else {
                        LOG.log(Level.WARNING, "Invalid cache root: {0} exists: {1} dir: {2} retry: {3}", new Object[]{sigs.getAbsolutePath(), sigs.exists(), sigs.isDirectory(), FileUtil.urlForArchiveOrDir(sigs)});  //NOI18N
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }

                cacheRoot = result;
            }

            assert result != null;
            
            return result;
        }

        @Override public ClassPathImplementation getContent() {
            return null;
        }

        @Override public void addPropertyChangeListener(PropertyChangeListener listener) {}

        @Override public void removePropertyChangeListener(PropertyChangeListener listener) {}

    }
}
