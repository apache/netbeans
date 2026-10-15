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
package org.netbeans.modules.groovy.editor.compiler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 * This factory produces shared ClassLoaders that load visitors into the Groovy
 * compiler used by IDE runtime. Visitors are searched for and added to the compiler
 * during each parsing request. Unlike class cache, these visitors are typically valid
 * until project configuration changes, or the library which supplies the visitor is
 * recompiled, updated or replaced. 
 * <p>
 * This class serves as a cache and a factory at the same time. It is registered 
 * <b>for a project</b> so that it is evicted from the memory eventually, after the 
 * project closes and references are gone. It is <b>also registered</b> in global Lookup
 * for files that do not belong to any project. Such files can luckily just default JDK(?)
 * and no libraries, so only limited number of ClassLoaders will be here (e.g. when the
 * default JDK switches).
 * <p>
 * @author sdedic
 */
@ServiceProvider(service = ClassLoaderFactory.class)
public class ClassLoaderFactory {
    private static final Logger LOG = Logger.getLogger(ClassLoaderFactory.class.getName());
    
    /**
     * Maximum cached loaders.
     */
    private static final int MAX_LOADER_CACHE = 20;

    /**
     * Name derived from the project's directory. Just for diagnostic purposes.
     */
    private final String name;
    
    /**
     * Map of loaders, keyed by list of classpaths. The classpaths have some identity, so the
     * loader is bound to that combined identity. If the combined classpath root changes, the
     * loader removes itself from the cache.
     */
    private Map<List<ClassPath>, Reference<CachedClassLoader>> loaderMap = new LinkedHashMap<List<ClassPath>, Reference<CachedClassLoader>>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<List<ClassPath>, Reference<CachedClassLoader>> eldest) {
            if (size() >= MAX_LOADER_CACHE) {
                CachedClassLoader l = eldest.getValue().get();
                if (l != null) {
                    l.detach();
                }
                return true;
            } else {
                return false;
            }
        }
    };
    
    /**
     * Constructor for global cache.
     */
    public ClassLoaderFactory() {
        this("<global>"); // NOI18N
    }
    
    /**
     * Constructor for per-project cache; name should be derived from project's directory name.
     * @param n cache name
     */
    private ClassLoaderFactory(String n) {
        name = n; 
    }

    @ProjectServiceProvider(projectType = {
        "org-netbeans-modules-gradle",
        "org-netbeans-modules-maven"
        }, service = ClassLoaderFactory.class
    )
    public static ClassLoaderFactory forProject(Project p) {
        return new ClassLoaderFactory(p.getProjectDirectory().getPath().toString());
    }
    
    /**
     * Creates a classloader. The ClassPaths passed in serve as a key: they should not be constructed on the fly
     * but extracted from the project. 
     * @param config compiler configuration
     * @param paths classpaths, possibly empty array
     * @return transformation class loader
     */
    public @NonNull ClassLoader createClassLoader(CompilerConfiguration config, ClassPath... paths) {
        CachedClassLoader l = null;
        synchronized (loaderMap) {
            List<ClassPath> p = Arrays.asList(paths);
            
            Reference<CachedClassLoader> ref = loaderMap.get(p);
            if (ref != null) {
                l = ref.get();
                if (l != null) {
                    if (l.validate(paths)) {
                        LOG.log(Level.FINER, "{0}: using existing classloader #{1}", new Object[] { name, l.id });
                        return l;
                    }
                }
            }
            ClassPath cp = ClassPathSupport.createProxyClassPath(paths);
            l = new CachedClassLoader(config, p, cp);
            final int id = l.id;

            LOG.log(Level.FINER, "{0}: creating classloader #{2} for paths {1}", new Object[] { name, p, id });
            loaderMap.put(p, new TimedSoftReference<>(l, r -> {
                synchronized (loaderMap) {
                    LOG.log(Level.FINER, "{0}: Expired reference to classloaedr #{1}", new Object[] { name, id });
                    loaderMap.remove(p, r);
                }
            }));
        }
        return l;
    }
    
    /**
     * Factory finder. For a file, obtain a relevant factory from the project, or use the global factory. Some implementation
     * must be registered in global Lookup, otherwise {@link IllegalStateException} is thrown.
     * 
     * @param f file, not null
     * @return loader factory
     */
    public static @NonNull ClassLoaderFactory forFile(@NonNull FileObject f) {
        ClassLoaderFactory clf = null;
        Project p = f == null ? null : FileOwnerQuery.getOwner(f);
        
        if (p != null) {
            clf = p.getLookup().lookup(ClassLoaderFactory.class);
        }
        if (clf == null) {
            clf = Lookup.getDefault().lookup(ClassLoaderFactory.class);
            if (clf == null) {
                throw new IllegalStateException();
            }
        }
        return clf;
    }
    
    /**
     * Diagnostic only: ID for the loader, also seves as a count of all created loaders.
     */
    private static final AtomicInteger loaderSerial = new AtomicInteger(1);
    
    /**
     * ClassLoader that is cached until the classpath roots change or update.
     */
    private class CachedClassLoader extends ClassLoader implements PropertyChangeListener {
        /**
         * Key of this classloader in the cache map.
         */
        private final List<ClassPath>   key;
        
        /**
         * Common implementation for this and {@link ClassNodeCache.ParsingClassLoader}.
         */
        private final ResourceCache resCache;
        
        /** 
         * Cached roots of classpath.
         */
        private final List<FileObject> pathRoots;
        
        /**
         * Timestamps of the roots/their archives.
         */
        private final List<Long> timestamps;
        
        /**
         * Listener to be UNregistered from ClassPath once this ClassLoader is invalidated.s
         */
        private final PropertyChangeListener weakL;
        
        /**
         * Diagnostics: id.
         */
        private final int id  = loaderSerial.getAndIncrement();
        
        public CachedClassLoader(CompilerConfiguration config, List<ClassPath> key, ClassPath p) {
            super(CompilationUnit.class.getClassLoader());
            this.key = key;
            
            pathRoots = Arrays.asList(p.getRoots());
            resCache = new ResourceCache(p);
            p.addPropertyChangeListener(weakL = WeakListeners.propertyChange(this, p));
            timestamps = timestamps(pathRoots);
        }
        
        private List<Long> timestamps(List<FileObject> roots) {
            List<Long> stamps = new ArrayList<>(roots.size());
            for (FileObject f : roots) {
                long ts;
                
                if (f.isValid()) {
                    FileObject ar = FileUtil.getArchiveFile(f);
                    if (ar != null) {
                        ts = ar.lastModified().getTime();
                    } else {
                        ts = f.lastModified().getTime();
                    }
                } else {
                    ts = -1;
                }
                stamps.add(ts);
            }
            return stamps;
        }
        
        void detach() {
            LOG.log(Level.FINER, "Detaching loader #{0} from classpath", id);
            resCache.getPath().removePropertyChangeListener(weakL);
        }
        
        void invalidate() {
            LOG.log(Level.FINER, "Invalidating loader #{0}", id);
            detach();
            synchronized (loaderMap) {
                Reference<CachedClassLoader> ref = loaderMap.get(key);
                if (ref != null && ref.get() == this) {
                    loaderMap.remove(key);
                }
            }
        }
        
        boolean validate(ClassPath... paths) {
            List<FileObject> roots = new ArrayList<>();
            for (ClassPath cp : paths) {
                roots.addAll(Arrays.asList(cp.getRoots()));
            }
            if (pathRoots.equals(roots) &&
                timestamps.equals(timestamps(roots))) {
                return true;
            } else {
                invalidate();
                return false;
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
                return;
            }
            LOG.log(Level.FINER, "Loader #{0} got PROP_ROOTS", id);
            ClassPath[] paths = key.toArray(new ClassPath[0]);
            validate(paths);
        }

        @Override
        public Enumeration<URL> findResources(String name) throws IOException {
            return resCache.getResources(name);
        }

        @Override
        public URL findResource(String name) {
            return resCache.getResource(name);
        }

        @Override 
        protected Class<?> findClass(final String name) throws ClassNotFoundException {
            String path = name.replace('.', '/').concat(".class");
            URL u = findResource(path);
            if (u == null) {
                return null;
            }
            PerfData.LOG.log(Level.FINER, "** Found class: {0} ", name);
            
            try {
                URLConnection con = u.openConnection();
                int len = con.getContentLength();
                int remains = len;
                byte[] buf = new byte[len];
                try (InputStream is = u.openStream()) {
                    int o = 0;
                    while (remains > 0) {
                        int x = is.read(buf, o, remains);
                        o += x;
                        remains -= x;
                    }
                }
                return defineClass(name, buf, 0, len);
            } catch (IOException ex) {
                throw new ClassNotFoundException(name, ex);
            }
        }
                
    }
}
