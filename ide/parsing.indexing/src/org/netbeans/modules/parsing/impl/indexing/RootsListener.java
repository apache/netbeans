/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
