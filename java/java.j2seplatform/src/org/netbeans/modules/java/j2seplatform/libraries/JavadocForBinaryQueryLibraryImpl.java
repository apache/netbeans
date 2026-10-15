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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.util.WeakListeners;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 * Implementation of Javadoc query for the library.
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation.class, position=150)
public class JavadocForBinaryQueryLibraryImpl implements JavadocForBinaryQueryImplementation {
    
    private final Map<URI,URL> normalizedURLCache = new ConcurrentHashMap<>();

    /** Default constructor for lookup. */
    public JavadocForBinaryQueryLibraryImpl() {
    }

    @Override
    @CheckForNull
    public JavadocForBinaryQuery.Result findJavadoc(@NonNull final URL b) {
        final Boolean isNormalizedURL = isNormalizedURL(b);
        if (isNormalizedURL != null) {
            for (LibraryManager mgr : LibraryManager.getOpenManagers()) {
                for (Library lib : mgr.getLibraries()) {
                    if (!lib.getType().equals(J2SELibraryTypeProvider.LIBRARY_TYPE)) {
                        continue;
                    }
                    for (URL entry : lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH)) {
                        URL normalizedEntry;
                        if (isNormalizedURL == Boolean.TRUE) {
                            normalizedEntry = getNormalizedURL(entry);
                        } else {
                            normalizedEntry = entry;
                        }
                        if (b.equals(normalizedEntry)) {
                            return new R(lib);
                        }
                    }
                }
            }
        }
        return null;
    }

    private URL getNormalizedURL (URL url) {
        //URL is already nornalized, return it
        final Boolean isNormalized = isNormalizedURL(url);
        if (isNormalized == null) {
            return null;
        }
        if (isNormalized == Boolean.TRUE) {
            return url;
        }
        //Todo: Should listen on the LibrariesManager and cleanup cache
        // in this case the search can use the cache onle and can be faster
        // from O(n) to O(ln(n))
        URI uri = null;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            Exceptions.printStackTrace(e);
        }
        URL normalizedURL = uri == null ? null : normalizedURLCache.get(uri);
        if (normalizedURL == null) {
            final FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                normalizedURL = fo.toURL();
                if (uri != null) {
                    this.normalizedURLCache.put (uri, normalizedURL);
                }
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
    private static Boolean isNormalizedURL (URL url) {
        if ("jar".equals(url.getProtocol())) { //NOI18N
            url = FileUtil.getArchiveFile(url);
            if (url == null) {
                //Broken URL
                return null;
            }
        }
        return "file".equals(url.getProtocol());    //NOI18N
    }

    private static class R implements JavadocForBinaryQuery.Result, PropertyChangeListener {

        private final Library lib;
        private final ChangeSupport cs = new ChangeSupport(this);
        private URL[] cachedRoots;


        public R (Library lib) {
            this.lib = lib;
            this.lib.addPropertyChangeListener (WeakListeners.propertyChange(this,this.lib));
        }

        @Override
        public synchronized URL[] getRoots() {
            if (this.cachedRoots == null) {
                List<URL> result = new ArrayList<URL>();
                for (URL u : lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_JAVADOC)) {
                    result.add (getIndexFolder(u));
                }
                this.cachedRoots = result.toArray(new URL[0]);
            }
            return this.cachedRoots;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            assert l != null : "Listener can not be null";
            cs.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            assert l != null : "Listener can not be null";
            cs.removeChangeListener(l);
        }

        @Override
        public void propertyChange (PropertyChangeEvent event) {
            if (Library.PROP_CONTENT.equals(event.getPropertyName())) {
                synchronized (this) {
                    this.cachedRoots = null;
                }
                cs.fireChange();
            }
        }

        private static URL getIndexFolder (final URL url) {
            assert url != null;
            final FileObject root = URLMapper.findFileObject(url);
            if (root == null) {
                return url;
            }
            final FileObject index = JavadocAndSourceRootDetection.findJavadocRoot(root);
            if (index == null) {
                return url;
            }
            return index.toURL();
        }
    }
}
