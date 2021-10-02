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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyResourceLoader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Enumerations;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
//@NotThreadSafe //Should be guarded by parsing.api infrastructure
public final class ClassNodeCache {
    
    private static final Logger LOG =
            Logger.getLogger(ClassNodeCache.class.getName());
    private static final ThreadLocal<ClassNodeCache> instance = new ThreadLocal<>();
    
    private static final int DEFAULT_NON_EXISTENT_CACHE_SIZE = 10000;
    private static final int NON_EXISTENT_CACHE_SIZE = Integer.getInteger(
            "groovy.editor.ClassNodeCache.nonExistent.size",
            DEFAULT_NON_EXISTENT_CACHE_SIZE);
    private static final char INNER_SEPARATOR = '$';    //NOI18N
    private static final char PKG_SEPARATOR = '.';      //NOI18N
    
    private final Map<CharSequence,ClassNode> cache;
    private final Map<CharSequence,Void> nonExistent;
    private Reference<JavaSource> resolver;
    private Reference<GroovyClassLoader> transformationLoaderRef;
    private Reference<GroovyClassLoader> resolveLoaderRef;
    private long invocationCount;
    private long hitCount;
    private PerfData perfData;
    
    private ClassNodeCache() {
        this.cache = new HashMap<>();
        this.nonExistent = new LinkedHashMap<CharSequence, Void>(16,0.75f,true) {
            @Override
            protected boolean removeEldestEntry(Entry<CharSequence, Void> eldest) {
                if (size() > NON_EXISTENT_CACHE_SIZE) {
                    LOG.log(
                        Level.FINE,
                        "Non existent cache full, removing : {0}",    //NOI18N
                        eldest.getKey());
                    return true;
                }
                return false;
            }
        };
        LOG.fine("ClassNodeCache created");     //NOI18N
    }
    
    @CheckForNull
    public ClassNode get(@NonNull final CharSequence name) {        
        final ClassNode result = cache.get(name);
        if (LOG.isLoggable(Level.FINER)) {
            invocationCount++;
            if (result != null) {
                hitCount++;
            } else {
                LOG.log(
                    Level.FINEST,
                    "No binding for: {0}",   //NOI18N
                    name);
            }
            LOG.log(
                Level.FINER,
                "Hit ratio: {0}%",  //NOI18N
                (double)hitCount/invocationCount*100);
        }
        return result;
    }
    
    public boolean isNonExistentResource(@NonNull final CharSequence name) {
        return nonExistent.containsKey(name);
    }
    
    public void addNonExistentResource(@NonNull final CharSequence name) {
        LOG.log(
            Level.FINE,
            "Unreachable resource: {0}",    //NOI18N
            name);
        nonExistent.putIfAbsent(name, null);
    }
    
    public boolean isNonExistent (@NonNull final CharSequence name) {
        if (!isValidClassName(name)) {
            return true;
        }
        final boolean res = getNonExistent(name) != null;
        if (LOG.isLoggable(Level.FINER)) {
            invocationCount++;
            if (res) {
                hitCount++;
            } else {
                LOG.log(
                    Level.FINEST,
                    "No binding for: {0}",   //NOI18N
                    name);
            }
            LOG.log(
                Level.FINER,
                "Hit ratio: {0}%",  //NOI18N
                (double)hitCount/invocationCount*100);
        }
        return res;
    }
    
    public void put (
        @NonNull final CharSequence name,
        @NullAllowed final ClassNode node) {
        if (node != null) {
            LOG.log(
                Level.FINE,
                "Added binding for: {0}",    //NOI18N
                name);
            cache.put(name,node);
        } else {
            final CharSequence parentName = getNonExistent(name);
            LOG.log(
                Level.FINE,
                "Added nonexistent class: {0}",    //NOI18N
                name);
            nonExistent.put(
                parentName != null ?
                    parentName : name,
                    null);
        }
    }
    
    public boolean containsKey(@NonNull final CharSequence name) {
        final boolean result = cache.containsKey(name);
        if (LOG.isLoggable(Level.FINER)) {
            invocationCount++;
            if (result) {
                hitCount++;
            } else {
                LOG.log(
                    Level.FINEST,
                    "No binding for: {0}",   //NOI18N
                    name);
            }
            LOG.log(
                Level.FINER,
                "Hit ratio: {0}%",  //NOI18N
                (double)hitCount/invocationCount*100);
        }
        return result;
        
    }
    
