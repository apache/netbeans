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
package org.netbeans.modules.parsing.ui.indexing.errors;

import java.awt.Image;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.impl.indexing.implspi.FileAnnotationsRefresh;
import org.netbeans.modules.parsing.impl.indexing.errors.TaskCache;
import org.netbeans.modules.parsing.impl.indexing.errors.Utilities;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

import static org.openide.util.ImageUtilities.assignToolTipToImage;
import static org.openide.util.ImageUtilities.loadImage;
import static org.openide.util.ImageUtilities.mergeImages;
import static org.openide.util.NbBundle.getMessage;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Jan Lahoda
 */
@ServiceProviders({
@ServiceProvider(service=FileAnnotationsRefresh.class, position=100),
@ServiceProvider(service=org.netbeans.modules.masterfs.providers.AnnotationProvider.class, position=100)
})
public class ErrorAnnotator extends AnnotationProvider implements FileAnnotationsRefresh {

    private static final Logger LOG = Logger.getLogger(ErrorAnnotator.class.getName());

    @StaticResource
    private static final String ERROR_BADGE_URL = "org/netbeans/modules/parsing/ui/resources/error-badge.gif";

    public ErrorAnnotator() {
    }

    @Override
    public String annotateName(String name, Set files) {
        return null;
    }

    @Override
    public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
        if (!Utilities.isBadgesEnabled()) {
            return null;
        }
        boolean inError = false;
        boolean singleFile = files.size() == 1;

        if (files instanceof NonRecursiveFolder) {
            FileObject folder = ((NonRecursiveFolder) files).getFolder();
            inError = isInError(folder, false, true);
            singleFile = false;
        } else {
            for (Object o : files) {
                if (o instanceof FileObject) {
                    FileObject f = (FileObject) o;

                    if (f.isFolder()) {
                        singleFile = false;
                        if (isInError(f, true, !inError)) {
                            inError = true;
                            continue;
                        }
                        if (inError)
                            continue;
                    } else {
                        if (f.isData()) {
                            if (isInError(f, true, !inError)) {
                                inError = true;
                            }
                        }
                    }
                }
            }
        }

        if (Logger.getLogger(ErrorAnnotator.class.getName()).isLoggable(Level.FINE)) {
            Logger.getLogger(ErrorAnnotator.class.getName()).log(Level.FINE, "files={0}, in error={1}", new Object[] {files, inError});
        }

