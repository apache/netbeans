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

package org.netbeans.modules.java.platform.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Implementation of Javadoc query for the platform.
 * @author Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation.class, position=150)
public class PlatformJavadocForBinaryQuery implements JavadocForBinaryQueryImplementation {

    private static final Logger LOG = Logger.getLogger(PlatformJavadocForBinaryQuery.class.getName());

    private static final int STATE_ERROR = -1;
    private static final int STATE_START = 0;
    private static final int STATE_DOCS = 1;
    private static final int STATE_LAN = 2;
    private static final int STATE_API = 3;
    private static final int STATE_INDEX = 4;

    private static final String NAME_DOCS = "docs"; //NOI18N
    private static final String NAME_API = "api";   //NOI18N
    private static final String NAME_IDNEX ="index-files";  //NOI18N

    /** Default constructor for lookup. */
    public PlatformJavadocForBinaryQuery() {
    }
    
    @Override
    public JavadocForBinaryQuery.Result findJavadoc(@NonNull final URL binaryRoot) {
        final Collection<JavaPlatform> candidates = new ArrayDeque<JavaPlatform>();
        for (JavaPlatform jp : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            for (ClassPath.Entry entry : jp.getBootstrapLibraries().entries()) {
                if (binaryRoot.equals(entry.getURL())) {
                    candidates.add(jp);
                }
            }
            for (ClassPath.Entry entry : jp.getSourceFolders().entries()) {
                if (binaryRoot.equals(entry.getURL())) {
                    candidates.add(jp);
                }
            }
        }
        return candidates.isEmpty() ? null : new R(candidates);
    }

    static final class R implements JavadocForBinaryQuery.Result, PropertyChangeListener {

        //@GuardedBy("R.class")
        private static Set<String> locales;

        private final Iterable<? extends JavaPlatform> platforms;
        private final ChangeSupport cs = new ChangeSupport(this);
        //@GuardedBy("this")
        private URL[] cachedRoots;

        public R (@NonNull final Iterable<? extends JavaPlatform> platforms) {
            Parameters.notNull("platforms", platforms); //NOI18N
            this.platforms = platforms;
            for (JavaPlatform platform : platforms) {
                platform.addPropertyChangeListener (WeakListeners.propertyChange(this,platform));
            }
        }

        @Override
        @NonNull
        public synchronized URL[] getRoots() {
            if (this.cachedRoots == null) {
                final List<URL> l = new ArrayList<URL>();
                for (JavaPlatform platform : platforms) {
                    final List<? extends URL> javadoc = platform.getJavadocFolders();
                    if (!javadoc.isEmpty()) {
                        for (URL u : javadoc) {
                            if (u != null) {
                                FileObject root = URLMapper.findFileObject(u);
                                if (root == null) {
                                    //Non existing
                                    l.add (u);
                                }
                                else if (root.isFolder()) {
                                    //Has to be folder
                                    try {
                                        l.add(getIndexFolder(root));
                                    } catch (FileStateInvalidException e) {
                                        Exceptions.printStackTrace(e);
                                    }
                                } else {
                                    LOG.log(
                                        Level.WARNING,
                                        "Ignoring non folder root: {0}",    //NOI18N
                                        FileUtil.getFileDisplayName(root));
                                }
                            }
                        }
                        break;
                    }
                }
                this.cachedRoots = l.toArray(new URL[0]);
            }
            return this.cachedRoots;
        }

        @Override
        public synchronized void addChangeListener(ChangeListener l) {
            assert l != null : "Listener can not be null";      //NOI18N
            cs.addChangeListener(l);
        }

        @Override
        public synchronized void removeChangeListener(ChangeListener l) {
            assert l != null : "Listener can not be null";  //NOI18N
            cs.removeChangeListener(l);
        }

        @Override
        public void propertyChange (PropertyChangeEvent event) {
            if (JavaPlatform.PROP_JAVADOC_FOLDER.equals(event.getPropertyName())) {
                synchronized (this) {
                    this.cachedRoots = null;
                }
                cs.fireChange();
            }
        }


        /**
         * Search for the actual root of the Javadoc containing the index-all.html or
         * index-files. In case when it is not able to find it, it returns the given Javadoc folder/file.
         * @param URL Javadoc folder/file
         * @return URL either the URL of folder containg the index or the given parameter if the index was not found.
         */
        private static URL getIndexFolder (FileObject root) throws FileStateInvalidException {
            FileObject result = findIndexFolder (root);
            return result == null ? root.toURL() : result.toURL();
        }

        //Package private, used by tests
        /*private*/ static FileObject findIndexFolder (FileObject fo) {
            int state = STATE_START;
            while (state != STATE_ERROR && state != STATE_INDEX) {
                switch (state) {
                    case STATE_START:
                        {
                            FileObject tmpFo = fo.getFileObject(NAME_DOCS);
                            if (tmpFo != null) {
                                fo = tmpFo;
                                state = STATE_DOCS;
                                break;
                            }
                            tmpFo = fo.getFileObject(NAME_API);
                            if (tmpFo != null) {
                                fo = tmpFo;
                                state = STATE_API;
                                break;
                            }
                            tmpFo = getLocalization (fo);
                            if (tmpFo != null) {
                                fo = tmpFo;
                                state = STATE_LAN;
                                break;

                            }
                            fo = null;
                            state = STATE_ERROR;
                            break;
                        }
                    case STATE_DOCS:
                        {
                            FileObject tmpFo = fo.getFileObject(NAME_API);
                            if (tmpFo != null) {
                                fo = tmpFo;
                                state = STATE_API;
                                break;
                            }
                            tmpFo = getLocalization (fo);
                            if (tmpFo != null) {
                                fo = tmpFo;
                                state = STATE_LAN;
                                break;
                            }

                            fo = null;
                            state = STATE_ERROR;
                            break;
                        }
                    case STATE_LAN:
                        {
                            FileObject tmpFo = fo.getFileObject(NAME_API);
                            if (tmpFo != null) {
                                fo = tmpFo;
                                state = STATE_API;
                                break;
                            }
                            fo = null;
                            state = STATE_ERROR;
                            break;
                        }
                    case STATE_API:
                        {
                            FileObject tmpFo = fo.getFileObject(NAME_IDNEX);
                            if (tmpFo !=null) {
                                state = STATE_INDEX;
                                break;
                            }
                            fo = null;
                            state = STATE_ERROR;
                            break;
                        }
                }
            }
            return fo;
        }

        private static FileObject getLocalization (final FileObject root)  {
            final FileObject[] children = root.getChildren();
            if (children.length == 0) {
                return null;
            }
            else if (children.length == 1) {
                return children[0];
            }
            else {
                final Set<FileObject> candidates = new HashSet<FileObject>();
                for (FileObject fo : children) {
                    if (!fo.isFolder()) {
                        continue;
                    }
                    if (fo.getName().charAt(0) == '.') { //Hidden, ignore
                        continue;
                    }
                    candidates.add(fo);
                }
                if (candidates.isEmpty()) {
                    return null;
                }
                else if (candidates.size() == 1) {
                    return candidates.iterator().next();
                }
                else {
                    //Slow, especially first call
                    final Set<String> locales = getLocales();
                    for (FileObject fo : candidates) {
                        if (locales.contains(fo.getName())) {
                            return fo;
                        }
                    }
                    return null;
                }
            }
        }

        private static synchronized Set<String> getLocales () {
            if (locales == null) {
                final Locale[] locs = Locale.getAvailableLocales();
                locales = new HashSet<String>();
                for (Locale l : locs) {
                    locales.add (l.toString());
                }
            }
            return locales;
        }
    }
    
}