    @NonNull
    public JavaSource createResolver(@NonNull final ClasspathInfo info) {
        JavaSource src = resolver == null ? null : resolver.get();
        if (src == null) {
            LOG.log(Level.FINE,"Javac resolver created.");  //NOI18N
            src = JavaSource.create(info);
            resolver = new SoftReference<>(src);
        }
        return src;
    }
    
    public GroovyClassLoader createTransformationLoader(
            @NonNull final ClassPath allResources,
            @NonNull final CompilerConfiguration configuration) {        
        GroovyClassLoader transformationLoader = transformationLoaderRef == null ? null : transformationLoaderRef.get();
        if (transformationLoader == null) {
            LOG.log(Level.FINE,"Transformation ClassLoader created.");  //NOI18N
            transformationLoader = 
                new TransformationClassLoader(
                    CompilationUnit.class.getClassLoader(),
                    allResources,
                    configuration);
            transformationLoaderRef = new SoftReference<>(transformationLoader);
        }
        return transformationLoader;
    }
    
    public ParsingClassLoader createResolveLoader(
            @NonNull final ClassPath allResources,
            @NonNull final CompilerConfiguration configuration) {
        GroovyClassLoader resolveLoader = resolveLoaderRef == null ? null : resolveLoaderRef.get();
        if (resolveLoader == null) {
            LOG.log(Level.FINE,"Resolver ClassLoader created.");  //NOI18N
            resolveLoader = new ParsingClassLoader(
                    allResources,
                    configuration,
                    this);
            resolveLoaderRef = new SoftReference<>(resolveLoader);
        }
        return (ParsingClassLoader)resolveLoader;
    }

    @CheckForNull
    private CharSequence getNonExistent(@NonNull final CharSequence name) {
        for (int index = name.length(); index > 0; index = getNextPoint(name,index)) {
            final CharSequence subName = name.subSequence(0, index);
            if (nonExistent.containsKey(subName)) {
                return subName;
            }
        }
        return null;
    }
    
    @NonNull
    public static ClassNodeCache get() {
        ClassNodeCache c = instance.get();
        if (c == null) {
            c = new ClassNodeCache();
        }
        return c;
    }    
    
    public static ClassNodeCache createThreadLocalInstance() {
        final ClassNodeCache c = new ClassNodeCache();
        instance.set(c);
        LOG.log(
            Level.FINE,
            "ClassNodeCache attached to thread: {0}",    //NOI18N
            Thread.currentThread().getId());
        return c;
    }
    
    public static void clearThreadLocalInstance() {        
        instance.remove();
        LOG.log(
            Level.FINE,
            "ClassNodeCache removed from thread: {0}",    //NOI18N
            Thread.currentThread().getId());
    }


