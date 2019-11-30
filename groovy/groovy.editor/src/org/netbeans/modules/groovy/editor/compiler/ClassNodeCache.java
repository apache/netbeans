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
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

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
    
    public GroovyClassLoader createResolveLoader(
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
        return resolveLoader;
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

    private static class ParsingClassLoader extends GroovyClassLoader {

        private static final ClassNotFoundException CNF = new ClassNotFoundException();
        
        private final CompilerConfiguration config;

        private final ClassPath path;
        
        private final ClassNodeCache cache;

        private final GroovyResourceLoader resourceLoader
                = (String filename) -> AccessController.doPrivileged(
                        (PrivilegedAction<URL>) () -> getSourceFile(filename));

        public ParsingClassLoader(
                @NonNull ClassPath path,
                @NonNull CompilerConfiguration config,
                @NonNull ClassNodeCache cache) {
            super(path.getClassLoader(true), config);
            this.config = config;
            this.path = path;
            this.cache = cache;
        }
        
        @Override
        public Class loadClass(
                final String name,
                final boolean lookupScriptFiles,
                final boolean preferClassOverScript,
                final boolean resolve) throws ClassNotFoundException, CompilationFailedException {
            if (preferClassOverScript && !lookupScriptFiles) {
                //Ideally throw CNF but we need to workaround fix of issue #206811
                //which hurts performance.
                if (cache.isNonExistent(name)) {
                    throw CNF;
                }
            }
            return super.loadClass(name, lookupScriptFiles, preferClassOverScript, resolve);
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
}
