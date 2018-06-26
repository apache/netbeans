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

package org.netbeans.modules.j2ee.deployment.impl.sharability;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.util.WeakListeners;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

/**
 * Implementation of Javadoc query for the library.
 */
@org.netbeans.api.annotations.common.SuppressWarnings(value="DMI_COLLECTION_OF_URLS", justification="File URLs only")
//@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation.class)
public class JavadocForBinaryQueryImpl implements JavadocForBinaryQueryImplementation {

    private static final String[] CLASSPATH_VOLUMES = new String[] {
        ServerLibraryTypeProvider.VOLUME_CLASSPATH,
        ServerLibraryTypeProvider.VOLUME_WS_COMPILE_CLASSPATH
    };

    private static int MAX_DEPTH = 3;
    private final Map<URL,URL> normalizedURLCache = new HashMap<URL, URL>();

    /** Default constructor for lookup. */
    public JavadocForBinaryQueryImpl() {
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value="DMI_BLOCKING_METHODS_ON_URL", justification="File URLs only")
    public JavadocForBinaryQuery.Result findJavadoc(final URL b) {
        class R implements JavadocForBinaryQuery.Result, PropertyChangeListener {

            private Library lib;
            private final ChangeSupport cs = new ChangeSupport(this);
            private URL[] cachedRoots;


            public R (Library lib) {
                this.lib = lib;
                this.lib.addPropertyChangeListener (WeakListeners.propertyChange(this,this.lib));
            }

            public synchronized URL[] getRoots() {
                if (this.cachedRoots == null) {
                    List<URL> result = new ArrayList<URL>();
                    for (URL u : lib.getContent(ServerLibraryTypeProvider.VOLUME_JAVADOC)) {
                        result.add(getIndexFolder(u));
                    }
                    this.cachedRoots = result.toArray(new URL[result.size()]);
                }
                return this.cachedRoots;
            }

            public synchronized void addChangeListener(ChangeListener l) {
                assert l != null : "Listener can not be null";
                cs.addChangeListener(l);
            }

            public synchronized void removeChangeListener(ChangeListener l) {
                assert l != null : "Listener can not be null";
                cs.removeChangeListener(l);
            }

            public void propertyChange (PropertyChangeEvent event) {
                if (Library.PROP_CONTENT.equals(event.getPropertyName())) {
                    synchronized (this) {
                        this.cachedRoots = null;
                    }
                    cs.fireChange();
                }
            }

        }

        boolean isNormalizedURL = isNormalizedURL(b);
        for (LibraryManager mgr : LibraryManager.getOpenManagers()) {
            for (Library lib : mgr.getLibraries()) {
                if (!lib.getType().equals(ServerLibraryTypeProvider.LIBRARY_TYPE)) {
                    continue;
                }
                for (String type : CLASSPATH_VOLUMES) {
                    for (URL entry : lib.getContent(type)) {
                        URL normalizedEntry;
                        if (isNormalizedURL) {
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
                try {
                    normalizedURL = fo.getURL();
                    this.normalizedURLCache.put (url, normalizedURL);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(e);
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
    private static boolean isNormalizedURL (URL url) {
        if ("jar".equals(url.getProtocol())) { //NOI18N
            url = FileUtil.getArchiveFile(url);
        }
        return "file".equals(url.getProtocol());    //NOI18N
    }



    /**
     * Tests if the query accepts the root as valid JavadocRoot,
     * the query accepts the JavaDoc root, if it can find the index-files
     * or index-all.html in the root.
     * @param rootURL the javadoc root
     * @return true if the root is a valid Javadoc root
     */
    static boolean isValidLibraryJavadocRoot (final URL rootURL) {
        assert rootURL != null && rootURL.toExternalForm().endsWith("/");
        final FileObject root = URLMapper.findFileObject(rootURL);
        if (root == null) {
            return false;
        }
        return findIndexFolder (root,1) != null;
    }

    /**
     * Search for the actual root of the Javadoc containing the index-all.html or
     * index-files. In case when it is not able to find it, it returns the given Javadoc folder/file.
     * @param URL Javadoc folder/file
     * @return URL either the URL of folder containg the index or the given parameter if the index was not found.
     */
    private static URL getIndexFolder (URL rootURL) {
        if (rootURL == null) {
            return null;
        }
        FileObject root = URLMapper.findFileObject(rootURL);
        if (root == null) {
            return rootURL;
        }
        FileObject result = findIndexFolder (root,1);
        try {
            return result == null ? rootURL : result.getURL();
        } catch (FileStateInvalidException e) {
            ErrorManager.getDefault().notify(e);
            return rootURL;
        }
    }

    private static FileObject findIndexFolder (FileObject fo, int depth) {
        if (depth > MAX_DEPTH) {
            return null;
        }
        if (fo.getFileObject("index-files",null)!=null || fo.getFileObject("index-all.html",null)!=null) {  //NOI18N
            return fo;
        }
        for (FileObject child : fo.getChildren()) {
            if (child.isFolder()) {
                FileObject result = findIndexFolder(child, depth+1);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

}
