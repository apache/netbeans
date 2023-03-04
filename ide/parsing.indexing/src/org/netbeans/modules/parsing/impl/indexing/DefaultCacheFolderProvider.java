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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.impl.indexing.implspi.CacheFolderProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
public final class DefaultCacheFolderProvider extends CacheFolderProvider {

    private static final int SLIDING_WINDOW = 500;
    private static final String SEGMENTS_FILE = "segments";      //NOI18N
    private static final String SLICE_PREFIX = "s";              //NOI18N
    private static final Logger LOG = Logger.getLogger(DefaultCacheFolderProvider.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(
            DefaultCacheFolderProvider.class.getName(),
            1,
            false,
            false);
    private static final DefaultCacheFolderProvider INSTANCE = new DefaultCacheFolderProvider();

    private final RequestProcessor.Task saver;

    //@GuardedBy("this")
    private FileObject cacheFolder;
    //@GuardedBy("this")
    private Properties segments;
    //@GuardedBy("this")
    private Map<String, String> invertedSegments;
    //@GuardedBy("this")
    private int index = 0;

    private DefaultCacheFolderProvider() {
        saver = RP.create(new Saver());
    }

    @Override
    @CheckForNull
    protected FileObject findCacheFolderForRoot(
            @NonNull final URL root,
            @NonNull final Set<CacheFolderProvider.Kind> kinds,
            @NonNull final CacheFolderProvider.Mode mode) throws IOException {
        final String rootName = root.toExternalForm();
        final FileObject _cacheFolder = getCacheFolder();
        String slice;
        synchronized (this) {
            loadSegments(_cacheFolder);
            slice = invertedSegments.get (rootName);
            if (slice == null) {
                if (mode == Mode.EXISTENT) {
                    return null;
                }
                slice = SLICE_PREFIX + (++index);
                while (segments.getProperty(slice) != null) {
                    slice = SLICE_PREFIX + (++index);
                }
                segments.put (slice,rootName);
                invertedSegments.put(rootName, slice);
                saver.schedule(SLIDING_WINDOW);
            }
        }
        assert slice != null;
        if (mode == Mode.EXISTENT) {
            return cacheFolder.getFileObject(slice);
        } else {
            return FileUtil.createFolder(_cacheFolder, slice);
        }
    }

    @Override
    @CheckForNull
    protected synchronized URL findRootForCacheFolder(FileObject folder) throws IOException {
        final FileObject segFolder = folder.getParent();
        if (segFolder == null || !segFolder.equals(cacheFolder)) {
            return null;
        }
        final String source = segments.getProperty(folder.getName());
        if (source != null) {
            return new URL (source);
        }
        return null;
    }

    @Override
    protected void collectRootsInFolder(@NonNull final URL folder, @NonNull final Collection<? super URL> collector) throws IOException {
        final String prefix = folder.toExternalForm();
        final Map<String,String> isdc;
        synchronized (this) {
            final FileObject _cacheFolder = getCacheFolder();
            loadSegments(_cacheFolder);
            isdc = new HashMap<>(invertedSegments);
        }
        for (Map.Entry<String, String> e : isdc.entrySet()) {
            if (e.getKey().startsWith(prefix)) {
                collector.add(new URL(e.getKey()));
            }
        }
    }

    public static DefaultCacheFolderProvider getInstance() {
        return INSTANCE;
    }

    //Package private implementation
    @NonNull
    synchronized FileObject getCacheFolder() {
        if (cacheFolder == null) {
            File cache = Places.getCacheSubdirectory("index"); // NOI18N
            if (!cache.isDirectory()) {
                throw new IllegalStateException("Indices cache folder " + cache.getAbsolutePath() + " is not a folder"); //NOI18N
            }
            if (!cache.canRead()) {
                throw new IllegalStateException("Can't read from indices cache folder " + cache.getAbsolutePath()); //NOI18N
            }
            if (!cache.canWrite()) {
                throw new IllegalStateException("Can't write to indices cache folder " + cache.getAbsolutePath()); //NOI18N
            }
            cacheFolder = FileUtil.toFileObject(cache);
            if (cacheFolder == null) {
                throw new IllegalStateException("Can't convert indices cache folder " + cache.getAbsolutePath() + " to FileObject"); //NOI18N
            }
        }
        return cacheFolder;
    }

    //Unit tests methods
    void setCacheFolder(@NonNull final FileObject folder) {
        saver.schedule(0);
        saver.waitFinished();
        synchronized (this) {
            assert folder != null && folder.canRead() && folder.canWrite();
            cacheFolder = folder;
            segments = null;
            invertedSegments = null;
            index = 0;
        }
    }

    //Private Implementations

    //@NotThreadSafe
    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="LI_LAZY_INIT_UPDATE_STATIC",
        justification="Caller already holds a monitor")
    private void loadSegments(@NonNull final FileObject folder) throws IOException {
        assert Thread.holdsLock(this);
        if (segments == null) {
            assert folder != null;
            segments = new Properties ();
            invertedSegments = new HashMap<> ();
            final FileObject segmentsFile =  folder.getFileObject(SEGMENTS_FILE);
            if (segmentsFile!=null) {
                try (final InputStream in = segmentsFile.getInputStream();) {
                    segments.load (in);
                }
            }
            for (Map.Entry entry : segments.entrySet()) {
                String segment = (String) entry.getKey();
                String root = (String) entry.getValue();
                invertedSegments.put(root,segment);
                try {
                    index = Math.max (index,Integer.parseInt(segment.substring(SLICE_PREFIX.length())));
                } catch (NumberFormatException nfe) {
                    LOG.log(Level.FINE, null, nfe);
                }
            }
        }
    }

    private void storeSegments(@NonNull final FileObject folder) throws IOException {
        assert Thread.holdsLock(this);
        assert folder != null;
        //It's safer to use FileUtil.createData(File) than FileUtil.createData(FileObject, String)
        //see issue #173094
        final File _file = FileUtil.toFile(folder);
        assert _file != null;
        final FileObject segmentsFile = FileUtil.createData(new File(_file, SEGMENTS_FILE));
        try (final OutputStream out = segmentsFile.getOutputStream();) {
            segments.store(out,null);
        }
    }

    private class Saver implements Runnable {
        @Override
        public void run() {
            try {
                final FileObject cf = getCacheFolder();
                // #170182 - preventing filesystem events being fired from under the DefaultCacheFolderProvider.this lock
                cf.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                    @Override
                    public void run() throws IOException {
                        synchronized (DefaultCacheFolderProvider.this) {
                            if (segments == null) {
                                return;
                            }
                            storeSegments(cf);
                        }
                    }
                });
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
}
