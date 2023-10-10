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
package org.netbeans.spi.java.classpath.support;

import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.modules.java.classpath.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileUtil;

/**
 * Convenience factory for creating classpaths of common sorts.
 * @since org.netbeans.api.java/1 1.4
 */
public class ClassPathSupport {

    private ClassPathSupport () {
    }


    /** Creates leaf PathResourceImplementation.
     * The created PathResourceImplementation has exactly one immutable root.
     * @param url the root of the resource. The URL must refer to folder. In the case of archive file
     * the jar protocol URL must be used. The folder URL has to end with '/' The {@link FileUtil#urlForArchiveOrDir}
     * can be used to create folder URLs.
     * @return PathResourceImplementation
     */
    public static PathResourceImplementation createResource (URL url) {
        return new SimplePathResourceImplementation (url);
    }


    /**
     * Create ClassPathImplementation for the given list of
     * {@link PathResourceImplementation} entries.
     * @param entries list of {@link PathResourceImplementation} instances;
     *     cannot be null; can be empty
     * @return SPI classpath
     */
    public static ClassPathImplementation createClassPathImplementation(List< ? extends PathResourceImplementation> entries) {
        if (entries == null) {
            throw new NullPointerException("Cannot pass null entries"); // NOI18N
        }
        return new SimpleClassPathImplementation(entries);
    }


    /**
     * Create ClassPath for the given list of
     * {@link PathResourceImplementation} entries.
     * @param entries list of {@link PathResourceImplementation} instances;
     *     cannot be null; can be empty
     * @return API classpath
     */
    public static ClassPath createClassPath(List<? extends PathResourceImplementation> entries) {
        if (entries == null) {
            throw new NullPointerException("Cannot pass null entries"); // NOI18N
        }
        return ClassPathFactory.createClassPath(createClassPathImplementation(entries));
    }


    /**
     * Create ClassPath for the given array of class path roots
     * @param roots array of fileobjects which must correspond to directory.
     * In the case of archive file, the FileObject representing the root of the
     * archive must be used.  Cannot be null; can be empty array; array can contain nulls.
     * @return API classpath
     */
    public static ClassPath createClassPath(FileObject... roots) {
        assert roots != null;
        List<PathResourceImplementation> l = new ArrayList<PathResourceImplementation> ();
        for (FileObject root : roots) {
            if (root == null || !root.isValid()) {
                continue;
            }
                URL u = root.toURL();
                l.add(createResource(u));
        }
        return createClassPath (l);
    }


    /**
     * Create ClassPath for the given array of class path roots
     * @param roots array of URLs which must correspond to folder.
     * In the case of archive file, the jar protocol URL must be used.
     * The folder URL has to end with '/'. The {@link FileUtil#urlForArchiveOrDir}
     * can be used to create folder URLs.
     * Cannot be null; can be empty array; array can contain nulls.
     * @return API classpath
     */
    public static ClassPath createClassPath(URL... roots) {
        assert roots != null;
        List<PathResourceImplementation> l = new ArrayList<PathResourceImplementation> ();
        for (URL root : roots) {
            if (root == null)
                continue;
            l.add (createResource(root));
        }
        return createClassPath(l);
    }

    /**
     * Convenience method to create a classpath object from a conventional string representation.
     * @param jvmPath a JVM-style classpath (folder or archive paths separated by {@link File#pathSeparator})
     * @return a corresponding classpath object
     * @throws IllegalArgumentException in case a path entry looks to be invalid
     * @since org.netbeans.api.java/1 1.15
     * @see FileUtil#urlForArchiveOrDir
     */
    public static ClassPath createClassPath(String jvmPath) throws IllegalArgumentException {
        List<PathResourceImplementation> l = new ArrayList<PathResourceImplementation>();
        for (String piece : jvmPath.split(File.pathSeparator)) {
            File f = FileUtil.normalizeFile(new File(piece));
            URL u = FileUtil.urlForArchiveOrDir(f);
            if (u == null) {
                throw new IllegalArgumentException("Path entry looks to be invalid: " + piece); // NOI18N
            }
            l.add(createResource(u));
        }
        return createClassPath(l);
    }

    /**
     * Creates read only proxy ClassPathImplementation for given delegates.
     * The order of resources is given by the order of the delegates
     * @param delegates ClassPathImplementations to delegate to.
     * @return SPI classpath
     */
    public static ClassPathImplementation createProxyClassPathImplementation(ClassPathImplementation... delegates) {
        return new ProxyClassPathImplementation (delegates);
    }


    /**
     * Creates read only proxy ClassPath for given delegates.
     * The order of resources is given by the order of the delegates
     * @param delegates ClassPaths to delegate to.
     * @return API classpath
     */
    public static ClassPath createProxyClassPath(ClassPath... delegates) {
        assert delegates != null;
        ClassPathImplementation[] impls = new ClassPathImplementation [delegates.length];
        for (int i = 0; i < delegates.length; i++) {
             impls[i] = ClassPathAccessor.getDefault().getClassPathImpl (delegates[i]);
        }
        return ClassPathFactory.createClassPath (createProxyClassPathImplementation(impls));
    }

    /**
     * Creates a {@link ClassPath} switching among several {@link ClassPath} instances.
     * @param selector  the active {@link ClassPath} provider
     * @return a newly created {@link ClassPath} instance
     * @since 1.54
     */
    @NonNull
    public static ClassPath createMultiplexClassPath(@NonNull final Selector selector) {
        return ClassPathFactory.createClassPath(new MuxClassPathImplementation(selector));
    }

    /**
     * The active {@link ClassPath} provider for multiplexing {@link ClassPath}.
     * @since 1.54
     */
    public static interface Selector {
        /**
         * The name of the <code>activeClassPath</code> property.
         */
        static final String PROP_ACTIVE_CLASS_PATH = "activeClassPath";  //NOI18N

        /**
         * Returns the active {@link ClassPath}.
         * @return the active {@link ClassPath}
         */
        @NonNull
        ClassPath getActiveClassPath();

        /**
         * Adds {@link PropertyChangeListener} listening on active {@link ClassPath} change.
         * @param listener the listener notified when an active {@link ClassPath} selection changes
         */
        void addPropertyChangeListener(@NonNull PropertyChangeListener listener);
        /**
         * Removes {@link PropertyChangeListener} listening on active {@link ClassPath} change.
         * @param listener the listener to be removed
         */
        void removePropertyChangeListener(@NonNull PropertyChangeListener listener);
    }

}
