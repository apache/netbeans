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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 *
 * The query is used for finding binaries for sources,
 * this is intended to be the inverse of the SourceForBinaryQuery.
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
    
    private static class DefaultResult implements Result {
        
        private final URL sourceRoot;
        
        DefaultResult (final URL sourceRoot) {
            this.sourceRoot = sourceRoot;
        }
    
        public URL[] getRoots() {
            FileObject fo = URLMapper.findFileObject(sourceRoot);
            if (fo == null) {
                return new URL[0];
            }
            ClassPath exec = ClassPath.getClassPath(fo, ClassPath.EXECUTE);
            if (exec == null) {
                return new URL[0];
            }           
            Set<URL> result = new HashSet<URL>();
            for (ClassPath.Entry e : exec.entries()) {
                final URL eurl = e.getURL();
                FileObject[] roots = SourceForBinaryQuery.findSourceRoots(eurl).getRoots();
                for (FileObject root : roots) {
                        if (sourceRoot.equals (root.toURL())) {
                            result.add (eurl);
                        }
                }
            }
            return result.toArray(new URL[result.size()]);
        }

        public void addChangeListener(ChangeListener l) {            
        }

        public void removeChangeListener(ChangeListener l) {            
        }
    }
}
