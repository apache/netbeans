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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.impl.indexing.implspi.CacheFolderProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class TimeStamps {

    // -J-Dorg.netbeans.modules.parsing.impl.indexing.TimeStamps.level=FINE
    private static final Logger LOG = Logger.getLogger(TimeStamps.class.getName());
    private static final String TIME_STAMPS_FILE = "timestamps.properties"; //NOI18N
    private static final String VERSION_2 = "#v2"; //NOI18N
    private static final String VERSION_3 = "#v3"; //NOI18N

    private final Implementation impl;


    private TimeStamps(@NonNull final Implementation impl) throws IOException {
        assert impl != null;
        this.impl = impl;
    }

    public boolean checkAndStoreTimestamp(FileObject f, String relativePath) {
        return impl.checkAndStoreTimestamp(f, relativePath);
    }

    public Set<String> getUnseenFiles() {
        return impl.getUnseenFiles();
    }

    public void store () throws IOException {
        impl.store();
    }

    void resetToNow() {
        final long now = System.currentTimeMillis();
        impl.reset(now);
    }

    void remove(@NonNull final Iterable<? extends String> relativePaths) {
        Parameters.notNull("relativePaths", relativePaths); //NOI18N
        impl.remove(relativePaths);
    }

    @NonNull
    Collection<? extends String> getEnclosedFiles(@NonNull String folder) {
        Parameters.notNull("folder", folder);           //NOI18N
        if (!folder.isEmpty() && folder.charAt(folder.length()-1) != '/') {  //NOI18N
            folder = folder + '/';                      //NOI18N
        }
        Collection<? extends String> res = impl.getEnclosedFiles(folder);
        return res;
    }

    public static TimeStamps forRoot(
            @NonNull final URL root,
            final boolean detectDeletedFiles) throws IOException {
        return new TimeStamps(new RegularImpl(root, detectDeletedFiles));
    }

    public static TimeStamps changedTransient() throws IOException {
        return new TimeStamps(new AllChangedTransientImpl());
    }

    public static boolean existForRoot (final URL root) throws IOException {
        assert root != null;

        FileObject cacheDir = CacheFolder.getDataFolder(
            root,
            EnumSet.of(CacheFolderProvider.Kind.SOURCES, CacheFolderProvider.Kind.LIBRARIES),
            CacheFolderProvider.Mode.EXISTENT);
        if (cacheDir != null) {
            return new File (FileUtil.toFile(cacheDir),TIME_STAMPS_FILE).exists();
        }

        return false;
    }

    private static interface Implementation {
        boolean checkAndStoreTimestamp(FileObject root, String relativePath);
        Set<String> getUnseenFiles();
        void reset(long time);
        void remove(@NonNull Iterable<? extends String> relativePaths);
        @NonNull
        Collection<? extends String> getEnclosedFiles(@NonNull String relativePath);
        void store() throws IOException;
    }

    private static final class RegularImpl implements Implementation {

        private final URL root;
        private final LongHashMap<String> timestamps = new LongHashMap<>();
        private final Set<String> unseen;
        private FileObject rootFoCache;

        private RegularImpl(
                @NonNull final URL root,
                final boolean detectDeletedFiles) throws IOException {
            assert root != null;
            this.root = root;
            this.unseen = detectDeletedFiles ? new HashSet<>() : null;
            load();
        }

        @Override
        public boolean checkAndStoreTimestamp(FileObject f, String relativePath) {
            if (rootFoCache == null) {
                rootFoCache = URLMapper.findFileObject(root);
            }
            final String fileId = relativePath != null ? relativePath : URLMapper.findURL(f, URLMapper.EXTERNAL).toExternalForm();
            if (fileId == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "The fileId == null, relativePath: {0}, FileObject: {1}, URL: {2}, external URL: {3}", //NOI18N
                    relativePath,
                    f,
                    f.toURL().toExternalForm(),
                    URLMapper.findURL(f, URLMapper.EXTERNAL).toExternalForm()));
            }
            long fts = f.lastModified().getTime();
            long lts = timestamps.put(fileId, fts);
            if (lts == LongHashMap.NO_VALUE) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "{0}: lastTimeStamp=null, fileTimeStamp={1} is out of date", new Object [] { f.getPath(), fts }); //NOI18N
                }
                return false;
            }

            if (unseen != null) {
                unseen.remove(fileId);
            }
            boolean isUpToDate = lts == fts;
            if (!isUpToDate) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "{0}: lastTimeStamp={1}, fileTimeStamp={2} is out of date", new Object [] { f.getPath(), lts, fts }); //NOI18N
                }
            }

            return isUpToDate;
        }

        @Override
        public Set<String> getUnseenFiles() {
            return unseen;
        }

        @Override
        public void reset(final long value) {
            for (LongHashMap.Entry<String> entry : timestamps.entrySet()) {
                entry.setValue(value);
            }
        }

        @Override
        public void remove(@NonNull final Iterable<? extends String> relativePaths) {
            for (String relPath : relativePaths) {
                timestamps.remove(relPath);
            }
        }

        @NonNull
        @Override
        public Collection<? extends String> getEnclosedFiles(@NonNull final String relativePath) {
            final Set<String> res = new HashSet<>();
            for (String filePath : timestamps.keySet()) {
                if (filePath.startsWith(relativePath)) {
                    res.add(filePath);
                }
            }
            return res;
        }

        @Override
        public void store() throws IOException {
            final File cacheDir = FileUtil.toFile(CacheFolder.getDataFolder(
                root,
                EnumSet.of(CacheFolderProvider.Kind.SOURCES, CacheFolderProvider.Kind.LIBRARIES),
                CacheFolderProvider.Mode.CREATE));
            final File f = new File(cacheDir, TIME_STAMPS_FILE);
            assert f != null;
            try {
                try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
                    if (unseen != null) {
                        timestamps.keySet().removeAll(unseen);
                    }

                    // write version
                    out.write(VERSION_3); //NOI18N
                    out.write(" ");
                    out.write(IndexabilityQuery.getInstance().getState());
                    out.newLine();

                    // write data
                    for(LongHashMap.Entry<String> entry : timestamps.entrySet()) {
                        out.write(entry.getKey());
                        out.write('='); //NOI18N
                        out.write(Long.toString(entry.getValue()));
                        out.newLine();
                    }

                    out.flush();
                }
            } catch (IOException e) {
                //In case of IOException props are not stored, everything is scanned next time
                LOG.log(Level.FINE, null, e);
            }
        }

        private void load () throws IOException {
            final FileObject cacheFolder = CacheFolder.getDataFolder(
                root,
                EnumSet.of(CacheFolderProvider.Kind.SOURCES, CacheFolderProvider.Kind.LIBRARIES),
                CacheFolderProvider.Mode.EXISTENT);
            if (cacheFolder != null) {
                final File cacheDir = FileUtil.toFile(cacheFolder);
                final File f = new File (cacheDir, TIME_STAMPS_FILE);
                if (f.exists()) {
                    try {
                        boolean readOldPropertiesFormat = false;
                        {
                            try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                                String line = in.readLine();
                                if (line != null && line.startsWith(VERSION_2)) {
                                    // it's the new format
                                    LOG.log(Level.FINE, "{0}: reading {1} timestamps", new Object [] { f.getPath(), VERSION_2 }); //NOI18N

                                    if (IndexabilityQuery.getInstance().isSameState("")) {
                                        while (null != (line = in.readLine())) {
                                            int idx = line.indexOf('='); //NOI18N
                                            if (idx != -1) {
                                                try {
                                                    final String path = line.substring(0, idx);
                                                    if (!path.isEmpty() && path.charAt(0) != '/') {
                                                        final long ts = Long.parseLong(line.substring(idx + 1));
                                                        timestamps.put(path, ts);
                                                    } else {
                                                        LOG.log(
                                                                Level.WARNING,
                                                                "Invalid timestamp entry {0} in {1}", //NOI18N
                                                                new Object[]{
                                                                    path,
                                                                    f.getAbsolutePath()
                                                                });
                                                    }
                                                } catch (NumberFormatException nfe) {
                                                    LOG.log(Level.FINE, "Invalid timestamp: line={0}, timestamps={1}, exception={2}", new Object[]{line, f.getPath(), nfe}); //NOI18N
                                                }
                                            }
                                        }
                                    }
                                } else if (line != null && line.startsWith(VERSION_3)) {
                                    // it's the new format
                                    LOG.log(Level.FINE, "{0}: reading {1} timestamps", new Object[]{f.getPath(), VERSION_3}); //NOI18N

                                    String state = line.substring(VERSION_3.length());

                                    if (IndexabilityQuery.getInstance().isSameState(state)) {
                                        while (null != (line = in.readLine())) {
                                            int idx = line.indexOf('='); //NOI18N
                                            if (idx != -1) {
                                                try {
                                                    final String path = line.substring(0, idx);
                                                    if (!path.isEmpty() && path.charAt(0) != '/') {
                                                        final long ts = Long.parseLong(line.substring(idx + 1));
                                                        timestamps.put(path, ts);
                                                    } else {
                                                        LOG.log(
                                                                Level.WARNING,
                                                                "Invalid timestamp entry {0} in {1}", //NOI18N
                                                                new Object[]{
                                                                    path,
                                                                    f.getAbsolutePath()
                                                                });
                                                    }
                                                } catch (NumberFormatException nfe) {
                                                    LOG.log(Level.FINE, "Invalid timestamp: line={0}, timestamps={1}, exception={2}", new Object[]{line, f.getPath(), nfe}); //NOI18N
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // it's the old format from Properties.store()
                                    readOldPropertiesFormat = true;
                                }
                            }
                        }

                        if (readOldPropertiesFormat) {
                            LOG.log(Level.FINE, "{0}: reading old Properties timestamps", f.getPath()); //NOI18N
                            final Properties p = new Properties();
                            try (InputStream in = new FileInputStream(f)) {
                                p.load(in);
                            }

                            for(Map.Entry<Object, Object> entry : p.entrySet()) {
                                try {
                                    final String fileId = (String) entry.getKey();
                                    if (fileId != null) {
                                        timestamps.put(fileId, Long.parseLong((String) entry.getValue()));
                                    }
                                } catch (NumberFormatException nfe) {
                                    LOG.log(Level.FINE, "Invalid timestamp: key={0}, value={1}, timestamps={2}, exception={3}", //NOI18N
                                            new Object[] { entry.getKey(), entry.getValue(), f, nfe });
                                }
                            }
                        }

                        if (unseen != null) {
                            for (Object k : timestamps.keySet()) {
                                unseen.add((String)k);
                            }
                        }

                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.log(Level.FINEST, "Timestamps loaded from {0}:", f.getPath()); //NOI18N
                            for(LongHashMap.Entry<String> entry : timestamps.entrySet()) {
                                LOG.log(Level.FINEST, "{0}={1}", new Object [] { entry.getKey(), Long.toString(entry.getValue()) }); //NOI18N
                            }
                            LOG.log(Level.FINEST, "---------------------------"); //NOI18N
                        }
                    } catch (IOException | RuntimeException e) {
                        // #176001: catching all exceptions, because j.u.Properties can throw IllegalArgumentException
                        // from its load() method
                        // In case of any exception props are empty, everything is scanned
                        timestamps.clear();
                        LOG.log(Level.FINE, null, e);
                    }
                }
            }
        }
    }

    private static class AllChangedTransientImpl implements Implementation {

        @Override
        public boolean checkAndStoreTimestamp(FileObject root, String relativePath) {
            return false;
        }

        @Override
        public Set<String> getUnseenFiles() {
            return Collections.<String>emptySet();
        }

        @Override
        public void reset(long time) {
        }

        @Override
        public void remove(@NonNull final Iterable<? extends String> relativePaths) {
        }

        @NonNull
        @Override
        public Collection<? extends String> getEnclosedFiles(@NonNull final String relativePath) {
            return Collections.<String>emptySet();
        }

        @Override
        public void store() throws IOException {
        }
    }

}
