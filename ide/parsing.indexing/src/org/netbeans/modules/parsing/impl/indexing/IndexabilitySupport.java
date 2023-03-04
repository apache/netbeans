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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.queries.VisibilityQueryChangeEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
class IndexabilitySupport {

    private static final int VISIBILITY_CHANGE_WINDOW = 1000;
    private static final Logger LOGGER = Logger.getLogger(IndexabilitySupport.class.getName());

    //@GuardedBy("visibilityCache")
    private final Map<FileObject,Boolean> visibilityCache = Collections.synchronizedMap(new WeakHashMap<>());
    private final SlidingTask visibilityTask;
    private final RequestProcessor.Task visibilityChanged;

    private final ChangeListener visibilityListener;
    private final ChangeListener indexabilityListener;

    private IndexabilitySupport(
            @NonNull final RepositoryUpdater ru,
            @NonNull final RequestProcessor worker) {
        this.visibilityTask = new SlidingTask(ru);
        this.visibilityChanged = worker.create(this.visibilityTask);

        visibilityListener = (ChangeEvent e) -> {
            visibilityCache.clear();
            if (Crawler.listenOnVisibility()) {
                if (e instanceof VisibilityQueryChangeEvent) {
                    final FileObject[] affectedFiles = ((VisibilityQueryChangeEvent) e).getFileObjects();
                    visibilityTask.localChange(affectedFiles);
                } else {
                    visibilityTask.globalChange();
                }
                visibilityChanged.schedule(VISIBILITY_CHANGE_WINDOW);
            }
        };

        indexabilityListener = (ChangeEvent e) -> {
            // Indexability could invalidate a subset of the indexer,
            // so there is a valid index state for a file, but it might
            // contain more/less data than intended after the change
            visibilityTask.globalChangeFull();
            visibilityChanged.schedule(VISIBILITY_CHANGE_WINDOW);
        };
    }

    void start() {
        VisibilityQuery.getDefault().addChangeListener(visibilityListener);
        IndexabilityQuery.getInstance().addChangeListener(indexabilityListener);
    }

    void stop() {
        VisibilityQuery.getDefault().removeChangeListener(visibilityListener);
        IndexabilityQuery.getInstance().removeChangeListener(indexabilityListener);
    }