    private static int getNextPoint(
            @NonNull final CharSequence name,
            final int currentPoint) {
        for (int i=currentPoint-1; i>0; i--) {
            if (name.charAt(i) == INNER_SEPARATOR) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isValidClassName(@NonNull final CharSequence name) {
        int lastDot = -1;
        for (int i=name.length()-1; i>=0; i--) {
            final char c = name.charAt(i);
            if (c == PKG_SEPARATOR) {
                lastDot = c;
            } else if (c == INNER_SEPARATOR) {
                if (lastDot > c) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static class TransformationClassLoader extends GroovyClassLoader {

        public TransformationClassLoader(ClassLoader parent, ClassPath cp, CompilerConfiguration config) {
            super(parent, config);
            for (ClassPath.Entry entry : cp.entries()) {
                this.addURL(entry.getURL());
            }
        }

    }

    public static class ParsingClassLoader extends GroovyClassLoader {

        private static final ClassNotFoundException CNF = new ClassNotFoundException();
        
        /**
         * Indicates the resource is just one, there are not multiple resources. Will be
         * replaced by URL in the cache on first reference, so next accesses do not need to
         * use URLMappers. Note: this tag value is compared by identity
         */
        private static final URL PLACEHOLDER; 
        
        /**
         * Multiple resources of the same name; use slow method. Note: this tag value is compared by identity
         */
        private static final URL MULTIPLE; 
        
        static {
            try {
                PLACEHOLDER = new URL("file:///");
                MULTIPLE = new URL("file:///");
            } catch (IOException ex) {
                throw new IllegalStateException();
            }
        }
        
        private final CompilerConfiguration config;

        private final ClassPath path;
        
        private final ClassNodeCache cache;

        private final GroovyResourceLoader resourceLoader
                = (String filename) -> AccessController.doPrivileged(
                        (PrivilegedAction<URL>) () -> getSourceFile(filename));
        
        private PerfData perfData;
        
        private CompilationUnit unit;
        
        /**
         * Map of folder contents. Indexed by folder path, values are file => URL/placeholder
         */
        private final Map<String, Map<String, URL>> folderContents = new HashMap<>();
        
        public ParsingClassLoader(
                @NonNull ClassPath path,
                @NonNull CompilerConfiguration config,
                @NonNull ClassNodeCache cache) {
            super(path.getClassLoader(true), config);
            this.config = config;
            this.path = path;
            this.cache = cache;
        }

        public void setPerfData(PerfData perfData) {
            this.perfData = perfData;
        }

        public void setUnit(CompilationUnit unit) {
            this.unit = unit;
        }

        @Override
        public Class loadClass(
                final String name,
                final boolean lookupScriptFiles,
                final boolean preferClassOverScript,
                final boolean resolve) throws ClassNotFoundException, CompilationFailedException {
            LOG.log(Level.FINE, "Parser {4} asking for {0}, scripts {1}, classOverScript {2}, resolve {3}", 
                    new Object[] { name, lookupScriptFiles, preferClassOverScript, resolve, 
                        System.identityHashCode(this)
                    });
            String rn = null;
            if (preferClassOverScript && !lookupScriptFiles) {
                rn = name.replace(".", "/") + ".class";
                if (cache.isNonExistentResource(rn)) {
                    LOG.log(Level.FINE, " -> cached NONE");
                    throw CNF;
                }
                
                //Ideally throw CNF but we need to workaround fix of issue #206811
                //which hurts performance.
                if (cache.isNonExistent(name)) {
                    LOG.log(Level.FINE, " -> cached NONE");
                    throw CNF;
                }
            }
            boolean ok = false;
            try {
                Class c = super.loadClass(name, lookupScriptFiles, preferClassOverScript, resolve);
                ok = true;
                return c;
            } catch (ClassNotFoundException ex) {
                // NETBEANS-5982: Groovy tries to load some types (i.e. annotations) into JVM. We serve .class resources
                // from .sig files produced by Java indexer, then Groovy "needs" to load them for further inspection,
                // but the ClassLoaderSupport refuses to do so. Until NETBEANS-5982 is fixed, attempt to load .sig file into
                // JVM.
                // This ClassLoader is a throwaway one, so if the source changes, the classes can be loaded again in a different
                // ParsingCL instance next parsing round.
                String cr = name.replace(".", "/") + ".class"; // NOI18N
                URL u = getResource(cr);
                if (u != null) {
                    try {
                        URLConnection con = u.openConnection(); 
                        byte[] contents = new byte[con.getContentLength()];
                        try (InputStream cs = u.openStream()) {
                            cs.read(contents);
                        }
                        Class c = defineClass(name, contents);
                        ok = true;
                        return c;
                    } catch (IOException ex2) {
                        throw ex;
                    }
                } else {
                    throw ex;
                }
            } finally {
                if (!ok && rn != null) {
                    cache.addNonExistentResource(rn);
                }
            }
        }
        
        /**
         * Will load folder from {@link #path} and return its contents. The 1st pair element
         * is the filename, the second is a Map of folder's contents.
         * @param resourceName full resource name to search for
         * @return filename and folder contents.
         */
        private Pair<String, Map<String, URL>> loadFolder(String resourceName) {
            int lastSlash = resourceName.lastIndexOf('/');
            String folderName = lastSlash == -1 ? "" : resourceName.substring(0, lastSlash);
            Map<String, URL> contents = folderContents.get(folderName);
            String rest = resourceName.substring(lastSlash + 1);
            if (cache.isNonExistentResource(folderName)) {
                return Pair.of(rest, Collections.emptyMap());
            }
            if (contents != null) {
                return Pair.of(rest, contents);
            }
            Map<String, URL> lhm = new LinkedHashMap<>();
            boolean empty = true;
            for (FileObject parent: path.findAllResources(folderName)) {
                for (FileObject f : parent.getChildren()) {
                    if (lhm.putIfAbsent(f.getNameExt(), PLACEHOLDER) != null) {
                        lhm.put(f.getNameExt(), MULTIPLE);
                    }
                    empty = false;
                }
            }
            folderContents.put(folderName, lhm);
            if (empty) {
                cache.addNonExistentResource(folderName);
            }
            return Pair.of(rest, lhm);
        }

        // allow to conditionally disable this optimization, for debugging.
        private static final boolean RESOURCES_FROM_FILESYSTEMS = Boolean.valueOf(System.getProperty(GroovyParser.class.getName() + ".useFilesystems", "true"));
        
        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            if (cache.isNonExistentResource(name)) {
                return Enumerations.empty();
            }
            if (!RESOURCES_FROM_FILESYSTEMS) {
                 Enumeration<URL> en = super.getResources(name);
                if (!en.hasMoreElements()) {
                    cache.addNonExistentResource(name);
                }
                return en;
            }
            Pair<String, Map<String, URL>> fl = loadFolder(name);
            URL res = fl.second().get(fl.first());
            if (res == null) {
                cache.addNonExistentResource(name);
                return Collections.emptyEnumeration();
            } else if (res == MULTIPLE) {
                return super.getResources(name);
            } else if (res == PLACEHOLDER) {
                Enumeration<URL> r = super.getResources(name);
                if (r.hasMoreElements()) {
                    res = r.nextElement();
                    fl.second().put(fl.first(), res);
                } else {
                    cache.addNonExistentResource(name);
                    return null;
                }
            }
            return Enumerations.singleton(res);
        }
        
        private URL doGetResource(String name) {
            Pair<String, Map<String, URL>> fl = loadFolder(name);
            URL res = fl.second().get(fl.first());
            if (res == null) {
                return null;
            }
            
            if (res == MULTIPLE) {
                FileObject f = path.findResource(name);
                return URLMapper.findURL(f, URLMapper.INTERNAL);
            } else if (res == PLACEHOLDER) {
                FileObject f = path.findResource(name);
                res = URLMapper.findURL(f, URLMapper.INTERNAL);
                fl.second().put(fl.first(), res);
                return res;
            } else {
                return res;
            }
        }

        @Override
        public URL getResource(String name) {
            long t = System.currentTimeMillis();
            try {
                if (cache.isNonExistentResource(name)) {
                    return null;
                }
                if (!RESOURCES_FROM_FILESYSTEMS) {
                    URL u = super.getResource(name);
                    if (u == null) {
                        LOG.log(Level.FINE, " -> caching nonexistent: " + name);
                        cache.addNonExistentResource(name);
                    }
                    return u;
                }
                URL u = doGetResource(name);
                if (u == null && name.endsWith(".class")) {
                    String sigName = name.substring(0, name.length() - 5) + "sig";
                    u = doGetResource(sigName);
                }
                if (u == null) {
                    LOG.log(Level.FINE, " -> caching nonexistent: " + name);
                    cache.addNonExistentResource(name);
                }
                return u;
            } finally {
                long t2 = System.currentTimeMillis();
                perfData.addVisitorTime(unit.getPhase(), "ParsingClassLoader", t2 - t);
            }
        }

        @Override
        public URL findResource(String name) {
            if (cache.isNonExistentResource(name)) {
                return null;
            }
            LOG.log(Level.FINE, "Parser {1} findResource {0}", 
                    new Object[] { name, System.identityHashCode(this) });
            return super.findResource(name);
        }

        @Override
        public GroovyResourceLoader getResourceLoader() {
            return resourceLoader;
        }

        private URL getSourceFile(String name) {
            // this is slightly faster then original implementation
            FileObject fo = path.findResource(name.replace('.', '/') + config.getDefaultScriptExtension());
            if (fo == null || fo.isFolder()) {
                return null;
            }
            return URLMapper.findURL(fo, URLMapper.EXTERNAL);
        }
    }

    public PerfData getPerfData() {
        return perfData;
    }

    public void setPerfData(PerfData perfData) {
        this.perfData = perfData;
    }
}