        if (inError) {
            //badge:
            URL errorBadgeIconURL = ErrorAnnotator.class.getResource("/" + ERROR_BADGE_URL);
            assert errorBadgeIconURL != null;
            String errorBadgeSingleTP = "<img src=\"" + errorBadgeIconURL + "\">&nbsp;" + getMessage(ErrorAnnotator.class, "TP_ErrorBadgeSingle");
            Image errorBadge = loadImage(ERROR_BADGE_URL);
            assert errorBadge != null;
            String errorBadgeFolderTP = "<img src=\"" + errorBadgeIconURL + "\">&nbsp;" + getMessage(ErrorAnnotator.class, "TP_ErrorBadgeFolder");
            Image i = mergeImages(icon, singleFile ? assignToolTipToImage(errorBadge, errorBadgeSingleTP) : assignToolTipToImage(errorBadge, errorBadgeFolderTP), 0, 8);
            Iterator<? extends AnnotationProvider> it = Lookup.getDefault().lookupAll(AnnotationProvider.class).iterator();
            boolean found = false;

            while (it.hasNext()) {
                AnnotationProvider p = it.next();
                if (found) {
                    Image res = p.annotateIcon(i, iconType, files);
                    if (res != null) {
                        return res;
                    }
                } else {
                    found = p == this;
                }
            }
            return i;
        }
        return null;
    }

    @Override
    public String annotateNameHtml(String name, Set files) {
        return null;
    }

    @Override
    public Action[] actions(Set files) {
        return null;
    }

    @Override
    public InterceptionListener getInterceptionListener() {
        return null;
    }

    public void updateAllInError() {
        try {
            File[] roots = File.listRoots();
            for (File root : roots) {
                FileObject rootFO = FileUtil.toFileObject(root);

                if (rootFO != null) {
                    fireFileStatusChanged(new FileStatusEvent(rootFO.getFileSystem(), true, false));
                }
            }
        } catch (FileStateInvalidException ex) {
            LOG.log(Level.INFO, ex.getMessage(), ex);
        }
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_COLLECTION_OF_URLS",
    justification="URLs have never host part")
    public synchronized void refresh(@NonNull final Set<URL> urls)  {
        Set<FileObject> toRefresh = new HashSet<>();
        for (Iterator<FileObject> it = knownFiles2Error.keySet().iterator(); it.hasNext(); ) {
            FileObject f = it.next();
            final URL furl = f.toURL();
            assert PathRegistry.noHostPart(furl) : furl;
            if (urls.contains(furl)) {
                toRefresh.add(f);
                Integer i = knownFiles2Error.get(f);

                if (i != null) {
                    knownFiles2Error.put(f, i | INVALID);

                    enqueue(f);
                }
            }
        }
    }

    public void fireFileStatusChanged(Set<FileObject> fos) {
        if (fos.isEmpty())
            return ;
        try {
            fireFileStatusChanged(new FileStatusEvent(fos.iterator().next().getFileSystem(), fos, true, false));
        } catch (FileStateInvalidException ex) {
            LOG.log(Level.INFO, ex.getMessage(), ex);
        }
    }

    private static final int IN_ERROR_REC = 1;
    private static final int IN_ERROR_NONREC = 2;
    private static final int INVALID = 4;
    private Map<FileObject, Integer> knownFiles2Error = new WeakHashMap<FileObject, Integer>();

    private void enqueue(FileObject file) {
        if (toProcess == null) {
            toProcess = new LinkedList<FileObject>();
            WORKER.schedule(50);
        }
        toProcess.add(file);
    }

    private synchronized boolean isInError(FileObject file, boolean recursive, boolean forceValue) {
        boolean result = false;
        Integer i = knownFiles2Error.get(file);

        if (i != null) {
            result = (i & (recursive ? IN_ERROR_REC : IN_ERROR_NONREC)) != 0;

            if ((i & INVALID) == 0)
                return result;
        }

        if (!forceValue) {
            if (i == null) {
                knownFiles2Error.put(file, null);
            }
            return result;
        }

        enqueue(file);
        return result;
    }

    private long cumulativeTime;
    private Collection<FileObject> toProcess = null;

    private final RequestProcessor WORKER_THREAD = new RequestProcessor("ErrorAnnotator worker", 1);
    private final RequestProcessor.Task WORKER = WORKER_THREAD.create(new Runnable() {
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            Collection<FileObject> toProcess;

            synchronized (ErrorAnnotator.this) {
                toProcess = ErrorAnnotator.this.toProcess;
                ErrorAnnotator.this.toProcess = null;
            }

            for (FileObject f : toProcess) {
                synchronized (ErrorAnnotator.this) {
                    Integer currentState = knownFiles2Error.get(f);

                    if (currentState != null && (currentState & INVALID) == 0) {
                        continue;
                    }
                }

                ensureListensOnFS(f);

                boolean recError = false;
                boolean nonRecError = false;
                if (f.isData()) {
                    recError = nonRecError = TaskCache.getDefault().isInError(f, true);
                } else {
                    ClassPath source = Utilities.getSourceClassPathFor (f);

                    if (source == null) {
                        //presumably not under an indexed root:
                        Project p = FileOwnerQuery.getOwner(f);

                        if (p != null) {
                            for (FileObject root : Utilities.findIndexedRootsUnderDirectory(p, f)) {
                                recError |= TaskCache.getDefault().isInError(root, true);
                            }
                        }
                    } else {
                        recError = TaskCache.getDefault().isInError(f, true);
                        nonRecError = TaskCache.getDefault().isInError(f, false);
                    }
                }

                Integer value = (recError ? IN_ERROR_REC : 0) | (nonRecError ? IN_ERROR_NONREC : 0);
                boolean stateChanged;

                synchronized (ErrorAnnotator.this) {
                    Integer origInteger = knownFiles2Error.get(f);
                    int orig;

                    if (origInteger != null) {
                        orig = origInteger & ~INVALID;
                    } else {
                        orig = 0;
                    }

                    stateChanged = orig != value;
                    knownFiles2Error.put(f, value);
                }

                if (stateChanged) {
                    fireFileStatusChanged(Collections.singleton(f));
                }
            }

            long endTime = System.currentTimeMillis();
            if (Logger.getLogger(ErrorAnnotator.class.getName()).isLoggable(Level.FINE)) {
                Logger.getLogger(ErrorAnnotator.class.getName()).log(Level.FINE, "time spent in error annotations computation: {0}, cumulative time: {1}", new Object[] {(endTime - startTime), (cumulativeTime += (endTime - startTime))});
            }
        }
    });

    private Map<FileSystem, FileChangeListener> system2RecursiveListener = new WeakHashMap<FileSystem, FileChangeListener>();

    private void ensureListensOnFS(FileObject f) {
        try {
            FileSystem fs = f.getFileSystem();
            if (!system2RecursiveListener.containsKey(fs)) {
                FileChangeListener l = new RootAddedDeletedListener();
                system2RecursiveListener.put(fs, l);
                fs.addFileChangeListener(l);
            }
        } catch (FileStateInvalidException ex) {
            LOG.log(Level.FINE, null, ex);
        }
    }

    private final class RootAddedDeletedListener extends FileChangeAdapter {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            update(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            update(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            update(fe);
        }

        private void update(FileEvent fe) {
            final RepositoryUpdater ru = RepositoryUpdater.getDefault();
            final FileObject fo = fe.getFile();
            if (!ru.isCacheFile(fo) && ru.getOwningSourceRoot(fo) == null) {
                update(fe.getFile().toURL());
            }
        }

        private void update(final URL root) {
            assert PathRegistry.noHostPart(root) : root;
            WORKER_THREAD.post(new Runnable() {

                @Override
                @org.netbeans.api.annotations.common.SuppressWarnings(
                value="DMI_COLLECTION_OF_URLS",
                justification="URLs have never host part")
                public void run() {
                    try {
                        Set<URL> toRefresh = new HashSet<URL>();
                        URL current = root;

                        toRefresh.add(current);
                        toRefresh.add(current = new URL(current, ".")); //NOI18N

                        for (int depth = current.getPath().split("/").length - 1; depth > 0; depth--) {  //NOI18N
                            current = new URL(current, ".."); //NOI18N
                            toRefresh.add(current);
                        }

                        refresh(toRefresh);
                    } catch (MalformedURLException ex) {
                        LOG.log(Level.FINE, null, ex);
                    }
                }
            });
        }
    }
}
