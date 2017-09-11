/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                this.cachedRoots = result.toArray(new URL[result.size()]);
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
