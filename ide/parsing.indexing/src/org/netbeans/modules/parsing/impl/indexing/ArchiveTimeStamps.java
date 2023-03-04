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

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
final class ArchiveTimeStamps {

    /*test*/ static final int SAVE_DELAY = 2500;
    private static final String ARCHIVE_TIME_STAMPS = "archives.properties";   //NOI18N
    private static final String PROP_BINARY_INDEXER_STATE = "ArchiveTimeStamps.indexerState";   //NOI18N
    private static final Pair<Long,Map<Pair<String,Integer>,Integer>> FORCE_RESCAN =
            Pair.<Long,Map<Pair<String,Integer>,Integer>>of(0L,Collections.<Pair<String,Integer>,Integer>emptyMap());
    private static final String SEPARATOR = ":";  //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(ArchiveTimeStamps.class.getName(), 1, false, false);
    private static final Saver saver = new Saver();
    private static final RequestProcessor.Task saverTask = RP.create(saver);
    private static final Logger LOG = Logger.getLogger(ArchiveTimeStamps.class.getName());
    //@GuardedBy("ArchiveTimeStamps.class")
    private static Reference<? extends Store> cachedStore;

    private ArchiveTimeStamps() {}

    @NonNull
    static Pair<Long,Map<Pair<String,Integer>,Integer>> getLastModified(
        @NonNull final URL archiveFile) throws IOException {
        Parameters.notNull("archiveFile", archiveFile); //NOI18N
        final Store store = getStore();
        assert store != null;
        return parse(store.get(archiveFile.toExternalForm()));

    }

    static void setLastModified(
        @NonNull final URL archiveFile,
        @NonNull final Pair<Long,Map<Pair<String,Integer>,Integer>> timeStamp) throws IOException {
        Parameters.notNull("archiveFile", archiveFile); //NOI18N
        Parameters.notNull("timeStamp", timeStamp); //NOI18N
        final Store store = getStore();
        assert store != null;
        store.put(archiveFile.toExternalForm(), generate(timeStamp));
        saver.getAndSetPropertiesToSave(store);
        saverTask.schedule(SAVE_DELAY);
    }

    static int getIndexerState(
            @NonNull final Context ctx) {
        final Object res = SPIAccessor.getInstance().getProperty(ctx, PROP_BINARY_INDEXER_STATE);
        return res instanceof Integer ? (Integer) res : 0;
    }

    static void setIndexerState(
            @NonNull final Context ctx,
            final int state) {
        SPIAccessor.getInstance().putProperty(ctx, PROP_BINARY_INDEXER_STATE, state);
    }

    @NonNull
    private static Pair<Long,Map<Pair<String,Integer>,Integer>> parse(@NullAllowed final String record) {
        if (record == null) {
            return FORCE_RESCAN;
        }
        final Map<Pair<String,Integer>,Integer> indexers = new HashMap<Pair<String,Integer>,Integer>();
        long timeStamp = 0L;
        final StringTokenizer tk = new StringTokenizer(record,SEPARATOR);
        do {
            final String first = tk.hasMoreTokens() ? tk.nextToken() : null;
            if (first == null) {
                //Broken record -> not up to date
                return FORCE_RESCAN;
            }
            final String second = tk.hasMoreTokens() ? tk.nextToken() : null;
            if (second != null) {
                final String third = tk.hasMoreTokens() ? tk.nextToken() : null;
                if (third == null) {
                    //Broken record -> not up to date
                    return FORCE_RESCAN;
                }
                try {
                    int ver = Integer.parseInt(second);
                    int state = Integer.parseInt(third);
                    indexers.put(Pair.<String,Integer>of(first, ver),state);
                } catch (NumberFormatException nfe) {
                    //Broken record -> not up to date
                    return FORCE_RESCAN;
                }
            } else {
                try {
                    timeStamp = Long.parseLong(first);
                } catch (NumberFormatException nfe) {
                    //Broken record -> not up to date
                    return FORCE_RESCAN;
                }
            }
        } while (tk.hasMoreElements());
        return Pair.<Long,Map<Pair<String,Integer>,Integer>>of(timeStamp,indexers);
    }

    @NonNull
    private static String generate(Pair<Long,Map<Pair<String,Integer>,Integer>> data) {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<Pair<String,Integer>,Integer> r : data.second().entrySet()) {
            final Pair<String,Integer> key = r.getKey();
            assert key.first().indexOf(SEPARATOR) < 0;
            sb.append(key.first());
            sb.append(SEPARATOR);
            sb.append(key.second());
            sb.append(SEPARATOR);
            sb.append(r.getValue());
            sb.append(SEPARATOR);
        }
        sb.append(data.first());
        return sb.toString();
    }

    /**
     * Returns possibly cached  {@link EditableProperties}.
     * @return the {@link EditableProperties}
     * @throws IOException when properties cannot be loaded.
     */
    @NonNull
    private static synchronized Store getStore () throws IOException {
        Store store = cachedStore == null ? null : cachedStore.get();
        if (store == null) {
            store = new Store();
            final FileObject file = getArchiveTimeStampsFile(false);
            if (file != null) {
                final InputStream in = new BufferedInputStream(file.getInputStream());
                try {
                    store.load(in);
                } finally {
                    in.close();
                }
            }
            cachedStore = new SoftReference<Store>(store);
        }
        return store;
    }

    @CheckForNull
    private static FileObject getArchiveTimeStampsFile(final boolean create) throws IOException {
        final FileObject cacheFolder = CacheFolder.getCacheFolder();
        return create ?
            FileUtil.createData(cacheFolder, ARCHIVE_TIME_STAMPS):
            cacheFolder.getFileObject(ARCHIVE_TIME_STAMPS);
    }

    private static class Saver implements Runnable {

        //@GuardedBy("this")
        private Store toSave;

        @Override
        public void run() {
            final Store store = getAndSetPropertiesToSave(null);
            if (store != null) {
                try {
                    LOG.fine("STORING");             //NOI18N
                    final FileObject file = getArchiveTimeStampsFile(true);
                    final FileLock lock = file.lock();
                    try {
                        final OutputStream out = new BufferedOutputStream(file.getOutputStream(lock));
                        try {
                            store.store(out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                    LOG.fine("STORED");             //NOI18N
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }

        @CheckForNull
        public synchronized Store getAndSetPropertiesToSave(@NullAllowed final Store store) {
            assert store == null || toSave == null || store == toSave;
            final Store old = toSave;
            toSave = store;
            return old;
        }
    }

    private static final class Store {

        private final Object lock;
        //@GuardedBy("lock")
        private final EditableProperties properties;

        public Store() {
            lock = new Object();
            properties = new EditableProperties(true);
        }

        public void put(
            @NonNull final String key,
            @NonNull final String value) {
            synchronized (lock) {
                properties.put(key, value);
            }
        }

        @CheckForNull
        public String get(
            @NonNull final String key) {
            synchronized (lock) {
                return properties.get(key);
            }
        }

        public void load(
            @NonNull final InputStream in) throws IOException {
            synchronized (lock) {
                properties.load(in);
            }
        }

        public void store(
            @NonNull final OutputStream out) throws IOException {
            synchronized (lock) {
                properties.store(out);
            }
        }
    }
}