    boolean canIndex(
        @NonNull FileObject file,
        @NullAllowed final FileObject root) {
        long st = 0L;
        if (LOGGER.isLoggable(Level.FINER)) {
            st = System.currentTimeMillis();
        }
        try {
            final VisibilityQuery vq = VisibilityQuery.getDefault();
            final IndexabilityQuery iq = IndexabilityQuery.getInstance();
            final Deque<FileObject> fta = new ArrayDeque<>();
            Boolean vote = null;
            boolean folder = false;
            while (root != null && !root.equals(file)) {
                vote = visibilityCache.get(file);
                if (vote != null) {
                    break;
                }
                if (folder || file.isFolder()) {
                    fta.offer(file);
                }
                if ((!vq.isVisible(file)) || iq.preventIndexing(file)) {
                    vote = Boolean.FALSE;
                    break;
                }
                file = file.getParent();
                folder = true;
            }
            if (vote == null) {
                vote = vq.isVisible(file) && (! iq.preventIndexing(file));
                fta.offer(file);
            }
            if (!fta.isEmpty()) {
                synchronized(visibilityCache) {
                    for (FileObject nf : fta) {
                        visibilityCache.put(nf, vote);
                    }
                }
            }
            return vote;
        } finally {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(
                    Level.FINER,
                    "reportVisibilityOverhead: {0}",    //NOI18N
                    (System.currentTimeMillis() - st));
            }
        }
    }

    private static class SlidingTask implements Runnable {

        private final RepositoryUpdater ru;

        //@GuardedBy("this")
        private boolean globalChange;
        private boolean fullScan;
        private final Set</*@GuardedBy("this")*/FileObject> localChanges = new HashSet<>();
        //@GuardedBy("this")
        private  LogContext visibilityLogCtx;

        SlidingTask(@NonNull final RepositoryUpdater ru) {
            this.ru = ru;
        }

        synchronized void globalChangeFull() {
            globalChange = true;
            fullScan = true;

            if (visibilityLogCtx == null) {
                visibilityLogCtx = LogContext.create(LogContext.EventType.FILE, null);
            }
        }

        synchronized void globalChange() {
            globalChange = true;

            if (visibilityLogCtx == null) {
                visibilityLogCtx = LogContext.create(LogContext.EventType.FILE, null);
            }
        }

        synchronized void localChange(final FileObject... onFiles) {
            localChanges.addAll(Arrays.asList(onFiles));
            if (visibilityLogCtx == null) {
                visibilityLogCtx = LogContext.create(LogContext.EventType.FILE, null);
            }
        }

        @Override
        public void run() {
            final boolean global;
            final boolean full;
            final Collection<FileObject> changedFiles;
            final LogContext logCtx;
            synchronized (this) {
                logCtx = visibilityLogCtx;
                visibilityLogCtx = null;
                global = globalChange;
                full = fullScan;
                globalChange = false;
                fullScan = false;
                changedFiles = new ArrayList<>(localChanges);
                localChanges.clear();
            }
            if (global) {
                LOGGER.fine ("VisibilityQuery global changed, reindexing");    //NOI18N
                if(full) {
                    ru.refreshAll(true, false, true, logCtx);
                } else {
                    ru.refreshAll(false, false, true, logCtx);
                }
            } else {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log (
                      Level.FINE,
                      "VisibilityQuery changed for {0}, reindexing these files.",    //NOI18N
                      Arrays.asList(changedFiles));
                }
                final Map<URI, Collection<URL>> srcShownPerRoot = new HashMap<>();
                final Map<URI, Set<String>> srcHiddenPerRoot = new HashMap<>();
                final Set<URI> binChangedRoot = new HashSet<>();
                final Map<URI,TimeStamps> tsPerRoot = new HashMap<>();
                final VisibilityQuery vq = VisibilityQuery.getDefault();
                final IndexabilityQuery iq = IndexabilityQuery.getInstance();
                for (FileObject chf : changedFiles) {
                    Pair<URL,FileObject> owner = ru.getOwningSourceRoot(chf);
                    if (owner != null) {
                        final boolean visible = vq.isVisible(chf)
                                && (! iq.preventIndexing(chf));
                        try {
                            final URI ownerURI = owner.first().toURI();
                            if (visible) {
                                Collection<URL> files = srcShownPerRoot.get(ownerURI);
                                if (files == null) {
                                    files = new ArrayList<>();
                                    srcShownPerRoot.put(ownerURI, files);
                                }
                                if (chf.equals(owner.second())) {
                                    for (FileObject cld : chf.getChildren()) {
                                        files.add(cld.toURL());
                                    }
                                } else {
                                    files.add(chf.toURL());
                                }
                            } else if (owner.second() != null) {
                                Set<String> files = srcHiddenPerRoot.get(ownerURI);
                                if (files == null) {
                                    files = new HashSet<>();
                                    srcHiddenPerRoot.put(ownerURI, files);
                                }
                                if (chf.isFolder()) {
                                    TimeStamps ts = tsPerRoot.get(ownerURI);
                                    if (ts == null) {
                                        ts = TimeStamps.forRoot(owner.first(), false);
                                        tsPerRoot.put(ownerURI, ts);
                                    }
                                    files.addAll(ts.getEnclosedFiles(FileUtil.getRelativePath(owner.second(), chf)));
                                } else {
                                    files.add(FileUtil.getRelativePath(owner.second(), chf));
                                }
                            }
                        } catch (URISyntaxException | IOException e) {
                            Exceptions.printStackTrace(e);
                        }
                        continue;
                    }
                    owner = ru.getOwningBinaryRoot(chf);
                    if (owner != null) {
                        try {
                            final URI ownerURI = owner.first().toURI();
                            binChangedRoot.add(ownerURI);
                        } catch (URISyntaxException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
                for (Map.Entry<URI,Collection<URL>> e : srcShownPerRoot.entrySet()) {
                    try {
                        ru.addIndexingJob(
                            e.getKey().toURL(),
                            e.getValue(),
                            false,
                            false,
                            false,
                            false,
                            true,
                            logCtx);
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                for (Map.Entry<URI,Set<String>> e : srcHiddenPerRoot.entrySet()) {
                    try {
                        ru.addDeleteJob(
                            e.getKey().toURL(),
                            e.getValue(),
                            logCtx);
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                for (URI e : binChangedRoot) {
                    try {
                        ru.addBinaryJob(
                            e.toURL(),
                            logCtx);
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    @NonNull
    static IndexabilitySupport create(
        @NonNull final RepositoryUpdater ru,
        @NonNull final RequestProcessor worker) {
        Parameters.notNull("ru", ru);   //NOI18N
        Parameters.notNull("worker", worker);   //NOI18N
        return new IndexabilitySupport(ru, worker);
    }


}
