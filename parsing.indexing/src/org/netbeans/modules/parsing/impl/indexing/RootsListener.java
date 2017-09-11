/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
final class RootsListener {

    private static final Logger LOG = Logger.getLogger(RootsListener.class.getName());
    private static final boolean USE_RECURSIVE_LISTENERS = Util.getSystemBoolean("netbeans.indexing.recursiveListeners", true); //NOI18N
    private static volatile boolean useAsyncListneres = Util.getSystemBoolean("netbeans.indexing.asyncListeners", true); //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(RootsListener.class);


    private FileChangeListener sourcesListener;
    private FileChangeListener binariesListener;
    private final Map<URL, File> sourceRoots = new HashMap<>();
    private final Map<URL, Pair<File, Boolean>> binaryRoots = new HashMap<>();
    private volatile boolean listens;

    private RootsListener() {
    }

    void setListener(
            final FileChangeListener sourcesListener,
            final FileChangeListener binariesListener) {
        assert (sourcesListener != null && binariesListener != null) || (sourcesListener == null && binariesListener == null) :
            "Both sourcesListener and binariesListener must either be null or non-null"; //NOI18N

        //todo: remove removeRecursiveListener from synchronized block
        synchronized (this) {
            if (sourcesListener != null) {
                assert this.sourcesListener == null : "Already using " + this.sourcesListener + "and " + this.binariesListener //NOI18N
                        + ", won't attach " + sourcesListener + " and " + binariesListener; //NOI18N
                assert sourceRoots.isEmpty() : "Expecting no source roots: " + sourceRoots; //NOI18N
                assert binaryRoots.isEmpty() : "Expecting no binary roots: " + binaryRoots; //NOI18N

                this.sourcesListener = sourcesListener;
                this.binariesListener = binariesListener;
                if (!USE_RECURSIVE_LISTENERS) {
                    FileUtil.addFileChangeListener(this.sourcesListener);
                }
                listens = true;
            } else {
                assert this.sourcesListener != null : "RootsListeners are already dormant"; //NOI18N

                if (!USE_RECURSIVE_LISTENERS) {
                    FileUtil.removeFileChangeListener(this.sourcesListener);
                }
                for(Map.Entry<URL, File> entry : sourceRoots.entrySet()) {
                    safeRemoveRecursiveListener(this.sourcesListener, entry.getValue());
                }
                sourceRoots.clear();
                for(Map.Entry<URL, Pair<File, Boolean>> entry : binaryRoots.entrySet()) {
                    if (entry.getValue().second()) {
                        safeRemoveFileChangeListener(this.binariesListener, entry.getValue().first());
                    } else {
                        safeRemoveRecursiveListener(this.binariesListener, entry.getValue().first());
                    }
                }
                binaryRoots.clear();
                this.sourcesListener = null;
                this.binariesListener = null;
                listens = false;
            }
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_COLLECTION_OF_URLS",
    justification="URLs have never host part")
    synchronized boolean addBinary(@NonNull final URL root) {
        if (binariesListener != null && !binaryRoots.containsKey(root)) {
            File f = null;
            final URL archiveUrl = FileUtil.getArchiveFile(root);
            try {
                final URI uri = archiveUrl != null ? archiveUrl.toURI() : root.toURI();
                if (uri.getScheme().equals("file")) { //NOI18N
                    f = BaseUtilities.toFile(uri);
                }
            } catch (URISyntaxException use) {
                LOG.log (
                    Level.INFO,
                    "Can't convert: {0} to java.io.File, due to: {1}, (archive url: {2}).", //NOI18N
                    new Object[]{
                        root,
                        use.getMessage(),
                        archiveUrl
                    });
            }
            if (f != null) {
                if (archiveUrl != null) {
                    // listening on an archive file
                    safeAddFileChangeListener(binariesListener, f);
                } else {
                    // listening on a folder
                    safeAddRecursiveListener(binariesListener, f, null);
                }
                binaryRoots.put(root, Pair.of(f, archiveUrl != null));
            }
        }
        return listens;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_COLLECTION_OF_URLS",
    justification="URLs have never host part")
    synchronized boolean addSource(
            @NonNull final URL root,
            @NullAllowed ClassPath.Entry entry) {
        if (sourcesListener != null) {
            if (!sourceRoots.containsKey(root) && root.getProtocol().equals("file")) { //NOI18N
                try {
                    final File f = BaseUtilities.toFile(root.toURI());
                    safeAddRecursiveListener(sourcesListener, f, entry);
                    sourceRoots.put(root, f);
                } catch (URISyntaxException use) {
                    LOG.log(Level.INFO, null, use);
                }
            }
        }
        return listens;
    }


    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_COLLECTION_OF_URLS",
    justification="URLs have never host part")
    synchronized void removeBinaries(@NonNull final Iterable<? extends URL> roots) {
        for (URL root : roots) {
            if (binariesListener != null) {
                final Pair<File, Boolean> pair = binaryRoots.remove(root);
                if (pair != null) {
                    if (pair.second()) {
                        safeRemoveFileChangeListener(binariesListener, pair.first());
                    } else {
                        safeRemoveRecursiveListener(binariesListener, pair.first());
                    }
                }
            }
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_COLLECTION_OF_URLS",
    justification="URLs have never host part")
    synchronized void removeSources(
            @NonNull final Iterable<? extends URL> roots) {
        for (URL root : roots) {
            if (sourcesListener != null) {
                File f = sourceRoots.remove(root);
                if (f != null) {
                    safeRemoveRecursiveListener(sourcesListener, f);
                }
            }
        }
    }

    boolean hasRecursiveListeners() {
        return USE_RECURSIVE_LISTENERS;
    }

    @NonNull
    static RootsListener newInstance() {
        return new RootsListener();
    }

    /*test*/ static void setUseAsyncListneres(final boolean asyncListeners) {
        useAsyncListneres = asyncListeners;
    }

    private void safeAddFileChangeListener(
            @NonNull final FileChangeListener listener,
            @NonNull final File path) {
        performSave(() -> {
                FileUtil.addFileChangeListener(listener, path);
                return null;
            });
    }

    private static void safeRemoveFileChangeListener(
            @NonNull final FileChangeListener listener,
            @NonNull final File path) {
        performSave(() -> {
                FileUtil.removeFileChangeListener(listener, path);
                return null;
            });
    }

    private void safeAddRecursiveListener(
            @NonNull final FileChangeListener listener,
            @NonNull final File path,
            @NullAllowed final ClassPath.Entry entry) {
        if (USE_RECURSIVE_LISTENERS) {
            final FileFilter filter = entry == null?
                null:
                (pathname) -> {
                    try {
                        return entry.includes(BaseUtilities.toURI(pathname).toURL());
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                        return true;
                    }
                };
            performAsync(() -> {
                    FileUtil.addRecursiveListener(
                        listener,
                        path,
                        filter,
                        () -> !listens);
                    return null;
                });
        }
    }

    private static void safeRemoveRecursiveListener(
            @NonNull final FileChangeListener listener,
            @NonNull final File path) {
        if (USE_RECURSIVE_LISTENERS) {
            performAsync(() -> {
                    FileUtil.removeRecursiveListener(listener, path);
                    return null;
                });
        }
    }

    private static <T> void performAsync(@NonNull final Callable<T> action) {
        if (useAsyncListneres) {
            RP.execute(() -> performSave(action));
        } else {
            performSave(action);
        }
    }

    private static <T> T performSave(@NonNull final Callable<T> action) {
        try {
            return action.call();
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable e) {
            // ignore
            LOG.log(Level.FINE, null, e);
            return null;
        }
    }
}
