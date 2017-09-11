/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seplatform.queries;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Zezula
 */
final class QueriesCache<T extends QueriesCache.ResultBase> {

    private static final Logger LOG = Logger.getLogger(QueriesCache.class.getName());

    private static QueriesCache<Javadoc> javadoc;
    private static QueriesCache<Sources> sources;
    private static final char SEP = '-';  //NOI18N

    private final Object lck = new Object();
    private final Class<T> clazz;
    //@GuardedBy("lck")
    private Map<URL,T> cache;

    private QueriesCache(@NonNull final Class<T> clazz){
        assert clazz != null;
        this.clazz = clazz;
    }

    @NonNull
    Map<URL,? extends T> getRoots() {
        return Collections.unmodifiableMap(loadRoots());
    }

    void updateRoot(final URL binaryRoot, final URL... rootsToAttach) {
        T currentMapping = null;
        synchronized (lck) {
            final Map<URL,T> currentRoots = loadRoots();
            currentMapping = currentRoots.get(binaryRoot);
            final Preferences root = NbPreferences.forModule(QueriesCache.class);
            final Preferences node = root.node(clazz.getSimpleName());
            final String binaryRootStr = binaryRoot.toExternalForm();
            try {
                for (String key : filterKeys(node.keys(),binaryRootStr)) {
                    node.remove(key);
                }
                for (int i=0; i < rootsToAttach.length; i++) {
                    node.put(String.format("%s-%d",binaryRootStr,i), rootsToAttach[i].toExternalForm());
                }
                node.flush();
            } catch (BackingStoreException bse) {
                Exceptions.printStackTrace(bse);
            }
            if (currentMapping == null) {
                try {
                    currentMapping = clazz.newInstance();
                    currentRoots.put(binaryRoot, currentMapping);
                } catch (InstantiationException ie) {
                    Exceptions.printStackTrace(ie);
                } catch (IllegalAccessException iae) {
                    Exceptions.printStackTrace(iae);
                }
            }
        }
        if (currentMapping != null) {
            currentMapping.update(Arrays.asList(rootsToAttach));
        }
    }

    @NonNull
    private Map<URL,T> loadRoots() {
        synchronized(lck) {
           if (cache == null) {
               Map<URL,T> result = new HashMap<URL, T>();
               final Preferences root = NbPreferences.forModule(QueriesCache.class);
               final String folder = clazz.getSimpleName();
               try {
                   if (root.nodeExists(folder)) {
                       final Preferences node = root.node(folder);
                       Map<URL,List<URL>> bindings = new HashMap<URL, List<URL>>();
                       for (String key : node.keys()) {
                           final String value = node.get(key, null);
                           if (value != null) {
                               final URL binUrl = getURL(key);
                               List<URL> binding = bindings.get(binUrl);
                               if(binding == null) {
                                   binding = new ArrayList<URL>();
                                   bindings.put(binUrl,binding);
                               }
                               binding.add(new URL(value));
                           }
                       }
                       for (Map.Entry<URL,List<URL>> e : bindings.entrySet()) {
                           final T instance = clazz.newInstance();
                           instance.update(e.getValue());
                           result.put(e.getKey(), instance);
                       }
                   }
               } catch (BackingStoreException bse) {
                   Exceptions.printStackTrace(bse);
               } catch (MalformedURLException mue) {
                   Exceptions.printStackTrace(mue);
               } catch (InstantiationException ie) {
                   Exceptions.printStackTrace(ie);
               } catch (IllegalAccessException iae) {
                   Exceptions.printStackTrace(iae);
               }
               cache = result;
           }
           return cache;
        }
    }

    private URL getURL(@NonNull final String pattern) throws MalformedURLException {
        final int index = pattern.lastIndexOf(SEP); //NOI18N
        assert index > 0;
        return new URL(pattern.substring(0, index));
    }

    private Iterable<? extends String> filterKeys(
            final String[] keys,
            final String binaryRoot) {
        final List<String> result = new ArrayList<String>();
        for (int i=0; i<keys.length; i++) {
            final int index = keys[i].lastIndexOf(SEP); //NOI18N
            if (index <=0) {
                continue;
            }
            final String root = keys[i].substring(0, index);
            if (root.equals(binaryRoot)) {
                result.add(keys[i]);
            }
        }
        return result;
    }

    @NonNull
    static synchronized QueriesCache<Javadoc> getJavadoc() {
        if (javadoc == null) {
            javadoc = new QueriesCache<Javadoc>(Javadoc.class);
        }
        return javadoc;
    }

    @NonNull
    static synchronized QueriesCache<Sources> getSources() {
        if (sources == null) {
            sources = new QueriesCache<Sources>(Sources.class);
        }
        return sources;
    }

    static abstract class ResultBase {
        private final ChangeSupport cs = new ChangeSupport(this);

        public void addChangeListener(@NonNull final ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(@NonNull final ChangeListener l) {
            cs.removeChangeListener(l);
        }

        public final void update(final Collection<? extends URL> roots) {
            updateImpl(roots);
            cs.fireChange();
        }

        protected abstract void updateImpl(final Collection<? extends URL> roots);

        protected abstract List<? extends URI> getRootURIs();
    }

    static class Javadoc extends ResultBase implements JavadocForBinaryQuery.Result {
        private volatile URL[] roots;

        public URL[] getRoots() {
            final URL[] tmp = roots;
            return tmp == null ? new URL[0] : Arrays.copyOf(tmp, tmp.length);
        }

        @Override
        protected void updateImpl(final Collection<? extends URL> roots) {
            this.roots = roots.toArray(new URL[roots.size()]);
        }

        @NonNull
        @Override
        protected List<? extends URI> getRootURIs() {
            final List<URI> result = new ArrayList<>(roots.length);
            for (URL root : roots) {
                try {
                    result.add(root.toURI());
                } catch (URISyntaxException ex) {
                    LOG.log(
                        Level.WARNING,
                        "Cannot convert: {0} to URI.",  //NOI18N
                        root);
                }
            }
            return Collections.unmodifiableList(result);
        }
    }

    static class Sources extends ResultBase implements SourceForBinaryQueryImplementation2.Result {
        private volatile FileObject[] roots;

        @Override
        public boolean preferSources() {
            return false;
        }

        @Override
        public FileObject[] getRoots() {
            final FileObject[] tmp = roots;
            return tmp == null ? new FileObject[0]: Arrays.copyOf(tmp, tmp.length);
        }

        @Override
        protected void updateImpl(final Collection<? extends URL> roots) {
            //Todo: replace by classpath to handle fo listening
            final List<FileObject> fos = new ArrayList<FileObject>(roots.size());
            for (URL url : roots) {
                final FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    fos.add(fo);
                }
            }
            this.roots = fos.toArray(new FileObject[fos.size()]);
        }

        @NonNull
        @Override
        protected List<? extends URI> getRootURIs() {
            final List<URI> result = new ArrayList<>(roots.length);
            for (FileObject root : roots) {
                final URI uri = root.toURI();
                if (uri != null) {
                    result.add(uri);
                } else {
                    LOG.log(
                        Level.WARNING,
                        "Cannot convert: {0} to URI.",  //NOI18N
                        FileUtil.getFileDisplayName(root));
                }
            }
            return Collections.unmodifiableList(result);
        }
    }
}
