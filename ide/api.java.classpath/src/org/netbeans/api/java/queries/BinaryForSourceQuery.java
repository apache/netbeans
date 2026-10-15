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
package org.netbeans.api.java.queries;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.classpath.QueriesAccessor;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 *
 * The query is used for finding binaries for sources,
 * this is intended to be the inverse of the {@link SourceForBinaryQuery}.
 * @see BinaryForSourceQueryImplementation2
 * @see BinaryForSourceQueryImplementation
 * @see SourceForBinaryQuery
 * @since org.netbeans.api.java/1 1.12
 * @author Tomas Zezula
 * 
 */
public final class BinaryForSourceQuery {
    
    
    private static final Logger LOG = Logger.getLogger(BinaryForSourceQuery.class.getName());
    
    
    /** Creates a new instance of BInaryForSOurceQuery */
    private BinaryForSourceQuery() {
    }
    
    /**
     * Returns the binary root for given source root.
     * @param sourceRoot the source path root. The URL must refer to folder. 
     * In the case of archive file the jar protocol URL must be used.
     * The folder URL has to end with '/' The {@link FileUtil#urlForArchiveOrDir}
     * can be used to create folder URLs.
     * @return a result object encapsulating the answer (never null)
     */
    public static Result findBinaryRoots (final URL sourceRoot) {
       assert sourceRoot != null;
       for (BinaryForSourceQueryImplementation impl : Lookup.getDefault().lookupAll(BinaryForSourceQueryImplementation.class)) {
           BinaryForSourceQuery.Result result = impl.findBinaryRoots (sourceRoot);
           if (result != null) {
               if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(
                        Level.FINE,
                        "findBinaryRoots({0}) -> {1} from {2}", //NOI18N
                        new Object[] {
                            sourceRoot,
                            Arrays.asList(result.getRoots()),
                            impl});
                }
               return result;
           }
       }
       LOG.log(
           Level.FINE,
           "findBinaryRoots({0}) -> nil",  //NOI18N
           sourceRoot);
       return new DefaultResult (sourceRoot);
    }

    /**
     * Returns the binary root for given source root as computed by {@link BinaryForSourceQueryImplementation2}.
     * @param sourceRoot the source path root. The URL must refer to folder.
     * In the case of archive file the jar protocol URL must be used.
     * The folder URL has to end with '/' The {@link FileUtil#urlForArchiveOrDir}
     * can be used to create folder URLs.
     * @return a result object encapsulating the answer (never null)
     * @since 1.58
     */
    public static Result2 findBinaryRoots2(URL sourceRoot) {
        return QueriesAccessor.wrap(findBinaryRoots(sourceRoot));
    }
    
    /**
     * Result of finding binaries, encapsulating the answer as well as the
     * ability to listen to it.
     */
    public static interface Result {
        
        /**
         * Get the binary roots.         
         * @return array of roots of compiled classes (may be empty but not null)
         */
        URL[] getRoots();
        
        /**
         * Add a listener to changes in the roots.
         * @param l a listener to add
         */
        void addChangeListener(ChangeListener l);
        
        /**
         * Remove a listener to changes in the roots.
         * @param l a listener to remove
         */
        void removeChangeListener(ChangeListener l);
    }

    /** Enhanced version of {@link Result} obtained via
     * {@link BinaryForSourceQuery#findBinaryRoots2(java.net.URL)} method.
     * Use {@link BinaryForSourceQueryImplementation2} to create instance
     * of this class.
     * @since 1.58
     */
    public abstract static class Result2 implements Result {
        Result2() {
        }

        /**
         * Check whether the binaries should be prefered over sources.
         * Return {@code true} if the classes (if newer than sources) shall be
         * copied instead of compiling the sources by the IDE.
         * @return true or false
         * @since 1.58
         */
        public abstract boolean preferBinaries();
    }
    
    private static class DefaultResult extends Result2 {
        
        private final URL sourceRoot;
        
        DefaultResult (final URL sourceRoot) {
            this.sourceRoot = sourceRoot;
        }
    
        @Override
        public URL[] getRoots() {
            FileObject fo = URLMapper.findFileObject(sourceRoot);
            if (fo == null) {
                return new URL[0];
            }
            ClassPath exec = ClassPath.getClassPath(fo, ClassPath.EXECUTE);
            if (exec == null) {
                return new URL[0];
            }           
            Set<URL> result = new HashSet<>();
            for (ClassPath.Entry e : exec.entries()) {
                final URL eurl = e.getURL();
                FileObject[] roots = SourceForBinaryQuery.findSourceRoots(eurl).getRoots();
                for (FileObject root : roots) {
                        if (sourceRoot.equals (root.toURL())) {
                            result.add (eurl);
                        }
                }
            }
            return result.toArray(new URL[0]);
        }

        @Override
        public void addChangeListener(ChangeListener l) {            
        }

        @Override
        public void removeChangeListener(ChangeListener l) {            
        }

        @Override
        public boolean preferBinaries() {
            return false;
        }
    }

    private static final class Result2Impl<T> extends Result2 {
        final BinaryForSourceQueryImplementation2<T> impl;
        final T value;

        Result2Impl(BinaryForSourceQueryImplementation2<T> impl, T value) {
            this.impl = impl;
            this.value = value;
        }

        @Override
        public boolean preferBinaries() {
            return impl.computePreferBinaries(value);
        }

        @Override
        public URL[] getRoots() {
            return impl.computeRoots(value);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            impl.computeChangeListener(value, true, l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            impl.computeChangeListener(value, false, l);
        }


    }
    
    static final QueriesAccessorImpl CACHE = new QueriesAccessorImpl();
    static {
        QueriesAccessor.setInstance(CACHE);
    }

    static final class QueriesAccessorImpl extends QueriesAccessor {
        QueriesAccessorImpl() {
        }
        private final Map<Object, Result2Impl<?>> cache = new WeakHashMap<>();

        @Override
        public synchronized <T> Result2 create(BinaryForSourceQueryImplementation2<T> impl, T value) {
            Result2Impl<?> result = cache.get(value);
            if (result == null) {
                result = new Result2Impl<>(impl, value);
                cache.put(value, result);
            }
            assert impl == result.impl;
            return result;
        }

        synchronized Object findRegistered(Object prototype) {
            for (Object object : cache.keySet()) {
                if (prototype.equals(object)) {
                    return object;
                }
            }
            return null;
        }
    }
}
